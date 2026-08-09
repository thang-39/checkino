/**
 * Check-in events: checkin_event and the dedupe bucket.
 *
 * <p>Deduplication is done with {@code UNIQUE INDEX (member_id, scan_point_id, dedupe_bucket)} +
 * {@code ON CONFLICT DO NOTHING}. NEVER check with {@code if (!exists)} in the app layer
 * (mechanism 1).
 */
package com.checkino.checkin;
