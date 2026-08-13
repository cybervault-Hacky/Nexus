package com.nexus.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * NEXUS Premium Color System
 *
 * A neutral-first palette inspired by premium product design.
 * Restrained accent usage, strong text contrast, sophisticated surfaces.
 */

// ── Brand Accent ─────────────────────────────────────────────
// A refined indigo — elegant without being loud
val NexusPrimary = Color(0xFF6C5CE7)          // Soft violet-indigo
val NexusPrimaryDark = Color(0xFF5A4BD1)      // Deeper variant
val NexusPrimaryLight = Color(0xFF9B8FFA)     // Lighter variant
val NexusSecondary = Color(0xFF0EA5E9)        // Muted sky blue
val NexusTertiary = Color(0xFF8B5CF6)         // Soft violet

// ── Dark Mode Surfaces ───────────────────────────────────────
// Deep, warm neutrals — not pure black
val DarkBackground = Color(0xFF0F0F14)
val DarkSurface = Color(0xFF17171E)
val DarkSurfaceVariant = Color(0xFF1E1E28)
val DarkSurfaceElevated = Color(0xFF252530)
val DarkOnBackground = Color(0xFFEAEAEE)
val DarkOnSurface = Color(0xFFD4D4DA)
val DarkOnSurfaceVariant = Color(0xFF8A8A96)
val DarkOutline = Color(0xFF2C2C38)
val DarkBorder = Color(0xFF28283A)

// ── Light Mode Surfaces ──────────────────────────────────────
// Clean, warm whites — not clinical
val LightBackground = Color(0xFFF6F6FA)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF0F0F5)
val LightSurfaceElevated = Color(0xFFFFFFFF)
val LightOnBackground = Color(0xFF111118)
val LightOnSurface = Color(0xFF1A1A24)
val LightOnSurfaceVariant = Color(0xFF6E6E80)
val LightOutline = Color(0xFFE0E0E6)
val LightBorder = Color(0xFFE8E8EE)

// ── Semantic Colors ──────────────────────────────────────────
// Reserved strictly for meaning — never decorative
val NexusSuccess = Color(0xFF22C55E)
val NexusWarning = Color(0xFFF59E0B)
val NexusError = Color(0xFFEF4444)
val NexusInfo = Color(0xFF3B82F6)

// ── Surface Overlays ─────────────────────────────────────────
val DarkGlassSurface = Color(0x0DFFFFFF)    // Subtle 5% white
val LightGlassSurface = Color(0x08000000)   // Subtle 3% black
