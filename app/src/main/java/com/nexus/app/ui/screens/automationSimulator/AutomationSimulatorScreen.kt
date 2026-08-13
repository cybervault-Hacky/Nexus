package com.nexus.app.ui.screens.automationSimulator

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nexus.app.data.automation.AutomationSimulator
import com.nexus.app.data.automation.SimulatorResult
import com.nexus.app.domain.model.automation.TriggerEvent
import com.nexus.app.ui.components.GlassSurface
import com.nexus.app.ui.components.StatusBadge
import com.nexus.app.ui.theme.NexusSpacing
import kotlinx.coroutines.launch

/**
 * Developer automation simulator — generate test events.
 * Clearly marked as SIMULATED.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomationSimulatorScreen(
    simulator: AutomationSimulator,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val lastResult by simulator.lastSimulation.collectAsState()

    val testEvents = listOf(
        "Wi-Fi Connected" to TriggerEvent.WifiConnected("TestNetwork"),
        "Wi-Fi Disconnected" to TriggerEvent.WifiDisconnected(),
        "Bluetooth Connected" to TriggerEvent.BluetoothConnected("TestDevice"),
        "Charging Started" to TriggerEvent.ChargingStarted(),
        "Charging Stopped" to TriggerEvent.ChargingStopped(),
        "Battery 15%" to TriggerEvent.BatteryLevelChanged(15),
        "Battery 90%" to TriggerEvent.BatteryLevelChanged(90),
        "Device Boot" to TriggerEvent.DeviceBoot(),
        "Screen On" to TriggerEvent.ScreenOn(),
        "Screen Off" to TriggerEvent.ScreenOff(),
        "NFC Tag Detected" to TriggerEvent.NfcTagDetected("TEST_TAG_001"),
        "Geofence Entered" to TriggerEvent.GeofenceEntered("test_fence"),
        "Geofence Exited" to TriggerEvent.GeofenceExited("test_fence"),
        "Calendar Event Started" to TriggerEvent.CalendarEventStarted("test_event", "Test Meeting"),
        "Notification Posted" to TriggerEvent.NotificationPosted("com.test.app"),
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Simulator", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Go back") } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(start = NexusSpacing.screenPadding, end = NexusSpacing.screenPadding, bottom = NexusSpacing.xxl),
            verticalArrangement = Arrangement.spacedBy(NexusSpacing.sm),
        ) {
            item {
                GlassSurface(modifier = Modifier.fillMaxWidth()) {
                    Text("⚠ SIMULATED — Events use the real trigger pipeline but no system state is modified.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }

            lastResult?.let { result ->
                item {
                    GlassSurface(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text("Last Simulation", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(NexusSpacing.sm))
                            Text("Event: ${result.eventType}", style = MaterialTheme.typography.bodyMedium)
                            Text("Matched: ${result.matchedCount} automations", style = MaterialTheme.typography.bodyMedium)
                            result.results.forEach { status ->
                                Text("→ $status", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(NexusSpacing.sm)) }

            items(testEvents) { (label, event) ->
                GlassSurface(
                    modifier = Modifier.fillMaxWidth().clickable {
                        scope.launch { simulator.simulate(event) }
                    },
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Bolt, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(NexusSpacing.sm))
                        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}
