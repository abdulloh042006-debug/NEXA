package ai.nexa.cognition.planning.api

import ai.nexa.core.permission.AuthorizationContext

data class PermissionedPlanningRequest(
    val planning: PlanningRequest,
    val authorizationContext: AuthorizationContext,
)

sealed interface PermissionedPlanningResult {
    data class Evaluated(
        val plan: ValidatedPlan,
        val authorization: PlanAuthorizationResult,
    ) : PermissionedPlanningResult

    data class PlanningFailed(val result: PlanningResult) : PermissionedPlanningResult {
        init {
            require(result !is PlanningResult.Valid) { "a valid plan must pass through capability authorization" }
        }
    }
}

/** Kernel-facing secure flow that ends at an authorization decision and performs no action. */
fun interface PermissionedPlanningPort {
    suspend fun proposeAndAuthorize(request: PermissionedPlanningRequest): PermissionedPlanningResult
}
