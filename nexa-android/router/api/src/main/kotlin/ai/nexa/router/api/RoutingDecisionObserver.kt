package ai.nexa.router.api

/** Receives privacy-safe routing metadata; request content is not part of the contract. */
fun interface RoutingDecisionObserver {
    fun onDecision(record: RoutingDecisionRecord)

    companion object {
        val NONE = RoutingDecisionObserver { }
    }
}
