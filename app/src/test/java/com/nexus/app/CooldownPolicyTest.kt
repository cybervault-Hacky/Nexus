package com.nexus.app

import com.nexus.app.data.automation.CooldownPolicy
import com.nexus.app.domain.model.automation.AutomationRule
import com.nexus.app.domain.model.automation.TriggerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CooldownPolicyTest {

    @Test
    fun `can execute when never triggered`() {
        val rule = makeRule(lastTriggeredAt = null)
        assertTrue(CooldownPolicy.canExecute(rule))
    }

    @Test
    fun `cannot execute when disabled`() {
        val rule = makeRule(isEnabled = false)
        assertFalse(CooldownPolicy.canExecute(rule))
    }

    @Test
    fun `can execute after cooldown expires`() {
        val pastTrigger = System.currentTimeMillis() - 120_000 // 2 minutes ago
        val rule = makeRule(lastTriggeredAt = pastTrigger, cooldownSeconds = 60)
        assertTrue(CooldownPolicy.canExecute(rule))
    }

    @Test
    fun `cannot execute during cooldown`() {
        val recentTrigger = System.currentTimeMillis() - 5_000 // 5 seconds ago
        val rule = makeRule(lastTriggeredAt = recentTrigger, cooldownSeconds = 60)
        assertFalse(CooldownPolicy.canExecute(rule))
    }

    @Test
    fun `zero cooldown always allows execution`() {
        val recentTrigger = System.currentTimeMillis() - 1_000
        val rule = makeRule(lastTriggeredAt = recentTrigger, cooldownSeconds = 0)
        assertTrue(CooldownPolicy.canExecute(rule))
    }

    @Test
    fun `remaining cooldown is correct`() {
        val recentTrigger = System.currentTimeMillis() - 30_000 // 30s ago
        val rule = makeRule(lastTriggeredAt = recentTrigger, cooldownSeconds = 60)
        val remaining = CooldownPolicy.remainingCooldownSeconds(rule)
        assertTrue(remaining in 28..32) // Allow small timing variance
    }

    @Test
    fun `remaining cooldown is zero when expired`() {
        val oldTrigger = System.currentTimeMillis() - 120_000
        val rule = makeRule(lastTriggeredAt = oldTrigger, cooldownSeconds = 60)
        assertEquals(0, CooldownPolicy.remainingCooldownSeconds(rule))
    }

    private fun makeRule(
        isEnabled: Boolean = true,
        lastTriggeredAt: Long? = null,
        cooldownSeconds: Int = 60,
    ) = AutomationRule(
        id = "auto1", name = "Test", description = "", isEnabled = isEnabled,
        triggerType = TriggerType.MANUAL, triggerPayload = "{}", contextId = "ctx1",
        cooldownSeconds = cooldownSeconds, lastTriggeredAt = lastTriggeredAt,
    )
}
