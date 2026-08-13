package com.nexus.app.domain.model

/**
 * A Capsule captures a point-in-time immutable snapshot of a Context's
 * complete state: metadata, associated apps, and configured actions.
 *
 * Capsules are independent — deleting the source Context does NOT affect them.
 * The snapshot data (apps, actions, context metadata) is frozen at capture time.
 * Only [name] and [description] may be edited after capture.
 */
data class NexusCapsule(
    val id: String,
    /** The Context this capsule was captured from. Metadata only — no live reference. */
    val sourceContextId: String,
    val name: String,
    val description: String,
    val schemaVersion: Int = CAPSULE_SCHEMA_VERSION,
    val accentColor: Long = 0xFF8B5CF6,
    val contextSnapshot: ContextSnapshot? = null,
    val appSnapshots: List<AppSnapshot> = emptyList(),
    val actionSnapshots: List<ActionSnapshot> = emptyList(),
    /** When the capsule was first created in the database. */
    val createdAt: Long = System.currentTimeMillis(),
    /** When the snapshot was captured (may equal createdAt). */
    val capturedAt: Long = System.currentTimeMillis(),
) {
    /** Derived count of apps in this capsule snapshot. */
    val appCount: Int get() = appSnapshots.size

    /** Derived count of actions in this capsule snapshot. */
    val actionCount: Int get() = actionSnapshots.size

    companion object {
        /** Current capsule schema version. Used for future migrations. */
        const val CAPSULE_SCHEMA_VERSION = 1
    }
}

/**
 * Snapshot of a Context's metadata at capture time.
 * Immutable once captured.
 */
data class ContextSnapshot(
    val name: String,
    val description: String,
    val iconId: String = "grid",
    val accentColor: Long = 0xFF6366F1,
)

/**
 * Snapshot of a single installed app at capture time.
 * Stores both packageName and appName so the capsule is self-contained.
 */
data class AppSnapshot(
    val packageName: String,
    val appName: String,
    val position: Int = 0,
)

/**
 * Snapshot of a single Action at capture time.
 * Immutable once captured.
 */
data class ActionSnapshot(
    val name: String,
    val description: String,
    val type: ActionType,
    val payload: String,
    val position: Int,
    val isEnabled: Boolean = true,
)
