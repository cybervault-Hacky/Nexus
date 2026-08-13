package com.nexus.app.data.environment.power

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

/**
 * Monitors charging state transitions.
 * Uses ACTION_POWER_CONNECTED / ACTION_POWER_DISCONNECTED broadcasts.
 */
class PowerEventSource(private val context: Context) : EnvironmentEventSource {
    override val sourceId = "power"
    override val displayName = "Power / Charging"
    override fun isSupported() = true

    private var receiver: BroadcastReceiver? = null

    override fun start() {
        if (receiver != null) return
        receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                // This receiver is used with callbackFlow below
            }
        }
    }

    override fun stop() {
        receiver?.let { try { context.unregisterReceiver(it) } catch (_: Exception) { } }
        receiver = null
    }

    override fun events(): Flow<TriggerEvent> = callbackFlow {
        val br = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_POWER_CONNECTED -> trySend(TriggerEvent.ChargingStarted())
                    Intent.ACTION_POWER_DISCONNECTED -> trySend(TriggerEvent.ChargingStopped())
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        context.registerReceiver(br, filter)
        receiver = br
        awaitClose { try { context.unregisterReceiver(br) } catch (_: Exception) { } }
    }
}
