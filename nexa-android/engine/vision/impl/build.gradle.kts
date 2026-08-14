plugins {
    alias(libs.plugins.nexa.android.impl)
}

android {
    namespace = "ai.nexa.engine.vision"
}

dependencies {
    api(project(":engine:vision:api"))
}
