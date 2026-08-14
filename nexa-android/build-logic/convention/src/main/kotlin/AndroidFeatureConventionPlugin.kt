import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import ai.nexa.buildlogic.libs

/**
 * Module type `android-feature` (SPEC §4.1): Compose UI + ViewModels only.
 * The type wires what every feature is allowed to see — the design system,
 * `:core:common`, and the navigation stack — so a new feature module starts
 * lawful by construction.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("nexa.android.library")
            pluginManager.apply("nexa.android.compose")
            pluginManager.apply("nexa.hilt")

            dependencies {
                add("implementation", project(":core:common"))
                add("implementation", project(":core:design"))
                add("implementation", libs.findLibrary("androidx-navigation-compose").get())
                add("implementation", libs.findLibrary("androidx-lifecycle-runtime-compose").get())
            }
        }
    }
}
