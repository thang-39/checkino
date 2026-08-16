package com.checkino.shared.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Cơ chế 2, lớp 1 — the interceptor half, end-to-end (M1-S03, AC #2).
 *
 * <p>Boots the real Spring context connecting as the non-superuser role {@code checkino_app} (so RLS
 * is in force), while Flyway runs as the privileged role to create that role + schema. Then, through
 * a genuinely {@code @Transactional} bean, proves that {@link OrgRlsAspect} pushes {@link OrgContext}
 * into {@code app.org_id} at the start of the transaction, and that RLS then filters accordingly.
 */
@SpringBootTest
@Testcontainers
class OrgRlsAspectIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>(DockerImageName.parse("postgres:18-alpine"));

  @DynamicPropertySource
  static void datasource(DynamicPropertyRegistry r) {
    // App runtime connects as checkino_app (RLS enforced)...
    r.add("spring.datasource.url", postgres::getJdbcUrl);
    r.add("spring.datasource.username", () -> "checkino_app");
    r.add("spring.datasource.password", () -> "checkino_app");
    // ...Flyway/DDL runs as the container superuser to create the role + schema first.
    r.add("spring.flyway.url", postgres::getJdbcUrl);
    r.add("spring.flyway.user", postgres::getUsername);
    r.add("spring.flyway.password", postgres::getPassword);
  }

  @Autowired private TenantProbe probe;

  @AfterEach
  void clearContext() {
    OrgContext.clear();
  }

  @Test
  void aspectPushesOrgContextIntoGucInsideTransaction() {
    UUID org = UUID.randomUUID();
    OrgContext.set(org);
    assertThat(probe.currentOrgSetting())
        .as("AC #2: interceptor set app.org_id at the start of the tx")
        .isEqualTo(org.toString());
  }

  @Test
  void noOrgContextLeavesGucUnset() {
    OrgContext.clear();
    assertThat(probe.currentOrgSetting())
        .as("no org in context → GUC stays empty (→ AC #3: RLS returns empty, not error)")
        .isEmpty();
  }

  @Test
  void rlsIsolatesTenantsThroughTheRealAspect() {
    UUID orgA = UUID.randomUUID();
    UUID orgB = UUID.randomUUID();

    OrgContext.set(orgA);
    probe.insertOrgAndMember(orgA, UUID.randomUUID(), "0911000001");
    OrgContext.set(orgB);
    probe.insertOrgAndMember(orgB, UUID.randomUUID(), "0911000002");

    OrgContext.set(orgA);
    assertThat(probe.countMembers()).as("org A sees only its own member").isEqualTo(1);
    OrgContext.set(orgB);
    assertThat(probe.countMembers()).as("org B sees only its own member").isEqualTo(1);
    OrgContext.clear();
    assertThat(probe.countMembers()).as("no org → sees nothing (AC #3)").isZero();
  }

  /**
   * A minimal {@code @Transactional} bean so the aspect has a real transaction boundary to wrap.
   * Uses native SQL because M1-S03 has no JPA entities yet. Registered via {@link Config}; the
   * transactional proxy is what {@link OrgRlsAspect} wraps.
   */
  static class TenantProbe {

    @PersistenceContext private EntityManager em;

    @Transactional
    public String currentOrgSetting() {
      return (String) em.createNativeQuery("SELECT current_setting('app.org_id', true)")
          .getSingleResult();
    }

    @Transactional
    public long countMembers() {
      return ((Number) em.createNativeQuery("SELECT count(*) FROM member").getSingleResult())
          .longValue();
    }

    @Transactional
    public void insertOrgAndMember(UUID org, UUID member, String phone) {
      em.createNativeQuery("INSERT INTO org (id, name) VALUES (?, ?)")
          .setParameter(1, org)
          .setParameter(2, "Org " + org)
          .executeUpdate();
      em.createNativeQuery(
              "INSERT INTO member (id, org_id, name, phone_normalized) VALUES (?, ?, ?, ?)")
          .setParameter(1, member)
          .setParameter(2, org)
          .setParameter(3, "M-" + member)
          .setParameter(4, phone)
          .executeUpdate();
    }
  }

  @TestConfiguration
  static class Config {
    @Bean
    TenantProbe tenantProbe() {
      return new TenantProbe();
    }
  }
}
