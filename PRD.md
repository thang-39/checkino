# PRD — CheckinHub (working name)

**One-line:** Self-serve QR check-in & membership SaaS for any class-based center in Vietnam — gym, martial arts, yoga, language, arts, tutoring. The free tier runs end-to-end at **zero variable cost**; Zalo notifications are a Pro add-on, not a dependency.

| | |
|---|---|
| Author | Thang Tran |
| Date | 2026-07-19 (v1.0/v1.1) · 2026-07-25 (v2.0, v2.1) |
| Status | Draft v2.1 — 2026-07-25, roster-as-identity gaps patched (F11, upsert import, Sheet write-direction, offline limits) |
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

**F1. Self-serve onboarding.** Owner signs up with **email + magic link** (F10) → creates org → locations → programs (bộ môn) → **imports the member roster** (CSV upload or paste from Excel; they already keep one) → downloads a printable QR poster per location/program. Target: **signup → first successful check-in < 10 minutes, no human contact** (north-star activation metric) — and the roster import is inside those 10 minutes.

The import step is **mandatory, not optional**: F2 identifies a member by matching their phone number against this org's roster, so an empty roster means nobody can check in. The onboarding wizard must say so, because the failure is silent and confusing otherwise — an owner who prints the QR and scans it themselves, before importing anyone, lands in the F4 trial form rather than a check-in, and concludes the product is broken. Wizard order is therefore: org → scan point → **roster** → QR poster.

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
- **Network failure fallback:** the page shows a 6-digit code for staff to key in manually.
- **Abuse signal:** one phone number binding on two devices within a short window raises an anomaly flag (F9) — it is logged and surfaced, not blocked.
- **Upgrade path, not v1:** OTP is a **Pro** feature (a ZNS auth message costs 300đ — §4 F6). The better long-term answer is **Zalo Login (OAuth)**, which is not billed per message; it costs only the one-time OA verification.

Why no OTP: it defends against exactly one attack — A entering B's number to check in on B's behalf — whose entire consequence is a distorted attendance ranking. See §6 (identity assurance NFR) for the accepted risk and its ceiling.

**F3. Staff roster check-in (`/staff` — the one surface that is a PWA).** Staff opens today's roster (filter by program), taps names present. Each tap writes to an IndexedDB outbox with a `client_event_id`, the UI ticks immediately, and the batch POSTs when connectivity returns (§6 offline NFR). Covers kids' classes and no-phone members.

**Full offline capability is a separate, sequenced piece of work, not part of the first cut of F3.** The outbox above survives a network that drops *while the page is open*; it does not survive a reload. Making `/staff` genuinely offline-capable adds: `@angular/pwa` (manifest + service worker + `ngsw-config.json`), caching today's roster into IndexedDB rather than memory, a no-network boot path, an explicit `SwUpdate` "new version available, reload" bar instead of silent swaps mid-class, and **an iOS install-instruction screen** (see §6). Splitting it out lets staff use roster check-in before offline lands; shipping the outbox alone was rejected as the worst of both worlds, since it *feels* offline-capable and then fails on reload (`DECISIONS.md`, mechanism 3). The three limits that remain even when this is done are in §6 — they are deliberate, not defects.

**F4. New member / trial registration.** Unknown phone scanning QR → registration form (name, phone, program, consent checkbox) → lands in owner's "trial pipeline" list with follow-up status (contacted / converted / lost). Converting a lead into a member happens in **F11**. This is also where an owner's own test scan lands before they import a roster (F1).

**F5. Entitlements (membership cards/packages).**
`{ type: TIME_BASED | SESSION_PACK | COURSE_TERM | TRIAL | DROP_IN; scope: program(s) | whole org; valid window; session quota; consume policy }`.
Owner creates plan templates here; **assigning, renewing and freezing a specific member's card happens in F11**. Expiry warnings surfaced at check-in and in owner dashboard.

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

