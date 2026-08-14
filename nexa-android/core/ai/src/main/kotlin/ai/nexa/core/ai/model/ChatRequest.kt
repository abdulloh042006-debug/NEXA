package ai.nexa.core.ai.model

/**
 * The vendor-neutral generation request (ARCHITECTURE §10.1, SPEC §12.1).
 *
 * Callers express intent only: there is deliberately no field to name a model or
 * vendor — placement is the router's decision (AF-04 works both directions).
 * [privacyClass] comes from data provenance and is set by code, never by a model.
 */
data class ChatRequest(
    val messages: List<ChatMessage>,
    val privacyClass: PrivacyClass,
    val latencyBudget: LatencyBudget,
    val toolSchemas: List<ToolSchema> = emptyList(),
    val sampling: SamplingParams = SamplingParams(),
    val languageHint: Language? = null,
    val contextBundle: ContextBundle = ContextBundle(),
) {
    init {
        require(messages.isNotEmpty()) { "a ChatRequest must carry at least one message" }
    }
}
