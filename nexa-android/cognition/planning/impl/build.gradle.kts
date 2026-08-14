plugins {
    alias(libs.plugins.nexa.android.impl)
}

android {
    namespace = "ai.nexa.cognition.planning"
}

dependencies {
    api(project(":cognition:planning:api"))
}
