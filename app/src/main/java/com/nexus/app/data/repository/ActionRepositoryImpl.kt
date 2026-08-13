package com.nexus.app.data.repository

import com.nexus.app.data.local.ActionDao
import com.nexus.app.data.local.ContextDao
import com.nexus.app.data.local.toDomain
import com.nexus.app.data.local.toEntity
import com.nexus.app.domain.model.NexusAction
import com.nexus.app.domain.repository.ActionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-backed implementation of [ActionRepository].
 * Also syncs the denormalized actionCount on the parent ContextEntity.
 */
class ActionRepositoryImpl(
    private val actionDao: ActionDao,
    private val contextDao: ContextDao,
) : ActionRepository {

    override fun observeActionsForContext(contextId: String): Flow<List<NexusAction>> =
        actionDao.observeByContext(contextId).map { entities -> entities.map { it.toDomain() } }

    override fun observeById(id: String): Flow<NexusAction?> =
        actionDao.observeById(id).map { it?.toDomain() }

    override suspend fun getById(id: String): NexusAction? =
        actionDao.getById(id)?.toDomain()

    override suspend fun insert(action: NexusAction): String {
        actionDao.insert(action.toEntity())
        syncActionCount(action.contextId)
        return action.id
    }

    override suspend fun update(action: NexusAction) {
        actionDao.update(action.copy(updatedAt = System.currentTimeMillis()).toEntity())
    }

    override suspend fun delete(id: String) {
        val entity = actionDao.getById(id)
        actionDao.deleteById(id)
        entity?.let { syncActionCount(it.contextId) }
    }

    override suspend fun deleteAllForContext(contextId: String) {
        actionDao.deleteAllForContext(contextId)
        syncActionCount(contextId)
    }

    override suspend fun setEnabled(id: String, isEnabled: Boolean) {
        actionDao.setEnabled(id, isEnabled, System.currentTimeMillis())
    }

    override suspend fun reorder(contextId: String, orderedIds: List<String>) {
        val now = System.currentTimeMillis()
        orderedIds.forEachIndexed { index, id ->
            actionDao.setPosition(id, index, now)
        }
    }

    override fun observeActionCount(contextId: String): Flow<Int> =
        actionDao.observeCount(contextId)

    override suspend fun getActionsForContext(contextId: String): List<NexusAction> =
        actionDao.getByContext(contextId).map { it.toDomain() }

    /** Sync the denormalized actionCount on the ContextEntity. */
    private suspend fun syncActionCount(contextId: String) {
        val count = actionDao.getCount(contextId)
        val entity = contextDao.getById(contextId) ?: return
        contextDao.update(entity.copy(actionCount = count, updatedAt = System.currentTimeMillis()))
    }
}
