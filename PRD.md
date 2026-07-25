# PRD — CheckinHub (working name)

**One-line:** Self-serve QR check-in & membership SaaS for any class-based center in Vietnam — gym, martial arts, yoga, language, arts, tutoring — with official Zalo notifications.

| | |
|---|---|
| Author | Thang Tran |
| Date | 2026-07-19 |
| Status | Draft v1.1 — updated after stack & data-ownership review |
| Companion docs | `GRILL-LOG.md` (assumption stress-test), `PLAN.md` (implementation) |

---

## 1. Problem

Small Vietnamese centers (1–5 locations) manage attendance with paper, Excel, or one-off freelancer builds (AppSheet/Glide, 5–6M VND, unmaintained). Existing software (GymMaster, KiotViet, PosApp) is sales-led, gym-only, and often hardware-bound. Demand is visible: recurring FB posts hiring freelancers for exactly this workflow (QR at the door → self check-in → Zalo notification → monthly attendance ranking).

**Evidence:** the FB gig this PRD is derived from — 3 sports centers, 5–6M VND budget, requirements: fixed QR per location, member self-scan showing remaining card validity, dedupe (multiple sessions/day = 1 count), new-member trial form, real-time Zalo notification, monthly attendance ranking in Google Sheets.

## 2. Product thesis (grill-tested)

Per `GRILL-LOG.md`: every vertical shares 6 primitives — **arrive → identify → check entitlement → record attendance → notify → report**. v1 ships only this generic core, configurable enough to fit all verticals. Vertical-specific features (booking, belts, homework, tuition) are explicitly out.

The two decisions that make "multi-vertical" true:

