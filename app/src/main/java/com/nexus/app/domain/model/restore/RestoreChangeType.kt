package com.nexus.app.domain.model.restore

/**
 * The type of change detected between capsule snapshot and target context.
 */
enum class RestoreChangeType {
    /** Present in capsule, not in target → will be added. */
    ADDED,
    /** Present in target, not in capsule → will be removed. */
    REMOVED,
    /** Present in both, identical → no change needed. */
    UNCHANGED,
    /** Present in both, different → will be updated. */
    MODIFIED,
    /** Present in capsule but app/action cannot be resolved. */
    MISSING,
}
