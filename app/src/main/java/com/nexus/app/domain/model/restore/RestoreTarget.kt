package com.nexus.app.domain.model.restore

/**
 * Describes what type of restoration the user wants.
 */
enum class RestoreTarget {
    /** Create a brand-new Context from the capsule snapshot. */
    CREATE_NEW,
    /** Replace the contents of an existing Context. */
    REPLACE_EXISTING,
}
