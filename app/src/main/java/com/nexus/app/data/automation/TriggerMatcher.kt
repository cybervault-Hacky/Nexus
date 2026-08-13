package com.nexus.app.data.automation

import com.nexus.app.domain.model.automation.AutomationRule
import com.nexus.app.domain.model.automation.TriggerEvent
import com.nexus.app.domain.model.automation.TriggerType
import org.json.JSONObject

/**
 * Determines whether a [TriggerEvent] matches an [AutomationRule].
 * Handles edge-trigger semantics for thresholds.
 */
object TriggerMatcher {

    private val lastBatteryLevel = mutableMapOf<String, Int>()

    fun matches(event: TriggerEvent, rule: AutomationRule): Boolean {
        return when (event) {
            // Phase 7
            is TriggerEvent.Manual -> event.automationId == rule.id
            is TriggerEvent.Time -> event.automationId == null || event.automationId == rule.id
            is TriggerEvent.AppOpened -> rule.triggerType == TriggerType.APP_OPEN && rule.triggerPayload.contains("\"${event.packageName}\"")
            is TriggerEvent.AppClosed -> rule.triggerType == TriggerType.APP_CLOSE && rule.triggerPayload.contains("\"${event.packageName}\"")
            is TriggerEvent.ContextActivated -> rule.triggerType == TriggerType.CONTEXT_ACTIVATED && rule.contextId == event.contextId

            // Phase 8
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

            // Phase 9
            is TriggerEvent.NfcTagDetected -> matchesNfc(event, rule)
            is TriggerEvent.NfcTagRemoved -> rule.triggerType == TriggerType.NFC_TAG_REMOVED
            is TriggerEvent.GeofenceEntered -> matchesGeofence(event.geofenceId, rule, TriggerType.GEOFENCE_ENTER)
            is TriggerEvent.GeofenceExited -> matchesGeofence(event.geofenceId, rule, TriggerType.GEOFENCE_EXIT)
            is TriggerEvent.CalendarEventStarted -> rule.triggerType == TriggerType.CALENDAR_EVENT_START
            is TriggerEvent.CalendarEventEnded -> rule.triggerType == TriggerType.CALENDAR_EVENT_END
            is TriggerEvent.NotificationPosted -> matchesNotification(event, rule)
            is TriggerEvent.NotificationRemoved -> rule.triggerType == TriggerType.NOTIFICATION_REMOVED
        }
    }

    private fun matchesNfc(event: TriggerEvent.NfcTagDetected, rule: AutomationRule): Boolean {
        if (rule.triggerType != TriggerType.NFC_TAG_DETECTED) return false
        val configuredTagId = try {
            JSONObject(rule.triggerPayload).optString("tagId", "")
        } catch (_: Exception) { "" }
        return configuredTagId.isBlank() || configuredTagId == event.tagId
    }

    private fun matchesGeofence(eventGeofenceId: String, rule: AutomationRule, expectedType: TriggerType): Boolean {
        if (rule.triggerType != expectedType) return false
        val configuredId = try {
            JSONObject(rule.triggerPayload).optString("geofenceId", "")
        } catch (_: Exception) { "" }
        return configuredId == eventGeofenceId
    }

    private fun matchesNotification(event: TriggerEvent.NotificationPosted, rule: AutomationRule): Boolean {
        if (rule.triggerType != TriggerType.NOTIFICATION_POSTED) return false
        val configuredPkg = try {
            JSONObject(rule.triggerPayload).optString("packageName", "")
        } catch (_: Exception) { "" }
        val configuredCat = try {
            JSONObject(rule.triggerPayload).optString("category", "")
        } catch (_: Exception) { "" }
        val pkgMatch = configuredPkg.isBlank() || configuredPkg == event.packageName
        val catMatch = configuredCat.isBlank() || configuredCat == event.category
        return pkgMatch && catMatch
    }

    private fun matchesBattery(event: TriggerEvent.BatteryLevelChanged, rule: AutomationRule): Boolean {
        if (rule.triggerType != TriggerType.BATTERY_BELOW && rule.triggerType != TriggerType.BATTERY_ABOVE) return false
        val threshold = try { JSONObject(rule.triggerPayload).optInt("thresholdPercent", 20) } catch (_: Exception) { 20 }
        val previous = lastBatteryLevel[rule.id]
        lastBatteryLevel[rule.id] = event.percent
        if (previous == null) {
            return when (rule.triggerType) {
                TriggerType.BATTERY_BELOW -> event.percent < threshold
                TriggerType.BATTERY_ABOVE -> event.percent > threshold
                else -> false
            }
        }
        return when (rule.triggerType) {
            TriggerType.BATTERY_BELOW -> previous >= threshold && event.percent < threshold
            TriggerType.BATTERY_ABOVE -> previous <= threshold && event.percent > threshold
            else -> false
        }
    }
}
