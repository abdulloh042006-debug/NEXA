plugins {
    alias(libs.plugins.nexa.android.impl)
}

android {
    namespace = "ai.nexa.engine.context"
}

dependencies {
    api(project(":engine:context:api"))
}
