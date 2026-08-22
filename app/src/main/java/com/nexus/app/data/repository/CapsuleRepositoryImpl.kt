package com.nexus.app.data.repository

import com.nexus.app.data.local.ActionDao
import com.nexus.app.data.local.CapsuleActionDao
import com.nexus.app.data.local.CapsuleActionEntity
import com.nexus.app.data.local.CapsuleAppDao
import com.nexus.app.data.local.CapsuleAppEntity
import com.nexus.app.data.local.CapsuleDao
import com.nexus.app.data.local.CapsuleEntity
import com.nexus.app.data.local.ContextAppDao
import com.nexus.app.data.local.ContextDao
import com.nexus.app.data.local.NexusDatabase
import com.nexus.app.data.local.toDomain
import com.nexus.app.data.local.toEntity
import com.nexus.app.domain.model.ActionSnapshot
import com.nexus.app.domain.model.ActionType
import com.nexus.app.domain.model.AppSnapshot
import com.nexus.app.domain.model.ContextSnapshot
import com.nexus.app.domain.model.NexusCapsule
import androidx.room.withTransaction
import com.nexus.app.domain.repository.CapsuleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Room-backed implementation of [CapsuleRepository].
 *
 * Capture is atomic (Room transaction) so no partial capsules can exist.
 * Capsules do NOT have a foreign key to contexts — they survive context deletion.
 */
class CapsuleRepositoryImpl(
    private val database: NexusDatabase,
    private val capsuleDao: CapsuleDao,
    private val capsuleAppDao: CapsuleAppDao,
    private val capsuleActionDao: CapsuleActionDao,
    private val contextDao: ContextDao,
    private val contextAppDao: ContextAppDao,
    private val actionDao: ActionDao,
) : CapsuleRepository {

    override fun observeAll(): Flow<List<NexusCapsule>> =
        capsuleDao.observeAll().map { entities ->
            entities.map { entity ->
                val apps = capsuleAppDao.getByCapsule(entity.id).map { it.toDomain() }
                val actions = capsuleActionDao.getByCapsule(entity.id).map { it.toDomain() }
                entity.toDomain(apps, actions)
            }
        }

    override fun observeById(id: String): Flow<NexusCapsule?> =
        capsuleDao.observeById(id).map { entity ->
            entity?.let {
                val apps = capsuleAppDao.getByCapsule(it.id).map { e -> e.toDomain() }
                val actions = capsuleActionDao.getByCapsule(it.id).map { e -> e.toDomain() }
                it.toDomain(apps, actions)
            }
        }

    override suspend fun getById(id: String): NexusCapsule? {
        val entity = capsuleDao.getById(id) ?: return null
        val apps = capsuleAppDao.getByCapsule(entity.id).map { it.toDomain() }
        val actions = capsuleActionDao.getByCapsule(entity.id).map { it.toDomain() }
        return entity.toDomain(apps, actions)
    }

    override suspend fun captureFromContext(contextId: String, name: String, description: String): String {
        // Use a Room transaction for atomicity — no partial capsules
        return database.withTransaction {
            val context = contextDao.getById(contextId) ?: return@withTransaction ""
            val now = System.currentTimeMillis()
            val capsuleId = UUID.randomUUID().toString()

            // 1. Create context snapshot
            val contextSnapshot = ContextSnapshot(
                name = context.name,
                description = context.description,
                iconId = context.iconId,
                accentColor = context.accentColor,
            )

            // 2. Capture app snapshots (frozen data)
            val appEntities = contextAppDao.getByContext(contextId)
            val appSnapshots = appEntities.mapIndexed { index, appEntity ->
                // Resolve app name — best effort
                val appName = try {
                    val pm = database.openHelper.readableDatabase
                    "" // We'll use packageName as fallback
                } catch (_: Exception) { "" }

                CapsuleAppEntity(
                    capsuleId = capsuleId,
                    packageName = appEntity.packageName,
                    appName = appEntity.packageName, // Use packageName as appName
                    position = index,
                )
            }

            // 3. Capture action snapshots (frozen data)
            val actionEntities = actionDao.getByContext(contextId)
            val actionSnapshots = actionEntities.map { action ->
                CapsuleActionEntity(
                    capsuleId = capsuleId,
                    name = action.name,
                    description = action.description,
                    type = action.type,
                    payload = action.payload,
                    isEnabled = action.isEnabled,
                    position = action.position,
                )
            }

            // 4. Insert capsule
            val capsuleEntity = CapsuleEntity(
                id = capsuleId,
                sourceContextId = contextId,
                name = name,
                description = description,
                schemaVersion = NexusCapsule.CAPSULE_SCHEMA_VERSION,
                accentColor = context.accentColor,
                contextSnapshot = org.json.JSONObject().apply {
                    put("name", contextSnapshot.name)
                    put("description", contextSnapshot.description)
                    put("iconId", contextSnapshot.iconId)
                    put("accentColor", contextSnapshot.accentColor)
                }.toString(),
                createdAt = now,
                capturedAt = now,
            )
            capsuleDao.insert(capsuleEntity)

            // 5. Insert app snapshots
            appSnapshots.forEach { capsuleAppDao.insert(it) }

            // 6. Insert action snapshots
            actionSnapshots.forEach { capsuleActionDao.insert(it) }

            capsuleId
        }
    }

    override suspend fun rename(id: String, newName: String) {
        capsuleDao.updateName(id, newName.trim())
    }

    override suspend fun updateDescription(id: String, newDescription: String) {
        capsuleDao.updateDescription(id, newDescription.trim())
    }

    override suspend fun delete(id: String) {
        capsuleDao.deleteById(id) // CASCADE deletes capsule_apps and capsule_actions
    }

    override fun observeForContext(contextId: String): Flow<List<NexusCapsule>> =
        capsuleDao.observeByContext(contextId).map { entities ->
            entities.map { entity ->
                val apps = capsuleAppDao.getByCapsule(entity.id).map { it.toDomain() }
                val actions = capsuleActionDao.getByCapsule(entity.id).map { it.toDomain() }
                entity.toDomain(apps, actions)
            }
        }

    override fun observeCountForContext(contextId: String): Flow<Int> =
        capsuleDao.observeCountByContext(contextId)
}
