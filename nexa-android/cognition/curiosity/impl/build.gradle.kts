plugins {
    alias(libs.plugins.nexa.android.impl)
}

android {
    namespace = "ai.nexa.cognition.curiosity"
}

dependencies {
    api(project(":cognition:curiosity:api"))
}
