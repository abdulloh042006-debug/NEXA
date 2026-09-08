import ai.nexa.buildlogic.libs
import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Convention for modules hosting Room persistence.
 *
 * Room schemas are exported into each owning module's committed `schemas/` directory.
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

            val schemaDirectory = layout.projectDirectory.dir("schemas").asFile.path
            extensions.configure<KspExtension> {
                arg("room.schemaLocation", schemaDirectory)
                arg("room.incremental", "true")
            }
        }
    }
}
