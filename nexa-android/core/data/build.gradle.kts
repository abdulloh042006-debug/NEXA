plugins {
    alias(libs.plugins.nexa.android.library)
    alias(libs.plugins.nexa.hilt)
    alias(libs.plugins.nexa.room)
}

android {
    namespace = "ai.nexa.core.data"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.proto)
    implementation(libs.androidx.datastore)
}
