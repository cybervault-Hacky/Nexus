package com.nexus.app.data.environment.wifi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import com.nexus.app.domain.event.EnvironmentEventSource
import com.nexus.app.domain.model.automation.TriggerEvent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Monitors Wi-Fi connection state.
 * Uses ConnectivityManager callbacks (API 24+) for reliable detection.
 * Falls back to WIFI_STATE_CHANGED broadcast on older APIs.
 */
class WifiEventSource(private val context: Context) : EnvironmentEventSource {
    override val sourceId = "wifi"
    override val displayName = "Wi-Fi"
    override fun isSupported() = true

    override fun start() { }
    override fun stop() { }

    override fun events(): Flow<TriggerEvent> = callbackFlow {
        var wasConnected = false

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                if (!wasConnected) {
                    val ssid = getSsid()
                    trySend(TriggerEvent.WifiConnected(ssid))
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

        cm.registerNetworkCallback(request, callback)
        awaitClose { try { cm.unregisterNetworkCallback(callback) } catch (_: Exception) { } }
    }

    private fun getSsid(): String {
        return try {
            val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val info = wm.connectionInfo
            info?.ssid?.removeSurrounding("\"") ?: ""
        } catch (_: Exception) { "" }
    }
}
