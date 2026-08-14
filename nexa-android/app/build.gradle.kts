plugins {
    alias(libs.plugins.nexa.android.application)
    alias(libs.plugins.nexa.android.compose)
    alias(libs.plugins.nexa.hilt)
}

android {
    namespace = "ai.nexa.app"

    defaultConfig {
        applicationId = "ai.nexa.app"
        versionCode = 1
        versionName = "0.1.0"
    }

    // BuildConfig strategy: the app module is the single BuildConfig owner.
    // NEXA_ENV distinguishes runtime environments by build type; distribution
    // differences ride the gms/nogms flavors (SPEC §2.5).
    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            buildConfigField("String", "NEXA_ENV", "\"dev\"")
        }
        release {
            buildConfigField("String", "NEXA_ENV", "\"prod\"")
        }
    }
}

dependencies {
    implementation(projects.core.design)

    // DI wiring only (SPEC §4.3): the app shell aggregates impl modules' Hilt bindings.
    implementation(projects.router.impl)

    implementation(libs.androidx.activity.compose)
    implementation(libs.timber)
}
