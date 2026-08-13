package com.nexus.app.ui.screens.automationTemplates

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.nexus.app.data.automation.template.AutomationTemplate
import com.nexus.app.data.automation.template.AutomationTemplates
import com.nexus.app.ui.components.GlassSurface
import com.nexus.app.ui.components.StatusBadge
import com.nexus.app.ui.theme.NexusSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(
    onUseTemplate: (AutomationTemplate) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val templates = AutomationTemplates.all()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Templates", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Go back") } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(start = NexusSpacing.screenPadding, end = NexusSpacing.screenPadding, bottom = NexusSpacing.xxl),
            verticalArrangement = Arrangement.spacedBy(NexusSpacing.itemGap),
        ) {
            item {
                Text("Choose a template to quickly create an automation.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = NexusSpacing.xxl))
                Spacer(Modifier.height(NexusSpacing.md))
            }
            items(templates) { template ->
                GlassSurface(modifier = Modifier.fillMaxWidth().clickable { onUseTemplate(template) }) {
                    Column {
                        Text(template.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(NexusSpacing.xs))
                        Text(template.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(NexusSpacing.sm))
                        StatusBadge(text = template.category.name, backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), textColor = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
