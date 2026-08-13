package com.nexus.app.data.automation.capability

import android.content.Context
import android.nfc.NfcAdapter
import android.os.Build
import android.provider.Settings

/**
 * Centralized capability checker for NEXUS features.
 * Returns capability state without requesting permissions.
 */
class CapabilityManager(private val context: Context) {

    fun checkNfc(): CapabilityState {
        val adapter = NfcAdapter.getDefaultAdapter(context)
            ?: return CapabilityState.UNSUPPORTED
        return if (adapter.isEnabled) CapabilityState.SUPPORTED
        else CapabilityState.DISABLED
    }

    fun checkLocation(): CapabilityState {
        val pm = context.packageManager
        val hasFeature = pm.hasSystemFeature(android.content.pm.PackageManager.FEATURE_LOCATION)
        if (!hasFeature) return CapabilityState.UNSUPPORTED
        // Check if permission is granted
        val granted = context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
        return if (granted) CapabilityState.SUPPORTED else CapabilityState.PERMISSION_REQUIRED
    }

    fun checkBackgroundLocation(): CapabilityState {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return CapabilityState.SUPPORTED
        val granted = context.checkSelfPermission(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
        return if (granted) CapabilityState.SUPPORTED else CapabilityState.PERMISSION_REQUIRED
    }

    fun checkCalendar(): CapabilityState {
        val granted = context.checkSelfPermission(android.Manifest.permission.READ_CALENDAR) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
        return if (granted) CapabilityState.SUPPORTED else CapabilityState.PERMISSION_REQUIRED
    }

    fun checkNotificationAccess(): CapabilityState {
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        return if (flat?.contains(context.packageName) == true) CapabilityState.SUPPORTED
        else CapabilityState.PERMISSION_REQUIRED
    }
}

enum class CapabilityState {
    SUPPORTED,
    PERMISSION_REQUIRED,
    PERMISSION_DENIED,
    DISABLED,
    UNSUPPORTED,
    ERROR,
}