1. **Two check-in modes** — self-scan (adults with phones) AND staff roster (kids' classes, offline-capable). *(Grill Q4, Q12)*
2. **Per-entitlement consume policy** — `ONCE_PER_DAY | PER_VISIT | PER_CLASS`, never hardcoded. *(Grill Q3)*

## 3. Users & personas

| Persona | Role | Top jobs-to-be-done |
|---|---|---|
| **Owner** (buyer) | Runs 1–5 locations | Know who showed up in real time; stop card-expiry disputes; reward loyal members; add locations himself, free |
| **Staff / teacher** | At the door / in class | Check students in fast (roster tap), register walk-in trials |
| **Member** (adult) | Trains at the center | Scan → see remaining validity → one-tap check-in |
| **Parent** (kids' verticals) | Pays tuition | Get "child arrived" Zalo message; see attendance history |

## 4. Scope

### In scope — v1

**F1. Self-serve onboarding.** Owner signs up (phone + OTP or Zalo login) → creates org → locations → programs (bộ môn) → downloads printable QR poster per location/program. Target: **signup → first successful check-in < 10 minutes, no human contact** (north-star activation metric).

**F2. Member self check-in (PWA, no install).** Phone camera scans printed QR (QR = a plain URL identifying the scan point) → web page opens → first time ever: enter phone + OTP, server binds a device token to that browser; every later scan is recognized automatically — **no password, no repeated login** → screen shows membership status & remaining validity/sessions → tap check-in → confirmation. Shared-device edge: "Not you? Switch number." Failure fallback: short code shown to staff.

**F3. Staff roster check-in.** Staff opens today's roster (filter by program), taps names present. Works offline, queues, syncs later. Covers kids' classes and no-phone members.

**F4. New member / trial registration.** Unknown phone scanning QR → registration form (name, phone, program, consent checkbox) → lands in owner's "trial pipeline" list with follow-up status (contacted / converted / lost).

**F5. Entitlements (membership cards/packages).**
`{ type: TIME_BASED | SESSION_PACK | COURSE_TERM | TRIAL | DROP_IN; scope: program(s) | whole org; valid window; session quota; consume policy }`.
Owner creates plan templates, assigns to members, renews, freezes. Expiry warnings surfaced at check-in and in owner dashboard.

**F6. Notifications (official channels only — Grill Q6).**
- Live check-in feed on owner dashboard (real-time, free).
- Daily digest to owner via Zalo OA/ZNS (check-ins, new trials, expiring cards).
- Per-event OA message to members/parents who follow the center's OA.
- **Parent alert add-on:** ZNS "child arrived" to any phone (prepaid credit wallet, cost passed through ~300–800đ/msg).
- Never: unofficial Zalo group APIs.

**F7. Reports, rankings & data ownership.** Monthly attendance ranking per program/location (the gig's "khen thưởng" list), attendance history per member, expiring-cards list, trial conversion. **Near-real-time one-way mirror into the customer's own Google Sheet** (their Drive, their property; monthly archive tabs) + CSV anytime. ToS guarantee: cancel anytime, the Sheet stays with all data. Architecture stance (from the "bridge-only" review): operational DB is ours — speed, transactions, race-safe dedupe; the customer's Sheet is the always-fresh visible copy. Never the reverse (Grill Q9).

**F8. Multi-location & roles.** Unlimited locations per org (paid tier), roles: Owner / Manager (per location) / Staff.

**F9. Anti-abuse ladder (static QR is photographable — Grill Q5).** Fraud incentive exists only for loyalty rankings (session-pack cheaters punish themselves). v1 ships three free layers: (1) **opening-hours gate** — check-ins outside the scan point's operating hours are rejected outright; (2) **GPS soft-check** — outside ~200m radius or permission denied → recorded but flagged "unverified location" (no hard block: indoor GPS is unreliable); (3) **anomaly flags** — one device checking in many phones, cross-branch impossible travel, suspicious streaks — surfaced as a review list the owner skims before monthly rewards. Human review at reward time beats any tech. Later premium: rotating QR (60s TOTP) on a cheap Android tablet (~1–2M VND) at the door.

### Out of scope — v1 (Grill Q15)

Class scheduling/booking/capacity · payment processing & tuition collection · payroll/PT commissions · belt/grade tracking · homework/lesson reports · hardware (turnstiles, fingerprint) · native iOS/Android apps · unofficial Zalo group posting · multi-language UI (VN-only; EN later).

## 5. Vertical fit matrix (v1 core only)

| Need | Gym | Võ thuật | Yoga/Dance | Language/Tutoring (kids) | Swim |
|---|---|---|---|---|---|
| Check-in mode | Self-scan | Self-scan | Self-scan | **Staff roster** | Self-scan/roster |
| Entitlement | TIME_BASED | TIME_BASED / COURSE_TERM | SESSION_PACK | COURSE_TERM | SESSION_PACK |
| Consume policy | ONCE_PER_DAY | ONCE_PER_DAY | PER_VISIT | PER_CLASS | PER_VISIT |
| Who gets notified | Member/owner | Member/parent | Member | **Parent (ZNS add-on)** | Parent |

Same core, four config values. No vertical-specific code in v1.

## 6. Non-functional requirements

- **Multi-tenant isolation:** org-scoped data, enforced at repository layer; no cross-tenant leaks (test-gated).
- **Privacy (PDPL — Law 91/2025/QH15, effective 2026-01-01, replaces Decree 13; guided by Decree 356/2025):** consent at registration, privacy policy, delete-on-request, minors need parental consent, cross-border transfer disclosed. Residency path: MVP may run on Supabase Singapore (disclosed in policy); migrate DB to a VN cloud region once paying customers arrive (see PLAN §2 exit door).
- **Performance:** check-in round-trip < 2s on 4G; roster mode fully offline-capable.
- **Concurrency:** dedupe must be race-safe (two scans of same member within seconds = one event).
- **Availability:** single-region OK for v1; daily backups; status page.

## 7. Business model

| Tier | Price | Includes |
|---|---|---|
| Free | 0đ | 1 location, ≤50 active members, self-scan + roster, dashboard, CSV export |
| Pro | ~199k VND/location/month | Unlimited members, Zalo OA integration, Sheets sync, rankings, trial pipeline |
| Business | ~499k VND/month | Multi-location dashboard, roles, API access, priority support |
| Credits | pass-through | ZNS parent-alert wallet |

Anchor: 12 months of Pro ≈ half a one-off freelancer build (5–6M). Free tier = growth engine in FB owner groups. Annual plan: pay 10 months, get 12.

**Collection mechanics (VN reality):** Stage 1 (<20 customers) — VietQR bank transfer with org code in the memo, manual admin confirmation, Zalo renewal reminders. Stage 2 — PayOS/SePay webhook auto-activates on payment (Stripe is unavailable to VN merchants). Dunning: 7-day grace → soft-lock Pro features only; **check-in and customer data are never locked** (no data hostage — reputation in FB groups is the growth channel). Legal: register a household business (HKD) or LLC for invoicing — many centers require VAT invoices; hire a bookkeeping service (~300–500k/month).

## 8. Success metrics

- Activation: % of signups reaching first successful check-in < 10 min (target 40%+).
- 10 paying orgs within 60 days of launch; ≥1 non-sports vertical among first 20 orgs (validates multi-vertical claim).
- Org week-4 retention ≥ 60%; check-in success rate ≥ 99%.

## 9. Risks (top 4)

| Risk | Mitigation |
|---|---|
| Zalo policy/pricing changes | Official APIs only; notification layer abstracted (Zalo today, SMS/email swappable) |
| Incumbents add self-serve | Speed + micro-segment focus + free tier distribution |
| Static QR abuse discredits rankings | Anomaly flags v1; rotating-QR premium later |
| Solo-builder bandwidth | Ruthless out-of-scope list; PLAN.md milestones |

## 10. Open questions

1. Zalo OA verified-account requirements/cost for *each customer's* OA vs. one platform OA — validate in M1 spike (PLAN).
2. Google Sheets near-real-time mirror at scale — queued/batched writes per customer Sheet; verify API quotas in M0 spike.
3. Brand name & domain (CheckinHub is a placeholder).
