package com.nexus.app.ui.screens.automationEditor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nexus.app.domain.model.NexusContext
import com.nexus.app.domain.model.automation.AutomationValidation
import com.nexus.app.domain.model.automation.TriggerType
import com.nexus.app.ui.components.GlassSurface
import com.nexus.app.ui.components.StatusBadge
import com.nexus.app.ui.screens.automations.AutomationUiState
import com.nexus.app.ui.screens.automations.AutomationViewModel
import com.nexus.app.ui.theme.NexusSpacing
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AutomationEditorScreen(
    editingId: String?,
    viewModel: AutomationViewModel,
    availableContexts: List<NexusContext>,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isEditing = editingId != null
    val automations by viewModel.automations.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val existing = remember(automations, editingId) { editingId?.let { id -> automations.find { it.id == id } } }

    var name by remember(existing) { mutableStateOf(existing?.name ?: "") }
    var description by remember(existing) { mutableStateOf(existing?.description ?: "") }
    var selectedTrigger by remember(existing) { mutableStateOf(existing?.triggerType ?: TriggerType.MANUAL) }
    var selectedContextId by remember(existing) { mutableStateOf(existing?.contextId ?: (availableContexts.firstOrNull()?.id ?: "")) }
    var cooldown by remember(existing) { mutableIntStateOf(existing?.cooldownSeconds ?: 60) }
    var timeHour by remember(existing) { mutableIntStateOf(9) }
    var timeMinute by remember(existing) { mutableIntStateOf(0) }
    var daysOfWeek by remember(existing) { mutableIntStateOf(127) }
    var appPackageName by remember(existing) { mutableStateOf("") }
    var batteryThreshold by remember(existing) { mutableIntStateOf(20) }
    var showError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(existing) {
        if (existing != null) {
            try {
                val obj = JSONObject(existing.triggerPayload)
                when (existing.triggerType) {
                    TriggerType.TIME -> { timeHour = obj.optInt("hour", 9); timeMinute = obj.optInt("minute", 0); daysOfWeek = obj.optInt("daysOfWeek", 127) }
                    TriggerType.APP_OPEN, TriggerType.APP_CLOSE -> { appPackageName = obj.optString("packageName", "") }
                    TriggerType.CONTEXT_ACTIVATED -> { selectedContextId = obj.optString("contextId", existing.contextId) }
                    else -> {}
                }
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AutomationUiState.Saved -> { viewModel.clearUiState(); onNavigateBack() }
            is AutomationUiState.Error -> { showError = (uiState as AutomationUiState.Error).message; viewModel.clearUiState() }
            else -> {}
        }
    }

    fun buildPayload(): String {
        return when (selectedTrigger) {
            TriggerType.MANUAL -> "{}"
            TriggerType.TIME -> JSONObject().apply { put("hour", timeHour); put("minute", timeMinute); put("daysOfWeek", daysOfWeek) }.toString()
            TriggerType.APP_OPEN, TriggerType.APP_CLOSE -> JSONObject().apply { put("packageName", appPackageName) }.toString()
            TriggerType.CONTEXT_ACTIVATED -> JSONObject().apply { put("contextId", selectedContextId) }.toString()
            TriggerType.BATTERY_BELOW, TriggerType.BATTERY_ABOVE -> JSONObject().apply { put("thresholdPercent", batteryThreshold) }.toString()
            else -> "{}" // Simple triggers with no payload
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (isEditing) "Edit Automation" else "New Automation", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Go back") } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val payload = buildPayload()
                    if (isEditing && editingId != null) viewModel.updateRule(editingId, name, description, selectedTrigger, payload, selectedContextId, cooldown)
                    else viewModel.createRule(name, description, selectedTrigger, payload, selectedContextId, cooldown)
                },
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                if (uiState is AutomationUiState.Saving) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                else Icon(Icons.Rounded.Check, contentDescription = "Save", tint = MaterialTheme.colorScheme.onPrimary)
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(start = NexusSpacing.screenPadding, end = NexusSpacing.screenPadding, bottom = NexusSpacing.xxxxl + 80.dp),
            verticalArrangement = Arrangement.spacedBy(NexusSpacing.sectionGap),
        ) {
            if (showError != null) {
                item {
                    GlassSurface(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f), borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.3f)) {
                        Text(showError!!, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            item {
                Column {
                    Text("Name", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = NexusSpacing.sm))
                    OutlinedTextField(value = name, onValueChange = { if (it.length <= AutomationValidation.MAX_NAME_LENGTH) { name = it; showError = null } }, placeholder = { Text("e.g. Morning Routine") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline))
                }
            }

            item {
                Column {
                    Text("Description", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = NexusSpacing.sm))
                    OutlinedTextField(value = description, onValueChange = { if (it.length <= AutomationValidation.MAX_DESCRIPTION_LENGTH) description = it }, placeholder = { Text("Brief description") }, maxLines = 2, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline))
                }
            }

            item {
                Column {
                    Text("Trigger Type", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = NexusSpacing.md))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(NexusSpacing.md), verticalArrangement = Arrangement.spacedBy(NexusSpacing.md)) {
                        TriggerType.entries.forEach { type ->
                            val label = when (type) {
                                TriggerType.MANUAL -> "Manual"
                                TriggerType.TIME -> "Time"
                                TriggerType.APP_OPEN -> "App Open"
                                TriggerType.APP_CLOSE -> "App Close"
                                TriggerType.CONTEXT_ACTIVATED -> "Context"
                                TriggerType.WIFI_CONNECTED -> "Wi-Fi On"
                                TriggerType.WIFI_DISCONNECTED -> "Wi-Fi Off"
                                TriggerType.BLUETOOTH_CONNECTED -> "BT On"
                                TriggerType.BLUETOOTH_DISCONNECTED -> "BT Off"
                                TriggerType.CHARGING_STARTED -> "Charging"
                                TriggerType.CHARGING_STOPPED -> "Unplug"
                                TriggerType.BATTERY_BELOW -> "Battery Low"
                                TriggerType.BATTERY_ABOVE -> "Battery High"
                                TriggerType.DEVICE_BOOT -> "Boot"
                                TriggerType.SCREEN_ON -> "Screen On"
                                TriggerType.SCREEN_OFF -> "Screen Off"
                                TriggerType.DEVICE_IDLE -> "Idle"
                                TriggerType.DEVICE_ACTIVE -> "Active"
                                TriggerType.NFC_TAG_DETECTED -> "NFC Tag"
                                TriggerType.NFC_TAG_REMOVED -> "NFC Removed"
                                TriggerType.GEOFENCE_ENTER -> "Geofence In"
                                TriggerType.GEOFENCE_EXIT -> "Geofence Out"
                                TriggerType.CALENDAR_EVENT_START -> "Cal Start"
                                TriggerType.CALENDAR_EVENT_END -> "Cal End"
                                TriggerType.NOTIFICATION_POSTED -> "Notification"
                                TriggerType.NOTIFICATION_REMOVED -> "Notif Off"
                                TriggerType.ALL_CONDITIONS -> "ALL"
                                TriggerType.ANY_CONDITION -> "ANY"
                            }
                            val selected = type == selectedTrigger
                            GlassSurface(
                                containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentPadding = NexusSpacing.md,
                                modifier = Modifier.clickable { selectedTrigger = type },
                            ) { Text(label, style = MaterialTheme.typography.bodyMedium, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                    }
                }
            }

            // Trigger-specific config
            if (selectedTrigger == TriggerType.TIME) {
                item {
                    GlassSurface {
                        Column {
                            Text("Time", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(NexusSpacing.sm))
                            Row(horizontalArrangement = Arrangement.spacedBy(NexusSpacing.md)) {
                                OutlinedTextField(value = timeHour.toString(), onValueChange = { it.toIntOrNull()?.let { h -> if (h in 0..23) timeHour = h } }, label = { Text("Hour") }, singleLine = true, modifier = Modifier.weight(1f))
                                OutlinedTextField(value = timeMinute.toString(), onValueChange = { it.toIntOrNull()?.let { m -> if (m in 0..59) timeMinute = m } }, label = { Text("Minute") }, singleLine = true, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            if (selectedTrigger == TriggerType.APP_OPEN || selectedTrigger == TriggerType.APP_CLOSE) {
                item {
                    GlassSurface {
                        Column {
                            Text("Package Name", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(NexusSpacing.sm))
                            OutlinedTextField(value = appPackageName, onValueChange = { appPackageName = it }, placeholder = { Text("com.example.app") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }

            if (selectedTrigger == TriggerType.CONTEXT_ACTIVATED) {
                item {
                    GlassSurface {
                        Column {
                            Text("Target Context", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(NexusSpacing.sm))
                            availableContexts.forEach { ctx ->
                                val selected = ctx.id == selectedContextId
                                GlassSurface(
                                    containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    contentPadding = NexusSpacing.sm,
                                    modifier = Modifier.fillMaxWidth().clickable { selectedContextId = ctx.id }.padding(vertical = NexusSpacing.xxs),
                                ) { Text(ctx.name, style = MaterialTheme.typography.bodyMedium, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) }
                            }
                        }
                    }
                }
            }

            if (selectedTrigger == TriggerType.BATTERY_BELOW || selectedTrigger == TriggerType.BATTERY_ABOVE) {
                item {
                    GlassSurface {
                        Column {
                            Text("Battery Threshold", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(NexusSpacing.sm))
                            OutlinedTextField(value = batteryThreshold.toString(), onValueChange = { it.toIntOrNull()?.let { v -> if (v in 0..100) batteryThreshold = v } }, label = { Text("Percentage") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            Text("Trigger when battery ${if (selectedTrigger == TriggerType.BATTERY_BELOW) "drops below" else "rises above"} this level", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            item {
                GlassSurface {
                    Column {
                        Text("Cooldown (seconds)", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(NexusSpacing.sm))
                        OutlinedTextField(value = cooldown.toString(), onValueChange = { it.toIntOrNull()?.let { c -> cooldown = c } }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        Text("Minimum time between executions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
