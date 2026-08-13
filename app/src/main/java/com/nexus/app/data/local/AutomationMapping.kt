package com.nexus.app.data.local

import com.nexus.app.domain.model.automation.AutomationExecution
import com.nexus.app.domain.model.automation.AutomationRule
import com.nexus.app.domain.model.automation.ExecutionStatus
import com.nexus.app.domain.model.automation.TriggerType

fun AutomationRule.toEntity(): AutomationEntity = AutomationEntity(
    id = id, name = name, description = description, isEnabled = isEnabled,
    triggerType = triggerType.name, triggerPayload = triggerPayload,
    contextId = contextId, cooldownSeconds = cooldownSeconds,
    lastTriggeredAt = lastTriggeredAt, createdAt = createdAt, updatedAt = updatedAt,
)

fun AutomationEntity.toDomain(): AutomationRule = AutomationRule(
    id = id, name = name, description = description, isEnabled = isEnabled,
    triggerType = TriggerType.valueOf(triggerType), triggerPayload = triggerPayload,
    contextId = contextId, cooldownSeconds = cooldownSeconds,
    lastTriggeredAt = lastTriggeredAt, createdAt = createdAt, updatedAt = updatedAt,
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
