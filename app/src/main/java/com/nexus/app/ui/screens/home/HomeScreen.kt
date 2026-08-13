package com.nexus.app.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.nexus.app.domain.model.NexusCapsule
import com.nexus.app.domain.model.NexusContext
import com.nexus.app.ui.components.defaultQuickActions
import com.nexus.app.ui.components.CapsuleCard
import com.nexus.app.ui.components.ContextCard
import com.nexus.app.ui.components.QuickActionCard
import com.nexus.app.ui.components.SectionHeader
import com.nexus.app.ui.navigation.Screen
import com.nexus.app.ui.theme.NexusSpacing
import kotlinx.coroutines.delay

/**
 * Home Dashboard — the default landing screen.
 * Shows the active context, recent capsules, and quick actions.
 */
@Composable
fun HomeScreen(
    onNavigate: (Screen) -> Unit,
    onNavigateToContextDetail: (String) -> Unit,
    onNavigateToCapsuleDetail: (String) -> Unit,
    activeContext: NexusContext?,
    recentCapsules: List<NexusCapsule>,
    activeAutomationCount: Int = 0,
    modifier: Modifier = Modifier,
) {
    // Staggered entrance animation
    var showGreeting by remember { mutableStateOf(false) }
    var showContext by remember { mutableStateOf(false) }
    var showCapsules by remember { mutableStateOf(false) }
    var showActions by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showGreeting = true; delay(80)
        showContext = true; delay(80)
        showCapsules = true; delay(80)
        showActions = true
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = NexusSpacing.lg),
    ) {
        // ── Header ───────────────────────────────────────────
        item {
            Column(
                modifier = Modifier.padding(
                    start = NexusSpacing.screenPadding,
                    end = NexusSpacing.screenPadding,
                    top = NexusSpacing.xxl,
                ),
            ) {
                AnimatedVisibility(
                    visible = showGreeting,
                    enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 4 },
                ) {
                    Column {
                        Text(
                            text = "NEXUS",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.height(NexusSpacing.xs))
                        Text(
                            text = greetingText(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }
        }

        // ── Active Context ───────────────────────────────────
        item {
            AnimatedVisibility(
                visible = showContext,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 4 },
            ) {
                Column(
                    modifier = Modifier.padding(top = NexusSpacing.sectionGap),
                ) {
                    SectionHeader(
                        title = stringResource(R.string.home_active_context),
                        modifier = Modifier.padding(horizontal = NexusSpacing.screenPadding),
                    )

                    if (activeContext != null) {
                        ContextCard(
                            context = activeContext,
                            onClick = { onNavigateToContextDetail(activeContext.id) },
                            modifier = Modifier.padding(horizontal = NexusSpacing.screenPadding),
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.home_no_active_context),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = NexusSpacing.screenPadding),
                        )
                    }
                }
            }
        }

        // ── Recent Capsules ──────────────────────────────────
        item {
            AnimatedVisibility(
                visible = showCapsules,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 4 },
            ) {
                Column(
                    modifier = Modifier.padding(top = NexusSpacing.sectionGap),
                ) {
                    SectionHeader(
                        title = stringResource(R.string.home_recent_capsules),
                        actionLabel = "See all",
                        onAction = { onNavigate(Screen.Capsules) },
                        modifier = Modifier.padding(horizontal = NexusSpacing.screenPadding),
                    )
                }
            }
        }

        if (recentCapsules.isNotEmpty()) {
            item {
                AnimatedVisibility(
                    visible = showCapsules,
                    enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 4 },
                ) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = NexusSpacing.screenPadding),
                        horizontalArrangement = Arrangement.spacedBy(NexusSpacing.itemGap),
                    ) {
                        items(
                            items = recentCapsules.take(5),
                            key = { it.id },
                        ) { capsule ->
                            CapsuleCard(
                                capsule = capsule,
                                onClick = { onNavigateToCapsuleDetail(capsule.id) },
                                modifier = Modifier.fillParentMaxWidth(0.85f),
                            )
                        }
                    }
                }
            }
        }

        // ── Automations ────────────────────────────────────
        item {
            AnimatedVisibility(
                visible = showActions,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 4 },
            ) {
                Column(
                    modifier = Modifier.padding(top = NexusSpacing.sectionGap),
                ) {
                    SectionHeader(
                        title = "Automations",
                        actionLabel = "See all",
                        onAction = { onNavigate(Screen.Automations) },
                        modifier = Modifier.padding(horizontal = NexusSpacing.screenPadding),
                    )
                    Text(
                        text = if (activeAutomationCount > 0) "$activeAutomationCount active automations" else "No active automations",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = NexusSpacing.screenPadding),
                    )
                }
            }
        }

        // ── Quick Actions ────────────────────────────────────
        item {
            AnimatedVisibility(
                visible = showActions,
                enter = fadeIn(tween(450)) + slideInVertically(tween(450)) { it / 4 },
            ) {
                Column(
                    modifier = Modifier.padding(top = NexusSpacing.sectionGap),
                ) {
                    SectionHeader(
                        title = stringResource(R.string.home_quick_actions),
                        modifier = Modifier.padding(horizontal = NexusSpacing.screenPadding),
                    )
                }
            }
        }

        item {
            AnimatedVisibility(
                visible = showActions,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 4 },
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = NexusSpacing.screenPadding),
                    horizontalArrangement = Arrangement.spacedBy(NexusSpacing.base),
                ) {
                    items(
                        items = defaultQuickActions,
                        key = { it.id },
                    ) { action ->
                        QuickActionCard(
                            action = action,
                            onClick = { onNavigate(Screen.Actions) },
                        )
                    }
                }
            }
        }

        // Bottom spacer
        item { Spacer(Modifier.height(NexusSpacing.lg)) }
    }
}

/** Returns a time-appropriate greeting. */
private fun greetingText(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good morning"
        hour < 18 -> "Good afternoon"
        else -> "Good evening"
    }
}
