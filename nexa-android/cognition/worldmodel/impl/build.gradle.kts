plugins {
    alias(libs.plugins.nexa.android.impl)
}

android {
    namespace = "ai.nexa.cognition.worldmodel"
}

dependencies {
    api(project(":cognition:worldmodel:api"))
}
