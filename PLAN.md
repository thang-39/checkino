# PLAN — CheckinHub v1

Implementation plan derived from `PRD.md` (v2.2) and `DECISIONS.md` (D1–D10). Assumes one
developer, part-time (~15–20h/week).

| | |
|---|---|
| Status | **v2.1 — 2026-07-26**, D6–D10 applied on top of the v2.0 D1–D5 rewrite |
| Target | **~8 weeks part-time to the end of M3** — that is the number; M4 (billing, PDPL pages, Zalo, pilot onboarding) is *not* inside it, see §4 |
| Companion docs | **`DECISIONS.md` (source of truth — wins on conflict)**, `PRD.md` (v2.2), `GRILL-LOG.md` (historical) |

**What changed from v1.1 of this document.** The stack decision was reversed (§2): Next.js +
Supabase is out, Spring Boot + Angular is in. Zalo left the critical path entirely, so the M0
spike week collapsed to ~2 days and Zalo moved from M3 to M4. Full offline capability for
`/staff` was split out of F3 into its own milestone slot (M3) and is now named as the heaviest
piece of frontend work in the project (§6). F11 (member management) was added to M2. The
6-week target became ~8 weeks — not because more was added blindly, but because the previous
number leaned on "Supabase cuts 2 weeks of scaffolding", which is no longer true.

**What changed in v2.1 of this document.** D6–D10 landed: `program` and `member_program` are real
tables (§3.1, §3.3), `member` carries `UNIQUE (org_id, phone_normalized)`, `audit_log` moved out of
*Grow later* into the core schema and is built in M1. Rankings left the "cut if late" list, because
D6 made them a free-tier feature the original gig explicitly asked for (§6). The M2 exit criteria
were corrected — the gig's monthly report lands in M3, so M2 cannot claim the whole workflow. The
"~8 weeks" figure is now stated as *to the end of M3*, which is what it always measured.

---

## 1. Platform decision: Web or App?

**Answer: Web. No native app in v1.** *(unchanged from Grill Q13 — the reasoning held.)*

| Criterion | Web | Native app | Zalo Mini App |
|---|---|---|---|
| Core loop (camera scans printed QR → opens URL) | Native fit — zero friction | Breaks loop: forces install at the door | Good — opens inside Zalo |
| Install friction for a first-time member | None | Fatal for trial signups | None (everyone has Zalo) |
| Push notifications | Not needed at free tier — the live dashboard feed is the channel (F6) | Main advantage, but redundant here | Via OA follow |
| Build & maintain (solo dev) | 1 codebase | 2 platforms + store reviews | 1 extra frontend, Zalo review |
| Distribution | Any QR/link/FB post | App Store/Play | Inside Zalo ecosystem |

Not going through an App Store is a feature, not a compromise: no review queue, no waiting on
Apple, a bug fix is a deploy.

### 1.1 "PWA" applies to `/staff` only

The v1.1 text of this document used "PWA" for the whole product. That conflated two different
claims — *"web, not native"* (the Q13 decision, product-wide) and *"installable, runs offline"*
(a technical requirement, `/staff` only). Fixed here: **PWA means installable + offline, and
only `/staff` is one.** *(`DECISIONS.md` D4)*

| Surface | manifest | service worker | What it actually is |
|---|---|---|---|
| `/q/{code}` — member | ❌ | ❌ | **An ordinary web page** — scan, look, tap, leave |
| `/staff` — teacher | ✅ | ✅ | **A full PWA** — installed icon, standalone, works offline |
| `/admin` — owner | ✅ | ❌ | A website plus a home-screen icon |

`display: "standalone"` in the manifest is what drops the address bar and makes `/staff` look
like a store-installed app.

### 1.2 `/q/{code}` is not part of the SPA

The member page is server-rendered with Thymeleaf. This is the one page in the product whose
user has **never visited the site before** — empty cache, standing at a door, on cold 4G.
Count the round trips:

| | SPA | Thymeleaf |
|---|---|---|
| | 1. fetch empty `index.html` | 1. fetch HTML that **already says** "12 sessions left" |
| | 2. fetch the JS bundle ← the painful one | |
| | 3. JS boots | |
| | 4. JS calls the API for card status | |
| | 5. text finally appears | |
| **Total** | 4–5 trips | **1 trip** |

