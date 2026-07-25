# PLAN — CheckinHub v1

Derived from `PRD.md` (v1.1). Assumes one developer, part-time (~15–20h/week). Target: pilot-ready in **6 weeks** — the switch to Next.js + Supabase cut ~2 weeks of scaffolding.

---

## 1. Platform decision: Web or App?

**Answer: Web — mobile-first PWA. No native app in v1.**

| Criterion | Web (PWA) | Native app | Zalo Mini App |
|---|---|---|---|
| Core loop (camera scans printed QR → opens URL) | Native fit — zero friction | Breaks loop: forces install at the door | Good — opens inside Zalo |
| Install friction for a first-time member | None | Fatal for trial signups | None (everyone has Zalo) |
| Push notifications | Not needed — Zalo OA/ZNS carries them | Main advantage, but redundant here | Via OA follow |
| Build & maintain (solo dev) | 1 codebase | 2 platforms + store reviews | 1 extra frontend, Zalo review |
| Distribution | Any QR/link/FB post | App Store/Play | Inside Zalo ecosystem |

**Phasing:** v1 = PWA (members, staff, owner). **Phase 2 = Zalo Mini App** as an alternative member frontend — gives Zalo identity, native OA-follow prompt (feeds F6 notifications), and VN-native distribution. Native app: only if a Business-tier customer demands it; default never.

## 2. Stack (revised — supersedes Grill Q14)

**Next.js (React) + Supabase.** One repo, one deploy. Supabase *is* the backend: managed Postgres 16 + Auth (phone OTP) + Realtime (live dashboard feed) + RLS for tenant isolation. "Using Supabase" ≠ "no backend" — it means not *operating* one.

- **App:** Next.js on Vercel, three areas: `/q/[code]` (member check-in), `/staff` (roster, PWA offline queue), `/admin` (owner dashboard). Tailwind.
- **Rule that keeps the exit open:** business logic lives in Next.js API routes/server actions — never in Supabase Edge Functions. DB access via ORM (Drizzle) + plain SQL migrations.
- **Integrations:** Zalo OA API + ZNS (official only), Google Sheets API (near-real-time mirror, queued), OTP via ZNS/SMS provider.
- Previously chosen: Spring Boot + Angular (builder's day-job stack) — dropped for solo speed; revisit only if a team forms.

**Exit door — "what if demand outgrows the MVP?"** Supabase is plain Postgres. With logic in the app layer and an ORM in between, migration = `pg_dump` → managed Postgres on a VN cloud (Viettel/VNG/FPT) → change one connection string; Auth is the only Supabase-specific piece to swap. That move is *already planned* for PDPL data residency once paying customers arrive — scaling and compliance point at the same destination, so nothing is wasted. Load math: 1,000 centers × 100 check-ins/day ≈ 100k writes/day ≈ 1–2 writes/second — trivial for one Postgres for years. The real growth pains will be Zalo quotas and support volume, not the database.

## 3. Domain model (core tables)

**Core 4 — ship first:** `scan_point` (org/location/program, QR code, opening hours, geo coords), `member` (name, phone, device_token, consent fields), `entitlement` (type, valid window, sessions_left, consume_policy — PRD F5), `checkin_event` (member, scan_point, timestamp, dedupe_bucket, source SELF|ROSTER, anomaly flags).

**Grow later:** `notification_log`, `user_account` + `role` (when owners add staff logins), `credit_wallet` + `ledger` (ZNS add-on), `subscription` (billing).

Race-safe dedupe: unique index on `(member_id, dedupe_bucket)` computed per consume policy — the database itself enforces "2 sessions/day = 1 count", not app logic. RLS on every table keyed by org.

## 4. Milestones

### M0 — Validation spikes (Week 1) — *cheap kills before code*
- Spike: register a Zalo OA, send OA message + ZNS template end-to-end; document verification requirements, per-message cost, and whether each customer needs their own OA vs. one platform OA. **(PRD open question #1 — this is the riskiest unknown; do it first.)**
- Spike: Sheets API mirror — write rates/quotas for near-real-time sync; browser geolocation accuracy test at a real storefront.
- Take the original FB gig as **design partner #0** (paid 5–6M or free pilot — either way, real requirements + testimonial).
- Exit criteria: Zalo path confirmed viable → green-light build. If not viable → pivot notification layer (dashboard + SMS/email) before any code.

### M1 — Walking skeleton (Weeks 2–3)
- Next.js on Vercel + Supabase project; RLS multi-tenant scaffolding + isolation tests; roles skeleton (F8).
- F1 minimal: signup/OTP → create scan points → printable QR (PDF).
- F2 happy path: scan → phone+OTP once → device token → check-in recorded → live dashboard feed (Supabase Realtime).
- Exit: end-to-end demo on a real phone at a real door.

### M2 — Entitlements + dedupe + roster (Weeks 3–4)
- F5 entitlement model + F2 validity display; race-safe consume policies (DB unique-index dedupe + integration tests).
- F9 free layers: opening-hours gate, GPS soft-check, anomaly flags.
- F3 staff roster PWA with offline queue; F4 trial registration + pipeline list.
- Exit: the entire original FB gig workflow works without Zalo yet.

### M3 — Zalo + reports (Weeks 5–6)
- F6: OA connect flow, daily owner digest, member OA messages, ZNS parent-alert wallet.
- F7: monthly ranking, expiring cards, near-real-time Google Sheet mirror (queued writes), CSV.
- Exit: design partner #0 live on it, notifications flowing.

### M4 — Pilot & monetize (Week 6+)
- Billing (Free/Pro; VietQR + manual confirmation first, PayOS/SePay webhook later; 7-day grace, never lock check-in/data), PDPL pages (consent, privacy, deletion).
- Onboard 5–10 pilot centers from FB groups (target ≥1 non-sports center — PRD success metric).
- Instrument activation funnel (signup → first check-in time).

## 5. Go-to-market (first 90 days)

1. Deliver design partner #0 (the FB gig) → case study with real numbers.
2. Post the case study in the same FB owner groups (gym/võ/yoga owners, trung tâm ngoại ngữ groups) — where demand already self-identifies.
3. Free tier as default answer to every "tìm freelancer làm app điểm danh" post.
4. Recruit 2–3 AppSheet freelancers as affiliates (they keep setup fee, you keep subscription) — Grill Q8.

## 6. Risk register (delta from PRD §9)

| Risk | Trigger | Response |
|---|---|---|
| Zalo OA verification too slow/costly per customer | M0 spike | Platform-level OA sending on customers' behalf, or digest-only v1 |
| Offline roster sync conflicts | M2 tests | Last-write-wins + server dedupe key; conflicts surfaced to staff |
| Solo timeline slips | Any milestone +1wk late | Cut F7 Sheet mirror and F9 GPS layer to post-pilot; never cut F2/F3/F5 |
| Outgrowing Supabase / PDPL residency | ~20 paying customers | Planned move: `pg_dump` → VN-region managed Postgres; logic already outside Supabase (§2 exit door) |

## 7. Definition of done — v1

A center owner found via FB can: sign up alone, print a QR, have an adult member self-check-in showing card validity, have a teacher roster-check a kids' class, see the live feed, get the daily Zalo digest, see the monthly ranking, and watch every check-in mirror into their own Google Sheet — with zero contact with the founder. That closes every requirement in the original FB gig, generalized to any vertical.
