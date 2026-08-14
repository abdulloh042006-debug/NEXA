package ai.nexa.core.ai.model

/**
 * One vendor-neutral turn in a conversation submitted to a [ai.nexa.core.ai.port.ChatModelPort].
 *
 * Prompt assembly (system frames, dialect rendering, provenance wrapping) happens
 * in the prompt infrastructure (SPEC §12.4) — messages here are already-assembled content.
 */
data class ChatMessage(
    val role: Role,
    val content: String,
    /** Links a [Role.TOOL] result back to the [ChatDelta.ToolCall] that requested it. */
    val toolCallId: String? = null,
) {
    init {
        require(toolCallId == null || role == Role.TOOL) {
            "toolCallId is only valid on TOOL messages"
        }
    }

    enum class Role {
        SYSTEM,
        USER,
        ASSISTANT,
        TOOL,
    }
}
