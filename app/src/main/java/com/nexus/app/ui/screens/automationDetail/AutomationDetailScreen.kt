package com.nexus.app.ui.screens.automationDetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nexus.app.domain.model.automation.AutomationExecution
import com.nexus.app.domain.model.automation.ExecutionStatus
import com.nexus.app.domain.model.automation.TriggerType
import com.nexus.app.ui.components.GlassSurface
import com.nexus.app.ui.components.StatusBadge
import com.nexus.app.ui.screens.automations.AutomationViewModel
import com.nexus.app.ui.theme.NexusSpacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomationDetailScreen(
    automationId: String,
    viewModel: AutomationViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val automations by viewModel.automations.collectAsState()
    val rule = remember(automations, automationId) { automations.find { it.id == automationId } }
    val executions by viewModel.recentExecutions.collectAsState()
    val ruleExecutions = remember(executions, automationId) { executions.filter { it.automationId == automationId } }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(rule?.name ?: "Automation Details", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Go back") } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { innerPadding ->
        if (rule == null) {
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Automation not found", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(start = NexusSpacing.screenPadding, end = NexusSpacing.screenPadding, bottom = NexusSpacing.xxl),
                verticalArrangement = Arrangement.spacedBy(NexusSpacing.sectionGap),
            ) {
                // Header
                item {
                    AnimatedVisibility(visible = true, enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 4 }) {
                        GlassSurface {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text(rule.name, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
                                if (rule.description.isNotBlank()) { Spacer(Modifier.height(NexusSpacing.xs)); Text(rule.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                Spacer(Modifier.height(NexusSpacing.md))
                                val triggerLabel = when (rule.triggerType) {
                                    TriggerType.MANUAL -> "Manual"
                                    TriggerType.TIME -> "Time"
                                    TriggerType.APP_OPEN -> "App Open"
                                    TriggerType.APP_CLOSE -> "App Close"
                                    TriggerType.CONTEXT_ACTIVATED -> "Context Activated"
                                    TriggerType.WIFI_CONNECTED -> "Wi-Fi Connected"
                                    TriggerType.WIFI_DISCONNECTED -> "Wi-Fi Disconnected"
                                    TriggerType.BLUETOOTH_CONNECTED -> "Bluetooth Connected"
                                    TriggerType.BLUETOOTH_DISCONNECTED -> "Bluetooth Disconnected"
                                    TriggerType.CHARGING_STARTED -> "Charging Started"
                                    TriggerType.CHARGING_STOPPED -> "Charging Stopped"
                                    TriggerType.BATTERY_BELOW -> "Battery Below"
                                    TriggerType.BATTERY_ABOVE -> "Battery Above"
                                    TriggerType.DEVICE_BOOT -> "Device Boot"
                                    TriggerType.SCREEN_ON -> "Screen On"
                                    TriggerType.SCREEN_OFF -> "Screen Off"
                                    TriggerType.DEVICE_IDLE -> "Device Idle"
                                    TriggerType.DEVICE_ACTIVE -> "Device Active"
                                    TriggerType.NFC_TAG_DETECTED -> "NFC Tag"
                                    TriggerType.NFC_TAG_REMOVED -> "NFC Removed"
                                    TriggerType.GEOFENCE_ENTER -> "Geofence Enter"
                                    TriggerType.GEOFENCE_EXIT -> "Geofence Exit"
                                    TriggerType.CALENDAR_EVENT_START -> "Calendar Start"
                                    TriggerType.CALENDAR_EVENT_END -> "Calendar End"
                                    TriggerType.NOTIFICATION_POSTED -> "Notification"
                                    TriggerType.NOTIFICATION_REMOVED -> "Notification Removed"
                                    TriggerType.ALL_CONDITIONS -> "All Conditions"
                                    TriggerType.ANY_CONDITION -> "Any Condition"
                                }
                                StatusBadge(text = triggerLabel, backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), textColor = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                // Details
                item {
                    AnimatedVisibility(visible = true, enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 4 }) {
                        GlassSurface {
                            Column {
                                Text("Details", style = MaterialTheme.typography.titleMedium)
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                Spacer(Modifier.height(NexusSpacing.sm))
                                DetailRow("Status", if (rule.isEnabled) "Enabled" else "Disabled")
                                DetailRow("Cooldown", "${rule.cooldownSeconds}s")
                                DetailRow("Target Context", rule.contextId.take(8) + "…")
                                rule.lastTriggeredAt?.let { DetailRow("Last triggered", formatDate(it)) }
                            }
                        }
                    }
                }

                // Actions
                item {
                    AnimatedVisibility(visible = true, enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 4 }) {
                        GlassSurface {
                            Column {
                                Text("Actions", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = NexusSpacing.md))
                                TextButton(onClick = { viewModel.runNow(automationId) }, modifier = Modifier.fillMaxWidth()) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(NexusSpacing.sm))
                                    Text("Run Now", color = MaterialTheme.colorScheme.primary)
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = NexusSpacing.sm))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Enabled", style = MaterialTheme.typography.bodyLarge)
                                    Switch(checked = rule.isEnabled, onCheckedChange = { viewModel.setEnabled(rule.id, it) })
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = NexusSpacing.sm))
                                TextButton(onClick = { onNavigateToEdit(automationId) }, modifier = Modifier.fillMaxWidth()) {
                                    Icon(Icons.Outlined.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(NexusSpacing.sm))
                                    Text("Edit", color = MaterialTheme.colorScheme.onSurface)
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = NexusSpacing.sm))
                                TextButton(onClick = { showDeleteDialog = true }, modifier = Modifier.fillMaxWidth()) {
                                    Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(NexusSpacing.sm))
                                    Text("Delete", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }

                // Execution history
                if (ruleExecutions.isNotEmpty()) {
                    item {
                        Text("Recent Executions", style = MaterialTheme.typography.titleMedium)
                    }
                    itemsIndexed(ruleExecutions.take(10), key = { _, e -> e.id }) { _, execution ->
                        ExecutionRow(execution)
                    }
                }
            }
        }
    }

    if (showDeleteDialog && rule != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete automation?") },
            text = { Text("\"${rule.name}\" will be permanently deleted.") },
            confirmButton = { TextButton(onClick = { showDeleteDialog = false; viewModel.deleteRule(automationId); onNavigateBack() }) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = NexusSpacing.xs), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun ExecutionRow(execution: AutomationExecution) {
    GlassSurface(modifier = Modifier.fillMaxWidth(), contentPadding = NexusSpacing.md) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(formatDate(execution.startedAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                execution.errorMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, maxLines = 1) }
            }
            val (label, color) = when (execution.status) {
                ExecutionStatus.SUCCESS -> "Success" to MaterialTheme.colorScheme.primary
                ExecutionStatus.FAILED -> "Failed" to MaterialTheme.colorScheme.error
                ExecutionStatus.CANCELLED -> "Cancelled" to MaterialTheme.colorScheme.onSurfaceVariant
                ExecutionStatus.RUNNING -> "Running" to MaterialTheme.colorScheme.tertiary
                ExecutionStatus.SKIPPED_COOLDOWN -> "Skipped" to MaterialTheme.colorScheme.onSurfaceVariant
                ExecutionStatus.SKIPPED_DISABLED -> "Skipped" to MaterialTheme.colorScheme.onSurfaceVariant
                ExecutionStatus.SKIPPED_INVALID -> "Skipped" to MaterialTheme.colorScheme.onSurfaceVariant
            }
            StatusBadge(text = label, backgroundColor = color.copy(alpha = 0.15f), textColor = color)
        }
    }
}

private fun formatDate(timestamp: Long): String = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(timestamp))
