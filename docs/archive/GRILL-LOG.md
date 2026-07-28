# Grill Log — "Can one check-in SaaS serve every type of center?"

Adversarial self-interrogation of the product concept before writing the PRD.
Claim under attack: **"Support all center types (gym, martial arts, yoga, language, arts, tutoring) from v1."**

Format: **Q** (attack) → **A** (defense) → **Verdict** (what goes into the PRD).

---

## Q1. "All center types" is a fantasy. A gym and an English center are different businesses. What actually generalizes?

**A:** Strip each vertical to its door-level workflow and the same 6 primitives appear everywhere:
person arrives → identify → check entitlement (is their card/package valid?) → record attendance → notify someone → aggregate into reports.
Everything else (class booking, belts, homework, tuition invoices) is vertical-specific.

**Verdict:** v1 = those 6 primitives done extremely well ("Attendance & Entitlement core"). Vertical-specific features are explicitly out of v1. "Support all types" means *generic core + configuration*, not per-vertical feature sets.

## Q2. Can one membership model really cover a gym punch card AND a 12-week English course?

**A:** Model entitlement as a single configurable object:
`Entitlement = { type: TIME_BASED | SESSION_PACK | COURSE_TERM | TRIAL | DROP_IN, scope: program(s) or whole org, valid_from/to, session_quota, consume_policy }`.
Time-based covers gym monthly cards; SESSION_PACK covers swim/yoga punch cards; COURSE_TERM covers a language course with fixed end date.

**Verdict:** Covers ~90% of cases. Tuition installment tracking = status flag only in v1, no payment processing.

## Q3. The FB gig wanted "multiple sessions/day = 1 attendance count." Is that rule universal?

**A:** No. A swim punch card must consume 1 session per visit; a gym loyalty ranking counts 1/day; a scheduled class counts per class occurrence.

**Verdict:** Dedupe/consume policy is **per-entitlement configuration** (`ONCE_PER_DAY | PER_VISIT | PER_CLASS`), never hardcoded. This single config decision is what makes multi-vertical credible.

## Q4. Kids in an English class don't own phones. Your whole "student scans QR at the door" loop dies. 

**A:** Correct — self-scan only works when the attendee carries a phone. For kids' centers the check-in actor must be staff.

**Verdict:** v1 ships **two check-in modes**: (1) self-scan via printed QR, (2) **staff roster mode** (teacher taps names on a phone/tablet list). Parent gets a "your child arrived" Zalo message — a safety feature parents will pay for. This is the decision that truly unlocks non-sports verticals.

## Q5. A student screenshots the QR and checks in from home to farm the attendance ranking. Now what?

**A:** Static printed QR is inherently spoofable. Rotating QR requires a display device at the door (contradicts "print and stick"). 

**Verdict:** Accept the risk in v1 (loyalty stakes are low): static QR + server-side anomaly flags (impossible travel, burst patterns, device fingerprint) + optional GPS soft-check. Rotating QR on a cheap Android tablet = later premium feature.

## Q6. Zalo has NO official API to post into a Zalo group. The FB gig's core ask is technically impossible without ToS violations. Doesn't that kill the product?

**A:** It kills the *naive* implementation, which is exactly why this is an opportunity — every AppSheet freelancer hits this wall. Legitimate paths: (a) real-time **live feed dashboard** for owners, (b) OA messages to members/parents who follow the center's OA, (c) ZNS template messages to any phone number (~200–800đ each), (d) daily digest instead of per-event spam.

**Verdict:** v1 notification stack = live dashboard (free, real-time) + daily ZNS/OA digest to owner + per-event OA message to OA followers + **ZNS to parent phone as a paid add-on** (message cost passed through as credits — a revenue line, not a cost). Never build on unofficial group APIs. Market it honestly: "official Zalo OA integration, no account-ban hacks."

> **Superseded 2026-07-25 (D1):** Zalo leaves the v1 core entirely — it is a **Pro-tier feature**.
> Two facts killed it as a core dependency: (a) calling any send API requires a **verified OA**,
> and OA verification requires the *customer's own business licence* (GPKD) — many small VN
> centers don't have one, and the multi-day paperwork is incompatible with the <10-min
> self-serve north star (Q7); (b) **ZNS has no free tier** — 300đ per authentication message,
> 120đ administrative, billed per successful send. The "pass-through credits" framing still
> holds for Pro, but it cannot fund a free tier.
>
> The official-only stance in this verdict **stands unchanged** — no unofficial group APIs, ever.
> What changed is placement: free tier ships with the **live dashboard as its only channel**
> (variable cost = 0đ), and the Zalo milestone drops off the critical path to M4.
> See `DECISIONS.md` D1.

## Q7. Why won't GymMaster / KiotViet / PosApp crush this?

**A:** They are sales-led ("contact for quote"), onboarding-heavy, gym-focused, often hardware-tied. Our wedge: self-serve signup → first successful check-in in under 10 minutes, free tier, no hardware, cross-vertical.

