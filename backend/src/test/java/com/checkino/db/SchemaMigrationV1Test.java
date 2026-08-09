package com.checkino.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Chạy Flyway thật lên một Postgres 18 trắng rồi soi schema qua information_schema / pg catalog.
 *
 * <p>Không dùng Spring context — story M1-S02 chỉ kiểm chính migration, không cần app boot. Đây
 * là bằng chứng cho AC "mvn flyway:migrate chạy sạch trên DB trắng": {@link #flywayChaySachTrenDbTrang()}
 * gọi {@code migrate()} trên container mới toanh và assert đúng V1 được áp dụng.
 */
@Testcontainers
class SchemaMigrationV1Test {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>(DockerImageName.parse("postgres:18-alpine"));

  static Flyway flyway;

  @BeforeAll
  static void migrate() {
    flyway =
        Flyway.configure()
            .dataSource(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
            .locations("classpath:db/migration")
            .load();
    flyway.migrate();
  }

  @Test
  void flywayChaySachTrenDbTrang() {
    // migrate() ở @BeforeAll đã chạy không ném lỗi; đây là bằng chứng "chạy sạch trên DB trắng".
    assertThat(flyway.info().applied())
        .as("đúng 1 migration được áp dụng")
        .hasSize(1);
    assertThat(flyway.info().current().getVersion().getVersion())
        .as("migration hiện tại là V1")
        .isEqualTo("1");
  }

  @Test
  void du12BangLoi() throws Exception {
    assertThat(tableNames())
        .as("AC #1 + #4: 8 bảng lõi + 4 bảng khung tối thiểu")
        .contains(
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
  }

  @Test
  void memberCoUniqueOrgPhone_khongUniquePhoneMotMinh() throws Exception {
    // AC #2 (D8): UNIQUE (org_id, phone_normalized), KHÔNG unique toàn cục trên phone.
    List<List<String>> uniqueSets = uniqueColumnSetsOf("member");
    assertThat(uniqueSets)
        .as("member phải có UNIQUE đúng bộ (org_id, phone_normalized)")
        .contains(List.of("org_id", "phone_normalized"));
    assertThat(uniqueSets)
        .as("D8: KHÔNG được có UNIQUE chỉ trên phone_normalized — cùng SĐT được ở 2 org")
        .doesNotContain(List.of("phone_normalized"));
  }

  @Test
  void auditLogDu8Cot() throws Exception {
    assertThat(columnNamesOf("audit_log"))
        .as("AC #3 (D10): audit_log đủ đúng 8 cột")
        .containsExactlyInAnyOrder(
            "id",
            "org_id",
            "actor_staff_user_id",
            "action",
            "entity_type",
            "entity_id",
            "summary",
            "created_at");
  }

  @Test
  void scanPointProgramIdNullable() throws Exception {
    // AC #1 (D7): NULL = QR dùng chung cả cơ sở.
    assertThat(isNullable("scan_point", "program_id"))
        .as("scan_point.program_id phải NULLABLE (D7)")
        .isTrue();
  }

  // ── helpers ─────────────────────────────────────────────────────────────────

  private static Connection conn() throws Exception {
    return java.sql.DriverManager.getConnection(
        postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
  }

  private List<String> tableNames() throws Exception {
    var names = new ArrayList<String>();
    try (var c = conn();
        var st = c.createStatement();
        ResultSet rs =
            st.executeQuery(
                "SELECT table_name FROM information_schema.tables"
                    + " WHERE table_schema = 'public' ORDER BY table_name")) {
      while (rs.next()) {
        names.add(rs.getString(1));
      }
    }
    return names;
  }

  private List<String> columnNamesOf(String table) throws Exception {
    var names = new ArrayList<String>();
    try (var c = conn();
        var ps =
            c.prepareStatement(
                "SELECT column_name FROM information_schema.columns"
                    + " WHERE table_schema = 'public' AND table_name = ?")) {
      ps.setString(1, table);
      try (var rs = ps.executeQuery()) {
        while (rs.next()) {
          names.add(rs.getString(1));
        }
      }
    }
    return names;
  }

  private boolean isNullable(String table, String column) throws Exception {
    try (var c = conn();
        var ps =
            c.prepareStatement(
                "SELECT is_nullable FROM information_schema.columns"
                    + " WHERE table_schema = 'public' AND table_name = ? AND column_name = ?")) {
      ps.setString(1, table);
      ps.setString(2, column);
      try (var rs = ps.executeQuery()) {
        assertThat(rs.next()).as("cột %s.%s phải tồn tại", table, column).isTrue();
        return "YES".equals(rs.getString(1));
      }
    }
  }

  /** Mỗi phần tử = tập cột của một ràng buộc UNIQUE (hoặc PK) trên bảng, đã sort theo tên cột. */
  private List<List<String>> uniqueColumnSetsOf(String table) throws Exception {
    var byConstraint = new java.util.LinkedHashMap<String, List<String>>();
    try (var c = conn();
        var ps =
            c.prepareStatement(
                "SELECT tc.constraint_name, kcu.column_name"
                    + " FROM information_schema.table_constraints tc"
                    + " JOIN information_schema.key_column_usage kcu"
                    + "   ON tc.constraint_name = kcu.constraint_name"
                    + "  AND tc.table_schema = kcu.table_schema"
                    + " WHERE tc.table_schema = 'public' AND tc.table_name = ?"
                    + "   AND tc.constraint_type = 'UNIQUE'"
                    + " ORDER BY tc.constraint_name, kcu.column_name")) {
      ps.setString(1, table);
      try (var rs = ps.executeQuery()) {
        while (rs.next()) {
          byConstraint
              .computeIfAbsent(rs.getString(1), k -> new ArrayList<>())
              .add(rs.getString(2));
        }
      }
    }
    return new ArrayList<>(byConstraint.values());
  }
}
