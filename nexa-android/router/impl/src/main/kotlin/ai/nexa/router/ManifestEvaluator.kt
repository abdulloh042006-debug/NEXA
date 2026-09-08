package ai.nexa.router

import ai.nexa.core.ai.model.Language
import ai.nexa.core.ai.model.LatencyBudget
import ai.nexa.core.ai.model.ModelManifest
import ai.nexa.core.ai.model.PrivacyClass
import ai.nexa.router.api.ExclusionReason
import ai.nexa.router.api.NetworkState
import ai.nexa.router.api.RoutingDeviceState
import ai.nexa.router.api.RoutingPolicy

internal class ManifestEvaluator {
    fun evaluate(manifest: ModelManifest, context: RouteContext): Candidate {
        val exclusions = capabilityExclusions(manifest, context) +
            compatibilityExclusions(manifest, context) +
            policyExclusions(manifest, context) +
            placementExclusions(manifest, context)
        return Candidate(
            manifest = manifest,
            exclusions = exclusions,
            score = if (exclusions.isEmpty()) score(manifest, context) else null,
        )
    }

    private fun capabilityExclusions(
        manifest: ModelManifest,
        context: RouteContext,
    ): List<ExclusionReason> = buildList {
        context.requiredCapabilities.forEach { capability ->
            if (capability !in manifest.capabilities) add(capability.missingReason())
        }
    }

    private fun compatibilityExclusions(
        manifest: ModelManifest,
        context: RouteContext,
    ): List<ExclusionReason> = buildList {
        if (!manifest.mayReceive(context.privacyClass)) add(ExclusionReason.PRIVACY_FLOOR_EXCEEDED)
        if (context.contextTokens > manifest.contextWindow) add(ExclusionReason.CONTEXT_OVERFLOW)
        val language = context.language
        if (language != null && manifest.scoreFor(language) < context.policy.minimumLanguageScore) {
            add(ExclusionReason.LANGUAGE_BELOW_THRESHOLD)
        }
    }

    private fun policyExclusions(
        manifest: ModelManifest,
        context: RouteContext,
    ): List<ExclusionReason> = buildList {
        val policy = context.policy
        if (policy.allowedModelIds.isNotEmpty() && manifest.id !in policy.allowedModelIds) {
            add(ExclusionReason.MODEL_RESTRICTED)
        }
        if (policy.allowedProviderIds.isNotEmpty() && manifest.providerId !in policy.allowedProviderIds) {
            add(ExclusionReason.PROVIDER_RESTRICTED)
        }
        val maximumCost = policy.maximumAverageCostPerMtok
        if (maximumCost != null && manifest.cost.averagePerMtok() > maximumCost) {
            add(ExclusionReason.COST_LIMIT_EXCEEDED)
        }
    }

    private fun placementExclusions(
        manifest: ModelManifest,
        context: RouteContext,
    ): List<ExclusionReason> = buildList {
        when (manifest.kind) {
            ModelManifest.ModelKind.CLOUD -> addCloudExclusions(context)
            ModelManifest.ModelKind.LOCAL,
            ModelManifest.ModelKind.AICORE,
            -> addOnDeviceExclusions(manifest, context.deviceState)
        }
    }

    private fun MutableList<ExclusionReason>.addCloudExclusions(context: RouteContext) {
        if (context.deviceState.network == NetworkState.UNAVAILABLE) add(ExclusionReason.NETWORK_UNAVAILABLE)
        if (context.policy.offlineOnly) add(ExclusionReason.OFFLINE_ONLY)
        if (!context.policy.cloudAllowed) add(ExclusionReason.CLOUD_FORBIDDEN)
    }

    private fun MutableList<ExclusionReason>.addOnDeviceExclusions(
        manifest: ModelManifest,
        deviceState: RoutingDeviceState,
    ) {
        if (manifest.id !in deviceState.availableOnDeviceModelIds) {
            add(ExclusionReason.LOCAL_MODEL_UNAVAILABLE)
        }
        val minimumRam = manifest.localSpec?.minRamMb ?: 0
        if (minimumRam > deviceState.availableRamMb) add(ExclusionReason.INSUFFICIENT_RAM)
    }

    private fun score(manifest: ModelManifest, context: RouteContext): Double {
        val tierScore = manifest.qualityTier.score
        val qualityGap = (context.qualityNeed.score - tierScore).coerceAtLeast(0.0)
        val languageScore = context.language?.let { manifest.scoreFor(it) } ?: 1.0
        val qualityComponent = (tierScore * QUALITY_TIER_SCALE) +
            (languageScore * LANGUAGE_SCORE_SCALE) -
            (qualityGap * QUALITY_GAP_PENALTY)
        val costComponent = manifest.cost.averagePerMtok()
        val latencyComponent = manifest.latencyP50Ms.toDouble() /
            context.latencyBudget.budgetMs * LATENCY_RATIO_SCALE
        return context.policy.weights.quality * qualityComponent -
            context.policy.weights.cost * costComponent -
            context.policy.weights.latency * latencyComponent
    }

    private companion object {
        const val QUALITY_TIER_SCALE = 100.0
        const val LANGUAGE_SCORE_SCALE = 10.0
        const val QUALITY_GAP_PENALTY = 25.0
        const val LATENCY_RATIO_SCALE = 10.0
    }
}

internal data class Candidate(
    val manifest: ModelManifest,
    val exclusions: List<ExclusionReason>,
    val score: Double?,
)

internal data class RouteContext(
    val requiredCapabilities: Set<ModelManifest.ModelCapability>,
    val privacyClass: PrivacyClass,
    val contextTokens: Int,
    val language: Language?,
    val latencyBudget: LatencyBudget,
    val qualityNeed: ModelManifest.QualityTier,
    val deviceState: RoutingDeviceState,
    val policy: RoutingPolicy,
)

private fun ModelManifest.scoreFor(language: Language): Double =
    languageScores[language] ?: if (language == Language.MIXED) {
        listOf(Language.UZ, Language.RU, Language.EN).mapNotNull(languageScores::get).minOrNull() ?: 0.0
    } else {
        0.0
    }

private fun ModelManifest.Cost.averagePerMtok(): Double = (inPerMtok + outPerMtok) / 2.0

private val ModelManifest.QualityTier.score: Double
    get() = when (this) {
        ModelManifest.QualityTier.NANO -> 0.25
        ModelManifest.QualityTier.FAST -> 0.5
        ModelManifest.QualityTier.STRONG -> 0.75
        ModelManifest.QualityTier.FRONTIER -> 1.0
    }

private fun ModelManifest.ModelCapability.missingReason(): ExclusionReason = when (this) {
    ModelManifest.ModelCapability.CHAT -> ExclusionReason.MISSING_CHAT_CAPABILITY
    ModelManifest.ModelCapability.TOOLS -> ExclusionReason.TOOLS_UNSUPPORTED
    ModelManifest.ModelCapability.VISION -> ExclusionReason.MISSING_VISION_CAPABILITY
    ModelManifest.ModelCapability.EMBEDDING -> ExclusionReason.MISSING_EMBEDDING_CAPABILITY
}
