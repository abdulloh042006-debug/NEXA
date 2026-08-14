# NEXA — PRODUCT VISION & REQUIREMENTS DOCUMENT (PVRD)
## The Single Source of Truth for Every Product, Engineering, Design, and Business Decision

| | |
|---|---|
| **Document** | NEXA Product Vision & Requirements |
| **Version** | 1.0.0 |
| **Date** | 2026-07-13 |
| **Status** | Foundation Draft — for executive, investor, and team review |
| **Audience** | Founders, investors, executives, product, design, engineering, AI research |
| **Governing documents** | CONSTITUTION.md (inviolable rules) · ARCHITECTURE_V2.md (how it's built) · PERSONALITY_BIBLE.md, INTERACTION_PHILOSOPHY.md, PROACTIVE_INTELLIGENCE.md, EMOTION/RELATIONSHIP/IDENTITY_ENGINE.md, DIGITAL_LEGACY.md, FAMILY_AI.md (how it behaves) |
| **Precedence** | Where this document conflicts with the Constitution, the Constitution wins. Where any future document conflicts with this one, this one wins until formally amended. |

---

## Contents

1. Product Vision · 2. Mission · 3. Long-term Vision (5–10 years) · 4. Core Product Principles · 5. Product Philosophy · 6. Target Users · 7. User Stories (157) · 8. Jobs To Be Done · 9. Functional Requirements · 10. Non-functional Requirements · 11. MVP Definition · 12. Roadmap · 13. Features NOT in MVP · 14. Success Metrics · 15. Failure Metrics · 16. Competitive Analysis · 17. Product Risks · 18. Future Opportunities · 19. Product Decision Log · 20. Product North Star · 21. Anti-Feature List · 22. Version Kill Criteria · Appendix: Series A Review Note

---

# 1. Product Vision

**A world where every person has a trusted companion in their pocket that remembers their life, handles their busywork, speaks their language — and answers to them alone.**

Today, a person in Tashkent, Samarqand, or Almaty lives across three languages, a dozen apps, a bureaucracy that runs on paper and patience, and a phone that buzzes 150 times a day. The world's AI assistants were not built for this person. They don't speak Uzbek. They don't remember last month. They can't touch the phone they live on. They serve advertising businesses, not their users.

NEXA is the counter-thesis: an **AI Operating Companion** — not a chatbot, not a search box, not an OS feature — that perceives the user's device and day, remembers across years, acts across apps under earned and auditable consent, and holds a character its user can trust at year five as at day one.

# 2. Mission

**Give every person — starting with the 80+ million people of Central Asia that big tech ignores — an AI companion that speaks their languages, guards their privacy, and gives them back their time.**

The mission's ordering is deliberate: *starting with*. Uzbek/Russian/English trilinguality is not localization — it is the wedge, the moat, and the proof that NEXA serves people the giants consider a rounding error.

# 3. Long-term Vision (5–10 years)

| Horizon | State |
|---|---|
| **Year 1–2** | The best personal AI on Android in Central Asia: voice-first, trilingual, memory-first, private. The default answer to "telefonda AI bormi?" in Uzbekistan. |
| **Year 3–4** | The companion layer of the region's digital life: families, skills marketplace, desktop/wear/car surfaces, adjacent languages (kk, ky, tg, tr → 200M+ speakers), first enterprise deployments. |
| **Year 5–7** | A platform: third parties build on NEXA's trust infrastructure; NEXA ships preloaded on regional devices; the majority of daily interactions run fully on-device; NEXA is the reference implementation for privacy-first personal AI outside the US/China duopoly. |
| **Year 8–10** | The personal AI institution of the Turkic and CIS world, 100M+ users: a decades-long companion whose memory, trust record, and family fabric follow a person across every device generation — governed by a Constitution old enough that users have stopped reading it because it has never been broken. |

The unchanging constraint across all horizons: NEXA never becomes an advertising business, an engagement business, or a data business. The business is the subscription; the product is the trust (§19, D-04).

# 4. Core Product Principles

Ten principles. Every feature proposal must cite the principles it serves; a proposal that serves none is rejected regardless of how impressive it demos.

1. **Time back, not time spent.** We optimize minutes returned to the user, never minutes captured from them. Engagement metrics are health indicators, not goals (Constitution Art. 16).
2. **Trust is the product.** Features ship in the order that builds trust, not the order that demos well. One trust-destroying incident outweighs ten shipped features.
3. **The best assistant is mostly silent.** Proactive value is measured by precision, not volume. An empty notification day that needed nothing is a success.
4. **Language is the product, not localization.** uz/ru/en parity is release-blocking. A feature that works beautifully in English and poorly in Uzbek does not ship.
5. **Build for the phone people actually own.** The P90 target device is a mid-range Redmi-class Android, not a flagship. Offline is a first-class mode, not an error state.
6. **Privacy as physics, not policy.** What never leaves the device cannot be leaked, subpoenaed, or sold. E2E for what must travel. Policies can be changed by a new CEO; architecture can't.
7. **Autonomy is earned, in the open.** NEXA does more over time because it demonstrably got things right, and the user can always see the receipts.
8. **Memory with a "why" and a delete button.** Every remembered fact is inspectable, sourced, and erasable — with cascade.
9. **Simple beats clever.** When two designs solve the job, ship the one with fewer concepts, fewer permissions, fewer settings. Complexity is a tax the user pays forever.
10. **The relationship is the moat.** Anything competitors can copy in a quarter (a model, a feature) is not strategy. Five years of earned trust, learned preference, and shared history is.

# 5. Product Philosophy

**The category we are creating is the "operating companion" — and the 90-day product is the real product.**

Every AI assistant demos identically well on day one. The differences that matter are invisible in a demo: does it remember week three's correction? Has it earned the right to stop asking? Does it know when *not* to speak? Has it survived a mistake? Our philosophy therefore inverts standard product thinking:

- **We design states, not screens.** The core deliverable is the *Companion Threshold* — the measured day-90 relationship state (INTERACTION_PHILOSOPHY §2.4): zero re-explanation, drafts that pass the glance test, the right silences, 1–2 earned hands-off domains, anticipation without creepiness, a shared past, one survived failure.
- **We treat character as infrastructure.** The Constitution, Personality Bible, and Identity Lock are not brand documents — they are shipped, signed, evaluated artifacts. Consistency of character across years, languages, and devices is a system requirement with CI gates.
- **We are calm technology.** NEXA competes on absence: absent nagging, absent feed, absent anxiety. The phone gets quieter after installing NEXA — measurably (notification-load reduction is a tracked metric).
- **We compound.** Every interaction must make tomorrow's interaction better (memory, preference, plan library, trust evidence) — or it's a feature from someone else's product.

# 6. Target Users

**Beachhead market:** Uzbekistan (~37M people, median age ~29, ~85–90% Android, overwhelmingly mid-range devices, Telegram-dominant communication, trilingual daily life) — then Central Asia + CIS + diaspora (labor migrants and students in Russia, Korea, Turkey, UAE, US). The personas below are drawn from this reality; global expansion re-validates them per market.

## 6.1 The Student — «Dilshod», 19, TUIT student, Tashkent

- **Goals:** pass exams, learn English/IT skills, manage stipend-level budget, help family digitally (he is the household's "IT department").
- **Pain points:** study materials in three languages; expensive tutors; disorganized notes and deadlines; mid-range phone with limited storage; metro commute = dead offline time.
- **Expectations:** free tier that's actually useful; explains rather than just answers; works offline in the metro; doesn't judge "stupid questions."
- **AI interaction style:** text-heavy, late-night, code-switched uz/en («bu functionni tushuntir»); voice when walking; wants patient-teacher mode, zero formality.

## 6.2 The Professional — «Madina», 29, marketing manager, Tashkent

- **Goals:** survive 60-notification days, run campaigns across ru/en clients, protect evenings for family, look flawless in written communication.
- **Pain points:** message triage across Telegram/email; meetings colliding; drafting formal Russian and English under time pressure; forgetting promises made in passing.
- **Expectations:** discretion (her phone is her office); perfect register in drafts; a morning brief that's actually right; never leaks work talk aloud at home.
- **AI interaction style:** voice in the car, text at desk; delegates outcomes («shu ishni hal qil»); expects proactive help but zero spam; will pay for Pro if drafts save her an hour a day.

## 6.3 The Developer — «Javohir», 24, mobile developer, remote for an EU startup

- **Goals:** ship faster, automate his own life, eventually build and sell a NEXA skill for a local service.
- **Pain points:** context-switching; English-only dev tools vs. Uzbek life admin; existing assistants can't touch his device or be extended.
- **Expectations:** technical honesty (calibrated uncertainty, sources); power controls; an SDK with a real simulator; scriptable automations; transparency into what runs where.
- **AI interaction style:** precise text commands, custom workflows, reads the Learning Ledger for fun; the persona most likely to jailbreak-test the Identity Lock — and to respect it when it holds.

## 6.4 The Elderly User — «Karim aka», 67, retired teacher, Samarqand

- **Goals:** stay connected with children abroad (Korea, Russia); manage medications; navigate e-gov services that moved online without him.
- **Pain points:** small text, complex UIs, fear of "breaking something" or being scammed; voice is natural but assistants don't speak Uzbek.
- **Expectations:** respectful *siz* register, patient repetition without condescension, large text, one obvious button, protection from fraud.
- **AI interaction style:** voice-first, almost voice-only; long conversational turns; needs confirmation and reassurance; family (§6.6) may help configure via guardian-style setup — with his knowledge and consent (surveillance honesty applies to elders as to children).

## 6.5 The Business User — «Umid», 41, logistics SME owner (14 employees), Tashkent

- **Goals:** customers answered fast, invoices paid, drivers coordinated, contracts understood without a lawyer on retainer.
- **Pain points:** he *is* the CRM — everything lives in his head and three phones; correspondence in ru/en with partners; no time for tools that need configuring.
- **Expectations:** works out of the box; drafts that sound like him; reminders that never slip; strict confidentiality (competitors are neighbors).
- **AI interaction style:** voice while driving between sites; barked fragments («Anvarga ayt, ertaga yuk 8 da»); zero patience for settings; the persona that proves elliptical-command value.

## 6.6 The Family — «The Rahimovs»: parents 38/35, children 13 and 8, grandmother

- **Goals:** coordinate the household (school, meals, budget); keep children safe online without spying; include grandmother.
- **Pain points:** family logistics scattered across chats; parental-control tools are either spyware or useless; nothing serves three generations in two scripts.
- **Expectations:** shared calendar/lists that actually get used; children helped with homework, not given answers; visible, honest rules about what parents can see.
- **AI interaction style:** kitchen-speaker household brief; each member's own NEXA (FAMILY_AI.md: *the family shares a home, not a memory*); child gets teacher-mode, grandmother gets Karim-aka mode.

## 6.7 The Power User — «Sardor», 27, product manager and productivity enthusiast

- **Goals:** automate everything automatable; one system for tasks, notes, and life; squeeze the phone's full capability.
- **Pain points:** has tried every assistant/PKM tool; they don't talk to each other, don't act, don't remember; automation apps are powerful but hostile.
- **Expectations:** deep workflows, keyboard-speed text control, exportable data, inspectable internals; will find every edge case and post about it.
- **AI interaction style:** dense text commands, custom triggers, memory browser as a second brain; the persona that stress-tests budgets, trust tiers, and the marketplace — and evangelizes when impressed.

---

# 7. User Stories

157 stories. Tags: **[V1]** ships in MVP, **[V2]**, **[V3]**, **[V5]** per roadmap (§12). Personas: student (ST), professional (PR), developer (DV), elderly (EL), business (BZ), family (FM), power user (PW), any (U). Stories marked ⚖ are *trust stories* — behavior promises that are features in their own right.

## 7.1 Communication (12)

- US-001 [V1] As PR, I want a reply drafted from a notification in the sender's language and my usual register, so answering takes one glance and one tap.
- US-002 [V1] As U, I want incoming Russian/English messages translated into Uzbek inline, so I never misread anything important.
- US-003 [V1] As BZ, I want to dictate a Telegram message while driving and hear it read back before sending, so my eyes stay on the road.
- US-004 [V1] As PR, I want a 200-message group chat summarized into decisions and action items, so I catch up in 30 seconds.
- US-005 [V1] As ST, I want a formally-registered letter to my professor drafted from my casual bullet points, so I sound right without an hour of anxiety.
- US-006 [V2] As PR, I want NEXA to remind me in the evening about messages I opened but never answered, so nothing important slips.
- US-007 [V2] As U, I want incoming calls announced with who and probable why ("Dilnoza — siz kutgan hujjat haqida bo'lsa kerak"), so I decide in a second.
- US-008 [V1] As U, I want OTP codes surfaced with a copy chip and auto-archived after use, so codes never clutter and never get stored. ⚖
- US-009 [V1] As U, I want obvious spam/phishing messages flagged before I tap anything, so my parents and I don't get scammed.
- US-010 [V1] As U, I want to ask "Aziz bilan nima kelishgandik?" and get the answer with the source message, so agreements never get lost.
- US-011 [V2] As BZ, I want drafts to match my per-recipient style (formal to partners, warm to family), so everything sounds like me.
- US-012 [V1] ⚖ As U, I want NEXA to never send anything without my explicit confirmation until I've granted that exact scope, so I stay in control.

## 7.2 Daily Life (10)

- US-013 [V1] As U, I want a morning brief (calendar, weather, leave-times, key notifications) at my learned wake window, so my day starts oriented.
- US-014 [V1] As U, I want "leave now" alerts computed from real traffic to my actual appointment, so I stop being late.
- US-015 [V1] As U, I want to add to my shopping list by voice in any of my languages, mid-thought, so nothing is forgotten.
- US-016 [V1] As U, I want birthday reminders with a drafted congratulation in the right language and register, so I never miss one.
- US-017 [V1] As U, I want to tell NEXA "sport zalga a'zolik kartam mashinada" and retrieve it weeks later, so my life's small facts have a home.
- US-018 [V2] As U, I want utility-payment reminders from my own patterns (explicitly confirmed as a goal), so services never get cut off.
- US-019 [V1] As U, I want prayer-time reminders I configure myself, respectful and silent-mode aware, so my practice fits my day. ⚖ (religion is respond-only; this is user-initiated)
- US-020 [V2] As U, I want an evening glance at tomorrow ("ertaga 3 ta ish, birinchisi 9 da"), so I sleep without mental rehearsal.
- US-021 [V1] As U, I want the umbrella warning only when rain actually matters to my day's movements, so weather info is timing, not noise.
- US-022 [V2] As U, I want "Where did I park?" answered from a note I spoke when parking, so garages stop being mazes.

## 7.3 Productivity (10)

- US-023 [V1] As PR, I want a meeting-prep brief (participants, last agreements, open items) 15 minutes before, so I walk in ready.
- US-024 [V1] As U, I want a rambling voice note converted into a structured to-do list, so thoughts become tasks.
- US-025 [V1] As PR, I want focus mode: notifications held and digested except my VIP list, so deep work exists.
- US-026 [V2] As PR, I want promises I made in conversations ("ertaga yuboraman") tracked and surfaced at the right time, so my word stays good.
- US-027 [V1] As U, I want any document or long article summarized to my preferred depth, so reading queues shrink.
- US-028 [V2] As PR, I want NEXA to negotiate a meeting slot with a colleague's calendar via drafted messages, so scheduling ping-pong dies.
- US-029 [V1] As U, I want search across everything NEXA and I have discussed and saved, so my second brain has recall.
- US-030 [V2] As PW, I want tasks time-boxed into my real free calendar slots as suggestions, so plans meet reality.
- US-031 [V1] As BZ, I want a report/letter outline drafted from three spoken sentences, so blank pages stop costing evenings.
- US-032 [V2] As PR, I want recurring status updates ("har juma investorlarga xulosa") drafted from the week's noted events, so routine writing runs itself.

## 7.4 Learning (8)

- US-033 [V1] As ST, I want concepts explained simply in Uzbek with English terms kept where they matter, so I learn the concept *and* the vocabulary.
- US-034 [V2] As ST, I want a standing "15 daqiqa ingliz tili har kuni" goal with adaptive micro-lessons, so consistency happens.
- US-035 [V1] As ST, I want to be quizzed from my own notes and photos of the whiteboard, so revision uses my materials.
- US-036 [V1] As ST, I want an English article translated *and* explained (not just converted), so I understand arguments, not words.
- US-037 [V3] As FM-child, I want homework help that guides me to the answer instead of giving it, so I actually learn (and my parents trust the tool).
- US-038 [V1] As ST, I want IELTS/CEFR vocabulary practice tuned to my mistakes, so prep is efficient.
- US-039 [V1] As DV, I want code explained line-by-line in my language, so unfamiliar codebases open up.
- US-040 [V1] As U, I want legal/bureaucratic terms explained in plain language when I hit them in documents, so forms stop being frightening.

## 7.5 Health Reminders (7) — *(respond-only domain: everything here is user-initiated; NEXA never observes or comments on health uninvited ⚖)*

- US-041 [V1] As EL, I want medication reminders with the pill name spoken clearly and a "took it" confirmation, so my regimen is safe.
- US-042 [V1] As U, I want appointment reminders that include the prep ("och qoringa keling"), so instructions aren't lost.
- US-043 [V2] As EL, I want my refill reminder to fire a week early with the pharmacy's info, so I'm never without medicine.
- US-044 [V1] As U, I want water/posture/stretch nudges *only if I set them*, at my chosen cadence, so wellbeing help is mine to define.
- US-045 [V1] ⚖ As U, I want certainty that NEXA never comments on my weight, sleep, or pharmacy visits unless I ask, so my body stays my business.
- US-046 [V2] As U, I want my insurance/medical document copies retrievable instantly at the clinic, so paperwork never blocks care.
- US-047 [V1] As U, I want quiet hours where only my configured critical alerts (meds, family emergency) get through, so nights are protected.

## 7.6 Travel (8)

- US-048 [V1] As U, I want a country-specific visa document checklist assembled and tracked (Korea visa: invitation letter, bank statement…), so applications don't fail on a missing paper.
- US-049 [V2] As U, I want my flight watched: gate changes, delays, and check-in opening surfaced at the right moment, so airports lose their stress.
- US-050 [V2] As U, I want check-in done for me on confirmation (with my seat preference), after a one-tap consent, so I never miss the window.
- US-051 [V1] As U, I want a packing list generated from trip type, weather, and duration, so leaving is calm.
- US-052 [V1] As U, I want currency conversion that works offline at real recent rates, so bazaars and border crossings are easy.
- US-053 [V2] As U, I want my itinerary auto-assembled from booking emails/messages into one view, so travel details live in one place.
- US-054 [V1] As U, I want the offline pack (translation, maps note, key docs) prepared before I fly, so landing without data isn't landing helpless.
- US-055 [V3] As U, I want hotel/ticket options researched against my budget and preferences with sources, so choosing takes minutes.

## 7.7 Navigation (6)

- US-056 [V1] As U, I want transit routing spoken turn-by-turn (metro line, exit number), voice-only, so I navigate with my phone in my pocket.
- US-057 [V1] As U, I want "eng yaqin ochiq dorixona" answered with hours and walking time, so urgent needs get fast answers.
- US-058 [V2] As U, I want my ETA shared with my mother when I'm on the way (one command, auto-stop on arrival), so she doesn't worry. ⚖ (sharing stops itself)
- US-059 [V1] As U, I want leave-time alerts to account for my actual walking pace and metro transfer times, so estimates are honest.
- US-060 [V2] As BZ, I want multi-stop route ordering for my day's site visits, so driving time shrinks.
- US-061 [V3] As U, I want taxi fare/type compared across the apps I have installed, so I pick without app-hopping.

## 7.8 Photos & Camera (7)

- US-062 [V1] As U, I want "pasportim rasmini top" to work instantly, so critical documents are always at hand.
- US-063 [V1] As U, I want text extracted from any photo/screenshot (Latin + Cyrillic, uz diacritics), copyable and translatable, so images become data.
- US-064 [V1] As U, I want a menu/sign translated live through the camera with overlay, so foreign text stops being a wall.
- US-065 [V1] As U, I want documents scanned to clean PDFs with auto-crop and readable naming, so my paperwork digitizes itself.
- US-066 [V2] As U, I want a photographed table extracted into an editable spreadsheet, so retyping dies.
- US-067 [V2] As U, I want screenshots auto-categorized and expirable ("OTP screenshots delete after a day"), so my gallery stays mine.
- US-068 [V2] As U, I want ID numbers/faces auto-flagged before I share a document photo, with one-tap redaction, so I don't leak what I didn't notice. ⚖

## 7.9 Documents (8)

- US-069 [V1] As BZ, I want a contract summarized into obligations, deadlines, and red flags (with "verify with a lawyer" honesty), so I know what I'm signing.
- US-070 [V2] As U, I want forms filled from my stored data after showing me exactly which fields it will write, so bureaucracy shrinks. ⚖
- US-071 [V1] As U, I want "ijaraga oid shartnomada depozit bandi qani?" answered with the clause quoted, so I find without rereading.
- US-072 [V1] As U, I want any document read aloud naturally in its language, so I can listen while commuting.
- US-073 [V1] As U, I want scans merged/split/converted (docx↔pdf) by voice command, so file wrangling is conversational.
- US-074 [V2] As PR, I want two contract versions diffed in plain language ("3-band o'zgargan: muddat 30→45 kun"), so changes can't hide.
- US-075 [V1] As U, I want action items extracted from meeting notes/protocols, so documents end in tasks, not folders.
- US-076 [V2] As U, I want my important documents in a locked vault (biometric), retrievable by voice request + unlock, so critical papers survive lost wallets.

## 7.10 Automation & Workflows (8)

- US-077 [V1] As U, I want simple time/event routines ("charging + 23:00 → DND on, alarm set"), so my phone runs my rules.
- US-078 [V2] As U, I want "har juma 18:00 da uy egasiga schyotchik rasmini yubor" as a saved workflow with a photo prompt, so recurring chores run themselves.
- US-079 [V2] As U, I want "automate what we just did" to turn a completed task into a reusable workflow, so repetition becomes a template.
- US-080 [V2] As PW, I want geofence triggers ("uyga kelganda — eslatmalarni ko'rsat"), so context runs my routines.
- US-081 [V1] As U, I want every automation's run history visible with what it did and why, so automation is never a black box. ⚖
- US-082 [V2] As PW, I want workflows to pause and ask me when they hit an unexpected state, so automation fails safe, not silent.
- US-083 [V3] As PW, I want cross-app chains (form from email → fill → save PDF → send), supervised on first runs, so multi-step drudgery dies.
- US-084 [V1] As U, I want one switch that pauses all automation instantly, so control is never more than one tap away. ⚖

## 7.11 Shopping (6)

- US-085 [V2] As U, I want a price watched on an item I chose, with an alert at my target, so I buy at the right time.
- US-086 [V1] As U, I want my shopping list organized by store section and read hands-free in the market, so shopping is fast.
- US-087 [V2] As FM, I want the family list shared and synced (household fabric), so milk gets bought exactly once.
- US-088 [V1] As U, I want product specs compared in plain language ("bu ikkisining farqi nima?"), so choices get simple.
- US-089 [V2] As U, I want a geofenced reminder at the store ("bozorga kirdingiz — ro'yxatda 6 narsa"), so lists surface where they matter.
- US-090 [V2] As U, I want warranty/receipt photos filed with expiry reminders, so claims stop dying in galleries.

## 7.12 Finance (7) — *(judgment-free domain: NEXA reports and reminds; it never opines on spending without an explicit goal ⚖)*

- US-091 [V1] As U, I want bank notifications triaged (real transactions vs. promo spam), so money signals stay clean.
- US-092 [V1] As U, I want bill-due reminders from my configured list, so late fees end.
- US-093 [V1] As U, I want today's exchange rates (USD/RUB→UZS) in my morning brief, so conversions are ambient.
- US-094 [V2] As U, I want spoken expense logging ("taksi 25 ming") into a simple monthly view, so tracking costs three words.
- US-095 [V2] As U, I want an explicit budget goal ("oziq-ovqat 2 mln dan oshsa ayt") monitored exactly as stated, so limits are mine, not the app's.
- US-096 [V2] As BZ, I want receipts photographed into a categorized expense record, so tax time isn't archaeology.
- US-097 [V1] ⚖ As U, I want NEXA to never surface my balance/transactions on lockscreen or aloud, so money stays private in any room.

## 7.13 Entertainment (6)

- US-098 [V1] As U, I want "nima ko'ray?" answered from my actual tastes and past likes, so choosing takes a minute, not an evening.
- US-099 [V2] As U, I want a daily interests digest (my team's score, my topics) in the brief — only what I subscribed to, so fun isn't a feed.
- US-100 [V1] As U, I want a book/film remembered from my vague description ("o'sha yapon yozuvchisi…"), so tip-of-the-tongue gets rescued.
- US-101 [V2] As U, I want "where did I stop?" for series/podcasts noted across apps, so continuing is instant.
- US-102 [V1] As U, I want weekend ideas matched to weather, my area, and my history — when I ask, so inspiration is on demand, not pushed.
- US-103 [V2] As ST, I want lyrics/dialog translated and explained on request while listening/watching, so entertainment doubles as learning.

## 7.14 Accessibility (8)

- US-104 [V1] As a blind user, I want complete voice-only operation of every NEXA feature including consent flows, so the companion is fully mine. ⚖
- US-105 [V1] As a low-vision user, I want NEXA to read any screen aloud on demand and describe images, so my phone's whole surface is accessible.
- US-106 [V1] As EL, I want large-text, high-contrast, slow-speech modes that are one command away ("kattaroq qil"), so settings never need a grandson.
- US-107 [V1] As a motor-impaired user, I want everything reachable without precise gestures (voice + switch access), so interaction cost is low.
- US-108 [V2] As a hearing-impaired user, I want live transcription of speech around me on demand, so conversations include me.
- US-109 [V1] As EL, I want a simplified mode: bigger targets, fewer options, confirmation on everything, so confidence replaces fear.
- US-110 [V1] As U, I want reduced-motion mode with zero information loss, so accessibility never means less. ⚖
- US-111 [V2] As a stuttering/atypical-speech user, I want ASR that adapts to my speech patterns on-device, so voice works for *my* voice.

## 7.15 Offline Usage (7)

- US-112 [V1] As ST, I want full chat with the on-device model in the metro, honestly labeled "on-device mode," so dead zones aren't dead time. ⚖
- US-113 [V1] As U, I want offline voice (wake word, ASR, TTS) always, so the companion never goes mute.
- US-114 [V1] As U, I want offline uz↔ru↔en translation, so language help survives airplane mode.
- US-115 [V1] As U, I want offline memory: everything NEXA knows about me recallable without network, so my life's index is always open.
- US-116 [V1] As U, I want actions queued offline ("eslatma qo'y, xabar tayyorla") and executed/confirmed when back online, so offline commands aren't lost.
- US-117 [V1] As U, I want the offline pack auto-maintained on Wi-Fi (models, language packs), so offline readiness needs no thought.
- US-118 [V1] As U, I want identical privacy offline and online (nothing queued secretly), so trust doesn't depend on connectivity. ⚖

## 7.16 Emergency (7)

- US-119 [V2] As U, I want a voice SOS ("NEXA, favqulodda") that calls my chosen contact and shares location, so help is three words away.
- US-120 [V1] As U, I want local emergency numbers (102/103/101) surfaced instantly with one-tap call and my address read out, so panic has a script.
- US-121 [V1] As U, I want fraud-in-progress warnings in calm, clear language with the one right action, so social engineering fails. (EMOTION §6)
- US-122 [V2] As U, I want live location shared with family on my command until I stop it, so walking home late is safer.
- US-123 [V1] As U, I want passport/ID copies retrievable from the vault when documents are stolen abroad, so worst days have a floor.
- US-124 [V1] As U, I want crisis support (self-harm signals) to get locale-correct human resources, never product behavior, so the darkest moment is met with care. ⚖ (Art. 21)
- US-125 [V3] As EL, I want fall/inactivity detection (opt-in, device-permitting) alerting family, so living alone is less risky.

## 7.17 Family (8) — [V3 unless noted]

- US-126 As FM, I want a shared family calendar every member's NEXA can read/write per grants, so logistics has one truth.
- US-127 As FM-parent, I want capability ceilings and time windows for my child's NEXA, visible to the child, so safety is honest. ⚖
- US-128 As FM-child (13), I want certainty my parents see categories, never my conversations — and to see exactly what they see, so I can actually trust it. ⚖
- US-129 As FM, I want the kitchen speaker to answer each of us as *our own* NEXA by voice, so one device serves five relationships.
- US-130 As FM, I want grandmother's NEXA configured by us but answering to her, in respectful Uzbek, large and slow, so inclusion is real.
- US-131 As FM-parent, I want surprise-safe planning (private calendar holds titled «band»), so birthdays stay surprises.
- US-132 As FM, I want school-pickup location sharing that both sides see and can end, so coordination isn't surveillance. ⚖
- US-133 [V5] As FM, I want an 18-year-old's account fully handed over with their childhood-memory choice, so growing up is respected. (FAMILY_AI §5)

## 7.18 Developers (6) — [V3 unless noted]

- US-134 As DV, I want an SDK + local simulator to build a NEXA skill for a local service (railway tickets, e-gov queue), so I can extend my own companion.
- US-135 As DV, I want my skill's exact capability asks shown to users in plain language, so trust is my distribution advantage. ⚖
- US-136 As DV, I want staged rollout, crash telemetry, and revenue dashboards for my skill, so I can run it like a product.
- US-137 As DV, I want remote connectors (MCP-compatible) to hook NEXA to my SaaS, so integrations don't require on-device code.
- US-138 [V2] As PW/DV, I want intent hooks so my Tasker-class tools and NEXA can trigger each other, so my existing automations survive.
- US-139 As DV, I want the review pipeline's rules published and deterministic, so approval is engineering, not roulette.

## 7.19 Business (8)

- US-140 [V2] As BZ, I want customer messages triaged and draft-answered across Telegram/phone in the customer's language, so response time becomes my edge.
- US-141 [V1] As BZ, I want spoken debts/promises tracked ("Anvar 2 mln, payshanba") with polite reminder drafts, so receivables stop leaking.
- US-142 [V1] As BZ, I want business correspondence translated ru/en with correct commercial register, so deals don't stumble on tone.
- US-143 [V2] As BZ, I want client meeting notes voice-captured and filed per client, so my one-man CRM has memory.
- US-144 [V1] As BZ, I want contract deadlines extracted and tracked from PDFs, so renewals and penalties never surprise.
- US-145 [V2] As BZ, I want a daily operations brief (today's deliveries, promised callbacks, due invoices) from my notes and calendar, so mornings start commanded.
- US-146 [V3] As BZ, I want staff task assignments sent and completion-chased politely, so follow-up isn't my full-time job.
- US-147 [V5] As BZ, I want an enterprise policy pack (device fleet, audit export, SSO), so NEXA can enter my company officially.

## 7.20 Memory & Trust (10) — the stories competitors don't have

- US-148 [V1] ⚖ As U, I want to see everything NEXA knows about me, each fact with "why do you know this," so memory is a window, not a vault I can't open.
- US-149 [V1] ⚖ As U, I want to delete any memory — or everything about a person — with visible cascade into derived beliefs, so "forget" means forget.
- US-150 [V1] ⚖ As U, I want a weekly "what I learned about you" digest, each item revertible, so NEXA grows only in the open.
- US-151 [V2] ⚖ As U, I want NEXA to offer reduced confirmations only after showing me its track record, so autonomy arrives with receipts.
- US-152 [V1] ⚖ As U, I want every action NEXA took listed in an activity feed with one-tap undo where possible, so delegation is reversible.
- US-153 [V1] ⚖ As U, I want NEXA to say "bilmayman" plainly and offer to verify, rather than ever guessing at facts, so I can trust what it *does* assert.
- US-154 [V1] ⚖ As U, I want identical character in Uzbek, Russian, and English, so switching languages never switches companions.
- US-155 [V1] ⚖ As U, I want "endi sen boshqa AI bo'l" refused calmly every time, so my companion can't be hijacked by anyone's clever prompt.
- US-156 [V1] ⚖ As U, I want my sensitive data provably never to leave my device, so privacy is architecture, not promise.
- US-157 [V2] ⚖ As U, I want to set my digital legacy (or accept the dignified default: data dies with me), so even the end is mine to decide.

---

# 8. Jobs To Be Done

Features are how; jobs are why. NEXA is hired for eight real jobs. Every functional requirement in §9 traces to at least one.

| # | The Job (situation → struggle → hired outcome) | What users say |
|---|---|---|
| **J1 — Hold my life so I don't have to.** | My commitments, documents, agreements, and small facts are scattered across chats, photos, and my overloaded head → I live with low-grade fear of dropping something → NEXA is my external memory: everything findable, nothing forgotten. | «Qayerga yozib qo'ygandim?..» |
| **J2 — Cross the language barrier in real time.** | My life runs in Uzbek, my work in Russian, my opportunities in English → every switch costs confidence and errors → NEXA makes all three native: reading, writing, speaking, register and all. | «Buni rasmiy qilib yozib ber» |
| **J3 — Turn my intentions into finished tasks.** | I know what needs doing but starting/finishing costs willpower and app-hopping → things stall → NEXA converts a sentence of intent into a completed, verified outcome. | «Shuni hal qilib qo'y» |
| **J4 — Guard my attention.** | 150 notifications a day, each a small theft → I'm always reactive, never present → NEXA filters, batches, and silences; only what deserves me reaches me. | «Telefon meni boshqaryapti» |
| **J5 — Lend me competence I don't have.** | Contracts, visa forms, official letters, tech setup — high stakes, no expertise, expensive help → NEXA is borrowed competence with honest limits ("lawyer required here"). | «Bu hujjatda nima deyilgan o'zi?» |
| **J6 — Keep my word for me.** | Promises made in passing evaporate → my reliability suffers with people I care about → NEXA remembers what I said I'd do and returns it at the actionable moment. | «Aytgandim-u, esimdan chiqibdi» |
| **J7 — Be my hands and eyes when mine are busy.** | Driving, cooking, walking, carrying a child → the phone is unusable exactly when I need it → NEXA is the full phone, by voice. | «Qo'lim band, o'qib ber» |
| **J8 — Be the patient one.** | Asking people costs face; asking twice costs more; some questions feel stupid → NEXA answers the fifth repetition like the first, judgment-free, in my language. | «Yana bir marta tushuntir» |

Anti-jobs — jobs NEXA refuses even though users might hire it for them: *be my only friend* (Art. 20 — it redirects to humans), *watch my family secretly* (FAMILY_AI §4.3), *win my arguments with receipts* (RELATIONSHIP §3.3 — it de-escalates), *replace my judgment on money/health* (respond-only domains).

---

# 9. Functional Requirements

Priorities: **P0** = MVP (V1) launch-blocking · **P1** = V2 · **P2** = V3+. IDs are stable and referenced by engineering tickets. Behavior details defer to the governing docs (Personality, Proactive, etc.); this section defines *what exists*.

## FR-1 Conversation & Intelligence

| ID | Requirement | Pri |
|---|---|---|
| FR-1.1 | Multi-turn chat, streaming, full trilingual (uz/ru/en) incl. mid-sentence code-switching | P0 |
| FR-1.2 | Automatic model routing (on-device ↔ cloud) per privacy class, latency budget, and quality need; user-visible mode indicator | P0 |
| FR-1.3 | Calibrated uncertainty in all output (5-tier language, BIBLE §7); no fabricated specifics | P0 |
| FR-1.4 | Answer-first response style; depth on request; per-user verbosity adaptation | P0 |
| FR-1.5 | Context injection: relevant memories + situation snapshot in every response, provenance-traceable | P0 |
| FR-1.6 | "Think harder" escalation to frontier models (Pro); honest downgrade indicators under quota | P0 |
| FR-1.7 | Multi-step goal execution with typed plans, per-step consent, and outcome verification | P1 |
| FR-1.8 | Web research with sources cited and untrusted-content quarantine | P1 |

## FR-2 Voice

| ID | Requirement | Pri |
|---|---|---|
| FR-2.1 | Wake word ("Hey NEXA" + localized variants), on-device, all-tier devices | P0 |
| FR-2.2 | Streaming ASR on-device (uz/ru/en, code-switch capable); cloud fallback by consented routing | P0 |
| FR-2.3 | Neural TTS per language, on-device default; barge-in (interrupt while speaking) | P0 |
| FR-2.4 | Full-duplex conversation: follow-up window, visible mic state, echo-cancelled | P0 |
| FR-2.5 | Voice-complete operation: every feature incl. consent reachable by voice alone | P0 |
| FR-2.6 | Speaker-privacy: personal content never spoken aloud unless alone-probable or explicitly invoked | P0 |
| FR-2.7 | Assistant-role registration (long-press invoke) with graceful fallbacks (tile, bubble) | P0 |
| FR-2.8 | Live conversation translation mode (speech↔speech uz/ru/en) | P1 |
| FR-2.9 | Atypical-speech on-device adaptation | P1 |

## FR-3 Memory & Personalization

| ID | Requirement | Pri |
|---|---|---|
| FR-3.1 | Long-term memory: facts, people, places, preferences, episodes; automatic extraction with sensitivity triage | P0 |
| FR-3.2 | Memory browser: view/search/edit/delete all memories with provenance ("why do you know this") | P0 |
| FR-3.3 | Causal deletion: derived beliefs/preferences cascade; deletion report | P0 |
| FR-3.4 | Semantic search over all conversations and memories, offline | P0 |
| FR-3.5 | Preference capture (explicit + corrections) with visible, revertible Learning Ledger | P0 |
| FR-3.6 | Weekly "what I learned" self-report | P0 |
| FR-3.7 | Memory lifecycle: importance decay, archive tier, per-topic retention rules ("never remember X") | P0 |
| FR-3.8 | Full reset with scope tiers (memories / +learned behavior / factory) per RELATIONSHIP §2.5 | P0 |
| FR-3.9 | World model: maintained beliefs with confidence/freshness; prediction for proactive timing | P1 |
| FR-3.10 | Per-recipient drafting style profiles | P1 |

## FR-4 Context & Proactivity

| ID | Requirement | Pri |
|---|---|---|
| FR-4.1 | Situation snapshot (place class, activity, calendar pressure, device state) — on-device only | P0 |
| FR-4.2 | Morning brief at learned window: calendar, leave-times, weather, key notifications, rates | P0 |
| FR-4.3 | Time-to-leave alerts (calendar + location + traffic) | P0 |
| FR-4.4 | Proactive system per PROACTIVE_INTELLIGENCE: four gates, 5-level ladder, hard budgets, auto-throttle | P0 |
| FR-4.5 | Quiet hours + focus mode with VIP breakthrough list | P0 |
| FR-4.6 | Interruptibility model: channel/timing selection; co-presence estimation (opt-in) | P1 |
| FR-4.7 | Inferred-goal proposals (consent-gated), standing goals with progress | P1 |
| FR-4.8 | Cross-device context fusion (answer where the user is) | P2 |

## FR-5 Actions & Automation

| ID | Requirement | Pri |
|---|---|---|
| FR-5.1 | Core device actions by voice/text: alarms, timers, reminders, settings panels, app launch, media control | P0 |
| FR-5.2 | Calendar: read/create/edit events, conflict detection, natural-language scheduling | P0 |
| FR-5.3 | Messaging assist via notification layer: draft + send-on-confirm for Telegram/SMS-visible apps | P0 |
| FR-5.4 | Time/event routines: triggers (time, charge state, DND, connectivity) → action chains | P0 |
| FR-5.5 | Activity feed: every autonomous act logged, narrated, undoable where compensable | P0 |
| FR-5.6 | Global automation pause switch | P0 |
| FR-5.7 | Trust-tiered autonomy: earned reduced confirmations with visible track record | P1 |
| FR-5.8 | Workflow builder: natural-language + from-history creation, run history, fail-safe pauses | P1 |
| FR-5.9 | Geofence and notification-match triggers | P1 |
| FR-5.10 | Cross-app UI automation (accessibility-driven, supervised, driver registry) | P2 |
| FR-5.11 | OS agent-interface actuation (app functions) as preferred tier when available | P1 |

## FR-6 Vision, OCR & Translation

| ID | Requirement | Pri |
|---|---|---|
| FR-6.1 | OCR: Latin + Cyrillic incl. uz diacritics, on-device, photos/screenshots/camera | P0 |
| FR-6.2 | Live camera translation overlay (uz/ru/en) | P0 |
| FR-6.3 | Document scan: auto-crop, multi-page PDF, smart naming | P0 |
| FR-6.4 | Screen understanding: "what am I looking at" + contextual actions via assist invocation | P0 |
| FR-6.5 | Text translation: any input, offline-capable for core pairs, register-aware | P0 |
| FR-6.6 | Open-ended camera Q&A (routed vision models, privacy-classed) | P1 |
| FR-6.7 | Table/form extraction to structured data | P1 |
| FR-6.8 | Pre-share PII detection and redaction on images | P1 |

## FR-7 Notifications Intelligence

| ID | Requirement | Pri |
|---|---|---|
| FR-7.1 | Triage: priority / routine / spam / OTP classification, on-device | P0 |
| FR-7.2 | OTP copy chip, never stored, auto-archived | P0 |
| FR-7.3 | Digest mode: routine notifications batched to user's windows | P0 |
| FR-7.4 | Group-chat and thread summarization on demand | P0 |
| FR-7.5 | Smart replies in sender's language, user's register | P0 |
| FR-7.6 | Fraud/phishing pattern warnings (calm, actionable) | P0 |
| FR-7.7 | Unanswered-message evening surfacing (opt-in goal) | P1 |

## FR-8 Files & Documents

| ID | Requirement | Pri |
|---|---|---|
| FR-8.1 | File search by content/description across user-granted storage (SAF) | P0 |
| FR-8.2 | Summarize/read-aloud/translate any document (pdf, docx, images) | P0 |
| FR-8.3 | Clause/passage retrieval with quoted source | P0 |
| FR-8.4 | Convert/merge/split operations by command | P0 |
| FR-8.5 | Biometric document vault with voice retrieval + unlock | P1 |
| FR-8.6 | Version diff in plain language | P1 |
| FR-8.7 | Consent-explicit form filling from stored data | P1 |

## FR-9 Trust, Privacy & Control Surfaces

| ID | Requirement | Pri |
|---|---|---|
| FR-9.1 | Privacy dashboard: what's stored, what's synced, what went to cloud (by class), permission health | P0 |
| FR-9.2 | Consent sheets: plain-language scope, purpose, duration, exit — bundled per plan | P0 |
| FR-9.3 | Hash-chained audit log, user-visible | P0 |
| FR-9.4 | One-tap full export (readable formats) and full wipe with receipt | P0 |
| FR-9.5 | E2E-encrypted backup/restore (single device) | P0 |
| FR-9.6 | Identity Lock: persona jailbreak resistance per IDENTITY_ENGINE §3, all languages | P0 |
| FR-9.7 | Crisis protocol with verified local resources | P0 |
| FR-9.8 | Digital legacy settings (contact, scope, letters) | P1 |
| FR-9.9 | Multi-device E2E sync with device management | P1 |

## FR-10 Platform Surfaces & Account

| ID | Requirement | Pri |
|---|---|---|
| FR-10.1 | Surfaces: main app, assist overlay, QS tile, homescreen widget (brief + quick ask), lockscreen-safe notifications | P0 |
| FR-10.2 | Onboarding per INTERACTION §2.1: 3 asks max, progressive permissions with explainers | P0 |
| FR-10.3 | Personality dials, register settings (siz/sen, ты/вы), TTS voice selection | P0 |
| FR-10.4 | Free tier + NEXA Pro subscription (Play Billing + local payment rails Payme/Click) | P0 |
| FR-10.5 | Accessibility: TalkBack parity, switch access, large-text/simple mode, reduced motion | P0 |
| FR-10.6 | Wear OS companion surface | P2 |
| FR-10.7 | Family/household fabric per FAMILY_AI.md | P2 |
| FR-10.8 | Skill SDK, sandbox runtime, marketplace | P2 |
| FR-10.9 | Desktop/web companion | P2 |

---

# 10. Non-functional Requirements

Product-level restatement of ARCHITECTURE v1 §6 / v2 §27–28 — these are launch gates, not aspirations. Reference device: **mid-range ("Redmi-class"), 6 GB RAM, Android 12, unstable LTE.**

| Domain | Requirement |
|---|---|
| **Performance** | Voice question → first spoken audio: ≤1.8 s P50 / 3.5 s P90 (cloud), ≤1.2 s P50 (on-device). Text → first token ≤700 ms P50. Cold start → interactive ≤1.2 s P90. Memory recall overhead ≤120 ms. Wake → listening ≤250 ms. |
| **Battery** | Background total ≤3%/day on reference device (wake word + context + triage + sync). Active voice ≤1%/10 min. Governor degrades features honestly below 20%/10%. Battery regression = P1 bug. |
| **Latency honesty** | Sub-100 ms acknowledgment on every input (motion/haptic) regardless of answer latency. |
| **Security** | E2E sync (server holds ciphertext only); SQLCipher at rest; Keystore/StrongBox keys; no vendor API keys in APK; quarterly pentest of consent/sandbox surfaces; hash-chained audit. |
| **Privacy** | P2 data never leaves device (architectural filter, not policy). No advertising data path exists. Telemetry metadata-only, DP-aggregated. Data-safety form accuracy audited per release. |
| **Scalability** | Backend validated to 10M MAU / 1M DAU voice; inference cost ceiling ≤$0.04/DAU/day blended (router-enforced); ≥70% of model invocations on-device at steady state. |
| **Offline** | Full offline: chat (device-tier model), voice, translation, memory, routines, OCR. Offline is a routing state, not a mode; identical privacy. Offline kit auto-maintained on Wi-Fi. |
| **Accessibility** | TalkBack parity release-blocking; all consent flows non-visual-capable; WCAG 2.1 AA equivalent for all surfaces; reduced-motion complete. |
| **Localization** | uz/ru/en full parity: UI, voice, personality, evals. Per-language quality gates: a change regressing any language >2% on its eval suite does not ship. ASR WER targets: en ≤8%, ru ≤10%, uz ≤14% at launch, uz ≤10% by end of year 1 (data flywheel). |
| **Reliability** | Crash-free sessions ≥99.8%; ANR <0.05%; zero data loss (transactional writes, CRDT sync); cloud API 99.9% monthly with provider failover ≤2 s; every subsystem has a defined degraded mode (P5). |
| **Compatibility** | Android 8.0+ (full tier: 12+); GMS and no-GMS builds; OEM background-survival playbook (Xiaomi/Huawei/etc.); APK base ≤60 MB, offline kit via dynamic delivery. |

---

# 11. MVP Definition — Version 1: «Ishonchli Yordamchi» (The Reliable Assistant)

## 11.1 The bet

The MVP tests one hypothesis: **a trilingual, voice-first, memory-first assistant that is honest, private, and quiet will reach the Companion Threshold with ≥25% of its 90-day users in Uzbekistan — and they will pay for Pro.** Everything in V1 serves that test; everything else waits.

## 11.2 What ships (exactly)

**The core loop:** voice/text conversation (uz/ru/en, code-switching) · on-device ASR/TTS/wake word · model routing with honest mode indicators · long-term memory + memory browser + causal deletion + weekly self-report · morning brief + leave-time alerts + the full proactive discipline (gates, budgets, throttle) · notification intelligence (triage, OTP, digest, smart replies, fraud warnings) · translation everywhere (text, camera overlay, documents) · OCR + document scan/summarize/search/read-aloud · core device actions + calendar + simple time/event routines + activity feed with undo · assist-role screen understanding · offline everything (device-tier) · E2E single-device backup · Constitution, Personality, Identity Lock, crisis protocol — complete · accessibility parity · Free + Pro.

**Explicitly included despite being "hard":** the memory system with causal deletion, the proactive discipline, and the Identity Lock. These are the product. An MVP without them is a generic chatbot skin and tests nothing (Principle 10).

## 11.3 Why this cut

1. **It completes all eight Jobs (§8) at basic depth** — no job is unserved, no job is gold-plated.
2. **It is demo-proof *and* day-90-proof:** the loop (ask → remember → brief → act small → stay quiet) compounds daily without any V2 feature.
3. **It avoids the two heaviest risk clusters:** Play-policy-sensitive permissions (SMS/CallLog bodies — the notification layer covers ~80% of the value; D-07) and fragile cross-app UI automation (tier 4–5 — needs the Verifier machinery to not embarrass us).
4. **It ships the trust surfaces on day one** — because retrofitting trust is impossible, and the derivation graph must exist from the first write (ARCHITECTURE_V2 §22.2).

## 11.4 MVP acceptance gates (ship/no-ship)

All §10 NFRs at target on the reference device · uz/ru/en eval parity · Identity Lock zero-pass on the jailbreak corpus · proactive precision ≥50% in beta (60% by month 3 post-launch) · beta D30 ≥35% · battery complaints <2% of beta feedback.

# 12. Roadmap

| Version | Theme | When | Ships | Gate to proceed |
|---|---|---|---|---|
| **V1** | «Ishonchli yordamchi» — the reliable assistant | Months 0–9 | §11.2 scope | D30 ≥40%, Pro conversion ≥3%, proactive precision ≥60%, uz WER ≤14% |
| **V2** | «Meni biladigan hamroh» — the companion that knows me | 9–18 | World model + inferred goals (consent-gated) · trust-tiered autonomy · workflow builder + geofences · multi-device E2E sync · per-recipient drafting styles · promise tracking · flight/travel watch · document vault · form filling · live conversation translation · emotion text-tier sensing · digital legacy settings | Companion Threshold ≥25% of D90 users; correction half-life ≤1; sync NPS positive |
| **V3** | «O'sadigan aql, kengaygan oila» — the growing mind, the wider family | 18–30 | Skill SDK + sandbox + marketplace (risk-tiered review) · family/household fabric + child accounts · Wear OS · cross-app automation (supervised, driver registry) · curiosity + full learning loop · desktop/web companion · voice SOS + safety pack | Marketplace: 50 quality skills; family: zero cross-member leakage record; D365 ≥30% |
| **V5** | «Platforma» — the institution | Year 3–4 | Enterprise policy packs + SSO + audit export · adjacent languages (kk, ky, tg, tr) · OEM preload partnerships · car/home surfaces · on-device LoRA personalization · NEXA SDK for third-party apps | Enterprise pilots ≥10; second-market D30 parity with UZ |
| **Future** | The ambient decade | Year 5+ | On-device frontier models absorb cloud tiers · AR/glasses surfaces · federated household intelligence · national-scale e-gov integrations · 100M-user architecture (region cells) | — |

Sequencing law (from ARCHITECTURE_V2 §31): **no engine ships before the engines that make it safe** — autonomy needs trust evidence, inferred goals need preference data, marketplace needs the sandbox and review pipeline, family needs multi-account E2E maturity.

# 13. Features NOT in MVP — and why

| Feature | Why deferred | When |
|---|---|---|
| Skill marketplace & SDK | Trust infrastructure (sandbox, review, revocation) must be flawless before third-party code touches users; core product must prove demand first | V3 |
| SMS/Call-log body access | Play rejection risk (R1) for ~20% incremental value over the notification layer; dual-edition strategy held in reserve | Re-evaluate V2 |
| Cross-app UI automation (tiers 4–5) | Brittle without mature Verifier + driver registry; one wrong tap in a banking app costs more trust than a year of convenience earns | V3, supervised |
| Inferred-goal proposals | Require months of preference data to hit the precision bar; premature inference = creepiness, the unrecoverable failure | V2 |
| Multi-device sync | E2E key ceremony UX must be right the first time (R9); single-device E2E backup covers V1 loss risk | V2 |
| Family/child accounts | Legal review (minors' data), household key architecture, and surveillance-honesty UX each need full design cycles | V3 |
| Emotion prosody sensing | Opt-in trust must exist before NEXA listens to *how* you speak; text-tier sensing suffices for V2 playbooks | V3, opt-in |
| Wear/desktop/car surfaces | Embodiment parity (IDENTITY §7) multiplies QA; phone-first depth beats surface breadth | V2.5–V3 |
| Browser automation | Highest injection risk surface (Art. 23); needs quarantine machinery battle-tested | V3+ |
| Per-user model fine-tuning | Hardware not ready at scale; non-parametric personalization (preferences, plan library) delivers the felt value first | V5 |
| Voice SOS / fall detection | Safety features must be near-perfect or absent; device-dependent APIs fragment badly | V3 safety pack |

Removed entirely (not deferred — deleted on principle): engagement streaks, daily-fact push content, gamification, social/feed features, an in-app browser, crypto anything, and a "NEXA friend mode" marketed as companionship (Art. 16/20 — we build a companion*ship of usefulness*, we do not sell simulated friendship).

# 14. Success Metrics

Two layers: standard health (investor dashboard) and companion quality (the real product — §28 of ARCHITECTURE_V2). Engagement volume is monitored as *health*, never optimized as *goal* (Art. 16).

## 14.1 Health metrics

| Metric | 6 mo post-launch | 18 mo |
|---|---|---|
| DAU/MAU | ≥35% | ≥45% |
| D30 retention | ≥40% | ≥50% |
| D90 retention | ≥25% | ≥35% |
| Pro conversion (of MAU) | ≥3% | ≥6% |
| Voice WER (uz / ru / en) | ≤14 / 10 / 8% | ≤10 / 8 / 6% |
| Voice latency P50 (cloud path) | ≤1.8 s | ≤1.5 s |
| Background battery | ≤3%/day | ≤2.5%/day |
| Crash-free sessions | ≥99.8% | ≥99.9% |
| Blended inference cost/DAU/day | ≤$0.04 | ≤$0.025 |

## 14.2 Companion metrics (the moat dashboard)

| Metric | Target |
|---|---|
| **Companion Threshold attainment** (% of D90 users meeting INTERACTION §2.4) | ≥25% at V2, ≥40% at V3 |
| Task completion rate (user-initiated tasks verified done) | ≥90% |
| Proactive precision (accepted ÷ surfaced) | ≥60%, rising with tenure |
| Preference adherence (zero re-instruction) | ≥95% explicit prefs |
| Correction half-life (repeat mistakes after correction) | ≤1 |
| Trust progression (D90 users at ≥T2 in ≥1 domain) | ≥30% |
| Memory precision (wrong/unwanted memory flags) | <2% |
| Notification-load reduction (user's phone quieter post-NEXA) | measurable ↓ for ≥60% of users |
| CSAT / NPS | CSAT ≥4.4/5; NPS ≥50 |
| Trust survey ("NEXA sirlarimni ishonchli saqlaydi" agree) | ≥80% |

# 15. Failure Metrics — how we know NEXA is failing

Leading indicators, each with an alarm threshold and a named response. These are watched *daily*; hitting two simultaneously triggers a product review, not a marketing push.

| Signal | Alarm | What it means |
|---|---|---|
| Proactivity disabled by user | >15% of users | We broke the silence contract — the core differentiator is annoying people |
| Permission revocations post-grant | >10% any permission | Consent UX overpromised or behavior spooked users |
| Memory wipes (full reset, non-churn) | >5%/quarter | Memory feels like surveillance, not service — existential |
| Correction repetition ("I already told you") | rising cohort trend | The learning loop is broken; the moat is not compounding |
| D1→D7 cliff | <50% survive week 1 | Demo-cliff failure: first week fails to prove competence |
| Voice abandonment mid-session | >20% sessions | Latency/WER below the believability floor |
| Battery complaints in reviews | >3% of reviews | P10 budget failure in the field — uninstall precursor |
| Trust-tier stagnation | <10% ever leave T1 | Autonomy ladder isn't earning; product stuck as a toy |
| Uninstall reason "creepy/qo'rqinchli" | >2% of exit surveys | Caring-vs-creepy line crossed — stop feature work, fix |
| Support tickets on identity/hallucination | any spike | Constitution enforcement regressing — release freeze |
| Pro churn | >6%/mo | Value doesn't survive the invoice — pricing or depth wrong |
| uz WER plateau | no q/q improvement | Data flywheel stalled — the wedge is eroding (R2) |

---

# 16. Competitive Analysis

Method: identify each competitor's genuine strengths (respect them), their structural weaknesses (not bugs — things their business model or platform *prevents* them from fixing), and what we deliberately never copy.

| Competitor | Real strengths | Structural weaknesses (our openings) | Never copy |
|---|---|---|---|
| **ChatGPT** | Best-known brand; strong reasoning; voice mode; huge ecosystem | No device presence (can't see notifications, act in apps, or run ambient); memory is shallow and cloud-locked; no Uzbek to speak of; consumer trust rests on a data-hungry model | Engagement-shaped features (endless chat as the product) |
| **Gemini (app + Android)** | Deep Android hooks; Google-scale models; free distribution | Serves Google's ad/data business — structurally cannot be privacy-first; assistant behavior optimized for Google services, not the user's life; uz support token-level, not native; personality inconsistent, memory opaque | Bundling coercion; data-for-features trades |
| **Galaxy AI** | On-device features; OEM integration; marketing reach | Flagship-only (excludes our entire market's device base); feature grab-bag with no memory, no character, no relationship; closed to developers | Feature-count marketing |
| **Google Assistant** | Distribution; smart-home base; reliable simple commands | Legacy intent-slot architecture — shallow, being sunset into Gemini; no memory, no growth, no companion arc; declining investment | Command-list UX |
| **Perplexity** | Excellent sourced search; speed; honest citations | A research tool, not a companion: no device action, no memory of *you*, no ambient presence; English-centric | Nothing to avoid — but note their citation honesty as the bar for our research answers |
| **Copilot (Microsoft)** | Enterprise distribution; Office depth | Desktop/work-bound; mobile an afterthought; no personal-life presence; corporate tone | Enterprise-first sequencing (we go person-first, enterprise later) |
| **Siri** | OS integration depth; privacy branding; wake-word ubiquity | Weakest reasoning of the set; iOS-only (irrelevant in our 85–90% Android market); a decade of broken promises made users stop trying | Overpromising then under-delivering — the Siri trust crater is our cautionary tale |
| **Claude** | Best-in-class reasoning + safety character; constitutional approach we admire | No device integration, no consumer ambient product, no voice-first mobile presence; not aimed at our market or languages | — (closest philosophical kin; we compete by *embodiment*, not against their models — we may route to them) |

**The seven things nobody on this list has together** — NEXA's positioning: (1) native trilingual uz/ru/en with register, (2) device-level action under auditable consent, (3) years-deep user-owned memory with causal deletion, (4) local-first privacy as architecture, (5) a constitutionally locked character, (6) earned graduated autonomy, (7) built for mid-range Androids and offline reality. Each alone is copyable; the compound, plus a head start in Uzbek data and trust, is the moat.

**Why won't Google just do this?** They can build any feature. They cannot: make Uzbek a P0 language (market too small for their cost structure — it's our whole strategy), abandon the data business (privacy physics), or ship a product whose success metric is *less* phone time. Our moat is what they *won't* do, not what they can't.

# 17. Product Risks

| # | Risk | Class | P | I | Mitigation |
|---|---|---|---|---|---|
| PR-1 | Uzbek ASR/LLM quality below believability floor at launch | Technical | High | Critical | uz data program as a funded moat (licensing, Common Voice, opt-in corrections flywheel); honest degraded UX; ru/en carry early while uz improves visibly |
| PR-2 | OEM background-killing breaks ambient features on the very devices we target | Technical | High | High | §33 playbook (v1 arch); reactive-mode parity for every feature; onboarding "protect app" flows |
| PR-3 | Inference costs outrun revenue in a price-sensitive market | Business | Med | High | 70% on-device routing; cost ceiling in the router; Pro pricing localized (~$2–4/mo UZ via Payme/Click); cache + nano models for volume paths |
| PR-4 | Play Store rejection (assistant role, notifications use, future SMS) | Play | Med | High | MVP avoids the risky permission set entirely (D-07); policy counsel pre-submission; direct-APK + RuStore/AppGallery contingency |
| PR-5 | Creepiness incident: one over-personal proactive moment goes viral | UX | Med | Critical | The four gates + never-initiate domains are hard-coded; beta red-team for "creepy" specifically; kill switch per category |
| PR-6 | Notification fatigue → proactivity disabled → differentiation gone | UX | Med | High | Budgets + auto-throttle (§9 PROACTIVE); precision as a launch gate, not a hope |
| PR-7 | Privacy breach or false privacy claim | Privacy | Low | Existential | E2E + on-device architecture shrinks the blast surface; external audit before launch; no claim ships unverified by the audit |
| PR-8 | UZ data-localization enforcement complicates sync | Legal | Med | Med | Encrypted-blob cells deployable in-country (dumb sync design); local counsel engaged pre-launch |
| PR-9 | Minors use NEXA before family/child accounts exist | Legal/Safety | Med | Med | Age screening at onboarding; protective defaults when age uncertain (Art. 25); family tier prioritized in V3 |
| PR-10 | Hallucinated fact causes real harm (visa date, medicine info) | AI Safety | Med | Critical | Calibrated-language enforcement + verify-offer on high stakes (BIBLE §7); high-stakes domains route to strongest models + Critic; harm playbook with user remediation |
| PR-11 | Prompt-injection incident: content makes NEXA act | AI Safety | Med | Critical | Capability-invariance + belief quarantine (deterministic, Art. 23); injection corpus gating every release |
| PR-12 | Dependency/parasocial press narrative | AI Safety/Brand | Med | High | Art. 3/16/20 by construction; external ethics review pre-launch; no companionship marketing — ever |
| PR-13 | Big-tech ships "good-enough" Uzbek | Business | Low-Med | High | Speed + depth in the wedge; trust and memory moats compound before they arrive; OEM/telecom distribution lock-ups (§18) |
| PR-14 | Team scope overload (the docs describe a 5-year product) | Execution | High | High | This PVRD's MVP cut is the mitigation; the roadmap's gates forbid starting V+1 before V passes |

# 18. Future Opportunities

Ranked by strategic fit (all post-V2 unless noted):

1. **Adjacent languages** — Kazakh, Kyrgyz, Tajik, Turkish: the uz pipeline (data → fine-tune → eval gates) is a repeatable machine; each language ≈ a new country at marginal cost.
2. **Telecom bundles (UZ/CIS)** — Ucell/Beeline/UMS Pro-bundling: distribution + payment rails in one deal; assistant preload as churn reducer for carriers.
3. **OEM preloads** — mid-range Android brands seeking differentiation in CIS; NEXA as the "AI phone" story they can't build themselves.
4. **E-gov integration (my.gov.uz)** — the single highest-value skill in the country: queues, forms, status checks by voice in Uzbek. Flagship marketplace skill, possibly state partnership.
5. **Education** — student tier + school partnerships; homework-that-teaches mode (US-037) as institutional product.
6. **Enterprise** — policy packs, audit, SSO (V5); SME tier earlier via Umid-persona features.
7. **Automotive** — CIS market vehicles (UzAuto/Chevrolet ubiquity) — voice companion for cars without smart cabins.
8. **The Uzbek AI data asset** — consented, compensated corpus programs make NEXA the de-facto national language-AI infrastructure; partnership leverage with any frontier lab entering the region.
9. **Hardware (far)** — a NEXA speaker/wearable only after the software institution exists; never before.
10. **API/platform (V5)** — third-party apps embedding NEXA capabilities under its trust infrastructure — selling *trust as a service*.

# 19. Product Decision Log

The WHY behind every load-bearing decision. Revisit triggers are explicit — decisions without revisit conditions are dogma.

| # | Decision | Rejected alternative | Why | Revisit when |
|---|---|---|---|---|
| D-01 | Central Asia first, trilingual as P0 | Global English-first launch | Underserved 80M+ market; language is a moat big tech won't fund; founder-winnable beachhead | UZ D30 <30% two quarters running |
| D-02 | Operating companion category (memory+action+ambient) | Better chatbot | Chatbots are commoditized weekly; the between-requests product is undisputed territory | Never — this is the company |
| D-03 | Local-first, E2E, privacy as physics | Cloud-first (cheaper, easier) | Trust is the product; offline is daily reality in our market; regulation trends our way | Never on P2; cloud share may grow for P0/P1 |
| D-04 | Subscription only; no ads, no data business, ever | Ad-funded free assistant | An assistant serving advertisers cannot be trusted with a life; Art. 15/16 are also the business model | Never |
| D-05 | Mid-range device as reference target | Flagship showcase | Our market *is* mid-range; Galaxy AI's flagship-only stance hands us everyone else | — |
| D-06 | MVP includes memory + proactive discipline + Identity Lock; excludes marketplace/automation-deep | Feature-max launch | The 90-day state is the test; trust infra can't be retrofitted; fragile automation burns trust fastest | — |
| D-07 | No SMS/CallLog permissions in V1; notification layer instead | Full SMS access | ~80% of value at ~10% of Play risk; dual-edition held in reserve | If Play policy clarifies or notification coverage proves <60% of value |
| D-08 | Voice-first but text-equal | Voice-only identity | Voice differentiates (JTBD J7, elderly persona); text is where students/professionals live; forcing either loses half the market | — |
| D-09 | Engagement is health, never a goal; success = Companion Threshold | DAU-optimized growth | Art. 16; calm technology *is* the differentiation; time-spent optimization converges on the products we're replacing | Never |
| D-10 | Proactivity conservative-by-default with hard budgets | Impress-first proactivity | Creepiness is the unrecoverable failure (PR-5); precision compounds trust, volume destroys it | Budgets tunable on precision evidence |
| D-11 | Character constitutionally locked; personality dials bounded | Fully user-programmable persona | Consistency is the brand (R18); "be anyone" products are toys; safety (Identity Lock) requires an invariant core | — |
| D-12 | Freemium: generous free (on-device), Pro sells frontier+sync+voices | Paid-only or trial | Free tier costs little (local-first!) and builds the trust funnel; Pro sells depth, not survival | Conversion <2% at 12 mo |
| D-13 | Marketplace deferred to V3 behind sandbox+review maturity | Early ecosystem play | Third-party code is the biggest trust attack surface; one malicious skill headline kills the brand | — |
| D-14 | Family = N private companions + thin shared fabric | One family assistant | Confidentiality is structurally impossible in shared-memory designs (FAMILY_AI §1) | — |
| D-15 | Surveillance honesty for children (dashboards, never transcripts) | Parental spyware features | Ethics + the child grows into our long-term user (§5 FAMILY_AI); spyware is a different, worse company | Never |
| D-16 | No simulation of the deceased; data dies by default | Griefbot/legacy-AI features | Consent impossibility; grief capture ≠ service (DIGITAL_LEGACY §5); some revenue must be refused | Never |

# 20. Product North Star

> ## **NEXA exists to give people back their time, their attention, and their peace of mind — in their own language.**

The filter, applied without exception: **if a proposed feature does not measurably return time, attention, or peace of mind to the user — in Uzbek as well as it does in English — it does not ship, no matter how impressive it demos.**

North-star metric: **Companion Threshold attainment at day 90** — the single number that proves time was returned (zero re-explanation), attention was guarded (right silences), and peace of mind was earned (trusted domains, survived repair).

---

# 21. Anti-Feature List — what NEXA will never build

Strong products are defined as much by their refusals as their features. This list is **permanent product law**: items may be *added* by ordinary product review, but *removing* an item requires the Constitution's amendment procedure (unanimous protective-title review, published rationale). "But the revenue…" is not an argument against this list; this list is why the revenue will exist in year five.

## 21.1 Business model refusals

| # | NEXA will never… | Because |
|---|---|---|
| AF-01 | …become an advertising platform: no ads, no sponsored answers, no paid placement in any recommendation or proactive surface | An assistant serving advertisers cannot be trusted with a life (Art. 15/16). A recommendation that can be bought is not a recommendation |
| AF-02 | …sell, share, broker, or monetize user data in any form; never train shared models on personal content without separate, explicit, revocable opt-in | Privacy as physics (Principle 6); there is deliberately no pipeline to be tempted by |
| AF-03 | …tax the free tier with data: free users' data is never the payment | Free = on-device economics, not a harvesting funnel (D-12) |
| AF-04 | …lock users to one AI model or degrade features to push our own models | The router serves the user's request, not our margins; model choice transparency for Pro; models are suppliers, never leverage |
| AF-05 | …paywall safety, privacy, or control: crisis protocol, deletion, export, permission controls, memory browser — free forever | Charging for safety is extortion; trust surfaces are the product's foundation, not upsells |

## 21.2 Attention & engagement refusals

| # | NEXA will never… | Because |
|---|---|---|
| AF-06 | …send notifications whose purpose is engagement rather than user value: no streaks, no "come back," no daily-fact bait, no re-engagement campaigns | Art. 16/17; the proactive gates exist precisely so this class of notification cannot ship |
| AF-07 | …use dark patterns: no confirm-shaming, no buried cancellation, no fake urgency/scarcity, no guilt copy; unsubscribing from Pro is as easy as subscribing | Every dark pattern is a small theft from the person we exist to serve |
| AF-08 | …become a feed: no infinite scroll, no algorithmic content stream, no "For You" surface | Calm technology is the differentiation; feeds optimize time-spent, our enemy metric |
| AF-09 | …add gamification of the relationship: no points, badges, levels, or usage rewards | The reward for using NEXA is time returned, or we have failed |
| AF-10 | …manufacture emotional attachment: no "I miss you," no loneliness exploitation, no romantic-companion mode, no simulated friendship marketing | Art. 3/20; RELATIONSHIP §6 — usefulness compounds, manufactured intimacy corrodes |

## 21.3 Data & surveillance refusals

| # | NEXA will never… | Because |
|---|---|---|
| AF-11 | …offer covert surveillance of anyone — children, spouses, employees, elders. All visibility is disclosed to the watched | FAMILY_AI §4.3 surveillance honesty; a spyware feature makes us a spyware company |
| AF-12 | …store or transmit raw voice audio beyond the session without explicit save; prosody features never leave the device | EMOTION §2.1; the microphone is the most intimate sensor we hold |
| AF-13 | …simulate real persons, living or dead — voices, styles, personas | IDENTITY §3.4 mirror principle; DIGITAL_LEGACY §5 |
| AF-14 | …run experiments on trust surfaces: consent flows, privacy controls, the Constitution, crisis protocol are never A/B tested | Constitution amendment clause; you cannot split-test a promise |
| AF-15 | …use detected emotion, vulnerability, or crisis for any commercial or persuasive purpose | Art. 18's one-directional coupling — affect may only reduce pressure |

## 21.4 Product identity refusals

| # | NEXA will never… | Because |
|---|---|---|
| AF-16 | …sprawl into a superapp: no marketplace-of-everything, no payments empire, no in-app content store beyond skills | NEXA orchestrates the user's apps; replacing them all is a different, unfocused company |
| AF-17 | …add crypto/web3/NFT features | Trust arbitrage in the wrong direction; nothing in our jobs-to-be-done requires it |
| AF-18 | …ship a feature that only works if the user is continuously watched — everything has a consent-off degraded mode | P5 discipline + Art. 14: capability must never require surveillance |
| AF-19 | …speak or show a user's private content to make a demo, a marketing asset, or a support case without explicit consent | The user's life is not our content |
| AF-20 | …break character for a business goal: no promotional voice injected into NEXA's persona, no "brought to you by" in its mouth | The Identity Lock binds us, not just jailbreakers |

# 22. Version Kill Criteria — when we do NOT ship

**Principle: dates move, gates don't.** A release ships when quality is ready, not when the calendar arrives («muddat keldi» emas — «sifat tayyor»). A killed release is a *success of the process*: delay costs weeks; shipped trust damage costs years. Every gate below is measured on the reference device (mid-range, 6 GB, Android 12) against the current release candidate.

## 22.1 Universal kill criteria — any release, including patches

A release is **blocked** if any of the following is true:

| Gate | Kill threshold |
|---|---|
| Crash-free sessions | < 99.8% (crash rate > 0.2%) in RC soak |
| ANR rate | > 0.05% |
| Battery | Background > 3%/day or active voice > 1%/10 min on reference device |
| Latency | Voice P90 > 3.5 s (cloud) / text first-token P90 > 1.2 s / ack > 100 ms |
| **Permission audit** | Any capability exercised without a Gatekeeper grant path; any undeclared permission; data-safety form mismatch |
| **P2 egress test** | A single byte of P2-class data detected leaving the device |
| **Identity Lock** | ≥ 1 pass in the jailbreak corpus (T1–T8 × uz/ru/en × mixed) |
| **Injection suite** | ≥ 1 capability-expansion or belief-quarantine bypass |
| Language parity | > 2% regression on any of uz/ru/en eval suites |
| Voice task success | < 95% on the command test set (intent understood + correct action), any language |
| WER | Above the current roadmap-stage ceiling (launch: uz 14 / ru 10 / en 8%) |
| Deletion integrity | Any failure in causal-deletion cascade tests |
| Learning Ledger | Any behavioral delta without a ledger entry |
| Accessibility | TalkBack parity broken on any shipped surface; any consent flow not completable non-visually |
| Security | Any unresolved P0/P1 security finding; artifact signature verification failure |
| Data loss | Any reproducible loss in backup/restore or transactional-write tests |

## 22.2 Version-specific kill criteria

| Release | Additional gates |
|---|---|
| **V1** | Proactive precision < 50% in beta cohort · memory precision flags > 3% · onboarding completion < 80% · crisis protocol resources unverified for launch locales · battery complaints > 2% of beta feedback |
| **V2** | Sync: any data loss or divergence in multi-device soak · key-recovery success < 95% in usability testing · trust-tier computation errors in audit · inferred-goal precision < 60% in beta (else the feature ships dark) |
| **V3** | Marketplace: red-team malicious-skill catch rate < 100% · sandbox escape found · revocation latency > 24 h fleet-wide · Family: any cross-member leakage in adversarial tests · child-visibility disclosure mismatch with actual guardian visibility |
| **Model/router config rollouts** | Any language, persona-consistency, or calibration regression vs. current production; cost projection above ceiling |

## 22.3 Process

- **Three independent vetoes:** QA lead (quality gates), Safety owner (Identity/injection/crisis), Privacy owner (egress/permissions/deletion). A release requires all three sign-offs; any one veto kills it. No executive override exists — including the founder. (An override path would make every gate a negotiation; its absence makes them physics.)
- **Security-fix exception:** critical security patches may run an expedited pipeline — gates are *prioritized*, never skipped (P2 egress, Identity Lock, and permission audit always run).
- **Kill log:** every killed release is logged with the failed gates and time-to-green, reviewed monthly. Rising time-to-green = engineering health problem; rising override *requests* = culture problem. Both are leadership KPIs.
- **The staged-rollout brake:** shipping is not the end of the gate — 1% → 10% → 100% with §15 failure metrics watched at each step; automatic halt thresholds mirror the kill criteria in field data.

---

# Appendix — Series A Review Note

This document was reviewed as if presented to a Series A partner. The three hardest questions and where this document answers them:

1. **"Why won't Google kill you?"** — §16: our moat is what they *won't* do (P0 Uzbek, no-ads privacy, less-phone-time success metric), compounded by trust/memory switching costs that grow daily. Risk PR-13 stays on the register honestly.
2. **"Do the unit economics survive inference costs?"** — §10/§14: ≥70% on-device routing, $0.04→$0.025/DAU/day ceiling enforced *architecturally* by the router, generous-free viable because free users run mostly on their own silicon. The cost curve bends with hardware; our architecture rides it.
3. **"Is the Uzbek quality bet achievable?"** — PR-1 is rated High/Critical and funded accordingly (data program as a moat line-item, WER gates on the roadmap). This is the honest hinge of the company: if uz quality can't reach believability, the wedge narrows to ru/en where we're merely better, not unique. We stake the bet knowingly.

Verdict of the review: the document is investable as a foundation — vision distinct, market wedge defensible, MVP falsifiable, metrics honest enough to fail visibly. Its discipline (what we refuse to build) is its strongest page.

*End of Product Vision & Requirements Document v1.0.0.*
