package com.nexus.app.domain.model.automation

/**
 * Execution status for automation runs.
 */
enum class ExecutionStatus {
    RUNNING,
    SUCCESS,
    FAILED,
    CANCELLED,
    SKIPPED_COOLDOWN,
    SKIPPED_DISABLED,
    SKIPPED_INVALID,
}
