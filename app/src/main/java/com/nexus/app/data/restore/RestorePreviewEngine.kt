package com.nexus.app.data.restore

import com.nexus.app.domain.model.ActionSnapshot
import com.nexus.app.domain.model.AppSnapshot
import com.nexus.app.domain.model.NexusAction
import com.nexus.app.domain.model.NexusCapsule
import com.nexus.app.domain.model.NexusContext
import com.nexus.app.domain.model.restore.ChangeCategory
import com.nexus.app.domain.model.restore.RestoreChange
import com.nexus.app.domain.model.restore.RestoreChangeType
import com.nexus.app.domain.model.restore.RestorePreview
import com.nexus.app.domain.repository.InstalledAppRepository

/**
 * Read-only engine that compares a Capsule snapshot against a target Context
 * to produce a [RestorePreview]. Does NOT modify any data.
 */
class RestorePreviewEngine(
    private val installedAppRepository: InstalledAppRepository,
) {

    /**
     * Build a preview for restoring a capsule into a brand-new context.
     * Since there's no target context, all snapshot items are ADDED,
     * and missing apps are flagged.
     */
    suspend fun previewNewContext(capsule: NexusCapsule): RestorePreview {
        val changes = mutableListOf<RestoreChange>()

        // Context metadata — all will be "added" (new context)
        changes.add(RestoreChange(
            category = ChangeCategory.CONTEXT_NAME,
            name = "Name",
            detail = capsule.contextSnapshot?.name ?: capsule.name,
            type = RestoreChangeType.ADDED,
        ))

        // Apps
        var appsMissing = 0
        for (app in capsule.appSnapshots) {
            val installed = installedAppRepository.resolveApp(app.packageName)
            if (installed != null) {
                changes.add(RestoreChange(
                    category = ChangeCategory.APP,
                    name = app.appName,
                    detail = app.packageName,
                    type = RestoreChangeType.ADDED,
                ))
            } else {
                appsMissing++
                changes.add(RestoreChange(
                    category = ChangeCategory.APP,
                    name = app.appName,
                    detail = "App unavailable (${app.packageName})",
                    type = RestoreChangeType.MISSING,
                ))
            }
        }

        // Actions
        var actionsInvalid = 0
        for (action in capsule.actionSnapshots.sortedBy { it.position }) {
            val validationError = validateActionSnapshot(action, installedAppRepository)
            if (validationError == null) {
                changes.add(RestoreChange(
                    category = ChangeCategory.ACTION,
                    name = action.name,
                    detail = action.type.name,
                    type = RestoreChangeType.ADDED,
                ))
            } else {
                actionsInvalid++
                changes.add(RestoreChange(
                    category = ChangeCategory.ACTION,
                    name = action.name,
                    detail = validationError,
                    type = RestoreChangeType.MISSING,
                ))
            }
        }

        return RestorePreview(
            capsuleName = capsule.name,
            targetContextName = "(New Context)",
            changes = changes,
            appsAdded = capsule.appSnapshots.size - appsMissing,
            appsRemoved = 0,
            appsMissing = appsMissing,
            appsUnchanged = 0,
            actionsAdded = capsule.actionSnapshots.size - actionsInvalid,
            actionsRemoved = 0,
            actionsInvalid = actionsInvalid,
            actionsUnchanged = 0,
            contextNameChanged = true,
            contextDescriptionChanged = false,
        )
    }

    /**
     * Build a preview for restoring a capsule into an existing context.
     * Compares snapshot against current context apps/actions.
     */
    suspend fun previewExistingContext(
        capsule: NexusCapsule,
        targetContext: NexusContext,
        targetApps: List<String>,
        targetActions: List<NexusAction>,
    ): RestorePreview {
        val changes = mutableListOf<RestoreChange>()

        // Context metadata comparison
        val snapshotName = capsule.contextSnapshot?.name ?: capsule.name
        val nameChanged = snapshotName != targetContext.name
        if (nameChanged) {
            changes.add(RestoreChange(
                category = ChangeCategory.CONTEXT_NAME,
                name = "Name",
                detail = "${targetContext.name} → $snapshotName",
                type = RestoreChangeType.MODIFIED,
            ))
        }

        val snapshotDesc = capsule.contextSnapshot?.description ?: ""
        val descChanged = snapshotDesc != targetContext.description
        if (descChanged) {
            changes.add(RestoreChange(
                category = ChangeCategory.CONTEXT_DESCRIPTION,
                name = "Description",
                detail = "Changed",
                type = RestoreChangeType.MODIFIED,
            ))
        }

        // Apps — compare by packageName
        val capsuleAppSet = capsule.appSnapshots.map { it.packageName }.toSet()
        val targetAppSet = targetApps.toSet()

        var appsMissing = 0
        // Apps in capsule but not in target (or new apps)
        for (app in capsule.appSnapshots) {
            val installed = installedAppRepository.resolveApp(app.packageName)
            if (installed != null) {
                val changeType = if (app.packageName !in targetAppSet) RestoreChangeType.ADDED
                    else RestoreChangeType.UNCHANGED
                changes.add(RestoreChange(
                    category = ChangeCategory.APP,
                    name = app.appName,
                    detail = app.packageName,
                    type = changeType,
                ))
            } else {
                appsMissing++
                changes.add(RestoreChange(
                    category = ChangeCategory.APP,
                    name = app.appName,
                    detail = "App unavailable (${app.packageName})",
                    type = RestoreChangeType.MISSING,
                ))
            }
        }
        // Apps in target but not in capsule (will be removed)
        for (pkg in targetApps) {
            if (pkg !in capsuleAppSet) {
                changes.add(RestoreChange(
                    category = ChangeCategory.APP,
                    name = pkg,
                    detail = pkg,
                    type = RestoreChangeType.REMOVED,
                ))
            }
        }

        // Actions — compare by position/name
        val capsuleActionNames = capsule.actionSnapshots.map { "${it.name}_${it.position}" }.toSet()
        val targetActionNames = targetActions.map { "${it.name}_${it.position}" }.toSet()

        var actionsInvalid = 0
        for (action in capsule.actionSnapshots.sortedBy { it.position }) {
            val key = "${action.name}_${action.position}"
            val validationError = validateActionSnapshot(action, installedAppRepository)
            if (validationError == null) {
                val changeType = if (key !in targetActionNames) RestoreChangeType.ADDED
                    else RestoreChangeType.UNCHANGED
                changes.add(RestoreChange(
                    category = ChangeCategory.ACTION,
                    name = action.name,
                    detail = action.type.name,
                    type = changeType,
                ))
            } else {
                actionsInvalid++
                changes.add(RestoreChange(
                    category = ChangeCategory.ACTION,
                    name = action.name,
                    detail = validationError,
                    type = RestoreChangeType.MISSING,
                ))
            }
        }
        for (action in targetActions) {
            val key = "${action.name}_${action.position}"
            if (key !in capsuleActionNames) {
                changes.add(RestoreChange(
                    category = ChangeCategory.ACTION,
                    name = action.name,
                    detail = action.type.name,
                    type = RestoreChangeType.REMOVED,
                ))
            }
        }

        val addedApps = changes.count { it.category == ChangeCategory.APP && it.type == RestoreChangeType.ADDED }
        val removedApps = changes.count { it.category == ChangeCategory.APP && it.type == RestoreChangeType.REMOVED }
        val unchangedApps = changes.count { it.category == ChangeCategory.APP && it.type == RestoreChangeType.UNCHANGED }
        val addedActions = changes.count { it.category == ChangeCategory.ACTION && it.type == RestoreChangeType.ADDED }
        val removedActions = changes.count { it.category == ChangeCategory.ACTION && it.type == RestoreChangeType.REMOVED }
        val unchangedActions = changes.count { it.category == ChangeCategory.ACTION && it.type == RestoreChangeType.UNCHANGED }

        return RestorePreview(
            capsuleName = capsule.name,
            targetContextName = targetContext.name,
            changes = changes,
            appsAdded = addedApps,
            appsRemoved = removedApps,
            appsMissing = appsMissing,
            appsUnchanged = unchangedApps,
            actionsAdded = addedActions,
            actionsRemoved = removedActions,
            actionsInvalid = actionsInvalid,
            actionsUnchanged = unchangedActions,
            contextNameChanged = nameChanged,
            contextDescriptionChanged = descChanged,
        )
    }
}

/**
 * Validate an action snapshot before restoration.
 * Returns an error message if invalid, null if valid.
 * For OPEN_APP, checks if the package is still installed.
 */
private suspend fun validateActionSnapshot(
    action: ActionSnapshot,
    installedAppRepository: InstalledAppRepository,
): String? {
    // Validate basic action properties
    val nameError = com.nexus.app.domain.model.ActionValidator.validateName(action.name)
    if (nameError != null) return nameError

    val payloadError = com.nexus.app.domain.model.ActionValidator.validatePayload(action.type, action.payload)
    if (payloadError != null) return payloadError

    // For OPEN_APP, check if the package is installed
    if (action.type == com.nexus.app.domain.model.ActionType.OPEN_APP) {
        val parsed = com.nexus.app.domain.model.ActionPayload.fromJson(action.type, action.payload)
        if (parsed is com.nexus.app.domain.model.ActionPayload.OpenApp) {
            val installed = installedAppRepository.resolveApp(parsed.packageName)
            if (installed == null) return "App not installed (${parsed.packageName})"
        }
    }

    return null
}
