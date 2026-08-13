package com.nexus.app

import com.nexus.app.domain.model.ActionSnapshot
import com.nexus.app.domain.model.ActionType
import com.nexus.app.domain.model.AppSnapshot
import com.nexus.app.domain.model.ContextSnapshot
import com.nexus.app.domain.model.NexusCapsule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the Capsule domain model.
 */
class CapsuleModelTest {

    @Test
    fun `NexusCapsule holds correct data`() {
        val capsule = NexusCapsule(
            id = "cap1",
            sourceContextId = "ctx1",
            name = "Coding Snapshot",
            description = "My coding setup",
        )
        assertEquals("cap1", capsule.id)
        assertEquals("ctx1", capsule.sourceContextId)
        assertEquals("Coding Snapshot", capsule.name)
    }

    @Test
    fun `appCount is derived from appSnapshots`() {
        val capsule = NexusCapsule(
            id = "cap1",
            sourceContextId = "ctx1",
            name = "Test",
            description = "",
            appSnapshots = listOf(
                AppSnapshot("com.a", "App A", 0),
                AppSnapshot("com.b", "App B", 1),
            ),
            actionSnapshots = listOf(
                ActionSnapshot("A", "", ActionType.OPEN_APP, "{}", 0),
                ActionSnapshot("B", "", ActionType.OPEN_URL, "{}", 1),
                ActionSnapshot("C", "", ActionType.DELAY, "{}", 2),
            ),
        )
        assertEquals(2, capsule.appCount)
        assertEquals(3, capsule.actionCount)
    }

    @Test
    fun `counts are zero for empty capsule`() {
        val capsule = NexusCapsule(id = "cap1", sourceContextId = "ctx1", name = "Empty", description = "")
        assertEquals(0, capsule.appCount)
        assertEquals(0, capsule.actionCount)
    }

    @Test
    fun `contextSnapshot defaults to null`() {
        val capsule = NexusCapsule(id = "cap1", sourceContextId = "ctx1", name = "Test", description = "")
        assertNull(capsule.contextSnapshot)
    }

    @Test
    fun `ContextSnapshot holds correct data`() {
        val snapshot = ContextSnapshot(
            name = "Coding",
            description = "Dev workspace",
            iconId = "code",
            accentColor = 0xFF6366F1,
        )
        assertEquals("Coding", snapshot.name)
        assertEquals("code", snapshot.iconId)
    }

    @Test
    fun `AppSnapshot holds correct data`() {
        val snapshot = AppSnapshot(
            packageName = "com.termux",
            appName = "Termux",
            position = 0,
        )
        assertEquals("com.termux", snapshot.packageName)
        assertEquals("Termux", snapshot.appName)
    }

    @Test
    fun `ActionSnapshot holds correct data`() {
        val snapshot = ActionSnapshot(
            name = "Open Termux",
            description = "",
            type = ActionType.OPEN_APP,
            payload = """{"packageName":"com.termux"}""",
            position = 0,
            isEnabled = true,
        )
        assertEquals("Open Termux", snapshot.name)
        assertEquals(ActionType.OPEN_APP, snapshot.type)
        assertTrue(snapshot.isEnabled)
    }

    @Test
    fun `schemaVersion defaults to CAPSULE_SCHEMA_VERSION`() {
        val capsule = NexusCapsule(id = "cap1", sourceContextId = "ctx1", name = "Test", description = "")
        assertEquals(NexusCapsule.CAPSULE_SCHEMA_VERSION, capsule.schemaVersion)
        assertEquals(1, capsule.schemaVersion)
    }

    @Test
    fun `NexusCapsule default accentColor is violet`() {
        val capsule = NexusCapsule(id = "cap1", sourceContextId = "ctx1", name = "Test", description = "")
        assertEquals(0xFF8B5CF6, capsule.accentColor)
    }

    @Test
    fun `copy preserves id and sourceContextId`() {
        val original = NexusCapsule(id = "cap1", sourceContextId = "ctx1", name = "Original", description = "Desc")
        val copy = original.copy(name = "Modified")
        assertEquals("cap1", copy.id)
        assertEquals("ctx1", copy.sourceContextId)
        assertEquals("Modified", copy.name)
        assertEquals("Desc", copy.description)
    }

    @Test
    fun `copy does not mutate original`() {
        val original = NexusCapsule(id = "cap1", sourceContextId = "ctx1", name = "Original", description = "Desc")
        val copy = original.copy(name = "Modified")
        assertEquals("Original", original.name)
        assertEquals("Modified", copy.name)
    }
}
