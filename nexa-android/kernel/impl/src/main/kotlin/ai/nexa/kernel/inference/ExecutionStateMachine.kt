package ai.nexa.kernel.inference

internal class ExecutionStateMachine {
    private var state = State.CREATED

    @Synchronized
    fun accept(event: InferenceEvent): Boolean {
        if (state.terminal) return false
        state = when (event) {
            is InferenceEvent.Queued -> if (state == State.CREATED) State.QUEUED else return false
            is InferenceEvent.Started -> if (state in setOf(State.QUEUED, State.STARTED)) State.STARTED else return false
            is InferenceEvent.Delta -> if (state in setOf(State.STARTED, State.STREAMING)) State.STREAMING else return false
            is InferenceEvent.Completed -> if (state in setOf(State.STARTED, State.STREAMING)) State.COMPLETED else return false
            is InferenceEvent.Cancelled -> State.CANCELLED
            is InferenceEvent.TimedOut -> State.TIMED_OUT
            is InferenceEvent.Failed -> State.FAILED
        }
        return true
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
