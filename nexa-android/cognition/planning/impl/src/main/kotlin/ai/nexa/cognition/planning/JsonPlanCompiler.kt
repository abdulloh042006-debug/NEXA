package ai.nexa.cognition.planning

import ai.nexa.cognition.planning.api.ActionIntent
import ai.nexa.cognition.planning.api.CompilationCode
import ai.nexa.cognition.planning.api.CompilationIssue
import ai.nexa.cognition.planning.api.Plan
import ai.nexa.cognition.planning.api.PlanCompilationResult
import ai.nexa.cognition.planning.api.PlanCompiler
import ai.nexa.cognition.planning.api.PlanCorrelationId
import ai.nexa.cognition.planning.api.PlanId
import ai.nexa.cognition.planning.api.PlanMetadata
import ai.nexa.cognition.planning.api.PlanNode
import ai.nexa.cognition.planning.api.PlanNodeId
import ai.nexa.cognition.planning.api.PlanOrigin
import ai.nexa.cognition.planning.api.PlanValidationResult
import ai.nexa.cognition.planning.api.PlanValidator
import ai.nexa.cognition.planning.api.PlanVersion
import ai.nexa.cognition.planning.api.UntrustedPlanProposal
import ai.nexa.core.permission.CapabilityRequirement
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

class JsonPlanCompiler @Inject constructor() : PlanCompiler {
    private val validator = PlanValidator()

    override fun compile(proposal: UntrustedPlanProposal): PlanCompilationResult {
        val source = proposal.content
        if (source.isBlank()) return rejected(CompilationCode.EMPTY_PROPOSAL)
        if (source.length > MAX_PROPOSAL_LENGTH) return rejected(CompilationCode.PROPOSAL_TOO_LARGE)
        if (JsonDuplicateKeyDetector.containsDuplicate(source)) return rejected(CompilationCode.DUPLICATE_FIELD)

        val document = try {
            Json.parseToJsonElement(source)
        } catch (_: RuntimeException) {
            return rejected(CompilationCode.MALFORMED_DOCUMENT)
        }
        val plan = try {
            parsePlan(document)
        } catch (failure: ProposalException) {
            return rejected(failure.code)
        } catch (_: IllegalArgumentException) {
            return rejected(CompilationCode.INVALID_FIELD)
        }
        return when (val result = validator.validate(plan)) {
            is PlanValidationResult.Valid -> PlanCompilationResult.Valid(result.plan)
            is PlanValidationResult.Invalid -> PlanCompilationResult.Rejected(
                compilationIssues = listOf(CompilationIssue(CompilationCode.VALIDATION_FAILED)),
                validationIssues = result.issues,
            )
        }
    }

    private fun parsePlan(element: JsonElement): Plan {
        val root = element.strictObject(ROOT_FIELDS)
        val schema = root.requiredInt("schemaVersion")
        if (schema != PlanVersion.CURRENT.value) throw ProposalException(CompilationCode.UNSUPPORTED_SCHEMA)
        val metadataObject = root.required("metadata").strictObject(METADATA_FIELDS)
        val correlation = metadataObject.optionalString("correlationId")?.let(::PlanCorrelationId)
        val metadata = PlanMetadata(
            origin = enumValue<PlanOrigin>(metadataObject.requiredString("origin")),
            correlationId = correlation,
        )
        val nodes = root.required("nodes").strictArray().map(::parseNode)
        return Plan(
            id = PlanId(root.requiredString("planId")),
            version = PlanVersion(schema),
            metadata = metadata,
            nodes = nodes,
        )
    }

    private fun parseNode(element: JsonElement): PlanNode {
        val node = element.strictObject(NODE_FIELDS)
        val dependencies = node.required("dependsOn").strictArray().map { PlanNodeId(it.strictString()) }
        val capabilities = node.required("capabilities").strictArray().map {
            CapabilityRequirement(enumValue(it.strictString()))
        }
        return PlanNode(
            id = PlanNodeId(node.requiredString("id")),
            action = parseAction(node.required("action")),
            dependencies = dependencies,
            declaredCapabilities = capabilities,
        )
    }

