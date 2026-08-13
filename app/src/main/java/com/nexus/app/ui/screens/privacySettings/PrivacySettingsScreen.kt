package com.nexus.app.ui.screens.privacySettings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.nexus.app.data.automation.capability.CapabilityManager
import com.nexus.app.data.automation.capability.CapabilityState
import com.nexus.app.data.environment.nfc.NfcEventSource
import com.nexus.app.data.environment.notification.NotificationEventSource
import com.nexus.app.ui.components.GlassSurface
import com.nexus.app.ui.components.StatusBadge
import com.nexus.app.ui.theme.NexusSpacing

/**
 * Privacy settings — shows what data NEXUS reads and why.
 * All data stays local. No cloud, no telemetry.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettingsScreen(
    capabilityManager: CapabilityManager,
    nfcSource: NfcEventSource,
    notificationSource: NotificationEventSource,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Privacy & Data", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Go back") } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(start = NexusSpacing.screenPadding, end = NexusSpacing.screenPadding, bottom = NexusSpacing.xxl),
            verticalArrangement = Arrangement.spacedBy(NexusSpacing.itemGap),
        ) {
            item {
                GlassSurface {
                    Column {
                        Text("Your Data Stays Local", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(NexusSpacing.sm))
                        Text("NEXUS processes all data on your device. Nothing is uploaded, synced, or transmitted to any external service.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item {
                PrivacyItem("Location", capabilityManager.checkLocation(),
                    "Used for geofence automations. Only accessed when you create a location-based automation. Never tracked continuously.")
            }
            item {
                PrivacyItem("Calendar", capabilityManager.checkCalendar(),
                    "Read-only access to detect event start/end times. NEXUS never modifies your calendar.")
            }
            item {
                PrivacyItem("Notifications", if (notificationSource.isListenerEnabled()) CapabilityState.SUPPORTED else CapabilityState.PERMISSION_REQUIRED,
                    "Matches notification metadata (package, category). Notification content is never stored.")
            }
            item {
                PrivacyItem("NFC", if (nfcSource.isNfcAvailable()) CapabilityState.SUPPORTED else CapabilityState.UNSUPPORTED,
                    "Detects NFC tag IDs for tap-based automations. Tag payloads are not stored.")
            }
        }
    }
}

@Composable
private fun PrivacyItem(name: String, state: CapabilityState, explanation: String) {
    GlassSurface(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(name, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                val (label, color) = when (state) {
                    CapabilityState.SUPPORTED -> "Available" to MaterialTheme.colorScheme.primary
                    CapabilityState.PERMISSION_REQUIRED -> "Permission needed" to MaterialTheme.colorScheme.tertiary
                    CapabilityState.PERMISSION_DENIED -> "Denied" to MaterialTheme.colorScheme.error
                    CapabilityState.DISABLED -> "Disabled" to MaterialTheme.colorScheme.onSurfaceVariant
                    CapabilityState.UNSUPPORTED -> "Unsupported" to MaterialTheme.colorScheme.onSurfaceVariant
                    CapabilityState.ERROR -> "Error" to MaterialTheme.colorScheme.error
                }
                StatusBadge(text = label, backgroundColor = color.copy(alpha = 0.15f), textColor = color)
            }
            Spacer(Modifier.height(NexusSpacing.sm))
            Text(explanation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
