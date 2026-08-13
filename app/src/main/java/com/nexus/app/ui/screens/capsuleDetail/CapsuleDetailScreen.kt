package com.nexus.app.ui.screens.capsuleDetail

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nexus.app.R
import com.nexus.app.domain.model.ActionType
import com.nexus.app.domain.model.NexusCapsule
import com.nexus.app.ui.components.GlassSurface
import com.nexus.app.ui.components.StatusBadge
import com.nexus.app.ui.screens.capsules.CapsuleViewModel
import com.nexus.app.ui.theme.NexusSpacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Detail view for a single Capsule.
 * Shows snapshot contents, rename/description editing, and delete.
 * Restoration is NOT implemented in Phase 5.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapsuleDetailScreen(
    capsuleId: String,
    viewModel: CapsuleViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToRestore: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val capsules by viewModel.capsules.collectAsState()
    val capsule = remember(capsules, capsuleId) { capsules.find { it.id == capsuleId } }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showEditDescriptionDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = capsule?.name ?: "Capsule Details",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Go back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { innerPadding ->
        if (capsule == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Capsule not found", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
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
                        CapsuleHeader(capsule!!)
                    }
                }

                // Snapshot indicator
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(450)) + slideInVertically(tween(450)) { it / 4 },
                    ) {
                        GlassSurface(
                            containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f),
                            borderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                        ) {
                            Text(
                                text = "This Capsule represents the state of the Context at capture time. " +
                                    "The snapshot is immutable — only the name and description can be edited.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                // Context snapshot
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 4 },
                    ) {
                        GlassSurface {
                            Column {
                                Text("Context Snapshot", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(NexusSpacing.sm))
                                val snapshot = capsule!!.contextSnapshot
                                if (snapshot != null) {
                                    MetadataRow("Name", snapshot.name)
                                    MetadataRow("Description", snapshot.description.ifBlank { "None" })
                                    MetadataRow("Icon", snapshot.iconId)
                                } else {
                                    Text("No context snapshot", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                // Apps
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 4 },
                    ) {
                        GlassSurface {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("Apps", style = MaterialTheme.typography.titleMedium)
                                    Text("${capsule!!.appCount}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(Modifier.height(NexusSpacing.sm))
                                if (capsule!!.appSnapshots.isEmpty()) {
                                    Text("No apps captured", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                } else {
                                    capsule!!.appSnapshots.forEach { app ->
                                        Text("${app.appName} (${app.packageName})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(Modifier.height(NexusSpacing.xs))
                                    }
                                }
                            }
                        }
                    }
                }

                // Actions
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(700)) + slideInVertically(tween(700)) { it / 4 },
                    ) {
                        GlassSurface {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("Actions", style = MaterialTheme.typography.titleMedium)
                                    Text("${capsule!!.actionCount}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(Modifier.height(NexusSpacing.sm))
                                if (capsule!!.actionSnapshots.isEmpty()) {
                                    Text("No actions captured", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                } else {
                                    capsule!!.actionSnapshots.sortedBy { it.position }.forEachIndexed { index, action ->
                                        val typeLabel = when (action.type) {
                                            ActionType.OPEN_APP -> "Open App"
                                            ActionType.OPEN_URL -> "Open URL"
                                            ActionType.DELAY -> "Delay"
                                        }
                                        Text("${index + 1}. ${action.name} ($typeLabel)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(Modifier.height(NexusSpacing.xs))
                                    }
                                }
                            }
                        }
                    }
                }

                // Restore, Edit metadata & Delete
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { it / 4 },
                    ) {
                        GlassSurface {
                            Column {
                                // Restore
                                TextButton(
                                    onClick = { onNavigateToRestore(capsuleId) },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Icon(Icons.Outlined.Restore, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(NexusSpacing.sm))
                                    Text("Restore Capsule", color = MaterialTheme.colorScheme.primary)
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = NexusSpacing.sm))

                                // Rename
                                TextButton(
                                    onClick = { showRenameDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Icon(Icons.Outlined.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(NexusSpacing.sm))
                                    Text("Rename Capsule", color = MaterialTheme.colorScheme.primary)
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = NexusSpacing.sm))

                                // Edit description
                                TextButton(
                                    onClick = { showEditDescriptionDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Icon(Icons.Outlined.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(NexusSpacing.sm))
                                    Text("Edit Description", color = MaterialTheme.colorScheme.onSurface)
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = NexusSpacing.sm))

                                // Delete
                                TextButton(
                                    onClick = { showDeleteDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(NexusSpacing.sm))
                                    Text("Delete Capsule", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete dialog
    if (showDeleteDialog && capsule != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete capsule?") },
            text = { Text("\"${capsule.name}\" will be permanently deleted. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteCapsule(capsuleId)
                    onNavigateBack()
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }

    // Rename dialog
    if (showRenameDialog && capsule != null) {
        var newName by remember { mutableStateOf(capsule.name) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Capsule") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { if (it.length <= 60) newName = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    ),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showRenameDialog = false
                    viewModel.renameCapsule(capsuleId, newName)
                }) { Text("Save", color = MaterialTheme.colorScheme.primary) }
            },
            dismissButton = { TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") } },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }

    // Edit description dialog
    if (showEditDescriptionDialog && capsule != null) {
        var newDesc by remember { mutableStateOf(capsule.description) }
        AlertDialog(
            onDismissRequest = { showEditDescriptionDialog = false },
            title = { Text("Edit Description") },
            text = {
                OutlinedTextField(
                    value = newDesc,
                    onValueChange = { if (it.length <= 200) newDesc = it },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    ),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showEditDescriptionDialog = false
                    viewModel.updateDescription(capsuleId, newDesc)
                }) { Text("Save", color = MaterialTheme.colorScheme.primary) }
            },
            dismissButton = { TextButton(onClick = { showEditDescriptionDialog = false }) { Text("Cancel") } },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }
}

@Composable
private fun CapsuleHeader(capsule: NexusCapsule) {
    val accent = Color(capsule.accentColor)
    GlassSurface {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(capsule.name, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
            if (capsule.description.isNotBlank()) {
                Spacer(Modifier.height(NexusSpacing.xs))
                Text(capsule.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(NexusSpacing.md))
            Row(horizontalArrangement = Arrangement.spacedBy(NexusSpacing.sm)) {
                StatusBadge(text = "${capsule.appCount} Apps", backgroundColor = accent.copy(alpha = 0.15f), textColor = accent)
                StatusBadge(text = "${capsule.actionCount} Actions", backgroundColor = accent.copy(alpha = 0.15f), textColor = accent)
            }
            Spacer(Modifier.height(NexusSpacing.sm))
            Text("Captured ${formatDate(capsule.capturedAt)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
