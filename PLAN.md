# PLAN — CheckinHub v1

Implementation plan derived from `PRD.md` (v2.1) and `DECISIONS.md` (D1–D5). Assumes one
developer, part-time (~15–20h/week).

| | |
|---|---|
| Status | **v2.0 — 2026-07-25**, rewritten against D1–D5 |
| Target | pilot-ready in **~8 weeks** part-time |
| Companion docs | **`DECISIONS.md` (source of truth — wins on conflict)**, `PRD.md` (v2.1), `GRILL-LOG.md` (historical) |

**What changed from v1.1 of this document.** The stack decision was reversed (§2): Next.js +
Supabase is out, Spring Boot + Angular is in. Zalo left the critical path entirely, so the M0
spike week collapsed to ~2 days and Zalo moved from M3 to M4. Full offline capability for
`/staff` was split out of F3 into its own milestone slot (M3) and is now named as the heaviest
piece of frontend work in the project (§6). F11 (member management) was added to M2. The
6-week target became ~8 weeks — not because more was added blindly, but because the previous
number leaned on "Supabase cuts 2 weeks of scaffolding", which is no longer true.

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
  stack**. Then D2 removed member OTP from v1 altogether, so the advantage did not just shrink,
  it disappeared.
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
| `scan_point` | location/program, QR code, opening hours, geo coords *(F1, F9)* |
| `member` | name, normalized phone (**the identity key** — D2), consent fields, active flag |
| `member_device` | `(id, member_id, token_hash, user_agent, created_at, last_seen_at, revoked_at)` — device token, **hash only**, max 3 per member, oldest evicted *(D3, F2)* |
| `staff_user` | email, role, org — magic-link auth *(D3, F10)*; roles per F8 |
| `entitlement` | type, scope, valid window, session quota, `consume_policy` *(F5)* |
| `checkin_event` | member, scan_point, timestamp, `dedupe_bucket`, `client_event_id`, source `SELF｜ROSTER`, anomaly flags |
| `lead` | trial pipeline: name, phone, program, consent, follow-up status *(F4)* |
| `notification_outbox` | the queue itself — a `@Scheduled` sweep drains it *(D5, F6)* |

