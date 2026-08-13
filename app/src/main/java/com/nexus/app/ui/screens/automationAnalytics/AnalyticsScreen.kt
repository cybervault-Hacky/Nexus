package com.nexus.app.ui.screens.automationAnalytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.nexus.app.data.automation.health.AutomationHealthEngine
import com.nexus.app.data.local.AutomationEntity
import com.nexus.app.data.local.AutomationExecutionEntity
import com.nexus.app.ui.components.GlassSurface
import com.nexus.app.ui.theme.NexusSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    rules: List<AutomationEntity>,
    executions: List<AutomationExecutionEntity>,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stats = AutomationHealthEngine.calculateStats(executions)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Analytics", style = MaterialTheme.typography.titleLarge) },
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
                GlassSurface(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("Execution Statistics", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(NexusSpacing.sm))
                        StatRow("Total executions", "${stats.totalExecutions}")
                        StatRow("Successful", "${stats.successfulExecutions}")
                        StatRow("Failed", "${stats.failedExecutions}")
                        StatRow("Cancelled", "${stats.cancelledExecutions}")
                        StatRow("Success rate", "${(stats.successRate * 100).toInt()}%")
                        StatRow("Avg duration", "${stats.averageDurationMs}ms")
                    }
                }
            }

            item {
                GlassSurface(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("Automation Health", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(NexusSpacing.sm))
                        rules.forEach { rule ->
                            val score = AutomationHealthEngine.healthScore(
                                AutomationHealthEngine.calculateStats(
                                    executions.filter { it.automationId == rule.id }
                                )
                            )
                            StatRow(rule.name, "$score/100 — ${rule.healthStatus}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = NexusSpacing.xs), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}
