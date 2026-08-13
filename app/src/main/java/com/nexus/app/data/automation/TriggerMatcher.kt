package com.nexus.app.data.automation

import com.nexus.app.domain.model.automation.AutomationRule
import com.nexus.app.domain.model.automation.TriggerEvent
import com.nexus.app.domain.model.automation.TriggerType
import org.json.JSONObject

/**
 * Determines whether a [TriggerEvent] matches an [AutomationRule]'s trigger configuration.
 * Handles threshold-based edge triggers (BATTERY_BELOW/ABOVE) to prevent repeated executions.
 */
object TriggerMatcher {

    /** Track last battery level per automation to detect threshold crossings. */
    private val lastBatteryLevel = mutableMapOf<String, Int>()

    fun matches(event: TriggerEvent, rule: AutomationRule): Boolean {
        return when (event) {
            // Phase 7
            is TriggerEvent.Manual -> event.automationId == rule.id
            is TriggerEvent.Time -> event.automationId == null || event.automationId == rule.id
            is TriggerEvent.AppOpened -> rule.triggerType == TriggerType.APP_OPEN &&
                rule.triggerPayload.contains("\"${event.packageName}\"")
            is TriggerEvent.AppClosed -> rule.triggerType == TriggerType.APP_CLOSE &&
                rule.triggerPayload.contains("\"${event.packageName}\"")
            is TriggerEvent.ContextActivated -> rule.triggerType == TriggerType.CONTEXT_ACTIVATED &&
                rule.contextId == event.contextId

            // Phase 8 environment
            is TriggerEvent.WifiConnected -> rule.triggerType == TriggerType.WIFI_CONNECTED
            is TriggerEvent.WifiDisconnected -> rule.triggerType == TriggerType.WIFI_DISCONNECTED
            is TriggerEvent.BluetoothConnected -> rule.triggerType == TriggerType.BLUETOOTH_CONNECTED
            is TriggerEvent.BluetoothDisconnected -> rule.triggerType == TriggerType.BLUETOOTH_DISCONNECTED
            is TriggerEvent.ChargingStarted -> rule.triggerType == TriggerType.CHARGING_STARTED
            is TriggerEvent.ChargingStopped -> rule.triggerType == TriggerType.CHARGING_STOPPED
            is TriggerEvent.BatteryLevelChanged -> matchesBattery(event, rule)
            is TriggerEvent.DeviceBoot -> rule.triggerType == TriggerType.DEVICE_BOOT
            is TriggerEvent.ScreenOn -> rule.triggerType == TriggerType.SCREEN_ON
            is TriggerEvent.ScreenOff -> rule.triggerType == TriggerType.SCREEN_OFF
            is TriggerEvent.DeviceIdle -> rule.triggerType == TriggerType.DEVICE_IDLE
            is TriggerEvent.DeviceActive -> rule.triggerType == TriggerType.DEVICE_ACTIVE
        }
    }

    /**
     * Edge-trigger semantics for battery thresholds.
     * Only triggers on threshold crossing, not while level stays below/above.
     */
    private fun matchesBattery(event: TriggerEvent.BatteryLevelChanged, rule: AutomationRule): Boolean {
        if (rule.triggerType != TriggerType.BATTERY_BELOW && rule.triggerType != TriggerType.BATTERY_ABOVE) return false
        val threshold = try {
            JSONObject(rule.triggerPayload).optInt("thresholdPercent", 20)
        } catch (_: Exception) { 20 }

        val previous = lastBatteryLevel[rule.id]
        lastBatteryLevel[rule.id] = event.percent

        if (previous == null) {
            // First event — check current state
            return when (rule.triggerType) {
                TriggerType.BATTERY_BELOW -> event.percent < threshold
                TriggerType.BATTERY_ABOVE -> event.percent > threshold
                else -> false
            }
        }

        // Edge trigger: detect crossing
        return when (rule.triggerType) {
            TriggerType.BATTERY_BELOW -> previous >= threshold && event.percent < threshold
            TriggerType.BATTERY_ABOVE -> previous <= threshold && event.percent > threshold
            else -> false
        }
    }
}
