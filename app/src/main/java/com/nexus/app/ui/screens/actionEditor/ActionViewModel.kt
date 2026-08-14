package com.nexus.app.ui.screens.actionEditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nexus.app.data.action.WorkflowExecutor
import com.nexus.app.domain.model.ActionType
import com.nexus.app.domain.model.ActionValidator
import com.nexus.app.domain.model.NexusAction
import com.nexus.app.domain.model.WorkflowState
import com.nexus.app.domain.repository.ActionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Drives the Action UI (action list in detail, action editor, workflow execution).
 */
class ActionViewModel(
    private val actionRepository: ActionRepository,
    private val workflowExecutor: WorkflowExecutor,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ActionUiState>(ActionUiState.Idle)
    val uiState: StateFlow<ActionUiState> = _uiState.asStateFlow()

    val workflowState: StateFlow<WorkflowState> = workflowExecutor.state

    // ── Observe ──────────────────────────────────────────────

    fun observeActionsForContext(contextId: String) =
        actionRepository.observeActionsForContext(contextId)

    fun observeActionCount(contextId: String) =
        actionRepository.observeActionCount(contextId)

    /** One-shot load of an action by id — result delivered via [onResult]. */
    fun getById(id: String, onResult: (NexusAction?) -> Unit) {
        viewModelScope.launch {
            val action = try {
                actionRepository.getById(id)
            } catch (_: Exception) {
                null
            }
            onResult(action)
        }
    }

    // ── Commands ─────────────────────────────────────────────

    fun createAction(
        contextId: String,
        name: String,
        description: String,
        type: ActionType,
        payload: String,
    ) {
        val trimmedName = name.trim()
        val trimmedDesc = description.trim()
        val validation = ActionValidator.validate(trimmedName, trimmedDesc, type, payload)
        if (validation != null) {
            _uiState.value = ActionUiState.Error(validation)
            return
        }

        viewModelScope.launch {
            _uiState.value = ActionUiState.Saving
            try {
                val now = System.currentTimeMillis()
                val existingActions = actionRepository.getActionsForContext(contextId)
                val maxPosition = if (existingActions.isEmpty()) 0
                    else existingActions.maxOf { it.position } + 1
                val action = NexusAction(
                    id = java.util.UUID.randomUUID().toString(),
                    contextId = contextId,
                    name = trimmedName,
                    description = trimmedDesc,
                    type = type,
                    payload = payload,
                    position = maxPosition,
                    createdAt = now,
                    updatedAt = now,
                )
                actionRepository.insert(action)
                _uiState.value = ActionUiState.Saved
            } catch (e: Exception) {
                _uiState.value = ActionUiState.Error(e.message ?: "Failed to create action")
            }
        }
    }

    fun updateAction(
        id: String,
        name: String,
        description: String,
        type: ActionType,
        payload: String,
    ) {
        val trimmedName = name.trim()
        val trimmedDesc = description.trim()
        val validation = ActionValidator.validate(trimmedName, trimmedDesc, type, payload)
        if (validation != null) {
            _uiState.value = ActionUiState.Error(validation)
            return
        }

        viewModelScope.launch {
            _uiState.value = ActionUiState.Saving
            try {
                val existing = actionRepository.getById(id)
                if (existing == null) {
                    _uiState.value = ActionUiState.Error("Action not found")
                    return@launch
                }
                actionRepository.update(
                    existing.copy(
                        name = trimmedName,
                        description = trimmedDesc,
                        type = type,
                        payload = payload,
                    )
                )
                _uiState.value = ActionUiState.Saved
            } catch (e: Exception) {
                _uiState.value = ActionUiState.Error(e.message ?: "Failed to update action")
            }
        }
    }

    fun deleteAction(id: String) {
        viewModelScope.launch {
            try {
                actionRepository.delete(id)
                _uiState.value = ActionUiState.Idle
            } catch (e: Exception) {
                _uiState.value = ActionUiState.Error(e.message ?: "Failed to delete action")
            }
        }
    }

    fun toggleEnabled(id: String, isEnabled: Boolean) {
        viewModelScope.launch {
            try {
                actionRepository.setEnabled(id, isEnabled)
            } catch (_: Exception) { }
        }
    }

    fun moveUp(contextId: String, actionId: String) {
        viewModelScope.launch {
            val actions = actionRepository.getActionsForContext(contextId).sortedBy { it.position }
            val index = actions.indexOfFirst { it.id == actionId }
            if (index <= 0) return@launch
            val mutable = actions.toMutableList()
            val temp = mutable[index]
            mutable[index] = mutable[index - 1]
            mutable[index - 1] = temp
            actionRepository.reorder(contextId, mutable.map { it.id })
        }
    }

    fun moveDown(contextId: String, actionId: String) {
        viewModelScope.launch {
            val actions = actionRepository.getActionsForContext(contextId).sortedBy { it.position }
            val index = actions.indexOfFirst { it.id == actionId }
            if (index < 0 || index >= actions.size - 1) return@launch
            val mutable = actions.toMutableList()
            val temp = mutable[index]
            mutable[index] = mutable[index + 1]
            mutable[index + 1] = temp
            actionRepository.reorder(contextId, mutable.map { it.id })
        }
    }

    /** Execute a single action. */
    fun executeAction(action: NexusAction) {
        viewModelScope.launch {
            _uiState.value = ActionUiState.Executing
            try {
                val result = workflowExecutor.execute(listOf(action))
                if (result.overallSuccess) {
                    _uiState.value = ActionUiState.ExecutionResult("Action completed")
                } else {
                    val error = result.actionResults.firstOrNull {
                        it is com.nexus.app.domain.model.ActionResult.Failed
                    }
                    val errorMsg = (error as? com.nexus.app.domain.model.ActionResult.Failed)?.error
                        ?: "Unknown error"
                    _uiState.value = ActionUiState.ExecutionResult("Failed: $errorMsg")
                }
            } catch (e: Exception) {
                _uiState.value = ActionUiState.ExecutionResult("Failed: ${e.message}")
            }
        }
    }

    /** Run the full workflow for a context. */
    fun runWorkflow(contextId: String) {
        viewModelScope.launch {
            workflowExecutor.reset()
            val actions = actionRepository.getActionsForContext(contextId)
            if (actions.isEmpty()) {
                _uiState.value = ActionUiState.ExecutionResult("No actions to run")
                return@launch
            }
            workflowExecutor.execute(actions)
        }
    }

    fun cancelWorkflow() {
        workflowExecutor.cancel()
    }

    fun resetWorkflow() {
        workflowExecutor.reset()
    }

    fun clearUiState() {
        _uiState.value = ActionUiState.Idle
    }

    // ── Factory ──────────────────────────────────────────────

    class Factory(
        private val actionRepository: ActionRepository,
        private val workflowExecutor: WorkflowExecutor,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ActionViewModel::class.java)) {
                return ActionViewModel(actionRepository, workflowExecutor) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

/** Transient UI state for action operations. */
sealed interface ActionUiState {
    data object Idle : ActionUiState
    data object Saving : ActionUiState
    data object Saved : ActionUiState
    data object Executing : ActionUiState
    data class ExecutionResult(val message: String) : ActionUiState
    data class Error(val message: String) : ActionUiState
}
