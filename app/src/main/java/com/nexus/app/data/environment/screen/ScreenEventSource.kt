package com.nexus.app.data.environment.screen

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.nexus.app.domain.event.EnvironmentEventSource
import com.nexus.app.domain.model.automation.TriggerEvent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Monitors screen on/off events.
 * Uses ACTION_SCREEN_ON / ACTION_SCREEN_OFF.
 * These must be registered programmatically (not in manifest).
 */
class ScreenEventSource(private val context: Context) : EnvironmentEventSource {
    override val sourceId = "screen"
    override val displayName = "Screen State"
    override fun isSupported() = true

    private var receiver: BroadcastReceiver? = null

    override fun start() { }

    override fun stop() {
        receiver?.let { try { context.unregisterReceiver(it) } catch (_: Exception) { } }
        receiver = null
    }

    override fun events(): Flow<TriggerEvent> = callbackFlow {
        val br = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_SCREEN_ON -> trySend(TriggerEvent.ScreenOn())
                    Intent.ACTION_SCREEN_OFF -> trySend(TriggerEvent.ScreenOff())
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        context.registerReceiver(br, filter)
        receiver = br
        awaitClose { try { context.unregisterReceiver(br) } catch (_: Exception) { } }
    }
}
