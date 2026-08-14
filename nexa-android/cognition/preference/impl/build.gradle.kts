plugins {
    alias(libs.plugins.nexa.android.impl)
}

android {
    namespace = "ai.nexa.cognition.preference"
}

dependencies {
    api(project(":cognition:preference:api"))
}
