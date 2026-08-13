package com.nexus.app

import com.nexus.app.ui.screens.contexts.ContextViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Tests for context input validation.
 * These run as pure JVM tests — no Android framework needed.
 */
class ContextValidationTest {

    @Test
    fun `blank name returns error`() {
        val result = ContextViewModel.validateInput("", "desc")
        assertNotNull(result)
        assertEquals("Name cannot be empty", result)
    }

    @Test
    fun `whitespace-only name returns error`() {
        val result = ContextViewModel.validateInput("   ", "desc")
        assertNotNull(result)
        assertEquals("Name cannot be empty", result)
    }

    @Test
    fun `valid name passes`() {
        val result = ContextViewModel.validateInput("Coding", "A coding context")
        assertNull(result)
    }

    @Test
    fun `name at max length passes`() {
        val name = "A".repeat(ContextViewModel.MAX_NAME_LENGTH)
        val result = ContextViewModel.validateInput(name, "")
        assertNull(result)
    }

    @Test
    fun `name over max length returns error`() {
        val name = "A".repeat(ContextViewModel.MAX_NAME_LENGTH + 1)
        val result = ContextViewModel.validateInput(name, "")
        assertNotNull(result)
    }

    @Test
    fun `description at max length passes`() {
        val desc = "A".repeat(ContextViewModel.MAX_DESCRIPTION_LENGTH)
        val result = ContextViewModel.validateInput("Name", desc)
        assertNull(result)
    }

    @Test
    fun `description over max length returns error`() {
        val desc = "A".repeat(ContextViewModel.MAX_DESCRIPTION_LENGTH + 1)
        val result = ContextViewModel.validateInput("Name", desc)
        assertNotNull(result)
    }

    @Test
    fun `empty description is valid`() {
        val result = ContextViewModel.validateInput("Name", "")
        assertNull(result)
    }
}
