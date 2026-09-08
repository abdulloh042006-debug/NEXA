package ai.nexa.core.ai.model

import java.net.URI

/** Stable provider identifier used by policy without exposing credentials or endpoints. */
@JvmInline
value class ModelProviderId(val value: String) {
    init {
        require(value.matches(IDENTIFIER_PATTERN)) {
            "provider id must contain only lowercase letters, digits, '.', '_' or '-'"
        }
    }

    private companion object {
        val IDENTIFIER_PATTERN = Regex("[a-z0-9][a-z0-9._-]*")
    }
}

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
    val providerId: ModelProviderId,
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
        require(id.matches(VERSIONED_ID_PATTERN)) {
            "manifest id must be a lowercase versioned id such as model-name@version"
        }
        require(capabilities.isNotEmpty()) { "manifest must declare at least one capability" }
        require(ModelCapability.TOOLS !in capabilities || ModelCapability.CHAT in capabilities) {
            "TOOLS capability requires CHAT capability"
        }
        require(contextWindow > 0) { "contextWindow must be > 0" }
        require(latencyP50Ms >= 0) { "latencyP50Ms must be >= 0" }
        require(languageScores.isNotEmpty()) { "languageScores must not be empty" }
        require(languageScores.values.all { it.isFinite() && it in 0.0..1.0 }) {
            "language scores must be finite and in [0, 1]"
        }
        require(maxRpmPerUser > 0) { "maxRpmPerUser must be > 0" }
        require((kind == ModelKind.LOCAL) == (localSpec != null)) {
            "localSpec must be present exactly when kind == LOCAL"
        }
        require(kind != ModelKind.CLOUD || privacyFloor != PrivacyClass.P2_SENSITIVE) {
            "cloud models can never be cleared for P2_SENSITIVE data"
        }
        require(kind == ModelKind.CLOUD || privacyFloor == PrivacyClass.P2_SENSITIVE) {
            "on-device models must preserve P2_SENSITIVE locality"
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
            require(inPerMtok.isFinite() && outPerMtok.isFinite()) { "cost must be finite" }
            require(inPerMtok >= 0 && outPerMtok >= 0) { "cost must be >= 0" }
        }

        companion object {
            val ZERO = Cost(inPerMtok = 0.0, outPerMtok = 0.0)
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
    ) {
        init {
            val uri = runCatching { URI(artifactUrl) }.getOrNull()
            require(uri?.scheme == "https" && uri.host != null) {
                "local artifact URL must use HTTPS"
            }
            require(uri.userInfo == null) { "local artifact URL must not contain credentials" }
            require(sha256.matches(SHA256_PATTERN)) { "sha256 must be 64 hexadecimal characters" }
            require(quantization.isNotBlank()) { "quantization must not be blank" }
            require(minRamMb > 0) { "minRamMb must be > 0" }
        }

        private companion object {
            val SHA256_PATTERN = Regex("[0-9a-fA-F]{64}")
        }
    }

    enum class LocalRuntime {
        LLAMA_CPP,
        ONNX,
    }

    private companion object {
        val VERSIONED_ID_PATTERN = Regex("[a-z0-9][a-z0-9._-]*@[a-z0-9][a-z0-9._-]*")
    }
}
