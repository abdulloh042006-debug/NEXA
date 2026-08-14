import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import ai.nexa.buildlogic.libs
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/**
 * Module type `kotlin-domain` (SPEC §4.1): pure Kotlin — models, ports, use-case
 * logic. Zero Android. May use coroutines and `javax.inject` only, so domain code
 * stays JVM-pure, millisecond-testable, and KMP-ready (SPEC E7, §24).
 */
class KotlinDomainConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.jvm")

            extensions.configure<KotlinJvmProjectExtension> {
                jvmToolchain(17)
            }

            dependencies {
                add("implementation", libs.findLibrary("kotlinx-coroutines-core").get())
                add("implementation", libs.findLibrary("javax-inject").get())
            }
        }
    }
}
