package com.nexus.app.ui.screens.appPicker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nexus.app.data.app.AppLauncher
import com.nexus.app.domain.model.InstalledApp
import com.nexus.app.domain.repository.ContextAppRepository
import com.nexus.app.domain.repository.InstalledAppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Drives the App Picker and app-related operations within Context screens.
 *
 * Responsibilities:
 * - Load installed apps
 * - Filter/search apps
 * - Track selection state during picking
 * - Add/remove apps from a context
 * - Launch apps safely
 */
class AppViewModel(
    private val installedAppRepository: InstalledAppRepository,
    private val contextAppRepository: ContextAppRepository,
    private val appLauncher: AppLauncher,
) : ViewModel() {

    // ── Installed apps list ──────────────────────────────────

    private val _allApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filteredApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val filteredApps: StateFlow<List<InstalledApp>> = _filteredApps.asStateFlow()

    // ── Selection state (for the picker) ─────────────────────

    private val _selectedPackages = MutableStateFlow<Set<String>>(emptySet())
    val selectedPackages: StateFlow<Set<String>> = _selectedPackages.asStateFlow()

    // ── UI feedback ──────────────────────────────────────────

    private val _launchError = MutableStateFlow<String?>(null)
    val launchError: StateFlow<String?> = _launchError.asStateFlow()

    // ── Observe ──────────────────────────────────────────────

    /** Observe apps for a context. Returns a Flow of InstalledApp list. */
    fun observeAppsForContext(contextId: String) =
        contextAppRepository.observeAppsForContext(contextId)

    /** Observe the app count for a context. */
    fun observeAppCount(contextId: String) =
        contextAppRepository.observeAppCount(contextId)

    // ── Commands ─────────────────────────────────────────────

    /** Load installed apps. Call once when the picker opens. */
    fun loadApps(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val apps = installedAppRepository.getInstalledApps(forceRefresh)
                _allApps.value = apps
                applyFilter()
            } catch (_: Exception) {
                _allApps.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Update the search query and re-filter. */
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        applyFilter()
    }

    /** Pre-select apps already in a context (called before opening picker). */
    fun preselectApps(contextId: String) {
        viewModelScope.launch {
            val existing = contextAppRepository.getPackageNames(contextId).toSet()
            _selectedPackages.value = existing
        }
    }

    /** Toggle selection for a package. */
    fun toggleSelection(packageName: String) {
        val current = _selectedPackages.value
        _selectedPackages.value = if (packageName in current) {
            current - packageName
        } else {
            current + packageName
        }
    }

    /** Clear selection state (call after confirming or cancelling). */
    fun clearSelection() {
        _selectedPackages.value = emptySet()
        _searchQuery.value = ""
    }

    /** Persist the current selection to the given context. */
    fun confirmSelection(contextId: String) {
        viewModelScope.launch {
            try {
                contextAppRepository.setApps(contextId, _selectedPackages.value.toList())
            } catch (_: Exception) {
                // Silently handle — UI will reflect actual DB state via Flow
            }
        }
    }

    /** Remove a single app from a context. */
    fun removeAppFromContext(contextId: String, packageName: String) {
        viewModelScope.launch {
            contextAppRepository.removeApp(contextId, packageName)
        }
    }

    /** Add a single app to a context (from detail screen). */
    fun addAppToContext(contextId: String, packageName: String) {
        viewModelScope.launch {
            contextAppRepository.addApp(contextId, packageName)
        }
    }

    /** Launch an app by package name. */
    fun launchApp(packageName: String) {
        if (!appLauncher.isInstalled(packageName)) {
            _launchError.value = "App is no longer installed"
            return
        }
        if (!appLauncher.launch(packageName)) {
            _launchError.value = "Unable to launch app"
        }
    }

    /** Check if a package is still installed. */
    fun isAppInstalled(packageName: String): Boolean = appLauncher.isInstalled(packageName)

    /** Clear the launch error after UI has consumed it. */
    fun clearLaunchError() {
        _launchError.value = null
    }

    // ── Internal ─────────────────────────────────────────────

    private fun applyFilter() {
        val query = _searchQuery.value
        val apps = _allApps.value
        _filteredApps.value = if (query.isBlank()) {
            apps
        } else {
            val lowerQuery = query.lowercase()
            apps.filter { app ->
                app.appName.lowercase().contains(lowerQuery) ||
                    app.packageName.lowercase().contains(lowerQuery)
            }
        }
    }

    // ── Factory ──────────────────────────────────────────────

    class Factory(
        private val installedAppRepository: InstalledAppRepository,
        private val contextAppRepository: ContextAppRepository,
        private val appLauncher: AppLauncher,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
                return AppViewModel(installedAppRepository, contextAppRepository, appLauncher) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