And what is on that page? One phone input, one line of text, one button. No complex state, no
routing, no real-time. A framework buys nothing here, and the page sits in the middle of the
core loop.

**Phasing.** v1 = web (member page + staff PWA + owner admin). **Phase 2 = Zalo Mini App** as an
alternative member frontend — it would give Zalo identity and native OA-follow, but it depends on
the same verified-OA prerequisite that pushed Zalo to Pro (D1), so it waits. Native app: only if a
Business-tier customer demands it; default never.

## 2. Stack & architecture

### 2.1 Stack — Spring Boot 3.5 + Postgres 16 + Angular 20 *(D4)*

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.5 + Postgres 16 + Flyway |
| `/q/{code}` — member page | **Thymeleaf server-render** + a few dozen lines of JS |
| `/staff` + `/admin` | **Angular 20**, built to static files, served by Spring Boot |
| `/staff` specifically | **Full PWA**: `@angular/pwa` + service worker + IndexedDB *(mechanism 3)* |
| Realtime | `SseEmitter` — server-sent events for the live dashboard (~30 lines) |
| Email | Resend or Brevo free tier — magic link for owner/staff (F10) |
| Deploy | Fly.io or one VPS + docker compose + Caddy — **no Kubernetes** |

**No SSR, no Node runtime in production.** `ng build` emits static files; Maven packages them
into the jar; Spring Boot serves them from `static/app/`. This needs one small piece of glue: a
**controller fallback returning `index.html`** for client-side routes such as `/admin/members`,
so a deep link or a refresh doesn't 404.

### 2.2 Why Next.js + Supabase was reversed

The v1.1 stack decision (2026-07-19) chose Next.js + Supabase, itself a reversal of Grill Q14.
It rested on one real advantage: **Supabase ships phone OTP**.

- That advantage is gone. Supabase phone auth runs through Twilio/Vonage — expensive and with
  poor deliverability to VN numbers. Delivering OTP over ZNS means **writing auth by hand on any
  stack**. Then D2 took member OTP out of the core check-in flow entirely (it survives only as a
  Pro feature in M4), so the advantage did not just shrink, it disappeared.
- Everything else Supabase provided has a cheap equivalent: Realtime → `SseEmitter`; RLS →
  a plain Postgres feature, not a Supabase one; managed Postgres → Neon / Railway / Fly.
- No technical constraint forces it. Estimated load is **1–2 writes/second** (1,000 centers ×
  100 check-ins/day). This is a product meant to live for years, maintained by one person —
  the stack that person is fluent in wins.

**And not Next.js regardless of the frontend framework**: it drags in a Node runtime to operate,
which is exactly the thing D4 removed.

### 2.3 React was reconsidered on 2026-07-25 and rejected

The frontend question has now flipped three times (Q14 Angular → 19/07 Next.js+React → D4
Angular). It was examined properly and the result is recorded so there is no fourth flip.

The proposal was React + Vite, motivated by *"React feels faster to build in"*. That is real, but
it lives in the ecosystem and in AI assistance — not in the framework. The two technical
advantages usually cited **do not apply to this problem**:

| React's claimed advantage | Applies here? |
|---|---|
| Small bundle, fast cold start | **No.** The cold-start-sensitive page is `/q`, already Thymeleaf. `/staff` and `/admin` sit behind a login, are used daily, and are installed — bundle size buys nothing |
| Simpler, smaller conceptual surface | **Partly.** But you then assemble router / data fetching / forms / PWA plugin yourself; Angular CLI hands them over |
| Wider ecosystem, better AI support | **Yes** — the one genuine advantage, and the one deliberately traded away |

Three reasons to keep Angular:

1. **It is the builder's day-job stack** *(Q14)* — existing fluency, conventions, tooling.
2. **The hardest part of this frontend is framework-independent.** The offline outbox and sync
   are hand-written code either way. React removes not one line of the largest risk (§6).
