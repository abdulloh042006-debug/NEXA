package ai.nexa.cognition.planning

import ai.nexa.cognition.planning.api.ActionIntent
import ai.nexa.cognition.planning.api.Plan
import ai.nexa.cognition.planning.api.PlanAuthorizationStatus
import ai.nexa.cognition.planning.api.PlanId
import ai.nexa.cognition.planning.api.PlanMetadata
import ai.nexa.cognition.planning.api.PlanNode
import ai.nexa.cognition.planning.api.PlanNodeId
import ai.nexa.cognition.planning.api.PlanOrigin
import ai.nexa.cognition.planning.api.PlanValidationResult
import ai.nexa.cognition.planning.api.PlanValidator
import ai.nexa.cognition.planning.api.PlanVersion
import ai.nexa.core.permission.AuthorizationAuditObserver
import ai.nexa.core.permission.AuthorizationClock
import ai.nexa.core.permission.AuthorizationContext
import ai.nexa.core.permission.CapabilityEngine
import ai.nexa.core.permission.CapabilityGrant
import ai.nexa.core.permission.ConsentProvenance
import ai.nexa.core.permission.GrantId
import ai.nexa.core.permission.GrantScope
import ai.nexa.core.permission.GrantSource
import ai.nexa.core.permission.InMemoryGrantStore
import ai.nexa.core.permission.RuntimePermissionState
import ai.nexa.core.permission.RuntimePermissionStatePort
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultPlanAuthorizationPortTest {
    private var now = 100L
    private val store = InMemoryGrantStore()
    private val clock = AuthorizationClock { now }
    private val engine = CapabilityEngine(
        store,
        RuntimePermissionStatePort { RuntimePermissionState.GRANTED },
        clock,
        AuthorizationAuditObserver {},
    )
    private val port = DefaultPlanAuthorizationPort(engine, clock)
    private val context = AuthorizationContext("request-1", "session-1")

    @Test
    fun `all allowed creates immutable revalidation snapshot`() {
        val plan = validated(node("open", ActionIntent.OpenApp("ai.nexa.app")))
        grant(plan.orderedNodes.single(), "grant-1")

        val result = port.authorize(plan, context)

        assertEquals(PlanAuthorizationStatus.AUTHORIZED, result.status)
        assertEquals(listOf(PlanNodeId("open")), result.allowedNodes)
        assertTrue(assertNotNull(result.snapshot).requiresLiveRevalidation)
        assertEquals(64, result.snapshot?.planFingerprint?.length)
        assertFailsWith<UnsupportedOperationException> {
            @Suppress("UNCHECKED_CAST")
            (result.snapshot?.grantIds as MutableList).add(GrantId("forged"))
        }
    }

    @Test
    fun `one denied node makes the whole dependency plan denied`() {
        val root = node("root", ActionIntent.OpenApp("ai.nexa.app"))
        val dependent = node("dependent", ActionIntent.OpenUrl("https://example.com"), "root")
        val plan = validated(root, dependent)
        grant(root, "grant-1")

        val result = port.authorize(plan, context)

        assertEquals(PlanAuthorizationStatus.DENIED, result.status)
        assertEquals(listOf(PlanNodeId("dependent")), result.deniedNodes)
        assertNull(result.snapshot)
    }

    @Test
    fun `consent node makes mixed parallel plan explicitly consent required`() {
        val app = node("app", ActionIntent.OpenApp("ai.nexa.app"))
        val reminder = node("reminder", ActionIntent.CreateReminder("safe category only", 200))
        val plan = validated(app, reminder)
        grant(app, "grant-1")

        val result = port.authorize(plan, context)

        assertEquals(PlanAuthorizationStatus.CONSENT_REQUIRED, result.status)
        assertEquals(listOf(PlanNodeId("reminder")), result.consentRequiredNodes)
        assertEquals("reminder.create", result.consentRequests.single().capabilityId)
        assertTrue(result.consentRequests.single().toString().contains("REMINDER_STORE"))
        assertNull(result.snapshot)
    }

    @Test
    fun `revoked grant makes a previously authorized snapshot stale`() {
        val plan = validated(node("open", ActionIntent.OpenApp("ai.nexa.app")))
        val grant = grant(plan.orderedNodes.single(), "grant-1")
        assertEquals(PlanAuthorizationStatus.AUTHORIZED, port.authorize(plan, context).status)

        store.revoke(grant.id, now)

        assertEquals(PlanAuthorizationStatus.DENIED, port.authorize(plan, context).status)
    }

    private fun validated(vararg nodes: PlanNode) =
        (
            PlanValidator().validate(
                Plan(
                    PlanId("plan-1"),
                    PlanVersion.CURRENT,
                    PlanMetadata(PlanOrigin.USER),
                    nodes.toList(),
                ),
            ) as PlanValidationResult.Valid
            ).plan

    private fun node(id: String, action: ActionIntent, vararg dependencies: String) = PlanNode(
        PlanNodeId(id),
        action,
        dependencies.map(::PlanNodeId),
        action.requiredCapabilities,
    )

    private fun grant(node: PlanNode, id: String): CapabilityGrant {
        val requirement = node.declaredCapabilities.single()
        return CapabilityGrant(
            GrantId(id),
            requirement.capability,
            requirement.target,
            GrantScope.Once(context.authorizationRequestId),
            issuedAtEpochMillis = 1,
            expiresAtEpochMillis = 1_000,
            consent = ConsentProvenance(GrantSource.USER_CONSENT, 1),
        ).also(store::put)
    }
}
