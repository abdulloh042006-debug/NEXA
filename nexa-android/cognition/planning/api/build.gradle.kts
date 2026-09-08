plugins {
    alias(libs.plugins.nexa.android.api)
}

android {
    namespace = "ai.nexa.cognition.planning.api"
}

dependencies {
    api(projects.core.permission)

    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
}
