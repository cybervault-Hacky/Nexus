package com.nexus.app.data.automation.conflict

import com.nexus.app.data.local.AutomationEntity
import com.nexus.app.domain.model.smart.AutomationConflict
import com.nexus.app.domain.model.smart.ConflictSeverity
import com.nexus.app.domain.model.smart.ConflictType

/**
 * Detects conflicts between automation rules.
 */
object AutomationConflictEngine {

    fun detectConflicts(rules: List<AutomationEntity>): List<AutomationConflict> {
        val conflicts = mutableListOf<AutomationConflict>()

        for (i in rules.indices) {
            for (j in i + 1 until rules.size) {
                val a = rules[i]
                val b = rules[j]

                // Same trigger type + same payload = duplicate
                if (a.triggerType == b.triggerType && a.triggerPayload == b.triggerPayload) {
                    conflicts.add(AutomationConflict(
                        automationId = a.id,
                        conflictingWithId = b.id,
                        type = ConflictType.DUPLICATE,
                        severity = ConflictSeverity.WARNING,
                        explanation = "\"${a.name}\" and \"${b.name}\" have identical trigger configuration.",
                    ))
                }

                // Same trigger type = trigger overlap
                if (a.triggerType == b.triggerType && a.contextId == b.contextId) {
                    conflicts.add(AutomationConflict(
                        automationId = a.id,
                        conflictingWithId = b.id,
                        type = ConflictType.TRIGGER_OVERLAP,
                        severity = ConflictSeverity.WARNING,
                        explanation = "\"${a.name}\" and \"${b.name}\" trigger on the same event for the same context.",
                    ))
                }

                // Short cooldown + same trigger = frequency risk
                if (a.triggerType == b.triggerType && a.cooldownSeconds < 30 && b.cooldownSeconds < 30) {
                    conflicts.add(AutomationConflict(
                        automationId = a.id,
                        conflictingWithId = b.id,
                        type = ConflictType.FREQUENCY_RISK,
                        severity = ConflictSeverity.WARNING,
                        explanation = "Both rules have very short cooldowns — may cause excessive execution.",
                    ))
                }
            }
        }

        return conflicts
    }
}
