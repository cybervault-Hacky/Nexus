package com.nexus.app.ui.screens.contexts

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.nexus.app.R
import com.nexus.app.ui.components.ContextCard
import com.nexus.app.ui.theme.NexusSpacing
import kotlinx.coroutines.delay

/**
 * Contexts screen — shows all available contexts backed by Room.
 * Phase 2: connected to real ContextEngine via [ContextViewModel].
 */
@Composable
fun ContextsScreen(
    viewModel: ContextViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contexts by viewModel.contexts.collectAsState()
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    if (contexts.isEmpty()) {
        // ── Empty state ──────────────────────────────────────
        EmptyContextsState(
            onCreateClick = onNavigateToCreate,
            modifier = modifier,
        )
    } else {
        // ── Context list ─────────────────────────────────────
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = NexusSpacing.screenPadding,
                end = NexusSpacing.screenPadding,
                bottom = NexusSpacing.lg,
            ),
            verticalArrangement = Arrangement.spacedBy(NexusSpacing.itemGap),
        ) {
            // Header
            item {
                Column(modifier = Modifier.padding(top = NexusSpacing.xxl)) {
                    Text(
                        text = stringResource(R.string.contexts_title),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.height(NexusSpacing.xs))
                    Text(
                        text = stringResource(R.string.contexts_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(NexusSpacing.sectionGap))
                }
            }

            // Context cards with stagger
            itemsIndexed(
                items = contexts,
                key = { _, ctx -> ctx.id },
            ) { index, context ->
                val animDelay = index * 100
                var cardVisible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(animDelay.toLong())
                    cardVisible = true
                }
                AnimatedVisibility(
                    visible = cardVisible,
                    enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 4 },
                ) {
                    ContextCard(
                        context = context,
                        onClick = { onNavigateToDetail(context.id) },
                    )
                }
            }
        }
    }
}

/**
 * Shown when no contexts exist yet.
 */
@Composable
private fun EmptyContextsState(
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(NexusSpacing.screenPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "No contexts yet",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(NexusSpacing.sm))
        Text(
            text = "Create your first context to start organizing your workflow.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(NexusSpacing.xl))
        FloatingActionButton(
            onClick = onCreateClick,
            containerColor = MaterialTheme.colorScheme.primary,
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = stringResource(R.string.contexts_new),
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}
