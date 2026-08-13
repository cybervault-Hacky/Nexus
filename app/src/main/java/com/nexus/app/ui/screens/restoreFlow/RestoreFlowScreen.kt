package com.nexus.app.ui.screens.restoreFlow

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nexus.app.domain.model.restore.RestoreChangeType
import com.nexus.app.domain.model.restore.RestoreResult
import com.nexus.app.domain.model.restore.RestoreStatus
import com.nexus.app.domain.model.restore.RestoreTarget
import com.nexus.app.ui.components.GlassSurface
import com.nexus.app.ui.components.StatusBadge
import com.nexus.app.ui.theme.NexusSpacing

/**
 * Multi-step restore flow: Target Selection → Preview → Result.
 * Uses [CapsuleRestoreViewModel] for state management.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestoreFlowScreen(
    capsuleId: String,
    viewModel: CapsuleRestoreViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToContext: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val contexts by viewModel.contexts.collectAsState()

    LaunchedEffect(capsuleId) {
        viewModel.startRestore(capsuleId)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Restore Capsule", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.reset()
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Go back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(
                start = NexusSpacing.screenPadding,
                end = NexusSpacing.screenPadding,
                bottom = NexusSpacing.xxl,
            ),
            verticalArrangement = Arrangement.spacedBy(NexusSpacing.sectionGap),
        ) {
            when (val state = uiState) {
                is RestoreUiState.Loading -> {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = NexusSpacing.xxxxl),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(NexusSpacing.md))
                            Text("Loading…", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                is RestoreUiState.Error -> {
                    item {
                        GlassSurface(
                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                            borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                        ) {
                            Text(state.message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                is RestoreUiState.Idle -> {
                    // Target selection
                    item {
                        AnimatedVisibility(visible = true, enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 4 }) {
                            GlassSurface {
                                Column {
                                    Text("Choose target", style = MaterialTheme.typography.titleMedium)
                                    Spacer(Modifier.height(NexusSpacing.md))

                                    // Create new
                                    TargetOption(
                                        label = "Create New Context",
                                        description = "A new context will be created from the snapshot",
                                        selected = false,
                                        onClick = {
                                            viewModel.selectTarget(RestoreTarget.CREATE_NEW)
                                            viewModel.buildPreview()
                                        },
                                    )

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = NexusSpacing.sm))

                                    // Existing contexts
                                    Text("Or restore into an existing context:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.height(NexusSpacing.sm))

                                    if (contexts.isEmpty()) {
                                        Text("No contexts available", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    } else {
                                        contexts.forEach { ctx ->
                                            TargetOption(
                                                label = ctx.name,
                                                description = "${ctx.appCount} apps • ${ctx.actionCount} actions",
                                                selected = false,
                                                onClick = {
                                                    viewModel.selectTarget(RestoreTarget.REPLACE_EXISTING)
                                                    viewModel.selectExistingContext(ctx.id)
                                                    viewModel.buildPreview()
                                                },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                is RestoreUiState.PreviewReady -> {
                    val preview = state.preview

                    // Preview header
                    item {
                        AnimatedVisibility(visible = true, enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 4 }) {
                            GlassSurface {
                                Column {
                                    Text("Restore Preview", style = MaterialTheme.typography.titleMedium)
                                    Spacer(Modifier.height(NexusSpacing.sm))
                                    Text("${preview.capsuleName} → ${preview.targetContextName}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.height(NexusSpacing.sm))
                                    Row(horizontalArrangement = Arrangement.spacedBy(NexusSpacing.sm)) {
                                        if (preview.appsAdded > 0) StatusBadge(text = "+${preview.appsAdded} Apps", backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), textColor = MaterialTheme.colorScheme.primary)
                                        if (preview.appsRemoved > 0) StatusBadge(text = "-${preview.appsRemoved} Apps", backgroundColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f), textColor = MaterialTheme.colorScheme.error)
                                        if (preview.actionsAdded > 0) StatusBadge(text = "+${preview.actionsAdded} Actions", backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), textColor = MaterialTheme.colorScheme.primary)
                                        if (preview.actionsRemoved > 0) StatusBadge(text = "-${preview.actionsRemoved} Actions", backgroundColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f), textColor = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }

                    // Changes list
                    item {
                        AnimatedVisibility(visible = true, enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 4 }) {
                            GlassSurface {
                                Column {
                                    Text("Changes", style = MaterialTheme.typography.titleMedium)
                                    Spacer(Modifier.height(NexusSpacing.sm))

                                    preview.changes.forEach { change ->
                                        val (icon, color, prefix) = when (change.type) {
                                            RestoreChangeType.ADDED -> Triple(Icons.Outlined.Add, MaterialTheme.colorScheme.primary, "+")
                                            RestoreChangeType.REMOVED -> Triple(Icons.Filled.Close, MaterialTheme.colorScheme.error, "-")
                                            RestoreChangeType.UNCHANGED -> Triple(null, MaterialTheme.colorScheme.onSurfaceVariant, "=")
                                            RestoreChangeType.MODIFIED -> Triple(Icons.Outlined.Restore, MaterialTheme.colorScheme.tertiary, "~")
                                            RestoreChangeType.MISSING -> Triple(Icons.Filled.Warning, MaterialTheme.colorScheme.error, "!")
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = NexusSpacing.xs),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(prefix, style = MaterialTheme.typography.bodyMedium, color = color, modifier = Modifier.width(20.dp))
                                            if (icon != null) {
                                                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(NexusSpacing.xs))
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(change.name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                                if (change.detail.isNotBlank()) {
                                                    Text(change.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Missing apps warning
                    if (preview.hasMissingApps) {
                        item {
                            AnimatedVisibility(visible = true, enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 4 }) {
                                GlassSurface(
                                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
                                    borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(NexusSpacing.sm))
                                        Text("${preview.appsMissing} app(s) unavailable — they will be skipped.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }

                    // Restore button
                    item {
                        AnimatedVisibility(visible = true, enter = fadeIn(tween(700)) + slideInVertically(tween(700)) { it / 4 }) {
                            TextButton(
                                onClick = { viewModel.executeRestore() },
                                modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Confirm restore" },
                            ) {
                                Icon(Icons.Outlined.Restore, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(NexusSpacing.sm))
                                Text("Restore", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                is RestoreUiState.Restoring -> {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = NexusSpacing.xxxxl),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(NexusSpacing.md))
                            Text("Restoring…", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                is RestoreUiState.Success -> {
                    item {
                        RestoreResultCard(
                            result = state.result,
                            onOpenContext = {
                                state.result.contextId?.let { onNavigateToContext(it) }
                            },
                            onDone = {
                                viewModel.reset()
                                onNavigateBack()
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TargetOption(
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = NexusSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary),
        )
        Spacer(Modifier.width(NexusSpacing.sm))
        Column {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RestoreResultCard(
    result: RestoreResult,
    onOpenContext: () -> Unit,
    onDone: () -> Unit,
) {
    val isSuccess = result.status == RestoreStatus.SUCCESS
    val isPartial = result.status == RestoreStatus.PARTIAL

    GlassSurface(
        containerColor = if (isSuccess) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            else if (isPartial) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f)
            else MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
        borderColor = if (isSuccess) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            else if (isPartial) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
            else MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isSuccess) Icons.Filled.CheckCircle else if (isPartial) Icons.Filled.Warning else Icons.Filled.Close,
                    contentDescription = null,
                    tint = if (isSuccess) MaterialTheme.colorScheme.primary else if (isPartial) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(NexusSpacing.sm))
                Text(
                    text = when {
                        isSuccess -> "Restoration Complete"
                        isPartial -> "Restoration Partially Complete"
                        else -> "Restoration Failed"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(Modifier.height(NexusSpacing.md))

            Text("${result.appsRestored} app(s) restored", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (result.appsSkipped > 0) {
                Text("${result.appsSkipped} app(s) unavailable", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
            Text("${result.actionsRestored} action(s) restored", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (result.actionsSkipped > 0) {
                Text("${result.actionsSkipped} action(s) skipped", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }

            if (result.warnings.isNotEmpty()) {
                Spacer(Modifier.height(NexusSpacing.sm))
                result.warnings.forEach { warning ->
                    Text("⚠ $warning", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(Modifier.height(NexusSpacing.lg))

            Row(horizontalArrangement = Arrangement.spacedBy(NexusSpacing.sm)) {
                if (result.contextId != null && result.isSuccessful) {
                    TextButton(onClick = onOpenContext) {
                        Text("Open Context", color = MaterialTheme.colorScheme.primary)
                    }
                }
                TextButton(onClick = onDone) {
                    Text("Done", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
