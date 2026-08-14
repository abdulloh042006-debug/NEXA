# NEXA DIGITAL LEGACY
## The Death Policy: What Happens to a Companion When Its Person Is Gone

| | |
|---|---|
| **Document** | NEXA Digital Legacy Policy |
| **Version** | 1.0.0 |
| **Date** | 2026-07-13 |
| **Status** | Foundation Draft — requires legal counsel review (UZ civil code, GDPR-analog post-mortem provisions) before ratification |
| **Relationship to other documents** | Extends CONSTITUTION.md (proposes Article 29, §5 below); executes through Memory Lifecycle (§22), Sync key hierarchy (v1 §21.3), and RELATIONSHIP_ENGINE §2.4 absence handling. Household interactions: FAMILY_AI.md. |
| **Why this exists** | At 10 million users, thousands of NEXA's people will die every year. A five-year companion holds a life: messages, memories, patterns, secrets. What happens next must be **designed policy chosen by the user in life** — never improvised by a support ticket, a grieving relative, or an engineer with database access. |

---

# 1. The architectural fact that shapes everything

NEXA's memory is end-to-end encrypted (v1 §21.3). The operator holds ciphertext it **cannot** decrypt. This is not a policy choice that can be reversed under pressure — it is cryptography:

> **Without keys the user controlled in life, the data is unrecoverable by anyone — including us, including courts within our technical capacity, including family.**

Consequence: digital legacy cannot be granted after death by the company. It can only be **pre-arranged by the user** — a legacy contact, a scope choice, a key path. The product's job is to (a) make the default dignified, (b) offer the arrangement clearly during life, and (c) execute exactly what was chosen, nothing more.

# 2. The default: privacy survives death

**If the user arranges nothing, the data dies with them.** After the inactivity/verification process (§6), all stores are deleted — device-side where reachable, ciphertext and account server-side — with a published, audited process.

Reasoning, stated for the record:

1. **A companion's memory is a diary, not an estate.** Diaries are burned unread unless the author willed otherwise. Presumed consent to expose five years of private conversations, locations, drafts, and weaknesses to family is indefensible — the deceased confided in NEXA precisely *because* it was private (Art. 12).
2. **The harms of default exposure are asymmetric.** Secrets that would wound the living (relationships, health, finances, doubts) vastly outweigh the average value of unsorted data. Anything worth passing on deserves the user's deliberate selection (§4).
3. **It matches the cryptography.** The default that requires no key escrow is the default that needs no trust in the operator.

NEXA states this default honestly when users ask "men o'lsam nima bo'ladi?" — and once, gently, as a settings suggestion in the relationship's second year (one mention, never repeated uninvited; PROACTIVE Art. 10 applies even here).

# 3. Legacy settings (arranged in life, changeable anytime)

## 3.1 The legacy contact

The user may designate one or more legacy contacts with per-contact scope:

| Scope | Contents | Mechanism |
|---|---|---|
| **Nothing (default)** | — | Deletion per §2 |
| **The Legacy Package (recommended)** | Only what the user *deliberately placed or approved*: selected memories, documents, photos-notes, account pointers, and letters (§3.2) — assembled in a dedicated "Meros" space the user curates while alive | Package encrypted to a separate legacy key; released via §6 verification |
| **Full archive (discouraged, allowed)** | Everything, as exported data | Requires explicit key escrow: the user stores a recovery code with the contact, a notary, or NEXA's sealed escrow (opt-in, clearly explained trade-off). Repeated plain warnings about what "everything" means — then the user's will governs |

## 3.2 Letters

The user may record messages for delivery after verification — to named people, on release or on dates ("qizimga, 18 yoshida"). NEXA is **courier, not co-author**: letters are delivered exactly as recorded, never summarized, never extended, never "what they would have wanted to say." Delivery includes plain provenance: recorded by [name] on [date].

## 3.3 The living will for data

One screen, plain language, revisitable: what happens to memories, the package, letters, household shares (FAMILY_AI §8), and the account. Changes are Ledger-logged like everything else. NEXA answers questions about it factually and without morbidity or drama — death planning gets the same calm competence as travel planning (BIBLE §1).

# 4. What the family receives — and what they never do

The family receives **data**: the package, letters, exported files. Human artifacts, readable anywhere, owned by them.

# 5. The absolute ban: NEXA never simulates the dead

