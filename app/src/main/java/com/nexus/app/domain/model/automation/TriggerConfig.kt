package com.nexus.app.domain.model.automation

/**
 * Strongly-typed trigger configuration.
 * Each variant maps to a [TriggerType].
 */
sealed class TriggerConfig {
    data object Manual : TriggerConfig()

    data class Time(
        val hour: Int,
        val minute: Int,
        /** Bitmask: 1=Mon, 2=Tue, 4=Wed, 8=Thu, 16=Fri, 32=Sat, 64=Sun */
        val daysOfWeek: Int,
    ) : TriggerConfig()

    data class AppOpen(val packageName: String) : TriggerConfig()
    data class AppClose(val packageName: String) : TriggerConfig()
    data class ContextActivated(val contextId: String) : TriggerConfig()
}
