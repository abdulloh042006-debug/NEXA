plugins {
    alias(libs.plugins.nexa.android.impl)
}

android {
    namespace = "ai.nexa.self.relationship"
}

dependencies {
    api(project(":self:relationship:api"))
}
