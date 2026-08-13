package com.nexus.app

import com.nexus.app.domain.model.ActionPayload
import com.nexus.app.domain.model.ActionType
import com.nexus.app.domain.model.NexusAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for Action domain models, payloads, and validation.
 */
class ActionModelTest {

    // ── NexusAction ──────────────────────────────────────────

    @Test
    fun `NexusAction holds correct data`() {
        val action = NexusAction(
            id = "a1",
            contextId = "ctx1",
            name = "Open Termux",
            description = "Launch terminal",
            type = ActionType.OPEN_APP,
            payload = """{"packageName":"com.termux"}""",
            isEnabled = true,
            position = 0,
        )
        assertEquals("a1", action.id)
        assertEquals("ctx1", action.contextId)
        assertEquals(ActionType.OPEN_APP, action.type)
        assertTrue(action.isEnabled)
    }

    @Test
    fun `NexusAction defaults are correct`() {
        val action = NexusAction(
            id = "a1", contextId = "ctx1", name = "Test",
            description = "", type = ActionType.DELAY, payload = "{}",
        )
        assertTrue(action.isEnabled)
        assertEquals(0, action.position)
    }

    // ── ActionType ───────────────────────────────────────────

    @Test
    fun `ActionType has three values`() {
        assertEquals(3, ActionType.entries.size)
    }

    // ── ActionPayload serialization ──────────────────────────

    @Test
    fun `OpenApp payload round-trips`() {
        val original = ActionPayload.OpenApp("com.termux")
        val json = original.toJson()
        val parsed = ActionPayload.fromJson(ActionType.OPEN_APP, json)
        assertEquals(original, parsed)
    }

    @Test
    fun `OpenUrl payload round-trips`() {
        val original = ActionPayload.OpenUrl("https://example.com")
        val json = original.toJson()
        val parsed = ActionPayload.fromJson(ActionType.OPEN_URL, json)
        assertEquals(original, parsed)
    }

    @Test
    fun `Delay payload round-trips`() {
        val original = ActionPayload.Delay(2000L)
        val json = original.toJson()
        val parsed = ActionPayload.fromJson(ActionType.DELAY, json)
        assertEquals(original, parsed)
    }

    @Test
    fun `fromJson returns null for invalid JSON`() {
        val result = ActionPayload.fromJson(ActionType.OPEN_APP, "not json")
        assertNull(result)
    }

    @Test
    fun `fromJson returns null for missing fields`() {
        val result = ActionPayload.fromJson(ActionType.OPEN_APP, """{"wrong":"field"}""")
        assertNull(result)
    }

    @Test
    fun `OpenApp JSON contains packageName`() {
        val json = ActionPayload.OpenApp("com.termux").toJson()
        assertTrue(json.contains("com.termux"))
    }

    @Test
    fun `Delay JSON contains durationMs`() {
        val json = ActionPayload.Delay(500L).toJson()
        assertTrue(json.contains("500"))
    }
}
