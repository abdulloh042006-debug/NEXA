plugins {
    alias(libs.plugins.nexa.android.impl)
}

android {
    namespace = "ai.nexa.engine.memory"
}

dependencies {
    api(project(":engine:memory:api"))
}
