package ai.nexa.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/**
 * Baseline Android + Kotlin configuration shared by application and library modules.
 *
 * SDK levels follow the approved compatibility strategy (ARCHITECTURE.md §33.1):
 * minSdk 26, compile/target latest stable.
 */
internal object AndroidSdk {
    const val COMPILE = 35
    const val MIN = 26
    const val TARGET = 35
}

internal fun Project.configureKotlinAndroid(commonExtension: CommonExtension<*, *, *, *, *, *>) {
    commonExtension.apply {
        compileSdk = AndroidSdk.COMPILE

        defaultConfig {
            minSdk = AndroidSdk.MIN
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        lint {
            abortOnError = true
            warningsAsErrors = false
        }
    }

    extensions.configure<KotlinAndroidProjectExtension> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            allWarningsAsErrors.set(false)
        }
    }
}
