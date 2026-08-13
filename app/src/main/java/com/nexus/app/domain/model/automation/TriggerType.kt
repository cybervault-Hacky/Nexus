package com.nexus.app.domain.model.automation

/**
 * Supported trigger types for automation rules.
 * Designed extensible — add new types without modifying existing handlers.
 */
enum class TriggerType {
    // ── Phase 7 triggers ─────────────────────────────────────
    MANUAL,
    TIME,
    APP_OPEN,
    APP_CLOSE,
    CONTEXT_ACTIVATED,

    // ── Phase 8 environment triggers ─────────────────────────
    WIFI_CONNECTED,
    WIFI_DISCONNECTED,
    BLUETOOTH_CONNECTED,
    BLUETOOTH_DISCONNECTED,
    CHARGING_STARTED,
    CHARGING_STOPPED,
    BATTERY_BELOW,
    BATTERY_ABOVE,
    DEVICE_BOOT,
    SCREEN_ON,
    SCREEN_OFF,
    DEVICE_IDLE,
    DEVICE_ACTIVE,
}
