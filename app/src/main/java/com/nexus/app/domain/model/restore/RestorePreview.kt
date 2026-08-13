package com.nexus.app.domain.model.restore

/**
 * A read-only preview of what restoration will do.
 * Produced by comparing the capsule snapshot against the target context.
 */
data class RestorePreview(
    val capsuleName: String,
    val targetContextName: String,
    val changes: List<RestoreChange>,
    val appsAdded: Int,
    val appsRemoved: Int,
    val appsMissing: Int,
    val appsUnchanged: Int,
    val actionsAdded: Int,
    val actionsRemoved: Int,
    val actionsInvalid: Int,
    val actionsUnchanged: Int,
    val contextNameChanged: Boolean,
    val contextDescriptionChanged: Boolean,
) {
    /** Whether any changes will actually be applied. */
    val hasChanges: Boolean
        get() = appsAdded > 0 || appsRemoved > 0 || actionsAdded > 0 ||
            actionsRemoved > 0 || contextNameChanged || contextDescriptionChanged

    /** Whether any apps are unavailable. */
    val hasMissingApps: Boolean get() = appsMissing > 0

    /** Whether any actions are invalid. */
    val hasInvalidActions: Boolean get() = actionsInvalid > 0
}
