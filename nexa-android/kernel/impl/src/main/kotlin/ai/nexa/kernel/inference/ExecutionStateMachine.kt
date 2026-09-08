package ai.nexa.kernel.inference

internal class ExecutionStateMachine {
    private var state = State.CREATED

    @Synchronized
    fun accept(event: InferenceEvent): Boolean {
        val next = if (state.terminal) null else nextState(event)
        if (next != null) state = next
        return next != null
    }

    private fun nextState(event: InferenceEvent): State? = when (event) {
        is InferenceEvent.Queued -> State.QUEUED.takeIf { state == State.CREATED }
        is InferenceEvent.Started -> State.STARTED.takeIf {
            state in setOf(State.QUEUED, State.STARTED)
        }
        is InferenceEvent.Delta -> State.STREAMING.takeIf {
            state in setOf(State.STARTED, State.STREAMING)
        }
        is InferenceEvent.Completed -> State.COMPLETED.takeIf {
            state in setOf(State.STARTED, State.STREAMING)
        }
        is InferenceEvent.Cancelled -> State.CANCELLED
        is InferenceEvent.TimedOut -> State.TIMED_OUT
        is InferenceEvent.Failed -> State.FAILED
    }

    private enum class State(val terminal: Boolean = false) {
        CREATED,
        QUEUED,
        STARTED,
        STREAMING,
        COMPLETED(true),
        CANCELLED(true),
        TIMED_OUT(true),
        FAILED(true),
    }
}
