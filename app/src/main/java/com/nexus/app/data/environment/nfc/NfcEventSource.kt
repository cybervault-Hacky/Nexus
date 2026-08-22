package com.nexus.app.data.environment.nfc

import android.content.Context
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import com.nexus.app.domain.event.EnvironmentEventSource
import com.nexus.app.domain.model.automation.TriggerEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * NFC event source.
 * NFC tag detection requires a foreground Activity with NFC intent dispatch.
 * This source reports capability but does not scan in background.
 */
class NfcEventSource(private val context: Context) : EnvironmentEventSource {
    override val sourceId = "nfc"
    override val displayName = "NFC"

    private fun adapterOrNull(): NfcAdapter? {
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_NFC)) return null
        return try {
            NfcAdapter.getDefaultAdapter(context)
        } catch (_: SecurityException) {
            null
        } catch (_: UnsupportedOperationException) {
            null
        }
    }

    fun isNfcAvailable(): Boolean = adapterOrNull() != null

    fun isNfcEnabled(): Boolean = try {
        adapterOrNull()?.isEnabled == true
    } catch (_: SecurityException) {
        false
    }

    override fun isSupported() = isNfcAvailable()
    override fun start() = Unit
    override fun stop() = Unit
    override fun events(): Flow<TriggerEvent> = emptyFlow() // Events dispatched via Activity
}
