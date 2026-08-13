package com.nexus.app.data.environment.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
    override fun isSupported(): Boolean {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        return adapter != null
    }

    override fun start() { }
    override fun stop() { }

    override fun events(): Flow<TriggerEvent> = callbackFlow {
        val br = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val device = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                }
                val deviceName = device?.name ?: device?.address ?: "Unknown"
                when (intent?.action) {
                    BluetoothDevice.ACTION_ACL_CONNECTED -> trySend(TriggerEvent.BluetoothConnected(deviceName))
                    BluetoothDevice.ACTION_ACL_DISCONNECTED -> trySend(TriggerEvent.BluetoothDisconnected(deviceName))
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }
        context.registerReceiver(br, filter)
        awaitClose { try { context.unregisterReceiver(br) } catch (_: Exception) { } }
    }
}
