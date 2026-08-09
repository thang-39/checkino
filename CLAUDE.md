# Checkino

QR check-in SaaS + membership card management for small centers in Vietnam
(gym, martial arts, yoga, language centers, tutoring). Built by one person, part-time.

## Status

Monorepo scaffolded (`M1-S01`): backend Spring Boot 4.1 + frontend Angular 22, no business
tables yet. Backlog: 5 epics, 58 stories in [`docs/STORIES.yml`](docs/STORIES.yml), rendered
into GitHub issues.

Want to know what to do next → run **`/next`**. Don't infer it yourself from the docs.

## Build / test / run commands

Toolchain pinned in [`.tool-versions`](.tool-versions) — Java 25, Node 22.23.1, Maven 3.9.3.
Node **must** be ≥ 22.22.3, otherwise Angular 22 refuses to run.

```bash
./scripts/build.sh          # ng build → copy into static/app/ → mvn package → ONE jar
mvn -f backend/pom.xml test # backend tests (Testcontainers → needs Docker running)
npx ng test --watch=false   # frontend tests, inside frontend/ (Vitest + jsdom, no Chrome needed)
docker compose up -d db     # Postgres 18 only
docker compose up -d --build  # whole stack
java -jar backend/target/checkino-*.jar
```

Run `mvn` with the sandbox **off** from the very first time, and export `TESTCONTAINERS_RYUK_DISABLED=true`.

Two environment traps on the current dev machine:

- **Ports 5432 and 8080 may already be taken by another project.** Compose reads `DB_PORT` / `PORT` —
  copy [`.env.example`](.env.example) to `.env` to change them (5433 / 8081).
- **Corporate TLS interception.** `docker build` fails because Maven inside the container lacks the
  company CA (`PKIX path building failed`). `docker pull` gets through because the daemon has the CA.
  On the host machine `mvn` works thanks to `JAVA_TOOL_OPTIONS` pointing the truststore at JDK 17 —
  **don't remove temurin-17 from asdf.** The image builds fine in CI, which has no MITM.

## Where the docs live, and which one wins

| Layer | File | Contains | Wins when there's a conflict |
|---|---|---|---|
| Why | [`DECISIONS.md`](DECISIONS.md) | D1–D15 + Three mechanisms + sample SQL | **Always wins** |
| What the product does | `PRD.md` (v2.3) | F1–F11, NFR, tiers, metrics | Product behavior |
| UI | [`docs/DESIGN.md`](docs/DESIGN.md) | four box layers, three top-of-screen slots, color tokens, type scale, per-screen spec | **UI shape** |
| Stack & shape | `PLAN.md` (v2.2) | stack, schema, M0–M4, DoD, cut list | Milestone intent |
| **Work to do** | [`docs/STORIES.yml`](docs/STORIES.yml) | acceptance criteria + dependency order | **Work breakdown** |
| Status | GitHub issues | open/closed, comments, PRs | Not a source of truth |

Anti-confusion rules — **follow them, don't work around them:**

- **Task-level detail lives ONLY in `docs/STORIES.yml`.** Don't add checklists to PRD/PLAN anymore.
- **Stories POINT to the docs, they don't copy the content.** A story only originates two things that
  exist nowhere else: acceptance criteria and dependency order. The one repeated exception: a short
  **forbidden** clause.
- **An issue is a one-way render** of `STORIES.yml`. Editing an issue body on GitHub will be overwritten.
- Add/edit a story → use the **`write-story`** skill, then `/sync-issues`.
- While coding, if you find a missing decision → **write it into `DECISIONS.md`**, don't decide it
  implicitly in code or in acceptance criteria.

`docs/archive/` holds docs that have served their purpose (`GRILL-LOG.md`, `plan-v2-rewrite.md`) — kept
as history, not read for work.

## Commands

| Command | What it does |
|---|---|
| `/next` | Which story to do next, with the reason |
| `/plan <N>` | Draft an execution plan for issue #N, comment it on the issue |
| `/work <N>` | Branch, code, test, PR |
| `/status` | Progress table by epic |
| `/sync-issues` | Push `docs/STORIES.yml` to GitHub issues |

GitHub: `thang-39/checkino`. `gh` picks the account **by directory** (hook in `~/.zshrc`):
`~/Documents/personal/*` → `thang-39` (personal), `~/Documents/workspace/*` → the company account.
This repo lives under `personal/` so `gh` is already `thang-39` — **no** `gh auth switch` needed. Mechanism:
each shell exports its own `GH_TOKEN`, without touching the global active account. The remote uses the ssh
alias `github.com-personal`. If you still get 404 → check `gh api user --jq .login` returns `thang-39`.

## Timeline — don't repeat the "8 weeks" number

`PLAN.md § 4` states ~8 weeks to finish M3. The backlog adds up to **43 person-days part-time**
(M0→M3), i.e. **17–21 weeks** at 15–20h/week. `PLAN.md` only estimated two line items and extrapolated
the total. When reporting progress, use the backlog number and convert it to weeks.

## The fifteen locked decisions (summary — details in `DECISIONS.md`)

- **D1** — Zalo OA/ZNS is a **Pro-tier** feature, not part of the v1 core. The free tier runs entirely
  without Zalo. Reason: OA verification requires the customer's business license, which kills the goal of
  under-10-minute self-serve onboarding.
- **D2** — v1 **does not send OTP** to members. The owner imports the roster → the member enters their
  phone number → on a match, bind the device token immediately. ZNS has no free tier (300đ/verification message).
- **D3** — Split the two auth needs: owner/staff use an **email magic link**; members use a
  **device token** (httpOnly cookie, TTL 1 year). Email for members is wrong for the market.