**Grow later:** `credit_wallet` + `ledger` (ZNS parent alerts), `subscription` (billing),
`audit_log`.

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
├── org/            # tenant, scan_point, opening hours, GPS
├── member/         # member, member_device
├── entitlement/    # packages/cards, consume policy
├── checkin/        # checkin_event, dedupe bucket
├── trial/          # lead pipeline (F4)
├── report/         # rankings, CSV, Sheet mirror
├── notification/   # outbox, Zalo (Pro)
├── auth/           # magic link, device token
└── shared/         # config, RLS interceptor, SSE
```

## 4. Milestones

Zalo no longer blocks anything, so the old "spike week" is gone and the order now follows what a
design partner can actually use.

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
- Schema `org` / `scan_point` / `member` + RLS + **the cross-tenant isolation test suite**
  *(mechanism 2 — first, not last)*.
- F10 email magic link for owner/staff; F8 roles skeleton.
- F1: create org → scan point → **roster import** → printable QR poster (PDF). Import is an
  **upsert on normalized phone, never deleting, with a preview screen** — *"12 new, 3 updated,
  185 unchanged"* — confirmed before anything is written.
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
- **F11 member management** — assign/renew/freeze a card, convert a lead, fix a mistyped phone,
  revoke a device token, mark inactive, add one member by hand. **In usability terms F11 comes
  before F5 and F4**: without it there is nowhere to click to assign a card or convert a lead.
- F3 staff roster: today's list + IndexedDB outbox + `client_event_id` idempotency — **service
  worker not yet** (see M3).
- F4 trial registration + pipeline list.
- Exit criteria: the entire original FB gig workflow runs, **with no Zalo anywhere**.

### M3 — Offline level 2 + reports (Weeks 5–7)

- **Offline level 2 for `/staff`** *(split out of F3)*: `ng add @angular/pwa` +
  `ngsw-config.json` (`assetGroups` with `installMode: prefetch` for the shell, `dataGroups` for
  the API) + cache today's roster into IndexedDB rather than memory + a no-network boot path +
  `SwUpdate` `versionUpdates` driving an explicit *"new version, reload"* bar — **never a silent
  swap**, which would discard in-memory taps mid-class — + **the iOS install-instruction screen**.
  Level 1 (outbox without a service worker) was explicitly rejected: it costs nearly as much and
  fails exactly when needed *(`DECISIONS.md` mechanism 3)*.
- F7: monthly ranking per program/location, attendance history, expiring-cards list, trial
  conversion, CSV export (all tiers), and the **one-way Google Sheet mirror** — protected ranges,
  a pinned warning line, batched writes, no inbound path.
- Exit criteria: ① turn on airplane mode → **reload the page** → the app still opens and records
  check-ins → sync completes fully when connectivity returns; ② design partner #0 is using it for
  real.

### M4 — Billing, Zalo (Pro), pilot (Week 7+)

- Billing: Free/Pro; VietQR + manual confirmation first, PayOS/SePay webhook later; 7-day grace →
  soft-lock Pro features only, **never check-in or data**.
- PDPL pages: consent, privacy policy, deletion request.
- **Then** Zalo OA/ZNS as a Pro feature *(D1)*: OA connect flow, daily owner digest, member OA
  messages, ZNS parent-alert credit wallet — all draining `notification_outbox`. Member OTP
  belongs to this milestone too, as a Pro feature.
- Onboard 5–10 pilot centers from FB groups (target ≥1 non-sports center — PRD success metric).
- Instrument the activation funnel (signup → first check-in time).
- Exit criteria: a first paying customer.

### Feature-to-milestone map

Every PRD feature appears in exactly one milestone.

| Feature | Milestone |
|---|---|
| F1 onboarding + roster import (upsert, preview) | M1 |
| F2 member self check-in + device token | M1 |
| F10 owner/staff magic link | M1 |
| F8 multi-location & roles | M1 (skeleton), M4 (Business tier) |
| F6 free-tier live feed (SSE) | M1 |
| F5 entitlements | M2 |
| F9 anti-abuse ladder | M2 |
| F11 member management | M2 |
| F3 staff roster + outbox | M2 |
| F4 trial registration + pipeline | M2 |
| F3 offline level 2 (service worker, IndexedDB roster, iOS install screen) | M3 |
| F7 reports, rankings, CSV, Sheet mirror | M3 |
| F6 Pro tier — Zalo OA/ZNS, digest, ZNS wallet | M4 |
| Member OTP (Pro) | M4 |

## 5. Go-to-market (first 90 days)

1. Deliver design partner #0 (the FB gig) → case study with real numbers.
2. Post the case study in the same FB owner groups (gym/võ/yoga owners, trung tâm ngoại ngữ) —
   where demand already self-identifies.
3. Free tier as the default answer to every *"tìm freelancer làm app điểm danh"* post. It costs
   0đ in variable cost to serve, so this is unlimited *(D1)*.
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
| Solo timeline slips | Any milestone +1 week late | Cut in this order: F7 Sheet mirror → F9 GPS layer → F7 rankings. **Never cut** F2/F3/F5/F11, nor the M3 offline work, nor the mechanism-2 test suite |
| Google Sheets API quota at multi-customer scale | M3 | Batched writes per customer Sheet; Sheet mirror is Pro-gated, so the blast radius is bounded. Still open — PRD §10.2 |
| Zalo policy/pricing change | Any time | **Low.** No longer on the critical path (D1): the free tier neither sends nor receives through Zalo, so a change costs a Pro add-on, not the product. Official APIs only; the notification layer stays abstracted behind the outbox |

Removed from the v1.1 register: *"Outgrowing Supabase / PDPL residency"* — there is no Supabase.
The residency path is now plain `pg_dump` to a VN region with no vendor-specific auth to swap
(§2.5).

## 7. Definition of done — v1

A center owner found via FB can, **with zero contact with the founder**: sign up with an email
magic link, import their existing member roster, print a QR poster, have an adult member
self-check-in and see their remaining validity, have a teacher roster-check a kids' class
**including with the phone in airplane mode after a reload**, manage members and cards in
`/admin`, watch the live check-in feed, see the monthly attendance ranking, and export or mirror
everything into their own Google Sheet.

That closes every requirement in the original FB gig, generalized to any vertical — **and it
closes them without Zalo**. "Receives the daily Zalo digest" was in the v1.1 definition of done
and is removed here: it is a Pro feature (M4), and requires the customer's own verified OA
*(D1)*.
