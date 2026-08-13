package com.nexus.app

import com.nexus.app.domain.model.smart.AutomationPriority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AutomationPriorityTest {

    @Test
    fun `priority has four levels`() {
        assertEquals(4, AutomationPriority.entries.size)
    }

    @Test
    fun `priority levels are ordered`() {
        assertTrue(AutomationPriority.LOW.level < AutomationPriority.NORMAL.level)
        assertTrue(AutomationPriority.NORMAL.level < AutomationPriority.HIGH.level)
        assertTrue(AutomationPriority.HIGH.level < AutomationPriority.CRITICAL.level)
    }

    @Test
    fun `default priority is NORMAL`() {
        assertEquals(1, AutomationPriority.NORMAL.level)
    }
}
