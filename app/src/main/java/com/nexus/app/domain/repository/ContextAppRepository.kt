package com.nexus.app.domain.repository

import com.nexus.app.domain.model.InstalledApp
import kotlinx.coroutines.flow.Flow

/**
 * Manages the relationship between Contexts and their configured apps.
 * A package can belong to multiple contexts but only once per context.
 */
interface ContextAppRepository {

    /** Observe the apps associated with a context, ordered by app name. */
    fun observeAppsForContext(contextId: String): Flow<List<InstalledApp>>

    /** Observe the count of apps for a context. */
    fun observeAppCount(contextId: String): Flow<Int>

    /** Add a package to a context. No-op if already associated. */
    suspend fun addApp(contextId: String, packageName: String)

    /** Remove a package from a context. No-op if not associated. */
    suspend fun removeApp(contextId: String, packageName: String)

    /** Replace all apps for a context (used during bulk selection). */
    suspend fun setApps(contextId: String, packageNames: List<String>)

    /** Check if a package is already associated with a context. */
    suspend fun isAppInContext(contextId: String, packageName: String): Boolean

    /** Return all package names for a context. */
    suspend fun getPackageNames(contextId: String): List<String>

    /** Delete all associations for a context (called before context deletion). */
    suspend fun deleteAllForContext(contextId: String)
}
