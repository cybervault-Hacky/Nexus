package com.nexus.app

import com.nexus.app.domain.model.ActionPayload
import com.nexus.app.domain.model.ActionType
import com.nexus.app.domain.model.ActionValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for centralized action validation.
 */
class ActionValidationTest {

    // ── Name validation ──────────────────────────────────────

    @Test
    fun `blank name returns error`() {
        assertNotNull(ActionValidator.validateName(""))
        assertNotNull(ActionValidator.validateName("   "))
    }

    @Test
    fun `valid name passes`() {
        assertNull(ActionValidator.validateName("Open Termux"))
    }

    @Test
    fun `name at max length passes`() {
        assertNull(ActionValidator.validateName("A".repeat(ActionValidator.MAX_NAME_LENGTH)))
    }

    @Test
    fun `name over max length returns error`() {
        assertNotNull(ActionValidator.validateName("A".repeat(ActionValidator.MAX_NAME_LENGTH + 1)))
    }

    // ── Description validation ───────────────────────────────

    @Test
    fun `empty description is valid`() {
        assertNull(ActionValidator.validateDescription(""))
    }

    @Test
    fun `description at max length passes`() {
        assertNull(ActionValidator.validateDescription("A".repeat(ActionValidator.MAX_DESCRIPTION_LENGTH)))
    }

    @Test
    fun `description over max length returns error`() {
        assertNotNull(ActionValidator.validateDescription("A".repeat(ActionValidator.MAX_DESCRIPTION_LENGTH + 1)))
    }

    // ── Payload validation: OPEN_APP ─────────────────────────

    @Test
    fun `OPEN_APP with blank package returns error`() {
        val payload = ActionPayload.OpenApp("").toJson()
        assertNotNull(ActionValidator.validatePayload(ActionType.OPEN_APP, payload))
    }

    @Test
    fun `OPEN_APP with valid package passes`() {
        val payload = ActionPayload.OpenApp("com.termux").toJson()
        assertNull(ActionValidator.validatePayload(ActionType.OPEN_APP, payload))
    }

    // ── Payload validation: OPEN_URL ─────────────────────────

    @Test
    fun `OPEN_URL with blank url returns error`() {
        val payload = ActionPayload.OpenUrl("").toJson()
        assertNotNull(ActionValidator.validatePayload(ActionType.OPEN_URL, payload))
    }

    @Test
    fun `OPEN_URL with valid http passes`() {
        val payload = ActionPayload.OpenUrl("http://example.com").toJson()
        assertNull(ActionValidator.validatePayload(ActionType.OPEN_URL, payload))
    }

    @Test
    fun `OPEN_URL with valid https passes`() {
        val payload = ActionPayload.OpenUrl("https://example.com").toJson()
        assertNull(ActionValidator.validatePayload(ActionType.OPEN_URL, payload))
    }

    @Test
    fun `OPEN_URL with file scheme returns error`() {
        val payload = ActionPayload.OpenUrl("file:///etc/passwd").toJson()
        assertNotNull(ActionValidator.validatePayload(ActionType.OPEN_URL, payload))
    }

    @Test
    fun `OPEN_URL without scheme returns error`() {
        val payload = ActionPayload.OpenUrl("example.com").toJson()
        assertNotNull(ActionValidator.validatePayload(ActionType.OPEN_URL, payload))
    }

    // ── Payload validation: DELAY ────────────────────────────

    @Test
    fun `DELAY with zero duration returns error`() {
        val payload = ActionPayload.Delay(0).toJson()
        assertNotNull(ActionValidator.validatePayload(ActionType.DELAY, payload))
    }

    @Test
    fun `DELAY with negative duration returns error`() {
        val payload = ActionPayload.Delay(-100).toJson()
        assertNotNull(ActionValidator.validatePayload(ActionType.DELAY, payload))
    }

    @Test
    fun `DELAY with valid duration passes`() {
        val payload = ActionPayload.Delay(1000).toJson()
        assertNull(ActionValidator.validatePayload(ActionType.DELAY, payload))
    }

    @Test
    fun `DELAY at max duration passes`() {
        val payload = ActionPayload.Delay(ActionValidator.MAX_DELAY_MS).toJson()
        assertNull(ActionValidator.validatePayload(ActionType.DELAY, payload))
    }

    @Test
    fun `DELAY over max duration returns error`() {
        val payload = ActionPayload.Delay(ActionValidator.MAX_DELAY_MS + 1).toJson()
        assertNotNull(ActionValidator.validatePayload(ActionType.DELAY, payload))
    }

    // ── Full validation ──────────────────────────────────────

    @Test
    fun `full validation passes for valid OPEN_APP`() {
        val payload = ActionPayload.OpenApp("com.termux").toJson()
        assertNull(ActionValidator.validate("Open Termux", "desc", ActionType.OPEN_APP, payload))
    }

    @Test
    fun `full validation fails for blank name`() {
        val payload = ActionPayload.OpenApp("com.termux").toJson()
        assertNotNull(ActionValidator.validate("", "desc", ActionType.OPEN_APP, payload))
    }
}
