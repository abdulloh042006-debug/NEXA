plugins {
    alias(libs.plugins.nexa.kotlin.domain)
    alias(libs.plugins.wire)
}

wire {
    kotlin {}
}

dependencies {
    api(libs.wire.runtime)
}
