package com.nexus.app

import com.nexus.app.domain.model.automation.AutomationValidation
import com.nexus.app.domain.model.automation.TriggerType
import org.json.JSONObject
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class Phase9ValidationTest {

    @Test
    fun `NFC payload passes validation`() {
        assertNull(AutomationValidation.validateTriggerPayload(TriggerType.NFC_TAG_DETECTED, "{}"))
    }

    @Test
    fun `valid geofence passes`() {
        val payload = JSONObject().apply {
            put("geofenceId", "home")
            put("latitude", 28.6139)
            put("longitude", 77.2090)
            put("radiusMeters", 500.0)
        }.toString()
        assertNull(AutomationValidation.validateTriggerPayload(TriggerType.GEOFENCE_ENTER, payload))
    }

    @Test
    fun `geofence with invalid latitude fails`() {
        val payload = JSONObject().apply {
            put("latitude", 100.0)
            put("longitude", 77.0)
            put("radiusMeters", 500.0)
        }.toString()
        assertNotNull(AutomationValidation.validateTriggerPayload(TriggerType.GEOFENCE_ENTER, payload))
    }

    @Test
    fun `geofence with zero radius fails`() {
        val payload = JSONObject().apply {
            put("latitude", 28.0)
            put("longitude", 77.0)
            put("radiusMeters", 0.0)
        }.toString()
        assertNotNull(AutomationValidation.validateTriggerPayload(TriggerType.GEOFENCE_ENTER, payload))
    }

    @Test
    fun `calendar payload passes`() {
        assertNull(AutomationValidation.validateTriggerPayload(TriggerType.CALENDAR_EVENT_START, "{}"))
    }

    @Test
    fun `notification payload passes`() {
        assertNull(AutomationValidation.validateTriggerPayload(TriggerType.NOTIFICATION_POSTED, "{}"))
    }

    @Test
    fun `ALL_CONDITIONS validates composite`() {
        val payload = JSONObject().apply {
            put("operator", "ALL")
            put("childConditions", org.json.JSONArray().apply { put("a"); put("b") })
        }.toString()
        assertNull(AutomationValidation.validateTriggerPayload(TriggerType.ALL_CONDITIONS, payload))
    }

    @Test
    fun `ALL_CONDITIONS with empty children fails`() {
        val payload = JSONObject().apply {
            put("operator", "ALL")
            put("childConditions", org.json.JSONArray())
        }.toString()
        assertNotNull(AutomationValidation.validateTriggerPayload(TriggerType.ALL_CONDITIONS, payload))
    }
}
