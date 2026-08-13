package com.nexus.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.nexus.app.domain.model.WorkflowState
import com.nexus.app.ui.theme.NexusSpacing

/**
 * Premium workflow progress indicator.
 */
@Composable
fun WorkflowProgress(
    state: WorkflowState,
    onCancel: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = state !is WorkflowState.Idle,
        enter = fadeIn(tween(300)),
    ) {
        GlassSurface(
            modifier = modifier.fillMaxWidth(),
            borderColor = when (state) {
                is WorkflowState.Running -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                is WorkflowState.Completed -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                is WorkflowState.Failed -> MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                is WorkflowState.Cancelled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
            },
        ) {
            Column {
                when (state) {
                    is WorkflowState.Running -> {
                        Text(
                            text = "Running workflow",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(NexusSpacing.sm))
                        Text(
                            text = "Step ${state.completedCount + 1} of ${state.totalCount}: ${state.currentActionName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(NexusSpacing.sm))
                        LinearProgressIndicator(
                            progress = {
                                if (state.totalCount > 0) (state.completedCount.toFloat()) / state.totalCount else 0f
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(MaterialTheme.shapes.extraSmall),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                        Spacer(Modifier.height(NexusSpacing.sm))
                        TextButton(onClick = onCancel) {
                            Text("Cancel", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    is WorkflowState.Completed -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(NexusSpacing.sm))
                            Text("Workflow completed: ${state.result.completedCount}/${state.result.totalCount}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(Modifier.height(NexusSpacing.sm))
                        TextButton(onClick = onDismiss) { Text("Dismiss") }
                    }
                    is WorkflowState.Failed -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(NexusSpacing.sm))
                            Text("Workflow stopped: ${state.result.completedCount}/${state.result.totalCount}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                        }
                        Spacer(Modifier.height(NexusSpacing.sm))
                        TextButton(onClick = onDismiss) { Text("Dismiss") }
                    }
                    is WorkflowState.Cancelled -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Cancel, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(NexusSpacing.sm))
                            Text("Workflow cancelled: ${state.result.completedCount}/${state.result.totalCount}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(NexusSpacing.sm))
                        TextButton(onClick = onDismiss) { Text("Dismiss") }
                    }
                    else -> {}
                }
            }
        }
    }
}
