---
name: design-screen
description: Use when building/coding the UI of a specific Checkino screen (/q, /staff, /admin) per the locked design system. Triggers on "/design-screen", "dựng màn", "code màn", "làm giao diện", or when starting a story that has a UI part (M2-S*, M3-S*…). Guides reading the right per-screen spec in docs/DESIGN.md §6 and loading tokens from design/tokens.css — never invent colors/sizes.
---

# Build one Checkino screen

## Rule #1: POINT, don't COPY

This skill holds **no** design law. The law lives in `docs/DESIGN.md` (D15 — source of truth) and
the tokens live in `design/tokens.css`. The skill's job: send you to the right place, then execute.
If you catch yourself typing a raw hex or px value, stop — it already has a token name.

Invoke: `/design-screen <story-id-or-screen-ref>` — e.g. `/design-screen M2-S01`, `/design-screen q#2`.

## Step 1 — Identify the screen(s)

The argument may be a **story** (`M2-S01`) or a **screen ref** (`q#2`, `staff#6`, `admin#13`).

- If it's a story → read it in [`docs/STORIES.yml`](../../../docs/STORIES.yml), find which surface it
  belongs to (`/q` · `/staff` · `/admin`) and which screen number(s) in DESIGN.md §6 it maps to.
- One story may cover **several screens** (e.g. the scan flow = normal + expired + out-of-hours
  states). List them all before coding — don't build a state short.

## Step 2 — Read the spec, in order

Read **all four** sources before writing the first line of markup:

1. [`docs/DESIGN.md`](../../../docs/DESIGN.md) **§6** — the row for the exact screen: `screen bg ·
   eyebrow · hero · main block · saturated tokens (≤2)`. This is the screen's contract.
2. `docs/DESIGN.md` **§1 (four box layers)** + **§2 (color meaning)** + **§3 (three head-of-screen
   slots, type scale)** — the shared laws. Re-read if not memorized. Don't half-remember them.
3. [`design/tokens.css`](../../../design/tokens.css) — token names for every color/size/radius/space.
   Load this file and use `var(--c-*)`, `var(--fs-*)`… — **never** write raw hex/px.
4. The matching build `designs/{q,staff,admin}.dc.html` — a **prototype to check one screen's detail**
   (not product code). Look at it for layout feel; don't carry over scaffold (§8).

## Step 3 — Execute, self-check the four easy-to-get-wrong things

Check as you build (this is a checklist, not the full law — the law is in DESIGN.md):

- [ ] **Screen bg** is exactly one of the three values §6 dictates (ink/sage/rust). Rust is bg only.
- [ ] **Three head slots** don't blur meaning: header = the place (fixed, logo/monogram §5),
      eyebrow = person/area name, hero 36px = status sentence. Hero **doesn't change on filter**.
- [ ] **Count saturated blocks ≤ 2** (§6 lists the screen's tokens). 3b icon boxes and real-object
      mocks don't count.
- [ ] **Color always paired with text** — no color patch stands alone as the only signal.
- [ ] Touch targets: ≥ 52px (primary action + everything on /staff), ≥ 44px (secondary on /admin).
      Input text ≥ 16px. Readable at 360px. Wide screen → single `max-width: var(--col-max)` column
      centered (D13).

## Step 4 — Right layer, right tech

- `/q` → **Thymeleaf server-render** (not SPA). Language memory via **cookie** (§7).
- `/admin`, `/staff` → **Angular SPA**. Language memory via **localStorage** (§7). Default `vi` on all three.
- Same `tokens.css` for both tiers — that's why token names are framework-neutral.

## When DESIGN.md is missing or contradictory

Don't decide silently in CSS. If §6 doesn't state a detail, or a build diverges from DESIGN.md,
record the decision in [`DECISIONS.md`](../../../DECISIONS.md) before coding — as CLAUDE.md requires.
The "ripples" DESIGN.md already flagged (builds still wrong per the four D15 meanings) are places
where **DESIGN.md wins**, not the build.
