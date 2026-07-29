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
 * <p>{@code db/migration} cố ý rỗng ở M1-S01 — {@code V1__core_schema.sql} là việc của M1-S02.
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
    assertThat(flyway.info().all())
        .as("M1-S01 chưa có migration nào; M1-S02 sẽ thêm V1__core_schema.sql")
        .isEmpty();
  }

  @Test
  void flywayDaDungBangLichSu() throws Exception {
    assertThat(tableNames())
        .as("Flyway dựng bảng lịch sử ngay cả khi 0 migration")
        .contains("flyway_schema_history");
  }

  @Test
  void chuaCoBangNghiepVuNao() throws Exception {
    assertThat(tableNames())
        .as("M1-S01 không dựng bảng nghiệp vụ — đó là việc của M1-S02")
        .containsExactly("flyway_schema_history");
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