3. **`/staff` needs a PWA and Angular ships one.** `ng add @angular/pwa` generates the manifest,
   the service worker and `ngsw-config.json`, where caching is declared in JSON. On React you
   wire up `vite-plugin-pwa` yourself. Angular helps most on precisely the heaviest part.

**The tie-breaker, fixed:** *the stack you are fluent in wins, unless a specific technical
constraint forces the change.* Checked 25/07 — there is none. Reopening this question requires
naming a technical constraint, not a feeling. *(`DECISIONS.md` D4)*

### 2.4 Architecture — monorepo + modular monolith, one process *(D5)*

One repo. **One process at runtime.** One deploy. Microservices are **not on the table**, now or
later, absent a team.

Two words that get conflated are actually independent axes: *repo* (monorepo ↔ multi-repo) and
*runtime* (monolith ↔ microservices). This is **monorepo + monolith**.

```
manage-pwa/
├── backend/                        # Spring Boot 3.5
│   ├── src/main/java/com/checkinhub/...
│   ├── src/main/resources/
│   │   ├── templates/q/            # Thymeleaf — /q/{code}
│   │   ├── db/migration/           # Flyway
│   │   └── static/app/             # ← Angular bundle copied in at build time
│   └── pom.xml
├── frontend/                       # Angular 20 → /staff + /admin
│   ├── src/
│   ├── ngsw-config.json            # service worker config (the /staff PWA)
│   └── angular.json
├── docker-compose.yml              # postgres + app (+ caddy)
└── Dockerfile
```

Build: `ng build` → static files → copied into `static/app/` → Maven packages **one** jar →
**one** image. `frontend-maven-plugin` can run `ng build` inside the Maven lifecycle, or a
shell script can call it — for one person the script is simpler and needs no plugin.

**Why not microservices — the strongest reason is inside the three mechanisms of §3.2.** The
usual argument applies (load is 1–2 writes/second; microservices solve an *organizational*
problem — many teams deploying independently — and there is one person here, so there is no
coordination cost to solve). But the harder reason is mechanical:

- **Mechanism 1 requires the check-in insert and the session decrement in one transaction.**
  Split `checkin-service` from `entitlement-service` and `@Transactional` becomes a **distributed
  saga with compensating actions** (check-in written, decrement fails, call back to delete, the
  delete can fail too…). That trades one annotation for a compensation system, to serve 2
  writes/second.
- **Mechanism 2 breaks too.** RLS with `SET LOCAL app.org_id` only means anything within **one
  connection, one transaction**. Split the services and the isolation layer is gone; `org_id`
  has to be policed by hand at every call site — the exact work RLS was chosen to avoid.

All three settled mechanisms rest on **one database, one transaction**. Microservices break all
three.

**But keep the exit — modular monolith.** One process, code split by business domain; see the
package layout in §3.3. The one rule that must hold: **modules call each other through
interfaces and never reach into each other's repositories.** Hold that, and if a split is ever
needed in five years the seams already exist; if it never is, nothing was lost. Same spirit as
the D4 exit door: **prepare the escape route, don't build the road.**

**Two things not to split out**, despite sounding async:

| Job | Sounds like it needs its own service | Reality |
|---|---|---|
| Zalo/ZNS sends, daily digest | ✅ | A `notification_outbox` table **is** the queue, plus one `@Scheduled` sweep. Postgres is a good enough message queue at this scale |
| Google Sheet mirror | ✅ | A periodic batched-write job inside the same app |

**Who serves the Angular static files.** **Spring Boot, from `static/`** — exactly one artifact
to deploy and to roll back, dev and prod identical. Caddy only terminates TLS. Having Caddy serve
the static files and proxy `/api` is better for cache headers but adds a second place to keep in
sync; switching later is a ten-minute change.

### 2.5 Exit door — PDPL data residency

Unchanged in intent, cheaper in practice now that Supabase is out. **Business logic lives in the
application layer, never in the database.** Migration = `pg_dump` → managed Postgres in a VN
region (Viettel/VNG/FPT) → change one connection string. There is no vendor-specific auth left to
swap, because auth is ours (F10 magic link, F2 device token). The MVP hosting region is disclosed
in the privacy policy until the move happens. That move is *already planned* for PDPL residency
once paying customers arrive — compliance and scaling point at the same destination, so nothing
is wasted.

