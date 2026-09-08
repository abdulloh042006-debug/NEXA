package ai.nexa.core.ai.port

/** Expected runtime failures that may trigger an eligible router fallback. */
sealed class ModelInvocationException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class Unavailable : ModelInvocationException("model is unavailable")

    class ProviderFailure(cause: Throwable) : ModelInvocationException("model provider failed", cause)
}
