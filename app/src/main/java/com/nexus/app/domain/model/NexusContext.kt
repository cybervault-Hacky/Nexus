package com.nexus.app.domain.model

/**
 * Represents a workflow context — a group of apps and actions
 * that form a coherent workspace.
 *
 * The domain model is UI/framework-independent. The [iconId] string
 * is resolved to a visual icon by the presentation layer.
 */
data class NexusContext(
    val id: String,
    val name: String,
    val description: String,
    val iconId: String = "grid",
    val appCount: Int = 0,
    val actionCount: Int = 0,
    val isActive: Boolean = false,
    val accentColor: Long = 0xFF6366F1,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
