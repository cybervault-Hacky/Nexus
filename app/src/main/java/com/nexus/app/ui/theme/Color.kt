package com.nexus.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Centralised colour tokens for the NEXUS design system.
 * Grouped by role rather than by screen so that every composable
 * references the same semantic palette.
 */

// ── Brand ───────────────────────────────────────────────────
val NexusPrimary = Color(0xFF818CF8)          // Indigo-400
val NexusPrimaryDark = Color(0xFF6366F1)      // Indigo-500
val NexusPrimaryLight = Color(0xFFA5B4FC)     // Indigo-300
val NexusSecondary = Color(0xFF22D3EE)        // Cyan-400
val NexusTertiary = Color(0xFFA78BFA)         // Violet-400

// ── Dark surface tokens ─────────────────────────────────────
val DarkBackground = Color(0xFF0A0A0F)
val DarkSurface = Color(0xFF13131A)
val DarkSurfaceVariant = Color(0xFF1C1C27)
val DarkSurfaceElevated = Color(0xFF22222F)
val DarkOnBackground = Color(0xFFE8E8ED)
val DarkOnSurface = Color(0xFFD1D1D9)
val DarkOnSurfaceVariant = Color(0xFF8E8E99)
val DarkOutline = Color(0xFF2E2E3A)
val DarkBorder = Color(0xFF2A2A36)

// ── Light surface tokens ────────────────────────────────────
val LightBackground = Color(0xFFF8F8FC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF0F0F6)
val LightSurfaceElevated = Color(0xFFFFFFFF)
val LightOnBackground = Color(0xFF111118)
val LightOnSurface = Color(0xFF1C1C24)
val LightOnSurfaceVariant = Color(0xFF6B6B78)
val LightOutline = Color(0xFFDDDDDF)
val LightBorder = Color(0xFFE5E5EA)

// ── Semantic colours (shared) ───────────────────────────────
val NexusSuccess = Color(0xFF34D399)
val NexusWarning = Color(0xFFFBBF24)
val NexusError = Color(0xFFF87171)
val NexusInfo = Color(0xFF60A5FA)

// ── Glass surface overlay ───────────────────────────────────
val DarkGlassSurface = Color(0x14FFFFFF)   // ~8% white overlay
val LightGlassSurface = Color(0x0A000000)  // ~4% black overlay
