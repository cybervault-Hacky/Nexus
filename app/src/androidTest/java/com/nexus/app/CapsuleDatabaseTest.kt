package com.nexus.app

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nexus.app.data.local.ActionDao
import com.nexus.app.data.local.ActionEntity
import com.nexus.app.data.local.CapsuleActionDao
import com.nexus.app.data.local.CapsuleActionEntity
import com.nexus.app.data.local.CapsuleAppDao
import com.nexus.app.data.local.CapsuleAppEntity
import com.nexus.app.data.local.CapsuleDao
import com.nexus.app.data.local.CapsuleEntity
import com.nexus.app.data.local.ContextAppDao
import com.nexus.app.data.local.ContextAppEntity
import com.nexus.app.data.local.ContextDao
import com.nexus.app.data.local.ContextEntity
import com.nexus.app.data.local.NexusDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for the Capsule DAOs.
 * Includes mandatory snapshot isolation tests and context deletion behavior.
 */
@RunWith(AndroidJUnit4::class)
class CapsuleDatabaseTest {

    private lateinit var database: NexusDatabase
    private lateinit var capsuleDao: CapsuleDao
    private lateinit var capsuleAppDao: CapsuleAppDao
    private lateinit var capsuleActionDao: CapsuleActionDao
    private lateinit var contextDao: ContextDao
    private lateinit var contextAppDao: ContextAppDao
    private lateinit var actionDao: ActionDao

