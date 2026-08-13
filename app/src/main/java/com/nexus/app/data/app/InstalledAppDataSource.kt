package com.nexus.app.data.app

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import com.nexus.app.domain.model.InstalledApp

/**
 * Discovers installed launchable applications using Android's PackageManager.
 *
 * Results are cached in memory for the application lifecycle.
 * Call [invalidateCache] after significant package changes (install/uninstall).
 */
class InstalledAppDataSource(private val applicationContext: Context) {

    @Volatile
    private var cachedApps: List<InstalledApp>? = null

    /**
     * Return all launchable applications, sorted by display name.
     * Uses the MAIN/LAUNCHER intent to discover user-facing apps.
     */
    fun getInstalledApps(forceRefresh: Boolean = false): List<InstalledApp> {
        if (!forceRefresh) cachedApps?.let { return it }

        val mainIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val pm = applicationContext.packageManager
        val resolveInfos: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(
                mainIntent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()),
            )
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(mainIntent, PackageManager.MATCH_DEFAULT_ONLY)
        }

        val apps = resolveInfos
            .filter { it.activityInfo != null }
            .map { resolveInfo ->
                val activityInfo = resolveInfo.activityInfo
                val packageName = activityInfo.packageName
                val appName = try {
                    val appInfo = pm.getApplicationInfo(packageName, 0)
                    pm.getApplicationLabel(appInfo).toString()
                } catch (_: PackageManager.NameNotFoundException) {
                    packageName
                }
                InstalledApp(
                    packageName = packageName,
                    appName = appName,
                    isLaunchable = true,
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.appName.lowercase() }

        cachedApps = apps
        return apps
    }

    /**
     * Resolve a single package name to an InstalledApp.
     * Returns null if the package is not installed or not launchable.
     */
    fun resolveApp(packageName: String): InstalledApp? {
        // Check cache first
        cachedApps?.find { it.packageName == packageName }?.let { return it }

        // Resolve from PackageManager
        val pm = applicationContext.packageManager
        val appName = try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (_: PackageManager.NameNotFoundException) {
            return null
        }

        // Check if it has a launcher intent
        val launchIntent = pm.getLaunchIntentForPackage(packageName)
        val isLaunchable = launchIntent != null

        return InstalledApp(
            packageName = packageName,
            appName = appName,
            isLaunchable = isLaunchable,
        )
    }

    /** Clear the cached list so the next call re-queries PackageManager. */
    fun invalidateCache() {
        cachedApps = null
    }
}
