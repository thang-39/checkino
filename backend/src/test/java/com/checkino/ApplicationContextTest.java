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
 * App boot được với Postgres thật, và Flyway chạy xong dù chưa có migration nào.
 *
 * <p>M1-S02 đã thêm {@code V1__core_schema.sql}: các assertion dưới phản ánh đã có 1 migration
 * và bảng nghiệp vụ đã tồn tại. Kiểm schema chi tiết nằm ở {@link SchemaMigrationV1Test}.
 *
 * <p><strong>Vì sao có assertion "Flyway được wire":</strong> Boot 4 modularise
 * autoconfiguration. Nếu pom chỉ có {@code flyway-core} trần thay vì
 * {@code spring-boot-starter-flyway} thì Flyway <em>không</em> được wire và migration im lặng
 * không chạy — app vẫn boot xanh, không có lỗi nào để thấy. Assertion này là cái bẫy đó.
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
  void flywayDuocWireVaChayKhongLoi() {
    assertThat(flyway).as("Flyway phải được autoconfigure").isNotNull();
    assertThat(flyway.info().applied())
        .as("M1-S02 đã thêm V1__core_schema.sql — phải có đúng 1 migration đã áp dụng")
        .hasSize(1);
    assertThat(flyway.info().current().getVersion().getVersion())
        .as("migration hiện tại là V1")
        .isEqualTo("1");
  }

  @Test
  void flywayDaDungBangLichSu() throws Exception {
    assertThat(tableNames())
        .as("Flyway dựng bảng lịch sử ngay cả khi 0 migration")
        .contains("flyway_schema_history");
  }

  @Test
  void daDungBangNghiepVuLoi() throws Exception {
    assertThat(tableNames())
        .as("M1-S02 dựng 12 bảng lõi + bảng lịch sử Flyway")
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
  void postgresLaPhienBan18() throws Exception {
    try (var conn = dataSource.getConnection();
        var st = conn.createStatement();
        var rs = st.executeQuery("SHOW server_version")) {
      rs.next();
      assertThat(rs.getString(1)).as("D4 chốt Postgres 18").startsWith("18");
    }
  }
}