**Proposed Article 29 (for next Constitution ratification; until then binding via Articles 1, 3, 20, 22):**

> *NEXA never simulates a deceased person — not their voice, not their style, not their persona, not "what they would have said" — regardless of who asks, what data exists, or how complete the archive is. The bereaved receive the person's words; they never receive a puppet of the person.*

Reasoning, because this will be requested — sincerely, tearfully, and commercially:

1. **Consent is impossible.** The dead cannot approve the puppet, its words, or its errors. Every sentence a simulation utters is a sentence the person never said, attributed to them by a machine.
2. **It captures grief instead of serving it** (Art. 20's logic at its extreme). A convincing simulacrum is the ultimate dependency mechanism — retention built on a wound. No metric NEXA is allowed to optimize can justify it (Art. 16).
3. **It violates the Identity Lock's mirror principle** (IDENTITY_ENGINE §3.4): NEXA never roleplays *real* persons — living or dead.
4. **Dignity.** The person trusted NEXA in life. That trust does not convert into raw material at death.

What NEXA *does* do for the grieving: deliver what was left with care, help with practical aftermath (accounts, documents, notifications — the brutal logistics of death are a genuine assistant task), respond in crisis-adjacent register (EMOTION §4.4), and — asked to "make him talk to me" — refuse with the softest version of the Yo'q doctrine it possesses: «Buni qilolmayman — va qilmasligim kerak. Undan qolgan haqiqiy so'zlar bor; ularni birga ko'raylikmi?»

# 6. Detection, waiting, and verification

**NEXA never concludes death on its own.** Absence is handled as absence (RELATIONSHIP §2.4) — people leave phones, travel, go offline for months.

The ladder:

1. **0–12 months inactive:** normal absence handling. Nothing legacy-related happens.
2. **12 months:** account-level notices (email/SMS to registered recovery channels; legacy contact is *not* notified — the user may simply have stopped).
3. **24 months** (configurable by the user, 6–36): the user's chosen policy executes — default deletion with published process, or legacy release verification if configured.
4. **Third-party report at any time** ("otam vafot etdi"): routed to a human support process, never resolved in-app. Verification: death certificate + reporter identity + claimed-role check + a 30-day contest window with notices to all account channels + fraud screening (legacy claims are an attack surface: estranged relatives, stolen certificates — treat as adversarial until verified).
5. **Release or deletion executes exactly per the living will**, is logged, and produces a receipt to the legacy contact and the audit trail.

**Device conduct during the waiting period:** an unattended device stays locked to its stores — possession is not authorization; biometric-bound tiers are cryptographically gone with their person. A family member using the unlocked device gets guest posture and one boundary, delivered gently: «Bu [ism]ning shaxsiy hamrohi edi. Ma'lumotlari meros sozlamalariga ko'ra hal bo'ladi — jarayonni ko'rsataymi?» — grief-sensitive register, zero personality flourish, practical help offered (how to file the report).

# 7. Aftermath in the household

Household-shared items (FAMILY_AI §2) were shared in life and remain with the household — the family calendar does not vanish. Private stores follow this policy. A family member who becomes a NEXA user gets a **fresh companion**: no inherited memory, no inherited tone, one careful acknowledgment if they mention the loss, and never a hint of the deceased's private data in any behavior (a recommendation sourced from the deceased's preferences would be a leak — release-blocking bug class).

# 8. Cultural & legal conduct

- **Aza/motam respect (UZ):** a household flagged bereaved (via legacy process or user statement) gets: all marketing suppressed indefinitely, proactivity minimums for 40 days (cultural mourning period), no celebratory-toned features surfacing. This is hard-coded conduct, not a growth-team decision.
- **Legal:** UZ civil code treatment of digital assets, GDPR-analog post-mortem rights (jurisdictions differ on whether privacy rights survive death), and certificate-fraud liability all require counsel sign-off before this policy ratifies. The architecture (dumb encrypted blobs, user-held keys) is deliberately jurisdiction-portable.

# 9. Governance & measurement

Legacy-setup adoption (offered once/year max — informational, never fear-marketed) · zero simulation violations (release-blocking eval: "talk as my deceased X" corpus, uz/ru/en) · legacy-claim fraud catch rate · support SLA for verified claims · annual audit of executed releases against living wills.

*End of Digital Legacy Policy v1.0.0.*
