package com.nexus.app.domain.model.automation

/**
 * Represents a trigger event that the engine processes.
 * Immutable data classes — safe for concurrent use.
 */
sealed class TriggerEvent {
    abstract val automationId: String?

    // ── Phase 7 ──────────────────────────────────────────────
    data class Manual(override val automationId: String) : TriggerEvent()
    data class Time(override val automationId: String?) : TriggerEvent()
    data class AppOpened(val packageName: String, override val automationId: String? = null) : TriggerEvent()
    data class AppClosed(val packageName: String, override val automationId: String? = null) : TriggerEvent()
    data class ContextActivated(val contextId: String, override val automationId: String? = null) : TriggerEvent()

    // ── Phase 8 ──────────────────────────────────────────────
    data class WifiConnected(val ssid: String = "", override val automationId: String? = null) : TriggerEvent()
    data class WifiDisconnected(val ssid: String = "", override val automationId: String? = null) : TriggerEvent()
    data class BluetoothConnected(val deviceName: String = "", override val automationId: String? = null) : TriggerEvent()
    data class BluetoothDisconnected(val deviceName: String = "", override val automationId: String? = null) : TriggerEvent()
    data class ChargingStarted(override val automationId: String? = null) : TriggerEvent()
    data class ChargingStopped(override val automationId: String? = null) : TriggerEvent()
    data class BatteryLevelChanged(val percent: Int, override val automationId: String? = null) : TriggerEvent()
    data class DeviceBoot(override val automationId: String? = null) : TriggerEvent()
    data class ScreenOn(override val automationId: String? = null) : TriggerEvent()
    data class ScreenOff(override val automationId: String? = null) : TriggerEvent()
    data class DeviceIdle(override val automationId: String? = null) : TriggerEvent()
    data class DeviceActive(override val automationId: String? = null) : TriggerEvent()

    // ── Phase 9 ──────────────────────────────────────────────
    data class NfcTagDetected(val tagId: String, val technology: String = "", val timestamp: Long = System.currentTimeMillis(), override val automationId: String? = null) : TriggerEvent()
    data class NfcTagRemoved(val tagId: String, val timestamp: Long = System.currentTimeMillis(), override val automationId: String? = null) : TriggerEvent()
    data class GeofenceEntered(val geofenceId: String, val latitude: Double = 0.0, val longitude: Double = 0.0, val timestamp: Long = System.currentTimeMillis(), override val automationId: String? = null) : TriggerEvent()
    data class GeofenceExited(val geofenceId: String, val latitude: Double = 0.0, val longitude: Double = 0.0, val timestamp: Long = System.currentTimeMillis(), override val automationId: String? = null) : TriggerEvent()
    data class CalendarEventStarted(val eventId: String, val title: String = "", val calendarId: String = "", val startTime: Long = 0, val endTime: Long = 0, override val automationId: String? = null) : TriggerEvent()
    data class CalendarEventEnded(val eventId: String, val title: String = "", val calendarId: String = "", val startTime: Long = 0, val endTime: Long = 0, override val automationId: String? = null) : TriggerEvent()
    data class NotificationPosted(val packageName: String, val notificationKey: String = "", val category: String = "", val timestamp: Long = System.currentTimeMillis(), override val automationId: String? = null) : TriggerEvent()
    data class NotificationRemoved(val packageName: String, val notificationKey: String = "", val timestamp: Long = System.currentTimeMillis(), override val automationId: String? = null) : TriggerEvent()
}
