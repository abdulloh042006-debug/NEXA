plugins {
    alias(libs.plugins.nexa.android.impl)
}

android {
    namespace = "ai.nexa.self.emotion"
}

dependencies {
    api(project(":self:emotion:api"))
}
