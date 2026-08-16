package com.checkino.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Cơ chế 2, lớp 1 — DB contract test (M1-S03). Runs the real migrations, seeds two orgs as the
 * superuser (which bypasses RLS), then connects as the non-superuser app role {@code checkino_app}
 * and proves the {@code org_isolation} policy from V2 actually isolates tenants.
 *
 * <p>No Spring here on purpose: this is the pure Postgres-layer contract the interceptor + M1-S04's
 * cross-tenant endpoint suite build on. The aspect/interceptor half is proven end-to-end in {@link
 * com.checkino.shared.tenant.OrgRlsAspectIntegrationTest}.
 *
 * <p>Maps to AC: policy on every org_id table + org (#1), unset org → empty not exception (#3), app
 * role has no BYPASSRLS (#4); plus WITH CHECK on writes and the transitive policy for join tables.
 */
@Testcontainers
class RlsOrgIsolationTest {

  private static final UUID ORG_A = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
  private static final UUID ORG_B = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
  private static final UUID MEMBER_A = UUID.fromString("a1111111-1111-1111-1111-111111111111");
  private static final UUID MEMBER_B = UUID.fromString("b1111111-1111-1111-1111-111111111111");

  private static final String APP_USER = "checkino_app";
  private static final String APP_PASSWORD = "checkino_app";

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>(DockerImageName.parse("postgres:18-alpine"));

  @BeforeAll
  static void migrateAndSeed() throws Exception {
    Flyway.configure()
        .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
        .locations("classpath:db/migration")
        .load()
        .migrate();

    // Seed as the container superuser — superusers bypass RLS, so cross-org rows insert freely.
    try (Connection c = superuserConn();
        Statement st = c.createStatement()) {
      st.execute("INSERT INTO org (id, name) VALUES ('" + ORG_A + "', 'Org A')");
      st.execute("INSERT INTO org (id, name) VALUES ('" + ORG_B + "', 'Org B')");
      st.execute(
          "INSERT INTO member (id, org_id, name, phone_normalized) VALUES ('"
              + MEMBER_A
              + "', '"
              + ORG_A
              + "', 'Alice', '0900000001')");
      st.execute(
          "INSERT INTO member (id, org_id, name, phone_normalized) VALUES ('"
              + MEMBER_B
              + "', '"
              + ORG_B
              + "', 'Bob', '0900000002')");
      // member_device has NO org_id → isolated only via the transitive policy through member.
      st.execute(
          "INSERT INTO member_device (member_id, token_hash) VALUES ('"
              + MEMBER_A
              + "', 'hashA')");
    }
  }

  /** Every table must carry the org_isolation policy AND have RLS enabled + forced. */
  private static final List<String> ALL_TABLES =
      List.of(
          "org",
          "staff_user",
          "program",
          "scan_point",
          "member",
          "member_program",
          "member_device",
          "audit_log",
          "entitlement",
          "checkin_event",
          "lead",
          "notification_outbox");

  @Test
  void everyTableHasPolicyAndRlsForced() throws Exception {
    // AC #1: org_isolation on MỌI bảng — not just the ones exercised by the scoping tests.
    try (Connection c = superuserConn();
        Statement st = c.createStatement()) {
      try (ResultSet rs =
          st.executeQuery(
              "SELECT tablename FROM pg_policies"
                  + " WHERE schemaname = 'public' AND policyname = 'org_isolation'")) {
        var withPolicy = new ArrayList<String>();
        while (rs.next()) {
          withPolicy.add(rs.getString(1));
        }
        assertThat(withPolicy)
            .as("org_isolation policy on every table")
            .containsExactlyInAnyOrderElementsOf(ALL_TABLES);
      }
      try (ResultSet rs =
          st.executeQuery(
              "SELECT relname FROM pg_class"
                  + " WHERE relnamespace = 'public'::regnamespace"
                  + "   AND relrowsecurity AND relforcerowsecurity")) {
        var forced = new ArrayList<String>();
        while (rs.next()) {
          forced.add(rs.getString(1));
        }
        assertThat(forced)
            .as("RLS enabled + forced on every table (#2: force so owner cannot bypass)")
            .containsAll(ALL_TABLES);
      }
    }
  }

  @Test
  void unsetOrgReturnsEmpty_notException() throws Exception {
    // AC #3: query before SET app.org_id → 0 rows, and crucially NO exception.
    try (Connection c = appConn()) {
      c.setAutoCommit(false);
      try (Statement st = c.createStatement();
          ResultSet rs = st.executeQuery("SELECT count(*) FROM member")) {
        rs.next();
        assertThat(rs.getInt(1)).as("unset org → empty result set").isZero();
      }
      c.rollback();
    }
  }

  @Test
  void eachOrgSeesOnlyItsOwnRows() throws Exception {
    // AC #1: with the GUC set, RLS filters member to exactly that org.
    assertThat(memberIdsForOrg(ORG_A)).containsExactly(MEMBER_A);
    assertThat(memberIdsForOrg(ORG_B)).containsExactly(MEMBER_B);
  }

  @Test
  void withCheckBlocksInsertingIntoAnotherOrg() throws Exception {
    // WITH CHECK: while acting as org A, inserting a row tagged org B must be rejected.
    try (Connection c = appConn()) {
      c.setAutoCommit(false);
      setOrg(c, ORG_A);
      assertThatThrownBy(
              () -> {
                try (PreparedStatement ps =
                    c.prepareStatement(
                        "INSERT INTO member (org_id, name, phone_normalized)"
                            + " VALUES (?, 'Mallory', '0900000003')")) {
                  ps.setObject(1, ORG_B);
                  ps.executeUpdate();
                }
              })
          .isInstanceOf(SQLException.class)
          .hasMessageContaining("row-level security");
      c.rollback();
    }
  }

  @Test
  void memberDeviceIsolatedTransitivelyThroughMember() throws Exception {
    // Join table has no org_id — the transitive policy must still scope it by org.
    assertThat(deviceCountForOrg(ORG_A)).as("org A owns member A's device").isEqualTo(1);
    assertThat(deviceCountForOrg(ORG_B)).as("org B sees no device").isZero();
  }

  @Test
  void appRoleHasNoSuperuserNoBypassRls() throws Exception {
    // AC #4: the runtime role must not be able to bypass RLS.
    try (Connection c = superuserConn();
        PreparedStatement ps =
            c.prepareStatement(
                "SELECT rolsuper, rolbypassrls FROM pg_roles WHERE rolname = ?")) {
      ps.setString(1, APP_USER);
      try (ResultSet rs = ps.executeQuery()) {
        assertThat(rs.next()).as("role %s must exist", APP_USER).isTrue();
        assertThat(rs.getBoolean("rolsuper")).as("NOSUPERUSER").isFalse();
        assertThat(rs.getBoolean("rolbypassrls")).as("NOBYPASSRLS").isFalse();
      }
    }
  }

  // ── helpers ─────────────────────────────────────────────────────────────────

  private static Connection superuserConn() throws SQLException {
    return DriverManager.getConnection(
        postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
  }

  private static Connection appConn() throws SQLException {
    return DriverManager.getConnection(postgres.getJdbcUrl(), APP_USER, APP_PASSWORD);
  }

  /** SET LOCAL app.org_id via set_config; requires an open transaction (autoCommit off). */
  private static void setOrg(Connection c, UUID org) throws SQLException {
    try (PreparedStatement ps = c.prepareStatement("SELECT set_config('app.org_id', ?, true)")) {
      ps.setString(1, org.toString());
      ps.execute();
    }
  }

  private List<UUID> memberIdsForOrg(UUID org) throws SQLException {
    var ids = new ArrayList<UUID>();
    try (Connection c = appConn()) {
      c.setAutoCommit(false);
      setOrg(c, org);
      try (Statement st = c.createStatement();
          ResultSet rs = st.executeQuery("SELECT id FROM member ORDER BY id")) {
        while (rs.next()) {
          ids.add(rs.getObject("id", UUID.class));
        }
      }
      c.rollback();
    }
    return ids;
  }

  private int deviceCountForOrg(UUID org) throws SQLException {
    try (Connection c = appConn()) {
      c.setAutoCommit(false);
      setOrg(c, org);
      try (Statement st = c.createStatement();
          ResultSet rs = st.executeQuery("SELECT count(*) FROM member_device")) {
        rs.next();
        return rs.getInt(1);
      } finally {
        c.rollback();
      }
    }
  }
}
