package com.nexus.app.ui.screens.automationHistory

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.nexus.app.domain.model.automation.AutomationExecution
import com.nexus.app.domain.model.automation.ExecutionStatus
import com.nexus.app.ui.components.GlassSurface
import com.nexus.app.ui.components.StatusBadge
import com.nexus.app.ui.screens.automations.AutomationViewModel
import com.nexus.app.ui.theme.NexusSpacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomationHistoryScreen(
    viewModel: AutomationViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val executions by viewModel.recentExecutions.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Execution History", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Go back") } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { innerPadding ->
        if (executions.isEmpty()) {
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No executions yet", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(start = NexusSpacing.screenPadding, end = NexusSpacing.screenPadding, bottom = NexusSpacing.xxl),
                verticalArrangement = Arrangement.spacedBy(NexusSpacing.sm),
            ) {
                itemsIndexed(executions, key = { _, e -> e.id }) { _, execution ->
                    HistoryRow(execution)
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(execution: AutomationExecution) {
    GlassSurface(modifier = Modifier.fillMaxWidth(), contentPadding = NexusSpacing.md) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(formatDate(execution.startedAt), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                Row {
                    Text("${execution.triggerType.name} • ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${execution.successfulActions} ok", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    if (execution.failedActions > 0) Text(" • ${execution.failedActions} fail", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
                execution.errorMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, maxLines = 1) }
            }
            val (label, color) = when (execution.status) {
                ExecutionStatus.SUCCESS -> "OK" to MaterialTheme.colorScheme.primary
                ExecutionStatus.FAILED -> "Fail" to MaterialTheme.colorScheme.error
                ExecutionStatus.CANCELLED -> "Cancel" to MaterialTheme.colorScheme.onSurfaceVariant
                ExecutionStatus.RUNNING -> "Run" to MaterialTheme.colorScheme.tertiary
                ExecutionStatus.SKIPPED_COOLDOWN -> "Skip" to MaterialTheme.colorScheme.onSurfaceVariant
                ExecutionStatus.SKIPPED_DISABLED -> "Skip" to MaterialTheme.colorScheme.onSurfaceVariant
                ExecutionStatus.SKIPPED_INVALID -> "Skip" to MaterialTheme.colorScheme.onSurfaceVariant
            }
            StatusBadge(text = label, backgroundColor = color.copy(alpha = 0.15f), textColor = color)
        }
    }
}

private fun formatDate(ts: Long) = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(ts))
