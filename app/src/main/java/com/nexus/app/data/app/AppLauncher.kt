package com.nexus.app.data.app

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

/**
 * Safely launches installed applications.
 * Keeps Android Intent logic out of the domain and UI layers.
 */
class AppLauncher(private val applicationContext: Context) {

    /**
     * Check if a package can be launched.
     * Returns false if the package is not installed or has no launch intent.
     */
    fun canLaunch(packageName: String): Boolean {
        return applicationContext.packageManager.getLaunchIntentForPackage(packageName) != null
    }

    /**
     * Attempt to launch the given package.
     * Returns true if the launch intent was started, false if the
     * package is not installed or has no launchable activity.
     */
    fun launch(packageName: String): Boolean {
        val intent = applicationContext.packageManager.getLaunchIntentForPackage(packageName)
            ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            applicationContext.startActivity(intent)
            return true
        } catch (_: Exception) {
            return false
        }
    }

    /**
     * Check if a package is currently installed.
     */
    fun isInstalled(packageName: String): Boolean {
        return try {
            applicationContext.packageManager.getApplicationInfo(packageName, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }
}
