package com.nexus.app

import com.nexus.app.domain.event.EnvironmentEventSourceRegistry
import com.nexus.app.domain.model.automation.TriggerEvent
import com.nexus.app.domain.model.automation.TriggerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EnvironmentEventTest {

    @Test
    fun `TriggerType has 28 values`() {
        // 5 (Phase 7) + 13 (Phase 8) + 10 (Phase 9, incl. composites) = 28
        assertEquals(28, TriggerType.entries.size)
    }

    @Test
    fun `TriggerType includes all Phase 7 types`() {
        assertTrue(TriggerType.entries.contains(TriggerType.MANUAL))
        assertTrue(TriggerType.entries.contains(TriggerType.TIME))
        assertTrue(TriggerType.entries.contains(TriggerType.APP_OPEN))
        assertTrue(TriggerType.entries.contains(TriggerType.APP_CLOSE))
        assertTrue(TriggerType.entries.contains(TriggerType.CONTEXT_ACTIVATED))
    }

    @Test
    fun `TriggerType includes all Phase 8 types`() {
        assertTrue(TriggerType.entries.contains(TriggerType.WIFI_CONNECTED))
        assertTrue(TriggerType.entries.contains(TriggerType.WIFI_DISCONNECTED))
        assertTrue(TriggerType.entries.contains(TriggerType.BLUETOOTH_CONNECTED))
        assertTrue(TriggerType.entries.contains(TriggerType.BLUETOOTH_DISCONNECTED))
        assertTrue(TriggerType.entries.contains(TriggerType.CHARGING_STARTED))
        assertTrue(TriggerType.entries.contains(TriggerType.CHARGING_STOPPED))
        assertTrue(TriggerType.entries.contains(TriggerType.BATTERY_BELOW))
        assertTrue(TriggerType.entries.contains(TriggerType.BATTERY_ABOVE))
        assertTrue(TriggerType.entries.contains(TriggerType.DEVICE_BOOT))
        assertTrue(TriggerType.entries.contains(TriggerType.SCREEN_ON))
        assertTrue(TriggerType.entries.contains(TriggerType.SCREEN_OFF))
        assertTrue(TriggerType.entries.contains(TriggerType.DEVICE_IDLE))
        assertTrue(TriggerType.entries.contains(TriggerType.DEVICE_ACTIVE))
    }

    @Test
    fun `EnvironmentEventSourceRegistry register and enable`() {
        val registry = EnvironmentEventSourceRegistry()
        val source = FakeEventSource("test", "Test Source", supported = true)
        registry.register(source)
        registry.enable("test")
        assertTrue(registry.isEnabled("test"))
    }

    @Test
    fun `EnvironmentEventSourceRegistry disable removes source`() {
        val registry = EnvironmentEventSourceRegistry()
        val source = FakeEventSource("test", "Test Source", supported = true)
        registry.register(source)
        registry.enable("test")
        registry.disable("test")
        assertFalse(registry.isEnabled("test"))
    }

    @Test
    fun `EnvironmentEventSourceRegistry getSupportedSources filters`() {
        val registry = EnvironmentEventSourceRegistry()
        registry.register(FakeEventSource("a", "A", supported = true))
        registry.register(FakeEventSource("b", "B", supported = false))
        val supported = registry.getSupportedSources()
        assertEquals(1, supported.size)
        assertEquals("a", supported[0].sourceId)
    }
}

private class FakeEventSource(
    override val sourceId: String,
    override val displayName: String,
    private val supported: Boolean,
) : com.nexus.app.domain.event.EnvironmentEventSource {
    override fun isSupported() = supported
    override fun start() {}
    override fun stop() {}
    override fun events() = kotlinx.coroutines.flow.emptyFlow<TriggerEvent>()
}
