package com.nexus.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Work
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Maps a context's iconId string to a Compose [ImageVector].
 * Lives in the UI layer so the domain model stays Compose-free.
 */
fun contextIconFor(iconId: String): ImageVector = when (iconId) {
    "code" -> Icons.Outlined.Code
    "school" -> Icons.Outlined.School
    "flight" -> Icons.Outlined.Flight
    "person" -> Icons.Outlined.Person
    "work" -> Icons.Outlined.Work
    else -> Icons.Outlined.GridView
}

/** Available icons for the context editor picker. */
data class IconOption(val id: String, val icon: ImageVector, val label: String)

val availableContextIcons = listOf(
    IconOption("grid", Icons.Outlined.GridView, "General"),
    IconOption("code", Icons.Outlined.Code, "Coding"),
    IconOption("school", Icons.Outlined.School, "Study"),
    IconOption("flight", Icons.Outlined.Flight, "Travel"),
    IconOption("person", Icons.Outlined.Person, "Personal"),
    IconOption("work", Icons.Outlined.Work, "Work"),
)

/** Available accent colours for the context editor picker. */
val availableAccentColors = listOf(
    0xFF6366F1L, // Indigo
    0xFF06B6D4L, // Cyan
    0xFFF59E0BL, // Amber
    0xFFEC4899L, // Pink
    0xFF8B5CF6L, // Violet
    0xFF14B8A6L, // Teal
    0xFFF97316L, // Orange
    0xFFEF4444L, // Red
    0xFF22C55EL, // Green
)
