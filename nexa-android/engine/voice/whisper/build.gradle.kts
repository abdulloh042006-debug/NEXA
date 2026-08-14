plugins {
    alias(libs.plugins.nexa.android.impl)
}

android {
    namespace = "ai.nexa.engine.voice.whisper"
}

dependencies {
    api(project(":engine:voice:api"))
}
