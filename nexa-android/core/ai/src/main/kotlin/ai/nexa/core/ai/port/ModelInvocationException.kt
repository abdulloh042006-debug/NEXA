package ai.nexa.core.ai.port

/** Sanitized adapter failures. Raw provider bodies never cross this boundary. */
sealed class ModelInvocationException(
    message: String,
    val retryable: Boolean,
    cause: Throwable? = null,
) : Exception(message, cause) {
    class Unavailable : ModelInvocationException("model is unavailable", retryable = true)

    class NetworkUnavailable(cause: Throwable? = null) :
        ModelInvocationException("network is unavailable", retryable = true, cause)

    class AuthenticationUnavailable :
        ModelInvocationException("model authentication is unavailable", retryable = false)

    class Rejected : ModelInvocationException("model rejected the request", retryable = false)

    class RateLimited : ModelInvocationException("model rate limit reached", retryable = true)

    class ProtocolFailure(cause: Throwable? = null) :
        ModelInvocationException("invalid model stream", retryable = false, cause)

    class ProviderFailure(cause: Throwable) :
        ModelInvocationException("model provider failed", retryable = true, cause)
}
