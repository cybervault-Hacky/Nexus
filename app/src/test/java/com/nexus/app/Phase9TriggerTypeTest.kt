package com.nexus.app

import com.nexus.app.domain.model.automation.TriggerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Phase9TriggerTypeTest {

    @Test
    fun `TriggerType has 28 values`() {
        assertEquals(28, TriggerType.entries.size)
    }

    @Test
    fun `Phase 9 types exist`() {
        assertTrue(TriggerType.entries.contains(TriggerType.NFC_TAG_DETECTED))
        assertTrue(TriggerType.entries.contains(TriggerType.NFC_TAG_REMOVED))
        assertTrue(TriggerType.entries.contains(TriggerType.GEOFENCE_ENTER))
        assertTrue(TriggerType.entries.contains(TriggerType.GEOFENCE_EXIT))
        assertTrue(TriggerType.entries.contains(TriggerType.CALENDAR_EVENT_START))
        assertTrue(TriggerType.entries.contains(TriggerType.CALENDAR_EVENT_END))
        assertTrue(TriggerType.entries.contains(TriggerType.NOTIFICATION_POSTED))
        assertTrue(TriggerType.entries.contains(TriggerType.NOTIFICATION_REMOVED))
        assertTrue(TriggerType.entries.contains(TriggerType.ALL_CONDITIONS))
        assertTrue(TriggerType.entries.contains(TriggerType.ANY_CONDITION))
    }

    @Test
    fun `all Phase 7 types still exist`() {
        assertTrue(TriggerType.entries.contains(TriggerType.MANUAL))
        assertTrue(TriggerType.entries.contains(TriggerType.TIME))
        assertTrue(TriggerType.entries.contains(TriggerType.APP_OPEN))
    }

    @Test
    fun `all Phase 8 types still exist`() {
        assertTrue(TriggerType.entries.contains(TriggerType.WIFI_CONNECTED))
        assertTrue(TriggerType.entries.contains(TriggerType.BATTERY_BELOW))
        assertTrue(TriggerType.entries.contains(TriggerType.DEVICE_BOOT))
    }
}
