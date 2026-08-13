package com.nexus.app

import com.nexus.app.data.automation.TriggerMatcher
import com.nexus.app.domain.model.automation.AutomationRule
import com.nexus.app.domain.model.automation.TriggerEvent
import com.nexus.app.domain.model.automation.TriggerType
import org.json.JSONObject
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for edge-trigger semantics — threshold transitions
 * must only fire on the crossing, not continuously.
 */
class TriggerTransitionTest {

    @Test
    fun `BATTERY_BELOW triggers only on crossing down`() {
        val payload = JSONObject().apply { put("thresholdPercent", 20) }.toString()
        val rule = makeRule(TriggerType.BATTERY_BELOW, payload = payload, id = "bat1")

        // 25% → 19%: crossing down — should trigger
        assertTrue(TriggerMatcher.matches(TriggerEvent.BatteryLevelChanged(25), rule))
        assertTrue(TriggerMatcher.matches(TriggerEvent.BatteryLevelChanged(19), rule))
    }

    @Test
    fun `BATTERY_BELOW does not trigger while staying below`() {
        val payload = JSONObject().apply { put("thresholdPercent", 20) }.toString()
        val rule = makeRule(TriggerType.BATTERY_BELOW, payload = payload, id = "bat2")

        // First event below threshold
        TriggerMatcher.matches(TriggerEvent.BatteryLevelChanged(19), rule)
        // Still below — should NOT trigger
        assertFalse(TriggerMatcher.matches(TriggerEvent.BatteryLevelChanged(18), rule))
        assertFalse(TriggerMatcher.matches(TriggerEvent.BatteryLevelChanged(15), rule))
    }

    @Test
    fun `BATTERY_BELOW triggers again after going above and back down`() {
        val payload = JSONObject().apply { put("thresholdPercent", 20) }.toString()
        val rule = makeRule(TriggerType.BATTERY_BELOW, payload = payload, id = "bat3")

        // Cross down
        TriggerMatcher.matches(TriggerEvent.BatteryLevelChanged(25), rule)
        assertTrue(TriggerMatcher.matches(TriggerEvent.BatteryLevelChanged(19), rule))

        // Go back above
        TriggerMatcher.matches(TriggerEvent.BatteryLevelChanged(22), rule)

        // Cross down again — should trigger
        assertTrue(TriggerMatcher.matches(TriggerEvent.BatteryLevelChanged(18), rule))
    }

    @Test
    fun `BATTERY_ABOVE triggers only on crossing up`() {
        val payload = JSONObject().apply { put("thresholdPercent", 80) }.toString()
        val rule = makeRule(TriggerType.BATTERY_ABOVE, payload = payload, id = "bat4")

        // 75% → 85%: crossing up — should trigger
        assertTrue(TriggerMatcher.matches(TriggerEvent.BatteryLevelChanged(75), rule))
        assertTrue(TriggerMatcher.matches(TriggerEvent.BatteryLevelChanged(85), rule))
    }

    @Test
    fun `BATTERY_ABOVE does not trigger while staying above`() {
        val payload = JSONObject().apply { put("thresholdPercent", 80) }.toString()
        val rule = makeRule(TriggerType.BATTERY_ABOVE, payload = payload, id = "bat5")

        TriggerMatcher.matches(TriggerEvent.BatteryLevelChanged(85), rule)
        assertFalse(TriggerMatcher.matches(TriggerEvent.BatteryLevelChanged(90), rule))
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
