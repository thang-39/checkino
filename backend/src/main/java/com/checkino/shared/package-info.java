/**
 * Hạ tầng dùng chung: config, RLS interceptor, SSE, audit log.
 *
 * <p>{@code audit_log} nằm ở đây vì mọi module đều ghi vào nó — đặt trong bất kỳ module nghiệp
 * vụ nào cũng phá quy tắc "module không chọc vào repository của nhau" (D5, D10).
 */
package com.checkino.shared;