    private fun parseAction(element: JsonElement): ActionIntent {
        val raw = element as? JsonObject ?: throw ProposalException(CompilationCode.INVALID_FIELD)
        val type = raw.requiredString("type")
        return when (type) {
            "OPEN_APP" -> raw.strictObject(OPEN_APP_FIELDS).let {
                ActionIntent.OpenApp(it.requiredString("packageName"))
            }
            "OPEN_URL" -> raw.strictObject(OPEN_URL_FIELDS).let {
                ActionIntent.OpenUrl(it.requiredString("url"))
            }
            "CREATE_REMINDER" -> raw.strictObject(REMINDER_FIELDS).let {
                ActionIntent.CreateReminder(
                    title = it.requiredString("title"),
                    dueAtEpochMillis = it.requiredLong("dueAtEpochMillis"),
                )
            }
            "DRAFT_MESSAGE" -> raw.strictObject(DRAFT_MESSAGE_FIELDS).let {
                ActionIntent.DraftMessage(
                    recipientReference = it.requiredString("recipientReference"),
                    body = it.requiredString("body"),
                )
            }
            else -> throw ProposalException(CompilationCode.UNKNOWN_ACTION)
        }
    }

    private fun JsonElement.strictObject(fields: Set<String>): JsonObject {
        val value = this as? JsonObject ?: throw ProposalException(CompilationCode.INVALID_FIELD)
        if ((value.keys - fields).isNotEmpty()) throw ProposalException(CompilationCode.UNKNOWN_FIELD)
        return value
    }

    private fun JsonElement.strictArray(): JsonArray =
        this as? JsonArray ?: throw ProposalException(CompilationCode.INVALID_FIELD)

    private fun JsonElement.strictString(): String {
        val primitive = this as? JsonPrimitive
        if (primitive?.isString != true) throw ProposalException(CompilationCode.INVALID_FIELD)
        return primitive.content
    }

    private fun JsonObject.required(name: String): JsonElement =
        this[name] ?: throw ProposalException(CompilationCode.MISSING_FIELD)

    private fun JsonObject.requiredString(name: String): String = required(name).strictString()

    private fun JsonObject.optionalString(name: String): String? = this[name]?.strictString()

    private fun JsonObject.requiredInt(name: String): Int =
        required(name).jsonPrimitive.intOrNull ?: throw ProposalException(CompilationCode.INVALID_FIELD)

    private fun JsonObject.requiredLong(name: String): Long =
        required(name).jsonPrimitive.content.toLongOrNull() ?: throw ProposalException(CompilationCode.INVALID_FIELD)

    private inline fun <reified T : Enum<T>> enumValue(value: String): T =
        enumValues<T>().firstOrNull { it.name == value } ?: throw ProposalException(CompilationCode.INVALID_FIELD)

    private fun rejected(code: CompilationCode) =
        PlanCompilationResult.Rejected(listOf(CompilationIssue(code)))

    private class ProposalException(val code: CompilationCode) : RuntimeException()

    private companion object {
        const val MAX_PROPOSAL_LENGTH = 65_536
        val ROOT_FIELDS = setOf("schemaVersion", "planId", "metadata", "nodes")
        val METADATA_FIELDS = setOf("origin", "correlationId")
        val NODE_FIELDS = setOf("id", "dependsOn", "capabilities", "action")
        val OPEN_APP_FIELDS = setOf("type", "packageName")
        val OPEN_URL_FIELDS = setOf("type", "url")
        val REMINDER_FIELDS = setOf("type", "title", "dueAtEpochMillis")
        val DRAFT_MESSAGE_FIELDS = setOf("type", "recipientReference", "body")
    }
}

private object JsonDuplicateKeyDetector {
    fun containsDuplicate(source: String): Boolean {
        val objectKeys = ArrayDeque<MutableSet<String>?>()
        var index = 0
        while (index < source.length) {
            when (source[index]) {
                '{' -> objectKeys.addLast(mutableSetOf())
                '[' -> objectKeys.addLast(null)
                '}', ']' -> if (objectKeys.isNotEmpty()) objectKeys.removeLast()
                '"' -> {
                    val (value, next) = readString(source, index)
                    val after = source.nextNonWhitespace(next)
                    if (after < source.length && source[after] == ':' && objectKeys.lastOrNull() != null) {
                        if (!checkNotNull(objectKeys.last()).add(value)) return true
                    }
                    index = next - 1
                }
            }
            index++
        }
        return false
    }

    private fun readString(source: String, start: Int): Pair<String, Int> {
        val value = StringBuilder()
        var escaped = false
        var index = start + 1
        while (index < source.length) {
            val character = source[index]
            when {
                escaped -> {
                    value.append(character)
                    escaped = false
                }
                character == '\\' -> escaped = true
                character == '"' -> return value.toString() to index + 1
                else -> value.append(character)
            }
            index++
        }
        return value.toString() to source.length
    }

    private fun String.nextNonWhitespace(start: Int): Int {
        var index = start
        while (index < length && this[index].isWhitespace()) index++
        return index
    }
}
