plugins {
    alias(libs.plugins.nexa.android.impl)
}

android {
    namespace = "ai.nexa.engine.vision.camera"
}

dependencies {
    api(project(":engine:vision:api"))
}
