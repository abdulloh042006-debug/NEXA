package ai.nexa.cognition.planning

import ai.nexa.cognition.planning.api.CompilationCode
import ai.nexa.cognition.planning.api.PlanCompilationResult
import ai.nexa.cognition.planning.api.UntrustedPlanProposal
import ai.nexa.cognition.planning.api.ValidationCode
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class JsonPlanCompilerTest {
    private val compiler = JsonPlanCompiler()

    @Test
    fun `allowlisted proposal compiles to validated typed plan`() {
        val result = assertIs<PlanCompilationResult.Valid>(compiler.compile(proposal()))

        assertEquals("plan-1", result.plan.plan.id.value)
        assertEquals(listOf("open", "remind"), result.plan.orderedNodes.map { it.id.value })
    }

    @Test
    fun `unknown actions shell attempts and executable fields fail closed`() {
        assertRejected(proposal(action = """{"type":"SHELL","command":"rm -rf /"}"""), CompilationCode.UNKNOWN_ACTION)
        assertRejected(
            proposal(action = """{"type":"OPEN_APP","packageName":"ai.nexa.app","command":"adb shell"}"""),
            CompilationCode.UNKNOWN_FIELD,
        )
        assertRejected(proposal(extraNodeField = ",\"className\":\"java.lang.Runtime\""), CompilationCode.UNKNOWN_FIELD)
    }

    @Test
    fun `malformed unsupported ambiguous and non-schema proposals fail closed`() {
        assertRejected(UntrustedPlanProposal(""), CompilationCode.EMPTY_PROPOSAL)
        assertRejected(UntrustedPlanProposal("I think you should open the app"), CompilationCode.MALFORMED_DOCUMENT)
        assertRejected(UntrustedPlanProposal("{\"schemaVersion\":"), CompilationCode.MALFORMED_DOCUMENT)
        assertRejected(proposal(schema = 2), CompilationCode.UNSUPPORTED_SCHEMA)
        assertRejected(
            UntrustedPlanProposal(
                proposal().content.replaceFirst(
                    "\"planId\":\"plan-1\"",
                    "\"planId\":\"a\",\"planId\":\"b\"",
                ),
            ),
            CompilationCode.DUPLICATE_FIELD,
        )
        assertRejected(proposal(nodeId = "bad id"), CompilationCode.INVALID_FIELD)
    }

    @Test
    fun `missing fields and invalid targets fail closed`() {
        assertRejected(
            UntrustedPlanProposal(proposal().content.replace("\"dependsOn\":[],", "")),
            CompilationCode.MISSING_FIELD,
        )
        assertRejected(
            proposal(action = """{"type":"OPEN_URL","url":"javascript:alert(1)"}"""),
            CompilationCode.INVALID_FIELD,
        )
    }

    @Test
    fun `structurally parsed unsafe graph is rejected by authoritative validator`() {
        val result = assertIs<PlanCompilationResult.Rejected>(
            compiler.compile(proposal(dependency = "missing")),
        )

        assertEquals(listOf(CompilationCode.VALIDATION_FAILED), result.compilationIssues.map { it.code })
        assertTrue(result.validationIssues.any { it.code == ValidationCode.MISSING_DEPENDENCY })
    }

    private fun assertRejected(proposal: UntrustedPlanProposal, code: CompilationCode) {
        val result = assertIs<PlanCompilationResult.Rejected>(compiler.compile(proposal))
        assertTrue(result.compilationIssues.any { it.code == code })
    }

    private fun proposal(
        schema: Int = 1,
        nodeId: String = "open",
        dependency: String? = null,
        action: String = """{"type":"OPEN_APP","packageName":"ai.nexa.app"}""",
        extraNodeField: String = "",
    ): UntrustedPlanProposal {
        val dependsOn = dependency?.let { "[\"$it\"]" } ?: "[]"
        return UntrustedPlanProposal(
            """
            {
              "schemaVersion":$schema,
              "planId":"plan-1",
              "metadata":{"origin":"USER","correlationId":"correlation-1"},
              "nodes":[
                {
                  "id":"$nodeId",
                  "dependsOn":$dependsOn,
                  "capabilities":["APP_LAUNCH"],
                  "action":$action$extraNodeField
                },
                {
                  "id":"remind",
                  "dependsOn":["$nodeId"],
                  "capabilities":["REMINDER_CREATE"],
                  "action":{"type":"CREATE_REMINDER","title":"Call Aziz","dueAtEpochMillis":1}
                }
              ]
            }
            """.trimIndent(),
        )
    }
}
