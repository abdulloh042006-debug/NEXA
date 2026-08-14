plugins {
    alias(libs.plugins.nexa.android.impl)
}

android {
    namespace = "ai.nexa.kernel"
}

dependencies {
    api(project(":kernel:api"))
}
