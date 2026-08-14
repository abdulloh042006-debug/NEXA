import io.gitlab.arturbosch.detekt.extensions.DetektExtension

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.wire) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.spotless)
}

// ---------------------------------------------------------------------------
// Quality tooling (ANDROID_ENGINEERING_SPECIFICATION §19.2):
//   ktlint   -> runs as detekt-formatting (official style, .editorconfig-driven)
//   detekt   -> static analysis, config/detekt/detekt.yml
//   spotless -> non-Kotlin hygiene (md/yml/toml) — no overlap with detekt-formatting
//   Konsist  -> architecture law, lives in :konsist-tests
// ---------------------------------------------------------------------------

spotless {
    format("misc") {
        target("**/*.md", "**/*.yml", "**/*.yaml", "**/*.toml")
        targetExclude("**/build/**", "**/.gradle/**")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

subprojects {
    apply(plugin = rootProject.libs.plugins.detekt.get().pluginId)

    configure<DetektExtension> {
        buildUponDefaultConfig = true
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
        parallel = true
    }

    dependencies {
        "detektPlugins"(rootProject.libs.detekt.formatting)
    }

    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        jvmTarget = "17"
        reports {
            html.required.set(true)
            xml.required.set(false)
            sarif.required.set(true)
        }
    }
}
