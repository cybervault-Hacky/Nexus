package com.nexus.app.ui.screens.actions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.nexus.app.R
import com.nexus.app.ui.components.defaultQuickActions
import com.nexus.app.ui.components.QuickActionCard
import com.nexus.app.ui.components.StatusBadge
import com.nexus.app.ui.theme.NexusSpacing
import kotlinx.coroutines.delay

/**
 * Quick Actions screen — grid of available actions.
 * Phase 4 will connect this to the real Action Engine.
 */
@Composable
fun QuickActionsScreen(modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Column(modifier = Modifier.padding(
            start = NexusSpacing.screenPadding,
            end = NexusSpacing.screenPadding,
            top = NexusSpacing.xxl,
        )) {
            Text(
                text = stringResource(R.string.actions_title),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(NexusSpacing.xs))
            Text(
                text = stringResource(R.string.actions_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(NexusSpacing.xs))
            StatusBadge(text = stringResource(R.string.actions_coming_soon))
            Spacer(Modifier.height(NexusSpacing.sectionGap))
        }

        // Grid of action buttons
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(
                start = NexusSpacing.screenPadding,
                end = NexusSpacing.screenPadding,
                bottom = NexusSpacing.lg,
            ),
            horizontalArrangement = Arrangement.spacedBy(NexusSpacing.itemGap),
            verticalArrangement = Arrangement.spacedBy(NexusSpacing.itemGap),
        ) {
            itemsIndexed(
                items = defaultQuickActions,
                key = { _, action -> action.id },
            ) { index, action ->
                val animDelay = index * 80
                var actionVisible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(animDelay.toLong())
                    actionVisible = true
                }
                AnimatedVisibility(
                    visible = actionVisible,
                    enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 4 },
                ) {
                    QuickActionCard(
                        action = action,
                        onClick = { /* Phase 4 */ },
                    )
                }
            }
        }
    }
}
