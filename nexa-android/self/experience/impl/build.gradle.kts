plugins {
    alias(libs.plugins.nexa.android.impl)
}

android {
    namespace = "ai.nexa.self.experience"
}

dependencies {
    api(project(":self:experience:api"))
}
