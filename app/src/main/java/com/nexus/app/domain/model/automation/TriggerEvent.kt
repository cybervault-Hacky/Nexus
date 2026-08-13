package com.nexus.app.domain.model.automation

/**
 * Represents a trigger event that the engine processes.
 * Events are produced by Android components and consumed by TriggerEngine.
 */
sealed class TriggerEvent {
    abstract val automationId: String?

    data class Manual(override val automationId: String) : TriggerEvent()
    data class Time(override val automationId: String?) : TriggerEvent()
    data class AppOpened(val packageName: String, override val automationId: String? = null) : TriggerEvent()
    data class AppClosed(val packageName: String, override val automationId: String? = null) : TriggerEvent()
    data class ContextActivated(val contextId: String, override val automationId: String? = null) : TriggerEvent()
}
