-- V1__core_schema.sql — Checkino core schema (story M1-S02)
--
-- IMMUTABLE MIGRATION: this file has run (or will run) against a real DB. Do NOT edit it after
-- merge. Change the schema = add a new V2__...sql. Editing V1 after it has been applied breaks
-- the Flyway checksum.
--
-- Story boundary (read DECISIONS.md, do not infer from memory):
--   * NO RLS here — RLS + the org_isolation policy is M1-S03. But EVERY org-scoped table must
--     already carry an org_id column now so M1-S03 can attach a policy to it (PLAN §3.1,
--     mechanism 2).
--   * checkin_event/entitlement/lead/notification_outbox are MINIMAL SKELETONS only — full
--     business columns (dedupe_bucket, client_event_id, consume_policy...) land in M2. In
--     particular do NOT add UNIQUE (member_id, scan_point_id, dedupe_bucket) to checkin_event
--     here: that is mechanism 1, which belongs to M2.
--
-- Conventions:
--   * PK/FK are uuid, DEFAULT gen_random_uuid() (matches the ...::uuid cast in the RLS sample,
--     mechanism 2).
--   * created_at timestamptz DEFAULT now(); business logic lives in the application layer, not
--     in the DB (keep the pg_dump escape hatch to a VN-region Postgres for PDPL) — so NO
--     triggers/procedures.

-- ── org: tenant root ───────────────────────────────────────────────────────────
CREATE TABLE org (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name        text NOT NULL,
    active      boolean NOT NULL DEFAULT true,
    created_at  timestamptz NOT NULL DEFAULT now()
);

-- ── staff_user: owner/staff, magic-link auth (D3, F10) ─────────────────────────
-- role = roles skeleton in M1 (a single text column). Multi-location + per-location roles is
-- the Pro/M4 cluster (F8) — there is no staff_user↔location/program link here yet
-- (DECISIONS § open question #4).
CREATE TABLE staff_user (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id      uuid NOT NULL REFERENCES org (id),
    email       text NOT NULL,
    role        text NOT NULL,
    active      boolean NOT NULL DEFAULT true,
    created_at  timestamptz NOT NULL DEFAULT now(),
    UNIQUE (org_id, email)
);

-- ── program: discipline/class, its own table (D7) ──────────────────────────────
-- OPTIONAL: an org may have no programs at all → every scope is org-wide.
CREATE TABLE program (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id      uuid NOT NULL REFERENCES org (id),
    name        text NOT NULL,
    active      boolean NOT NULL DEFAULT true,
    created_at  timestamptz NOT NULL DEFAULT now()
);

-- ── scan_point: one QR in one place (F1, F9, D7) ───────────────────────────────
-- program_id NULLABLE (D7): NULL = the QR is shared by the whole site, not tied to a program.
CREATE TABLE scan_point (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id      uuid NOT NULL REFERENCES org (id),
    program_id  uuid REFERENCES program (id),   -- intentionally NULLABLE (D7)
    name        text NOT NULL,
    code        text NOT NULL,                  -- the code in the /q/{code} URL
    active      boolean NOT NULL DEFAULT true,
    created_at  timestamptz NOT NULL DEFAULT now(),
    UNIQUE (org_id, code)
);

-- ── member: the member; normalized phone is the identity key (D2, D8) ──────────
-- D8: UNIQUE (org_id, phone_normalized) — NOT globally unique. The same phone number may exist
-- in two different orgs (each org is an independent tenant).
CREATE TABLE member (
    id                uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id            uuid NOT NULL REFERENCES org (id),
    name              text NOT NULL,
    phone_normalized  text NOT NULL,
    active            boolean NOT NULL DEFAULT true,
    created_at        timestamptz NOT NULL DEFAULT now(),
    UNIQUE (org_id, phone_normalized)           -- D8: the key the F1 upsert + F11 phone fix rely on
);

-- ── member_program: many-to-many member↔program (D7) ───────────────────────────
-- Source for the /staff roster filter and per-program ranking (D7).
CREATE TABLE member_program (
    member_id   uuid NOT NULL REFERENCES member (id),
    program_id  uuid NOT NULL REFERENCES program (id),
    PRIMARY KEY (member_id, program_id)
);

-- ── member_device: device token, stores the hash only (D3, F2) ─────────────────
-- Max 3 devices per member, oldest evicted — enforced in the app layer, not in the DB.
CREATE TABLE member_device (
    id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    member_id     uuid NOT NULL REFERENCES member (id),
    token_hash    text NOT NULL,
    user_agent    text,
    created_at    timestamptz NOT NULL DEFAULT now(),
    last_seen_at  timestamptz,
    revoked_at    timestamptz
);

-- ── audit_log: built FULLY in M1, NOT grow-later (D10) ─────────────────────────
-- All 8 columns. summary is human-readable text ("import: 12 new, 3 updated") so the owner can
-- read it without tooling. Each feature writes its own entries as it is built (F1 in M1, F11 in M2).
CREATE TABLE audit_log (
    id                   uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id               uuid NOT NULL REFERENCES org (id),
    actor_staff_user_id  uuid REFERENCES staff_user (id),  -- NULL = system / unknown actor
    action               text NOT NULL,
    entity_type          text NOT NULL,
    entity_id            uuid,
    summary              text NOT NULL,
    created_at           timestamptz NOT NULL DEFAULT now()
);

-- ═══════════════════════════════════════════════════════════════════════════════
-- MINIMAL SKELETONS — full business columns land in M2. Only id + org_id + a few skeleton
-- columns so the tables exist and carry org_id for RLS (M1-S03). Do NOT add business
-- columns/indexes here.
-- ═══════════════════════════════════════════════════════════════════════════════

-- entitlement: membership card / session pack (F5). consume_policy, session_quota, valid
-- window → M2.
CREATE TABLE entitlement (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id      uuid NOT NULL REFERENCES org (id),
    member_id   uuid NOT NULL REFERENCES member (id),
    created_at  timestamptz NOT NULL DEFAULT now()
);

-- checkin_event: a single check-in. dedupe_bucket + client_event_id + the UNIQUE dedupe index
-- (mechanism 1) and source SELF|ROSTER → M2. Do NOT add the dedupe unique index here.
CREATE TABLE checkin_event (
    id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id         uuid NOT NULL REFERENCES org (id),
    member_id      uuid NOT NULL REFERENCES member (id),
    scan_point_id  uuid NOT NULL REFERENCES scan_point (id),
    created_at     timestamptz NOT NULL DEFAULT now()
);

-- lead: trial pipeline (F4). name/phone/program/consent/follow-up → M2.
CREATE TABLE lead (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id      uuid NOT NULL REFERENCES org (id),
    created_at  timestamptz NOT NULL DEFAULT now()
);

-- notification_outbox: the notification queue (D5, F6).
-- IMPORTANT NOTE: NOTHING writes to or reads from this table until M4. Do NOT build a
-- @Scheduled sweep early — it is created here only so the table exists and carries org_id for
-- RLS. Full payload/status/attempts columns come later (M4).
CREATE TABLE notification_outbox (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id      uuid NOT NULL REFERENCES org (id),
    created_at  timestamptz NOT NULL DEFAULT now()
);
