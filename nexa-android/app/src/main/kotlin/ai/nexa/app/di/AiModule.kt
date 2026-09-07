package ai.nexa.app.di

import ai.nexa.app.BuildConfig
import ai.nexa.core.ai.model.Language
import ai.nexa.core.ai.model.ModelManifest
import ai.nexa.core.ai.model.PrivacyClass
import ai.nexa.core.ai.port.ChatModelPort
import ai.nexa.core.network.inference.GeminiGatewayClient
import ai.nexa.core.network.inference.createGeminiGatewayClient
import ai.nexa.router.gemini.GeminiApiAdapter
import ai.nexa.router.offline.OfflineChatModelPort
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AiModule {
    @Provides
    @Singleton
    fun provideGatewayClient(): GeminiGatewayClient = createGeminiGatewayClient(
        baseUrl = BuildConfig.NEXA_INFERENCE_BASE_URL,
        sessionTokenProvider = { null },
    )

    @Provides
    @Singleton
    fun provideGeminiAdapter(client: GeminiGatewayClient): GeminiApiAdapter =
        GeminiApiAdapter(cloudManifest(), client)

    @Provides
    @Singleton
    fun provideChatModel(): ChatModelPort = OfflineChatModelPort(offlineManifest())

    private fun cloudManifest() = baseManifest(
        id = "gemini@gateway",
        kind = ModelManifest.ModelKind.CLOUD,
        privacyFloor = PrivacyClass.P1_PERSONAL,
    )

    private fun offlineManifest() = baseManifest(
        id = "phase1-unavailable@local",
        kind = ModelManifest.ModelKind.LOCAL,
        privacyFloor = PrivacyClass.P2_SENSITIVE,
        localSpec = ModelManifest.LocalModelSpec(
            artifactUrl = "unavailable://phase1",
            sha256 = "0".repeat(SHA256_HEX_LENGTH),
            runtime = ModelManifest.LocalRuntime.ONNX,
            quantization = "none",
            minRamMb = 0,
        ),
    )

    private fun baseManifest(
        id: String,
        kind: ModelManifest.ModelKind,
        privacyFloor: PrivacyClass,
        localSpec: ModelManifest.LocalModelSpec? = null,
    ) = ModelManifest(
        id = id,
        kind = kind,
        capabilities = setOf(ModelManifest.ModelCapability.CHAT),
        contextWindow = 1,
        qualityTier = ModelManifest.QualityTier.NANO,
        cost = ModelManifest.Cost(0.0, 0.0),
        latencyP50Ms = 0,
        languageScores = Language.entries.associateWith { 0.0 },
        privacyFloor = privacyFloor,
        maxRpmPerUser = 0,
        localSpec = localSpec,
    )

    private const val SHA256_HEX_LENGTH = 64
}
