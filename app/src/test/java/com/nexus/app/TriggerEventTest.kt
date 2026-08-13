package com.nexus.app

import com.nexus.app.domain.model.automation.TriggerEvent
import com.nexus.app.domain.model.automation.TriggerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class TriggerEventTest {

    @Test
    fun `Manual event has correct automationId`() {
        val event = TriggerEvent.Manual("auto1")
        assertEquals("auto1", event.automationId)
    }

    @Test
    fun `Time event with specific automation`() {
        val event = TriggerEvent.Time("auto1")
        assertEquals("auto1", event.automationId)
    }

    @Test
    fun `Time event with null automation matches all`() {
        val event = TriggerEvent.Time(null)
        assertNull(event.automationId)
    }

    @Test
    fun `AppOpened event has package name`() {
        val event = TriggerEvent.AppOpened("com.termux")
        assertEquals("com.termux", event.packageName)
        assertNull(event.automationId)
    }

    @Test
    fun `ContextActivated event has context id`() {
        val event = TriggerEvent.ContextActivated("ctx1")
        assertEquals("ctx1", event.contextId)
        assertNull(event.automationId)
    }
}
