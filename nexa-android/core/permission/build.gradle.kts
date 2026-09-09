plugins {
    alias(libs.plugins.nexa.android.library)
    alias(libs.plugins.nexa.hilt)
}

android {
    namespace = "ai.nexa.core.permission"
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
}
