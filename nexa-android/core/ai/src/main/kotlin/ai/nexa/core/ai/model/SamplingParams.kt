package ai.nexa.core.ai.model

/**
 * Vendor-neutral sampling parameters (ARCHITECTURE §10.1).
 *
 * `null` means "use the routed model's manifest/runtime default" — callers only
 * set what they have an opinion about.
 */
data class SamplingParams(
    val temperature: Double? = null,
    val topP: Double? = null,
    val maxOutputTokens: Int? = null,
    val stopSequences: List<String> = emptyList(),
) {
    init {
        require(temperature == null || temperature >= 0.0) { "temperature must be >= 0" }
        require(topP == null || topP in 0.0..1.0) { "topP must be in [0, 1]" }
        require(maxOutputTokens == null || maxOutputTokens > 0) { "maxOutputTokens must be > 0" }
    }
}
