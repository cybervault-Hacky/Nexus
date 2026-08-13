package com.nexus.app.domain.model.automation

/**
 * Supported trigger types for automation rules.
 * Designed extensible — add new types without modifying existing handlers.
 */
enum class TriggerType {
    /** User manually runs the automation. */
    MANUAL,
    /** Fires at a specific time on selected days. */
    TIME,
    /** Fires when a specific app is opened. */
    APP_OPEN,
    /** Fires when a specific app is closed. */
    APP_CLOSE,
    /** Fires when a specific context is activated. */
    CONTEXT_ACTIVATED,
}
