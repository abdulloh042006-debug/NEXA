plugins {
    alias(libs.plugins.nexa.android.impl)
}

android {
    namespace = "ai.nexa.kernel"

    testOptions.unitTests.isIncludeAndroidResources = true
}

dependencies {
    api(project(":kernel:api"))

    implementation(projects.core.ai)
    implementation(projects.core.data)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(testFixtures(projects.core.ai))
}
