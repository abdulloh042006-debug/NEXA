package ai.nexa.router.api

/** Why a manifest failed the router's hard filters (ARCHITECTURE §11.2 step 1). */
enum class ExclusionReason {
    /** The model does not declare the CHAT capability. */
    MISSING_CHAT_CAPABILITY,

    /** The model does not declare the EMBEDDING capability. */
    MISSING_EMBEDDING_CAPABILITY,

    /** The request requires image understanding and the model does not declare it. */
    MISSING_VISION_CAPABILITY,

    /** The request offers tools but the model lacks the TOOLS capability. */
    TOOLS_UNSUPPORTED,

    /**
     * `ModelManifest.mayReceive(privacyClass)` is false — e.g. a P2_SENSITIVE
     * request can never reach a model cleared only for P1 (ARCHITECTURE §11.2:
     * a sensitive request can only reach local models).
     */
    PRIVACY_FLOOR_EXCEEDED,

    /** Estimated prompt tokens plus the output reserve exceed the model's context window. */
    CONTEXT_OVERFLOW,

    /** The measured eval score for the request's language hint is below the routing threshold. */
    LANGUAGE_BELOW_THRESHOLD,

    /** A cloud model cannot run without network connectivity. */
    NETWORK_UNAVAILABLE,

    /** Policy explicitly requires on-device execution. */
    OFFLINE_ONLY,

    /** Policy forbids cloud execution even while connectivity exists. */
    CLOUD_FORBIDDEN,

    /** A local/AICore manifest exists but its runtime or artifact is unavailable. */
    LOCAL_MODEL_UNAVAILABLE,

    /** Available memory is below the local manifest's declared minimum. */
    INSUFFICIENT_RAM,

    /** A caller-supplied explicit model allowlist excludes this candidate. */
    MODEL_RESTRICTED,

    /** A caller-supplied explicit provider allowlist excludes this candidate. */
    PROVIDER_RESTRICTED,

    /** The candidate exceeds a hard policy cost ceiling. */
    COST_LIMIT_EXCEEDED,
}
