package com.nexus.app.domain.model.automation

/**
 * A persisted record of a single automation execution.
 */
data class AutomationExecution(
    val id: String,
    val automationId: String,
    val startedAt: Long,
    val completedAt: Long?,
    val status: ExecutionStatus,
    val triggerType: TriggerType,
    val contextId: String?,
    val successfulActions: Int = 0,
    val failedActions: Int = 0,
    val errorMessage: String? = null,
)
