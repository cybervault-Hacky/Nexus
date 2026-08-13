package com.nexus.app.ui.screens.automations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nexus.app.domain.model.automation.AutomationRule
import com.nexus.app.domain.model.automation.TriggerType
import com.nexus.app.ui.components.GlassSurface
import com.nexus.app.ui.components.StatusBadge
import com.nexus.app.ui.theme.NexusSpacing
import kotlinx.coroutines.delay

@Composable
fun AutomationScreen(
    viewModel: AutomationViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val automations by viewModel.automations.collectAsState()
    val enabledCount by viewModel.enabledCount.collectAsState()
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = NexusSpacing.screenPadding, end = NexusSpacing.screenPadding, bottom = NexusSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(NexusSpacing.itemGap),
    ) {
        item {
            Column(modifier = Modifier.padding(top = NexusSpacing.xxl)) {
                Text("Automations", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(NexusSpacing.xs))
                Text("$enabledCount active", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(NexusSpacing.sectionGap))
            }
        }

        if (automations.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = NexusSpacing.xxxxl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(NexusSpacing.md))
                    Text("No automations yet", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.height(NexusSpacing.sm))
                    Text("Create your first automation to get started.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(NexusSpacing.lg))
                    TextButton(onClick = onNavigateToCreate) {
                        Icon(Icons.Outlined.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(NexusSpacing.sm))
                        Text("Create Automation", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        } else {
            item {
                TextButton(onClick = onNavigateToCreate, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(NexusSpacing.sm))
                    Text("Create Automation", color = MaterialTheme.colorScheme.primary)
                }
            }

            itemsIndexed(automations, key = { _, r -> r.id }) { index, rule ->
                val animDelay = index * 80
                var cardVisible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { delay(animDelay.toLong()); cardVisible = true }
                AnimatedVisibility(visible = cardVisible, enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 4 }) {
                    AutomationCard(rule = rule, viewModel = viewModel, onClick = { onNavigateToDetail(rule.id) })
                }
            }
        }
    }
}

@Composable
private fun AutomationCard(
    rule: AutomationRule,
    viewModel: AutomationViewModel,
    onClick: () -> Unit,
) {
    val triggerLabel = when (rule.triggerType) {
        TriggerType.MANUAL -> "Manual"
        TriggerType.TIME -> "Time"
        TriggerType.APP_OPEN -> "App Open"
        TriggerType.APP_CLOSE -> "App Close"
        TriggerType.CONTEXT_ACTIVATED -> "Context"
    }

    GlassSurface(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(rule.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(rule.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
                Switch(checked = rule.isEnabled, onCheckedChange = { viewModel.setEnabled(rule.id, it) }, colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary))
            }
            Spacer(Modifier.height(NexusSpacing.sm))
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusBadge(text = triggerLabel, backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), textColor = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(NexusSpacing.sm))
                if (rule.cooldownSeconds > 0) StatusBadge(text = "${rule.cooldownSeconds}s cooldown")
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { viewModel.runNow(rule.id) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Run now", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = { viewModel.deleteRule(rule.id) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
