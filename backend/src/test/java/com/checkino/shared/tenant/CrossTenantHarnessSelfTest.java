package com.checkino.shared.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Proves the {@link CrossTenantTestSupport} harness itself works (M1-S04) — not RLS per se (that is
 * M1-S03's {@code RlsOrgIsolationTest} / {@code OrgRlsAspectIntegrationTest}), but that the reusable
 * base future endpoint tests build on actually seeds two orgs and isolates them through {@link
 * #actingAsOrg}. Without this the base would be untested scaffolding.
 */
class CrossTenantHarnessSelfTest extends CrossTenantTestSupport {

  @Test
  void eachOrgSeesOnlyItsOwnMember() {
    assertThat(actingAsOrg(ORG_A, () -> seed.countMembers())).isEqualTo(1);
    assertThat(actingAsOrg(ORG_B, () -> seed.countMembers())).isEqualTo(1);
  }

  @Test
  void orgACannotReadOrgBData() {
    assertThat(actingAsOrg(ORG_A, () -> seed.memberIds())).containsExactly(MEMBER_A);
    assertThat(actingAsOrg(ORG_B, () -> seed.memberIds())).containsExactly(MEMBER_B);
  }

  @Test
  void noOrgContextSeesNothing() {
    // The "403 hoặc RỖNG" rule: no org set → RLS returns empty, never an error.
    OrgContext.clear();
    assertThat(seed.countMembers()).as("no org → empty, not exception").isZero();
  }
}
