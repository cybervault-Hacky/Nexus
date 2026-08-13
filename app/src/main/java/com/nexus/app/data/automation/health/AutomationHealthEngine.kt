package com.nexus.app.data.automation.health

import com.nexus.app.data.local.AutomationExecutionEntity
import com.nexus.app.data.local.AutomationEntity
import com.nexus.app.domain.model.smart.AutomationHealth
import com.nexus.app.domain.model.smart.AutomationStats

/**
 * Evaluates automation health based on execution history.
 */
object AutomationHealthEngine {

    fun evaluateHealth(entity: AutomationEntity, executions: List<AutomationExecutionEntity>): AutomationHealth {
        if (!entity.isEnabled) return AutomationHealth.DISABLED

        val stats = calculateStats(executions)
        if (stats.totalExecutions == 0) return AutomationHealth.UNKNOWN

        return when {
            stats.successRate >= 0.9f -> AutomationHealth.HEALTHY
            stats.successRate >= 0.6f -> AutomationHealth.WARNING
            stats.successRate >= 0.3f -> AutomationHealth.FAILING
            else -> AutomationHealth.BLOCKED
        }
    }

    fun calculateStats(executions: List<AutomationExecutionEntity>): AutomationStats {
        if (executions.isEmpty()) return AutomationStats()

        val total = executions.size
        val success = executions.count { it.status == "SUCCESS" }
        val failed = executions.count { it.status == "FAILED" }
        val cancelled = executions.count { it.status == "CANCELLED" }
        val durations = executions.mapNotNull { exec ->
            exec.completedAt?.let { it - exec.startedAt }
        }
        val avgDuration = if (durations.isNotEmpty()) durations.average().toLong() else 0L
        val successRate = if (total > 0) success.toFloat() / total else 0f

        return AutomationStats(
            totalExecutions = total,
            successfulExecutions = success,
            failedExecutions = failed,
            cancelledExecutions = cancelled,
            averageDurationMs = avgDuration,
            successRate = successRate,
            lastExecutionAt = executions.maxByOrNull { it.startedAt }?.startedAt,
        )
    }

    fun healthScore(stats: AutomationStats): Int {
        if (stats.totalExecutions == 0) return 50
        val base = (stats.successRate * 100).toInt()
        val penalty = when {
            stats.failedExecutions > stats.successfulExecutions -> 20
            stats.failedExecutions > 0 -> 10
            else -> 0
        }
        return (base - penalty).coerceIn(0, 100)
    }
}
