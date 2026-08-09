package com.checkino;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * The app boots against a real Postgres and Flyway completes.
 *
 * <p>M1-S02 added {@code V1__core_schema.sql}: the assertions below reflect that one migration has
 * been applied and the business tables now exist. Detailed schema checks live in
 * {@link com.checkino.db.SchemaMigrationV1Test}.
 *
 * <p><strong>Why the "Flyway is wired" assertion exists:</strong> Boot 4 modularises
 * autoconfiguration. If the pom only has bare {@code flyway-core} instead of
 * {@code spring-boot-starter-flyway}, Flyway is <em>not</em> wired and the migration silently does
 * not run — the app still boots green, with no error to see. This assertion is the trap for that.
 */
@SpringBootTest
@Testcontainers
class ApplicationContextTest {

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>(DockerImageName.parse("postgres:18-alpine"));

  @Autowired private DataSource dataSource;
  @Autowired private Flyway flyway;

  @Test
  void flywayIsWiredAndRunsWithoutError() {
    assertThat(flyway).as("Flyway must be autoconfigured").isNotNull();
    assertThat(flyway.info().applied())
        .as("M1-S02 added V1__core_schema.sql — exactly 1 migration must be applied")
        .hasSize(1);
    assertThat(flyway.info().current().getVersion().getVersion())
        .as("current migration is V1")
        .isEqualTo("1");
  }

  @Test
  void flywayCreatedItsHistoryTable() throws Exception {
    assertThat(tableNames())
        .as("Flyway creates its history table")
        .contains("flyway_schema_history");
  }

  @Test
  void businessTablesExist() throws Exception {
    assertThat(tableNames())
        .as("M1-S02 creates 12 core tables + the Flyway history table")
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
            "notification_outbox",
            "flyway_schema_history");
  }

  private java.util.List<String> tableNames() throws Exception {
    var names = new java.util.ArrayList<String>();
    try (var conn = dataSource.getConnection();
        var st = conn.createStatement();
        var rs =
            st.executeQuery(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'"
                    + " ORDER BY table_name")) {
      while (rs.next()) {
        names.add(rs.getString(1));
      }
    }
    return names;
  }

  @Test
  void postgresIsVersion18() throws Exception {
    try (var conn = dataSource.getConnection();
        var st = conn.createStatement();
        var rs = st.executeQuery("SHOW server_version")) {
      rs.next();
      assertThat(rs.getString(1)).as("D4 pins Postgres 18").startsWith("18");
    }
  }
}
