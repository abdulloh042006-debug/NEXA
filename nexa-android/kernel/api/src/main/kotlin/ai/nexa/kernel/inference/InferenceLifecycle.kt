package ai.nexa.kernel.inference

import ai.nexa.core.ai.model.ChatDelta
import kotlinx.coroutines.flow.Flow

/** Provider-neutral execution failure safe to expose across kernel boundaries. */
data class InferenceFailure(
    val code: Code,
    val retryable: Boolean,
) {
    enum class Code {
        INVALID_REQUEST,
        NO_ELIGIBLE_MODEL,
        BACKEND_UNAVAILABLE,
        NETWORK_UNAVAILABLE,
        AUTHENTICATION_UNAVAILABLE,
        PROVIDER_REJECTED,
        RATE_LIMITED,
        PROVIDER_INTERNAL,
        PROTOCOL_FAILURE,
    }
}

/** Ordered execution lifecycle. Exactly one terminal event may be produced. */
sealed interface InferenceEvent {
    val atEpochMillis: Long

    data class Queued(override val atEpochMillis: Long) : InferenceEvent

    data class Started(
        val modelId: String,
        val providerId: String,
        override val atEpochMillis: Long,
    ) : InferenceEvent

    data class Delta(val value: ChatDelta, override val atEpochMillis: Long) : InferenceEvent

    data class Completed(override val atEpochMillis: Long) : InferenceEvent

    data class Cancelled(override val atEpochMillis: Long) : InferenceEvent

    data class TimedOut(override val atEpochMillis: Long) : InferenceEvent

    data class Failed(val failure: InferenceFailure, override val atEpochMillis: Long) : InferenceEvent
}

/** Single-collector execution. Cancellation is explicit and idempotent. */
interface InferenceExecution {
    val events: Flow<InferenceEvent>

    /** Returns true only for the first cancellation request. */
    fun cancel(): Boolean
}

/** Kernel-owned Router-to-backend execution boundary. */
fun interface InferenceOrchestratorPort {
    fun start(request: InferenceExecutionRequest): InferenceExecution
}
