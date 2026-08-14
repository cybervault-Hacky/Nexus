package com.nexus.app

import com.nexus.app.data.mock.MockData
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Tests for the remaining MockData object.
 *
 * Context data was removed in Phase 2 (Room-backed) and capsule data in
 * Phase 5 (Room-backed), so MockData is now an empty placeholder object.
 * This test simply guards that the object still exists for any remaining
 * Phase 1 UI dependencies until it is fully removed.
 */
class MockDataTest {

    @Test
    fun `MockData object exists`() {
        assertNotNull(MockData)
    }
}
