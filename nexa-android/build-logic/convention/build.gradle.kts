plugins {
    `kotlin-dsl`
}

group = "ai.nexa.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "nexa.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "nexa.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidApi") {
            id = "nexa.android.api"
            implementationClass = "AndroidApiConventionPlugin"
        }
        register("androidImpl") {
            id = "nexa.android.impl"
            implementationClass = "AndroidImplConventionPlugin"
        }
        register("androidFeature") {
            id = "nexa.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("androidPlatform") {
            id = "nexa.android.platform"
            implementationClass = "AndroidPlatformConventionPlugin"
        }
        register("androidCompose") {
            id = "nexa.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("kotlinDomain") {
            id = "nexa.kotlin.domain"
            implementationClass = "KotlinDomainConventionPlugin"
        }
        register("hilt") {
            id = "nexa.hilt"
            implementationClass = "HiltConventionPlugin"
        }
        register("room") {
            id = "nexa.room"
            implementationClass = "RoomConventionPlugin"
        }
    }
}
