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

## Q14. Solo part-time builder. Spring Boot + Angular (your stack) or Next.js + Supabase (faster)?

**A:** Supabase accelerates auth/multi-tenant RLS but means learning while building. Thang's productivity, conventions, and tooling are Spring Boot + Angular; this is a long-lived product, not a hackathon.

**Verdict:** Spring Boot 3.5 + Angular 20 PWA + Postgres, OpenAPI-first, single VPS/Fly.io deploy. Note the Supabase alternative for a future co-founder discussion.

> **Superseded 2026-07-19:** decision flipped to **Next.js + Supabase** after complexity review — solo speed wins. Q14's maintainability concern is answered by an explicit exit door (logic in app layer, ORM, `pg_dump` path to VN-region Postgres) documented in `PLAN.md` §2, not by the heavier stack.

## Q15. What is explicitly OUT of v1? (The question that saves the ship date.)

**Verdict — out of v1:** class scheduling/booking/capacity, payment processing & tuition collection, payroll/PT commissions, belt/grade tracking, homework/lesson reports, turnstile/hardware integration, native apps, unofficial Zalo group posting, multi-language UI (VN only; English later).

---

## Final verdict

The claim **"support every center type from v1" survives** — but only in the narrowed form:
**"one generic Attendance & Entitlement core + two check-in modes + configurable consume policies, sold vertical-by-vertical."**
The two decisions that make it true: staff roster mode (Q4) and per-entitlement consume policy (Q3).
The two decisions that keep it shippable: the out-of-scope list (Q15) and the official-only Zalo stance (Q6).
