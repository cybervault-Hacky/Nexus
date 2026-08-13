package com.nexus.app.data.automation

import com.nexus.app.data.action.WorkflowExecutor
import com.nexus.app.domain.model.NexusAction
import com.nexus.app.domain.model.automation.AutomationExecution
import com.nexus.app.domain.model.automation.AutomationRule
import com.nexus.app.domain.model.automation.ExecutionStatus
import com.nexus.app.domain.model.automation.TriggerEvent
import com.nexus.app.domain.model.automation.TriggerType
import com.nexus.app.domain.repository.ActionRepository
import com.nexus.app.domain.repository.AutomationRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

/**
 * Core automation engine.
 *
 * Receives [TriggerEvent]s, finds matching automations, checks cooldowns,
 * executes workflows via the existing [WorkflowExecutor], and records
 * execution history.
 *
 * Thread-safe: uses a Mutex to prevent duplicate concurrent executions
 * of the same automation.
 */
class TriggerEngine(
    private val automationRepository: AutomationRepository,
    private val actionRepository: ActionRepository,
    private val workflowExecutor: WorkflowExecutor,
) {
    private val executionMutex = Mutex()

    /** Process a trigger event. */
    suspend fun onTrigger(event: TriggerEvent): List<AutomationExecution> {
        val matchingRules = findMatchingRules(event)
        return matchingRules.map { rule -> executeAutomation(rule, event) }
    }

    /** Manually trigger a specific automation by ID. */
    suspend fun manualTrigger(automationId: String): AutomationExecution? {
        val rule = automationRepository.getById(automationId) ?: return null
        val event = TriggerEvent.Manual(automationId)
        return executeAutomation(rule, event)
    }

    private suspend fun findMatchingRules(event: TriggerEvent): List<AutomationRule> {
        return when (event) {
            is TriggerEvent.Manual -> {
                val id = event.automationId
                val rule = automationRepository.getById(id)
                if (rule != null && rule.isEnabled) listOf(rule) else emptyList()
            }
            is TriggerEvent.Time -> {
                if (event.automationId != null) {
                    val rule = automationRepository.getById(event.automationId!!)
                    if (rule != null && rule.isEnabled && rule.triggerType == TriggerType.TIME) listOf(rule)
                    else emptyList()
                } else {
                    automationRepository.getEnabledByTriggerType(TriggerType.TIME)
                }
            }
            is TriggerEvent.AppOpened -> {
                automationRepository.getEnabledByTriggerType(TriggerType.APP_OPEN).filter { rule ->
                    val payload = rule.triggerPayload
                    payload.contains("\"${event.packageName}\"")
                }
            }
            is TriggerEvent.AppClosed -> {
                automationRepository.getEnabledByTriggerType(TriggerType.APP_CLOSE).filter { rule ->
                    val payload = rule.triggerPayload
                    payload.contains("\"${event.packageName}\"")
                }
            }
            is TriggerEvent.ContextActivated -> {
                automationRepository.getEnabledByTriggerType(TriggerType.CONTEXT_ACTIVATED).filter { rule ->
                    rule.contextId == event.contextId
                }
            }
        }
    }

    private suspend fun executeAutomation(
        rule: AutomationRule,
        event: TriggerEvent,
    ): AutomationExecution {
        val executionId = UUID.randomUUID().toString()
        val startedAt = System.currentTimeMillis()

        return executionMutex.withLock {
            // Check cooldown
            if (!CooldownPolicy.canExecute(rule)) {
                val skipped = AutomationExecution(
                    id = executionId, automationId = rule.id, startedAt = startedAt,
                    completedAt = System.currentTimeMillis(), status = ExecutionStatus.SKIPPED_COOLDOWN,
                    triggerType = event.toTriggerType(), contextId = rule.contextId,
                    errorMessage = "Cooldown active (${CooldownPolicy.remainingCooldownSeconds(rule)}s remaining)",
                )
                automationRepository.recordExecution(skipped)
                return@withLock skipped
            }

            // Check disabled (double-check)
            if (!rule.isEnabled) {
                val skipped = AutomationExecution(
                    id = executionId, automationId = rule.id, startedAt = startedAt,
                    completedAt = System.currentTimeMillis(), status = ExecutionStatus.SKIPPED_DISABLED,
                    triggerType = event.toTriggerType(), contextId = rule.contextId,
                )
                automationRepository.recordExecution(skipped)
                return@withLock skipped
            }

            // Load workflow actions
            val actions = actionRepository.getActionsForContext(rule.contextId)
            if (actions.isEmpty()) {
                val skipped = AutomationExecution(
                    id = executionId, automationId = rule.id, startedAt = startedAt,
                    completedAt = System.currentTimeMillis(), status = ExecutionStatus.SKIPPED_INVALID,
                    triggerType = event.toTriggerType(), contextId = rule.contextId,
                    errorMessage = "No actions configured for context",
                )
                automationRepository.recordExecution(skipped)
                return@withLock skipped
            }

            // Mark triggered (starts cooldown)
            automationRepository.markTriggered(rule.id, startedAt)

            // Execute workflow using existing WorkflowExecutor
            try {
                val result = workflowExecutor.execute(actions)
                val completedAt = System.currentTimeMillis()
                val status = if (result.overallSuccess) ExecutionStatus.SUCCESS else ExecutionStatus.FAILED

                val execution = AutomationExecution(
                    id = executionId, automationId = rule.id, startedAt = startedAt,
                    completedAt = completedAt, status = status,
                    triggerType = event.toTriggerType(), contextId = rule.contextId,
                    successfulActions = result.completedCount,
                    failedActions = result.totalCount - result.completedCount,
                )
                automationRepository.recordExecution(execution)
                execution
            } catch (e: Exception) {
                val execution = AutomationExecution(
                    id = executionId, automationId = rule.id, startedAt = startedAt,
                    completedAt = System.currentTimeMillis(), status = ExecutionStatus.FAILED,
                    triggerType = event.toTriggerType(), contextId = rule.contextId,
                    errorMessage = e.message ?: "Unknown error",
                )
                automationRepository.recordExecution(execution)
                execution
            }
        }
    }
}

private fun TriggerEvent.toTriggerType(): TriggerType = when (this) {
    is TriggerEvent.Manual -> TriggerType.MANUAL
    is TriggerEvent.Time -> TriggerType.TIME
    is TriggerEvent.AppOpened -> TriggerType.APP_OPEN
    is TriggerEvent.AppClosed -> TriggerType.APP_CLOSE
    is TriggerEvent.ContextActivated -> TriggerType.CONTEXT_ACTIVATED
}
