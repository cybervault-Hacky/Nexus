package com.nexus.app

import com.nexus.app.domain.model.automation.AutomationRule
import com.nexus.app.domain.model.automation.AutomationValidation
import com.nexus.app.domain.model.automation.ExecutionStatus
import com.nexus.app.domain.model.automation.AutomationExecution
import com.nexus.app.domain.model.automation.TriggerType
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AutomationModelTest {

    @Test
    fun `AutomationRule holds correct data`() {
        val rule = makeRule()
        assertEquals("auto1", rule.id)
        assertEquals("Test", rule.name)
        assertEquals(TriggerType.MANUAL, rule.triggerType)
        assertTrue(rule.isEnabled)
    }

    @Test
    fun `TriggerType includes the five Phase 7 values`() {
        // Phases 8-9 extended TriggerType; the original Phase 7 five must remain.
        assertTrue(TriggerType.entries.containsAll(listOf(
            TriggerType.MANUAL,
            TriggerType.TIME,
            TriggerType.APP_OPEN,
            TriggerType.APP_CLOSE,
            TriggerType.CONTEXT_ACTIVATED,
        )))
    }

    @Test
    fun `ExecutionStatus has seven values`() {
        assertEquals(7, ExecutionStatus.entries.size)
    }

    @Test
    fun `AutomationExecution holds correct data`() {
        val exec = AutomationExecution(
            id = "e1", automationId = "a1", startedAt = 1000, completedAt = 2000,
            status = ExecutionStatus.SUCCESS, triggerType = TriggerType.MANUAL, contextId = "c1",
            successfulActions = 3, failedActions = 0,
        )
        assertEquals(ExecutionStatus.SUCCESS, exec.status)
        assertEquals(3, exec.successfulActions)
    }

    // ── Validation ───────────────────────────────────────────

    @Test
    fun `blank name returns error`() {
        assertNotNull(AutomationValidation.validateName(""))
        assertNotNull(AutomationValidation.validateName("   "))
    }

    @Test
    fun `valid name passes`() {
        assertNull(AutomationValidation.validateName("Morning Routine"))
    }

    @Test
    fun `name too long returns error`() {
        assertNotNull(AutomationValidation.validateName("A".repeat(61)))
    }

    @Test
    fun `negative cooldown returns error`() {
        assertNotNull(AutomationValidation.validateCooldown(-1))
    }

    @Test
    fun `valid cooldown passes`() {
        assertNull(AutomationValidation.validateCooldown(60))
    }

    @Test
    fun `TIME payload with invalid hour returns error`() {
        val payload = JSONObject().apply { put("hour", 25); put("minute", 0); put("daysOfWeek", 1) }.toString()
        assertNotNull(AutomationValidation.validateTriggerPayload(TriggerType.TIME, payload))
    }

    @Test
    fun `TIME payload with no days returns error`() {
        val payload = JSONObject().apply { put("hour", 9); put("minute", 0); put("daysOfWeek", 0) }.toString()
        assertNotNull(AutomationValidation.validateTriggerPayload(TriggerType.TIME, payload))
    }

    @Test
    fun `valid TIME payload passes`() {
        val payload = JSONObject().apply { put("hour", 9); put("minute", 30); put("daysOfWeek", 127) }.toString()
        assertNull(AutomationValidation.validateTriggerPayload(TriggerType.TIME, payload))
    }

    @Test
    fun `APP_OPEN with empty package returns error`() {
        val payload = JSONObject().apply { put("packageName", "") }.toString()
        assertNotNull(AutomationValidation.validateTriggerPayload(TriggerType.APP_OPEN, payload))
    }

    @Test
    fun `APP_OPEN with valid package passes`() {
        val payload = JSONObject().apply { put("packageName", "com.termux") }.toString()
        assertNull(AutomationValidation.validateTriggerPayload(TriggerType.APP_OPEN, payload))
    }

    @Test
    fun `MANUAL trigger always passes`() {
        assertNull(AutomationValidation.validateTriggerPayload(TriggerType.MANUAL, "{}"))
    }

    @Test
    fun `full validation for valid rule passes`() {
        val payload = JSONObject().apply { put("packageName", "com.termux") }.toString()
        assertNull(AutomationValidation.validate("c1", TriggerType.APP_OPEN, "Test", "Desc", 60, payload))
    }

    @Test
    fun `CONTEXT_ACTIVATED without contextId returns error`() {
        val payload = JSONObject().apply { put("contextId", "") }.toString()
        assertNotNull(AutomationValidation.validate("", TriggerType.CONTEXT_ACTIVATED, "Test", "", 0, payload))
    }

    private fun makeRule() = AutomationRule(
        id = "auto1", name = "Test", description = "", triggerType = TriggerType.MANUAL,
        triggerPayload = "{}", contextId = "ctx1",
    )
}
