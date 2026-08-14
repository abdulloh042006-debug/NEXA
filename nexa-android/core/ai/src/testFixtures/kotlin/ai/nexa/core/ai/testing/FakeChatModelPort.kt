package ai.nexa.core.ai.testing

import ai.nexa.core.ai.model.ChatDelta
import ai.nexa.core.ai.model.ChatRequest
import ai.nexa.core.ai.model.ModelManifest
import ai.nexa.core.ai.port.ChatModelPort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Scriptable [ChatModelPort] fake (SPEC §6.3 — every port ships a Fake).
 *
 * The flow is cold like the real contract: nothing is recorded or emitted
 * until collection, and each collection replays the script.
 */
class FakeChatModelPort(
    override val manifest: ModelManifest = FakeManifests.chat(),
    private val script: (ChatRequest) -> List<ChatDelta> = ::echoScript,
    private val failure: Throwable? = null,
) : ChatModelPort {

    private val recorded = mutableListOf<ChatRequest>()

    /** Every request that has been collected, in collection order. */
    val recordedRequests: List<ChatRequest> get() = recorded

    override fun streamChat(request: ChatRequest): Flow<ChatDelta> = flow {
        recorded += request
        script(request).forEach { emit(it) }
        failure?.let { throw it }
    }

    companion object {
        /** Default script: streams the last message back word by word, then usage. */
        fun echoScript(request: ChatRequest): List<ChatDelta> {
            val words = request.messages.last().content.split(" ")
            return words.map { ChatDelta.Token("$it ") } +
                ChatDelta.Usage(inputTokens = words.size, outputTokens = words.size)
        }
    }
}
