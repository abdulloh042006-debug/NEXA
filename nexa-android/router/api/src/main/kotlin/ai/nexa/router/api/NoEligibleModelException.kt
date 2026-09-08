package ai.nexa.router.api

/**
 * Failure of [RouterPort.streamChat] or [RouterPort.routeEmbedding] when no
 * registered model survives the hard filters (e.g. a P2_SENSITIVE request
 * while no local model is available). Carries the [decision] so the "why"
 * stays answerable at the failure site (ARCHITECTURE §11.4); adapters map this
 * to a DomainError at the module boundary (SPEC §17).
 */
class NoEligibleModelException(
    message: String,
    val decision: RouteDecision,
) : IllegalStateException(
    "$message; excluded: " + decision.excluded.entries.joinToString { "${it.key} -> ${it.value}" },
) {
    constructor(request: ChatRouteRequest, decision: RouteDecision) : this(
        "no model is eligible for privacyClass=${request.request.privacyClass}, " +
            "latencyBudget=${request.request.latencyBudget}",
        decision,
    )
}
