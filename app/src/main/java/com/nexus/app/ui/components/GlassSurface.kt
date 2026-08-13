package com.nexus.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nexus.app.ui.theme.NexusSpacing

/**
 * Premium surface card — the foundational container throughout NEXUS.
 * Subtle translucency with refined borders. Not flashy — elegant.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = NexusSpacing.md,
    contentPadding: Dp = NexusSpacing.cardPadding,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = MaterialTheme.shapes.medium
    Box(
        modifier = modifier
            .clip(shape)
            .background(containerColor, shape)
            .border(0.5.dp, borderColor, shape)
            .padding(contentPadding),
        content = content,
    )
}
