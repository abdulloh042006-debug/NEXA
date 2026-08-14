package ai.nexa.core.ai.port

import ai.nexa.core.ai.model.ChatDelta
import ai.nexa.core.ai.model.ChatRequest
import ai.nexa.core.ai.model.ModelManifest
import kotlinx.coroutines.flow.Flow

/**
 * Streaming text generation (ARCHITECTURE §10.1).
 *
 * The returned flow is cold and cancellable (SPEC §12.2): nothing is generated
 * until collection, and collector cancellation must propagate to the runtime
 * (native abort callback) or wire call (gRPC cancel) within the 150 ms
 * cancellation-to-silence budget. Generation runs in the caller's scope —
 * no detached inference.
 */
interface ChatModelPort : ModelPort {

    /**
     * Function calling is a capability flag on chat models, not a separate port
     * (ARCHITECTURE §10.1). When false, [ChatRequest.toolSchemas] must be empty.
     */
    val supportsFunctionCalling: Boolean
        get() = ModelManifest.ModelCapability.TOOLS in manifest.capabilities

    fun streamChat(request: ChatRequest): Flow<ChatDelta>
}
