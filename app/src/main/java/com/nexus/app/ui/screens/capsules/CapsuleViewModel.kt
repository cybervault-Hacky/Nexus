package com.nexus.app.ui.screens.capsules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nexus.app.domain.model.NexusCapsule
import com.nexus.app.domain.repository.CapsuleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Drives the Capsules UI.
 */
class CapsuleViewModel(
    private val capsuleRepository: CapsuleRepository,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow(CapsuleSortOrder.NEWEST)
    val sortOrder: StateFlow<CapsuleSortOrder> = _sortOrder.asStateFlow()

    /** All capsules from the repository, combined with search and sort. */
    val capsules: StateFlow<List<NexusCapsule>> = combine(
        capsuleRepository.observeAll(),
        _searchQuery,
        _sortOrder,
    ) { allCapsules, query, sort ->
        val filtered = if (query.isBlank()) allCapsules
        else {
            val lowerQuery = query.lowercase()
            allCapsules.filter { capsule ->
                capsule.name.lowercase().contains(lowerQuery) ||
                    capsule.description.lowercase().contains(lowerQuery) ||
                    capsule.contextSnapshot?.name?.lowercase()?.contains(lowerQuery) == true
            }
        }
        when (sort) {
            CapsuleSortOrder.NEWEST -> filtered.sortedByDescending { it.capturedAt }
            CapsuleSortOrder.OLDEST -> filtered.sortedBy { it.capturedAt }
            CapsuleSortOrder.NAME_ASC -> filtered.sortedBy { it.name.lowercase() }
        }
    }
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _uiState = MutableStateFlow<CapsuleUiState>(CapsuleUiState.Idle)
    val uiState: StateFlow<CapsuleUiState> = _uiState.asStateFlow()

    // ── Commands ─────────────────────────────────────────────

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onSortOrderChanged(order: CapsuleSortOrder) {
        _sortOrder.value = order
    }

    fun captureFromContext(contextId: String, name: String, description: String) {
        val trimmedName = name.trim()
        val trimmedDesc = description.trim()
        if (trimmedName.isBlank()) {
            _uiState.value = CapsuleUiState.Error("Name cannot be empty")
            return
        }
        if (trimmedName.length > 60) {
            _uiState.value = CapsuleUiState.Error("Name is too long (max 60 characters)")
            return
        }
        if (trimmedDesc.length > 200) {
            _uiState.value = CapsuleUiState.Error("Description is too long (max 200 characters)")
            return
        }

        viewModelScope.launch {
            _uiState.value = CapsuleUiState.Saving
            try {
                val capsuleId = capsuleRepository.captureFromContext(contextId, trimmedName, trimmedDesc)
                if (capsuleId.isNotEmpty()) {
                    _uiState.value = CapsuleUiState.Saved
                } else {
                    _uiState.value = CapsuleUiState.Error("Context not found")
                }
            } catch (e: Exception) {
                _uiState.value = CapsuleUiState.Error(e.message ?: "Failed to capture capsule")
            }
        }
    }

    fun renameCapsule(id: String, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) {
            _uiState.value = CapsuleUiState.Error("Name cannot be empty")
            return
        }
        if (trimmed.length > 60) {
            _uiState.value = CapsuleUiState.Error("Name is too long (max 60 characters)")
            return
        }
        viewModelScope.launch {
            try {
                capsuleRepository.rename(id, trimmed)
            } catch (e: Exception) {
                _uiState.value = CapsuleUiState.Error(e.message ?: "Failed to rename capsule")
            }
        }
    }

    fun updateDescription(id: String, newDescription: String) {
        val trimmed = newDescription.trim()
        if (trimmed.length > 200) {
            _uiState.value = CapsuleUiState.Error("Description is too long (max 200 characters)")
            return
        }
        viewModelScope.launch {
            try {
                capsuleRepository.updateDescription(id, trimmed)
            } catch (e: Exception) {
                _uiState.value = CapsuleUiState.Error(e.message ?: "Failed to update description")
            }
        }
    }

    fun deleteCapsule(id: String) {
        viewModelScope.launch {
            try {
                capsuleRepository.delete(id)
                _uiState.value = CapsuleUiState.Idle
            } catch (e: Exception) {
                _uiState.value = CapsuleUiState.Error(e.message ?: "Failed to delete capsule")
            }
        }
    }

    fun clearUiState() {
        _uiState.value = CapsuleUiState.Idle
    }

    // ── Factory ──────────────────────────────────────────────

    class Factory(private val capsuleRepository: CapsuleRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CapsuleViewModel::class.java)) {
                return CapsuleViewModel(capsuleRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

sealed interface CapsuleUiState {
    data object Idle : CapsuleUiState
    data object Saving : CapsuleUiState
    data object Saved : CapsuleUiState
    data class Error(val message: String) : CapsuleUiState
}

enum class CapsuleSortOrder {
    NEWEST,
    OLDEST,
    NAME_ASC,
}
