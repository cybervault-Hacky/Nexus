package com.nexus.app

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nexus.app.data.local.ContextDao
import com.nexus.app.data.local.ContextAppDao
import com.nexus.app.data.local.ContextAppEntity
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
 * Instrumented tests for the Room DAOs.
 * Uses an in-memory database so tests are isolated and fast.
 */
@RunWith(AndroidJUnit4::class)
class ContextDatabaseTest {

    private lateinit var database: NexusDatabase
    private lateinit var contextDao: ContextDao
    private lateinit var contextAppDao: ContextAppDao

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, NexusDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        contextDao = database.contextDao()
        contextAppDao = database.contextAppDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    // ── Helpers ──────────────────────────────────────────────

    private fun makeContext(
        id: String = "ctx_1",
        name: String = "Coding",
        isActive: Boolean = false,
    ) = ContextEntity(
        id = id,
        name = name,
        description = "A test context",
        iconId = "code",
        appCount = 0,
        actionCount = 0,
        isActive = isActive,
        accentColor = 0xFF6366F1,
        createdAt = 1000L,
        updatedAt = 2000L,
    )

    private fun makeContextApp(
        contextId: String = "ctx_1",
        packageName: String = "com.example.app",
    ) = ContextAppEntity(contextId = contextId, packageName = packageName)

    // ── Context DAO ──────────────────────────────────────────

    @Test
    fun insertAndRetrieveContext() = runTest {
        contextDao.insert(makeContext())
        val retrieved = contextDao.getById("ctx_1")
        assertNotNull(retrieved)
        assertEquals("Coding", retrieved!!.name)
    }

    @Test
    fun insertMultipleContexts() = runTest {
        contextDao.insert(makeContext(id = "ctx_1", name = "Coding"))
        contextDao.insert(makeContext(id = "ctx_2", name = "Study"))
        val all = contextDao.observeAll().first()
        assertEquals(2, all.size)
    }

    @Test
    fun getContextById_returnsNull_forMissingId() = runTest {
        val result = contextDao.getById("nonexistent")
        assertNull(result)
    }

    @Test
    fun observeContexts_returnsOrderedByUpdatedAt() = runTest {
        contextDao.insert(makeContext(id = "ctx_1", name = "Older").copy(updatedAt = 1000L))
        contextDao.insert(makeContext(id = "ctx_2", name = "Newer").copy(updatedAt = 9000L))
        val all = contextDao.observeAll().first()
        assertEquals("Newer", all[0].name)
        assertEquals("Older", all[1].name)
    }

    @Test
    fun updateContext_modifiesEntity() = runTest {
        contextDao.insert(makeContext(id = "ctx_1", name = "Original"))
        val entity = contextDao.getById("ctx_1")!!
        contextDao.update(entity.copy(name = "Modified"))
        val updated = contextDao.getById("ctx_1")!!
        assertEquals("Modified", updated.name)
    }

    @Test
    fun deleteContext_removesEntity() = runTest {
        contextDao.insert(makeContext(id = "ctx_1"))
        contextDao.deleteById("ctx_1")
        assertNull(contextDao.getById("ctx_1"))
    }

    @Test
    fun deleteContext_cascadesToApps() = runTest {
        contextDao.insert(makeContext(id = "ctx_1"))
        contextAppDao.insert(makeContextApp(contextId = "ctx_1", packageName = "com.a"))
        contextAppDao.insert(makeContextApp(contextId = "ctx_1", packageName = "com.b"))
        assertEquals(2, contextAppDao.getByContext("ctx_1").size)

        contextDao.deleteById("ctx_1")
        assertEquals(0, contextAppDao.getByContext("ctx_1").size)
    }

    @Test
    fun setActive_makesContextActive() = runTest {
        contextDao.insert(makeContext(id = "ctx_1", isActive = false))
        contextDao.setActive("ctx_1", isActive = true, updatedAt = 5000L)
        assertTrue(contextDao.getById("ctx_1")!!.isActive)
    }

    @Test
    fun deactivateAll_deactivatesAllContexts() = runTest {
        contextDao.insert(makeContext(id = "ctx_1", isActive = true))
        contextDao.insert(makeContext(id = "ctx_2", isActive = true))
        contextDao.deactivateAll()
        val all = contextDao.observeAll().first()
        assertTrue(all.none { it.isActive })
    }

    @Test
    fun observeActive_returnsOnlyActiveContext() = runTest {
        contextDao.insert(makeContext(id = "ctx_1", name = "Inactive", isActive = false))
        contextDao.insert(makeContext(id = "ctx_2", name = "Active", isActive = true))
        val active = contextDao.observeActive().first()
        assertNotNull(active)
        assertEquals("Active", active!!.name)
    }

    @Test
    fun activateSingleContext_enforcesInvariant() = runTest {
        contextDao.insert(makeContext(id = "ctx_1", isActive = true))
        contextDao.insert(makeContext(id = "ctx_2", isActive = false))
        contextDao.deactivateAll()
        contextDao.setActive("ctx_2", isActive = true, updatedAt = 5000L)
        val active = contextDao.observeActive().first()
        assertEquals("ctx_2", active!!.id)
    }

