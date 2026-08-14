plugins {
    alias(libs.plugins.nexa.kotlin.domain)
    `java-test-fixtures`
}

// AI core: the vendor-neutral ModelPort contracts (ARCHITECTURE §10.1) and,
// later, prompt infrastructure and guardrails (SPEC §6.2, §12.1). Pure Kotlin
// by design — the AI seam must never leak Android or vendor types.
//
// The port Fakes ship as test fixtures (SPEC §6.3, §18.1): they are part of the
// api contract, consumed by dependents' tests via testFixtures(":core:ai").

dependencies {
    testFixturesImplementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
