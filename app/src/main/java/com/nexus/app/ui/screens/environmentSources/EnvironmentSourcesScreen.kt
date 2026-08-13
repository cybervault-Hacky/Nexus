package com.nexus.app.ui.screens.environmentSources

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.nexus.app.data.automation.AutomationSettingsImpl
import com.nexus.app.domain.event.EnvironmentEventSource
import com.nexus.app.domain.event.EnvironmentEventSourceRegistry
import com.nexus.app.ui.components.GlassSurface
import com.nexus.app.ui.components.StatusBadge
import com.nexus.app.ui.theme.NexusSpacing

/**
 * Shows all environment event sources with their status and enable/disable controls.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnvironmentSourcesScreen(
    registry: EnvironmentEventSourceRegistry,
    settings: AutomationSettingsImpl,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sources = registry.getAllSources()
    val enabledStates = remember { mutableStateMapOf<String, Boolean>() }
    var globalEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        globalEnabled = settings.isGlobalEnabled()
        sources.forEach { source ->
            enabledStates[source.sourceId] = settings.isSourceEnabled(source.sourceId)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Environment Triggers", style = MaterialTheme.typography.titleLarge) },
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
            // Global switch
            item {
                GlassSurface {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Environment Triggers", style = MaterialTheme.typography.titleMedium)
                            Text("Master switch for all environment-based automation", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = globalEnabled, onCheckedChange = {
                            globalEnabled = it
                            kotlinx.coroutines.runBlocking { settings.setGlobalEnabled(it) }
                        })
                    }
                }
            }

            item { Spacer(Modifier.height(NexusSpacing.sm)) }

            // Individual sources
            items(sources) { source ->
                val sourceEnabled = enabledStates[source.sourceId] ?: true
                EnvironmentSourceCard(
                    source = source,
                    enabled = sourceEnabled && globalEnabled,
                    onToggle = {
                        enabledStates[source.sourceId] = it
                        kotlinx.coroutines.runBlocking { settings.setSourceEnabled(source.sourceId, it) }
                        if (it) registry.enable(source.sourceId) else registry.disable(source.sourceId)
                    },
                )
            }
        }
    }
}

@Composable
private fun EnvironmentSourceCard(
    source: EnvironmentEventSource,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    val supported = source.isSupported()

    GlassSurface(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(source.displayName, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(NexusSpacing.xs))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (supported) {
                        StatusBadge(text = "Available", backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), textColor = MaterialTheme.colorScheme.primary)
                    } else {
                        StatusBadge(text = "Unsupported", backgroundColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f), textColor = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Spacer(Modifier.width(NexusSpacing.md))
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                enabled = supported,
            )
        }
    }
}
