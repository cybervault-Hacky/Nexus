package com.nexus.app.data.automation.capability

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * Centralized capability checker for NEXUS features.
 * Returns capability state without requesting permissions.
 */
class CapabilityManager(private val context: Context) {

    fun checkNfc(): CapabilityState {
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_NFC)) {
            return CapabilityState.UNSUPPORTED
        }
        val adapter = try {
            NfcAdapter.getDefaultAdapter(context)
        } catch (_: SecurityException) {
            return CapabilityState.PERMISSION_REQUIRED
        } catch (_: UnsupportedOperationException) {
            return CapabilityState.UNSUPPORTED
        } ?: return CapabilityState.UNSUPPORTED

        return try {
            if (adapter.isEnabled) CapabilityState.SUPPORTED else CapabilityState.DISABLED
        } catch (_: SecurityException) {
            CapabilityState.PERMISSION_REQUIRED
        }
    }

    fun checkBluetooth(): CapabilityState {
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            return CapabilityState.UNSUPPORTED
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return CapabilityState.PERMISSION_REQUIRED
        }
        return CapabilityState.SUPPORTED
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
