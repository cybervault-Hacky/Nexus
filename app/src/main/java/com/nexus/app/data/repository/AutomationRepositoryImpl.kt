package com.nexus.app.data.repository

import com.nexus.app.data.local.AutomationDao
import com.nexus.app.data.local.AutomationExecutionDao
import com.nexus.app.data.local.toDomain
import com.nexus.app.data.local.toEntity
import com.nexus.app.domain.model.automation.AutomationExecution
import com.nexus.app.domain.model.automation.AutomationRule
import com.nexus.app.domain.model.automation.TriggerType
import com.nexus.app.domain.model.smart.AutomationHealth
import com.nexus.app.domain.model.smart.AutomationPriority
import com.nexus.app.domain.repository.AutomationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AutomationRepositoryImpl(
    private val automationDao: AutomationDao,
    private val executionDao: AutomationExecutionDao,
) : AutomationRepository {

    override fun observeAll(): Flow<List<AutomationRule>> =
        automationDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeEnabled(): Flow<List<AutomationRule>> =
        automationDao.observeEnabled().map { list -> list.map { it.toDomain() } }

    override fun observeById(id: String): Flow<AutomationRule?> =
        automationDao.observeById(id).map { it?.toDomain() }

    override suspend fun getById(id: String): AutomationRule? =
        automationDao.getById(id)?.toDomain()

    override suspend fun create(rule: AutomationRule): String {
        automationDao.insert(rule.toEntity())
        return rule.id
    }

    override suspend fun update(rule: AutomationRule) {
        automationDao.update(rule.copy(updatedAt = System.currentTimeMillis()).toEntity())
    }

    override suspend fun delete(id: String) {
        automationDao.deleteById(id)
    }

    override suspend fun setEnabled(id: String, enabled: Boolean) {
        automationDao.setEnabled(id, enabled, System.currentTimeMillis())
    }

    override suspend fun markTriggered(id: String, timestamp: Long) {
        automationDao.updateLastTriggeredAt(id, timestamp)
    }

    override suspend fun getEnabledByTriggerType(type: TriggerType): List<AutomationRule> =
        automationDao.getEnabledByTriggerType(type.name).map { it.toDomain() }

    override fun observeCount(): Flow<Int> = automationDao.observeCount()
    override fun observeEnabledCount(): Flow<Int> = automationDao.observeEnabledCount()

    override suspend fun recordExecution(execution: AutomationExecution) {
        executionDao.insert(execution.toEntity())
    }

    override fun observeRecentExecutions(limit: Int): Flow<List<AutomationExecution>> =
        executionDao.observeRecent(limit).map { list -> list.map { it.toDomain() } }

    override fun observeExecutionsForAutomation(automationId: String): Flow<List<AutomationExecution>> =
        executionDao.observeForAutomation(automationId).map { list -> list.map { it.toDomain() } }

    override suspend fun pruneOldExecutions(keepCount: Int) {
        val count = executionDao.count()
        if (count > keepCount) {
            val cutoff = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
            executionDao.deleteOlderThan(cutoff)
        }
    }

    // Phase 10
    override suspend fun updatePriority(id: String, priority: AutomationPriority) {
        automationDao.updatePriority(id, priority.level, System.currentTimeMillis())
    }

    override suspend fun updateHealth(id: String, health: AutomationHealth) {
        automationDao.updateHealth(id, health.name, System.currentTimeMillis())
    }

    override suspend fun recordSuccess(id: String) {
        automationDao.recordSuccess(id, System.currentTimeMillis())
    }

    override suspend fun recordFailure(id: String) {
        automationDao.recordFailure(id, System.currentTimeMillis())
    }

    override suspend fun getAllRules(): List<AutomationRule> =
        automationDao.getAllRules().map { it.toDomain() }

    override suspend fun getAllExecutions(): List<AutomationExecution> =
        // One-shot read via first emission
        executionDao.observeRecent(1000).map { list -> list.map { it.toDomain() } }.first()
}
