-- V1__core_schema.sql — schema lõi Checkino (story M1-S02)
--
-- MIGRATION IMMUTABLE: file này đã (hoặc sẽ) chạy trên DB thật. KHÔNG sửa nó sau khi merge.
-- Đổi schema = thêm V2__...sql mới. Sửa V1 sau khi đã áp dụng làm hỏng checksum Flyway.
--
-- Ranh giới story (đọc DECISIONS.md, đừng suy từ trí nhớ):
--   * KHÔNG có RLS ở đây — RLS + policy org_isolation là M1-S03. Nhưng MỌI bảng org-scoped
--     phải mang cột org_id ngay bây giờ để M1-S03 gắn policy lên (PLAN §3.1, cơ chế 2).
--   * checkin_event/entitlement/lead/notification_outbox chỉ dựng KHUNG TỐI THIỂU — cột
--     nghiệp vụ đầy đủ (dedupe_bucket, client_event_id, consume_policy...) là M2. Cụ thể
--     KHÔNG thêm UNIQUE (member_id, scan_point_id, dedupe_bucket) vào checkin_event ở đây:
--     đó là cơ chế 1, thuộc M2.
--
-- Quy ước:
--   * PK/FK kiểu uuid, DEFAULT gen_random_uuid() (khớp cast ...::uuid ở RLS mẫu, cơ chế 2).
--   * created_at timestamptz DEFAULT now(); business logic ở tầng ứng dụng, không trong DB
--     (giữ đường thoát pg_dump sang Postgres region VN cho PDPL) — nên KHÔNG trigger/procedure.

-- ── org: gốc tenant ───────────────────────────────────────────────────────────
CREATE TABLE org (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name        text NOT NULL,
    active      boolean NOT NULL DEFAULT true,
    created_at  timestamptz NOT NULL DEFAULT now()
);

