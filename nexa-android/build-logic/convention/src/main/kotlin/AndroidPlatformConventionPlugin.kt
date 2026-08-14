import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Module type `android-platform` (SPEC §4.1): the ONLY modules that may import
 * sensitive Android APIs (telephony, calendar, accessibility, camera, sensors,
 * notifications). Every adapter is consumed through an engine port and gated by
 * `:core:permission` — hence the type-level dependency.
 */
class AndroidPlatformConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("nexa.android.library")

            dependencies {
                add("implementation", project(":core:common"))
                add("implementation", project(":core:permission"))
            }
        }
    }
}
