package com.nexus.app.domain.model.restore

/**
 * Structured result of a capsule restoration.
 */
data class RestoreResult(
    val status: RestoreStatus,
    val contextId: String?,
    val appsRestored: Int,
    val appsSkipped: Int,
    val actionsRestored: Int,
    val actionsSkipped: Int,
    val warnings: List<String>,
    val errors: List<String>,
    val startedAt: Long,
    val completedAt: Long,
) {
    /** Whether the restoration completed with at least some success. */
    val isSuccessful: Boolean
        get() = status == RestoreStatus.SUCCESS || status == RestoreStatus.PARTIAL
}

enum class RestoreStatus {
    SUCCESS,
    PARTIAL,
    FAILED,
    CANCELLED,
}
