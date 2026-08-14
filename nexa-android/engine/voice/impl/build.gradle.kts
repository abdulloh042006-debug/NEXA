plugins {
    alias(libs.plugins.nexa.android.impl)
}

android {
    namespace = "ai.nexa.engine.voice"
}

dependencies {
    api(project(":engine:voice:api"))
}
