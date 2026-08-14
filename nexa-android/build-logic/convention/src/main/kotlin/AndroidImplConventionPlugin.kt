import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Module type `android-impl` (SPEC §4.1): implementations and their DI bindings
 * (`di/` package, SPEC §8.2). Depends on its own `:api`, `:core:*`, and
 * `:platform:*` via ports. Hilt is part of the type because bindings live here.
 */
class AndroidImplConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("nexa.android.library")
            pluginManager.apply("nexa.hilt")
        }
    }
}
