package ai.nexa.cognition.planning

import ai.nexa.cognition.planning.api.ActionIntent
import ai.nexa.cognition.planning.api.PermissionedPlanningRequest
import ai.nexa.cognition.planning.api.PermissionedPlanningResult
import ai.nexa.cognition.planning.api.Plan
import ai.nexa.cognition.planning.api.PlanAuthorizationResult
import ai.nexa.cognition.planning.api.PlanAuthorizationStatus
import ai.nexa.cognition.planning.api.PlanCorrelationId
import ai.nexa.cognition.planning.api.PlanId
import ai.nexa.cognition.planning.api.PlanMetadata
import ai.nexa.cognition.planning.api.PlanNode
import ai.nexa.cognition.planning.api.PlanNodeId
import ai.nexa.cognition.planning.api.PlanOrigin
import ai.nexa.cognition.planning.api.PlanValidationResult
import ai.nexa.cognition.planning.api.PlanValidator
import ai.nexa.cognition.planning.api.PlanVersion
import ai.nexa.cognition.planning.api.PlanningRequest
import ai.nexa.cognition.planning.api.PlanningResult
import ai.nexa.cognition.planning.api.ValidatedPlan
import ai.nexa.core.ai.model.PrivacyClass
import ai.nexa.core.permission.AuthorizationContext
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DefaultPermissionedPlanningPortTest {
    @Test
    fun `valid plan always crosses authorization port before return`() = runTest {
        val plan = validPlan()
        var authorized = false
        val port = DefaultPermissionedPlanningPort(
            planning = { PlanningResult.Valid(plan) },
            authorization = { candidate, _ ->
                authorized = true
                PlanAuthorizationResult(
                    candidate.plan.id,
                    PlanAuthorizationStatus.DENIED,
                    emptyList(),
                    emptyList(),
                    null,
                )
            },
        )

        val result = assertIs<PermissionedPlanningResult.Evaluated>(port.proposeAndAuthorize(request()))

        assertEquals(PlanAuthorizationStatus.DENIED, result.authorization.status)
        assertEquals(true, authorized)
    }

    @Test
    fun `planning failure cannot manufacture authorization`() = runTest {
        var authorized = false
        val port = DefaultPermissionedPlanningPort(
            planning = { PlanningResult.Cancelled },
            authorization = { _, _ ->
                authorized = true
                error("must not authorize")
            },
        )

        assertIs<PermissionedPlanningResult.PlanningFailed>(port.proposeAndAuthorize(request()))
        assertEquals(false, authorized)
    }

    private fun request() = PermissionedPlanningRequest(
        PlanningRequest(
            "open app",
            PlanCorrelationId("correlation-1"),
            PrivacyClass.P0_PUBLIC,
        ),
        AuthorizationContext("request-1", "session-1"),
    )

    private fun validPlan(): ValidatedPlan {
        val action = ActionIntent.OpenApp("ai.nexa.app")
        val plan = Plan(
            PlanId("plan-1"),
            PlanVersion.CURRENT,
            PlanMetadata(PlanOrigin.USER),
            listOf(
                PlanNode(
                    PlanNodeId("node-1"),
                    action,
                    declaredCapabilities = action.requiredCapabilities,
                ),
            ),
        )
        return (PlanValidator().validate(plan) as PlanValidationResult.Valid).plan
    }
}
