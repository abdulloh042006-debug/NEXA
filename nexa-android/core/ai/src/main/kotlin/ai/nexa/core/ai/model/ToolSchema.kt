package ai.nexa.core.ai.model

/**
 * A tool offered to the model for function calling (ARCHITECTURE §10.1
 * FunctionCallingPort — a capability of chat models, not a separate port).
 *
 * Vendor-neutral: adapters translate to each provider's tool-definition dialect.
 */
data class ToolSchema(
    val name: String,
    val description: String,
    /** JSON Schema for the tool's arguments, as a raw JSON string. */
    val parametersJsonSchema: String,
)