## 3. Domain model

### 3.1 Tables

**Core — ship first:**

| Table | Notes |
|---|---|
| `org` | tenant root; every org-scoped table carries `org_id` for RLS |
| `scan_point` | **one QR code in one place**: location, QR code, opening hours, geo coords, plus `program_id NULL` — NULL means the QR is shared by the whole site *(F1, F9, D7)* |
| `program` | `(id, org_id, name, active)` — bộ môn. **Optional**: an org may have none, in which case every scope is org-wide *(D7)* |
| `member_program` | `(member_id, program_id)` — many-to-many. This is what the `/staff` roster filter and the per-program ranking read; deriving them from the scanned QR was the defect D7 fixed |
| `member` | name, normalized phone (**the identity key** — D2), consent fields, active flag. **`UNIQUE (org_id, phone_normalized)`** *(D8)* — the constraint the F1 upsert and the F11 phone correction both rely on |
| `member_device` | `(id, member_id, token_hash, user_agent, created_at, last_seen_at, revoked_at)` — device token, **hash only**, max 3 per member, oldest evicted *(D3, F2)* |
| `staff_user` | email, role, org — magic-link auth *(D3, F10)*; roles per F8 |
| `entitlement` | type, scope, valid window, session quota, `consume_policy` *(F5)* |
| `checkin_event` | member, scan_point, timestamp, `dedupe_bucket`, `client_event_id`, source `SELF｜ROSTER`, anomaly flags |
| `lead` | trial pipeline: name, phone, program, consent, follow-up status *(F4)* |
| `notification_outbox` | the queue itself — a `@Scheduled` sweep drains it *(D5, F6)* |
| `audit_log` | `(id, org_id, actor_staff_user_id, action, entity_type, entity_id, summary, created_at)` — `summary` stays human-readable (*"import: 12 new, 3 updated"*) so the owner can read it without tooling. **Table built in M1** *(D10)* |

**Grow later:** `credit_wallet` + `ledger` (ZNS parent alerts), `subscription` (billing).

`audit_log` used to be in that list and was moved up *(D10)*. Two reasons: the roster is identity
data since D2, so mutating it deserves a trace; and the PRD's reason for refusing Sheet-as-roster
cites an audit trail the app was not going to have in v1. The table is cheap — one write per
mutation, roughly half a day to a day in total — and each feature writes its own entries as it is
built: F1 import in M1, F11 mutations in M2.

### 3.2 The three mechanisms that are easy to get wrong

Full reasoning and sample code live in `DECISIONS.md`. Summarized here because they constrain the
milestone order:

1. **Race-safe dedupe pushed down to a DB constraint** —
   `UNIQUE INDEX (member_id, scan_point_id, dedupe_bucket)` + `INSERT ... ON CONFLICT DO NOTHING
   RETURNING id` (no row returned → already checked in). **Never** an app-level `if (!exists)`:
   two concurrent scans both see "not there" and write two rows. `dedupe_bucket` is derived from
   the entitlement's consume policy — date for `ONCE_PER_DAY`, random UUID for `PER_VISIT`,
   `date#slot` for `PER_CLASS`. The insert and the session decrement share one transaction;
   `UPDATE ... WHERE sessions_used < session_quota` with affected-rows = 0 → out of sessions →
   rollback.
2. **Multi-tenant isolation, two layers, and the test layer is mandatory** — Postgres RLS keyed
   on `current_setting('app.org_id')`, with `SET LOCAL app.org_id` at the start of every
   transaction (a `shared/` interceptor does this); **plus** an automated cross-tenant test suite
   that, for every endpoint, uses org A's token against org B's data and asserts 403 or empty.
   **Written before the features, not after** — without layer 2, layer 1 is a belief.
3. **Offline roster — idempotency key *and* service worker** — taps write to an IndexedDB outbox
   as `{client_event_id: uuid, member_id, ...}`, the UI ticks optimistically, the batch POSTs when
   connectivity returns, and the server dedupes on a unique index over `client_event_id`. Ten
   retries still yield one row, so no conflict-resolution logic is needed. Two distinct jobs, both
   required: the **service worker** lets the app *open* with no network (it caches the shell); the
   **IndexedDB outbox** keeps taps from being lost (it holds the data).

