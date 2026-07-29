/**
 * Sự kiện điểm danh: checkin_event và dedupe bucket.
 *
 * <p>Chống trùng bằng {@code UNIQUE INDEX (member_id, scan_point_id, dedupe_bucket)} +
 * {@code ON CONFLICT DO NOTHING}. TUYỆT ĐỐI không kiểm bằng {@code if (!exists)} ở tầng app
 * (cơ chế 1).
 */
package com.checkino.checkin;
