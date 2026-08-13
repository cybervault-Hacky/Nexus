package com.nexus.app.ui.screens.automationConflicts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.nexus.app.domain.model.smart.AutomationConflict
import com.nexus.app.domain.model.smart.ConflictSeverity
import com.nexus.app.ui.components.GlassSurface
import com.nexus.app.ui.components.StatusBadge
import com.nexus.app.ui.theme.NexusSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConflictsScreen(
    conflicts: List<AutomationConflict>,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Conflicts", style = MaterialTheme.typography.titleLarge) },
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
            if (conflicts.isEmpty()) {
                item {
                    Column(modifier = Modifier.padding(top = NexusSpacing.xxxxl)) {
                        Text("No conflicts detected", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            items(conflicts) { conflict ->
                GlassSurface(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        val severityColor = when (conflict.severity) {
                            ConflictSeverity.SAFE -> MaterialTheme.colorScheme.primary
                            ConflictSeverity.WARNING -> MaterialTheme.colorScheme.tertiary
                            ConflictSeverity.CONFLICT -> MaterialTheme.colorScheme.error
                        }
                        StatusBadge(text = conflict.severity.name, backgroundColor = severityColor.copy(alpha = 0.15f), textColor = severityColor)
                        Spacer(Modifier.height(NexusSpacing.sm))
                        Text(conflict.type.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text(conflict.explanation, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
