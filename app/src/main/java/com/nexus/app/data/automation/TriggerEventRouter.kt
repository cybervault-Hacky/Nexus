package com.nexus.app.data.automation

import com.nexus.app.domain.event.EnvironmentEventSourceRegistry
import com.nexus.app.domain.model.automation.TriggerEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

/**
 * Routes events from all sources to the TriggerEngine.
 * Applies deduplication, global safety checks, and isolates source failures.
 */
class TriggerEventRouter(
    private val registry: EnvironmentEventSourceRegistry,
    private val triggerEngine: TriggerEngine,
    private val deduplicator: EventDeduplicator,
    private val automationSettings: AutomationSettings,
) {
    /**
     * Start routing events from all enabled sources.
     * Call from a coroutine scope (e.g., Application.onCreate).
     */
    fun startRouting(scope: CoroutineScope) {
        scope.launch {
            registry.mergedEvents()
                .catch { /* Isolate source failures — don't crash the whole router */ }
                .filter { deduplicator.shouldProcess(it) }
                .filter { automationSettings.isGlobalEnabled() }
                .collect { event ->
                    try {
                        triggerEngine.onTrigger(event)
                    } catch (_: Exception) {
                        // Isolate engine failures
                    }
                }
        }
    }
}

/**
 * Simple settings interface for global automation state.
 * Persisted via DataStore or SharedPreferences.
 */
interface AutomationSettings {
    suspend fun isGlobalEnabled(): Boolean
    suspend fun isEnvironmentTriggersEnabled(): Boolean
    suspend fun isSourceEnabled(sourceId: String): Boolean
    suspend fun setGlobalEnabled(enabled: Boolean)
    suspend fun setEnvironmentTriggersEnabled(enabled: Boolean)
    suspend fun setSourceEnabled(sourceId: String, enabled: Boolean)
}
