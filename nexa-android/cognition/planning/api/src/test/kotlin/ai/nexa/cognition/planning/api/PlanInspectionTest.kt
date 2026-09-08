package ai.nexa.cognition.planning.api

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlanInspectionTest {
    @Test
    fun `dry run describes validated plan without invoking any action`() {
        var sideEffects = 0
        val action = ActionIntent.OpenApp("ai.nexa.app")
        val candidate = Plan(
            id = PlanId("plan-1"),
            version = PlanVersion.CURRENT,
            metadata = PlanMetadata(PlanOrigin.USER),
            nodes = listOf(
                PlanNode(
                    id = PlanNodeId("open"),
                    action = action,
                    declaredCapabilities = action.requiredCapabilities,
                ),
            ),
        )
        val validated = (PlanValidator().validate(candidate) as PlanValidationResult.Valid).plan

        val inspection = PlanInspector().inspect(validated)

        assertEquals(0, sideEffects)
        assertEquals(ProposedActionKind.OPEN_APP, inspection.proposedActions.single().kind)
        assertTrue(inspection.authorizationRequired)
        assertEquals(1, inspection.nodeCount)
    }
}
