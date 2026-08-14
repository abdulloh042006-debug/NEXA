import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Module type `android-api` (SPEC §4.1): the public contract of an engine or
 * feature — interfaces, models, nav routes. May depend on domain modules and
 * `:core:common` only; never on implementations.
 */
class AndroidApiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("nexa.android.library")

            dependencies {
                add("implementation", project(":core:common"))
            }
        }
    }
}
