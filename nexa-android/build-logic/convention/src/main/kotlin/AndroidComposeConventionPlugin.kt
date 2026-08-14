import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import ai.nexa.buildlogic.libs

/**
 * Convention for any module using Jetpack Compose (Kotlin 2.x compose compiler plugin).
 *
 * Adds the Compose BOM plus the baseline UI stack uniformly so modules never drift
 * onto mismatched Compose artifact versions.
 */
class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            val android = extensions.getByName("android") as CommonExtension<*, *, *, *, *, *>
            android.buildFeatures.compose = true

            dependencies {
                val bom = libs.findLibrary("androidx-compose-bom").get()
                add("implementation", platform(bom))
                add("implementation", libs.findLibrary("androidx-compose-ui").get())
                add("implementation", libs.findLibrary("androidx-compose-material3").get())
                add("implementation", libs.findLibrary("androidx-compose-ui-tooling-preview").get())
                add("debugImplementation", libs.findLibrary("androidx-compose-ui-tooling").get())
            }
        }
    }
}
