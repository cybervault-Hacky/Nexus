package com.nexus.app.ui.screens.automations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nexus.app.data.automation.AutomationScheduler
import com.nexus.app.data.automation.TriggerEngine
import com.nexus.app.domain.model.automation.AutomationExecution
import com.nexus.app.domain.model.automation.AutomationRule
import com.nexus.app.domain.model.automation.AutomationValidation
import com.nexus.app.domain.model.automation.ExecutionStatus
import com.nexus.app.domain.model.automation.TriggerType
import com.nexus.app.domain.repository.AutomationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AutomationViewModel(
    private val repository: AutomationRepository,
    private val triggerEngine: TriggerEngine,
    private val scheduler: AutomationScheduler,
) : ViewModel() {

    val automations: StateFlow<List<AutomationRule>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val enabledCount: StateFlow<Int> = repository.observeEnabledCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val recentExecutions: StateFlow<List<AutomationExecution>> = repository.observeRecentExecutions(20)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _uiState = MutableStateFlow<AutomationUiState>(AutomationUiState.Idle)
    val uiState: StateFlow<AutomationUiState> = _uiState.asStateFlow()

    fun createRule(
        name: String, description: String, triggerType: TriggerType,
        triggerPayload: String, contextId: String, cooldownSeconds: Int,
    ) {
        val trimmedName = name.trim()
        val trimmedDesc = description.trim()
        val error = AutomationValidation.validate(contextId, triggerType, trimmedName, trimmedDesc, cooldownSeconds, triggerPayload)
        if (error != null) { _uiState.value = AutomationUiState.Error(error); return }

        viewModelScope.launch {
            _uiState.value = AutomationUiState.Saving
            try {
                val now = System.currentTimeMillis()
                val rule = AutomationRule(
                    id = java.util.UUID.randomUUID().toString(), name = trimmedName, description = trimmedDesc,
                    triggerType = triggerType, triggerPayload = triggerPayload, contextId = contextId,
                    cooldownSeconds = cooldownSeconds, createdAt = now, updatedAt = now,
                )
                repository.create(rule)
                if (triggerType == TriggerType.TIME) scheduleTime(rule)
                _uiState.value = AutomationUiState.Saved
            } catch (e: Exception) { _uiState.value = AutomationUiState.Error(e.message ?: "Failed") }
        }
    }

    fun updateRule(
        id: String, name: String, description: String, triggerType: TriggerType,
        triggerPayload: String, contextId: String, cooldownSeconds: Int,
    ) {
        val trimmedName = name.trim()
        val trimmedDesc = description.trim()
        val error = AutomationValidation.validate(contextId, triggerType, trimmedName, trimmedDesc, cooldownSeconds, triggerPayload)
        if (error != null) { _uiState.value = AutomationUiState.Error(error); return }

        viewModelScope.launch {
            _uiState.value = AutomationUiState.Saving
            try {
                val existing = repository.getById(id) ?: run { _uiState.value = AutomationUiState.Error("Not found"); return@launch }
                repository.update(existing.copy(name = trimmedName, description = trimmedDesc, triggerType = triggerType, triggerPayload = triggerPayload, contextId = contextId, cooldownSeconds = cooldownSeconds))
                if (triggerType == TriggerType.TIME) scheduleTime(existing.copy(name = trimmedName, triggerPayload = triggerPayload, cooldownSeconds = cooldownSeconds))
                _uiState.value = AutomationUiState.Saved
            } catch (e: Exception) { _uiState.value = AutomationUiState.Error(e.message ?: "Failed") }
        }
    }

    fun deleteRule(id: String) {
        viewModelScope.launch {
            try { scheduler.cancelTimeAutomation(id); repository.delete(id); _uiState.value = AutomationUiState.Idle }
            catch (e: Exception) { _uiState.value = AutomationUiState.Error(e.message ?: "Failed") }
        }
    }

    fun setEnabled(id: String, enabled: Boolean) {
        viewModelScope.launch {
            try {
                repository.setEnabled(id, enabled)
                val rule = repository.getById(id)
                if (rule != null) {
                    if (enabled && rule.triggerType == TriggerType.TIME) scheduleTime(rule)
                    else if (!enabled) scheduler.cancelTimeAutomation(id)
                }
            } catch (_: Exception) {}
        }
    }

    fun runNow(id: String) {
        viewModelScope.launch {
            _uiState.value = AutomationUiState.Executing
            try {
                val execution = triggerEngine.manualTrigger(id)
                _uiState.value = when (execution?.status) {
                    ExecutionStatus.SUCCESS -> AutomationUiState.ExecutionResult("Completed successfully")
                    ExecutionStatus.FAILED -> AutomationUiState.ExecutionResult("Failed: ${execution.errorMessage ?: "Unknown"}")
                    ExecutionStatus.SKIPPED_COOLDOWN -> AutomationUiState.ExecutionResult("Skipped: cooldown active")
                    ExecutionStatus.SKIPPED_DISABLED -> AutomationUiState.ExecutionResult("Skipped: automation disabled")
                    else -> AutomationUiState.ExecutionResult("No execution")
                }
            } catch (e: Exception) { _uiState.value = AutomationUiState.Error(e.message ?: "Failed") }
        }
    }

    fun clearUiState() { _uiState.value = AutomationUiState.Idle }

    private fun scheduleTime(rule: AutomationRule) {
        try {
            val obj = org.json.JSONObject(rule.triggerPayload)
            scheduler.scheduleTimeAutomation(rule.id, obj.optInt("hour", 9), obj.optInt("minute", 0), obj.optInt("daysOfWeek", 127))
        } catch (_: Exception) {}
    }

    class Factory(
        private val repository: AutomationRepository,
        private val triggerEngine: TriggerEngine,
        private val scheduler: AutomationScheduler,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AutomationViewModel(repository, triggerEngine, scheduler) as T
        }
    }
}

sealed interface AutomationUiState {
    data object Idle : AutomationUiState
    data object Saving : AutomationUiState
    data object Saved : AutomationUiState
    data object Executing : AutomationUiState
    data class ExecutionResult(val message: String) : AutomationUiState
    data class Error(val message: String) : AutomationUiState
}
