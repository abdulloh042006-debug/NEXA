plugins {
    alias(libs.plugins.nexa.android.impl)
}

android {
    namespace = "ai.nexa.self.identity"
}

dependencies {
    api(project(":self:identity:api"))
}
