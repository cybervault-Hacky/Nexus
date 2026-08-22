package com.nexus.app.data.restore

import com.nexus.app.data.local.ActionEntity
import com.nexus.app.data.local.ContextAppEntity
import com.nexus.app.data.local.ContextEntity
import com.nexus.app.data.local.ContextDao
import com.nexus.app.data.local.ContextAppDao
import com.nexus.app.data.local.ActionDao
import androidx.room.withTransaction
import com.nexus.app.data.local.NexusDatabase
import com.nexus.app.domain.model.ActionPayload
import com.nexus.app.domain.model.ActionType
import com.nexus.app.domain.model.ActionValidator
import com.nexus.app.domain.model.NexusCapsule
import com.nexus.app.domain.model.restore.RestoreResult
import com.nexus.app.domain.model.restore.RestoreStatus
import com.nexus.app.domain.repository.InstalledAppRepository
import java.util.UUID

/**
 * Executes capsule restoration into a real Context.
 *
 * Uses Room transactions for atomicity — if anything fails, all changes
 * are rolled back. Does NOT execute any restored actions.
 */
class CapsuleRestoreEngine(
    private val database: NexusDatabase,
    private val contextDao: ContextDao,
    private val contextAppDao: ContextAppDao,
    private val actionDao: ActionDao,
    private val installedAppRepository: InstalledAppRepository,
) {

    /**
     * Restore a capsule as a brand-new Context.
     * Creates a new Context with the capsule's metadata, apps, and actions.
     */
    suspend fun restoreAsNew(capsule: NexusCapsule): RestoreResult {
        val startedAt = System.currentTimeMillis()
        val warnings = mutableListOf<String>()
        val errors = mutableListOf<String>()

        return try {
            database.withTransaction {
                val contextId = UUID.randomUUID().toString()
                val now = System.currentTimeMillis()
                val snapshot = capsule.contextSnapshot

                // Generate unique context name
                val baseName = snapshot?.name ?: capsule.name
                val contextName = generateUniqueName(baseName, contextId)

                // 1. Create the context
                val context = ContextEntity(
                    id = contextId,
                    name = contextName,
                    description = snapshot?.description ?: capsule.description,
                    iconId = snapshot?.iconId ?: "grid",
                    appCount = 0, // Will be updated below
                    actionCount = 0, // Will be updated below
                    isActive = false, // Restored contexts start inactive
                    accentColor = snapshot?.accentColor ?: capsule.accentColor,
                    createdAt = now,
                    updatedAt = now,
                )
                contextDao.insert(context)

                // 2. Restore apps (skip missing)
                var appsRestored = 0
                var appsSkipped = 0
                for ((index, app) in capsule.appSnapshots.withIndex()) {
                    val installed = installedAppRepository.resolveApp(app.packageName)
                    if (installed != null) {
                        contextAppDao.insert(
                            ContextAppEntity(contextId = contextId, packageName = app.packageName)
                        )
                        appsRestored++
                    } else {
                        appsSkipped++
                        warnings.add("App unavailable: ${app.appName} (${app.packageName})")
                    }
                }

                // 3. Restore actions (validate each one)
                var actionsRestored = 0
                var actionsSkipped = 0
                for (action in capsule.actionSnapshots.sortedBy { it.position }) {
                    val validationError = validateForRestore(action, installedAppRepository)
                    if (validationError == null) {
                        // Determine if action should be enabled
                        val shouldEnable = if (action.type == ActionType.OPEN_APP) {
                            val parsed = ActionPayload.fromJson(action.type, action.payload)
                            if (parsed is ActionPayload.OpenApp) {
                                installedAppRepository.resolveApp(parsed.packageName) != null
                            } else action.isEnabled
                        } else action.isEnabled

                        actionDao.insert(
                            ActionEntity(
                                id = UUID.randomUUID().toString(),
                                contextId = contextId,
                                name = action.name,
                                description = action.description,
                                type = action.type.name,
                                payload = action.payload,
                                isEnabled = shouldEnable,
                                position = action.position,
                                createdAt = now,
                                updatedAt = now,
                            )
                        )
                        actionsRestored++
                    } else {
                        actionsSkipped++
                        warnings.add("Action skipped: ${action.name} — $validationError")
                    }
                }

                // 4. Update counts
                val updatedContext = context.copy(
                    appCount = appsRestored,
                    actionCount = actionsRestored,
                )
                contextDao.update(updatedContext)

                // 5. Build result
                val status = when {
                    appsSkipped == 0 && actionsSkipped == 0 -> RestoreStatus.SUCCESS
                    appsRestored > 0 || actionsRestored > 0 -> RestoreStatus.PARTIAL
                    else -> RestoreStatus.FAILED
                }

                RestoreResult(
                    status = status,
                    contextId = contextId,
                    appsRestored = appsRestored,
                    appsSkipped = appsSkipped,
                    actionsRestored = actionsRestored,
                    actionsSkipped = actionsSkipped,
                    warnings = warnings,
                    errors = errors,
                    startedAt = startedAt,
                    completedAt = System.currentTimeMillis(),
                )
            }
        } catch (e: Exception) {
            RestoreResult(
                status = RestoreStatus.FAILED,
                contextId = null,
                appsRestored = 0,
                appsSkipped = 0,
                actionsRestored = 0,
                actionsSkipped = 0,
                warnings = warnings,
                errors = listOf(e.message ?: "Unknown error"),
                startedAt = startedAt,
                completedAt = System.currentTimeMillis(),
            )
        }
    }

    /**
     * Restore a capsule into an existing Context.
     * Replaces the context's apps and actions with the capsule snapshot.
     */
    suspend fun restoreIntoExisting(
        capsule: NexusCapsule,
        targetContextId: String,
    ): RestoreResult {
        val startedAt = System.currentTimeMillis()
        val warnings = mutableListOf<String>()
        val errors = mutableListOf<String>()

        return try {
            database.withTransaction {
                val now = System.currentTimeMillis()
                val snapshot = capsule.contextSnapshot

                // 1. Verify target exists
                val existingContext = contextDao.getById(targetContextId)
                    ?: return@withTransaction RestoreResult(
                        status = RestoreStatus.FAILED,
                        contextId = null,
                        appsRestored = 0, appsSkipped = 0,
                        actionsRestored = 0, actionsSkipped = 0,
                        warnings = warnings,
                        errors = listOf("Target context not found"),
                        startedAt = startedAt,
                        completedAt = System.currentTimeMillis(),
                    )

                // 2. Clear existing apps and actions
                contextAppDao.deleteAllForContext(targetContextId)
                actionDao.deleteAllForContext(targetContextId)

                // 3. Update context metadata
                val newName = snapshot?.name ?: capsule.name
                val newDesc = snapshot?.description ?: capsule.description
                contextDao.update(
                    existingContext.copy(
                        name = newName,
                        description = newDesc,
                        iconId = snapshot?.iconId ?: existingContext.iconId,
                        accentColor = snapshot?.accentColor ?: existingContext.accentColor,
                        updatedAt = now,
                    )
                )

                // 4. Restore apps
                var appsRestored = 0
                var appsSkipped = 0
                for (app in capsule.appSnapshots) {
                    val installed = installedAppRepository.resolveApp(app.packageName)
                    if (installed != null) {
                        contextAppDao.insert(
                            ContextAppEntity(contextId = targetContextId, packageName = app.packageName)
                        )
                        appsRestored++
                    } else {
                        appsSkipped++
                        warnings.add("App unavailable: ${app.appName} (${app.packageName})")
                    }
                }

                // 5. Restore actions
                var actionsRestored = 0
                var actionsSkipped = 0
                for (action in capsule.actionSnapshots.sortedBy { it.position }) {
                    val validationError = validateForRestore(action, installedAppRepository)
                    if (validationError == null) {
                        val shouldEnable = if (action.type == ActionType.OPEN_APP) {
                            val parsed = ActionPayload.fromJson(action.type, action.payload)
                            if (parsed is ActionPayload.OpenApp) {
                                installedAppRepository.resolveApp(parsed.packageName) != null
                            } else action.isEnabled
                        } else action.isEnabled

                        actionDao.insert(
                            ActionEntity(
                                id = UUID.randomUUID().toString(),
                                contextId = targetContextId,
                                name = action.name,
                                description = action.description,
                                type = action.type.name,
                                payload = action.payload,
                                isEnabled = shouldEnable,
                                position = action.position,
                                createdAt = now,
                                updatedAt = now,
                            )
                        )
                        actionsRestored++
                    } else {
                        actionsSkipped++
                        warnings.add("Action skipped: ${action.name} — $validationError")
                    }
                }

                // 6. Update counts
                contextDao.update(
                    existingContext.copy(
                        name = newName,
                        description = newDesc,
                        iconId = snapshot?.iconId ?: existingContext.iconId,
                        accentColor = snapshot?.accentColor ?: existingContext.accentColor,
                        appCount = appsRestored,
                        actionCount = actionsRestored,
                        updatedAt = now,
                    )
                )

                val status = when {
                    appsSkipped == 0 && actionsSkipped == 0 -> RestoreStatus.SUCCESS
                    appsRestored > 0 || actionsRestored > 0 -> RestoreStatus.PARTIAL
                    else -> RestoreStatus.FAILED
                }

                RestoreResult(
                    status = status,
                    contextId = targetContextId,
                    appsRestored = appsRestored,
                    appsSkipped = appsSkipped,
                    actionsRestored = actionsRestored,
                    actionsSkipped = actionsSkipped,
                    warnings = warnings,
                    errors = errors,
                    startedAt = startedAt,
                    completedAt = System.currentTimeMillis(),
                )
            }
        } catch (e: Exception) {
            RestoreResult(
                status = RestoreStatus.FAILED,
                contextId = targetContextId,
                appsRestored = 0, appsSkipped = 0,
                actionsRestored = 0, actionsSkipped = 0,
                warnings = warnings,
                errors = listOf(e.message ?: "Unknown error"),
                startedAt = startedAt,
                completedAt = System.currentTimeMillis(),
            )
        }
    }

    /**
     * Generate a unique context name to avoid overwriting existing contexts.
     * If "Name" exists, tries "Name (Restored)", then "Name (Restored 2)", etc.
     */
    private suspend fun generateUniqueName(baseName: String, excludeId: String): String {
        val existing = contextDao.getByName(baseName)
        if (existing == null || existing.id == excludeId) return baseName

        var suffix = ""
        var counter = 2
        while (true) {
            val candidate = "$baseName (Restored$suffix)"
            val conflict = contextDao.getByName(candidate)
            if (conflict == null) return candidate
            suffix = " $counter"
            counter++
            if (counter > 100) return "${baseName}_${UUID.randomUUID().toString().take(6)}"
        }
    }
}

/**
 * Validate an action snapshot before restoration.
 * Returns error message if invalid, null if valid.
 */
private suspend fun validateForRestore(
    action: com.nexus.app.domain.model.ActionSnapshot,
    installedAppRepository: InstalledAppRepository,
): String? {
    val nameError = ActionValidator.validateName(action.name)
    if (nameError != null) return nameError

    val payloadError = ActionValidator.validatePayload(action.type, action.payload)
    if (payloadError != null) return payloadError

    // For OPEN_APP, check package availability
    if (action.type == ActionType.OPEN_APP) {
        val parsed = ActionPayload.fromJson(action.type, action.payload)
        if (parsed is ActionPayload.OpenApp) {
            val installed = installedAppRepository.resolveApp(parsed.packageName)
            if (installed == null) return "App not installed"
        }
    }

    return null
}
