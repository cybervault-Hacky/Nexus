package com.nexus.app.data.environment.nfc

import android.content.Context
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

    fun isNfcAvailable(): Boolean = NfcAdapter.getDefaultAdapter(context) != null
    fun isNfcEnabled(): Boolean = NfcAdapter.getDefaultAdapter(context)?.isEnabled == true

    override fun isSupported() = isNfcAvailable()
    override fun start() { }
    override fun stop() { }
    override fun events(): Flow<TriggerEvent> = emptyFlow() // Events dispatched via Activity
}
