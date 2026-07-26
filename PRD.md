# PRD — Checkino

**One-line:** Self-serve QR check-in & membership SaaS for any class-based center in Vietnam — gym, martial arts, yoga, language, arts, tutoring. The free tier runs end-to-end at **zero variable cost**; Zalo notifications are a Pro add-on, not a dependency.

| | |
|---|---|
| Author | Thang Tran |
| Date | 2026-07-19 (v1.0/v1.1) · 2026-07-25 (v2.0, v2.1) · 2026-07-26 (v2.2, v2.3) |
| Status | Draft v2.3 — 2026-07-26, product named **Checkino** (D11); v2.2 applied D6–D10 (rankings & trials are free, `program` is its own table, one phone = one member, no 6-digit fallback, `audit_log` in v1) |
| Companion docs | **`DECISIONS.md` (source of truth for architecture & product decisions — wins on conflict)**, `GRILL-LOG.md` (assumption stress-test), `PLAN.md` (implementation) |

---

## 1. Problem

Small Vietnamese centers (1–5 locations) manage attendance with paper, Excel, or one-off freelancer builds (AppSheet/Glide, 5–6M VND, unmaintained). Existing software (GymMaster, KiotViet, PosApp) is sales-led, gym-only, and often hardware-bound. Demand is visible: recurring FB posts hiring freelancers for exactly this workflow (QR at the door → self check-in → notification → monthly attendance ranking).

**Evidence:** the FB gig this PRD is derived from — 3 sports centers, 5–6M VND budget, requirements: fixed QR per location, member self-scan showing remaining card validity, dedupe (multiple sessions/day = 1 count), new-member trial form, real-time Zalo notification, monthly attendance ranking in Google Sheets.

Note on that last-but-one requirement: the gig asked for *real-time notification*, and assumed Zalo was the only way to deliver it. v1 delivers the job — a live check-in feed the owner watches on their phone — without Zalo. Zalo becomes the Pro-tier upgrade for owners who want the message to land in a chat thread (§4 F6, `DECISIONS.md` D1).

## 2. Product thesis (grill-tested)

Per `GRILL-LOG.md`: every vertical shares 6 primitives — **arrive → identify → check entitlement → record attendance → notify → report**. v1 ships only this generic core, configurable enough to fit all verticals. Vertical-specific features (booking, belts, homework, tuition) are explicitly out.

The three decisions that make "multi-vertical, zero-cost free tier" true:

