-- V2__rls_org_isolation.sql — Checkino cơ chế 2, lớp 1 (story M1-S03)
--
-- IMMUTABLE MIGRATION: đã (sẽ) chạy trên DB thật. KHÔNG sửa sau khi merge — đổi schema thì thêm
-- V3__...sql. Sửa V2 sau khi applied sẽ vỡ checksum Flyway.
--
-- Ranh giới story (đọc DECISIONS.md § Ba cơ chế — 2, đừng suy từ trí nhớ):
--   * Đây là LỚP 1 (RLS + policy). Lớp 2 (bộ test cross-tenant mọi endpoint + CI) là M1-S04.
--   * Bốn điểm bắt buộc — chi tiết + lý do ở DECISIONS.md § Ba cơ chế — 2:
--     (1) current_setting('app.org_id', true) + NULLIF → chưa set org thì trả rỗng, KHÔNG nổ.
--     (2) App nối bằng role riêng checkino_app (NOSUPERUSER NOBYPASSRLS); superuser bỏ qua RLS.
--     (3) Policy có cả WITH CHECK (cô lập đường ghi), không chỉ USING.
--     (4) member_program / member_device không có org_id → policy bắc cầu qua member.

-- ── App runtime role ───────────────────────────────────────────────────────────
-- Tạo trong migration (không phải compose init) để Testcontainers tái lập y hệt cho bộ test
-- cô lập. Mật khẩu 'checkino_app' chỉ là mặc định DEV (đồng bộ với default 'checkino' của
-- compose). PROD phải xoay mật khẩu ngoài luồng: ALTER ROLE checkino_app PASSWORD '...'.
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'checkino_app') THEN
    CREATE ROLE checkino_app LOGIN PASSWORD 'checkino_app' NOSUPERUSER NOBYPASSRLS NOCREATEDB NOCREATEROLE;
  END IF;
END
$$;

GRANT USAGE ON SCHEMA public TO checkino_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO checkino_app;
-- Bảng do các migration V3+ tạo (chạy bằng owner checkino) cũng tự được cấp quyền cho app role.
ALTER DEFAULT PRIVILEGES IN SCHEMA public
  GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO checkino_app;

-- ── Lớp 1: RLS + policy org_isolation ──────────────────────────────────────────
-- ENABLE + FORCE (điểm 2) trên MỌI bảng; policy USING + WITH CHECK (điểm 3) với
-- current_setting(...,true)+NULLIF (điểm 1). org lọc theo id; các bảng org-scoped lọc theo org_id.

-- org: gốc tenant, lọc theo chính id
ALTER TABLE org ENABLE ROW LEVEL SECURITY;
ALTER TABLE org FORCE  ROW LEVEL SECURITY;
CREATE POLICY org_isolation ON org
  USING      (id = NULLIF(current_setting('app.org_id', true), '')::uuid)
  WITH CHECK (id = NULLIF(current_setting('app.org_id', true), '')::uuid);

-- Các bảng org-scoped: lọc theo org_id
ALTER TABLE staff_user ENABLE ROW LEVEL SECURITY;
ALTER TABLE staff_user FORCE  ROW LEVEL SECURITY;
CREATE POLICY org_isolation ON staff_user
  USING      (org_id = NULLIF(current_setting('app.org_id', true), '')::uuid)
  WITH CHECK (org_id = NULLIF(current_setting('app.org_id', true), '')::uuid);

ALTER TABLE program ENABLE ROW LEVEL SECURITY;
ALTER TABLE program FORCE  ROW LEVEL SECURITY;
CREATE POLICY org_isolation ON program
  USING      (org_id = NULLIF(current_setting('app.org_id', true), '')::uuid)
  WITH CHECK (org_id = NULLIF(current_setting('app.org_id', true), '')::uuid);

ALTER TABLE scan_point ENABLE ROW LEVEL SECURITY;
ALTER TABLE scan_point FORCE  ROW LEVEL SECURITY;
CREATE POLICY org_isolation ON scan_point
  USING      (org_id = NULLIF(current_setting('app.org_id', true), '')::uuid)
  WITH CHECK (org_id = NULLIF(current_setting('app.org_id', true), '')::uuid);

ALTER TABLE member ENABLE ROW LEVEL SECURITY;
ALTER TABLE member FORCE  ROW LEVEL SECURITY;
CREATE POLICY org_isolation ON member
  USING      (org_id = NULLIF(current_setting('app.org_id', true), '')::uuid)
  WITH CHECK (org_id = NULLIF(current_setting('app.org_id', true), '')::uuid);

ALTER TABLE audit_log ENABLE ROW LEVEL SECURITY;
ALTER TABLE audit_log FORCE  ROW LEVEL SECURITY;
CREATE POLICY org_isolation ON audit_log
  USING      (org_id = NULLIF(current_setting('app.org_id', true), '')::uuid)
  WITH CHECK (org_id = NULLIF(current_setting('app.org_id', true), '')::uuid);

ALTER TABLE entitlement ENABLE ROW LEVEL SECURITY;
ALTER TABLE entitlement FORCE  ROW LEVEL SECURITY;
CREATE POLICY org_isolation ON entitlement
  USING      (org_id = NULLIF(current_setting('app.org_id', true), '')::uuid)
  WITH CHECK (org_id = NULLIF(current_setting('app.org_id', true), '')::uuid);

ALTER TABLE checkin_event ENABLE ROW LEVEL SECURITY;
ALTER TABLE checkin_event FORCE  ROW LEVEL SECURITY;
CREATE POLICY org_isolation ON checkin_event
  USING      (org_id = NULLIF(current_setting('app.org_id', true), '')::uuid)
  WITH CHECK (org_id = NULLIF(current_setting('app.org_id', true), '')::uuid);

ALTER TABLE lead ENABLE ROW LEVEL SECURITY;
ALTER TABLE lead FORCE  ROW LEVEL SECURITY;
CREATE POLICY org_isolation ON lead
  USING      (org_id = NULLIF(current_setting('app.org_id', true), '')::uuid)
  WITH CHECK (org_id = NULLIF(current_setting('app.org_id', true), '')::uuid);

ALTER TABLE notification_outbox ENABLE ROW LEVEL SECURITY;
ALTER TABLE notification_outbox FORCE  ROW LEVEL SECURITY;
CREATE POLICY org_isolation ON notification_outbox
  USING      (org_id = NULLIF(current_setting('app.org_id', true), '')::uuid)
  WITH CHECK (org_id = NULLIF(current_setting('app.org_id', true), '')::uuid);

-- ── Bảng nối không có org_id → policy bắc cầu qua member (điểm 4) ────────────────
-- member đã bị RLS lọc theo org, nên subquery chỉ trả member của org hiện tại.
ALTER TABLE member_device ENABLE ROW LEVEL SECURITY;
ALTER TABLE member_device FORCE  ROW LEVEL SECURITY;
CREATE POLICY org_isolation ON member_device
  USING      (member_id IN (SELECT id FROM member))
  WITH CHECK (member_id IN (SELECT id FROM member));

ALTER TABLE member_program ENABLE ROW LEVEL SECURITY;
ALTER TABLE member_program FORCE  ROW LEVEL SECURITY;
CREATE POLICY org_isolation ON member_program
  USING      (member_id IN (SELECT id FROM member))
  WITH CHECK (member_id IN (SELECT id FROM member));
