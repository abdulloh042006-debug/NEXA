plugins {
    alias(libs.plugins.nexa.android.impl)
}

android {
    namespace = "ai.nexa.cognition.critic"
}

dependencies {
    api(project(":cognition:critic:api"))
}
