plugins {
    alias(libs.plugins.nexa.android.impl)
}

android {
    namespace = "ai.nexa.engine.vision.screen"
}

dependencies {
    api(project(":engine:vision:api"))
}
