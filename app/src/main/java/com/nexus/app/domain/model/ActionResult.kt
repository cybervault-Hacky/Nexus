package com.nexus.app.domain.model

/**
 * Structured result of executing a single action.
 */
sealed class ActionResult {
    abstract val actionId: String
    abstract val startTime: Long
    abstract val endTime: Long

    data class Success(
        override val actionId: String,
        override val startTime: Long,
        override val endTime: Long,
    ) : ActionResult()

    data class Failed(
        override val actionId: String,
        override val startTime: Long,
        override val endTime: Long,
        val error: String,
    ) : ActionResult()

    data class Cancelled(
        override val actionId: String,
        override val startTime: Long,
        override val endTime: Long,
    ) : ActionResult()
}

/**
 * Overall result of executing a workflow (sequence of actions).
 */
data class WorkflowResult(
    val actionResults: List<ActionResult>,
    val completedCount: Int,
    val totalCount: Int,
    val overallSuccess: Boolean,
)