**Verdict:** The moat is thin (speed + Zalo-native UX + distribution in FB groups). Accept it: win the micro/small segment they can't serve economically. Activation metric (<10 min to first check-in) goes into the PRD as a north star.

## Q8. AppSheet freelancers charge 5–6M one-off. Why would anyone pay you monthly?

**A:** Freelancer builds are single-tenant, unmaintained, and die when the freelancer disappears. Subscription includes hosting, updates, support, and new features.

**Verdict:** Price so that 12 months ≈ half a freelancer build (~199k/month). Log v2 idea: recruit those freelancers as white-label/affiliate channel.

## Q9. The FB poster explicitly wanted Google Sheets so he "owns the data." Your SaaS takes that away.

**A:** The real need is *data ownership feeling*, not Sheets as a database.

**Verdict:** One-way nightly **export/sync to the customer's own Google Sheet** + CSV export anytime. Never architect the product on Sheets (can't do multi-tenant, race conditions on concurrent check-ins).

## Q10. One pricing model across verticals: a gym has 500 members, a tutor has 20. Per-member pricing punishes the gym.

**A:** Flat per-location tiers, not per-member: Free (1 location, ≤50 active members) / Pro ~199k/location/month (unlimited members, Zalo OA, exports) / Business ~499k (multi-location dashboard, API, priority). Message (ZNS) costs = prepaid credit wallet, passed through.

**Verdict:** Adopted. Free tier is the growth engine in FB groups.

## Q11. You'll store children's data and parents' phone numbers. Vietnam PDPL / Decree 13 compliance?

**A:** Registration form must capture consent; privacy policy; delete-on-request; prefer VN-region hosting; minors' data flagged.

**Verdict:** v1 ships consent checkbox + privacy policy + data deletion; hosting region disclosed. Not optional — parents and schools ask.

## Q12. Basement gym, no wifi, student has no data plan. Check-in fails, owner rages.

**A:** Self-scan needs connectivity (acceptable — VN 4G coverage is good). Staff roster mode must work offline.

**Verdict:** Staff roster PWA gets an offline queue with background sync. Self-scan failure shows a "show this screen to staff" fallback code.

## Q13. App or web? Everyone asks for "an app."

**A:** The core loop starts with a phone camera scanning a printed QR → that opens a URL. A native app adds an install step that kills the loop at the door. Push notifications aren't needed on the member side (Zalo carries them). Owners get an installable PWA.

**Verdict:** **Web-first (mobile-first PWA) for v1. No native app.** Phase 2 = **Zalo Mini App** (zero-install inside Zalo, gives Zalo identity + OA follow flow — the VN-native distribution cheat code). Native app: only if enterprise customers demand it, likely never.

> **Note 2026-07-25 (D4) — the conclusion stands, the wording was sloppy.** Web-not-native is
> still correct, and for exactly the reason given: the loop starts at a camera pointed at a
> printed QR, so an install step at the door kills it. But this verdict says "PWA" about the
> *whole product*, and the word was carrying **two different meanings**: (a) *"web, not native"*
> — the actual Q13 decision, product-wide; (b) *"installable app that runs offline"* — a
> technical requirement that applies to **`/staff` only**.
>
> Corrected split (`DECISIONS.md` D4): `/q` member page = **an ordinary web page**, no manifest,
> no service worker; **`/staff` = the full PWA** (manifest + service worker + IndexedDB);
> `/admin` = website + manifest for the icon only. Read "PWA" in this verdict as sense (a).

## Q14. Solo part-time builder. Spring Boot + Angular (your stack) or Next.js + Supabase (faster)?

**A:** Supabase accelerates auth/multi-tenant RLS but means learning while building. Thang's productivity, conventions, and tooling are Spring Boot + Angular; this is a long-lived product, not a hackathon.

**Verdict:** Spring Boot 3.5 + Angular 20 PWA + Postgres, OpenAPI-first, single VPS/Fly.io deploy. Note the Supabase alternative for a future co-founder discussion.

> **Superseded 2026-07-19:** decision flipped to **Next.js + Supabase** after complexity review — solo speed wins. Q14's maintainability concern is answered by an explicit exit door (logic in app layer, ORM, `pg_dump` path to VN-region Postgres) documented in `PLAN.md` §2, not by the heavier stack.

> **Un-superseded 2026-07-25 (D4) — Q14's original verdict is reinstated: Spring Boot 3.5 +
> Postgres 16 + Angular 20.** The 2026-07-19 flip rested on one concrete advantage: Supabase
> ships **phone OTP** out of the box. That advantage evaporated on inspection — Supabase phone
> auth runs through Twilio/Vonage (expensive, poor deliverability to VN numbers), so sending OTP
> via ZNS means **hand-writing auth on any stack**. Then D2 removed member OTP from v1 entirely,
> and the advantage disappeared outright.
>
> Everything else Supabase provided has a cheap equivalent: Realtime → `SseEmitter` (~30 lines);
> RLS → plain Postgres, not a Supabase feature; managed Postgres → Neon/Railway/Fly. No technical
> constraint forces Next/Supabase at an estimated **1–2 writes/second**. The exit door from the
> 07-19 note is kept as-is (logic in the app layer, `pg_dump` → VN-region Postgres for PDPL) —
> it was a good idea independent of which stack won.
>
> **And regardless of framework, no Next.js:** it drags in a Node runtime to operate, which is
> the exact thing D4 removes. Angular (or React) here builds to static files served by Spring Boot.

