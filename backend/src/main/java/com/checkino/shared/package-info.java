/**
 * Shared infrastructure: config, the RLS interceptor, SSE, audit log.
 *
 * <p>{@code audit_log} lives here because every module writes to it — placing it in any one
 * business module would break the "a module does not reach into another's repository" rule
 * (D5, D10).
 */
package com.checkino.shared;
