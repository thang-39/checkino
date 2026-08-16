package com.checkino.shared.tenant;

import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Cơ chế 2, lớp 2 — reusable base for cross-tenant isolation tests (M1-S04). See DECISIONS.md §
 * Ba cơ chế — 2, "Cách lớp 2 hạ cánh".
 *
 * <p>Every future endpoint story (M1-S05+) extends this: it seeds two orgs (A and B) with one
 * member each, boots Spring connecting as the non-superuser role {@code checkino_app} (so RLS is in
 * force) while Flyway runs as the privileged role, and hands subclasses {@link #actingAsOrg} to run
 * work "as" a given org. A subclass then proves org A cannot read/write org B's data (assert 403 or
 * empty), and registers its endpoint pattern in {@code CrossTenantCoverageTest.CROSS_TENANT_COVERED}
 * so the coverage guard goes green.
 *
 * <p>Container is a SINGLETON (started once per JVM, never stopped here) so all subclasses share one
 * Postgres and Spring caches a single context — the identical {@code @SpringBootTest} config across
 * subclasses is what makes that caching kick in. Ryuk (disabled locally for podman) reaps it on JVM
 * exit.
 *
 * <p>"Token org A" at the HTTP layer arrives with auth in M1-S05; until then the acting-as-org
 * primitive is {@link OrgContext} (thread-local), which is enough for service/repository-level
 * isolation tests. MockMvc-based subclasses will authenticate instead once the auth filter exists.
 */
@SpringBootTest
@Import(CrossTenantTestSupport.SeedProbe.class)
public abstract class CrossTenantTestSupport {

  protected static final UUID ORG_A = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
  protected static final UUID ORG_B = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
  protected static final UUID MEMBER_A = UUID.fromString("a1111111-1111-1111-1111-111111111111");
  protected static final UUID MEMBER_B = UUID.fromString("b1111111-1111-1111-1111-111111111111");

  // Singleton container — see class javadoc. No @Container/@Testcontainers on purpose.
  protected static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>(DockerImageName.parse("postgres:18-alpine"));

  static {
    POSTGRES.start();
  }

  @DynamicPropertySource
  static void datasource(DynamicPropertyRegistry r) {
    // App runtime as checkino_app → RLS enforced; Flyway/DDL as the container superuser.
    r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    r.add("spring.datasource.username", () -> "checkino_app");
    r.add("spring.datasource.password", () -> "checkino_app");
    r.add("spring.flyway.url", POSTGRES::getJdbcUrl);
    r.add("spring.flyway.user", POSTGRES::getUsername);
    r.add("spring.flyway.password", POSTGRES::getPassword);
  }

  @Autowired protected SeedProbe seed;

  /** Seed org A and org B (idempotent) before each test, then leave the context cleared. */
  @BeforeEach
  void seedBothOrgs() {
    actingAsOrg(ORG_A, () -> seed.insertOrgAndMember(ORG_A, MEMBER_A, "0900000001"));
    actingAsOrg(ORG_B, () -> seed.insertOrgAndMember(ORG_B, MEMBER_B, "0900000002"));
    OrgContext.clear();
  }

  @AfterEach
  void clearContext() {
    OrgContext.clear();
  }

  /** Run {@code body} as if the request belonged to {@code org} — the RLS aspect scopes every query. */
  protected <T> T actingAsOrg(UUID org, Supplier<T> body) {
    OrgContext.set(org);
    try {
      return body.get();
    } finally {
      OrgContext.clear();
    }
  }

  protected void actingAsOrg(UUID org, Runnable body) {
    actingAsOrg(
        org,
        () -> {
          body.run();
          return null;
        });
  }

  /**
   * Minimal {@code @Transactional} bean so {@link OrgRlsAspect} has a real transaction boundary to
   * wrap. Native SQL because M1-S04 has no JPA entities yet. Seeding uses {@code ON CONFLICT DO
   * NOTHING} so {@link #seedBothOrgs} is safe to run before every test.
   */
  public static class SeedProbe {

    @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager em;

    @org.springframework.transaction.annotation.Transactional
    public void insertOrgAndMember(UUID org, UUID member, String phone) {
      em.createNativeQuery("INSERT INTO org (id, name) VALUES (?, ?) ON CONFLICT (id) DO NOTHING")
          .setParameter(1, org)
          .setParameter(2, "Org " + org)
          .executeUpdate();
      em.createNativeQuery(
              "INSERT INTO member (id, org_id, name, phone_normalized) VALUES (?, ?, ?, ?)"
                  + " ON CONFLICT (id) DO NOTHING")
          .setParameter(1, member)
          .setParameter(2, org)
          .setParameter(3, "M-" + member)
          .setParameter(4, phone)
          .executeUpdate();
    }

    @org.springframework.transaction.annotation.Transactional
    public long countMembers() {
      return ((Number) em.createNativeQuery("SELECT count(*) FROM member").getSingleResult())
          .longValue();
    }

    @org.springframework.transaction.annotation.Transactional
    public java.util.List<UUID> memberIds() {
      @SuppressWarnings("unchecked")
      java.util.List<UUID> ids =
          em.createNativeQuery("SELECT id FROM member ORDER BY id").getResultList();
      return ids;
    }
  }
}
