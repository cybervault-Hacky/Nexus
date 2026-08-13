package com.nexus.app.data.automation

import com.nexus.app.domain.model.automation.AutomationRule
import com.nexus.app.domain.model.automation.CompositeOperator
import com.nexus.app.domain.model.automation.TriggerEvent
import com.nexus.app.domain.model.automation.TriggerType
import org.json.JSONArray
import org.json.JSONObject

/**
 * Evaluates composite trigger conditions (ALL/ANY).
 * Supports nested composites with depth protection.
 */
object CompositeTriggerEvaluator {

    private const val MAX_DEPTH = 3

    /**
     * Check if a composite rule's conditions are met given the current event.
     * Returns true if the composite should fire.
     */
    fun evaluate(
        rule: AutomationRule,
        event: TriggerEvent,
        allRules: List<AutomationRule>,
        depth: Int = 0,
    ): Boolean {
        if (depth > MAX_DEPTH) return false
        if (rule.triggerType != TriggerType.ALL_CONDITIONS && rule.triggerType != TriggerType.ANY_CONDITION) return false

        val operator = if (rule.triggerType == TriggerType.ALL_CONDITIONS) CompositeOperator.ALL else CompositeOperator.ANY
        val childIds = parseChildIds(rule.triggerPayload)
        if (childIds.isEmpty()) return false

        val childResults = childIds.mapNotNull { childId ->
            val childRule = allRules.find { it.id == childId } ?: return@mapNotNull null
            evaluateChild(childRule, event, allRules, depth + 1)
        }

        return when (operator) {
            CompositeOperator.ALL -> childResults.isNotEmpty() && childResults.all { it }
            CompositeOperator.ANY -> childResults.any { it }
        }
    }

    private fun evaluateChild(
        rule: AutomationRule,
        event: TriggerEvent,
        allRules: List<AutomationRule>,
        depth: Int,
    ): Boolean {
        return if (rule.triggerType == TriggerType.ALL_CONDITIONS || rule.triggerType == TriggerType.ANY_CONDITION) {
            evaluate(rule, event, allRules, depth)
        } else {
            TriggerMatcher.matches(event, rule)
        }
    }

    private fun parseChildIds(payload: String): List<String> {
        return try {
            val arr = JSONObject(payload).optJSONArray("childConditions") ?: return emptyList()
            (0 until arr.length()).map { arr.getString(it) }
        } catch (_: Exception) { emptyList() }
    }

    /** Validate composite configuration. */
    fun validate(payload: String): String? {
        return try {
            val obj = JSONObject(payload)
            val operator = obj.optString("operator", "")
            if (operator != "ALL" && operator != "ANY") return "Operator must be ALL or ANY"
            val children = obj.optJSONArray("childConditions")
            if (children == null || children.length() == 0) return "Must contain at least one condition"
            if (children.length() > 10) return "Too many conditions (max 10)"
            null
        } catch (_: Exception) {
            "Invalid composite configuration"
        }
    }
}