1. **Two check-in modes** — self-scan (adults with phones) AND staff roster (kids' classes, offline-capable). *(Grill Q4, Q12)*
2. **Per-entitlement consume policy** — `ONCE_PER_DAY | PER_VISIT | PER_CLASS`, never hardcoded. *(Grill Q3)*
3. **Two identity mechanisms, deliberately different** — owner/staff sign in ~monthly and tolerate friction, so they get **email magic link**; members identify themselves at the door constantly and are hypersensitive to friction, so they get a **bound device token**. These two needs were previously collapsed into one, which is what forced OTP into the design. *(`DECISIONS.md` D3)*

The `notify` primitive at free tier is served by the **live owner dashboard**, not by Zalo. That single substitution is what removes every variable cost from the free tier.

## 3. Users & personas

| Persona | Role | Top jobs-to-be-done |
|---|---|---|
| **Owner** (buyer) | Runs 1–5 locations | Know who showed up in real time; stop card-expiry disputes; reward loyal members; add locations himself, free |
| **Staff / teacher** | At the door / in class | Check students in fast (roster tap), register walk-in trials |
| **Member** (adult) | Trains at the center | Scan → see remaining validity → one-tap check-in |
| **Parent** (kids' verticals) | Pays tuition | See the child's attendance history (free); get a "child arrived" Zalo message **(Pro)** |

## 4. Scope

### In scope — v1

**F1. Self-serve onboarding.** Owner signs up with **email + magic link** (F10) → creates org → locations → *(optionally)* programs (bộ môn) → **imports the member roster** (CSV upload or paste from Excel; they already keep one) → downloads a printable QR poster per location/program. Target: **signup → first successful check-in < 10 minutes, no human contact** (north-star activation metric) — and the roster import is inside those 10 minutes.

The import step is **mandatory, not optional**: F2 identifies a member by matching their phone number against this org's roster, so an empty roster means nobody can check in. The onboarding wizard must say so, because the failure is silent and confusing otherwise — an owner who prints the QR and scans it themselves, before importing anyone, lands in the F4 trial form rather than a check-in, and concludes the product is broken. Wizard order is therefore: org → scan point → **roster** → QR poster.

**Programs (bộ môn) are a skippable step — that is the condition for not breaking the north-star.** *(`DECISIONS.md` D7)* A program is a first-class entity (its own table, many-to-many with members; a scan point may optionally point at one), because members, entitlement scopes (F5), roster filters (F3) and rankings (F7) all need to reference it independently of *which QR was scanned*. But the four-step wizard above stays four steps:

- The program step can be **skipped entirely**. Skip it and every card and every ranking is org-wide, exactly as before — a single-program center never meets the concept.
- The **program column in the import file is optional**. A member can belong to several programs; a member with no program is org-wide.
- Program names in the file that don't exist yet are **listed in the F1 preview** (*"will create 2 new programs: Boxing, Yoga"*) and created only on confirm — same "show before you write" rule as the roster diff below, never silent creation.

**Import is a repeated operation, not a one-off — it is an upsert keyed on the phone number.** Owners re-import at the start of every term, whenever a class is added, whenever they clean up their Excel. Two constraints follow, and both exist because D2 turned the roster into *identity* data rather than reporting data:

- **Import never deletes.** A member absent from the uploaded file is left untouched, never removed or deactivated. The failure this prevents is fatal and easy to hit: uploading last year's file would wipe the roster, and every member at that center would be unable to check in the next morning. Ending a membership is a deliberate manual action in F11.
- **Preview before apply.** The upload is parsed and diffed first — *"12 new, 3 updated, 185 unchanged"*, with the rows listed — and nothing is written until the owner confirms. Rows that fail validation (unparseable phone, duplicate phone within the file) are reported in the same preview and skipped, not silently dropped.
- **The match key is the normalized phone number.** Consequently a typo'd phone cannot be repaired by re-importing — a corrected number is simply a new person to the upsert. Fixing a wrong number is an F11 operation.

**F2. Member self check-in (a plain web page, no install; no OTP in v1).** *(`DECISIONS.md` D2, D3)*

> **Terminology, fixed here once.** "PWA" in this document means exactly one thing — *installable, runs offline* — and it applies to **`/staff` only** (F3). `/q` is an ordinary web page: no manifest, no service worker. `/admin` gets a manifest for the home-screen icon and nothing more. The earlier phrase "PWA, no install" was self-contradictory: a PWA you don't install is a website. Three-tier table in `DECISIONS.md` D4.

```
Member scans printed QR (QR = a plain URL identifying the scan point)
   │
`/q/{code}` opens → enter phone number
   │
   ├─ Number IS on this org's roster → bind a device token immediately, no OTP
   │     → show membership status & remaining validity/sessions → tap check-in → confirmation
   │     → every later scan on that device is recognized automatically: no phone entry, no login
   │
   └─ Number is UNKNOWN → trial registration form → F4
```

- **Device token:** httpOnly cookie, TTL 1 year. The server stores only a **hash** of the token, never the token itself. Max 3 devices per member; the oldest is evicted.
- **Revoking a token** (member changed or lost their phone, or an anomaly flag needs clearing) is an owner action in **F11**, not something the member can do from `/q`.
- **Shared device:** "Not you? Switch number" re-runs the phone step.
- **No network → ask a staff member to check you in via `/staff` (F3).** There is no 6-digit fallback code, and the honest reason is that there is nothing to show it on: `/q` has no service worker by design *(`DECISIONS.md` D4)*, so with no network the page does not open at all. A code would only have covered the narrow case of a page that loaded and then lost connectivity, and it would still have needed to be verifiable by a `/staff` app that may itself be offline. `/staff` already does this job in fewer steps — the staff member types a name, taps once, and it works offline *(D9)*.
- **Abuse signal:** one phone number binding on two devices within a short window raises an anomaly flag (F9) — it is logged and surfaced, not blocked.
- **Upgrade path, not v1:** OTP is a **Pro** feature (a ZNS auth message costs 300đ — §4 F6). The better long-term answer is **Zalo Login (OAuth)**, which is not billed per message; it costs only the one-time OA verification.

Why no OTP: it defends against exactly one attack — A entering B's number to check in on B's behalf — whose entire consequence is a distorted attendance ranking. See §6 (identity assurance NFR) for the accepted risk and its ceiling.

**F3. Staff roster check-in (`/staff` — the one surface that is a PWA).** Staff opens today's roster (filter by program — the filter reads `member_program`, not the scanned QR, per D7), taps names present. Each tap writes to an IndexedDB outbox with a `client_event_id`, the UI ticks immediately, and the batch POSTs when connectivity returns (§6 offline NFR). This is the check-in mode for kids' classes.

**Here the teacher taps a name and never touches a phone number.** Worth stating because §6 fixes one phone number per member (D8), and that could otherwise read as a problem for kids' classes: it is not. Phone numbers matter only at import (F1) and at member self-scan (F2). A class of thirty children is checked in by name.

**Full offline capability is a separate, sequenced piece of work, not part of the first cut of F3.** The outbox above survives a network that drops *while the page is open*; it does not survive a reload. Making `/staff` genuinely offline-capable adds: `@angular/pwa` (manifest + service worker + `ngsw-config.json`), caching today's roster into IndexedDB rather than memory, a no-network boot path, an explicit `SwUpdate` "new version available, reload" bar instead of silent swaps mid-class, and **an iOS install-instruction screen** (see §6). Splitting it out lets staff use roster check-in before offline lands; shipping the outbox alone was rejected as the worst of both worlds, since it *feels* offline-capable and then fails on reload (`DECISIONS.md`, mechanism 3). The three limits that remain even when this is done are in §6 — they are deliberate, not defects.

**F4. New member / trial registration — free tier.** Unknown phone scanning QR → registration form (name, phone, program, consent checkbox) → lands in owner's "trial pipeline" list with follow-up status (contacted / converted / lost). Converting a lead into a member happens in **F11**. This is also where an owner's own test scan lands before they import a roster (F1) — which is the second reason it cannot sit behind a paywall: gating it would break onboarding, and onboarding is the north-star *(`DECISIONS.md` D6)*.

**F5. Entitlements (membership cards/packages).**
`{ type: TIME_BASED | SESSION_PACK | COURSE_TERM | TRIAL | DROP_IN; scope: program(s) | whole org; valid window; session quota; consume policy }`.
The `scope` field references real `program` rows *(D7)*; an org that skipped the program step has org-wide scope only. Owner creates plan templates here; **assigning, renewing and freezing a specific member's card happens in F11**.

**Expiry warning, split across two features so neither claims the other's work:** the **warning shown to the member and staff at the moment of check-in** ("card expires in 3 days", "2 sessions left") is part of F5 — it is computed from the entitlement being consumed. The **list of soon-to-expire cards** the owner reviews proactively is a report and belongs to **F7**. Same underlying data, two different surfaces, delivered in different milestones (PLAN M2 and M3).

**F6. Notifications — live feed free, Zalo is Pro.** *(`DECISIONS.md` D1; Grill Q6)*

- **Free tier — the live check-in feed on the owner dashboard is the only channel, and it is enough.** Real-time via server-sent events from our own server. Cost: 0đ. Nothing in the free tier sends a message to anyone.
- **Pro tier — Zalo OA/ZNS.** Daily owner digest (check-ins, new trials, expiring cards); per-event OA message to members/parents who follow the center's OA; **parent alert add-on:** ZNS "child arrived" to any phone, billed from a prepaid credit wallet at pass-through cost.
- **Prerequisite that shapes the whole product:** sending requires **the customer's own verified OA** plus a registered app on `developers.zalo.me`. OA verification requires **the customer's business licence (GPKD)**, original or notarized and still valid. Many small VN centers have none, or won't do the paperwork. That is why Zalo cannot be in the core loop — it would break the "< 10 minutes, no human contact" promise for a large share of the market.
- **ZNS price list (verified 2026-07-25):**

  | Message type | Price / message |
  |---|---|
  | **Authentication (OTP)** | **300đ** |
  | Payment request | 300đ |
  | Voucher | 300đ |
  | Administrative | 120đ |
  | Other | 200đ |

  Surcharges: +100đ per CTA button beyond the first, +200đ for an image. Billed per **successfully delivered** message. **There is no free quota.** For comparison, SMS Brandname is 600–1.000đ/message. From 01/01/2026 Zalo renamed ZCA → **ZBS Account** and merged the types under "ZBS Template Message".
- **Never:** unofficial Zalo group APIs. Official APIs only, in every tier.

**F7. Reports, rankings & data ownership.** Monthly attendance ranking per program/location (the gig's "khen thưởng" list — ranked per `program`, not per QR code, which is why D7 made `program` a table), attendance history per member, expiring-cards list *(the proactive list; the at-check-in warning is F5)*, trial conversion. **Rankings and every report listed here are free tier** *(`DECISIONS.md` D6)* — the ranking is the exact job the original gig hired a freelancer to do, and D2's "the only consequence of check-in fraud is a distorted ranking" is empty at the tier most users are on if the ranking isn't there. **CSV export at any tier, including free** (0đ, no external quota). **Near-real-time one-way mirror into the customer's own Google Sheet** (their Drive, their property; monthly archive tabs) is a **Pro** feature — gated on API quota risk at multi-customer scale (§10.2), not on cost. ToS guarantee: cancel anytime, the Sheet and every export stay with the customer. Architecture stance (from the "bridge-only" review): the operational DB is ours — speed, transactions, race-safe dedupe; the customer's Sheet is the always-fresh visible copy. Never the reverse (Grill Q9).

**Write-direction contract — what happens when the owner edits the Sheet.** "One-way" has to say what the other way does, or owners will discover it by losing an afternoon's edits:

- Tabs written by the app are **protected ranges**, with a warning line pinned at the top of each: *"Tab này do app ghi. Sửa tay sẽ bị ghi đè."*
- The rule for owners is one sentence: **read in the Sheet, edit in the app** (F11).
- There is **no inbound Sheets API path** — no extra OAuth scope requested, no background reconciliation, no attempt to detect or merge manual edits. An edit made in a protected tab is overwritten at the next mirror write, and that is the specified behavior.

Two-way sync is out of scope, for a reason stated in the out-of-scope list below rather than left implicit.

**F8. Multi-location & roles.** Unlimited locations per org (paid tier), roles: Owner / Manager (per location) / Staff.

**F9. Anti-abuse ladder (static QR is photographable — Grill Q5).** Fraud incentive exists only for loyalty rankings (session-pack cheaters punish themselves). v1 ships three free layers:

1. **Opening-hours gate** — check-ins outside the scan point's operating hours are rejected outright.
2. **GPS soft-check** — outside ~200m radius or permission denied → recorded but flagged "unverified location". No hard block: indoor GPS is unreliable (radius still to be calibrated — §10.2).
3. **Anomaly flags** — surfaced as a review list the owner skims before monthly rewards.

Because v1 ships **without OTP** (F2), layer 3 carries more weight than it did in v1.1 and its flags are specified rather than left vague:

| Flag | Signal |
|---|---|
| One device, many numbers | A single device token / browser checking in several different phone numbers |
| One number, many devices | The same phone number binding on 2+ devices within a short window |
| Impossible travel | Same member checked in at two branches too far apart, too close in time |
| Implausible streak | Perfect attendance inconsistent with the member's own history, near reward cut-off |
| Unverified location | From layer 2 |

Human review at reward time beats any technical control — the owner knows their members. Later premium: rotating QR (60s TOTP) on a cheap Android tablet (~1–2M VND) at the door.

**F10. Owner & staff authentication — email magic link.** *(`DECISIONS.md` D3)* Owner or staff enters their email → receives a one-time sign-in link → session cookie. Sent through the free tier of a transactional email provider (Resend/Brevo): **0đ**. Roles come from F8.

- **Not phone OTP:** 300đ per ZNS auth message with no free quota (F6) — unacceptable for a login that must work before the customer pays anything.
- **Not passwords:** password resets are a support load a solo builder cannot carry.
- **Members never use email.** People training at a VN gym don't check email, many have no email they use regularly, and standing at the door opening Gmail for a code is worse than typing an OTP. Email is right for owner/staff and wrong for members — hence two mechanisms, not one.
- **Important distinction:** we still **store** every member's phone number (the center needs it to call them). *Needing to store a number* ≠ *needing to verify it.*

**F11. Member management (`/admin`).** A searchable, filterable member list opening onto a per-member detail page. This is the owner's day-to-day workspace, and it exists as its own feature because the jobs it carries belong to three different features — folding it into F5 would put "fix a member's phone number" inside a feature called *Entitlements*.

Why each of the four is mandatory rather than nice-to-have:

| Job | Why it cannot be dropped |
|---|---|
| Assign / renew / freeze a card | *(F5)* The only place an entitlement is attached to a specific person. F5 defines plan templates; F11 is where they are applied |
| Convert a lead into a member | *(F4)* The trial pipeline's exit. Without it, walk-in registrations never reach the roster and therefore never check in |
| Correct a mistyped phone number | *(F2)* The phone number **is the identity key**. A wrong digit means that member can never check in, and re-importing cannot repair it — the upsert (F1) would create a second person instead |
| Revoke a device token | *(F2)* New phone, lost phone, shared handset, or clearing an F9 anomaly flag. Members cannot do this from `/q` |

Also here, for completeness of the roster lifecycle: **mark a member inactive** — the only exit path, since import never deletes (F1) — and **add a single member by hand**, for the owner who has one new person and no file.

**Every mutation here is written to an audit log, and so is every import (F1).** *(`DECISIONS.md` D10)* Since D2 the roster is identity data: editing it changes who may walk through the door, so assigning or renewing a card, correcting a phone number, revoking a device token and marking someone inactive all leave a trace — `(org_id, actor, action, entity, human-readable summary, timestamp)`, readable by the owner without tooling. This is v1, not "grow later": with several people sharing `/admin` under different roles (F8), *"who changed my number"* and *"who ended my membership"* need an answer rather than a guess. **Correcting a phone number additionally rejects the change if that number already belongs to another member** — the identity key is unique per org (§6, D8).

**The four ways into the roster, after F11 exists** — deliberately non-overlapping, and F4 carries the common case of a single newcomer:

| Situation | Path | Owner typing |
|---|---|---|
| Day one, Excel already exists | File import *(F1)* | none |
| New term, a whole class added | Re-import, upsert on phone *(F1)* | none |
| One newcomer walks in | **F4** — they fill in the trial form themselves → owner converts | one tap *(F11)* |
| Fix a name, change a number, end a membership | Member management *(F11)* | a few fields |

### Out of scope — v1 (Grill Q15)

Class scheduling/booking/capacity · payment processing & tuition collection · payroll/PT commissions · belt/grade tracking · homework/lesson reports · hardware (turnstiles, fingerprint) · native iOS/Android apps · unofficial Zalo group posting · multi-language UI (VN-only; EN later) · **members without a phone number** (§6, D8) · **a 6-digit offline fallback code at `/q`** (F2, D9) · **member OTP at the free tier** — note that member OTP itself is *in* v1, as a Pro feature (F2, F6); what is out of scope is putting a metered message in the free tier · **Zalo as a required dependency** — no flow in the core product may block, degrade, or wait on the customer having a verified OA · **two-way Google Sheet sync** and **managing the roster by typing into the Sheet** (F7).

That last one gets its reasons spelled out, in order of strength, because the roster is identity data since D2 — a Sheet is a fine place to *read* it and a bad place to *keep* it:

1. **No permissions.** Anyone with the link can edit. The app has roles (F8) and RLS (§6). And editing the roster is not a reporting mistake — **adding your own phone number to the Sheet is free membership**, since F2 admits whoever matches the roster.
2. **No data constraints.** A Sheet has no `UNIQUE (org_id, phone_normalized)` (D8), no phone normalization, and no preview-before-write step (F1). Two rows with the same number, or a number with a stray space, are silently accepted and then break check-in.
3. **Deletion is unrecoverable.** Selecting rows and pressing delete is exactly the failure that the "import never deletes" rule (F1) was written to close. Reopening it through the side door defeats the rule.
4. **No audit trail worth the name.** Sheets version history exists but is weak and not scoped per-record; the app writes an audit log on every roster mutation (F11, D10).

The app is the system of record, the Sheet is a mirror.

## 5. Vertical fit matrix (v1 core only)

| Need | Gym | Võ thuật | Yoga/Dance | Language/Tutoring (kids) | Swim |
|---|---|---|---|---|---|
| Check-in mode | Self-scan | Self-scan | Self-scan | **Staff roster** | Self-scan/roster |
| Entitlement | TIME_BASED | TIME_BASED / COURSE_TERM | SESSION_PACK | COURSE_TERM | SESSION_PACK |
| Consume policy | ONCE_PER_DAY | ONCE_PER_DAY | PER_VISIT | PER_CLASS | PER_VISIT |
| Free-tier channel | Owner live feed | Owner live feed | Owner live feed | Owner live feed | Owner live feed |
| Pro add-on channel | Member OA msg | Member/parent OA msg | Member OA msg | **Parent ZNS alert** | Parent ZNS alert |

Same core, four config values — and the notification channel is a tier setting, not vertical-specific code. No vertical-specific code in v1.

## 6. Non-functional requirements

- **Multi-tenant isolation (two layers, and the test layer is not negotiable).** Layer 1: Postgres RLS on every org-scoped table, with `SET LOCAL app.org_id` at the start of each transaction. Layer 2: an automated cross-tenant test suite — create org A and org B; for **every** endpoint, use A's token to read or modify B's data and assert 403 or empty. Runs in CI. **Written before the features, not after** — without layer 2, layer 1 is just a belief. *(`DECISIONS.md`, mechanism 2)*
- **Privacy (PDPL — Law 91/2025/QH15, effective 2026-01-01, replaces Decree 13; guided by Decree 356/2025):** consent at registration, privacy policy, delete-on-request, minors need parental consent, cross-border transfer disclosed. **Residency path:** business logic lives in the application layer, never in the database, so migration is `pg_dump` → managed Postgres in a VN region (Viettel/VNG/FPT) → change one connection string. The MVP hosting region is disclosed in the privacy policy until that move happens (see PLAN §2 exit door).
- **Performance:** check-in round-trip < 2s on 4G. `/q/{code}` is **server-rendered, not a SPA** — it must load fast on a cold 4G connection while someone stands at the door, and it needs nothing a framework provides *(`DECISIONS.md` D4)*.
- **Concurrency:** dedupe is enforced by the database, not by app logic — `UNIQUE INDEX (member_id, scan_point_id, dedupe_bucket)` + `INSERT ... ON CONFLICT DO NOTHING`, where `dedupe_bucket` is derived from the entitlement's consume policy. Never an app-level `if (!exists)` check: two concurrent scans both see "not there" and write two rows. The insert and the session decrement share one transaction. *(`DECISIONS.md`, mechanism 1)*
- **Offline (roster mode):** fully offline-capable. Staff taps write to an IndexedDB outbox as `{client_event_id: uuid, member_id, ...}`, the UI ticks optimistically, and the batch POSTs when connectivity returns. The server dedupes on a unique index over `client_event_id`, so a flaky network resending ten times still produces one row — no conflict-resolution logic. Two distinct jobs, both required: the **service worker** is what lets the app *open* with no network (it caches the shell); the **IndexedDB outbox** is what keeps taps from being lost (it holds the data). *(`DECISIONS.md`, mechanism 3)*
- **Accepted offline limits — by design, not bugs.** Written down so they are not filed as defects later:
  1. **The first launch must be online.** A service worker can only be installed while the page loads over the network. A new staff member on a new phone who has never opened the app is out of luck in a basement. → Onboarding must include the step *"open the app once somewhere with signal, before class"*.
  2. **The cached roster can be stale.** A roster cached at 8am does not contain a member added at 5pm, and there is no way to learn that offline. The workaround is the owner correcting it afterwards in F11. **Not solved in v1.**
  3. **iOS never offers to install.** Chrome/Android show an install prompt on their own; Safari requires Share → scroll → *"Add to Home Screen"*, and nobody discovers that unaided. → An illustrated instruction screen, shown when Safari-on-iPhone is detected, is part of F3's offline work. Skipping it makes the whole offline effort useless for roughly half the users.
  - Two operational notes: a service worker only runs over **HTTPS** (or `localhost`), and its scope is its own path — registering under `/staff/` leaves `/q` and `/admin` untouched, which is what we want.
- **One phone number = one member — the fourth accepted limit.** *(`DECISIONS.md` D8)* `UNIQUE (org_id, phone_normalized)` on `member`. The phone number has been the identity key since D2: it keys the F1 import upsert, the F2 lookup at `/q`, and the F11 correction path. Letting one number point at several people forces a new branch into all three — the import wouldn't know whom it is updating, `/q` wouldn't know whom to return. Two consequences, written down so they are not filed as defects:
  1. **A parent with two children needs two numbers.** The second is usually already available (the other parent's). Rare, and the owner resolves it in F11 without waiting for a feature.
  2. **Members with no phone number are not supported.** This does *not* affect kids' classes — see F3: the teacher taps names and never touches a number. The escape route, if a real customer ever complains: make the upsert key *(phone + normalized name)* and add a *"which one are you?"* step at `/q`. Not built pre-emptively.
- **Identity assurance is soft by design in v1.** Member identity rests on a roster match only; there is no OTP *(F2, `DECISIONS.md` D2)*.
  - **Accepted risk:** A enters B's number and checks in on B's behalf.
  - **Consequence ceiling:** a distorted monthly attendance ranking. No money moves and no sensitive data is exposed — the screen shows only "12 sessions left".
  - **Compensating controls:** the F9 anomaly flags, plus owner review of the flag list before monthly rewards are handed out.
  - **Revisit thresholds (explicit, so this isn't a permanent shrug):** enable OTP (Pro) or rotating QR when *either* a paying customer attaches real money to the ranking, *or* flagged check-ins exceed **2% in a month** (§8).
- **Availability:** single-region OK for v1; daily backups; status page.

## 7. Business model

**The free tier has a variable cost of 0đ per org.** No SMS, no ZNS, no member OTP; owner/staff magic-link email rides a provider's free tier (F10); the live feed is SSE from our own server (F6); CSV export touches no metered API (F7). The only cost of a free org is a slice of one Postgres and one app process — already paid for. The `≤50 active members` cap below is therefore a **monetization boundary, not a cost boundary**. *(`DECISIONS.md` D1)*

**Per-org marginal cost is 0đ; the aggregate is 0đ only up to a ceiling, and the ceiling is the email provider.** The earlier phrasing — *"an unlimited number of free orgs never produces a loss"* — was right about unit price and wrong about quotas. Transactional-email free tiers are capped: Resend gives ~3,000 emails/month with a ~100/day ceiling, Brevo ~300/day. At ~2–3 magic links per org per month that is ~1,000 orgs against the monthly cap, but **the daily cap binds first and sign-ins are bursty** (owners sign in on the same weekday evenings), so the practical safe headroom is on the order of **a few hundred free orgs**. Past that, the first paid line item appears — a paid email plan in the tens of USD/month, funded by the paying orgs that arrive long before that many free ones. The number needs measuring rather than guessing (§10.2).

| Tier | Price | Includes |
|---|---|---|
| Free | 0đ | 1 location, ≤50 active members, self-scan + roster, live dashboard feed, **monthly attendance rankings & all reports (F7)**, **trial pipeline (F4)**, CSV export |
| Pro | ~199k VND/location/month | **Unlimited members** (the cap lifts), **additional locations + roles (F8)**, one-way Google Sheets mirror, **member OTP → needs the customer's GPKD**, **Zalo OA integration → needs the customer's GPKD** |
| Business | ~499k VND/month | Cross-location dashboard, API access, priority support |
| Credits | pass-through | ZNS wallet for parent alerts, billed at the F6 rates (120–300đ/message + surcharges) |

**Rankings and the trial pipeline are free, deliberately** *(`DECISIONS.md` D6)*. A free tier without the monthly ranking is not a reduced version of this product, it is a different product — the ranking is the job the original gig was hiring for (§1). And F4 carries the most common way a member enters the roster (§4, "the four ways in"), so paywalling it caps a free roster at whatever fits in an imported file. The `≤50 members` cap is a clean enough boundary on its own: it grows with the value the customer is getting.

Anchor: 12 months of Pro ≈ half a one-off freelancer build (5–6M). Free tier = growth engine in FB owner groups. Annual plan: pay 10 months, get 12.

**Upgrade journey — and the trap inside it.** Free → Pro activates instantly for everything *except two things: Zalo and member OTP.* Both are blocked behind the same door, and it is worth naming both rather than only Zalo: **member OTP is delivered as a ZNS authentication message (300đ), which also requires the customer's own verified OA, which requires their GPKD** (F6). So the multi-day assisted step — obtain or produce a GPKD, verify the OA, register an app — gates a *pair* of Pro features, not one.

Two consequences: (1) sell Pro on the things that switch on the instant they pay — **unlimited members, extra locations with roles, and the Sheets mirror**; treat Zalo and OTP as bonuses that arrive later. (Rankings and the trial pipeline are no longer part of the pitch: they are free, per D6.) (2) **Never make anything in the core loop depend on either** (§4 out of scope). Note that the Sheets line above is a **one-way mirror** — read there, edit in the app (F7).

**Collection mechanics (VN reality):** Stage 1 (<20 customers) — VietQR bank transfer with org code in the memo, manual admin confirmation, renewal reminders. Stage 2 — PayOS/SePay webhook auto-activates on payment (Stripe is unavailable to VN merchants). Dunning: 7-day grace → soft-lock Pro features only; **check-in and customer data are never locked** (no data hostage — reputation in FB groups is the growth channel). Legal: register a household business (HKD) or LLC for invoicing — many centers require VAT invoices; hire a bookkeeping service (~300–500k/month).

## 8. Success metrics

- **Activation:** % of signups reaching first successful check-in < 10 min (target 40%+). The clock covers the whole F1 wizard including roster import, since check-in is impossible before it.
- 10 paying orgs within 60 days of launch; ≥1 non-sports vertical among first 20 orgs (validates multi-vertical claim).
- Org week-4 retention ≥ 60%; check-in success rate ≥ 99%.
- **Ranking integrity:** anomaly-flagged check-ins < **2%** of all check-ins per month — the same threshold that triggers the §6 identity-assurance revisit.

## 9. Risks

| Risk | Level | Mitigation |
|---|---|---|
| Attendance fraud with no OTP in v1 | **Medium** (new in v2.0) | Consequence is capped at ranking distortion (§6); F9 anomaly flags + owner review before rewards; OTP available in Pro; measured by flagged rate against the 2% threshold |
| Static QR abuse discredits rankings | Medium | Same asset as the row above — two attack vectors on one target, ranking integrity. Opening-hours gate + GPS soft-check + anomaly flags in v1; rotating-QR premium later |
| Incumbents add self-serve | Medium | Speed + micro-segment focus + free tier distribution |
| Solo-builder bandwidth | Medium | Ruthless out-of-scope list; PLAN.md milestones |
| Zalo policy/pricing changes | **Low** (downgraded in v2.0) | No longer on the critical path: the free tier neither sends nor receives anything through Zalo, so a policy or price change costs us a Pro add-on, not the product. Official APIs only; the notification layer stays abstracted (Zalo today, SMS/email swappable) *(`DECISIONS.md` D1)* |

## 10. Open questions

### 10.1 Verified — closed 2026-07-25

The v1.1 open question #1 — *"Zalo OA verified-account requirements/cost for each customer's OA vs. one platform OA"* — now has an answer, and it is the reason this document is at v2.0:

- Sending any OA message or ZNS requires a **verified OA**, and verification requires **that customer's own business licence (GPKD)**.
- ZNS costs **300đ per authentication message**, billed on successful delivery, with **no free quota** (full table in §4 F6).
- Therefore Zalo left the v1 core (D1) and member OTP left the default check-in flow (D2). Precisely: OTP is not gone from v1 — it ships as a **Pro** feature in M4 (F2, §7); what it can never be is a step in the free tier's core loop, where a 300đ metered message would break the 0đ constraint.

Full reasoning and the five verification sources are in `DECISIONS.md`. The v1.1 plan to "validate in an M1 spike" is void — there is nothing left to spike, and Zalo is no longer on the critical path.

### 10.2 Still open

1. **Google Sheets API quota at multi-customer scale** — measure real write rates; write in batches per customer Sheet (gates F7's Sheet mirror to Pro).
2. ~~**Brand name & domain** — "CheckinHub" is a placeholder.~~ **Settled 2026-07-26: Checkino** (`DECISIONS.md` D11 — criteria, rejected candidates, collision checks). What remains is the domain: a manual whois of `checkino.vn` / `.com` / `.app`, then purchase.
3. **Indoor GPS accuracy at a real storefront** — determines the radius threshold for the F9 soft-check layer.
4. **The email free-tier ceiling — how many free orgs fit inside it** (§7). Needs the real number, not the estimate: measure magic links per org per month during the pilot, and measure the *daily peak*, since the daily cap (Resend ~100/day, Brevo ~300/day) binds before the monthly one. Also worth knowing before it matters: whether one paid email plan (tens of USD/month) is the right answer or whether magic-link sessions should simply live longer, which reduces the send rate at no cost.

## 11. Changelog

### v2.0 — changes from v1.1

| Change | Decision |
|---|---|
| Zalo OA/ZNS moved out of the v1 core into the Pro tier; the free tier's only notification channel is the live dashboard feed | **D1** |
| Member OTP dropped from v1 — replaced by roster match + bound device token; OTP becomes a Pro feature, Zalo Login the long-term path | **D2** |
| Authentication split in two: email magic link for owner/staff (new **F10**), device token for members; F1 no longer uses phone OTP or Zalo login | **D3** |
| `/q/{code}` is server-rendered rather than part of the SPA; PDPL residency path is `pg_dump` to a VN region, with no Supabase in the picture | **D4** |
| Free tier's variable cost stated as a hard constraint: **0đ** | cross-cutting |

The v1.1 text is preserved in git: `git show 4d227dc:PRD.md`. The reasoning, cost arithmetic, and verification sources behind every row above live in `DECISIONS.md`.

### v2.1 — changes from v2.0

**v2.1 (2026-07-25)** — patched the six roster-as-identity gaps D2 opened, all traceable to one root cause: D2 turned the roster from reporting data into identity data, but F1/F5/F7 were still written as if it were only there to be looked at. Member management is now its own feature (**F11**, spanning jobs that belong to F2/F4/F5); import is an upsert on phone that never deletes and previews before applying (F1); the Google Sheet write-direction contract is explicit — protected ranges, read there, edit in the app (F7); two-way Sheet sync and Sheet-as-roster are out of scope; "PWA" now names only `/staff` and full offline is sequenced separately from F3 (F2, F3); and the three accepted offline limits are written down so they are not later reported as bugs (§6).

### v2.2 — changes from v2.1

**v2.2 (2026-07-26)** applies D6–D10 plus the wording defects found by the PRD ↔ PLAN cross-check. Nothing here is a new decision; the reasoning is in `DECISIONS.md`.

| Change | Where | Decision |
|---|---|---|
| Monthly rankings and the trial pipeline moved from Pro to **Free**. Pro now sells on: unlimited members, extra locations + roles, Sheets mirror, member OTP, Zalo | §7 table, F4, F7 | **D6** |
| Upgrade journey: instant activation *except Zalo* → **except Zalo *and* member OTP** — both need the customer's GPKD, because OTP is a ZNS message | §7 | cross-check **G** |
| *"Unlimited free orgs never produce a loss"* corrected: per-org cost is 0đ, but the email provider's free tier caps the aggregate at roughly a few hundred free orgs | §7, §10.2 #4 | cross-check **J** |
| `program` (bộ môn) is a first-class entity, many-to-many with members, optional and skippable; optional import column; new programs listed in the F1 preview | F1, F3, F5, F7 | **D7** |
| One phone number = one member, `UNIQUE (org_id, phone_normalized)`. *"No-phone members"* removed from F3; the two accepted consequences written down; F11 rejects a duplicate number | §6, F3, F11 | **D8** |
| The 6-digit offline fallback code at `/q` is **dropped** — with no service worker there is no page to display it on. No network → a staff member checks you in via `/staff` | F2, out of scope | **D9** |
| Expiry warning split explicitly: **at check-in → F5**, **the proactive list → F7** | F5, F7 | cross-check **M** |
| Member OTP is no longer listed as out of scope — it is a Pro feature *inside* v1; only "OTP at the free tier" is out | out of scope | cross-check **E** |
| Every roster mutation and every import writes an **audit log**, in v1. The Sheet-as-roster rejection was rewritten to lead with **no permissions** (editing the Sheet = free membership), then no data constraints, then unrecoverable deletion; audit trail is the fourth reason, not the first | F1, F11, out of scope | **D10** |

### v2.3 — changes from v2.2

**v2.3 (2026-07-26)** — the product is named: **Checkino** (repo `checkino`, package `com.checkino`). No scope change; naming criteria, rejected candidates, and internet-collision checks are in `DECISIONS.md` D11. §10.2 #2 is closed except for the domain purchase.
