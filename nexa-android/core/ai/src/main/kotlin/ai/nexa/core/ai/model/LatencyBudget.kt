package ai.nexa.core.ai.model

/**
 * Per-call latency budget classes (SPEC §11 timeout table, §12.1).
 *
 * Callers declare intent; the router and transport derive deadlines from it.
 * Budgets are hard: streaming calls carry a deadline, never an infinite wait.
 */
enum class LatencyBudget(val budgetMs: Long) {
    /** Active voice turn — the user is waiting with their ears (SPEC §16 voice budgets). */
    INTERACTIVE_VOICE(budgetMs = 4_000),

    /** On-screen interactive request — the user is watching a surface. */
    INTERACTIVE(budgetMs = 10_000),

    /** Deliberative or idle-domain cognition — nobody is waiting (ARCHITECTURE_V2 §7.2–7.3). */
    BACKGROUND(budgetMs = 60_000),
}
