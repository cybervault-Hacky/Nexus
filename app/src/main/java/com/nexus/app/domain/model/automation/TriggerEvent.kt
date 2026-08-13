package com.nexus.app.domain.model.automation

/**
 * Represents a trigger event that the engine processes.
 * Events are produced by Android components and consumed by TriggerEngine.
 */
sealed class TriggerEvent {
    abstract val automationId: String?

    // ── Phase 7 events ───────────────────────────────────────
    data class Manual(override val automationId: String) : TriggerEvent()
    data class Time(override val automationId: String?) : TriggerEvent()
    data class AppOpened(val packageName: String, override val automationId: String? = null) : TriggerEvent()
    data class AppClosed(val packageName: String, override val automationId: String? = null) : TriggerEvent()
    data class ContextActivated(val contextId: String, override val automationId: String? = null) : TriggerEvent()

    // ── Phase 8 environment events ───────────────────────────
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
}
