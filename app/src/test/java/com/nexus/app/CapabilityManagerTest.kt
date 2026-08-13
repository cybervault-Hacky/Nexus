package com.nexus.app

import com.nexus.app.data.automation.capability.CapabilityState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CapabilityManagerTest {

    @Test
    fun `CapabilityState has six values`() {
        assertEquals(6, CapabilityState.entries.size)
    }

    @Test
    fun `CapabilityState includes expected values`() {
        assertTrue(CapabilityState.entries.contains(CapabilityState.SUPPORTED))
        assertTrue(CapabilityState.entries.contains(CapabilityState.PERMISSION_REQUIRED))
        assertTrue(CapabilityState.entries.contains(CapabilityState.PERMISSION_DENIED))
        assertTrue(CapabilityState.entries.contains(CapabilityState.DISABLED))
        assertTrue(CapabilityState.entries.contains(CapabilityState.UNSUPPORTED))
        assertTrue(CapabilityState.entries.contains(CapabilityState.ERROR))
    }
}
