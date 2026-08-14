plugins {
    alias(libs.plugins.nexa.android.impl)
}

android {
    namespace = "ai.nexa.self.personality"
}

dependencies {
    api(project(":self:personality:api"))
}
