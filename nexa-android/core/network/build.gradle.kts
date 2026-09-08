plugins {
    alias(libs.plugins.nexa.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "ai.nexa.core.network"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)

    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)
}