### 3.3 Package layout — by business domain *(D5)*

```
com.checkinhub
├── org/            # tenant, scan_point, program, opening hours, GPS
├── member/         # member, member_device, member_program
├── entitlement/    # packages/cards, consume policy
├── checkin/        # checkin_event, dedupe bucket
├── trial/          # lead pipeline (F4)
├── report/         # rankings, CSV, Sheet mirror
├── notification/   # outbox, Zalo (Pro)
├── auth/           # magic link, device token
└── shared/         # config, RLS interceptor, SSE, audit log
```

**Where the two new pieces live** *(D7, D10)*:

- **`program` goes in `org/`**, next to `scan_point`. A program is part of how the owner configures
  their org — it is created in the F1 wizard, it is CRUD'd in `/admin` alongside locations, and
  `scan_point.program_id` references it. Putting it in `member/` would be reading the link table
  backwards. **The join table `member_program` goes in `member/`**, because that is the side that
  owns the membership relation and the side `/staff`'s roster query starts from.
- **`audit_log` goes in `shared/`**, exposed as one small `AuditLog.record(...)` interface. Every
  domain module writes to it, so it cannot live inside any one of them without breaking the rule
  that modules don't reach into each other's repositories (§2.4).

## 4. Milestones

Zalo no longer blocks anything, so the old "spike week" is gone and the order now follows what a
design partner can actually use.

**What "~8 weeks" covers.** It is **M0 through M3** — the end of M3 is the first point where a
design partner is using the product for real and the original gig's requirements are all met. The
week ranges below overlap (M1 W1–3, M2 W3–5, M3 W5–7) because part-time weeks are not clean
boundaries: the last days of one milestone bleed into the first of the next. **M4 is not inside the
8 weeks and should not be read as "week 7 to week 8"** — billing, the PDPL pages, the whole Zalo
Pro surface, member OTP, and onboarding 5–10 pilot centers is several weeks of work on its own, and
it is gated on the M3 exit anyway. M4 is dated by its exit criterion (a first paying customer), not
by a week number.

### M0 — Design partner + the one remaining spike (~2 days)

- Sign the original FB gig as **design partner #0** (paid 5–6M or free pilot — either way: real
  requirements plus a testimonial).
- Test browser geolocation accuracy at a real storefront (indoors) → sets the F9 soft-check
  radius. This is the only PRD open question that needs a physical spike.
