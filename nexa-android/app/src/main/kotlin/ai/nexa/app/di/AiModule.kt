package ai.nexa.app.di

import ai.nexa.app.BuildConfig
import ai.nexa.core.ai.model.Language
import ai.nexa.core.ai.model.ModelManifest
import ai.nexa.core.ai.model.ModelProviderId
import ai.nexa.core.ai.model.PrivacyClass
import ai.nexa.core.network.inference.GeminiGatewayClient
import ai.nexa.core.network.inference.createGeminiGatewayClient
import ai.nexa.router.DeterministicModelRouter
import ai.nexa.router.api.NetworkState
import ai.nexa.router.api.RouterPort
import ai.nexa.router.api.RoutingDeviceState
import ai.nexa.router.api.RoutingEnvironmentPort
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
    fun provideOfflineChatModel(): OfflineChatModelPort = OfflineChatModelPort(offlineManifest())

    @Provides
    @Singleton
    fun provideRouter(
        gemini: GeminiApiAdapter,
        offline: OfflineChatModelPort,
    ): RouterPort = DeterministicModelRouter(listOf(gemini, offline))

    @Provides
    @Singleton
    fun provideRoutingEnvironment(): RoutingEnvironmentPort = RoutingEnvironmentPort {
        // Phase 2 intentionally has no connectivity/model-runtime monitor. Unknown
        // state is represented conservatively instead of enabling a hidden fallback.
        RoutingDeviceState(
            network = NetworkState.UNAVAILABLE,
            availableRamMb = 0,
            availableOnDeviceModelIds = emptySet(),
        )
    }

    private fun cloudManifest() = baseManifest(
        id = "gemini@gateway",
        providerId = ModelProviderId("google"),
        kind = ModelManifest.ModelKind.CLOUD,
        privacyFloor = PrivacyClass.P1_PERSONAL,
    )

    private fun offlineManifest() = baseManifest(
        id = "phase1-unavailable@local",
        providerId = ModelProviderId("nexa-local"),
        kind = ModelManifest.ModelKind.LOCAL,
        privacyFloor = PrivacyClass.P2_SENSITIVE,
        localSpec = ModelManifest.LocalModelSpec(
            artifactUrl = "https://invalid.nexa.app/phase1-unavailable.onnx",
            sha256 = "0".repeat(SHA256_HEX_LENGTH),
            runtime = ModelManifest.LocalRuntime.ONNX,
            quantization = "none",
            minRamMb = 1,
        ),
    )

    private fun baseManifest(
        id: String,
        providerId: ModelProviderId,
        kind: ModelManifest.ModelKind,
        privacyFloor: PrivacyClass,
        localSpec: ModelManifest.LocalModelSpec? = null,
    ) = ModelManifest(
        id = id,
        providerId = providerId,
        kind = kind,
        capabilities = setOf(ModelManifest.ModelCapability.CHAT),
        contextWindow = 1,
        qualityTier = ModelManifest.QualityTier.NANO,
        cost = ModelManifest.Cost(0.0, 0.0),
        latencyP50Ms = 0,
        languageScores = Language.entries.associateWith { 0.0 },
        privacyFloor = privacyFloor,
        maxRpmPerUser = 1,
        localSpec = localSpec,
    )

    private const val SHA256_HEX_LENGTH = 64
}
