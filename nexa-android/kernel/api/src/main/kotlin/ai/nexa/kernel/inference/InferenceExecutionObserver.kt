package ai.nexa.kernel.inference

/** Privacy-safe lifecycle record. It never includes request or generated content. */
data class InferenceExecutionRecord(
    val executionId: InferenceExecutionId,
    val state: State,
    val modelId: String? = null,
    val providerId: String? = null,
    val failureCode: InferenceFailure.Code? = null,
) {
    enum class State { QUEUED, STARTED, STREAMING, COMPLETED, CANCELLED, TIMED_OUT, FAILED }
}

fun interface InferenceExecutionObserver {
    fun onEvent(record: InferenceExecutionRecord)

    companion object {
        val NONE = InferenceExecutionObserver { }
    }
}
