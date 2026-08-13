package com.nexus.app.ui.screens.contextDetail

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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nexus.app.R
import com.nexus.app.domain.model.InstalledApp
import com.nexus.app.domain.model.NexusAction
import com.nexus.app.domain.model.NexusContext
import com.nexus.app.ui.components.ActionItem
import com.nexus.app.ui.components.ContextAppItem
import com.nexus.app.ui.components.GlassSurface
import com.nexus.app.ui.components.StatusBadge
import com.nexus.app.ui.components.WorkflowProgress
import com.nexus.app.ui.components.contextIconFor
import com.nexus.app.ui.screens.actionEditor.ActionViewModel
import com.nexus.app.ui.screens.appPicker.AppViewModel
import com.nexus.app.ui.screens.contexts.ContextViewModel
import com.nexus.app.ui.theme.NexusSpacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Detail view for a single Context.
 * Shows metadata, configured apps, actions, and management controls.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContextDetailScreen(
    contextId: String,
    viewModel: ContextViewModel,
    appViewModel: AppViewModel,
    actionViewModel: ActionViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToAddApps: (String) -> Unit,
    onNavigateToAddAction: (String) -> Unit,
    onNavigateToEditAction: (String) -> Unit,
    onNavigateToCaptureCapsule: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val contexts by viewModel.contexts.collectAsState()
    val context = remember(contexts, contextId) {
        contexts.find { it.id == contextId }
    }

    val contextApps by appViewModel.observeAppsForContext(contextId).collectAsState(initial = emptyList())
    val contextActions by actionViewModel.observeActionsForContext(contextId).collectAsState(initial = emptyList())
    val workflowState by actionViewModel.workflowState.collectAsState()
    val launchError by appViewModel.launchError.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDeleteActionDialog by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(launchError) {
        launchError?.let {
            snackbarHostState.showSnackbar(it)
            appViewModel.clearLaunchError()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = context?.name ?: "Context Details",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Go back",
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
    ) { innerPadding ->
        if (context == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Context not found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(
                    start = NexusSpacing.screenPadding,
                    end = NexusSpacing.screenPadding,
                    bottom = NexusSpacing.xxl,
                ),
                verticalArrangement = Arrangement.spacedBy(NexusSpacing.sectionGap),
            ) {
                // Header
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 4 },
                    ) {
                        DetailHeader(context = context)
                    }
                }

                // Apps section
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 4 },
                    ) {
                        AppsSection(
                            contextId = contextId,
                            apps = contextApps,
                            appViewModel = appViewModel,
                            onAddApps = { onNavigateToAddApps(contextId) },
                        )
                    }
                }

                // Actions section
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 4 },
                    ) {
                        ActionsSection(
                            contextId = contextId,
                            actions = contextActions,
                            actionViewModel = actionViewModel,
                            onAddAction = { onNavigateToAddAction(contextId) },
                            onEditAction = onNavigateToEditAction,
                            onDeleteAction = { showDeleteActionDialog = it },
                        )
                    }
                }

                // Workflow progress
                item {
                    WorkflowProgress(
                        state = workflowState,
                        onCancel = { actionViewModel.cancelWorkflow() },
                        onDismiss = { actionViewModel.resetWorkflow() },
                    )
                }

                // Run workflow button
                if (contextActions.isNotEmpty()) {
                    item {
                        val runDesc = stringResource(R.string.detail_run_workflow)
                        TextButton(
                            onClick = { actionViewModel.runWorkflow(contextId) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { contentDescription = runDesc },
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PlayArrow,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.width(NexusSpacing.sm))
                            Text(
                                text = "Run Workflow",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }

                // Metadata
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(700)) + slideInVertically(tween(700)) { it / 4 },
                    ) {
                        DetailMetadata(context = context)
                    }
                }

                // Context actions
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { it / 4 },
                    ) {
                        DetailActions(
                            context = context,
                            viewModel = viewModel,
                            onEdit = { onNavigateToEdit(contextId) },
                            onDelete = { showDeleteDialog = true },
                            onCaptureCapsule = { onNavigateToCaptureCapsule(contextId) },
                        )
                    }
                }
            }
        }
    }

    // Delete context dialog
    if (showDeleteDialog && context != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete context?") },
            text = {
                Text(
                    "\"${context.name}\" and its configuration will be permanently deleted. " +
                        "This cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteContext(contextId)
                        onNavigateBack()
                    },
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }

    // Delete action dialog
    showDeleteActionDialog?.let { actionId ->
        AlertDialog(
            onDismissRequest = { showDeleteActionDialog = null },
            title = { Text("Delete action?") },
            text = { Text("This action will be permanently deleted.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteActionDialog = null
                        actionViewModel.deleteAction(actionId)
                    },
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteActionDialog = null }) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }
}

