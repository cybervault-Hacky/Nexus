package com.nexus.app

import com.nexus.app.data.automation.TriggerMatcher
import com.nexus.app.domain.model.automation.AutomationRule
import com.nexus.app.domain.model.automation.TriggerEvent
import com.nexus.app.domain.model.automation.TriggerType
import org.json.JSONObject
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TriggerMatcherTest {

    // ── Phase 7 matching ─────────────────────────────────────

    @Test
    fun `Manual event matches correct automation`() {
        val rule = makeRule(TriggerType.MANUAL, id = "a1")
        assertTrue(TriggerMatcher.matches(TriggerEvent.Manual("a1"), rule))
    }

    @Test
    fun `Manual event does not match different automation`() {
        val rule = makeRule(TriggerType.MANUAL, id = "a1")
        assertFalse(TriggerMatcher.matches(TriggerEvent.Manual("a2"), rule))
    }

    @Test
    fun `AppOpened event matches correct package`() {
        val payload = JSONObject().apply { put("packageName", "com.termux") }.toString()
        val rule = makeRule(TriggerType.APP_OPEN, payload = payload)
        assertTrue(TriggerMatcher.matches(TriggerEvent.AppOpened("com.termux"), rule))
    }

    @Test
    fun `AppOpened event does not match wrong package`() {
        val payload = JSONObject().apply { put("packageName", "com.termux") }.toString()
        val rule = makeRule(TriggerType.APP_OPEN, payload = payload)
        assertFalse(TriggerMatcher.matches(TriggerEvent.AppOpened("com.chrome"), rule))
    }

    // ── Phase 8 environment matching ─────────────────────────

    @Test
    fun `WifiConnected matches WIFI_CONNECTED rule`() {
        val rule = makeRule(TriggerType.WIFI_CONNECTED)
        assertTrue(TriggerMatcher.matches(TriggerEvent.WifiConnected("MyNetwork"), rule))
    }

    @Test
    fun `WifiDisconnected matches WIFI_DISCONNECTED rule`() {
        val rule = makeRule(TriggerType.WIFI_DISCONNECTED)
        assertTrue(TriggerMatcher.matches(TriggerEvent.WifiDisconnected(), rule))
    }

    @Test
    fun `BluetoothConnected matches BLUETOOTH_CONNECTED rule`() {
        val rule = makeRule(TriggerType.BLUETOOTH_CONNECTED)
        assertTrue(TriggerMatcher.matches(TriggerEvent.BluetoothConnected("Headphones"), rule))
    }

    @Test
    fun `ChargingStarted matches CHARGING_STARTED rule`() {
        val rule = makeRule(TriggerType.CHARGING_STARTED)
        assertTrue(TriggerMatcher.matches(TriggerEvent.ChargingStarted(), rule))
    }

    @Test
    fun `DeviceBoot matches DEVICE_BOOT rule`() {
        val rule = makeRule(TriggerType.DEVICE_BOOT)
        assertTrue(TriggerMatcher.matches(TriggerEvent.DeviceBoot(), rule))
    }

    @Test
    fun `ScreenOn matches SCREEN_ON rule`() {
        val rule = makeRule(TriggerType.SCREEN_ON)
        assertTrue(TriggerMatcher.matches(TriggerEvent.ScreenOn(), rule))
    }

    @Test
    fun `ScreenOff matches SCREEN_OFF rule`() {
        val rule = makeRule(TriggerType.SCREEN_OFF)
        assertTrue(TriggerMatcher.matches(TriggerEvent.ScreenOff(), rule))
    }

    @Test
    fun `DeviceIdle matches DEVICE_IDLE rule`() {
        val rule = makeRule(TriggerType.DEVICE_IDLE)
        assertTrue(TriggerMatcher.matches(TriggerEvent.DeviceIdle(), rule))
    }

    @Test
    fun `DeviceActive matches DEVICE_ACTIVE rule`() {
        val rule = makeRule(TriggerType.DEVICE_ACTIVE)
        assertTrue(TriggerMatcher.matches(TriggerEvent.DeviceActive(), rule))
    }

    // ── Battery edge trigger ─────────────────────────────────

    @Test
    fun `BatteryBelow triggers on threshold crossing`() {
        val payload = JSONObject().apply { put("thresholdPercent", 20) }.toString()
        val rule = makeRule(TriggerType.BATTERY_BELOW, payload = payload)
        // First event at 15% — triggers because we're below
        assertTrue(TriggerMatcher.matches(TriggerEvent.BatteryLevelChanged(15), rule))
    }

    @Test
    fun `BatteryBelow does not trigger when staying below`() {
        val payload = JSONObject().apply { put("thresholdPercent", 20) }.toString()
        val rule = makeRule(TriggerType.BATTERY_BELOW, payload = payload, id = "battery_test")
        // First event sets state
        TriggerMatcher.matches(TriggerEvent.BatteryLevelChanged(15), rule)
        // Second event still below — should NOT trigger
        assertFalse(TriggerMatcher.matches(TriggerEvent.BatteryLevelChanged(14), rule))
    }

    // ── Negative matching ────────────────────────────────────

    @Test
    fun `WifiConnected does not match WIFI_DISCONNECTED rule`() {
        val rule = makeRule(TriggerType.WIFI_DISCONNECTED)
        assertFalse(TriggerMatcher.matches(TriggerEvent.WifiConnected("net"), rule))
    }

    @Test
    fun `ChargingStarted does not match CHARGING_STOPPED rule`() {
        val rule = makeRule(TriggerType.CHARGING_STOPPED)
        assertFalse(TriggerMatcher.matches(TriggerEvent.ChargingStarted(), rule))
    }

    @Test
    fun `ScreenOn does not match SCREEN_OFF rule`() {
        val rule = makeRule(TriggerType.SCREEN_OFF)
        assertFalse(TriggerMatcher.matches(TriggerEvent.ScreenOn(), rule))
    }

    private fun makeRule(
        triggerType: TriggerType,
        payload: String = "{}",
        id: String = "auto1",
    ) = AutomationRule(
        id = id, name = "Test", description = "", isEnabled = true,
        triggerType = triggerType, triggerPayload = payload, contextId = "ctx1",
    )
}
