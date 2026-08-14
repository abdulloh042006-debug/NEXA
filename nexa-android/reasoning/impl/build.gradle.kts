plugins {
    alias(libs.plugins.nexa.android.impl)
}

android {
    namespace = "ai.nexa.reasoning"
}

dependencies {
    api(project(":reasoning:api"))
}
