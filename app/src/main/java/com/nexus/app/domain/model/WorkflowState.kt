package com.nexus.app.domain.model

/**
 * Represents the current state of a running workflow.
 * Used by the UI to display progress.
 */
sealed class WorkflowState {
    /** No workflow is running. */
    data object Idle : WorkflowState()

    /** A workflow is actively executing. */
    data class Running(
        val currentActionId: String,
        val currentActionName: String,
        val completedCount: Int,
        val totalCount: Int,
    ) : WorkflowState()

    /** Workflow completed successfully. */
    data class Completed(val result: WorkflowResult) : WorkflowState()

    /** Workflow failed at a specific action. */
    data class Failed(val result: WorkflowResult) : WorkflowState()

    /** Workflow was cancelled by the user. */
    data class Cancelled(val result: WorkflowResult) : WorkflowState()
}
