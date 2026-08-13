package com.nexus.app.ui.screens.settings

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.nexus.app.R
import com.nexus.app.domain.model.ThemeMode
import com.nexus.app.ui.components.GlassSurface
import com.nexus.app.ui.theme.NexusSpacing
import com.nexus.app.ui.theme.NexusThemeState

/**
 * Settings screen with Appearance, Behaviour, and About sections.
 * Uses a scalable list structure so future phases can add rows
 * without restructuring the screen.
 */
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
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
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(NexusSpacing.sectionGap))
            }
        }

        // ── Appearance ───────────────────────────────────────
        item {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 4 },
            ) {
                SettingsSection(title = stringResource(R.string.settings_appearance)) {
                    ThemeSettingRow()
                    Spacer(Modifier.height(NexusSpacing.sm))
                    DynamicColorSettingRow()
                    Spacer(Modifier.height(NexusSpacing.sm))
                    AnimationSettingRow()
                }
            }
        }

        // ── Behaviour ────────────────────────────────────────
        item {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 4 },
            ) {
                SettingsSection(title = stringResource(R.string.settings_behavior)) {
                    DefaultScreenSettingRow()
                    Spacer(Modifier.height(NexusSpacing.sm))
                    ConfirmActionsSettingRow()
                }
            }
        }

        // ── About ────────────────────────────────────────────
        item {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 4 },
            ) {
                SettingsSection(title = stringResource(R.string.settings_about)) {
                    VersionSettingRow()
                    Spacer(Modifier.height(NexusSpacing.sm))
                    SettingRow(
                        title = stringResource(R.string.settings_licenses),
                        subtitle = "Open-source licences",
                        onClick = { /* Phase 6+ */ },
                    )
                    Spacer(Modifier.height(NexusSpacing.sm))
                    SettingRow(
                        title = stringResource(R.string.settings_open_source),
                        subtitle = "github.com/nexus-app",
                        onClick = { /* Phase 6+ */ },
                    )
                }
            }
        }
    }
}

// ── Section wrapper ──────────────────────────────────────────

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = NexusSpacing.md),
        )
        GlassSurface {
            Column { content() }
        }
    }
}

// ── Generic clickable row ────────────────────────────────────

@Composable
private fun SettingRow(
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = NexusSpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(NexusSpacing.base))
            trailing()
        }
    }
}

// ── Individual setting rows ──────────────────────────────────

@Composable
private fun ThemeSettingRow() {
    val modes = ThemeMode.entries
    val labels = listOf(
        stringResource(R.string.settings_theme_dark),
        stringResource(R.string.settings_theme_light),
        stringResource(R.string.settings_theme_system),
    )
    val currentIndex = modes.indexOf(NexusThemeState.themeMode)

    SettingRow(
        title = stringResource(R.string.settings_theme),
        subtitle = labels[currentIndex],
        trailing = {
            Row {
                labels.forEachIndexed { idx, label ->
                    val selected = idx == currentIndex
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clickable { NexusThemeState.themeMode = modes[idx] }
                            .padding(
                                horizontal = NexusSpacing.sm,
                                vertical = NexusSpacing.md,
                            ),
                    )
                }
            }
        },
    )
}

@Composable
private fun DynamicColorSettingRow() {
    var enabled by remember { mutableStateOf(false) }
    val desc = stringResource(R.string.settings_dynamic_color_desc)
    SettingRow(
        title = stringResource(R.string.settings_dynamic_color),
        subtitle = stringResource(R.string.settings_dynamic_color_desc),
        trailing = {
            Switch(
                checked = enabled,
                onCheckedChange = { enabled = it },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier.semantics { contentDescription = desc },
            )
        },
    )
}

@Composable
private fun AnimationSettingRow() {
    val desc = stringResource(R.string.settings_animations_desc)
    SettingRow(
        title = stringResource(R.string.settings_animations),
        subtitle = stringResource(R.string.settings_animations_desc),
        trailing = {
            Switch(
                checked = NexusThemeState.animationsEnabled,
                onCheckedChange = { NexusThemeState.animationsEnabled = it },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier.semantics { contentDescription = desc },
            )
        },
    )
}

@Composable
private fun DefaultScreenSettingRow() {
    val screens = listOf("Home", "Contexts", "Capsules", "Actions")
    var selected by remember { mutableStateOf("Home") }
    SettingRow(
        title = stringResource(R.string.settings_default_screen),
        subtitle = selected,
        trailing = {
            Row {
                screens.forEachIndexed { _, screen ->
                    Text(
                        text = screen,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (screen == selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clickable { selected = screen }
                            .padding(
                                horizontal = NexusSpacing.xs,
                                vertical = NexusSpacing.md,
                            ),
                    )
                }
            }
        },
    )
}

@Composable
private fun ConfirmActionsSettingRow() {
    var enabled by remember { mutableStateOf(true) }
    val desc = stringResource(R.string.settings_confirm_actions_desc)
    SettingRow(
        title = stringResource(R.string.settings_confirm_actions),
        subtitle = stringResource(R.string.settings_confirm_actions_desc),
        trailing = {
            Switch(
                checked = enabled,
                onCheckedChange = { enabled = it },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier.semantics { contentDescription = desc },
            )
        },
    )
}

@Composable
private fun VersionSettingRow() {
    SettingRow(
        title = stringResource(R.string.settings_version),
        subtitle = "1.0.0 (Phase 1)",
    )
}
