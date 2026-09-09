package ai.nexa.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.Test

/**
 * The law of the graph (ANDROID_ENGINEERING_SPECIFICATION §4.3), executable.
 * These rules hold trivially over the Sprint 1 skeleton and harden automatically
 * as real code lands — a forbidden edge becomes a red build, not a review debate.
 */
class ModuleGraphRulesTest {

    /** SPEC §4.1: api contracts and domain modules must stay Android-free. */
    @Test
    fun `api and domain module sources import no android types`() {
        Konsist
            .scopeFromProject()
            .files
            .filter { file ->
                val path = file.path.replace('\\', '/')
                path.contains("/api/src/") ||
                    path.contains("/core/proto/src/") ||
                    path.contains("/core/ai/src/")
            }
            .assertFalse(testName = "api-modules-android-free") { file ->
                file.hasImport { import ->
                    import.name.startsWith("android.") || import.name.startsWith("androidx.")
                }
            }
    }

    /** SPEC §4.3 rule 2: no module reaches into another module's internals. */
    @Test
    fun `no source imports another module's internal package`() {
        Konsist
            .scopeFromProject()
            .files
            .assertFalse(testName = "no-cross-module-internal-imports") { file ->
                val filePackage = file.packagee?.name.orEmpty()
                file.hasImport { import ->
                    import.name.startsWith("ai.nexa.") &&
                        import.name.contains(".internal.") &&
                        !filePackage.startsWith(import.name.substringBefore(".internal."))
                }
            }
    }

    /** Plans remain inert data: planning code has no actuator or Android execution dependency. */
    @Test
    fun `planning cannot depend on action executors or Android APIs`() {
        Konsist
            .scopeFromProject()
            .files
            .filter { file -> file.path.replace('\\', '/').contains("/cognition/planning/") }
            .assertFalse(testName = "planning-has-no-actuators") { file ->
                file.hasImport { import ->
                    import.name.startsWith("android.") ||
                        import.name.startsWith("androidx.") ||
                        import.name.startsWith("ai.nexa.engine.automation") ||
                        import.name.startsWith("ai.nexa.platform")
                }
            }
    }

    /** Probabilistic provider adapters cannot construct or bypass validated plans. */
    @Test
    fun `router and provider code cannot depend on planning`() {
        Konsist
            .scopeFromProject()
            .files
            .filter { file -> file.packagee?.name.orEmpty().startsWith("ai.nexa.router") }
            .assertFalse(testName = "providers-cannot-create-plans") { file ->
                file.hasImport { import -> import.name.startsWith("ai.nexa.cognition.planning") }
            }
    }

    /** Capability authority cannot be minted by planning, model/provider, or feature production code. */
    @Test
    fun `only permission core owns grants and authorization policy`() {
        Konsist
            .scopeFromProject()
            .files
            .filter { file ->
                val path = file.path.replace('\\', '/')
                path.contains("/src/main/") && AUTHORITY_CONSUMER_PATHS.any(path::contains)
            }
            .assertFalse(testName = "permission-core-is-authority") { file ->
                file.hasImport { import ->
                    import.name in FORBIDDEN_AUTHORITY_IMPORTS
                }
            }
    }

    /** Phase 5 is deterministic domain policy and has no Android or actuator dependency. */
    @Test
    fun `permission policy contains no Android actions or automation executor`() {
        Konsist
            .scopeFromProject()
            .files
            .filter { file -> file.path.replace('\\', '/').contains("/core/permission/src/main/") }
            .assertFalse(testName = "permission-policy-has-no-actuators") { file ->
                file.hasImport { import ->
                    import.name.startsWith("android.") ||
                        import.name.startsWith("androidx.") ||
                        import.name.startsWith("ai.nexa.engine.automation") ||
                        import.name.startsWith("ai.nexa.platform")
                }
            }
    }

    private companion object {
        val FORBIDDEN_AUTHORITY_IMPORTS = setOf(
            "ai.nexa.core.permission.CapabilityGrant",
            "ai.nexa.core.permission.ConsentProvenance",
            "ai.nexa.core.permission.GrantStore",
            "ai.nexa.core.permission.InMemoryGrantStore",
        )
        val AUTHORITY_CONSUMER_PATHS = setOf(
            "/cognition/planning/",
            "/router/",
            "/core/ai/",
            "/feature/",
        )
    }
}
