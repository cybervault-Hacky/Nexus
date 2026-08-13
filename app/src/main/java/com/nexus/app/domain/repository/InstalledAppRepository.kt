package com.nexus.app.domain.repository

import com.nexus.app.domain.model.InstalledApp

/**
 * Provides access to installed applications on the device.
 * Implementations live in the data layer and use Android APIs.
 */
interface InstalledAppRepository {

    /**
     * Return all launchable installed applications, sorted by display name.
     * Results are cached within the application lifecycle.
     */
    suspend fun getInstalledApps(forceRefresh: Boolean = false): List<InstalledApp>

    /**
     * Filter installed apps by query.
     * Searches both app name and package name (case-insensitive).
     */
    suspend fun searchApps(query: String): List<InstalledApp>

    /**
     * Resolve a package name to its current [InstalledApp].
     * Returns null if the package is not installed or not launchable.
     */
    suspend fun resolveApp(packageName: String): InstalledApp?
}
