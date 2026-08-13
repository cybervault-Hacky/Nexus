package com.nexus.app.domain.model.automation

/**
 * Domain model for an automation rule.
 * Persisted in Room; UI/framework-free.
 */
data class AutomationRule(
    val id: String,
    val name: String,
    val description: String,
    val isEnabled: Boolean = true,
    val triggerType: TriggerType,
    /** JSON payload whose schema depends on [triggerType]. */
    val triggerPayload: String,
    /** The context whose actions form the workflow to execute. */
    val contextId: String,
    val cooldownSeconds: Int = 60,
    val lastTriggeredAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