> **Second note, same day — React was reconsidered and Angular was kept.** The frontend question
> had already flipped three times (Q14 Angular → 07-19 Next.js+React → D4 Angular), so it was
> examined properly and the result recorded to avoid a fourth flip. The proposal was React + Vite,
> motivated by *"React feels faster."* That speed is real but lives in the ecosystem and AI
> assistance — the two technical advantages usually cited **don't apply here**:
>
> | Claimed React advantage | Applies? |
> |---|---|
> | Light bundle, fast cold start | **No.** The cold-start-sensitive page is `/q`, already Thymeleaf. `/staff` and `/admin` sit behind login, are used daily, installed as a PWA |
> | Simpler, smaller surface | **Partly.** But router / data fetching / forms / PWA plugin must be assembled by hand; Angular CLI ships them |
> | Wider ecosystem, better AI support | **Yes** — the one real advantage, and the one traded away |
>
> Decisive: (1) Angular is the builder's day-job stack — this verdict's original argument, still
> the strongest; (2) the hardest frontend work (offline outbox + sync) is hand-written either way,
> so React removes zero lines of the biggest risk; (3) `/staff` needs a PWA and `ng add
> @angular/pwa` generates manifest + service worker + JSON-declared caching, whereas React needs
> `vite-plugin-pwa` wired up manually.
>
> **Stopping rule, fixed here:** *the stack already mastered wins, unless a specific technical
> constraint forces a change.* Checked 2026-07-25 — **there is none.** Reopening this question
> requires naming a technical constraint, not a feeling. Details: `DECISIONS.md` D4.

## Q15. What is explicitly OUT of v1? (The question that saves the ship date.)

**Verdict — out of v1:** class scheduling/booking/capacity, payment processing & tuition collection, payroll/PT commissions, belt/grade tracking, homework/lesson reports, turnstile/hardware integration, native apps, unofficial Zalo group posting, multi-language UI (VN only; English later).

## Q16. Where does the money for OTP come from before there is any revenue? *(added 2026-07-25)*

**A:** It doesn't — so v1 sends no OTP at all. ZNS authentication messages cost **300đ each**
with **no free allowance**, while the free tier must have a **variable cost of 0đ** to be safe to
give away. But the frightening number only appears if the design is wrong:

| Design | OTP messages | Cost |
|---|---|---|
| **Device token** — OTP once per member, ever · 10 centers × 100 members | 1,000 messages, **one time** | **300,000đ** total |
| Login on every visit — same scale | 1,000 × 12 visits/month = 12,000/**month** | **3.6M/month** |

Device token brings a per-visit cost down to a one-off. D2 then takes it to **0đ**: the owner
already has the member list in Excel, so **import the roster and let it be the identity check** —
member scans QR → types phone number → number is on this center's roster → bind the device token
immediately, no OTP. Unknown number → trial-registration form (lead), which is a *feature* (F4),
not a failure path.

What OTP would have defended against: A typing B's number to check in on B's behalf. The only
consequence is a **skewed attendance ranking** — no money moves, nothing sensitive leaks (the
screen shows *"12 sessions left"*). That risk is handled by anomaly flags (one number binding on
two devices in a short window) plus owner review before monthly prizes.

**Verdict:** v1 ships **no member OTP** — roster import + device token (httpOnly cookie, 1-year
TTL), soft identity by design, documented as an accepted risk in `PRD.md` §6. OTP becomes a Pro
upgrade, where 300đ/message is already covered by the 199k/month subscription. Later upgrade path:
**Zalo Login (OAuth)** — no per-message fee, only one-off OA verification. See `DECISIONS.md` D2/D3.

> Consequence worth naming: this turns the roster from *reporting data* into **identity data**.
> Hence the v2.1 patches — import must be an **upsert by phone number that never deletes**, with
> a preview before applying, and there must be a **member-management screen** (F11) to fix a
> mistyped number, since a wrong number locks that member out permanently.

---

## Final verdict

The claim **"support every center type from v1" survives** — but only in the narrowed form:
**"one generic Attendance & Entitlement core + two check-in modes + configurable consume policies, sold vertical-by-vertical."**
The two decisions that make it true: staff roster mode (Q4) and per-entitlement consume policy (Q3).
The two decisions that keep it shippable: the out-of-scope list (Q15) and the official-only Zalo stance (Q6).
