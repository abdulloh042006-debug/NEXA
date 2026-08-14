plugins {
    alias(libs.plugins.nexa.android.impl)
}

android {
    namespace = "ai.nexa.engine.plugin"
}

dependencies {
    api(project(":engine:plugin:api"))
}
