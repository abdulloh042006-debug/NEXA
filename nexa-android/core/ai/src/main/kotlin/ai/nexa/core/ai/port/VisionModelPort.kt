package ai.nexa.core.ai.port

import ai.nexa.core.ai.model.ChatDelta
import ai.nexa.core.ai.model.VisionRequest
import kotlinx.coroutines.flow.Flow

/**
 * Streaming image understanding (ARCHITECTURE §10.1): images + prompt in,
 * [ChatDelta] stream out. Same cold/cancellable flow contract as
 * [ChatModelPort.streamChat] (SPEC §12.2).
 */
interface VisionModelPort : ModelPort {

    fun describe(request: VisionRequest): Flow<ChatDelta>
}
