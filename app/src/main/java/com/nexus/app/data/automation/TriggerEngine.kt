package com.nexus.app.data.automation

import com.nexus.app.data.action.WorkflowExecutor
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
 * Core automation engine. Reused across all phases.
 * Receives events → matches rules → checks cooldown → executes workflow.
 */
class TriggerEngine(
    private val automationRepository: AutomationRepository,
    private val actionRepository: ActionRepository,
    private val workflowExecutor: WorkflowExecutor,
) {
    private val executionMutex = Mutex()

    suspend fun onTrigger(event: TriggerEvent): List<AutomationExecution> {
        val matchingRules = findMatchingRules(event)
        return matchingRules.map { rule -> executeAutomation(rule, event) }
    }

    suspend fun manualTrigger(automationId: String): AutomationExecution? {
        val rule = automationRepository.getById(automationId) ?: return null
        return executeAutomation(rule, TriggerEvent.Manual(automationId))
    }

    private suspend fun findMatchingRules(event: TriggerEvent): List<AutomationRule> {
        return when (event) {
            is TriggerEvent.Manual -> {
                val rule = automationRepository.getById(event.automationId)
                if (rule != null && rule.isEnabled) listOf(rule) else emptyList()
            }
            is TriggerEvent.Time -> {
                if (event.automationId != null) {
                    val rule = automationRepository.getById(event.automationId!!)
                    if (rule != null && rule.isEnabled && rule.triggerType == TriggerType.TIME) listOf(rule) else emptyList()
                } else {
                    automationRepository.getEnabledByTriggerType(TriggerType.TIME)
                }
            }
            // Composite triggers
            is TriggerEvent.WifiConnected, is TriggerEvent.WifiDisconnected,
            is TriggerEvent.BluetoothConnected, is TriggerEvent.BluetoothDisconnected,
            is TriggerEvent.ChargingStarted, is TriggerEvent.ChargingStopped,
            is TriggerEvent.BatteryLevelChanged,
            is TriggerEvent.DeviceBoot, is TriggerEvent.ScreenOn, is TriggerEvent.ScreenOff,
            is TriggerEvent.DeviceIdle, is TriggerEvent.DeviceActive,
            is TriggerEvent.NfcTagDetected, is TriggerEvent.NfcTagRemoved,
            is TriggerEvent.GeofenceEntered, is TriggerEvent.GeofenceExited,
            is TriggerEvent.CalendarEventStarted, is TriggerEvent.CalendarEventEnded,
            is TriggerEvent.NotificationPosted, is TriggerEvent.NotificationRemoved,
            is TriggerEvent.AppOpened, is TriggerEvent.AppClosed,
            is TriggerEvent.ContextActivated -> {
                val targetType = event.toTriggerType()
                val directRules = automationRepository.getEnabledByTriggerType(targetType).filter { TriggerMatcher.matches(event, it) }
                // Also check composite triggers
                val allRules = automationRepository.getEnabledByTriggerType(TriggerType.ALL_CONDITIONS) +
                    automationRepository.getEnabledByTriggerType(TriggerType.ANY_CONDITION)
                val compositeRules = allRules.filter { CompositeTriggerEvaluator.evaluate(it, event, allRules) }
                directRules + compositeRules
            }
        }
    }

    private suspend fun executeAutomation(rule: AutomationRule, event: TriggerEvent): AutomationExecution {
        val executionId = UUID.randomUUID().toString()
        val startedAt = System.currentTimeMillis()
        val triggerType = event.toTriggerType()

        return executionMutex.withLock {
            if (!CooldownPolicy.canExecute(rule)) {
                return@withLock skip(executionId, rule, startedAt, triggerType, ExecutionStatus.SKIPPED_COOLDOWN,
                    "Cooldown active (${CooldownPolicy.remainingCooldownSeconds(rule)}s)")
            }
            if (!rule.isEnabled) {
                return@withLock skip(executionId, rule, startedAt, triggerType, ExecutionStatus.SKIPPED_DISABLED)
            }
            val actions = actionRepository.getActionsForContext(rule.contextId)
            if (actions.isEmpty()) {
                return@withLock skip(executionId, rule, startedAt, triggerType, ExecutionStatus.SKIPPED_INVALID, "No actions")
            }

            automationRepository.markTriggered(rule.id, startedAt)
            try {
                val result = workflowExecutor.execute(actions)
                val execution = AutomationExecution(
                    id = executionId, automationId = rule.id, startedAt = startedAt,
                    completedAt = System.currentTimeMillis(),
                    status = if (result.overallSuccess) ExecutionStatus.SUCCESS else ExecutionStatus.FAILED,
                    triggerType = triggerType, contextId = rule.contextId,
                    successfulActions = result.completedCount,
                    failedActions = result.totalCount - result.completedCount,
                )
                automationRepository.recordExecution(execution)
                execution
            } catch (e: Exception) {
                val execution = AutomationExecution(
                    id = executionId, automationId = rule.id, startedAt = startedAt,
                    completedAt = System.currentTimeMillis(), status = ExecutionStatus.FAILED,
                    triggerType = triggerType, contextId = rule.contextId,
                    errorMessage = e.message ?: "Unknown error",
                )
                automationRepository.recordExecution(execution)
                execution
            }
        }
    }

    private suspend fun skip(id: String, rule: AutomationRule, startedAt: Long, triggerType: TriggerType, status: ExecutionStatus, msg: String? = null): AutomationExecution {
        val execution = AutomationExecution(
            id = id, automationId = rule.id, startedAt = startedAt,
            completedAt = System.currentTimeMillis(), status = status,
            triggerType = triggerType, contextId = rule.contextId, errorMessage = msg,
        )
        automationRepository.recordExecution(execution)
        return execution
    }
}

