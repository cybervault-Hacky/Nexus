package com.nexus.app.data.repository

import com.nexus.app.data.local.EventHistoryDao
import com.nexus.app.data.local.EventHistoryEntity
import com.nexus.app.domain.model.automation.TriggerEvent
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Stores lightweight event history metadata.
 * Maximum 500 records — oldest auto-pruned.
 */
class EventHistoryRepositoryImpl(private val dao: EventHistoryDao) {

    fun observeRecent(limit: Int = 100): Flow<List<EventHistoryEntity>> = dao.observeRecent(limit)

    suspend fun recordEvent(event: TriggerEvent, matchedCount: Int) {
        dao.insert(
            EventHistoryEntity(
                id = UUID.randomUUID().toString(),
                source = event.javaClass.simpleName,
                eventType = event.toTypeName(),
                timestamp = System.currentTimeMillis(),
                matchedAutomationCount = matchedCount,
            )
        )
        // Auto-prune
        if (dao.count() > 500) dao.keepMostRecent(500)
    }

    suspend fun clearAll() = dao.deleteAll()
}

private fun TriggerEvent.toTypeName(): String = when (this) {
    is TriggerEvent.Manual -> "MANUAL"
    is TriggerEvent.Time -> "TIME"
    is TriggerEvent.AppOpened -> "APP_OPEN"
    is TriggerEvent.AppClosed -> "APP_CLOSE"
    is TriggerEvent.ContextActivated -> "CONTEXT"
    is TriggerEvent.WifiConnected -> "WIFI_ON"
    is TriggerEvent.WifiDisconnected -> "WIFI_OFF"
    is TriggerEvent.BluetoothConnected -> "BT_ON"
    is TriggerEvent.BluetoothDisconnected -> "BT_OFF"
    is TriggerEvent.ChargingStarted -> "CHARGE_ON"
    is TriggerEvent.ChargingStopped -> "CHARGE_OFF"
    is TriggerEvent.BatteryLevelChanged -> "BATTERY"
    is TriggerEvent.DeviceBoot -> "BOOT"
    is TriggerEvent.ScreenOn -> "SCREEN_ON"
    is TriggerEvent.ScreenOff -> "SCREEN_OFF"
    is TriggerEvent.DeviceIdle -> "IDLE"
    is TriggerEvent.DeviceActive -> "ACTIVE"
    is TriggerEvent.NfcTagDetected -> "NFC"
    is TriggerEvent.NfcTagRemoved -> "NFC_OFF"
    is TriggerEvent.GeofenceEntered -> "GEOFENCE_IN"
    is TriggerEvent.GeofenceExited -> "GEOFENCE_OUT"
    is TriggerEvent.CalendarEventStarted -> "CAL_START"
    is TriggerEvent.CalendarEventEnded -> "CAL_END"
    is TriggerEvent.NotificationPosted -> "NOTIF"
    is TriggerEvent.NotificationRemoved -> "NOTIF_OFF"
}
