plugins {
    alias(libs.plugins.nexa.android.library)
    alias(libs.plugins.nexa.hilt)
    alias(libs.plugins.nexa.room)
}

android {
    namespace = "ai.nexa.core.data"

    testOptions.unitTests.isIncludeAndroidResources = true
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.proto)
    implementation(libs.androidx.datastore)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.sqlcipher.android)

    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
}
