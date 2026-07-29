/**
 * Hàng chờ gửi tin: notification_outbox, Zalo OA/ZNS (gói Pro).
 *
 * <p>Bảng {@code notification_outbox} CHÍNH LÀ hàng chờ — một {@code @Scheduled} quét bảng là
 * đủ ở quy mô 1–2 write/giây. Không dựng message broker, không tách service (D5).
 * Zalo thuộc Pro, free tier chạy hoàn toàn không cần Zalo (D1).
 */
package com.checkino.notification;
