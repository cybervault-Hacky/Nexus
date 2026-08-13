package com.nexus.app

import com.nexus.app.data.automation.CooldownPolicy
import com.nexus.app.domain.model.automation.AutomationRule
import com.nexus.app.domain.model.automation.TriggerType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for loop protection via cooldown.
 * Prevents recursive automation chains.
 */
class AutomationLoopProtectionTest {

    @Test
    fun `cooldown prevents immediate re-execution`() {
        val rule = makeRule(cooldownSeconds = 60, lastTriggeredAt = System.currentTimeMillis())
        assertFalse(CooldownPolicy.canExecute(rule))
    }

    @Test
    fun `cooldown allows execution after expiry`() {
        val rule = makeRule(cooldownSeconds = 1, lastTriggeredAt = System.currentTimeMillis() - 2000)
        assertTrue(CooldownPolicy.canExecute(rule))
    }

    @Test
    fun `zero cooldown allows immediate re-execution`() {
        val rule = makeRule(cooldownSeconds = 0, lastTriggeredAt = System.currentTimeMillis())
        assertTrue(CooldownPolicy.canExecute(rule))
    }

    @Test
    fun `never-triggered rule can execute`() {
        val rule = makeRule(lastTriggeredAt = null)
        assertTrue(CooldownPolicy.canExecute(rule))
    }

    @Test
    fun `disabled rule cannot execute`() {
        val rule = makeRule(isEnabled = false)
        assertFalse(CooldownPolicy.canExecute(rule))
    }

    @Test
    fun `remaining cooldown decreases over time`() {
        val rule = makeRule(cooldownSeconds = 60, lastTriggeredAt = System.currentTimeMillis() - 30_000)
        val remaining = CooldownPolicy.remainingCooldownSeconds(rule)
        assertTrue(remaining in 28..32)
    }

    private fun makeRule(
        isEnabled: Boolean = true,
        cooldownSeconds: Int = 60,
        lastTriggeredAt: Long? = null,
    ) = AutomationRule(
        id = "auto1", name = "Test", description = "", isEnabled = isEnabled,
        triggerType = TriggerType.MANUAL, triggerPayload = "{}", contextId = "ctx1",
        cooldownSeconds = cooldownSeconds, lastTriggeredAt = lastTriggeredAt,
    )
}
