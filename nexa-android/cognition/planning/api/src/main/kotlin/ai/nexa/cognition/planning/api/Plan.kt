package ai.nexa.cognition.planning.api

import ai.nexa.core.permission.CapabilityRequirement
import java.util.Collections

@JvmInline
value class PlanId(val value: String) {
    init {
        require(value.matches(OPAQUE_ID)) { "invalid plan id" }
    }
}

@JvmInline
value class PlanNodeId(val value: String) : Comparable<PlanNodeId> {
    init {
        require(value.matches(OPAQUE_ID)) { "invalid plan node id" }
    }

    override fun compareTo(other: PlanNodeId): Int = value.compareTo(other.value)
}

@JvmInline
value class PlanCorrelationId(val value: String) {
    init {
        require(value.matches(OPAQUE_ID)) { "invalid plan correlation id" }
    }
}

@JvmInline
value class PlanVersion(val value: Int) {
    init {
        require(value > 0) { "plan version must be positive" }
    }

    companion object {
        val CURRENT = PlanVersion(1)
    }
}

enum class PlanOrigin { USER, PROACTIVE, WORKFLOW }

data class PlanMetadata(
    val origin: PlanOrigin,
    val correlationId: PlanCorrelationId? = null,
)

class PlanNode(
    val id: PlanNodeId,
    val action: ActionIntent,
    dependencies: Collection<PlanNodeId> = emptyList(),
    declaredCapabilities: Collection<CapabilityRequirement>,
) {
    val dependencies: List<PlanNodeId> = immutableCopy(dependencies)
    val declaredCapabilities: List<CapabilityRequirement> = immutableCopy(declaredCapabilities)
}

class Plan(
    val id: PlanId,
    val version: PlanVersion,
    val metadata: PlanMetadata,
    nodes: Collection<PlanNode>,
) {
    val nodes: List<PlanNode> = immutableCopy(nodes)
}

/** A validator-issued immutable snapshot. It carries no authorization or execution power. */
class ValidatedPlan internal constructor(
    val plan: Plan,
    orderedNodes: Collection<PlanNode>,
) {
    val orderedNodes: List<PlanNode> = immutableCopy(orderedNodes)
    val requiredCapabilities: Set<CapabilityRequirement> = Collections.unmodifiableSet(
        orderedNodes.flatMap(PlanNode::declaredCapabilities).toSet(),
    )
}

private val OPAQUE_ID = Regex("[A-Za-z0-9._-]{1,64}")

private fun <T> immutableCopy(values: Collection<T>): List<T> =
    Collections.unmodifiableList(values.toList())
