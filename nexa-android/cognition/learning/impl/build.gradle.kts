plugins {
    alias(libs.plugins.nexa.android.impl)
}

android {
    namespace = "ai.nexa.cognition.learning"
}

dependencies {
    api(project(":cognition:learning:api"))
}
