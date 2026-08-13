package com.nexus.app.data.automation

import com.nexus.app.domain.model.automation.TriggerEvent
import com.nexus.app.domain.model.automation.TriggerType
import com.nexus.app.domain.repository.AutomationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Developer/test tool for generating simulated trigger events.
 * Events flow through the same TriggerEvent pipeline as real events.
 * Never interacts with real system state.
 */
class AutomationSimulator(
    private val triggerEngine: TriggerEngine,
    private val automationRepository: AutomationRepository,
) {
    private val _lastSimulation = MutableStateFlow<SimulatorResult?>(null)
    val lastSimulation: StateFlow<SimulatorResult?> = _lastSimulation.asStateFlow()

    suspend fun simulate(event: TriggerEvent): SimulatorResult {
        val executions = triggerEngine.onTrigger(event)
        val result = SimulatorResult(
            eventType = event.javaClass.simpleName,
            matchedCount = executions.size,
            results = executions.map { it.status.name },
            isSimulated = true,
        )
        _lastSimulation.value = result
        return result
    }

    suspend fun simulateForAutomation(automationId: String): SimulatorResult {
        val rule = automationRepository.getById(automationId)
            ?: return SimulatorResult("UNKNOWN", 0, listOf("Automation not found"), isSimulated = true)
        val event = TriggerEvent.Manual(automationId)
        return simulate(event)
    }
}

data class SimulatorResult(
    val eventType: String,
    val matchedCount: Int,
    val results: List<String>,
    val isSimulated: Boolean,
)
