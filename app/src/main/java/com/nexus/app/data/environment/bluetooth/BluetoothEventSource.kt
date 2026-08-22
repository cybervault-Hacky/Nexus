package com.nexus.app.data.environment.bluetooth

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.nexus.app.domain.event.EnvironmentEventSource
import com.nexus.app.domain.model.automation.TriggerEvent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Monitors Bluetooth connection state.
 * Uses ACTION_ACL_CONNECTED / ACTION_ACL_DISCONNECTED broadcasts.
 * Does NOT scan or perform device discovery.
 */
class BluetoothEventSource(private val context: Context) : EnvironmentEventSource {
    override val sourceId = "bluetooth"
    override val displayName = "Bluetooth"

    /**
     * Check hardware and permission without calling BluetoothAdapter APIs.
     * BluetoothAdapter.getDefaultAdapter() itself is permission-gated for apps
     * targeting Android 12, so it is not a safe capability probe there.
     */
    override fun isSupported(): Boolean =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) &&
            hasConnectPermission()

    fun hasConnectPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT,
            ) == PackageManager.PERMISSION_GRANTED

    override fun start() = Unit
    override fun stop() = Unit

    override fun events(): Flow<TriggerEvent> = callbackFlow {
        if (!isSupported()) {
            close()
            return@callbackFlow
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context?, intent: Intent?) {
                // Permission can be revoked while this source is active. Do not
                // touch BluetoothDevice properties after that happens.
                if (!hasConnectPermission()) return

                val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent?.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE,
                        BluetoothDevice::class.java,
                    )
                } else {
                    @Suppress("DEPRECATION")
                    intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                }
                val deviceName = device?.name ?: device?.address ?: "Unknown"
                when (intent?.action) {
                    BluetoothDevice.ACTION_ACL_CONNECTED ->
                        trySend(TriggerEvent.BluetoothConnected(deviceName))
                    BluetoothDevice.ACTION_ACL_DISCONNECTED ->
                        trySend(TriggerEvent.BluetoothDisconnected(deviceName))
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }
        context.registerReceiver(receiver, filter)
        awaitClose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: IllegalArgumentException) {
                // Already unregistered during source shutdown.
            }
        }
    }
}