    // ── Context App DAO ──────────────────────────────────────

    @Test
    fun addAppToContext() = runTest {
        contextDao.insert(makeContext(id = "ctx_1"))
        contextAppDao.insert(makeContextApp(contextId = "ctx_1", packageName = "com.termux"))
        val apps = contextAppDao.getByContext("ctx_1")
        assertEquals(1, apps.size)
        assertEquals("com.termux", apps[0].packageName)
    }

    @Test
    fun addMultipleAppsToContext() = runTest {
        contextDao.insert(makeContext(id = "ctx_1"))
        contextAppDao.insert(makeContextApp(contextId = "ctx_1", packageName = "com.a"))
        contextAppDao.insert(makeContextApp(contextId = "ctx_1", packageName = "com.b"))
        contextAppDao.insert(makeContextApp(contextId = "ctx_1", packageName = "com.c"))
        assertEquals(3, contextAppDao.getByContext("ctx_1").size)
    }

    @Test
    fun addDuplicateApp_preventedByUniqueIndex() = runTest {
        contextDao.insert(makeContext(id = "ctx_1"))
        contextAppDao.insert(makeContextApp(contextId = "ctx_1", packageName = "com.a"))
        contextAppDao.insert(makeContextApp(contextId = "ctx_1", packageName = "com.a")) // IGNORE
        assertEquals(1, contextAppDao.getByContext("ctx_1").size)
    }

    @Test
    fun samePackageInDifferentContexts() = runTest {
        contextDao.insert(makeContext(id = "ctx_1"))
        contextDao.insert(makeContext(id = "ctx_2"))
        contextAppDao.insert(makeContextApp(contextId = "ctx_1", packageName = "com.a"))
        contextAppDao.insert(makeContextApp(contextId = "ctx_2", packageName = "com.a"))
        assertEquals(1, contextAppDao.getByContext("ctx_1").size)
        assertEquals(1, contextAppDao.getByContext("ctx_2").size)
    }

    @Test
    fun removeAppFromContext() = runTest {
        contextDao.insert(makeContext(id = "ctx_1"))
        contextAppDao.insert(makeContextApp(contextId = "ctx_1", packageName = "com.a"))
        contextAppDao.insert(makeContextApp(contextId = "ctx_1", packageName = "com.b"))
        contextAppDao.delete("ctx_1", "com.a")
        val remaining = contextAppDao.getByContext("ctx_1")
        assertEquals(1, remaining.size)
        assertEquals("com.b", remaining[0].packageName)
    }

    @Test
    fun removeApp_noopForNonExistent() = runTest {
        contextDao.insert(makeContext(id = "ctx_1"))
        contextAppDao.delete("ctx_1", "nonexistent") // Should not throw
    }

    @Test
    fun clearContext_removesAllApps() = runTest {
        contextDao.insert(makeContext(id = "ctx_1"))
        contextAppDao.insert(makeContextApp(contextId = "ctx_1", packageName = "com.a"))
        contextAppDao.insert(makeContextApp(contextId = "ctx_1", packageName = "com.b"))
        contextAppDao.clearContext("ctx_1")
        assertEquals(0, contextAppDao.getByContext("ctx_1").size)
    }

    @Test
    fun countApps() = runTest {
        contextDao.insert(makeContext(id = "ctx_1"))
        contextAppDao.insert(makeContextApp(contextId = "ctx_1", packageName = "com.a"))
        contextAppDao.insert(makeContextApp(contextId = "ctx_1", packageName = "com.b"))
        val count = contextAppDao.observeCount("ctx_1").first()
        assertEquals(2, count)
    }

    @Test
    fun countApps_returnsZeroForEmptyContext() = runTest {
        contextDao.insert(makeContext(id = "ctx_1"))
        val count = contextAppDao.observeCount("ctx_1").first()
        assertEquals(0, count)
    }

    @Test
    fun getPackageNames() = runTest {
        contextDao.insert(makeContext(id = "ctx_1"))
        contextAppDao.insert(makeContextApp(contextId = "ctx_1", packageName = "com.b"))
        contextAppDao.insert(makeContextApp(contextId = "ctx_1", packageName = "com.a"))
        val names = contextAppDao.getPackageNames("ctx_1")
        assertEquals(2, names.size)
    }

    @Test
    fun isAppInContext_returnsCorrectly() = runTest {
        contextDao.insert(makeContext(id = "ctx_1"))
        contextAppDao.insert(makeContextApp(contextId = "ctx_1", packageName = "com.a"))
        assertEquals(1, contextAppDao.count("ctx_1", "com.a"))
        assertEquals(0, contextAppDao.count("ctx_1", "com.b"))
    }

    @Test
    fun observeApps_emitsUpdates() = runTest {
        contextDao.insert(makeContext(id = "ctx_1"))
        val initial = contextAppDao.observeByContext("ctx_1").first()
        assertEquals(0, initial.size)

        contextAppDao.insert(makeContextApp(contextId = "ctx_1", packageName = "com.a"))
        val updated = contextAppDao.observeByContext("ctx_1").first()
        assertEquals(1, updated.size)
    }
}
