package ai.nexa.cognition.planning.api

import ai.nexa.core.permission.CapabilityRequirement
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PlanValidatorTest {
    private val validator = PlanValidator()

    @Test
    fun `linear parallel and diamond plans have deterministic topological order`() {
        val plan = plan(
            node("finish", "left", "right"),
            node("right", "root"),
            node("left", "root"),
            node("independent"),
            node("root"),
        )

        val result = assertIs<PlanValidationResult.Valid>(validator.validate(plan))

        assertEquals(
            listOf("independent", "root", "left", "right", "finish"),
            result.plan.orderedNodes.map { it.id.value },
        )
    }

    @Test
    fun `duplicate missing self and cyclic dependencies fail closed`() {
        val cases = listOf(
            plan(node("same"), node("same")) to ValidationCode.DUPLICATE_NODE_ID,
            plan(node("a", "missing")) to ValidationCode.MISSING_DEPENDENCY,
            plan(node("self", "self")) to ValidationCode.SELF_DEPENDENCY,
            plan(node("a", "b"), node("b", "a")) to ValidationCode.CYCLIC_GRAPH,
            plan(node("a", "root", "root"), node("root")) to ValidationCode.DUPLICATE_DEPENDENCY,
        )

        cases.forEach { (candidate, expected) ->
            assertTrue(expected in invalidCodes(candidate))
        }
    }

    @Test
    fun `empty oversized and excessive fan-in plans fail closed`() {
        assertTrue(ValidationCode.EMPTY_PLAN in invalidCodes(plan()))
        val oversized = (0..PlanLimits.MAX_NODES).map { node("node-$it") }
        assertTrue(ValidationCode.TOO_MANY_NODES in invalidCodes(plan(*oversized.toTypedArray())))
        val roots = (0..PlanLimits.MAX_DEPENDENCIES_PER_NODE).map { node("root-$it") }
        val target = node("target", *roots.map { it.id.value }.toTypedArray())
        assertTrue(
            ValidationCode.TOO_MANY_DEPENDENCIES in invalidCodes(plan(*(roots + target).toTypedArray())),
        )
    }

    @Test
    fun `unsupported schema and capability mismatch fail closed`() {
        val unsupported = Plan(
            PlanId("plan-1"),
            PlanVersion(2),
            PlanMetadata(PlanOrigin.USER),
            listOf(node("a")),
        )
        assertTrue(ValidationCode.UNSUPPORTED_SCHEMA in invalidCodes(unsupported))

        val action = ActionIntent.OpenApp("ai.nexa.app")
        val missing = PlanNode(PlanNodeId("a"), action, declaredCapabilities = emptyList())
        assertTrue(ValidationCode.MISSING_CAPABILITY in invalidCodes(plan(missing)))

        val wrong = PlanNode(
            PlanNodeId("a"),
            action,
            declaredCapabilities = listOf(
                CapabilityRequirement(CapabilityRequirement.Capability.MESSAGE_DRAFT),
            ),
        )
        assertTrue(ValidationCode.CAPABILITY_MISMATCH in invalidCodes(plan(wrong)))
    }

    @Test
    fun `validated plan is isolated from mutable proposal collections`() {
        val dependencies = mutableListOf<PlanNodeId>()
        val action = ActionIntent.OpenApp("ai.nexa.app")
        val nodes = mutableListOf(
            PlanNode(PlanNodeId("a"), action, dependencies, action.requiredCapabilities),
        )
        val candidate = Plan(
            PlanId("plan-1"),
            PlanVersion.CURRENT,
            PlanMetadata(PlanOrigin.USER),
            nodes,
        )
        val validated = assertIs<PlanValidationResult.Valid>(validator.validate(candidate)).plan

        dependencies += PlanNodeId("later")
        nodes.clear()

        assertEquals(listOf("a"), validated.orderedNodes.map { it.id.value })
        assertTrue(validated.orderedNodes.single().dependencies.isEmpty())
    }

    private fun invalidCodes(plan: Plan): Set<ValidationCode> =
        assertIs<PlanValidationResult.Invalid>(validator.validate(plan)).issues.mapTo(mutableSetOf()) { it.code }

    private fun plan(vararg nodes: PlanNode) = Plan(
        PlanId("plan-1"),
        PlanVersion.CURRENT,
        PlanMetadata(PlanOrigin.USER, PlanCorrelationId("correlation-1")),
        nodes.toList(),
    )

    private fun node(id: String, vararg dependencies: String): PlanNode {
        val action = ActionIntent.OpenApp("ai.nexa.app")
        return PlanNode(
            id = PlanNodeId(id),
            action = action,
            dependencies = dependencies.map(::PlanNodeId),
            declaredCapabilities = action.requiredCapabilities,
        )
    }
}
