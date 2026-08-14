import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import ai.nexa.buildlogic.libs

/**
 * Convention for modules hosting Room persistence.
 *
 * Sprint 1 applies this to `:core:data` only, with no entities yet — the KSP
 * processor is wired and verified by the build so Sprint 2 can add the first
 * `@Database` (over SQLCipher, SPEC §10) without touching build logic.
 */
class RoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.google.devtools.ksp")

            dependencies {
                add("api", libs.findLibrary("androidx-room-runtime").get())
                add("implementation", libs.findLibrary("androidx-room-ktx").get())
                add("ksp", libs.findLibrary("androidx-room-compiler").get())
            }
        }
    }
}
