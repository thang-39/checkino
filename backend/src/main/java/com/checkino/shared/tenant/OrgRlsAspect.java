package com.checkino.shared.tenant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.UUID;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Cơ chế 2, lớp 1 — the interceptor half. At the start of every transaction it pushes {@link
 * OrgContext}'s org into the Postgres GUC {@code app.org_id} via {@code set_config(..., true)} (=
 * SET LOCAL, so it is scoped to this transaction only). The RLS policy from migration V2 then
 * filters every table by it.
 *
 * <p>No org in context → nothing is set → {@code current_setting('app.org_id', true)} stays NULL →
 * every RLS-protected query returns EMPTY, never an error. See DECISIONS.md § Ba cơ chế — 2.
 *
 * <p>ORDERING TRAP: this aspect MUST run INSIDE the transaction (after BEGIN); otherwise SET LOCAL
 * lands outside a transaction and is silently discarded. {@link TenantConfig} pins the transaction
 * advisor to order 100 so this aspect (order 200) runs within it.
 */
@Aspect
@Component
@Order(200)
public class OrgRlsAspect {

  @PersistenceContext private EntityManager entityManager;

  @Around(
      "@annotation(org.springframework.transaction.annotation.Transactional)"
          + " || @within(org.springframework.transaction.annotation.Transactional)")
  public Object applyOrgId(ProceedingJoinPoint pjp) throws Throwable {
    UUID orgId = OrgContext.get();
    if (orgId != null) {
      // set_config(name, value, is_local=true) == SET LOCAL: transaction-scoped AND param-bindable.
      // SET LOCAL itself cannot bind params → set_config avoids string-concatenated SQL.
      entityManager
          .createNativeQuery("SELECT set_config('app.org_id', :orgId, true)")
          .setParameter("orgId", orgId.toString())
          .getSingleResult();
    }
    return pjp.proceed();
  }
}
