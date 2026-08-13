package com.nexus.app.domain.model

/**
 * Domain model for an Action belonging to a Context.
 * The [payload] is a JSON string whose schema depends on [type].
 * The domain layer does not parse the payload — that happens in
 * the execution layer via [ActionPayload].
 */
data class NexusAction(
    val id: String,
    val contextId: String,
    val name: String,
    val description: String,
    val type: ActionType,
    val payload: String,
    val isEnabled: Boolean = true,
    val position: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
