package com.nexus.app.domain.model.automation

/**
 * Supported trigger types for automation rules.
 * Designed extensible — add new types without modifying existing handlers.
 */
enum class TriggerType {
    // ── Phase 7 ──────────────────────────────────────────────
    MANUAL, TIME, APP_OPEN, APP_CLOSE, CONTEXT_ACTIVATED,

    // ── Phase 8 ──────────────────────────────────────────────
    WIFI_CONNECTED, WIFI_DISCONNECTED,
    BLUETOOTH_CONNECTED, BLUETOOTH_DISCONNECTED,
    CHARGING_STARTED, CHARGING_STOPPED,
    BATTERY_BELOW, BATTERY_ABOVE,
    DEVICE_BOOT, SCREEN_ON, SCREEN_OFF, DEVICE_IDLE, DEVICE_ACTIVE,

    // ── Phase 9 ──────────────────────────────────────────────
    NFC_TAG_DETECTED, NFC_TAG_REMOVED,
    GEOFENCE_ENTER, GEOFENCE_EXIT,
    CALENDAR_EVENT_START, CALENDAR_EVENT_END,
    NOTIFICATION_POSTED, NOTIFICATION_REMOVED,
    ALL_CONDITIONS, ANY_CONDITION,
}
