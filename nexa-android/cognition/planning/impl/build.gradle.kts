plugins {
    alias(libs.plugins.nexa.android.impl)
}

android {
    namespace = "ai.nexa.cognition.planning"
}

dependencies {
    api(project(":cognition:planning:api"))

    implementation(projects.core.ai)
    implementation(projects.kernel.api)
    implementation(projects.router.api)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
}
