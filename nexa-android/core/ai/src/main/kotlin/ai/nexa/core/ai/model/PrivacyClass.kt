package ai.nexa.core.ai.model

/**
 * Privacy classification of a datum or request (ARCHITECTURE §23.2, SPEC E6).
 *
 * Set by code from data provenance — never by a model. Ordering is significant:
 * a higher ordinal is more sensitive, and the router's hard filter only admits
 * models cleared for the request's class (ARCHITECTURE §11.2).
 */
enum class PrivacyClass {
    /** Public, non-personal content: web lookups, definitions, translations of public text. */
    P0_PUBLIC,

    /** Personal but non-critical content: conversation history, preferences, calendar titles. */
    P1_PERSONAL,

    /** Sensitive content that must never leave the device: message bodies, health, finance. */
    P2_SENSITIVE,
}
