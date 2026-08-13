package com.nexus.app.domain.repository

import com.nexus.app.domain.model.automation.AutomationExecution
import com.nexus.app.domain.model.automation.AutomationRule
import com.nexus.app.domain.model.automation.TriggerType
import kotlinx.coroutines.flow.Flow

interface AutomationRepository {
    fun observeAll(): Flow<List<AutomationRule>>
    fun observeEnabled(): Flow<List<AutomationRule>>
    fun observeById(id: String): Flow<AutomationRule?>
    suspend fun getById(id: String): AutomationRule?
    suspend fun create(rule: AutomationRule): String
    suspend fun update(rule: AutomationRule)
    suspend fun delete(id: String)
    suspend fun setEnabled(id: String, enabled: Boolean)
    suspend fun markTriggered(id: String, timestamp: Long)
    suspend fun getEnabledByTriggerType(type: TriggerType): List<AutomationRule>
    fun observeCount(): Flow<Int>
    fun observeEnabledCount(): Flow<Int>
    suspend fun recordExecution(execution: AutomationExecution)
    fun observeRecentExecutions(limit: Int = 50): Flow<List<AutomationExecution>>
    fun observeExecutionsForAutomation(automationId: String): Flow<List<AutomationExecution>>
    suspend fun pruneOldExecutions(keepCount: Int = 100)
}