    @Before
    fun createDb() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(ctx, NexusDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        capsuleDao = database.capsuleDao()
        capsuleAppDao = database.capsuleAppDao()
        capsuleActionDao = database.capsuleActionDao()
        contextDao = database.contextDao()
        contextAppDao = database.contextAppDao()
        actionDao = database.actionDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    // ── Helpers ──────────────────────────────────────────────

    private fun makeContext(id: String = "ctx1") = ContextEntity(
        id = id, name = "Coding", description = "Dev", iconId = "code",
        appCount = 0, actionCount = 0, isActive = false,
        accentColor = 0xFF6366F1, createdAt = 1000L, updatedAt = 2000L,
    )

    private fun makeCapsule(
        id: String = "cap1",
        sourceContextId: String = "ctx1",
        name: String = "Coding Snapshot",
    ) = CapsuleEntity(
        id = id,
        sourceContextId = sourceContextId,
        name = name,
        description = "Test capsule",
        schemaVersion = 1,
        accentColor = 0xFF8B5CF6,
        contextSnapshot = """{"name":"Coding","description":"Dev","iconId":"code","accentColor":6632225}""",
        createdAt = 1000L,
        capturedAt = 1000L,
    )

    // ── Capsule CRUD ─────────────────────────────────────────

    @Test
    fun insertAndRetrieveCapsule() = runTest {
        capsuleDao.insert(makeCapsule())
        val retrieved = capsuleDao.getById("cap1")
        assertNotNull(retrieved)
        assertEquals("Coding Snapshot", retrieved!!.name)
        assertEquals("ctx1", retrieved.sourceContextId)
        assertEquals(1, retrieved.schemaVersion)
    }

    @Test
    fun insertMultipleCapsules() = runTest {
        capsuleDao.insert(makeCapsule(id = "cap1", name = "First"))
        capsuleDao.insert(makeCapsule(id = "cap2", name = "Second"))
        val all = capsuleDao.observeAll().first()
        assertEquals(2, all.size)
    }

    @Test
    fun observeAll_returnsOrderedByCapturedAt() = runTest {
        capsuleDao.insert(makeCapsule(id = "cap1", name = "Older").copy(capturedAt = 1000L))
        capsuleDao.insert(makeCapsule(id = "cap2", name = "Newer").copy(capturedAt = 9000L))
        val all = capsuleDao.observeAll().first()
        assertEquals("Newer", all[0].name)
        assertEquals("Older", all[1].name)
    }

    @Test
    fun observeById_emitsUpdates() = runTest {
        capsuleDao.insert(makeCapsule(name = "Original"))
        val observed = capsuleDao.observeById("cap1").first()
        assertEquals("Original", observed!!.name)
    }

    @Test
    fun rename_updatesOnlyName() = runTest {
        capsuleDao.insert(makeCapsule())
        capsuleDao.updateName("cap1", "Renamed")
        val updated = capsuleDao.getById("cap1")!!
        assertEquals("Renamed", updated.name)
        assertEquals("Test capsule", updated.description)
    }

    @Test
    fun updateDescription_updatesOnlyDescription() = runTest {
        capsuleDao.insert(makeCapsule())
        capsuleDao.updateDescription("cap1", "New description")
        val updated = capsuleDao.getById("cap1")!!
        assertEquals("Coding Snapshot", updated.name)
        assertEquals("New description", updated.description)
    }

    // ── Delete ───────────────────────────────────────────────

    @Test
    fun deleteCapsule_cascadesToApps() = runTest {
        capsuleDao.insert(makeCapsule())
        capsuleAppDao.insert(CapsuleAppEntity(capsuleId = "cap1", packageName = "com.a", appName = "A", position = 0))
        capsuleAppDao.insert(CapsuleAppEntity(capsuleId = "cap1", packageName = "com.b", appName = "B", position = 1))
        assertEquals(2, capsuleAppDao.getByCapsule("cap1").size)

        capsuleDao.deleteById("cap1")
        assertEquals(0, capsuleAppDao.getByCapsule("cap1").size)
    }

    @Test
    fun deleteCapsule_cascadesToActions() = runTest {
        capsuleDao.insert(makeCapsule())
        capsuleActionDao.insert(CapsuleActionEntity(capsuleId = "cap1", name = "A", description = "", type = "OPEN_APP", payload = "{}", isEnabled = true, position = 0))
        assertEquals(1, capsuleActionDao.getByCapsule("cap1").size)

        capsuleDao.deleteById("cap1")
        assertEquals(0, capsuleActionDao.getByCapsule("cap1").size)
    }

    // ── Capsule Apps ─────────────────────────────────────────

    @Test
    fun addAppsToCapsule() = runTest {
        capsuleDao.insert(makeCapsule())
        capsuleAppDao.insert(CapsuleAppEntity(capsuleId = "cap1", packageName = "com.termux", appName = "Termux", position = 0))
        capsuleAppDao.insert(CapsuleAppEntity(capsuleId = "cap1", packageName = "com.chrome", appName = "Chrome", position = 1))
        val apps = capsuleAppDao.getByCapsule("cap1")
        assertEquals(2, apps.size)
        assertEquals("Termux", apps[0].appName)
    }

    // ── Capsule Actions ──────────────────────────────────────

    @Test
    fun addActionsToCapsule() = runTest {
        capsuleDao.insert(makeCapsule())
        capsuleActionDao.insert(CapsuleActionEntity(capsuleId = "cap1", name = "Open Termux", description = "", type = "OPEN_APP", payload = "{}", isEnabled = true, position = 0))
        capsuleActionDao.insert(CapsuleActionEntity(capsuleId = "cap1", name = "Delay", description = "", type = "DELAY", payload = "{}", isEnabled = true, position = 1))
        val actions = capsuleActionDao.getByCapsule("cap1")
        assertEquals(2, actions.size)
        assertEquals("Open Termux", actions[0].name)
    }

    @Test
    fun observeActions_returnsOrderedByPosition() = runTest {
        capsuleDao.insert(makeCapsule())
        capsuleActionDao.insert(CapsuleActionEntity(capsuleId = "cap1", name = "Second", description = "", type = "DELAY", payload = "{}", isEnabled = true, position = 1))
        capsuleActionDao.insert(CapsuleActionEntity(capsuleId = "cap1", name = "First", description = "", type = "OPEN_APP", payload = "{}", isEnabled = true, position = 0))
        val actions = capsuleActionDao.observeByCapsule("cap1").first()
        assertEquals("First", actions[0].name)
        assertEquals("Second", actions[1].name)
    }

    // ── Snapshot Isolation (Mandatory) ───────────────────────

    @Test
    fun snapshotIsolation_capsuleUnaffectedByContextChanges() = runTest {
        // 1. Create context with App A and Action A
        contextDao.insert(makeContext())
        contextAppDao.insert(ContextAppEntity(contextId = "ctx1", packageName = "com.appa"))
        actionDao.insert(ActionEntity(
            id = "a1", contextId = "ctx1", name = "Action A", description = "",
            type = "OPEN_APP", payload = "{}", isEnabled = true, position = 0,
            createdAt = 1000L, updatedAt = 1000L,
        ))

        // 2. Capture capsule
        capsuleDao.insert(makeCapsule())
        capsuleAppDao.insert(CapsuleAppEntity(capsuleId = "cap1", packageName = "com.appa", appName = "App A", position = 0))
        capsuleActionDao.insert(CapsuleActionEntity(capsuleId = "cap1", name = "Action A", description = "", type = "OPEN_APP", payload = "{}", isEnabled = true, position = 0))

        // 3. Modify context: add App B and Action B
        contextAppDao.insert(ContextAppEntity(contextId = "ctx1", packageName = "com.appb"))
        actionDao.insert(ActionEntity(
            id = "a2", contextId = "ctx1", name = "Action B", description = "",
            type = "DELAY", payload = "{}", isEnabled = true, position = 1,
            createdAt = 2000L, updatedAt = 2000L,
        ))

        // 4. Read capsule — must still contain only App A and Action A
        val capsuleApps = capsuleAppDao.getByCapsule("cap1")
        val capsuleActions = capsuleActionDao.getByCapsule("cap1")
        assertEquals(1, capsuleApps.size)
        assertEquals("com.appa", capsuleApps[0].packageName)
        assertEquals(1, capsuleActions.size)
        assertEquals("Action A", capsuleActions[0].name)
    }

    // ── Capsule Survives Context Deletion (Mandatory) ────────

    @Test
    fun capsuleSurvivesContextDeletion() = runTest {
        // 1. Create context and capsule
        contextDao.insert(makeContext())
        capsuleDao.insert(makeCapsule())
        capsuleAppDao.insert(CapsuleAppEntity(capsuleId = "cap1", packageName = "com.a", appName = "A", position = 0))
        capsuleActionDao.insert(CapsuleActionEntity(capsuleId = "cap1", name = "Act", description = "", type = "OPEN_APP", payload = "{}", isEnabled = true, position = 0))

        // 2. Delete context
        contextDao.deleteById("ctx1")
        assertNull(contextDao.getById("ctx1"))

        // 3. Capsule must still exist with all data
        val capsule = capsuleDao.getById("cap1")
        assertNotNull(capsule)
        assertEquals("Coding Snapshot", capsule!!.name)
        assertEquals("ctx1", capsule.sourceContextId)

        val apps = capsuleAppDao.getByCapsule("cap1")
        assertEquals(1, apps.size)
        assertEquals("com.a", apps[0].packageName)

        val actions = capsuleActionDao.getByCapsule("cap1")
        assertEquals(1, actions.size)
        assertEquals("Act", actions[0].name)
    }

    // ── Search ───────────────────────────────────────────────

    @Test
    fun search_byName() = runTest {
        capsuleDao.insert(makeCapsule(id = "cap1", name = "Friday Coding"))
        capsuleDao.insert(makeCapsule(id = "cap2", name = "Study Session"))
        val results = capsuleDao.search("coding").first()
        assertEquals(1, results.size)
        assertEquals("Friday Coding", results[0].name)
    }

    @Test
    fun search_byDescription() = runTest {
        capsuleDao.insert(makeCapsule(id = "cap1").copy(description = "My coding setup"))
        capsuleDao.insert(makeCapsule(id = "cap2").copy(description = "Study notes"))
        val results = capsuleDao.search("coding").first()
        assertEquals(1, results.size)
    }

    @Test
    fun search_caseInsensitive() = runTest {
        capsuleDao.insert(makeCapsule(name = "Friday Coding"))
        val results = capsuleDao.search("FRIDAY").first()
        assertEquals(1, results.size)
    }

    @Test
    fun search_noResults() = runTest {
        capsuleDao.insert(makeCapsule())
        val results = capsuleDao.search("nonexistent").first()
        assertEquals(0, results.size)
    }

    // ── Context-scoped queries ───────────────────────────────

    @Test
    fun observeByContext_returnsOnlyMatching() = runTest {
        capsuleDao.insert(makeCapsule(id = "cap1", sourceContextId = "ctx1"))
        capsuleDao.insert(makeCapsule(id = "cap2", sourceContextId = "ctx2"))
        capsuleDao.insert(makeCapsule(id = "cap3", sourceContextId = "ctx1"))
        val results = capsuleDao.observeByContext("ctx1").first()
        assertEquals(2, results.size)
    }

    @Test
    fun observeCountByContext_returnsCorrectCount() = runTest {
        capsuleDao.insert(makeCapsule(id = "cap1", sourceContextId = "ctx1"))
        capsuleDao.insert(makeCapsule(id = "cap2", sourceContextId = "ctx2"))
        capsuleDao.insert(makeCapsule(id = "cap3", sourceContextId = "ctx1"))
        val count = capsuleDao.observeCountByContext("ctx1").first()
        assertEquals(2, count)
    }

    // ── Update ───────────────────────────────────────────────

    @Test
    fun updateCapsule() = runTest {
        capsuleDao.insert(makeCapsule())
        val entity = capsuleDao.getById("cap1")!!
        capsuleDao.update(entity.copy(description = "Updated"))
        assertEquals("Updated", capsuleDao.getById("cap1")!!.description)
    }
}
