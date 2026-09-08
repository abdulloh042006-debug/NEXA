package ai.nexa.router.api

import ai.nexa.core.ai.model.ModelProviderId

/** Hard limits and soft scoring weights for one deterministic route. */
data class RoutingPolicy(
    val offlineOnly: Boolean = false,
    val cloudAllowed: Boolean = true,
    val minimumLanguageScore: Double = DEFAULT_MINIMUM_LANGUAGE_SCORE,
    val maximumAverageCostPerMtok: Double? = null,
    val allowedModelIds: Set<String> = emptySet(),
    val allowedProviderIds: Set<ModelProviderId> = emptySet(),
    val weights: RoutingWeights = RoutingWeights.BALANCED,
) {
    init {
        require(minimumLanguageScore.isFinite() && minimumLanguageScore in 0.0..1.0) {
            "minimumLanguageScore must be finite and in [0, 1]"
        }
        require(
            maximumAverageCostPerMtok == null ||
                (maximumAverageCostPerMtok.isFinite() && maximumAverageCostPerMtok >= 0),
        ) { "maximumAverageCostPerMtok must be finite and >= 0" }
        require(allowedModelIds.none(String::isBlank)) { "allowed model ids must not be blank" }
    }

    companion object {
        const val DEFAULT_MINIMUM_LANGUAGE_SCORE = 0.5
    }
}

/** Soft preferences cannot make a model bypass [RoutingPolicy]'s hard limits. */
data class RoutingWeights(
    val quality: Double,
    val cost: Double,
    val latency: Double,
) {
    init {
        require(listOf(quality, cost, latency).all { it.isFinite() && it >= 0 }) {
            "routing weights must be finite and >= 0"
        }
        require(quality + cost + latency > 0) { "at least one routing weight must be positive" }
    }

    companion object {
        val BALANCED = RoutingWeights(quality = 1.0, cost = 1.0, latency = 1.0)
        val COST_SENSITIVE = RoutingWeights(quality = 0.5, cost = 10.0, latency = 0.5)
        val LATENCY_SENSITIVE = RoutingWeights(quality = 0.5, cost = 1.0, latency = 5.0)
        val QUALITY_FIRST = RoutingWeights(quality = 2.0, cost = 0.25, latency = 0.25)
    }
}
