package ai.nexa.core.ai.model

/**
 * One increment of a streamed generation (SPEC §12.2): token, tool-call, or
 * usage deltas. Streams are cold [kotlinx.coroutines.flow.Flow]s — collector
 * cancellation must propagate to the underlying runtime or wire call.
 */
sealed interface ChatDelta {

    /** A fragment of generated text, in emission order. */
    data class Token(val text: String) : ChatDelta

    /**
     * The model requests a tool invocation. [argumentsJson] is untrusted model
     * output — it enters the app only through schema validation with bounded
     * repair (SPEC §12.4, P7).
     */
    data class ToolCall(
        val id: String,
        val name: String,
        val argumentsJson: String,
    ) : ChatDelta

    /** Terminal accounting frame — emitted at most once, after all other deltas. */
    data class Usage(
        val inputTokens: Int,
        val outputTokens: Int,
    ) : ChatDelta
}