- **The Zalo spike is dropped from the critical path.** Its questions are answered (verified OA
  requires the customer's GPKD; ZNS is 300đ/message with no free quota) and the answers moved
  Zalo to Pro *(D1)*. There is nothing left to spike.
- Exit criteria: a design partner is committed.

### M1 — Walking skeleton (Weeks 1–3)

- Monorepo scaffold: `backend/` Spring Boot + Postgres + Flyway + docker compose; `frontend/`
  Angular 20 building into `static/app/` + the `index.html` controller fallback *(D5)*.
- Schema `org` / `scan_point` / `program` / `member_program` / `member` (with
  `UNIQUE (org_id, phone_normalized)` — D8) + `audit_log` + RLS + **the cross-tenant isolation test
  suite** *(mechanism 2 — first, not last)*. `audit_log` is part of the founding schema, not a later
  addition *(D10)*.
- F10 email magic link for owner/staff; F8 roles skeleton.
- F1: create org → scan point → *(optional)* programs → **roster import** → printable QR poster
  (PDF). Import is an **upsert on normalized phone, never deleting, with a preview screen** —
  *"12 new, 3 updated, 185 unchanged"* — confirmed before anything is written.
- F1 program step *(D7)*: creating programs is a **skippable** wizard step; the import file's
  program column is optional; the preview additionally lists programs that would be newly created
  (*"will create 2 new programs: Boxing, Yoga"*) and creates them only on confirm. Skip it all and
  every scope is org-wide — a single-program center never sees the concept.
- F1 import writes an `audit_log` entry per run, with the diff counts in `summary` *(D10)*.
- F2 happy path: `/q/{code}` Thymeleaf page → enter phone → roster match → bind device token →
  check-in recorded; unknown number → F4 form.
- Live owner dashboard feed over SSE *(F6 free tier)*.
- Exit criteria: end-to-end demo on a real phone at a real door **and** re-importing last term's
  file loses nobody.

### M2 — Entitlements, dedupe, roster, member management (Weeks 3–5)

- F5 entitlement model + consume policies + validity display at check-in.
- Race-safe dedupe: unique index + `ON CONFLICT DO NOTHING` + Testcontainers concurrency tests
  *(mechanism 1)*.
- F9 free anti-abuse layers: opening-hours gate, GPS soft-check, anomaly flags (the five signals
  in PRD F9).
- F5 expiry warning **at check-in** — the proactive expiring-cards *list* is a report and lands
  with F7 in M3 *(PRD F5/F7)*.
- **F11 member management** — assign/renew/freeze a card, convert a lead, fix a mistyped phone,
  revoke a device token, mark inactive, add one member by hand, edit program membership
  (`member_program`). **In usability terms F11 comes before F5 and F4**: without it there is nowhere
  to click to assign a card or convert a lead.
  - **Every one of those mutations writes an `audit_log` entry** *(D10)* — the table exists from M1,
    so this is one call per handler, not new infrastructure.
  - **Correcting a phone number must check the unique constraint and refuse** if the number already
    belongs to another member in the org *(D8)*.
- F3 staff roster: today's list + **program filter** *(reading `member_program`, not the scanned
  QR — D7)* + IndexedDB outbox + `client_event_id` idempotency — **service worker not yet**
  (see M3).
- F4 trial registration + pipeline list.
- Exit criteria: the entire original FB gig workflow runs **except the monthly report**, and **with
  no Zalo anywhere**. The report is deliberately excluded from this line — the gig asked for a
  monthly attendance ranking and that is F7, in M3. Claiming "the entire workflow" here was the
  cross-check defect **B**.

### M3 — Offline level 2 + reports (Weeks 5–7)

- **Offline level 2 for `/staff`** *(split out of F3)*: `ng add @angular/pwa` +
  `ngsw-config.json` (`assetGroups` with `installMode: prefetch` for the shell, `dataGroups` for
  the API) + cache today's roster into IndexedDB rather than memory + a no-network boot path +
  `SwUpdate` `versionUpdates` driving an explicit *"new version, reload"* bar — **never a silent
  swap**, which would discard in-memory taps mid-class — + **the iOS install-instruction screen**.
  Level 1 (outbox without a service worker) was explicitly rejected: it costs nearly as much and
  fails exactly when needed *(`DECISIONS.md` mechanism 3)*.
- F7: monthly ranking **per `program`** *(the real table — D7 — not per QR code)* and per location,
  attendance history, the proactive **expiring-cards list** *(the at-check-in warning shipped with
  F5 in M2)*, trial conversion, CSV export. **All of that is free tier** *(D6)*; only the **one-way
  Google Sheet mirror** is Pro — protected ranges, a pinned warning line, batched writes, no
  inbound path.
- Exit criteria: ① turn on airplane mode → **reload the page** → the app still opens and records
  check-ins → sync completes fully when connectivity returns; ② design partner #0 is using it for
  real.

### M4 — Billing, Zalo (Pro), pilot (after M3; not inside the ~8 weeks)

- Billing: Free/Pro; VietQR + manual confirmation first, PayOS/SePay webhook later; 7-day grace →
  soft-lock Pro features only, **never check-in or data**.
- PDPL pages: consent, privacy policy, deletion request.
- **Then** Zalo OA/ZNS as a Pro feature *(D1)*: OA connect flow, daily owner digest, member OA
  messages, ZNS parent-alert credit wallet — all draining `notification_outbox`.
