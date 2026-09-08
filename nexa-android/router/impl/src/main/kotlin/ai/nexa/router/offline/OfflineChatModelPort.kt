package ai.nexa.router.offline

import ai.nexa.core.ai.model.ChatDelta
import ai.nexa.core.ai.model.ChatRequest
import ai.nexa.core.ai.model.ModelManifest
import ai.nexa.core.ai.port.ChatModelPort
import ai.nexa.core.ai.port.ModelInvocationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Honest Phase 1 fallback used until a local model or authenticated cloud session is available. */
class OfflineChatModelPort(
    override val manifest: ModelManifest,
) : ChatModelPort {
    override fun streamChat(request: ChatRequest): Flow<ChatDelta> = flow {
        throw ModelInvocationException.Unavailable()
    }
}
