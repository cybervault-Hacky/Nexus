package com.nexus.app.data.repository

import com.nexus.app.data.app.InstalledAppDataSource
import com.nexus.app.domain.model.InstalledApp
import com.nexus.app.domain.repository.InstalledAppRepository

/**
 * Wraps [InstalledAppDataSource] behind the domain [InstalledAppRepository] interface.
 */
class InstalledAppRepositoryImpl(
    private val dataSource: InstalledAppDataSource,
) : InstalledAppRepository {

    override suspend fun getInstalledApps(forceRefresh: Boolean): List<InstalledApp> =
        dataSource.getInstalledApps(forceRefresh)

    override suspend fun searchApps(query: String): List<InstalledApp> {
        if (query.isBlank()) return dataSource.getInstalledApps()
        val lowerQuery = query.lowercase()
        return dataSource.getInstalledApps().filter { app ->
            app.appName.lowercase().contains(lowerQuery) ||
                app.packageName.lowercase().contains(lowerQuery)
        }
    }

    override suspend fun resolveApp(packageName: String): InstalledApp? =
        dataSource.resolveApp(packageName)
}
