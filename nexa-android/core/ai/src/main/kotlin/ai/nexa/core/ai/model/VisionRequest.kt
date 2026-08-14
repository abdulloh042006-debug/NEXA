package ai.nexa.core.ai.model

/**
 * Images + prompt for a [ai.nexa.core.ai.port.VisionModelPort] (ARCHITECTURE §10.1).
 *
 * Per the vision layer's tree-first rule (SPEC §6.2), pixel paths require an
 * explicit privacy class — there is no default, so no caller can forget it.
 */
data class VisionRequest(
    val images: List<ImageInput>,
    val prompt: String,
    val privacyClass: PrivacyClass,
    val latencyBudget: LatencyBudget,
    val languageHint: Language? = null,
) {
    init {
        require(images.isNotEmpty()) { "a VisionRequest must carry at least one image" }
    }

    /** Encoded image bytes — pure Kotlin on purpose; adapters decode, this module never does. */
    class ImageInput(
        val bytes: ByteArray,
        /** e.g. `"image/jpeg"`, `"image/png"`. */
        val mimeType: String,
    ) {
        init {
            require(bytes.isNotEmpty()) { "image bytes must not be empty" }
            require(mimeType.startsWith("image/")) { "mimeType must be an image/* type" }
        }

        override fun equals(other: Any?): Boolean =
            this === other ||
                (other is ImageInput && mimeType == other.mimeType && bytes.contentEquals(other.bytes))

        override fun hashCode(): Int = 31 * mimeType.hashCode() + bytes.contentHashCode()

        override fun toString(): String = "ImageInput(mimeType=$mimeType, bytes=${bytes.size}B)"
    }
}
