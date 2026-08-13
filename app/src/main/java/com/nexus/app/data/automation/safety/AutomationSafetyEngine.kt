package com.nexus.app.data.automation.safety

/**
 * Safety limits for automation execution.
 */
object AutomationSafetyEngine {
    const val MAX_EXECUTIONS_PER_MINUTE = 10
    const val MAX_CONCURRENT_WORKFLOWS = 3
    const val MAX_WORKFLOW_NODES = 50
    const val MAX_WORKFLOW_DURATION_MS = 300_000L // 5 minutes
    const val MAX_COMPOSITE_DEPTH = 5
    const val MAX_CONDITIONS_PER_RULE = 20

    private val recentExecutions = mutableMapOf<String, MutableList<Long>>()

    /** Check if an automation is within rate limits. */
    fun canExecute(automationId: String): Boolean {
        val now = System.currentTimeMillis()
        val timestamps = recentExecutions.getOrPut(automationId) { mutableListOf() }
        timestamps.removeAll { now - it > 60_000 }
        return timestamps.size < MAX_EXECUTIONS_PER_MINUTE
    }

    /** Record an execution for rate limiting. */
    fun recordExecution(automationId: String) {
        val timestamps = recentExecutions.getOrPut(automationId) { mutableListOf() }
        timestamps.add(System.currentTimeMillis())
        // Prune old entries
        if (timestamps.size > 100) timestamps.subList(0, timestamps.size - 50).clear()
    }

    /** Get remaining executions in current window. */
    fun remainingInWindow(automationId: String): Int {
        val now = System.currentTimeMillis()
        val timestamps = recentExecutions[automationId] ?: return MAX_EXECUTIONS_PER_MINUTE
        val recent = timestamps.count { now - it < 60_000 }
        return maxOf(0, MAX_EXECUTIONS_PER_MINUTE - recent)
    }
}
