package com.nexus.app.data.repository

import com.nexus.app.data.local.ContextDao
import com.nexus.app.data.local.toDomain
import com.nexus.app.data.local.toEntity
import com.nexus.app.domain.model.NexusContext
import com.nexus.app.domain.repository.ContextRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-backed implementation of [ContextRepository].
 *
 * Keeps all database mapping logic in one place so the domain
 * and UI layers never see Room types.
 */
class ContextRepositoryImpl(
    private val dao: ContextDao,
) : ContextRepository {

    override fun observeAll(): Flow<List<NexusContext>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeById(id: String): Flow<NexusContext?> =
        dao.observeById(id).map { it?.toDomain() }

    override suspend fun getById(id: String): NexusContext? =
        dao.getById(id)?.toDomain()

    override suspend fun insert(context: NexusContext): String {
        dao.insert(context.toEntity())
        return context.id
    }

    override suspend fun update(context: NexusContext) {
        // Verify the context exists before updating
        require(dao.getById(context.id) != null) {
            "Cannot update context '${context.id}': not found"
        }
        dao.update(context.copy(updatedAt = System.currentTimeMillis()).toEntity())
    }

    override suspend fun delete(id: String) {
        dao.deleteById(id)
    }

    override suspend fun activate(id: String) {
        // Enforce single-active invariant at the data layer
        dao.deactivateAll()
        dao.setActive(id, isActive = true, updatedAt = System.currentTimeMillis())
    }

    override suspend fun deactivate(id: String) {
        dao.setActive(id, isActive = false, updatedAt = System.currentTimeMillis())
    }

    override fun observeActive(): Flow<NexusContext?> =
        dao.observeActive().map { it?.toDomain() }
}
