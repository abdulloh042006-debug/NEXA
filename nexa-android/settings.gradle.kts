pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "nexa"

// App shell
include(":app")

// Kernel · Reasoning · Router (ANDROID_ENGINEERING_SPECIFICATION §4.2)
include(":kernel:api", ":kernel:impl")
include(":reasoning:api", ":reasoning:impl")
include(":router:api", ":router:impl")

// Cognition engines (api stubs are day-1 seams; impls fill per phase)
include(":cognition:worldmodel:api", ":cognition:worldmodel:impl")
include(":cognition:goal:api", ":cognition:goal:impl")
include(":cognition:planning:api", ":cognition:planning:impl")
include(":cognition:critic:api", ":cognition:critic:impl")
include(":cognition:reflection:api", ":cognition:reflection:impl")
include(":cognition:learning:api", ":cognition:learning:impl")
include(":cognition:curiosity:api", ":cognition:curiosity:impl")
include(":cognition:preference:api", ":cognition:preference:impl")

// Self engines
include(":self:identity:api", ":self:identity:impl")
include(":self:personality:api", ":self:personality:impl")
include(":self:emotion:api", ":self:emotion:impl")
include(":self:trust:api", ":self:trust:impl")
include(":self:relationship:api", ":self:relationship:impl")
include(":self:experience:api", ":self:experience:impl")

// Capability engines
include(":engine:memory:api", ":engine:memory:impl")
include(":engine:context:api", ":engine:context:impl")
include(":engine:automation:api", ":engine:automation:impl")
include(":engine:voice:api", ":engine:voice:impl")
include(":engine:voice:whisper", ":engine:voice:tts", ":engine:voice:androidspeech")
include(":engine:vision:api", ":engine:vision:impl")
include(":engine:vision:ocr", ":engine:vision:screen", ":engine:vision:camera")
include(":engine:plugin:api", ":engine:plugin:impl")

// Core services
include(":core:common")
include(":core:proto")
include(":core:ai")
include(":core:inference-local")
include(":core:permission")
include(":core:data")
include(":core:sync")
include(":core:events")
include(":core:background")
include(":core:network")
include(":core:design")

// Platform adapters — the only home of sensitive Android APIs
include(":platform:telephony")
include(":platform:calendar")
include(":platform:contacts")
include(":platform:files")
include(":platform:notifications")
include(":platform:accessibility")
include(":platform:camera")
include(":platform:sensors")
include(":platform:connectivity")
include(":platform:media")

// Features
include(":feature:chat")
include(":feature:voice-ui")
include(":feature:overlay")
include(":feature:memory-browser")
include(":feature:workflows")
include(":feature:goals")
include(":feature:trust")
include(":feature:timeline")
include(":feature:onboarding")
include(":feature:settings-privacy")

// Quality
include(":benchmark")
include(":konsist-tests")
