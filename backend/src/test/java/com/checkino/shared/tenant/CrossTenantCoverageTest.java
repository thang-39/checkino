package com.checkino.shared.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.mvc.condition.PathPatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Cơ chế 2, lớp 2 — the self-detecting guard (M1-S04, AC #2). See DECISIONS.md § Ba cơ chế — 2,
 * "Cách lớp 2 hạ cánh".
 *
 * <p>Enumerates every registered URL pattern from {@link RequestMappingHandlerMapping} and asserts
 * each one is EITHER explicitly public (not org-scoped) OR has a cross-tenant test. A new endpoint
 * that is neither fails this test — which is the operational meaning of "an endpoint with no
 * cross-tenant test is not done". The two lists are the only thing an author edits: after writing a
 * cross-tenant test (extend {@link CrossTenantTestSupport}), add the pattern to {@link
 * #CROSS_TENANT_COVERED}; if the endpoint genuinely carries no org data, add it to {@link
 * #PUBLIC_ALLOWLIST} with a reason.
 *
 * <p>Extends the harness only to reuse its booted context + shared container (a full context is
 * needed to see the real mappings without mocking each controller's dependencies); it does not use
 * the seeded data.
 */
class CrossTenantCoverageTest extends CrossTenantTestSupport {

  /**
   * Endpoints that carry NO org-scoped data, so cross-tenant isolation does not apply. Today: the
   * SPA forwards from {@code SpaFallbackController} (static shell, no data) and Boot's error page.
   */
  static final Set<String> PUBLIC_ALLOWLIST =
      Set.of("/", "/admin", "/admin/**", "/staff", "/staff/**", "/error");

  /**
   * Endpoints that DO serve org-scoped data and have a cross-tenant isolation test proving org A
   * cannot reach org B. Empty today — no data endpoint exists yet (auth is M1-S05). Grows with
   * every endpoint story.
   */
  static final Set<String> CROSS_TENANT_COVERED = Set.of();

  @Autowired private RequestMappingHandlerMapping handlerMapping;

  @Test
  void everyEndpointIsPublicOrHasCrossTenantTest() {
    Set<String> allPatterns = new TreeSet<>();
    for (RequestMappingInfo info : handlerMapping.getHandlerMethods().keySet()) {
      PathPatternsRequestCondition patterns = info.getPathPatternsCondition();
      if (patterns != null) {
        allPatterns.addAll(patterns.getPatternValues());
      }
    }

    Set<String> uncovered = new TreeSet<>(allPatterns);
    uncovered.removeAll(PUBLIC_ALLOWLIST);
    uncovered.removeAll(CROSS_TENANT_COVERED);

    assertThat(uncovered)
        .as(
            "Endpoint chưa được phủ cô lập cross-tenant (cơ chế 2, lớp 2). Mỗi endpoint gắn org"
                + " PHẢI có test A-không-thấy-B → thêm pattern vào CROSS_TENANT_COVERED sau khi viết"
                + " test (extend CrossTenantTestSupport). Nếu endpoint KHÔNG gắn org thì thêm vào"
                + " PUBLIC_ALLOWLIST kèm lý do. Đang thiếu: %s",
            uncovered)
        .isEmpty();
  }
}
