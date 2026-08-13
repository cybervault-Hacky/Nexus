package com.nexus.app.data.automation

import com.nexus.app.domain.model.automation.TriggerEvent

/**
 * Prevents duplicate system broadcasts from causing duplicate executions.
 * Uses a sliding time window per event signature.
 */
class EventDeduplicator(
    private val windowMs: Long = 5_000, // 5 second dedup window
) {
    private val recentEvents = mutableMapOf<String, Long>()

    /** Returns true if this event should be processed (not a duplicate). */
    fun shouldProcess(event: TriggerEvent): Boolean {
        val now = System.currentTimeMillis()
        val signature = eventSignature(event)
        val lastTime = recentEvents[signature]

        if (lastTime != null && (now - lastTime) < windowMs) {
            return false // Duplicate
        }

        recentEvents[signature] = now
        pruneOld(now)
        return true
    }

    private fun eventSignature(event: TriggerEvent): String = when (event) {
        is TriggerEvent.Manual -> "manual_${event.automationId}"
        is TriggerEvent.Time -> "time_${event.automationId ?: "all"}"
        is TriggerEvent.AppOpened -> "app_open_${event.packageName}"
        is TriggerEvent.AppClosed -> "app_close_${event.packageName}"
        is TriggerEvent.ContextActivated -> "ctx_${event.contextId}"
        is TriggerEvent.WifiConnected -> "wifi_on_${event.ssid}"
        is TriggerEvent.WifiDisconnected -> "wifi_off"
        is TriggerEvent.BluetoothConnected -> "bt_on_${event.deviceName}"
        is TriggerEvent.BluetoothDisconnected -> "bt_off_${event.deviceName}"
        is TriggerEvent.ChargingStarted -> "charge_start"
        is TriggerEvent.ChargingStopped -> "charge_stop"
        is TriggerEvent.BatteryLevelChanged -> "battery_${event.percent}"
        is TriggerEvent.DeviceBoot -> "boot"
        is TriggerEvent.ScreenOn -> "screen_on"
        is TriggerEvent.ScreenOff -> "screen_off"
        is TriggerEvent.DeviceIdle -> "idle"
        is TriggerEvent.DeviceActive -> "active"
    }

    private fun pruneOld(now: Long) {
        if (recentEvents.size > 100) {
            recentEvents.entries.removeAll { (now - it.value) > windowMs * 2 }
        }
    }
}
