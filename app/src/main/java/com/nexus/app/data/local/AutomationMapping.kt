package com.nexus.app.data.local

import com.nexus.app.domain.model.automation.AutomationExecution
import com.nexus.app.domain.model.automation.AutomationRule
import com.nexus.app.domain.model.automation.ExecutionStatus
import com.nexus.app.domain.model.automation.TriggerType
import com.nexus.app.domain.model.smart.AutomationHealth
import com.nexus.app.domain.model.smart.AutomationPriority

fun AutomationRule.toEntity(): AutomationEntity = AutomationEntity(
    id = id, name = name, description = description, isEnabled = isEnabled,
    triggerType = triggerType.name, triggerPayload = triggerPayload,
    contextId = contextId, cooldownSeconds = cooldownSeconds,
    lastTriggeredAt = lastTriggeredAt, createdAt = createdAt, updatedAt = updatedAt,
    priority = priority.level, healthStatus = healthStatus.name,
    conditionsJson = conditionsJson,
    executionCount = executionCount, failureCount = failureCount, successCount = successCount,
)

fun AutomationEntity.toDomain(): AutomationRule = AutomationRule(
    id = id, name = name, description = description, isEnabled = isEnabled,
    triggerType = TriggerType.valueOf(triggerType), triggerPayload = triggerPayload,
    contextId = contextId, cooldownSeconds = cooldownSeconds,
    lastTriggeredAt = lastTriggeredAt, createdAt = createdAt, updatedAt = updatedAt,
    priority = AutomationPriority.entries.getOrElse(priority) { AutomationPriority.NORMAL },
    healthStatus = try { AutomationHealth.valueOf(healthStatus) } catch (_: Exception) { AutomationHealth.UNKNOWN },
    conditionsJson = conditionsJson,
    executionCount = executionCount, failureCount = failureCount, successCount = successCount,
)

fun AutomationExecution.toEntity(): AutomationExecutionEntity = AutomationExecutionEntity(
    id = id, automationId = automationId, startedAt = startedAt, completedAt = completedAt,
    status = status.name, triggerType = triggerType.name, contextId = contextId,
    successfulActions = successfulActions, failedActions = failedActions, errorMessage = errorMessage,
)

fun AutomationExecutionEntity.toDomain(): AutomationExecution = AutomationExecution(
    id = id, automationId = automationId, startedAt = startedAt, completedAt = completedAt,
    status = ExecutionStatus.valueOf(status), triggerType = TriggerType.valueOf(triggerType),
    contextId = contextId, successfulActions = successfulActions, failedActions = failedActions,
    errorMessage = errorMessage,
)
