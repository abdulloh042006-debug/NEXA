plugins {
    alias(libs.plugins.nexa.android.api)
}

android {
    namespace = "ai.nexa.kernel.api"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}