**F7. Reports, rankings & data ownership.** Monthly attendance ranking per program/location (the gig's "khen thưởng" list), attendance history per member, expiring-cards list, trial conversion. **CSV export at any tier, including free** (0đ, no external quota). **Near-real-time one-way mirror into the customer's own Google Sheet** (their Drive, their property; monthly archive tabs) is a **Pro** feature — gated on API quota risk at multi-customer scale (§10.2), not on cost. ToS guarantee: cancel anytime, the Sheet and every export stay with the customer. Architecture stance (from the "bridge-only" review): the operational DB is ours — speed, transactions, race-safe dedupe; the customer's Sheet is the always-fresh visible copy. Never the reverse (Grill Q9).

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

**The four ways into the roster, after F11 exists** — deliberately non-overlapping, and F4 carries the common case of a single newcomer:

| Situation | Path | Owner typing |
|---|---|---|
| Day one, Excel already exists | File import *(F1)* | none |
| New term, a whole class added | Re-import, upsert on phone *(F1)* | none |
| One newcomer walks in | **F4** — they fill in the trial form themselves → owner converts | one tap *(F11)* |
| Fix a name, change a number, end a membership | Member management *(F11)* | a few fields |

### Out of scope — v1 (Grill Q15)

Class scheduling/booking/capacity · payment processing & tuition collection · payroll/PT commissions · belt/grade tracking · homework/lesson reports · hardware (turnstiles, fingerprint) · native iOS/Android apps · unofficial Zalo group posting · multi-language UI (VN-only; EN later) · **SMS/ZNS OTP for members** (Pro upgrade, see F2) · **Zalo as a required dependency** — no flow in the core product may block, degrade, or wait on the customer having a verified OA · **two-way Google Sheet sync** and **managing the roster by typing into the Sheet** (F7) — the roster is authentication data since D2, and it will not live in a file that anyone holding the link can edit with no audit trail; the app is the system of record, the Sheet is a mirror.

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
- **Identity assurance is soft by design in v1.** Member identity rests on a roster match only; there is no OTP *(F2, `DECISIONS.md` D2)*.
  - **Accepted risk:** A enters B's number and checks in on B's behalf.
  - **Consequence ceiling:** a distorted monthly attendance ranking. No money moves and no sensitive data is exposed — the screen shows only "12 sessions left".
  - **Compensating controls:** the F9 anomaly flags, plus owner review of the flag list before monthly rewards are handed out.
  - **Revisit thresholds (explicit, so this isn't a permanent shrug):** enable OTP (Pro) or rotating QR when *either* a paying customer attaches real money to the ranking, *or* flagged check-ins exceed **2% in a month** (§8).
- **Availability:** single-region OK for v1; daily backups; status page.

## 7. Business model

**The free tier has a variable cost of 0đ.** No SMS, no ZNS, no member OTP; owner/staff magic-link email rides a provider's free tier (F10); the live feed is SSE from our own server (F6); CSV export touches no metered API (F7). The only cost of a free org is a slice of one Postgres and one app process — already paid for. **So an unlimited number of free orgs never produces a loss**, and the `≤50 active members` cap below exists as a **monetization boundary, not a cost boundary**. *(`DECISIONS.md` D1)*

| Tier | Price | Includes |
|---|---|---|
| Free | 0đ | 1 location, ≤50 active members, self-scan + roster, live dashboard feed, CSV export |
| Pro | ~199k VND/location/month | Unlimited members, rankings, trial pipeline, one-way Google Sheets mirror, member OTP, **Zalo OA integration (requires the customer's own verified OA → their GPKD)** |
| Business | ~499k VND/month | Multi-location dashboard, roles, API access, priority support |
| Credits | pass-through | ZNS wallet for parent alerts, billed at the F6 rates (120–300đ/message + surcharges) |

Anchor: 12 months of Pro ≈ half a one-off freelancer build (5–6M). Free tier = growth engine in FB owner groups. Annual plan: pay 10 months, get 12.

**Upgrade journey — and the trap inside it.** Free → Pro activates instantly for everything *except* Zalo. Zalo is a separate, assisted, multi-day step: the customer must obtain or produce a GPKD, verify their OA, and register an app before a single message can be sent. Two consequences: (1) sell Pro on rankings, unlimited members, and Sheets sync — features that switch on immediately — and treat Zalo as a bonus that arrives later; (2) **never make anything in the core loop depend on it** (§4 out of scope). Note that the Sheets line above is a **one-way mirror** — read there, edit in the app (F7).

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
- Therefore Zalo left the v1 core (D1) and member OTP was dropped from v1 (D2).

Full reasoning and the five verification sources are in `DECISIONS.md`. The v1.1 plan to "validate in an M1 spike" is void — there is nothing left to spike, and Zalo is no longer on the critical path.

### 10.2 Still open

1. **Google Sheets API quota at multi-customer scale** — measure real write rates; write in batches per customer Sheet (gates F7's Sheet mirror to Pro).
2. **Brand name & domain** — "CheckinHub" is a placeholder.
3. **Indoor GPS accuracy at a real storefront** — determines the radius threshold for the F9 soft-check layer.

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
