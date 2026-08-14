package ai.nexa.core.ai.testing

import ai.nexa.core.ai.model.ChatDelta
import ai.nexa.core.ai.model.ModelManifest
import ai.nexa.core.ai.model.VisionRequest
import ai.nexa.core.ai.port.VisionModelPort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Scriptable [VisionModelPort] fake — same cold-flow semantics as [FakeChatModelPort]. */
class FakeVisionModelPort(
    override val manifest: ModelManifest = FakeManifests.vision(),
    private val script: (VisionRequest) -> List<ChatDelta> = ::describeScript,
) : VisionModelPort {

    private val recorded = mutableListOf<VisionRequest>()

    /** Every request that has been collected, in collection order. */
    val recordedRequests: List<VisionRequest> get() = recorded

    override fun describe(request: VisionRequest): Flow<ChatDelta> = flow {
        recorded += request
        script(request).forEach { emit(it) }
    }

    companion object {
        fun describeScript(request: VisionRequest): List<ChatDelta> = listOf(
            ChatDelta.Token("A description of ${request.images.size} image(s)."),
            ChatDelta.Usage(inputTokens = request.images.size, outputTokens = 1),
        )
    }
}
