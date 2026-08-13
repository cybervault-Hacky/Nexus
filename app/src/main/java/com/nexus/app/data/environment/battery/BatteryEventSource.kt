package com.nexus.app.data.environment.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.nexus.app.domain.event.EnvironmentEventSource
import com.nexus.app.domain.model.automation.TriggerEvent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Monitors battery level changes.
 * Uses ACTION_BATTERY_CHANGED broadcasts.
 * Deduplicates identical level events.
 */
class BatteryEventSource(private val context: Context) : EnvironmentEventSource {
    override val sourceId = "battery"
    override val displayName = "Battery Level"
    override fun isSupported() = true

    override fun start() { }
    override fun stop() { }

    override fun events(): Flow<TriggerEvent> = callbackFlow {
        val br = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    if (level >= 0 && scale > 0) {
                        val percent = (level * 100) / scale
                        trySend(TriggerEvent.BatteryLevelChanged(percent))
                    }
                }
            }
        }
        context.registerReceiver(br, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        awaitClose { try { context.unregisterReceiver(br) } catch (_: Exception) { } }
    }.distinctUntilChanged { old, new ->
        old is TriggerEvent.BatteryLevelChanged && new is TriggerEvent.BatteryLevelChanged &&
            old.percent == new.percent
    }
}
