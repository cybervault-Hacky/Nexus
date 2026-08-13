package com.nexus.app.ui.screens.contexts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nexus.app.domain.model.NexusContext
import com.nexus.app.domain.repository.ContextRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Drives the Contexts UI.
 *
 * Exposes:
 * - [contexts]: the full list from Room
 * - [activeContext]: the currently active context
 * - [uiState]: loading/error/success feedback for one-off operations
 *
 * Does NOT contain any Compose code.
 */
class ContextViewModel(
    private val repository: ContextRepository,
) : ViewModel() {

    /** All contexts, ordered by most-recently updated. */
    val contexts: StateFlow<List<NexusContext>> = repository
        .observeAll()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** The single active context (or null). */
    val activeContext: StateFlow<NexusContext?> = repository
        .observeActive()
        .catch { emit(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _uiState = MutableStateFlow<ContextUiState>(ContextUiState.Idle)
    val uiState: StateFlow<ContextUiState> = _uiState.asStateFlow()

    // ── Commands ─────────────────────────────────────────────

    fun createContext(
        name: String,
        description: String,
        iconId: String = "grid",
        accentColor: Long = 0xFF6366F1,
    ) {
        val trimmedName = name.trim()
        val trimmedDesc = description.trim()
        val validation = validateInput(trimmedName, trimmedDesc)
        if (validation != null) {
            _uiState.value = ContextUiState.Error(validation)
            return
        }

        viewModelScope.launch {
            _uiState.value = ContextUiState.Saving
            try {
                val now = System.currentTimeMillis()
                val context = NexusContext(
                    id = java.util.UUID.randomUUID().toString(),
                    name = trimmedName,
                    description = trimmedDesc,
                    iconId = iconId,
                    accentColor = accentColor,
                    createdAt = now,
                    updatedAt = now,
                )
                repository.insert(context)
                _uiState.value = ContextUiState.Saved
            } catch (e: Exception) {
                _uiState.value = ContextUiState.Error(
                    e.message ?: "Failed to create context"
                )
            }
        }
    }

    fun updateContext(
        id: String,
        name: String,
        description: String,
        iconId: String,
        accentColor: Long,
    ) {
        val trimmedName = name.trim()
        val trimmedDesc = description.trim()
        val validation = validateInput(trimmedName, trimmedDesc)
        if (validation != null) {
            _uiState.value = ContextUiState.Error(validation)
            return
        }

        viewModelScope.launch {
            _uiState.value = ContextUiState.Saving
            try {
                val existing = repository.getById(id)
                if (existing == null) {
                    _uiState.value = ContextUiState.Error("Context not found")
                    return@launch
                }
                repository.update(
                    existing.copy(
                        name = trimmedName,
                        description = trimmedDesc,
                        iconId = iconId,
                        accentColor = accentColor,
                    )
                )
                _uiState.value = ContextUiState.Saved
            } catch (e: Exception) {
                _uiState.value = ContextUiState.Error(
                    e.message ?: "Failed to update context"
                )
            }
        }
    }

    fun deleteContext(id: String) {
        viewModelScope.launch {
            try {
                repository.delete(id)
                _uiState.value = ContextUiState.Idle
            } catch (e: Exception) {
                _uiState.value = ContextUiState.Error(
                    e.message ?: "Failed to delete context"
                )
            }
        }
    }

    fun duplicateContext(id: String) {
        viewModelScope.launch {
            _uiState.value = ContextUiState.Saving
            try {
                val original = repository.getById(id)
                if (original == null) {
                    _uiState.value = ContextUiState.Error("Context not found")
                    return@launch
                }
                val now = System.currentTimeMillis()
                val copy = original.copy(
                    id = java.util.UUID.randomUUID().toString(),
                    name = "${original.name} Copy",
                    isActive = false,
                    createdAt = now,
                    updatedAt = now,
                )
                repository.insert(copy)
                _uiState.value = ContextUiState.Saved
            } catch (e: Exception) {
                _uiState.value = ContextUiState.Error(
                    e.message ?: "Failed to duplicate context"
                )
            }
        }
    }

    fun activateContext(id: String) {
        viewModelScope.launch {
            try {
                repository.activate(id)
            } catch (e: Exception) {
                _uiState.value = ContextUiState.Error(
                    e.message ?: "Failed to activate context"
                )
            }
        }
    }

    fun deactivateContext(id: String) {
        viewModelScope.launch {
            try {
                repository.deactivate(id)
            } catch (e: Exception) {
                _uiState.value = ContextUiState.Error(
                    e.message ?: "Failed to deactivate context"
                )
            }
        }
    }

    /** Reset transient UI state after the UI has consumed it. */
    fun clearUiState() {
        _uiState.value = ContextUiState.Idle
    }

    // ── Validation ───────────────────────────────────────────

    companion object {
        const val MAX_NAME_LENGTH = 50
        const val MAX_DESCRIPTION_LENGTH = 200

        /** Returns an error message if input is invalid, null otherwise. */
        fun validateInput(name: String, description: String): String? {
            if (name.isBlank()) return "Name cannot be empty"
            if (name.length > MAX_NAME_LENGTH) return "Name is too long (max $MAX_NAME_LENGTH characters)"
            if (description.length > MAX_DESCRIPTION_LENGTH) return "Description is too long (max $MAX_DESCRIPTION_LENGTH characters)"
            return null
        }
    }

    // ── Factory ──────────────────────────────────────────────

    class Factory(private val repository: ContextRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ContextViewModel::class.java)) {
                return ContextViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

/**
 * Transient UI state for one-off events (save success, validation errors).
 * The UI should call [ContextViewModel.clearUiState] after consuming.
 */
sealed interface ContextUiState {
    data object Idle : ContextUiState
    data object Saving : ContextUiState
    data object Saved : ContextUiState
    data class Error(val message: String) : ContextUiState
}
