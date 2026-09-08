plugins {
    alias(libs.plugins.nexa.android.api)
}

android {
    namespace = "ai.nexa.kernel.api"
}

dependencies {
    api(projects.core.ai)
    api(projects.router.api)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
}
