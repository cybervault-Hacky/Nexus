package com.nexus.app

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nexus.app.data.local.ActionDao
import com.nexus.app.data.local.ActionEntity
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
 * Instrumented tests for the Action DAO and database migration.
 */
@RunWith(AndroidJUnit4::class)
class ActionDatabaseTest {

    private lateinit var database: NexusDatabase
    private lateinit var contextDao: ContextDao
    private lateinit var actionDao: ActionDao

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, NexusDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        contextDao = database.contextDao()
        actionDao = database.actionDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    private fun makeContext(id: String = "ctx_1") = ContextEntity(
        id = id, name = "Coding", description = "Test", iconId = "code",
        appCount = 0, actionCount = 0, isActive = false,
        accentColor = 0xFF6366F1, createdAt = 1000L, updatedAt = 2000L,
    )

    private fun makeAction(
        id: String = "a1",
        contextId: String = "ctx_1",
        name: String = "Open Termux",
        type: String = "OPEN_APP",
        payload: String = """{"packageName":"com.termux"}""",
        isEnabled: Boolean = true,
        position: Int = 0,
    ) = ActionEntity(
        id = id, contextId = contextId, name = name, description = "",
        type = type, payload = payload, isEnabled = isEnabled,
        position = position, createdAt = 1000L, updatedAt = 2000L,
    )

    // ── Insert & Retrieve ────────────────────────────────────

    @Test
    fun insertAndRetrieveAction() = runTest {
        contextDao.insert(makeContext())
        actionDao.insert(makeAction())
        val retrieved = actionDao.getById("a1")
        assertNotNull(retrieved)
        assertEquals("Open Termux", retrieved!!.name)
    }

    @Test
    fun insertMultipleActions() = runTest {
        contextDao.insert(makeContext())
        actionDao.insert(makeAction(id = "a1", position = 0))
        actionDao.insert(makeAction(id = "a2", position = 1))
        val all = actionDao.getByContext("ctx_1")
        assertEquals(2, all.size)
    }

    // ── Ordering ─────────────────────────────────────────────

    @Test
    fun actionsOrderedByPosition() = runTest {
        contextDao.insert(makeContext())
        actionDao.insert(makeAction(id = "a2", name = "Second", position = 1))
        actionDao.insert(makeAction(id = "a1", name = "First", position = 0))
        val all = actionDao.getByContext("ctx_1")
        assertEquals("First", all[0].name)
        assertEquals("Second", all[1].name)
    }

    @Test
    fun observeActionsOrderedByPosition() = runTest {
        contextDao.insert(makeContext())
        actionDao.insert(makeAction(id = "a2", position = 1))
        actionDao.insert(makeAction(id = "a1", position = 0))
        val all = actionDao.observeByContext("ctx_1").first()
        assertEquals("a1", all[0].id)
        assertEquals("a2", all[1].id)
    }

    // ── Update ───────────────────────────────────────────────

    @Test
    fun updateAction() = runTest {
        contextDao.insert(makeContext())
        actionDao.insert(makeAction())
        val entity = actionDao.getById("a1")!!
        actionDao.update(entity.copy(name = "Modified"))
        assertEquals("Modified", actionDao.getById("a1")!!.name)
    }

    // ── Delete ───────────────────────────────────────────────

    @Test
    fun deleteAction() = runTest {
        contextDao.insert(makeContext())
        actionDao.insert(makeAction())
        actionDao.deleteById("a1")
        assertNull(actionDao.getById("a1"))
    }

    @Test
    fun deleteAllForContext() = runTest {
        contextDao.insert(makeContext())
        actionDao.insert(makeAction(id = "a1"))
        actionDao.insert(makeAction(id = "a2"))
        actionDao.deleteAllForContext("ctx_1")
        assertEquals(0, actionDao.getByContext("ctx_1").size)
    }

    // ── CASCADE ──────────────────────────────────────────────

    @Test
    fun deleteContext_cascadesToActions() = runTest {
        contextDao.insert(makeContext())
        actionDao.insert(makeAction(id = "a1"))
        actionDao.insert(makeAction(id = "a2"))
        assertEquals(2, actionDao.getByContext("ctx_1").size)

        contextDao.deleteById("ctx_1")
        assertEquals(0, actionDao.getByContext("ctx_1").size)
    }

    // ── Enable/Disable ───────────────────────────────────────

    @Test
    fun setEnabled_togglesState() = runTest {
        contextDao.insert(makeContext())
        actionDao.insert(makeAction(isEnabled = true))
        actionDao.setEnabled("a1", false, 5000L)
        assertEquals(false, actionDao.getById("a1")!!.isEnabled)
    }

    @Test
    fun setEnabled_reEnable() = runTest {
        contextDao.insert(makeContext())
        actionDao.insert(makeAction(isEnabled = false))
        actionDao.setEnabled("a1", true, 5000L)
        assertEquals(true, actionDao.getById("a1")!!.isEnabled)
    }

    // ── Count ────────────────────────────────────────────────

    @Test
    fun observeCount_returnsCorrectCount() = runTest {
        contextDao.insert(makeContext())
        actionDao.insert(makeAction(id = "a1"))
        actionDao.insert(makeAction(id = "a2"))
        val count = actionDao.observeCount("ctx_1").first()
        assertEquals(2, count)
    }

    @Test
    fun getCount_returnsCorrectCount() = runTest {
        contextDao.insert(makeContext())
        actionDao.insert(makeAction(id = "a1"))
        actionDao.insert(makeAction(id = "a2"))
        actionDao.insert(makeAction(id = "a3"))
        val count = actionDao.getCount("ctx_1")
        assertEquals(3, count)
    }

    // ── Position ─────────────────────────────────────────────

    @Test
    fun setPosition_updatesPosition() = runTest {
        contextDao.insert(makeContext())
        actionDao.insert(makeAction(position = 0))
        actionDao.setPosition("a1", 5, 5000L)
        assertEquals(5, actionDao.getById("a1")!!.position)
    }

    // ── Context isolation ────────────────────────────────────

    @Test
    fun actionsIsolatedByContext() = runTest {
        contextDao.insert(makeContext(id = "ctx_1"))
        contextDao.insert(makeContext(id = "ctx_2"))
        actionDao.insert(makeAction(id = "a1", contextId = "ctx_1"))
        actionDao.insert(makeAction(id = "a2", contextId = "ctx_2"))
        assertEquals(1, actionDao.getByContext("ctx_1").size)
        assertEquals(1, actionDao.getByContext("ctx_2").size)
    }

    // ── Observe by ID ────────────────────────────────────────

    @Test
    fun observeById_emitsUpdates() = runTest {
        contextDao.insert(makeContext())
        actionDao.insert(makeAction(name = "Original"))
        val observed = actionDao.observeById("a1").first()
        assertEquals("Original", observed!!.name)
    }
}
