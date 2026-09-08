package ai.nexa.kernel.inference

import ai.nexa.core.ai.model.ChatDelta
import ai.nexa.core.ai.port.ChatModelPort
import ai.nexa.core.ai.port.ChatModelRegistry
import ai.nexa.core.ai.port.ModelInvocationException
import ai.nexa.router.api.RouterPort
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.concurrent.atomic.AtomicBoolean

class DefaultInferenceOrchestrator(
    private val router: RouterPort,
    private val registry: ChatModelRegistry,
    private val observer: InferenceExecutionObserver = InferenceExecutionObserver.NONE,
    private val now: () -> Long = System::currentTimeMillis,
) : InferenceOrchestratorPort {
    override fun start(request: InferenceExecutionRequest): InferenceExecution = Execution(request)

    private inner class Execution(private val request: InferenceExecutionRequest) : InferenceExecution {
        private val cancelRequested = AtomicBoolean(false)
        private val collected = AtomicBoolean(false)
        private val cancelSignal = CompletableDeferred<Unit>()

        override fun cancel(): Boolean = cancelRequested.compareAndSet(false, true).also {
            if (it) cancelSignal.complete(Unit)
        }

        override val events: Flow<InferenceEvent> = channelFlow {
            check(collected.compareAndSet(false, true)) { "an inference execution may only be collected once" }
            val lifecycle = ExecutionStateMachine()
            if (cancelRequested.get()) {
                publish(lifecycle, InferenceEvent.Cancelled(now()))
                return@channelFlow
            }
            val runner = launch { runExecution(request, lifecycle) }
            val cancellationWatcher = launch {
                cancelSignal.await()
                val event = InferenceEvent.Cancelled(now())
                if (lifecycle.accept(event)) {
                    runner.cancelAndJoin()
                    send(event)
                    observe(request, event)
                }
            }
            runner.join()
            if (cancelRequested.get()) cancellationWatcher.join() else cancellationWatcher.cancel()
        }

        private suspend fun ProducerScope<InferenceEvent>.runExecution(
            request: InferenceExecutionRequest,
            lifecycle: ExecutionStateMachine,
        ) {
            publish(lifecycle, InferenceEvent.Queued(now()))
            try {
                withTimeout(request.timeoutMillis) { executeCandidates(request, lifecycle) }
            } catch (_: TimeoutCancellationException) {
                publish(lifecycle, InferenceEvent.TimedOut(now()))
            } catch (cancelled: CancellationException) {
                val event = InferenceEvent.Cancelled(now())
                if (lifecycle.accept(event)) observe(request, event)
                throw cancelled
            } catch (_: BackendResolutionException) {
                publish(lifecycle, backendUnavailable(now()))
            } catch (failure: ModelInvocationException) {
                publish(lifecycle, InferenceEvent.Failed(failure.toInferenceFailure(), now()))
            }
        }

        private suspend fun ProducerScope<InferenceEvent>.executeCandidates(
            request: InferenceExecutionRequest,
            lifecycle: ExecutionStateMachine,
        ) {
            val decision = router.resolveChat(request.routeRequest)
            if (decision.ranked.isEmpty()) {
                publish(
                    lifecycle,
                    InferenceEvent.Failed(
                        InferenceFailure(InferenceFailure.Code.NO_ELIGIBLE_MODEL, false),
                        now(),
                    ),
                )
            } else {
                for ((index, manifest) in decision.ranked.withIndex()) {
                    val backend = registry.resolve(manifest.id) ?: throw BackendResolutionException()
                    publish(
                        lifecycle,
                        InferenceEvent.Started(manifest.id, manifest.providerId.value, now()),
                    )
                    val completed = executeAttempt(
                        backend,
                        request,
                        lifecycle,
                        isLast = index == decision.ranked.lastIndex,
                    )
                    if (completed) break
                }
            }
        }

        private suspend fun ProducerScope<InferenceEvent>.executeAttempt(
            backend: ChatModelPort,
            request: InferenceExecutionRequest,
            lifecycle: ExecutionStateMachine,
            isLast: Boolean,
        ): Boolean {
            var emitted = false
            return try {
                backend.streamChat(request.routeRequest.request).collect { delta ->
                    validateDelta(delta, request)
                    emitted = true
                    publish(lifecycle, InferenceEvent.Delta(delta, now()))
                }
                publish(lifecycle, InferenceEvent.Completed(now()))
                true
            } catch (failure: ModelInvocationException) {
                if (emitted || isLast) throw failure
                false
            }
        }

        private fun validateDelta(delta: ChatDelta, request: InferenceExecutionRequest) {
            if (delta is ChatDelta.ToolCall && request.routeRequest.request.toolSchemas.isEmpty()) {
                throw ModelInvocationException.ProtocolFailure()
            }
        }

        private suspend fun ProducerScope<InferenceEvent>.publish(
            lifecycle: ExecutionStateMachine,
            event: InferenceEvent,
        ) {
            if (lifecycle.accept(event)) {
                send(event)
                observe(request, event)
            }
        }
    }

    private fun observe(request: InferenceExecutionRequest, event: InferenceEvent) {
        observer.onEvent(
            InferenceExecutionRecord(
                executionId = request.executionId,
                state = event.toRecordState(),
                modelId = (event as? InferenceEvent.Started)?.modelId,
                providerId = (event as? InferenceEvent.Started)?.providerId,
                failureCode = (event as? InferenceEvent.Failed)?.failure?.code,
            ),
        )
    }

    private fun InferenceEvent.toRecordState() = when (this) {
        is InferenceEvent.Queued -> InferenceExecutionRecord.State.QUEUED
        is InferenceEvent.Started -> InferenceExecutionRecord.State.STARTED
        is InferenceEvent.Delta -> InferenceExecutionRecord.State.STREAMING
        is InferenceEvent.Completed -> InferenceExecutionRecord.State.COMPLETED
        is InferenceEvent.Cancelled -> InferenceExecutionRecord.State.CANCELLED
        is InferenceEvent.TimedOut -> InferenceExecutionRecord.State.TIMED_OUT
        is InferenceEvent.Failed -> InferenceExecutionRecord.State.FAILED
    }

    private fun ModelInvocationException.toInferenceFailure(): InferenceFailure = when (this) {
        is ModelInvocationException.Unavailable -> failure(InferenceFailure.Code.BACKEND_UNAVAILABLE)
        is ModelInvocationException.NetworkUnavailable -> failure(InferenceFailure.Code.NETWORK_UNAVAILABLE)
        is ModelInvocationException.AuthenticationUnavailable -> {
            failure(InferenceFailure.Code.AUTHENTICATION_UNAVAILABLE)
        }
        is ModelInvocationException.Rejected -> failure(InferenceFailure.Code.PROVIDER_REJECTED)
        is ModelInvocationException.RateLimited -> failure(InferenceFailure.Code.RATE_LIMITED)
        is ModelInvocationException.ProtocolFailure -> failure(InferenceFailure.Code.PROTOCOL_FAILURE)
        is ModelInvocationException.ProviderFailure -> failure(InferenceFailure.Code.PROVIDER_INTERNAL)
    }

    private fun ModelInvocationException.failure(code: InferenceFailure.Code) =
        InferenceFailure(code, retryable)

    private fun backendUnavailable(at: Long) = InferenceEvent.Failed(
        InferenceFailure(InferenceFailure.Code.BACKEND_UNAVAILABLE, false),
        at,
    )

    private class BackendResolutionException : Exception()
}
