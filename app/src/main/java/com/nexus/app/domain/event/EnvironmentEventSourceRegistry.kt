package com.nexus.app.domain.event

import com.nexus.app.domain.model.automation.TriggerEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge

/**
 * Registry that manages all environment event sources.
 * Merges events from all enabled sources into a single Flow.
 */
class EnvironmentEventSourceRegistry {

    private val sources = mutableMapOf<String, EnvironmentEventSource>()
    private val enabledSources = mutableSetOf<String>()

    fun register(source: EnvironmentEventSource) {
        sources[source.sourceId] = source
    }

    fun startEnabled() {
        sources.values.forEach { source ->
            if (source.sourceId in enabledSources && source.isSupported()) {
                try { source.start() } catch (_: Exception) { }
            }
        }
    }

    fun stopAll() {
        sources.values.forEach { source ->
            try { source.stop() } catch (_: Exception) { }
        }
    }

    fun enable(sourceId: String) {
        enabledSources.add(sourceId)
        sources[sourceId]?.let { if (it.isSupported()) try { it.start() } catch (_: Exception) { } }
    }

    fun disable(sourceId: String) {
        enabledSources.remove(sourceId)
        sources[sourceId]?.let { try { it.stop() } catch (_: Exception) { } }
    }

    fun isEnabled(sourceId: String): Boolean = sourceId in enabledSources

    fun getSupportedSources(): List<EnvironmentEventSource> =
        sources.values.filter { it.isSupported() }

    fun getAllSources(): List<EnvironmentEventSource> = sources.values.toList()

    /** Merge events from all enabled and supported sources. */
    fun mergedEvents(): Flow<TriggerEvent> {
        val activeFlows = sources.values
            .filter { it.sourceId in enabledSources && it.isSupported() }
            .map { it.events() }
        return if (activeFlows.isEmpty()) kotlinx.coroutines.flow.emptyFlow()
        else activeFlows.reduce { acc, flow -> merge(acc, flow) }
    }
}
