/**
 * The send queue: notification_outbox, Zalo OA/ZNS (Pro tier).
 *
 * <p>The {@code notification_outbox} table IS the queue — a single {@code @Scheduled} sweep is
 * enough at a scale of 1–2 writes/second. No message broker, no separate service (D5). Zalo is
 * Pro; the free tier runs entirely without Zalo (D1).
 */
package com.checkino.notification;