- **Member OTP is built here too, and it is a Pro feature that is *in* v1 — not out of scope.** The
  PRD is explicit about this now: what is out of scope is member OTP *at the free tier*. It sits in
  M4 rather than earlier for one reason: it is delivered as a ZNS authentication message (300đ), so
  it needs the customer's own verified OA — the same GPKD gate as Zalo. Both Pro features unlock
  together, which is why the PRD's upgrade journey names two exceptions rather than one *(PRD §7)*.
- Onboard 5–10 pilot centers from FB groups (target ≥1 non-sports center — PRD success metric).
- Instrument the activation funnel (signup → first check-in time).
- Exit criteria: a first paying customer.

### Feature-to-milestone map

**Every PRD feature has exactly one *owning* milestone; three features have a Pro slice split out
into M4, and F3 has its offline slice split into M3.** The earlier wording — *"every feature appears
in exactly one milestone"* — contradicted the table directly underneath it, which already showed F3
in M2+M3 and F6/F8 in M1+M4 (cross-check defect **F**). The splits are deliberate, not slippage: in
each case the free-tier or usable part ships first and the part gated on payment or on the heaviest
frontend work follows.

| Feature | Milestone |
|---|---|
| F1 onboarding + roster import (upsert, preview) + optional programs | M1 |
| F2 member self check-in + device token | M1 |
| F10 owner/staff magic link | M1 |
| F8 multi-location & roles | **M1** (owner, roles skeleton) · M4 (Pro/Business: extra locations, cross-location dashboard) |
| F6 free-tier live feed (SSE) | **M1** (the whole free-tier notification channel) |
| `audit_log` table *(D10)* | M1 (table + F1 import entries) · M2 (F11 entries) |
| F5 entitlements + at-check-in expiry warning | M2 |
| F9 anti-abuse ladder | M2 |
| F11 member management | M2 |
| F3 staff roster + outbox + program filter | **M2** (usable without offline) |
| F4 trial registration + pipeline *(free tier — D6)* | M2 |
| F3 offline level 2 (service worker, IndexedDB roster, iOS install screen) | M3 (the split-out slice) |
| F7 reports, rankings, expiring list, CSV — **free** *(D6)* | M3 |
| F7 one-way Google Sheet mirror — **Pro** | M3 |
| F6 Pro tier — Zalo OA/ZNS, digest, ZNS wallet | M4 (the Pro slice) |
| Member OTP — **a Pro feature in v1**, not out of scope | M4 |

## 5. Go-to-market (first 90 days)

1. Deliver design partner #0 (the FB gig) → case study with real numbers.
2. Post the case study in the same FB owner groups (gym/võ/yoga owners, trung tâm ngoại ngữ) —
   where demand already self-identifies.
