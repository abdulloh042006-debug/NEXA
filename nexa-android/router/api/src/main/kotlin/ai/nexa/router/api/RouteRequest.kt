package ai.nexa.router.api

import ai.nexa.core.ai.model.ChatRequest
import ai.nexa.core.ai.model.EmbeddingPurpose
import ai.nexa.core.ai.model.ModelManifest

data class ChatRouteRequest(
    val request: ChatRequest,
    val taskClass: TaskClass = TaskClass.CHAT,
    val qualityNeed: ModelManifest.QualityTier = ModelManifest.QualityTier.FAST,
    val estimatedInputTokens: Int,
    val deviceState: RoutingDeviceState,
    val policy: RoutingPolicy = RoutingPolicy(),
) {
    init {
        require(estimatedInputTokens > 0) { "estimatedInputTokens must be > 0" }
    }

    val requiredCapabilities: Set<ModelManifest.ModelCapability>
        get() = buildSet {
            add(ModelManifest.ModelCapability.CHAT)
            if (request.toolSchemas.isNotEmpty()) add(ModelManifest.ModelCapability.TOOLS)
            if (taskClass == TaskClass.VISION_QA) add(ModelManifest.ModelCapability.VISION)
        }
}

data class EmbeddingRouteRequest(
    val purpose: EmbeddingPurpose,
    val deviceState: RoutingDeviceState,
    val policy: RoutingPolicy = RoutingPolicy(offlineOnly = true, cloudAllowed = false),
)

data class RoutingDeviceState(
    val network: NetworkState,
    val availableRamMb: Int,
    val availableOnDeviceModelIds: Set<String>,
) {
    init {
        require(availableRamMb >= 0) { "availableRamMb must be >= 0" }
        require(availableOnDeviceModelIds.none(String::isBlank)) {
            "available on-device model ids must not be blank"
        }
    }
}

enum class NetworkState { UNAVAILABLE, METERED, UNMETERED }

enum class TaskClass { CHAT, SUMMARIZE, EXTRACT, PLAN, CODE, TRANSLATE, VISION_QA }
