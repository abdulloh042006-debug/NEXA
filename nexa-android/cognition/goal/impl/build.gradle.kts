plugins {
    alias(libs.plugins.nexa.android.impl)
}

android {
    namespace = "ai.nexa.cognition.goal"
}

dependencies {
    api(project(":cognition:goal:api"))
}
