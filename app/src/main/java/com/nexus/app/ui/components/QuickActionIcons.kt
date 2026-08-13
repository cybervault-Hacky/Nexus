package com.nexus.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.nexus.app.domain.model.QuickAction

/**
 * Maps a [QuickAction] id to its display icon.
 * Isolated here so the domain model stays Compose-free.
 */
fun QuickAction.icon(): ImageVector = when (id) {
    "new_context" -> Icons.Outlined.AddCircle
    "new_capsule" -> Icons.Outlined.FolderOpen
    "search" -> Icons.Outlined.Search
    "run_action" -> Icons.Outlined.Bolt
    else -> Icons.Outlined.Bolt
}

/** Static set of quick actions for Phase 1 UI preview. */
val defaultQuickActions = listOf(
    QuickAction(
        id = "new_context",
        label = "New Context",
        description = "Create a new workflow context",
    ),
    QuickAction(
        id = "new_capsule",
        label = "New Capsule",
        description = "Save current workspace state",
    ),
    QuickAction(
        id = "search",
        label = "Search",
        description = "Search across everything",
    ),
    QuickAction(
        id = "run_action",
        label = "Run Action",
        description = "Execute a quick action",
    ),
)