3. Free tier as the default answer to every *"tìm freelancer làm app điểm danh"* post. Serving one
   more free org costs 0đ in variable cost *(D1)*, so there is no reason to hold back — with one
   ceiling to keep an eye on: the transactional-email free tier caps the *aggregate* at roughly a
   few hundred free orgs (PRD §7, §10.2 #4). That is a good problem to have and a cheap one to fix.
4. Recruit 2–3 AppSheet freelancers as affiliates (they keep the setup fee, you keep the
   subscription) — Grill Q8.

## 6. Risk register (delta from PRD §9)

| Risk | Trigger | Response |
|---|---|---|
| **`/staff` offline level 2 is the heaviest frontend work in the project** | M3 | Budgeted, not hoped: **~2–3 days part-time on top of the outbox**, itemized as `ng add @angular/pwa` + `ngsw-config.json` (~½ day), roster into IndexedDB instead of RAM (~½ day), no-network boot path (~½ day), and ~1 day fighting the two classic service-worker pains — deploying and still seeing the old build because the old worker serves from cache, and debugging phantoms because you believe you are running new code. **Plus ~½ day for the iOS install-instruction screen, which is not optional**: Safari never offers to install, nobody finds Share → "Add to Home Screen" unaided, and skipping it makes the entire offline effort useless for roughly half the users. If M3 slips, this piece is the reason — and it is still not the thing to cut, because the milestone before it ships an outbox that *feels* offline-capable and dies on reload |
| Attendance fraud with no OTP in v1 | Continuous; PRD §8 threshold | Consequence is capped at ranking distortion (PRD §6). F9 flags + owner review before rewards. Revisit — enable Pro OTP or rotating QR — if a paying customer attaches real money to the ranking **or** flagged check-ins exceed **2%/month** |
| Cross-tenant data leak | Any new endpoint | Mechanism 2: RLS **plus** the cross-tenant test suite in CI, written in M1 before the features. A new endpoint without a cross-tenant test is not done |
| Dedupe race writes duplicate check-ins | M2 concurrency tests | Mechanism 1: DB unique index + `ON CONFLICT DO NOTHING`, verified by Testcontainers tests that fire concurrent scans. Never an app-level existence check |
| Offline roster sync loses or duplicates taps | M2/M3 tests | Idempotency key `client_event_id` with a server-side unique index. Resends are free; no conflict resolution to get wrong |
| Stale cached roster offline | Live use in M3 | **Accepted limit, not a bug** (PRD §6): a roster cached at 8am misses a 5pm addition. Workaround is the owner correcting it afterwards in F11. Not solved in v1 |
| Solo timeline slips | Any milestone +1 week late | Cut in this order — and it is now a list of **two**: ① **F7 Sheet mirror** (Pro-only, and the one feature with an unresolved external quota question) → ② **the F9 GPS layer** (the softest of three anti-abuse layers; opening-hours gate and anomaly flags stay). **F7 rankings left this list** *(D6)*: they are a free-tier feature and the exact thing the original gig hired for, so cutting them means failing design partner #0. **Never cut** F2/F3/F5/F11/F7 rankings, nor the M3 offline work, nor the mechanism-2 test suite. Note the knock-on if ② is cut: the M0 GPS accuracy spike is wasted, and it is the only remaining item in that milestone — so cut the GPS layer *before* doing the spike, not after |
| Google Sheets API quota at multi-customer scale | M3 | Batched writes per customer Sheet; Sheet mirror is Pro-gated, so the blast radius is bounded. Still open — PRD §10.2 |
| Zalo policy/pricing change | Any time | **Low.** No longer on the critical path (D1): the free tier neither sends nor receives through Zalo, so a change costs a Pro add-on, not the product. Official APIs only; the notification layer stays abstracted behind the outbox |

Removed from the v1.1 register: *"Outgrowing Supabase / PDPL residency"* — there is no Supabase.
The residency path is now plain `pg_dump` to a VN region with no vendor-specific auth to swap
(§2.5).

## 7. Definition of done — v1

Split in two, because "zero contact with the founder" is true of the free tier and **not** true of
Pro — Pro is collected by VietQR with a human confirming the transfer (PRD §7), so folding a Pro
feature into a no-contact promise made the promise false (cross-check defect **N**).

**Free tier — genuinely zero contact with the founder.** A center owner found via FB can, entirely
self-serve: sign up with an email magic link, import their existing member roster (optionally with
programs), print a QR poster, have an adult member self-check-in and see their remaining validity,
have a teacher roster-check a kids' class **including with the phone in airplane mode after a
reload**, manage members and cards in `/admin`, watch the live check-in feed, **see the monthly
attendance ranking**, and export everything to CSV.

That closes every requirement in the original FB gig, generalized to any vertical — **and it closes
them without Zalo and without contacting us**. The ranking and the trial pipeline are inside this
list rather than behind the paywall, per D6; they are the gig's actual deliverable.

**Pro tier — one deliberate human step, and only one.** Upgrading is not self-serve at stage 1:
VietQR transfer with the org code in the memo, an admin confirming it by hand (PRD §7). Everything
that unlocks — unlimited members, extra locations with roles, the one-way Google Sheet mirror — then
works without further contact. Two Pro features need *more* than our confirmation, and it is the
customer's own paperwork rather than our support load: **Zalo OA/ZNS and member OTP both require the
customer's verified OA, hence their GPKD** *(D1)*. Stage 2 (PayOS/SePay webhook auto-activation)
removes our manual step; nothing removes the GPKD one.

"Receives the daily Zalo digest" was in the v1.1 definition of done and is removed: it is a Pro
feature (M4) and requires the customer's own verified OA *(D1)*.
