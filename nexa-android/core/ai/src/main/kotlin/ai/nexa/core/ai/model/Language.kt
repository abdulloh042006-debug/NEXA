package ai.nexa.core.ai.model

/**
 * The product's language space (ARCHITECTURE P9 — trilingual-first).
 *
 * Used both as a request hint (which language the model should reason and
 * answer in) and as the key of per-language quality scores in [ModelManifest].
 */
enum class Language {
    UZ,
    RU,
    EN,

    /** Code-switched input (uz/ru/en mixed in one utterance) — common in the target market. */
    MIXED,
}
