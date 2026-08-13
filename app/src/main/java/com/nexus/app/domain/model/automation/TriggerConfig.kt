package com.nexus.app.domain.model.automation

/**
 * Strongly-typed trigger configuration.
 * Each variant maps to a [TriggerType].
 */
sealed class TriggerConfig {
    // ── Phase 7 ──────────────────────────────────────────────
    data object Manual : TriggerConfig()
    data class Time(val hour: Int, val minute: Int, val daysOfWeek: Int) : TriggerConfig()
    data class AppOpen(val packageName: String) : TriggerConfig()
    data class AppClose(val packageName: String) : TriggerConfig()
    data class ContextActivated(val contextId: String) : TriggerConfig()

    // ── Phase 8 ──────────────────────────────────────────────
    data class WifiConnected(val ssid: String = "") : TriggerConfig()
    data object WifiDisconnected : TriggerConfig()
    data class BluetoothConnected(val deviceName: String = "") : TriggerConfig()
    data object BluetoothDisconnected : TriggerConfig()
    data object ChargingStarted : TriggerConfig()
    data object ChargingStopped : TriggerConfig()
    data class BatteryBelow(val thresholdPercent: Int) : TriggerConfig()
    data class BatteryAbove(val thresholdPercent: Int) : TriggerConfig()
    data object DeviceBoot : TriggerConfig()
    data object ScreenOn : TriggerConfig()
    data object ScreenOff : TriggerConfig()
    data object DeviceIdle : TriggerConfig()
    data object DeviceActive : TriggerConfig()

    // ── Phase 9 ──────────────────────────────────────────────
    data class NfcTag(val tagId: String = "") : TriggerConfig()
    data class Geofence(val geofenceId: String, val latitude: Double, val longitude: Double, val radiusMeters: Float) : TriggerConfig()
    data class Calendar(val calendarId: String = "", val eventTitlePattern: String = "", val minutesBefore: Int = 0) : TriggerConfig()
    data class Notification(val packageName: String = "", val category: String = "") : TriggerConfig()
    data class Composite(val operator: CompositeOperator, val childConditions: List<String>) : TriggerConfig()
}

/** Operator for composite triggers. */
enum class CompositeOperator { ALL, ANY }
