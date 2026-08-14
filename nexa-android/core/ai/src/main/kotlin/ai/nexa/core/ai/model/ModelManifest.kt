package ai.nexa.core.ai.model

/**
 * Declarative description of one model, local or cloud (ARCHITECTURE §10.2).
 *
 * Manifests are configuration delivered via signed remote config — the router
 * consumes only manifests, so adding a model never requires an app release
 * unless it needs a new runtime.
 */
data class ModelManifest(
    /** Stable versioned id, e.g. `"claude-sonnet-5@2026-05"` or `"qwen2.5-3b-q4@r2"`. */
    val id: String,
    val kind: ModelKind,
    val capabilities: Set<ModelCapability>,
    /** Context window in tokens. */
    val contextWindow: Int,
    val qualityTier: QualityTier,
    val cost: Cost,
    val latencyP50Ms: Long,
    /** Measured per-language eval scores in [0, 1] — not vendor claims. */
    val languageScores: Map<Language, Double>,
    /**
     * The most sensitive [PrivacyClass] this model is cleared to receive
     * (§10.2 `privacy_floor`). See [mayReceive].
     */
    val privacyFloor: PrivacyClass,
    val maxRpmPerUser: Int,
    /** Present iff [kind] is [ModelKind.LOCAL] — artifact and device requirements. */
    val localSpec: LocalModelSpec? = null,
) {
    init {
        require(id.isNotBlank()) { "manifest id must not be blank" }
        require(contextWindow > 0) { "contextWindow must be > 0" }
        require((kind == ModelKind.LOCAL) == (localSpec != null)) {
            "localSpec must be present exactly when kind == LOCAL"
        }
    }

    /**
     * Router hard filter (ARCHITECTURE §11.2): a request may reach this model
     * only if its privacy class does not exceed the model's clearance.
     */
    fun mayReceive(privacyClass: PrivacyClass): Boolean =
        privacyClass.ordinal <= privacyFloor.ordinal

    enum class ModelKind {
        CLOUD,
        LOCAL,

        /** System-managed on-device model (AICore / Gemini Nano) — use when present, never depend on it. */
        AICORE,
    }

    enum class ModelCapability {
        CHAT,
        TOOLS,
        VISION,
        EMBEDDING,
    }

    /** Quality tiers as routed by quality_need (ARCHITECTURE §11.1). */
    enum class QualityTier {
        FRONTIER,
        STRONG,
        FAST,
        NANO,
    }

    /** USD per million tokens; zero for local models (their cost is battery, not dollars). */
    data class Cost(
        val inPerMtok: Double,
        val outPerMtok: Double,
    ) {
        init {
            require(inPerMtok >= 0 && outPerMtok >= 0) { "cost must be >= 0" }
        }
    }

    /** Local-model additions (ARCHITECTURE §10.2): artifact, runtime, and device requirements. */
    data class LocalModelSpec(
        val artifactUrl: String,
        /** Hex SHA-256 of the artifact — verified before load (SPEC §10 model integrity). */
        val sha256: String,
        val runtime: LocalRuntime,
        /** Quantization label, e.g. `"Q4_K_M"`. */
        val quantization: String,
        val minRamMb: Int,
    )

    enum class LocalRuntime {
        LLAMA_CPP,
        ONNX,
        AICORE,
    }
}
