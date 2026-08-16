package com.checkino.shared.tenant;

import java.util.UUID;

/**
 * Holds the org (tenant) of the current request/thread. {@link OrgRlsAspect} reads it at the start
 * of each transaction and pushes it into Postgres as the GUC {@code app.org_id}, which the RLS
 * policy {@code org_isolation} (migration V2) uses to filter every row. Cơ chế 2, lớp 1.
 *
 * <p>M1-S03 only wires the plumbing. WHO sets the context is M1-S05 (magic-link auth): that story's
 * filter resolves the logged-in staff's org, calls {@link #set(UUID)}, then {@link #clear()} when
 * the request ends. Until then only tests set it.
 */
public final class OrgContext {

  private static final ThreadLocal<UUID> CURRENT = new ThreadLocal<>();

  private OrgContext() {}

  public static void set(UUID orgId) {
    CURRENT.set(orgId);
  }

  public static UUID get() {
    return CURRENT.get();
  }

  public static void clear() {
    CURRENT.remove();
  }
}
