package com.nexus.app.data.environment.wifi

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import androidx.core.content.ContextCompat
import com.nexus.app.domain.event.EnvironmentEventSource
import com.nexus.app.domain.model.automation.TriggerEvent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Monitors Wi-Fi connection state with ConnectivityManager callbacks.
 * SSID access is best-effort because Android intentionally redacts it unless
 * the user has granted location access and device location is enabled.
 */
class WifiEventSource(private val context: Context) : EnvironmentEventSource {
    override val sourceId = "wifi"
    override val displayName = "Wi-Fi"

    override fun isSupported(): Boolean =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI) &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_NETWORK_STATE,
            ) == PackageManager.PERMISSION_GRANTED

    override fun start() = Unit
    override fun stop() = Unit

    override fun events(): Flow<TriggerEvent> = callbackFlow {
        if (!isSupported()) {
            close()
            return@callbackFlow
        }

        var wasConnected = false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (connectivityManager == null) {
            close()
            return@callbackFlow
        }

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                if (!wasConnected) {
                    trySend(TriggerEvent.WifiConnected(getSsid()))
                    wasConnected = true
                }
            }

            override fun onLost(network: Network) {
                if (wasConnected) {
                    trySend(TriggerEvent.WifiDisconnected())
                    wasConnected = false
                }
            }
        }

        connectivityManager.registerNetworkCallback(request, callback)
        awaitClose {
            try {
                connectivityManager.unregisterNetworkCallback(callback)
            } catch (_: IllegalArgumentException) {
                // Callback was already removed during source shutdown.
            }
        }
    }

    private fun getSsid(): String {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return ""
        }

        return try {
            val wifiManager =
                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            @Suppress("DEPRECATION")
            wifiManager?.connectionInfo?.ssid?.removeSurrounding("\"") ?: ""
        } catch (_: SecurityException) {
            ""
        }
    }
}
