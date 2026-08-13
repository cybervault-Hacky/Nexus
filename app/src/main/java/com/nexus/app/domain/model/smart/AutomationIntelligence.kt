package com.nexus.app.domain.model.smart

/**
 * Priority levels for automation rules.
 */
enum class AutomationPriority(val level: Int) {
    LOW(0),
    NORMAL(1),
    HIGH(2),
    CRITICAL(3),
}

/**
 * Health states for automation rules.
 */
enum class AutomationHealth {
    HEALTHY,
    WARNING,
    FAILING,
    DISABLED,
    BLOCKED,
    UNKNOWN,
}

/**
 * Conflict severity levels.
 */
enum class ConflictSeverity {
    SAFE,
    WARNING,
    CONFLICT,
}

/**
 * A detected conflict between automations.
 */
data class AutomationConflict(
    val automationId: String,
    val conflictingWithId: String,
    val type: ConflictType,
    val severity: ConflictSeverity,
    val explanation: String,
)

enum class ConflictType {
    DUPLICATE,
    ACTION_CONFLICT,
    TRIGGER_OVERLAP,
    CONDITION_CONFLICT,
    RESOURCE_CONFLICT,
    FREQUENCY_RISK,
}

/**
 * A smart suggestion based on pattern analysis.
 */
data class AutomationSuggestion(
    val id: String,
    val title: String,
    val description: String,
    val reason: String,
    val score: Int,
    val suggestedTriggerType: String = "",
    val suggestedPayload: String = "",
    val suggestedContextId: String = "",
)

/**
 * Aggregated execution statistics for an automation.
 */
data class AutomationStats(
    val totalExecutions: Int = 0,
    val successfulExecutions: Int = 0,
    val failedExecutions: Int = 0,
    val cancelledExecutions: Int = 0,
    val averageDurationMs: Long = 0,
    val successRate: Float = 0f,
    val lastExecutionAt: Long? = null,
)
