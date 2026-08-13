package com.nexus.app.data.environment.idle

import android.content.Context
import android.os.Build
import android.os.PowerManager
import com.nexus.app.domain.event.EnvironmentEventSource
import com.nexus.app.domain.model.automation.TriggerEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Monitors device idle/active state using PowerManager.
 * Uses polling since there's no direct broadcast for idle state.
 * Polls every 60 seconds — lightweight and battery-safe.
 */
class IdleStateEventSource(private val context: Context) : EnvironmentEventSource {
    override val sourceId = "idle"
    override val displayName = "Idle State"
    override fun isSupported() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

    private var running = false

    override fun start() { running = true }
    override fun stop() { running = false }

    override fun events(): Flow<TriggerEvent> = flow {
        var lastIdle = false
        while (running) {
            try {
                val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                val isIdle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    pm.isDeviceIdleMode
                } else false
                if (isIdle && !lastIdle) emit(TriggerEvent.DeviceIdle())
                else if (!isIdle && lastIdle) emit(TriggerEvent.DeviceActive())
                lastIdle = isIdle
            } catch (_: Exception) { }
            delay(60_000)
        }
    }
}
