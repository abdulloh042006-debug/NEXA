import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import ai.nexa.buildlogic.AndroidSdk
import ai.nexa.buildlogic.configureKotlinAndroid

/**
 * Convention for the application module (`:app`).
 *
 * Signing strategy (Sprint 1, approved): debug builds use the default debug keystore;
 * release builds reuse the debug signing config so CI can verify `assembleRelease`
 * end-to-end. Real release signing is provisioned outside the repository before any
 * distribution build (docs/ENGINEERING_DECISIONS.md, ED-7).
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)

                defaultConfig.targetSdk = AndroidSdk.TARGET

                buildTypes {
                    debug {
                        applicationIdSuffix = ".debug"
                    }
                    release {
                        isMinifyEnabled = true
                        isShrinkResources = true
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro",
                        )
                        signingConfig = signingConfigs.getByName("debug")
                    }
                }

                // Distribution flavors (SPEC §2.5/§19.1): Play + GMS devices vs.
                // no-GMS stores (RuStore/AppGallery/direct APK) from one codebase.
                flavorDimensions += "distribution"
                productFlavors {
                    create("gms") {
                        dimension = "distribution"
                        isDefault = true
                    }
                    create("nogms") {
                        dimension = "distribution"
                        versionNameSuffix = "-nogms"
                    }
                }
            }
        }
    }
}
