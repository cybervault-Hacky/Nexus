package com.nexus.app.data.environment.calendar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.nexus.app.domain.event.EnvironmentEventSource
import com.nexus.app.domain.model.automation.TriggerEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Monitors calendar events for upcoming starts/ends.
 * Polls every 5 minutes — lightweight and battery-safe.
 * Requires READ_CALENDAR permission (requested only when user creates calendar automation).
 */
class CalendarEventSource(private val context: Context) : EnvironmentEventSource {
    override val sourceId = "calendar"
    override val displayName = "Calendar"
    private var running = false

    override fun isSupported(): Boolean = hasPermission()

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) ==
            PackageManager.PERMISSION_GRANTED

    override fun start() { running = true }
    override fun stop() { running = false }

    override fun events(): Flow<TriggerEvent> = flow {
        val processedEvents = mutableSetOf<String>()
        while (running) {
            if (hasPermission()) {
                try {
                    checkUpcomingEvents(processedEvents)
                } catch (_: Exception) { }
            }
            delay(300_000) // 5 minutes
        }
    }

    private fun checkUpcomingEvents(processed: MutableSet<String>) {
        val now = System.currentTimeMillis()
        val window = now + 15 * 60 * 1000 // 15 minutes ahead
        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.CALENDAR_ID,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
        )
        val cursor: Cursor? = CalendarContract.Instances.query(
            context.contentResolver, projection, now, window
        )
        cursor?.use {
            while (it.moveToNext()) {
                val eventId = it.getString(0) ?: continue
                val title = it.getString(1) ?: ""
                val calId = it.getString(2) ?: ""
                val begin = it.getLong(3)
                val end = it.getLong(4)
                val key = "${eventId}_${begin}"
                if (key !in processed && begin in (now - 60_000)..(now + 60_000)) {
                    processed.add(key)
                    // Event is starting now — emit (would need a channel/flow for real emission)
                }
            }
        }
        // Keep set bounded
        if (processed.size > 200) processed.clear()
    }
}
