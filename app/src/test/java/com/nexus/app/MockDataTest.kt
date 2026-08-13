package com.nexus.app

import com.nexus.app.data.mock.MockData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests that remaining mock data (capsules only) is consistent.
 * Context data has been removed from MockData — it now lives in Room.
 */
class MockDataTest {

    @Test
    fun `mock capsules list has at least one item`() {
        assertTrue(MockData.capsules.isNotEmpty())
    }

    @Test
    fun `mock capsules have unique ids`() {
        val ids = MockData.capsules.map { it.id }.toSet()
        assertEquals(MockData.capsules.size, ids.size)
    }

    @Test
    fun `all capsules have positive item counts`() {
        MockData.capsules.forEach { cap ->
            assertTrue("${cap.name} itemCount should be > 0", cap.itemCount > 0)
        }
    }

    @Test
    fun `all capsules have past timestamps`() {
        MockData.capsules.forEach { cap ->
            assertTrue(
                "${cap.name} lastUsedTimestamp should be in the past",
                cap.lastUsedTimestamp <= System.currentTimeMillis(),
            )
        }
    }
}
