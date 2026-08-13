package com.nexus.app.data.automation.pattern

import com.nexus.app.data.local.AutomationEntity
import com.nexus.app.data.local.AutomationExecutionEntity
import com.nexus.app.data.local.EventHistoryEntity
import com.nexus.app.domain.model.smart.AutomationSuggestion
import java.util.UUID

/**
 * Local pattern analyzer — deterministic/statistical analysis only.
 * No AI, no cloud, no ML. Pure frequency + recency scoring.
 */
object PatternAnalyzer {

    /** Analyze execution history for patterns. */
    fun analyzePatterns(
        executions: List<AutomationExecutionEntity>,
        events: List<EventHistoryEntity>,
        rules: List<AutomationEntity>,
    ): List<PatternInsight> {
        val insights = mutableListOf<PatternInsight>()

        // Detect frequently triggered automations
        val triggerCounts = executions.groupBy { it.automationId }.mapValues { it.value.size }
        triggerCounts.entries.sortedByDescending { it.value }.take(5).forEach { (id, count) ->
            if (count >= 5) {
                val rule = rules.find { it.id == id }
                insights.add(PatternInsight(
                    type = PatternType.FREQUENT_TRIGGER,
                    description = "\"${rule?.name ?: id}\" triggered $count times",
                    automationId = id,
                    frequency = count,
                ))
            }
        }

        // Detect frequently failed automations
        val failureCounts = executions.filter { it.status == "FAILED" }
            .groupBy { it.automationId }.mapValues { it.value.size }
        failureCounts.entries.sortedByDescending { it.value }.take(3).forEach { (id, count) ->
            if (count >= 3) {
                val rule = rules.find { it.id == id }
                insights.add(PatternInsight(
                    type = PatternType.FREQUENT_FAILURE,
                    description = "\"${rule?.name ?: id}\" failed $count times",
                    automationId = id,
                    frequency = count,
                ))
            }
        }

        // Detect unused automations
        val triggeredIds = executions.map { it.automationId }.toSet()
        rules.filter { it.isEnabled && it.id !in triggeredIds }.forEach { rule ->
            insights.add(PatternInsight(
                type = PatternType.UNUSED,
                description = "\"${rule.name}\" has never been triggered",
                automationId = rule.id,
                frequency = 0,
            ))
        }

        // Detect common event types
        val eventCounts = events.groupBy { it.eventType }.mapValues { it.value.size }
        eventCounts.entries.sortedByDescending { it.value }.take(3).forEach { (type, count) ->
            insights.add(PatternInsight(
                type = PatternType.COMMON_EVENT,
                description = "$type occurred $count times",
                frequency = count,
            ))
        }

        return insights
    }

    /** Generate suggestions from patterns. */
    fun generateSuggestions(
        insights: List<PatternInsight>,
        rules: List<AutomationEntity>,
    ): List<AutomationSuggestion> {
        return insights.mapNotNull { insight ->
            when (insight.type) {
                PatternType.FREQUENT_FAILURE -> {
                    val rule = rules.find { it.id == insight.automationId }
                    AutomationSuggestion(
                        id = UUID.randomUUID().toString(),
                        title = "Review \"${rule?.name ?: "Unknown"}\"",
                        description = "This automation has failed ${insight.frequency} times.",
                        reason = "Frequent failures indicate a configuration issue.",
                        score = 80,
                    )
                }
                PatternType.UNUSED -> {
                    val rule = rules.find { it.id == insight.automationId }
                    AutomationSuggestion(
                        id = UUID.randomUUID().toString(),
                        title = "Review \"${rule?.name ?: "Unknown"}\"",
                        description = "This automation has never been triggered.",
                        reason = "Consider whether the trigger conditions are correct.",
                        score = 40,
                    )
                }
                PatternType.FREQUENT_TRIGGER -> null
                PatternType.COMMON_EVENT -> null
            }
        }
    }
}

data class PatternInsight(
    val type: PatternType,
    val description: String,
    val automationId: String? = null,
    val frequency: Int = 0,
)

enum class PatternType {
    FREQUENT_TRIGGER, FREQUENT_FAILURE, UNUSED, COMMON_EVENT,
}
