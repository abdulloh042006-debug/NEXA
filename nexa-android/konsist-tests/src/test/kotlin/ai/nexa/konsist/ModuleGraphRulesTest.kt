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
}
