package com.nexus.app

import com.nexus.app.data.automation.conflict.AutomationConflictEngine
import com.nexus.app.data.local.AutomationEntity
import com.nexus.app.domain.model.smart.ConflictType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AutomationConflictTest {

    @Test
    fun `duplicate rules detected`() {
        val rules = listOf(
            makeRule("a1", "WIFI_CONNECTED", "{}", "ctx1"),
            makeRule("a2", "WIFI_CONNECTED", "{}", "ctx1"),
        )
        val conflicts = AutomationConflictEngine.detectConflicts(rules)
        assertTrue(conflicts.any { it.type == ConflictType.DUPLICATE })
    }

    @Test
    fun `no conflicts for different triggers`() {
        val rules = listOf(
            makeRule("a1", "WIFI_CONNECTED", "{}", "ctx1"),
            makeRule("a2", "BLUETOOTH_CONNECTED", "{}", "ctx1"),
        )
        val conflicts = AutomationConflictEngine.detectConflicts(rules)
        assertEquals(0, conflicts.size)
    }

    @Test
    fun `trigger overlap detected`() {
        val rules = listOf(
            makeRule("a1", "WIFI_CONNECTED", """{"ssid":"a"}""", "ctx1"),
            makeRule("a2", "WIFI_CONNECTED", """{"ssid":"b"}""", "ctx1"),
        )
        val conflicts = AutomationConflictEngine.detectConflicts(rules)
        assertTrue(conflicts.any { it.type == ConflictType.TRIGGER_OVERLAP })
    }

    @Test
    fun `frequency risk detected for short cooldowns`() {
        val rules = listOf(
            makeRule("a1", "WIFI_CONNECTED", "{}", "ctx1", cooldown = 5),
            makeRule("a2", "WIFI_CONNECTED", "{}", "ctx2", cooldown = 5),
        )
        val conflicts = AutomationConflictEngine.detectConflicts(rules)
        assertTrue(conflicts.any { it.type == ConflictType.FREQUENCY_RISK })
    }

    private fun makeRule(id: String, type: String, payload: String, ctx: String, cooldown: Int = 60) =
        AutomationEntity(
            id = id, name = "Rule $id", description = "", isEnabled = true,
            triggerType = type, triggerPayload = payload, contextId = ctx,
            cooldownSeconds = cooldown, lastTriggeredAt = null, createdAt = 1000, updatedAt = 1000,
        )
}
