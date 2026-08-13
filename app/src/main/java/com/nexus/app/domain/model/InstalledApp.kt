package com.nexus.app.domain.model

/**
 * Represents an installed Android application.
 * This model is UI/framework-safe — it contains no Android-specific objects.
 * The [packageName] is the stable identity used for all persistence.
 */
data class InstalledApp(
    val packageName: String,
    val appName: String,
    val isLaunchable: Boolean = true,
)
