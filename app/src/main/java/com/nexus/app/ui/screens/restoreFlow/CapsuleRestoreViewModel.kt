package com.nexus.app.ui.screens.restoreFlow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nexus.app.data.restore.CapsuleRestoreEngine
import com.nexus.app.data.restore.RestorePreviewEngine
import com.nexus.app.domain.model.NexusAction
import com.nexus.app.domain.model.NexusCapsule
import com.nexus.app.domain.model.NexusContext
import com.nexus.app.domain.model.restore.RestorePreview
import com.nexus.app.domain.model.restore.RestoreResult
import com.nexus.app.domain.model.restore.RestoreStatus
import com.nexus.app.domain.model.restore.RestoreTarget
import com.nexus.app.domain.repository.CapsuleRepository
import com.nexus.app.domain.repository.ContextAppRepository
import com.nexus.app.domain.repository.ContextRepository
import com.nexus.app.domain.repository.ActionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Drives the multi-step capsule restore flow:
 * Target Selection → Preview → Restore → Result
 */
class CapsuleRestoreViewModel(
    private val capsuleRepository: CapsuleRepository,
    private val contextRepository: ContextRepository,
    private val contextAppRepository: ContextAppRepository,
    private val actionRepository: ActionRepository,
    private val previewEngine: RestorePreviewEngine,
    private val restoreEngine: CapsuleRestoreEngine,
) : ViewModel() {

    private val _uiState = MutableStateFlow<RestoreUiState>(RestoreUiState.Idle)
    val uiState: StateFlow<RestoreUiState> = _uiState.asStateFlow()

    private val _contexts = MutableStateFlow<List<NexusContext>>(emptyList())
    val contexts: StateFlow<List<NexusContext>> = _contexts.asStateFlow()

    private var capsule: NexusCapsule? = null
    private var selectedTarget = RestoreTarget.CREATE_NEW
    private var selectedContextId: String? = null

    // ── Flow steps ───────────────────────────────────────────

    fun startRestore(capsuleId: String) {
        viewModelScope.launch {
            _uiState.value = RestoreUiState.Loading
            try {
                capsule = capsuleRepository.getById(capsuleId)
                if (capsule == null) {
                    _uiState.value = RestoreUiState.Error("Capsule not found")
                    return@launch
                }
                // Load available contexts (one-shot)
                val ctxList = mutableListOf<NexusContext>()
                contextRepository.observeAll().collect { list ->
                    ctxList.addAll(list)
                    return@collect
                }
                _contexts.value = ctxList
                _uiState.value = RestoreUiState.Idle
            } catch (e: Exception) {
                _uiState.value = RestoreUiState.Error(e.message ?: "Failed to load capsule")
            }
        }
    }

    fun selectTarget(target: RestoreTarget) {
        selectedTarget = target
    }

    fun selectExistingContext(contextId: String) {
        selectedContextId = contextId
    }

    fun buildPreview() {
        val cap = capsule ?: return
        viewModelScope.launch {
            _uiState.value = RestoreUiState.Loading
            try {
                val preview = when (selectedTarget) {
                    RestoreTarget.CREATE_NEW -> {
                        previewEngine.previewNewContext(cap)
                    }
                    RestoreTarget.REPLACE_EXISTING -> {
                        val ctxId = selectedContextId
                        if (ctxId == null) {
                            _uiState.value = RestoreUiState.Error("No context selected")
                            return@launch
                        }
                        val targetCtx = contextRepository.getById(ctxId)
                        if (targetCtx == null) {
                            _uiState.value = RestoreUiState.Error("Context not found")
                            return@launch
                        }
                        // Get current apps and actions for the target context
                        val targetApps = mutableListOf<String>()
                        contextAppRepository.observeAppsForContext(ctxId).collect { apps ->
                            targetApps.addAll(apps.map { it.packageName })
                            return@collect
                        }
                        val targetActions = mutableListOf<NexusAction>()
                        actionRepository.observeActionsForContext(ctxId).collect { actions ->
                            targetActions.addAll(actions)
                            return@collect
                        }
                        previewEngine.previewExistingContext(cap, targetCtx, targetApps, targetActions)
                    }
                }
                _uiState.value = RestoreUiState.PreviewReady(preview)
            } catch (e: Exception) {
                _uiState.value = RestoreUiState.Error(e.message ?: "Failed to build preview")
            }
        }
    }

    fun executeRestore() {
        val cap = capsule ?: return
        viewModelScope.launch {
            _uiState.value = RestoreUiState.Restoring
            try {
                val result = when (selectedTarget) {
                    RestoreTarget.CREATE_NEW -> restoreEngine.restoreAsNew(cap)
                    RestoreTarget.REPLACE_EXISTING -> {
                        val ctxId = selectedContextId
                        if (ctxId == null) {
                            _uiState.value = RestoreUiState.Error("No context selected")
                            return@launch
                        }
                        restoreEngine.restoreIntoExisting(cap, ctxId)
                    }
                }
                _uiState.value = when (result.status) {
                    RestoreStatus.SUCCESS, RestoreStatus.PARTIAL -> RestoreUiState.Success(result)
                    else -> RestoreUiState.Error(result.errors.firstOrNull() ?: "Restoration failed")
                }
            } catch (e: Exception) {
                _uiState.value = RestoreUiState.Error(e.message ?: "Restoration failed")
            }
        }
    }

    fun reset() {
        _uiState.value = RestoreUiState.Idle
        capsule = null
        selectedTarget = RestoreTarget.CREATE_NEW
        selectedContextId = null
    }

    class Factory(
        private val capsuleRepository: CapsuleRepository,
        private val contextRepository: ContextRepository,
        private val contextAppRepository: ContextAppRepository,
        private val actionRepository: ActionRepository,
        private val previewEngine: RestorePreviewEngine,
        private val restoreEngine: CapsuleRestoreEngine,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CapsuleRestoreViewModel::class.java)) {
                return CapsuleRestoreViewModel(capsuleRepository, contextRepository, contextAppRepository, actionRepository, previewEngine, restoreEngine) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

sealed interface RestoreUiState {
    data object Idle : RestoreUiState
    data object Loading : RestoreUiState
    data class PreviewReady(val preview: RestorePreview) : RestoreUiState
    data object Restoring : RestoreUiState
    data class Success(val result: RestoreResult) : RestoreUiState
    data class Error(val message: String) : RestoreUiState
}