fun TriggerEvent.toTriggerType(): TriggerType = when (this) {
    is TriggerEvent.Manual -> TriggerType.MANUAL
    is TriggerEvent.Time -> TriggerType.TIME
    is TriggerEvent.AppOpened -> TriggerType.APP_OPEN
    is TriggerEvent.AppClosed -> TriggerType.APP_CLOSE
    is TriggerEvent.ContextActivated -> TriggerType.CONTEXT_ACTIVATED
    is TriggerEvent.WifiConnected -> TriggerType.WIFI_CONNECTED
    is TriggerEvent.WifiDisconnected -> TriggerType.WIFI_DISCONNECTED
    is TriggerEvent.BluetoothConnected -> TriggerType.BLUETOOTH_CONNECTED
    is TriggerEvent.BluetoothDisconnected -> TriggerType.BLUETOOTH_DISCONNECTED
    is TriggerEvent.ChargingStarted -> TriggerType.CHARGING_STARTED
    is TriggerEvent.ChargingStopped -> TriggerType.CHARGING_STOPPED
    is TriggerEvent.BatteryLevelChanged -> TriggerType.BATTERY_BELOW
    is TriggerEvent.DeviceBoot -> TriggerType.DEVICE_BOOT
    is TriggerEvent.ScreenOn -> TriggerType.SCREEN_ON
    is TriggerEvent.ScreenOff -> TriggerType.SCREEN_OFF
    is TriggerEvent.DeviceIdle -> TriggerType.DEVICE_IDLE
    is TriggerEvent.DeviceActive -> TriggerType.DEVICE_ACTIVE
    is TriggerEvent.NfcTagDetected -> TriggerType.NFC_TAG_DETECTED
    is TriggerEvent.NfcTagRemoved -> TriggerType.NFC_TAG_REMOVED
    is TriggerEvent.GeofenceEntered -> TriggerType.GEOFENCE_ENTER
    is TriggerEvent.GeofenceExited -> TriggerType.GEOFENCE_EXIT
    is TriggerEvent.CalendarEventStarted -> TriggerType.CALENDAR_EVENT_START
    is TriggerEvent.CalendarEventEnded -> TriggerType.CALENDAR_EVENT_END
    is TriggerEvent.NotificationPosted -> TriggerType.NOTIFICATION_POSTED
    is TriggerEvent.NotificationRemoved -> TriggerType.NOTIFICATION_REMOVED
}
