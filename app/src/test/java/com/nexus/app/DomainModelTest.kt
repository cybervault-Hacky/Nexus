package com.nexus.app

import com.nexus.app.domain.model.NexusCapsule
import com.nexus.app.domain.model.NexusContext
import com.nexus.app.domain.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the domain models.
 * Updated for Phase 2 — NexusContext now includes timestamps and iconId.
 */
class DomainModelTest {

    // ── NexusContext ─────────────────────────────────────────

    @Test
    fun `NexusContext holds correct data`() {
        val ctx = NexusContext(
            id = "test",
            name = "Test Context",
            description = "A test",
            appCount = 3,
            actionCount = 1,
            isActive = true,
        )
        assertEquals("test", ctx.id)
        assertEquals("Test Context", ctx.name)
        assertTrue(ctx.isActive)
    }

    @Test
    fun `NexusContext default isActive is false`() {
        val ctx = NexusContext(id = "x", name = "X", description = "")
        assertFalse(ctx.isActive)
    }

    @Test
    fun `NexusContext default iconId is grid`() {
        val ctx = NexusContext(id = "x", name = "X", description = "")
        assertEquals("grid", ctx.iconId)
    }

    @Test
    fun `NexusContext default appCount and actionCount are zero`() {
        val ctx = NexusContext(id = "x", name = "X", description = "")
        assertEquals(0, ctx.appCount)
        assertEquals(0, ctx.actionCount)
    }

    @Test
    fun `NexusContext timestamps default to current time`() {
        val before = System.currentTimeMillis()
        val ctx = NexusContext(id = "x", name = "X", description = "")
        val after = System.currentTimeMillis()
        assertTrue(ctx.createdAt in before..after)
        assertTrue(ctx.updatedAt in before..after)
    }

    @Test
    fun `NexusContext copy preserves id`() {
        val original = NexusContext(id = "abc", name = "Original", description = "Desc")
        val copy = original.copy(name = "Modified")
        assertEquals("abc", copy.id)
        assertEquals("Modified", copy.name)
        assertEquals("Desc", copy.description)
    }

    @Test
    fun `NexusContext copies are independent`() {
        val original = NexusContext(id = "abc", name = "Original", description = "Desc")
        val copy = original.copy(name = "Modified", id = "xyz")
        assertNotEquals(original.id, copy.id)
        assertNotEquals(original.name, copy.name)
        assertEquals("Original", original.name) // Original unchanged
    }

    @Test
    fun `NexusContext accentColor has default value`() {
        val ctx = NexusContext(id = "x", name = "X", description = "")
        assertEquals(0xFF6366F1, ctx.accentColor)
    }

    // ── NexusCapsule ─────────────────────────────────────────

    @Test
    fun `NexusCapsule holds correct data`() {
        val cap = NexusCapsule(
            id = "cap1", sourceContextId = "ctx1", name = "My Capsule", description = "Desc",
            appSnapshots = listOf(
                com.nexus.app.domain.model.AppSnapshot(packageName = "com.example.a", appName = "A"),
                com.nexus.app.domain.model.AppSnapshot(packageName = "com.example.b", appName = "B"),
            ),
            actionSnapshots = listOf(
                com.nexus.app.domain.model.ActionSnapshot(
                    name = "Open A",
                    description = "",
                    type = com.nexus.app.domain.model.ActionType.OPEN_APP,
                    payload = "{\"packageName\":\"com.example.a\"}",
                    position = 0,
                ),
            ),
        )
        assertEquals("cap1", cap.id)
        assertEquals("ctx1", cap.sourceContextId)
        assertEquals(2, cap.appCount)
        assertEquals(1, cap.actionCount)
    }

    @Test
    fun `NexusCapsule accentColor has default value`() {
        val cap = NexusCapsule(
            id = "x", sourceContextId = "ctx", name = "X", description = "",
        )
        assertEquals(0xFF8B5CF6, cap.accentColor)
    }

    // ── ThemeMode ────────────────────────────────────────────

    @Test
    fun `ThemeMode has three values`() {
        assertEquals(3, ThemeMode.entries.size)
    }

    @Test
    fun `ThemeMode contains expected values`() {
        assertTrue(ThemeMode.entries.contains(ThemeMode.DARK))
        assertTrue(ThemeMode.entries.contains(ThemeMode.LIGHT))
        assertTrue(ThemeMode.entries.contains(ThemeMode.SYSTEM))
    }
}
