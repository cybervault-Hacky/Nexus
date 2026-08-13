package com.nexus.app.ui.screens.capsules

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.nexus.app.ui.components.CapsuleCard
import com.nexus.app.ui.components.GlassSurface
import com.nexus.app.ui.theme.NexusSpacing
import kotlinx.coroutines.delay

/**
 * Capsules screen — lists saved workspace capsules.
 * Phase 5: connected to real Capsule Engine via [CapsuleViewModel].
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CapsulesScreen(
    viewModel: CapsuleViewModel,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val capsules by viewModel.capsules.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

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
                    text = stringResource(R.string.capsules_title),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(NexusSpacing.xs))
                Text(
                    text = stringResource(R.string.capsules_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(NexusSpacing.sectionGap))
            }
        }

        // Search
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text("Search capsules…") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                ),
            )
        }

        // Sort options
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(NexusSpacing.sm),
            ) {
                CapsuleSortOrder.entries.forEach { order ->
                    val label = when (order) {
                        CapsuleSortOrder.NEWEST -> "Newest"
                        CapsuleSortOrder.OLDEST -> "Oldest"
                        CapsuleSortOrder.NAME_ASC -> "Name A-Z"
                    }
                    val selected = order == sortOrder
                    GlassSurface(
                        containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        borderColor = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        contentPadding = NexusSpacing.sm,
                        modifier = Modifier.clickable { viewModel.onSortOrderChanged(order) },
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        // Empty state or capsule list
        if (capsules.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = NexusSpacing.xxxxl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = if (searchQuery.isBlank()) "No capsules yet" else "No matching capsules",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    if (searchQuery.isBlank()) {
                        Spacer(Modifier.height(NexusSpacing.sm))
                        Text(
                            text = "Capture a context snapshot to create your first capsule.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        } else {
            // Capsule cards with stagger
            itemsIndexed(
                items = capsules,
                key = { _, cap -> cap.id },
            ) { index, capsule ->
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
                    CapsuleCard(
                        capsule = capsule,
                        onClick = { onNavigateToDetail(capsule.id) },
                    )
                }
            }
        }
    }
}
