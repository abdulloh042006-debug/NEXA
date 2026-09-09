package ai.nexa.cognition.planning

import ai.nexa.cognition.planning.api.PermissionedPlanningPort
import ai.nexa.cognition.planning.api.PermissionedPlanningRequest
import ai.nexa.cognition.planning.api.PermissionedPlanningResult
import ai.nexa.cognition.planning.api.PlanAuthorizationPort
import ai.nexa.cognition.planning.api.PlanningPort
import ai.nexa.cognition.planning.api.PlanningResult
import javax.inject.Inject

class DefaultPermissionedPlanningPort @Inject constructor(
    private val planning: PlanningPort,
    private val authorization: PlanAuthorizationPort,
) : PermissionedPlanningPort {
    override suspend fun proposeAndAuthorize(request: PermissionedPlanningRequest): PermissionedPlanningResult =
        when (val result = planning.propose(request.planning)) {
            is PlanningResult.Valid -> PermissionedPlanningResult.Evaluated(
                result.plan,
                authorization.authorize(result.plan, request.authorizationContext),
            )
            else -> PermissionedPlanningResult.PlanningFailed(result)
        }
}
