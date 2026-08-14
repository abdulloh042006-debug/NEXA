plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

// Architecture law (SPEC §4.3, §19.2). These tests read the whole repository's
// sources and fail the build on forbidden dependencies or naming violations.

dependencies {
    testImplementation(libs.konsist)
    testImplementation(libs.junit)
}
