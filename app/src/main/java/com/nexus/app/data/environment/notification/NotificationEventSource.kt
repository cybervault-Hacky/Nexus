package com.nexus.app.data.environment.notification

import android.content.Context
import android.provider.Settings
import com.nexus.app.domain.event.EnvironmentEventSource
import com.nexus.app.domain.model.automation.TriggerEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Notification event source.
 * Requires NotificationListenerService to be enabled by the user.
 * This source reports capability; actual events come from the listener service.
 */
class NotificationEventSource(private val context: Context) : EnvironmentEventSource {
    override val sourceId = "notifications"
    override val displayName = "Notifications"

    fun isListenerEnabled(): Boolean {
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        return flat?.contains(context.packageName) == true
    }

    override fun isSupported() = true
    override fun start() { }
    override fun stop() { }
    override fun events(): Flow<TriggerEvent> = emptyFlow() // Events dispatched via service
}
