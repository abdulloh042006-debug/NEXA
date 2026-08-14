package ai.nexa.core.ai.testing

import ai.nexa.core.ai.model.Language
import ai.nexa.core.ai.model.ModelManifest
import ai.nexa.core.ai.model.PrivacyClass

/** Canonical manifests for tests — local models per port capability plus a cloud chat variant. */
object FakeManifests {

    private val defaultLanguageScores =
        mapOf(Language.UZ to 0.9, Language.RU to 0.9, Language.EN to 0.9)

    fun chat(
        id: String = "fake-chat-3b@test",
        capabilities: Set<ModelManifest.ModelCapability> = setOf(
            ModelManifest.ModelCapability.CHAT,
            ModelManifest.ModelCapability.TOOLS,
        ),
        privacyFloor: PrivacyClass = PrivacyClass.P2_SENSITIVE,
        qualityTier: ModelManifest.QualityTier = ModelManifest.QualityTier.NANO,
        latencyP50Ms: Long = 50,
        contextWindow: Int = 8_192,
    ): ModelManifest = local(id, capabilities, privacyFloor, qualityTier, latencyP50Ms, contextWindow)

    /**
     * Cloud chat manifest for router tests: higher quality tier, dollar cost,
     * and — deliberately — no P2 clearance (a cloud model must never survive
     * the router's privacy hard filter for sensitive requests).
     */
    fun cloudChat(
        id: String = "fake-cloud-strong@test",
        capabilities: Set<ModelManifest.ModelCapability> = setOf(
            ModelManifest.ModelCapability.CHAT,
            ModelManifest.ModelCapability.TOOLS,
        ),
        privacyFloor: PrivacyClass = PrivacyClass.P1_PERSONAL,
        qualityTier: ModelManifest.QualityTier = ModelManifest.QualityTier.STRONG,
        latencyP50Ms: Long = 1_200,
        languageScores: Map<Language, Double> = defaultLanguageScores,
    ): ModelManifest = ModelManifest(
        id = id,
        kind = ModelManifest.ModelKind.CLOUD,
        capabilities = capabilities,
        contextWindow = 128_000,
        qualityTier = qualityTier,
        cost = ModelManifest.Cost(inPerMtok = 3.0, outPerMtok = 15.0),
        latencyP50Ms = latencyP50Ms,
        languageScores = languageScores,
        privacyFloor = privacyFloor,
        maxRpmPerUser = 60,
    )

    fun embedding(
        id: String = "fake-embed-mini@test",
    ): ModelManifest = local(id, setOf(ModelManifest.ModelCapability.EMBEDDING), PrivacyClass.P2_SENSITIVE)

    fun vision(
        id: String = "fake-vision-2b@test",
    ): ModelManifest = local(id, setOf(ModelManifest.ModelCapability.VISION), PrivacyClass.P2_SENSITIVE)

    private fun local(
        id: String,
        capabilities: Set<ModelManifest.ModelCapability>,
        privacyFloor: PrivacyClass,
        qualityTier: ModelManifest.QualityTier = ModelManifest.QualityTier.NANO,
        latencyP50Ms: Long = 50,
        contextWindow: Int = 8_192,
    ): ModelManifest = ModelManifest(
        id = id,
        kind = ModelManifest.ModelKind.LOCAL,
        capabilities = capabilities,
        contextWindow = contextWindow,
        qualityTier = qualityTier,
        cost = ModelManifest.Cost(inPerMtok = 0.0, outPerMtok = 0.0),
        latencyP50Ms = latencyP50Ms,
        languageScores = defaultLanguageScores,
        privacyFloor = privacyFloor,
        maxRpmPerUser = Int.MAX_VALUE,
        localSpec = ModelManifest.LocalModelSpec(
            artifactUrl = "https://cdn.test/models/$id.gguf",
            sha256 = "0".repeat(64),
            runtime = ModelManifest.LocalRuntime.LLAMA_CPP,
            quantization = "Q4_K_M",
            minRamMb = 2_048,
        ),
    )
}
