package com.nexus.app.data.automation

import com.nexus.app.domain.model.automation.AutomationRule

/**
 * Determines whether an automation can execute based on its cooldown.
 * Thread-safe via @Volatile on lastTriggeredAt.
 */
object CooldownPolicy {

    fun canExecute(rule: AutomationRule): Boolean {
        if (!rule.isEnabled) return false
        if (rule.cooldownSeconds <= 0) return true
        val last = rule.lastTriggeredAt ?: return true
        val elapsed = (System.currentTimeMillis() - last) / 1000
        return elapsed >= rule.cooldownSeconds
    }

    fun remainingCooldownSeconds(rule: AutomationRule): Int {
        if (rule.cooldownSeconds <= 0) return 0
        val last = rule.lastTriggeredAt ?: return 0
        val elapsed = ((System.currentTimeMillis() - last) / 1000).toInt()
        return maxOf(0, rule.cooldownSeconds - elapsed)
    }
}
