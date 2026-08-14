package ai.nexa.core.ai.model

/**
 * The grounding context assembled by the kernel's GROUND stage (SPEC §12.4,
 * ARCHITECTURE_V2 §6.1): memory recalls + situation snapshot + identity frame.
 *
 * The prompt assembler renders this per model dialect under a deterministic
 * token budget — features never concatenate context into message strings.
 */
data class ContextBundle(
    /** Recalled memory snippets, ordered by relevance (highest first). */
    val memoryRecalls: List<String> = emptyList(),
    /** Current-situation summary from the Context Engine (time, place, activity). */
    val situationSnapshot: String? = null,
    /** Identity/constitution frame compiled by the Identity Engine (ARCHITECTURE_V2 §16.2). */
    val identityFrame: String? = null,
)
