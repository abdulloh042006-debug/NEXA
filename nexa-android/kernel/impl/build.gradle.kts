plugins {
    alias(libs.plugins.nexa.android.impl)
}

android {
    namespace = "ai.nexa.kernel"
}

dependencies {
    api(project(":kernel:api"))

    implementation(projects.core.ai)
    implementation(projects.core.data)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(testFixtures(projects.core.ai))
}
