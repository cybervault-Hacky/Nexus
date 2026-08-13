package com.nexus.app.data.environment.boot

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
 * Monitors device boot events.
 * Uses ACTION_BOOT_COMPLETED.
 * Note: Also needs BOOT_COMPLETED permission in manifest.
 */
class BootEventSource(private val context: Context) : EnvironmentEventSource {
    override val sourceId = "boot"
    override val displayName = "Device Boot"
    override fun isSupported() = true

    override fun start() { }
    override fun stop() { }

    override fun events(): Flow<TriggerEvent> = callbackFlow {
        val br = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
                    trySend(TriggerEvent.DeviceBoot())
                }
            }
        }
        context.registerReceiver(br, IntentFilter(Intent.ACTION_BOOT_COMPLETED))
        awaitClose { try { context.unregisterReceiver(br) } catch (_: Exception) { } }
    }
}
