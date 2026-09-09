package ai.nexa.cognition.planning.api

import java.util.PriorityQueue

enum class ValidationCode {
    UNSUPPORTED_SCHEMA,
    EMPTY_PLAN,
    TOO_MANY_NODES,
    DUPLICATE_NODE_ID,
    MISSING_DEPENDENCY,
    SELF_DEPENDENCY,
    DUPLICATE_DEPENDENCY,
    TOO_MANY_DEPENDENCIES,
    CYCLIC_GRAPH,
    MISSING_CAPABILITY,
    CAPABILITY_MISMATCH,
    DUPLICATE_CAPABILITY,
}

enum class ValidationSeverity { ERROR }

data class ValidationIssue(
    val code: ValidationCode,
    val severity: ValidationSeverity = ValidationSeverity.ERROR,
    val nodeId: PlanNodeId? = null,
)

sealed interface PlanValidationResult {
    data class Valid(val plan: ValidatedPlan) : PlanValidationResult

    data class Invalid(val issues: List<ValidationIssue>) : PlanValidationResult
}

object PlanLimits {
    const val MAX_NODES = 64
    const val MAX_DEPENDENCIES_PER_NODE = 16
}

/** Deterministic, fail-closed authority between proposed plan data and future authorization. */
class PlanValidator {
    fun validate(plan: Plan): PlanValidationResult {
        val issues = mutableListOf<ValidationIssue>()
        if (plan.version != PlanVersion.CURRENT) issues += issue(ValidationCode.UNSUPPORTED_SCHEMA)
        if (plan.nodes.isEmpty()) issues += issue(ValidationCode.EMPTY_PLAN)
        if (plan.nodes.size > PlanLimits.MAX_NODES) issues += issue(ValidationCode.TOO_MANY_NODES)

        val nodesById = linkedMapOf<PlanNodeId, PlanNode>()
        plan.nodes.forEach { node ->
            if (nodesById.putIfAbsent(node.id, node) != null) {
                issues += issue(ValidationCode.DUPLICATE_NODE_ID, node.id)
            }
        }
        plan.nodes.forEach { node -> validateNode(node, nodesById, issues) }

        val order = topologicalOrder(nodesById)
        if (order.size != nodesById.size) issues += issue(ValidationCode.CYCLIC_GRAPH)
        return if (issues.isEmpty()) {
            PlanValidationResult.Valid(ValidatedPlan(plan, order))
        } else {
            PlanValidationResult.Invalid(issues.toList())
        }
    }

    private fun validateNode(
        node: PlanNode,
        nodesById: Map<PlanNodeId, PlanNode>,
        issues: MutableList<ValidationIssue>,
    ) {
        if (node.dependencies.size > PlanLimits.MAX_DEPENDENCIES_PER_NODE) {
            issues += issue(ValidationCode.TOO_MANY_DEPENDENCIES, node.id)
        }
        if (node.dependencies.size != node.dependencies.distinct().size) {
            issues += issue(ValidationCode.DUPLICATE_DEPENDENCY, node.id)
        }
        node.dependencies.distinct().forEach { dependency ->
            when {
                dependency == node.id -> issues += issue(ValidationCode.SELF_DEPENDENCY, node.id)
                dependency !in nodesById -> issues += issue(ValidationCode.MISSING_DEPENDENCY, node.id)
            }
        }
        if (node.declaredCapabilities.isEmpty()) {
            issues += issue(ValidationCode.MISSING_CAPABILITY, node.id)
        }
        if (node.declaredCapabilities.size != node.declaredCapabilities.distinct().size) {
            issues += issue(ValidationCode.DUPLICATE_CAPABILITY, node.id)
        }
        if (node.declaredCapabilities.toSet() != node.action.requiredCapabilities) {
            issues += issue(ValidationCode.CAPABILITY_MISMATCH, node.id)
        }
    }

    private fun topologicalOrder(nodesById: Map<PlanNodeId, PlanNode>): List<PlanNode> {
        val indegree = nodesById.keys.associateWith { 0 }.toMutableMap()
        val dependents = nodesById.keys.associateWith { mutableListOf<PlanNodeId>() }
        nodesById.values.forEach { node ->
            node.dependencies.distinct().filter(nodesById::containsKey).forEach { dependency ->
                indegree[node.id] = indegree.getValue(node.id) + 1
                dependents.getValue(dependency) += node.id
            }
        }
        val ready = PriorityQueue<PlanNodeId>()
        indegree.filterValues { it == 0 }.keys.forEach(ready::add)
        val ordered = mutableListOf<PlanNode>()
        while (ready.isNotEmpty()) {
            val id = ready.remove()
            ordered += nodesById.getValue(id)
            dependents.getValue(id).sorted().forEach { dependent ->
                val remaining = indegree.getValue(dependent) - 1
                indegree[dependent] = remaining
                if (remaining == 0) ready += dependent
            }
        }
        return ordered
    }

    private fun issue(code: ValidationCode, nodeId: PlanNodeId? = null) =
        ValidationIssue(code = code, nodeId = nodeId)
}
