plugins {
    alias(libs.plugins.nexa.android.impl)
}

android {
    namespace = "ai.nexa.engine.automation"
}

dependencies {
    api(project(":engine:automation:api"))
}
