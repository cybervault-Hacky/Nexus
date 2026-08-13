package com.nexus.app.domain.model

/**
 * Represents a quick action available to the user.
 * The UI icon is resolved in the presentation layer, keeping
 * the domain model free of Compose dependencies.
 * Phase 4 connects the real Action Engine to this model.
 */
data class QuickAction(
    val id: String,
    val label: String,
    val description: String,
)
