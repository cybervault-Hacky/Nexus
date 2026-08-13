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
 * NEXUS Home Dashboard — premium, spacious, informative.
 * Communicates: "What is active? What is happening? What can I do?"
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
    var showGreeting by remember { mutableStateOf(false) }
    var showContext by remember { mutableStateOf(false) }
    var showCapsules by remember { mutableStateOf(false) }
    var showActions by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showGreeting = true; delay(60)
        showContext = true; delay(60)
        showCapsules = true; delay(60)
        showActions = true
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = NexusSpacing.xxl),
    ) {
        // ── Header ───────────────────────────────────────────
        item {
            Column(
                modifier = Modifier.padding(
                    start = NexusSpacing.screenPadding,
                    end = NexusSpacing.screenPadding,
                    top = NexusSpacing.xxxl,
                ),
            ) {
                AnimatedVisibility(
                    visible = showGreeting,
                    enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 6 },
                ) {
                    Column {
                        Text(
                            text = greetingText(),
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Spacer(Modifier.height(NexusSpacing.xs))
                        Text(
                            text = "NEXUS",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }

        // ── Active Context ───────────────────────────────────
        item {
            AnimatedVisibility(
                visible = showContext,
                enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 6 },
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
                enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 6 },
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
                    enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 6 },
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

        // ── Automations ──────────────────────────────────────
        item {
            AnimatedVisibility(
                visible = showActions,
                enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 6 },
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
                        text = if (activeAutomationCount > 0) "$activeAutomationCount active" else "No active automations",
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
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 6 },
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
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 6 },
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

        item { Spacer(Modifier.height(NexusSpacing.lg)) }
    }
}

private fun greetingText(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good morning"
        hour < 18 -> "Good afternoon"
        else -> "Good evening"
    }
}
