package ai.nexa.kernel.inference

import ai.nexa.router.api.ChatRouteRequest

@JvmInline
value class InferenceExecutionId(val value: String) {
    init {
        require(value.matches(ID_PATTERN)) { "execution id must be an opaque 1-64 character identifier" }
    }

    private companion object {
        val ID_PATTERN = Regex("[A-Za-z0-9._-]{1,64}")
    }
}

@JvmInline
value class InferenceCorrelationId(val value: String) {
    init {
        require(value.matches(ID_PATTERN)) { "correlation id must be an opaque 1-64 character identifier" }
    }

    private companion object {
        val ID_PATTERN = Regex("[A-Za-z0-9._-]{1,64}")
    }
}

/** A validated execution envelope around the router's existing structured request. */
data class InferenceExecutionRequest(
    val executionId: InferenceExecutionId,
    val correlationId: InferenceCorrelationId,
    val routeRequest: ChatRouteRequest,
    val timeoutMillis: Long = routeRequest.request.latencyBudget.budgetMs,
) {
    init {
        require(timeoutMillis > 0) { "timeoutMillis must be > 0" }
        require(timeoutMillis <= MAX_TIMEOUT_MILLIS) { "timeoutMillis exceeds the execution ceiling" }
    }

    private companion object {
        const val MAX_TIMEOUT_MILLIS = 300_000L
    }
}
