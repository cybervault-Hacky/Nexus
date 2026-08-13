package com.nexus.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nexus.app.R
import com.nexus.app.domain.model.NexusContext
import com.nexus.app.ui.theme.NexusSpacing

/**
 * Premium context card.
 * Clean layout, strong hierarchy, elegant accent treatment.
 */
@Composable
fun ContextCard(
    context: NexusContext,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = Color(context.accentColor)
    val animatedAccent by animateColorAsState(
        targetValue = if (context.isActive) accent else accent.copy(alpha = 0.35f),
        animationSpec = tween(400),
        label = "contextAccent",
    )

    val cardDescription = stringResource(R.string.cd_context_card, context.name)

    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = cardDescription }
            .clickable(onClick = onClick),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(animatedAccent),
                    )
                    Spacer(Modifier.width(NexusSpacing.sm))
                    Text(
                        text = context.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (context.isActive) {
                    StatusBadge(
                        text = stringResource(R.string.contexts_active),
                        backgroundColor = accent.copy(alpha = 0.12f),
                        textColor = accent,
                    )
                }
            }

            Spacer(Modifier.height(NexusSpacing.sm))

            Text(
                text = context.description.ifBlank { "No description" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(NexusSpacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${context.appCount} apps  ·  ${context.actionCount} actions",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = stringResource(R.string.home_open),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