@Composable
private fun DetailHeader(context: NexusContext) {
    val accent = Color(context.accentColor)

    GlassSurface {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = contextIconFor(context.iconId),
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(48.dp),
            )
            Spacer(Modifier.height(NexusSpacing.md))
            Text(
                text = context.name,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (context.description.isNotBlank()) {
                Spacer(Modifier.height(NexusSpacing.xs))
                Text(
                    text = context.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(NexusSpacing.md))
            if (context.isActive) {
                StatusBadge(
                    text = "Active",
                    backgroundColor = accent.copy(alpha = 0.15f),
                    textColor = accent,
                )
            } else {
                StatusBadge(text = "Inactive")
            }
        }
    }
}

@Composable
private fun AppsSection(
    contextId: String,
    apps: List<InstalledApp>,
    appViewModel: AppViewModel,
    onAddApps: () -> Unit,
) {
    GlassSurface {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Apps",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "${apps.size}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(Modifier.height(NexusSpacing.md))

            if (apps.isEmpty()) {
                Text(
                    text = "No apps configured yet. Add apps to this context.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(NexusSpacing.md))
            } else {
                apps.forEach { app ->
                    val isInstalled = appViewModel.isAppInstalled(app.packageName)
                    ContextAppItem(
                        app = app,
                        isInstalled = isInstalled,
                        onRemove = { appViewModel.removeAppFromContext(contextId, app.packageName) },
                        onOpen = { appViewModel.launchApp(app.packageName) },
                    )
                    Spacer(Modifier.height(NexusSpacing.xs))
                }
            }

            TextButton(
                onClick = onAddApps,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(NexusSpacing.sm))
                Text(
                    text = "Add Apps",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun ActionsSection(
    contextId: String,
    actions: List<NexusAction>,
    actionViewModel: ActionViewModel,
    onAddAction: () -> Unit,
    onEditAction: (String) -> Unit,
    onDeleteAction: (String) -> Unit,
) {
    GlassSurface {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Actions",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "${actions.size}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(Modifier.height(NexusSpacing.md))

            if (actions.isEmpty()) {
                Text(
                    text = "No actions configured yet. Add actions to automate this context.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(NexusSpacing.md))
            } else {
                actions.forEachIndexed { index, action ->
                    ActionItem(
                        action = action,
                        index = index,
                        onToggleEnabled = { actionViewModel.toggleEnabled(action.id, it) },
                        onEdit = { onEditAction(action.id) },
                        onDelete = { onDeleteAction(action.id) },
                        onRun = { actionViewModel.executeAction(action) },
                        onMoveUp = { actionViewModel.moveUp(contextId, action.id) },
                        onMoveDown = { actionViewModel.moveDown(contextId, action.id) },
                    )
                    Spacer(Modifier.height(NexusSpacing.xs))
                }
            }

            TextButton(
                onClick = onAddAction,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(NexusSpacing.sm))
                Text(
                    text = "Add Action",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun DetailMetadata(context: NexusContext) {
    GlassSurface {
        Column(verticalArrangement = Arrangement.spacedBy(NexusSpacing.md)) {
            Text(
                text = "Details",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            MetadataRow("Apps", "${context.appCount}")
            MetadataRow("Actions", "${context.actionCount}")
            MetadataRow("Created", formatDate(context.createdAt))
            MetadataRow("Updated", formatDate(context.updatedAt))
            MetadataRow("ID", context.id.take(8) + "…")
        }
    }
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun DetailActions(
    context: NexusContext,
    viewModel: ContextViewModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCaptureCapsule: () -> Unit,
) {
    GlassSurface {
        Column {
            Text(
                text = "Actions",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = NexusSpacing.md),
            )

            if (context.isActive) {
                ActionRow(
                    label = "Deactivate Context",
                    description = "Remove this as the active context",
                    onClick = { viewModel.deactivateContext(context.id) },
                )
            } else {
                ActionRow(
                    label = "Activate Context",
                    description = "Set this as the active context",
                    onClick = { viewModel.activateContext(context.id) },
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                modifier = Modifier.padding(vertical = NexusSpacing.sm),
            )

            ActionRow(
                label = "Edit Context",
                description = "Modify name, description, icon, and color",
                onClick = onEdit,
                icon = Icons.Outlined.Edit,
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                modifier = Modifier.padding(vertical = NexusSpacing.sm),
            )

            ActionRow(
                label = "Duplicate Context",
                description = "Create a copy of this context",
                onClick = { viewModel.duplicateContext(context.id) },
                icon = Icons.Outlined.ContentCopy,
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                modifier = Modifier.padding(vertical = NexusSpacing.sm),
            )

            ActionRow(
                label = "Save as Capsule",
                description = "Capture this context as a reusable snapshot",
                onClick = onCaptureCapsule,
                icon = Icons.Outlined.Save,
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                modifier = Modifier.padding(vertical = NexusSpacing.sm),
            )

            ActionRow(
                label = "Delete Context",
                description = "Permanently remove this context",
                onClick = onDelete,
                icon = Icons.Outlined.Delete,
                isDestructive = true,
            )
        }
    }
}

@Composable
private fun ActionRow(
    label: String,
    description: String,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isDestructive: Boolean = false,
) {
    val labelColor = if (isDestructive) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.onSurface

    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "$label. $description" },
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = labelColor,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(NexusSpacing.sm))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = labelColor,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