-- ── staff_user: chủ/nhân viên, auth magic-link (D3, F10) ───────────────────────
-- role = roles skeleton ở M1 (một cột text). Đa cơ sở + phân quyền theo cơ sở là cụm Pro/M4
-- (F8) — chưa có liên kết staff_user↔location/program ở đây (DECISIONS § câu hỏi mở #4).
CREATE TABLE staff_user (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id      uuid NOT NULL REFERENCES org (id),
    email       text NOT NULL,
    role        text NOT NULL,
    active      boolean NOT NULL DEFAULT true,
    created_at  timestamptz NOT NULL DEFAULT now(),
    UNIQUE (org_id, email)
);

-- ── program: bộ môn, bảng riêng (D7) ───────────────────────────────────────────
-- TUỲ CHỌN: một org có thể không có program nào → mọi phạm vi là org-wide.
CREATE TABLE program (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id      uuid NOT NULL REFERENCES org (id),
    name        text NOT NULL,
    active      boolean NOT NULL DEFAULT true,
    created_at  timestamptz NOT NULL DEFAULT now()
);

-- ── scan_point: một QR ở một chỗ (F1, F9, D7) ──────────────────────────────────
-- program_id NULLABLE (D7): NULL = QR dùng chung cả cơ sở, không gắn bộ môn nào.
CREATE TABLE scan_point (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id      uuid NOT NULL REFERENCES org (id),
    program_id  uuid REFERENCES program (id),   -- NULLABLE có chủ đích (D7)
    name        text NOT NULL,
    code        text NOT NULL,                  -- mã trong URL /q/{code}
    active      boolean NOT NULL DEFAULT true,
    created_at  timestamptz NOT NULL DEFAULT now(),
    UNIQUE (org_id, code)
);

-- ── member: hội viên; SĐT chuẩn hoá là khoá định danh (D2, D8) ──────────────────
-- D8: UNIQUE (org_id, phone_normalized) — KHÔNG unique toàn cục. Cùng một SĐT được phép
-- tồn tại ở hai org khác nhau (mỗi org là một tenant độc lập).
CREATE TABLE member (
    id                uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id            uuid NOT NULL REFERENCES org (id),
    name              text NOT NULL,
    phone_normalized  text NOT NULL,
    active            boolean NOT NULL DEFAULT true,
    created_at        timestamptz NOT NULL DEFAULT now(),
    UNIQUE (org_id, phone_normalized)           -- D8: khoá upsert F1 + sửa SĐT F11 dựa vào
);

-- ── member_program: nối nhiều–nhiều member↔program (D7) ─────────────────────────
-- Nguồn cho bộ lọc roster /staff và xếp hạng theo bộ môn (D7).
CREATE TABLE member_program (
    member_id   uuid NOT NULL REFERENCES member (id),
    program_id  uuid NOT NULL REFERENCES program (id),
    PRIMARY KEY (member_id, program_id)
);

-- ── member_device: device token, chỉ lưu hash (D3, F2) ─────────────────────────
-- Tối đa 3 thiết bị / hội viên, cái cũ nhất bị đuổi — thực thi ở tầng app, không ở DB.
CREATE TABLE member_device (
    id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    member_id     uuid NOT NULL REFERENCES member (id),
    token_hash    text NOT NULL,
    user_agent    text,
    created_at    timestamptz NOT NULL DEFAULT now(),
    last_seen_at  timestamptz,
    revoked_at    timestamptz
);

-- ── audit_log: dựng ĐỦ ở M1, KHÔNG grow-later (D10) ────────────────────────────
-- Đủ 8 cột. summary là text người-đọc-được ("import: 12 mới, 3 cập nhật") để chủ đọc
-- được không cần công cụ. Mỗi feature tự ghi entry của nó khi được xây (F1 ở M1, F11 ở M2).
CREATE TABLE audit_log (
    id                   uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id               uuid NOT NULL REFERENCES org (id),
    actor_staff_user_id  uuid REFERENCES staff_user (id),  -- NULL = hệ thống/không rõ actor
    action               text NOT NULL,
    entity_type          text NOT NULL,
    entity_id            uuid,
    summary              text NOT NULL,
    created_at           timestamptz NOT NULL DEFAULT now()
);

-- ═══════════════════════════════════════════════════════════════════════════════
-- KHUNG TỐI THIỂU — cột nghiệp vụ đầy đủ ở M2. Chỉ id + org_id + vài cột khung để
-- các bảng tồn tại và mang org_id cho RLS (M1-S03). ĐỪNG thêm cột/index nghiệp vụ ở đây.
-- ═══════════════════════════════════════════════════════════════════════════════

-- entitlement: thẻ/gói buổi (F5). consume_policy, session_quota, valid window → M2.
CREATE TABLE entitlement (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id      uuid NOT NULL REFERENCES org (id),
    member_id   uuid NOT NULL REFERENCES member (id),
    created_at  timestamptz NOT NULL DEFAULT now()
);

-- checkin_event: một lần điểm danh. dedupe_bucket + client_event_id + UNIQUE dedupe
-- (cơ chế 1) và source SELF|ROSTER → M2. KHÔNG thêm unique index dedupe ở đây.
CREATE TABLE checkin_event (
    id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id         uuid NOT NULL REFERENCES org (id),
    member_id      uuid NOT NULL REFERENCES member (id),
    scan_point_id  uuid NOT NULL REFERENCES scan_point (id),
    created_at     timestamptz NOT NULL DEFAULT now()
);

-- lead: pipeline trial (F4). name/phone/program/consent/follow-up → M2.
CREATE TABLE lead (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id      uuid NOT NULL REFERENCES org (id),
    created_at  timestamptz NOT NULL DEFAULT now()
);

-- notification_outbox: hàng đợi thông báo (D5, F6).
-- GHI CHÚ QUAN TRỌNG: KHÔNG ai ghi hay đọc bảng này cho tới M4. ĐỪNG xây @Scheduled sweep
-- sớm — nó dựng ở đây chỉ để bảng tồn tại và mang org_id cho RLS. Cột payload/status/
-- attempts đầy đủ về sau (M4).
CREATE TABLE notification_outbox (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id      uuid NOT NULL REFERENCES org (id),
    created_at  timestamptz NOT NULL DEFAULT now()
);
