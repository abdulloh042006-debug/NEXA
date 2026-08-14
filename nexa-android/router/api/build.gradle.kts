plugins {
    alias(libs.plugins.nexa.kotlin.domain)
}

// Router api: the RouterPort contract (ARCHITECTURE §11, SPEC §12.1). Pure
// Kotlin like :core:ai — the routing seam is domain law and must never grow an
// Android or vendor dependency (SPEC E7). Exposed types build on the
// vendor-neutral :core:ai models, so :core:ai is an api dependency.

dependencies {
    api(project(":core:ai"))
}
