package com.nexus.app

import com.nexus.app.data.automation.CompositeTriggerEvaluator
import com.nexus.app.domain.model.automation.AutomationRule
import com.nexus.app.domain.model.automation.TriggerEvent
import com.nexus.app.domain.model.automation.TriggerType
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CompositeTriggerTest {

    @Test
    fun `ALL composite with matching children returns true`() {
        // The evaluator is event-driven: ALL is satisfied when every child
        // condition matches the incoming event.
        val child1 = makeRule("c1", TriggerType.WIFI_CONNECTED)
        val child2 = makeRule("c2", TriggerType.WIFI_CONNECTED)
        val composite = makeComposite("comp", TriggerType.ALL_CONDITIONS, listOf("c1", "c2"))
        val event = TriggerEvent.WifiConnected()
        assertTrue(CompositeTriggerEvaluator.evaluate(composite, event, listOf(child1, child2, composite)))
    }

    @Test
    fun `ALL composite with non-matching child returns false`() {
        val child1 = makeRule("c1", TriggerType.WIFI_CONNECTED)
        val child2 = makeRule("c2", TriggerType.BLUETOOTH_CONNECTED)
        val composite = makeComposite("comp", TriggerType.ALL_CONDITIONS, listOf("c1", "c2"))
        val event = TriggerEvent.WifiConnected()
        assertFalse(CompositeTriggerEvaluator.evaluate(composite, event, listOf(child1, child2, composite)))
    }

    @Test
    fun `ANY composite with one matching child returns true`() {
        val child1 = makeRule("c1", TriggerType.WIFI_CONNECTED)
        val child2 = makeRule("c2", TriggerType.BLUETOOTH_CONNECTED)
        val composite = makeComposite("comp", TriggerType.ANY_CONDITION, listOf("c1", "c2"))
        val event = TriggerEvent.WifiConnected()
        assertTrue(CompositeTriggerEvaluator.evaluate(composite, event, listOf(child1, child2, composite)))
    }

    @Test
    fun `ANY composite with no matching children returns false`() {
        val child1 = makeRule("c1", TriggerType.WIFI_CONNECTED)
        val child2 = makeRule("c2", TriggerType.BLUETOOTH_CONNECTED)
        val composite = makeComposite("comp", TriggerType.ANY_CONDITION, listOf("c1", "c2"))
        val event = TriggerEvent.ScreenOn()
        assertFalse(CompositeTriggerEvaluator.evaluate(composite, event, listOf(child1, child2, composite)))
    }

    @Test
    fun `composite with empty children returns false`() {
        val composite = makeComposite("comp", TriggerType.ALL_CONDITIONS, emptyList())
        assertFalse(CompositeTriggerEvaluator.evaluate(composite, TriggerEvent.WifiConnected(), listOf(composite)))
    }

    @Test
    fun `composite validation passes for valid payload`() {
        val payload = makeCompositePayload("ALL", listOf("a", "b"))
        assertNull(CompositeTriggerEvaluator.validate(payload))
    }

    @Test
    fun `composite validation fails for empty children`() {
        val payload = makeCompositePayload("ALL", emptyList())
        assertNotNull(CompositeTriggerEvaluator.validate(payload))
    }

    @Test
    fun `composite validation fails for invalid operator`() {
        val payload = makeCompositePayload("INVALID", listOf("a"))
        assertNotNull(CompositeTriggerEvaluator.validate(payload))
    }

    private fun makeRule(id: String, type: TriggerType) = AutomationRule(
        id = id, name = "Child $id", description = "", isEnabled = true,
        triggerType = type, triggerPayload = "{}", contextId = "ctx1",
    )

    private fun makeComposite(id: String, type: TriggerType, childIds: List<String>) = AutomationRule(
        id = id, name = "Composite", description = "", isEnabled = true,
        triggerType = type, triggerPayload = makeCompositePayload(
            if (type == TriggerType.ALL_CONDITIONS) "ALL" else "ANY", childIds
        ), contextId = "ctx1",
    )

    private fun makeCompositePayload(operator: String, childIds: List<String>): String {
        val arr = JSONArray()
        childIds.forEach { arr.put(it) }
        return JSONObject().apply {
            put("operator", operator)
            put("childConditions", arr)
        }.toString()
    }
}
