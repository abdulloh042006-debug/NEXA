plugins {
    alias(libs.plugins.nexa.android.impl)
}

android {
    namespace = "ai.nexa.self.trust"
}

dependencies {
    api(project(":self:trust:api"))
}
