package com.nexus.app.ui.screens.eventDiagnostics

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
import androidx.compose.foundation.lazy.items
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
import com.nexus.app.data.local.EventHistoryEntity
import com.nexus.app.data.repository.EventHistoryRepositoryImpl
import com.nexus.app.domain.event.EnvironmentEventSource
import com.nexus.app.domain.event.EnvironmentEventSourceRegistry
import com.nexus.app.ui.components.GlassSurface
import com.nexus.app.ui.components.StatusBadge
import com.nexus.app.ui.theme.NexusSpacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Developer diagnostics — shows event source state and recent events.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDiagnosticsScreen(
    registry: EnvironmentEventSourceRegistry,
    historyRepository: EventHistoryRepositoryImpl,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val history by historyRepository.observeRecent(50).collectAsState(initial = emptyList())

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Event Diagnostics", style = MaterialTheme.typography.titleLarge) },
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
                Text("Event Sources", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = NexusSpacing.xxl))
            }
            items(registry.getAllSources()) { source ->
                SourceStatusCard(source, registry.isEnabled(source.sourceId))
            }

            item {
                Spacer(Modifier.height(NexusSpacing.md))
                Text("Recent Events", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
            if (history.isEmpty()) {
                item {
                    Text("No events recorded yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(history.take(20)) { entry ->
                    EventHistoryRow(entry)
                }
            }
        }
    }
}

@Composable
private fun SourceStatusCard(source: EnvironmentEventSource, enabled: Boolean) {
    GlassSurface(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(source.displayName, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                val status = when {
                    !source.isSupported() -> "Unsupported" to MaterialTheme.colorScheme.error
                    enabled -> "Active" to MaterialTheme.colorScheme.primary
                    else -> "Disabled" to MaterialTheme.colorScheme.onSurfaceVariant
                }
                StatusBadge(text = status.first, backgroundColor = status.second.copy(alpha = 0.15f), textColor = status.second)
            }
        }
    }
}

@Composable
private fun EventHistoryRow(entry: EventHistoryEntity) {
    GlassSurface(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(entry.eventType, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(entry.source, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatTime(entry.timestamp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (entry.matchedAutomationCount > 0) {
                    Text("${entry.matchedAutomationCount} matched", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

private fun formatTime(ts: Long) = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(ts))
