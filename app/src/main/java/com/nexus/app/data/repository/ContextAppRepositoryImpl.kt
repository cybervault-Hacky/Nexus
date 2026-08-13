package com.nexus.app.data.repository

import com.nexus.app.data.local.ContextAppDao
import com.nexus.app.data.local.ContextAppEntity
import com.nexus.app.data.local.ContextDao
import com.nexus.app.domain.model.InstalledApp
import com.nexus.app.domain.repository.ContextAppRepository
import com.nexus.app.domain.repository.InstalledAppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-backed implementation of [ContextAppRepository].
 *
 * Delegates to [InstalledAppRepository] for resolving package names
 * to human-readable [InstalledApp] instances.
 *
 * Also updates the denormalized [ContextEntity.appCount] column
 * so the ContextCard can display real counts without extra queries.
 */
class ContextAppRepositoryImpl(
    private val contextAppDao: ContextAppDao,
    private val contextDao: ContextDao,
    private val installedAppRepository: InstalledAppRepository,
) : ContextAppRepository {

    override fun observeAppsForContext(contextId: String): Flow<List<InstalledApp>> {
        return contextAppDao.observeByContext(contextId).map { entities ->
            entities.mapNotNull { entity ->
                installedAppRepository.resolveApp(entity.packageName)
            }
        }
    }

    override fun observeAppCount(contextId: String): Flow<Int> =
        contextAppDao.observeCount(contextId)

    override suspend fun addApp(contextId: String, packageName: String) {
        // IGNORE on conflict prevents duplicate associations
        contextAppDao.insert(
            ContextAppEntity(contextId = contextId, packageName = packageName)
        )
        syncAppCount(contextId)
    }

    override suspend fun removeApp(contextId: String, packageName: String) {
        contextAppDao.delete(contextId, packageName)
        syncAppCount(contextId)
    }

    override suspend fun setApps(contextId: String, packageNames: List<String>) {
        contextAppDao.clearContext(contextId)
        packageNames.forEach { pkg ->
            contextAppDao.insert(ContextAppEntity(contextId = contextId, packageName = pkg))
        }
        syncAppCount(contextId)
    }

    override suspend fun isAppInContext(contextId: String, packageName: String): Boolean =
        contextAppDao.count(contextId, packageName) > 0

    override suspend fun getPackageNames(contextId: String): List<String> =
        contextAppDao.getPackageNames(contextId)

    override suspend fun deleteAllForContext(contextId: String) {
        contextAppDao.deleteAllForContext(contextId)
    }

    /**
     * Sync the denormalized appCount on the ContextEntity so
     * the UI can read it without an additional query.
     */
    private suspend fun syncAppCount(contextId: String) {
        val count = contextAppDao.getPackageNames(contextId).size
        val entity = contextDao.getById(contextId) ?: return
        contextDao.update(entity.copy(appCount = count, updatedAt = System.currentTimeMillis()))
    }
}
