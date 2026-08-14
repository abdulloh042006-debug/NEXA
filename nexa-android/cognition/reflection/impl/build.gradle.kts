plugins {
    alias(libs.plugins.nexa.android.impl)
}

android {
    namespace = "ai.nexa.cognition.reflection"
}

dependencies {
    api(project(":cognition:reflection:api"))
}