- **D4** — Stack **Spring Boot 4.1 + Java 25 + Postgres 18 + Angular 22** (bumped 29/07/2026).
  `/q/{code}` specifically is server-rendered with Thymeleaf, not a SPA. Reversed the earlier Next.js +
  Supabase choice. **Re-evaluated React on 25/07 and kept Angular** — reopening this question requires a
  technical constraint, not a feeling. Angular builds static files for Spring Boot to serve, **no SSR**.
  Two ripples not to forget: Boot 4.1 pulls in **Testcontainers 2.0.5** (major, API differs from 1.x) and
  Angular 22 requires **Node ≥ 22.22.3** (a build-time constraint, pinned in `.tool-versions`).
- **D5** — **Monorepo + modular monolith, one process.** Microservices are off the table: they break all
  three mechanisms below, because all three rely on one database + one transaction.

- **D6** — **Monthly ranking and the trial pipeline are in the free tier.** Pro is sold by: lifting the
  50-member cap, Sheet mirror, multi-location + roles, Zalo, member OTP. Reason: ranking is exactly the
  original gig work, and D2 only stands if ranking exists.
- **D7** — **`program` (discipline) is its own table** + `member_program` many-to-many; `scan_point` has a
  nullable `program_id`. A discipline is **optional**, skippable, so it doesn't break the 10-minute north star.
- **D8** — **One phone = one member** (`UNIQUE (org_id, phone_normalized)`). Members without a phone number
  aren't supported; kids use a parent's number, one number per kid. At `/staff` the teacher taps by **name**,
  never touching the phone number.

- **D9** — **Drop the "6-digit code" fallback at `/q`.** Offline, `/q` can't open (no service worker), so
  there's no code to show. If the network fails → ask staff to check in on your behalf via `/staff` (F3).
- **D10** — **`audit_log` is in v1**, table built in M1. The roster is identity data (D2), so operations that
  edit it must leave a trace. But the reason for rejecting Sheet-as-roster **leads with "no role separation"**,
  not with the audit trail.
- **D11** — Brand name: **Checkino** (the temporary name CheckinHub is retired). Brand = repo = package, one
  token: `checkino`, `com.checkino`. The criteria, rejected names, and collision checks are in `DECISIONS.md`.
  Domain not bought yet — needs a manual whois first.
- **D12** — Design direction **Variant 2 · Dashboard**, eight color tokens, each color one meaning. Three things
  easy to get wrong: **screen background has only three values** (charcoal / sage / rust `#8E2C1B`); **header =
  place, small label = person's name, 36px hero = state**, no slot doubles up on two meanings; **gradients only
  travel within one color family**. Yellow = still on time, rust = blocked, coral is never an error. Full rules in
  `docs/DESIGN.md` (D15 moved them here from the build-time prompt). Builds live in `designs/*.dc.html` (Claude Design) — run them
  with the committed `designs/support.js`, but they must be **served over local HTTP** (the runtime `fetch`es the
  file itself, doesn't accept `file://`) and **need the network** (React + Archivo).
- **D13** — **`/admin` on a wide screen is a single centered `max-width:440px` column**, no reflow, no other
  breakpoint. Applies to `/staff` too. Recognized risk: importing Excel and printing an A4 poster are
  laptop-jobs but still have to happen in the narrow column — reopen only when the `M4-S13` pilot reports back,
  and reopen with a dedicated design pass, not by adding a stray breakpoint while coding.
- **D14** — **Bilingual vi/en UI, Vietnamese by default** across all three surfaces; the user can switch, and the
  choice is remembered (localStorage at `/admin` `/staff`, cookie at `/q`). Only two languages, no multi-locale
  framework. The reason is a **bet on later market expansion**, not a confirmed v1 need — the cost is two copy-key
  sets that must stay in sync; it's allowed to fall back to vi-only until M4 if there's no signal outside VN.
- **D15** — **`docs/DESIGN.md` is the source of truth for the design system** during the product-coding phase; it
  absorbs all the shared rules from `00-he-thong.md` (four layers, color tokens, type scale) + the per-screen spec.
  The `docs/design/prompts/*` prompts become **build-time history that has served its role**, not read for work.
  Don't reverse D12–D14 — DESIGN.md enforces them. Four meanings locked in on absorption: **yellow** means only
  "still on time" (drop "achievement"), **sage** means only "done" (the "today" one → coral), **purple** = "a number
  about a person", **header avatar** = logo/monogram (drop the 🏋️ emoji). The `*.dc.html` builds need a dedicated
  fix pass to match these four meanings.

**Cross-cutting constraint:** the free tier must have a variable cost = **0đ**.

**The word "PWA" applies only to `/staff`** — `/q` is a plain web page (no manifest, no service worker),
`/admin` only needs a manifest for the icon. Don't call the whole product a "PWA".

## Three mechanisms easy to get wrong (details + sample code in `DECISIONS.md`)

1. **Check-in dedupe** — push it down to `UNIQUE INDEX (member_id, scan_point_id, dedupe_bucket)`
   + `ON CONFLICT DO NOTHING`. **Do not** check with `if (!exists)` at the app layer.
2. **Multi-tenant isolation** — Postgres RLS + `SET LOCAL app.org_id`, **and** an automated cross-tenant
   test suite for every endpoint. Write these tests first, not after.
3. **Offline roster** — idempotency key `client_event_id` with a unique index on the server, **and** a
   service worker (offline "level 2"). Without a service worker, an F5 while offline is a dead app —
   and on mobile F5 happens on its own when the OS kills the tab.

## Conventions

- Business logic lives in the application layer, not in the DB — keep the `pg_dump` escape hatch
  to a VN-region Postgres for PDPL.
