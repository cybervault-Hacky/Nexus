package com.nexus.app

import com.nexus.app.data.automation.EventDeduplicator
import com.nexus.app.domain.model.automation.TriggerEvent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EventDeduplicatorTest {

    @Test
    fun `first event is processed`() {
        val dedup = EventDeduplicator(windowMs = 5000)
        assertTrue(dedup.shouldProcess(TriggerEvent.ChargingStarted()))
    }

    @Test
    fun `duplicate event within window is rejected`() {
        val dedup = EventDeduplicator(windowMs = 5000)
        dedup.shouldProcess(TriggerEvent.ChargingStarted())
        assertFalse(dedup.shouldProcess(TriggerEvent.ChargingStarted()))
    }

    @Test
    fun `different events are both processed`() {
        val dedup = EventDeduplicator(windowMs = 5000)
        assertTrue(dedup.shouldProcess(TriggerEvent.ChargingStarted()))
        assertTrue(dedup.shouldProcess(TriggerEvent.ChargingStopped()))
    }

    @Test
    fun `same event after window is processed again`() {
        val dedup = EventDeduplicator(windowMs = 1) // 1ms window
        dedup.shouldProcess(TriggerEvent.ScreenOn())
        Thread.sleep(5)
        assertTrue(dedup.shouldProcess(TriggerEvent.ScreenOn()))
    }

    @Test
    fun `battery events at different levels are distinct`() {
        val dedup = EventDeduplicator(windowMs = 5000)
        assertTrue(dedup.shouldProcess(TriggerEvent.BatteryLevelChanged(50)))
        assertTrue(dedup.shouldProcess(TriggerEvent.BatteryLevelChanged(49)))
    }

    @Test
    fun `battery events at same level are deduplicated`() {
        val dedup = EventDeduplicator(windowMs = 5000)
        assertTrue(dedup.shouldProcess(TriggerEvent.BatteryLevelChanged(50)))
        assertFalse(dedup.shouldProcess(TriggerEvent.BatteryLevelChanged(50)))
    }
}
