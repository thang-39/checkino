/**
 * Tenant và cấu hình của tenant: org, scan_point, program, giờ mở cửa, GPS.
 *
 * <p>{@code program} (bộ môn) nằm ở đây chứ không ở {@code member}: nó là một phần của việc
 * chủ cấu hình cơ sở, được tạo trong wizard F1, và {@code scan_point.program_id} trỏ tới nó
 * (D7). Bảng nối {@code member_program} thì thuộc {@code member}.
 */
package com.checkino.org;
