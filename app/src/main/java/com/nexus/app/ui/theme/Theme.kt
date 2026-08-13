package com.nexus.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.nexus.app.domain.model.ThemeMode

// ── Dark colour scheme ──────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary = NexusPrimary,
    onPrimary = Color(0xFF1A1A2E),
    primaryContainer = NexusPrimaryDark,
    onPrimaryContainer = NexusPrimaryLight,
    secondary = NexusSecondary,
    onSecondary = Color(0xFF003344),
    secondaryContainer = Color(0xFF004D60),
    onSecondaryContainer = Color(0xFF97F0FF),
    tertiary = NexusTertiary,
    onTertiary = Color(0xFF2D1B4E),
    tertiaryContainer = Color(0xFF443370),
    onTertiaryContainer = Color(0xFFD4BBFF),
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkBorder,
    error = NexusError,
    onError = Color(0xFF420000),
)

// ── Light colour scheme ─────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary = NexusPrimaryDark,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE0E0FF),
    onPrimaryContainer = Color(0xFF1A1A5E),
    secondary = Color(0xFF0891B2),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCFFAFE),
    onSecondaryContainer = Color(0xFF003344),
    tertiary = Color(0xFF7C3AED),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFEDE9FE),
    onTertiaryContainer = Color(0xFF2D1B4E),
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightBorder,
    error = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF),
)

/**
 * Global singleton for the current theme mode.
 * A simple StateHolder is sufficient for Phase 1;
 * a DataStore-backed implementation will replace this later.
 */
object NexusThemeState {
    var themeMode by mutableStateOf(ThemeMode.DARK)
    var animationsEnabled by mutableStateOf(true)
}

/**
 * NEXUS theme wrapper.
 * Applies the correct colour scheme based on [NexusThemeState.themeMode]
 * and sets system-bar colours to match.
 */
@Composable
fun NexusTheme(
    themeMode: ThemeMode = NexusThemeState.themeMode,
    content: @Composable () -> Unit,
) {
    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !isDark
                isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NexusTypography,
        shapes = NexusShapes,
        content = content,
    )
}
