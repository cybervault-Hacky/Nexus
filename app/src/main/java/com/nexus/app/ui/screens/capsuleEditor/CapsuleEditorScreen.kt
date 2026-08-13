package com.nexus.app.ui.screens.capsuleEditor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nexus.app.ui.components.GlassSurface
import com.nexus.app.ui.screens.capsules.CapsuleUiState
import com.nexus.app.ui.screens.capsules.CapsuleViewModel
import com.nexus.app.ui.theme.NexusSpacing

/**
 * Screen for capturing a new Capsule from an existing Context.
 * Shows a name and description editor with sensible defaults.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapsuleEditorScreen(
    contextId: String,
    contextName: String,
    viewModel: CapsuleViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    var name by remember { mutableStateOf("$contextName Snapshot") }
    var description by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState) {
        when (uiState) {
            is CapsuleUiState.Saved -> {
                viewModel.clearUiState()
                onNavigateBack()
            }
            is CapsuleUiState.Error -> {
                showError = (uiState as CapsuleUiState.Error).message
                viewModel.clearUiState()
            }
            else -> {}
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Capture Capsule", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Go back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.captureFromContext(contextId, name, description)
                },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.semantics { contentDescription = "Capture capsule" },
            ) {
                if (uiState is CapsuleUiState.Saving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Rounded.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(
                start = NexusSpacing.screenPadding,
                end = NexusSpacing.screenPadding,
                bottom = NexusSpacing.xxxxl + 80.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(NexusSpacing.sectionGap),
        ) {
            if (showError != null) {
                item {
                    GlassSurface(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                        borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                    ) {
                        Text(showError!!, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            item {
                GlassSurface {
                    Column {
                        Text("Create Capsule", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(NexusSpacing.sm))
                        Text(
                            "This will capture the current state of \"$contextName\" — including its apps and actions — into an immutable snapshot.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item {
                Column {
                    Text("Capsule Name", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = NexusSpacing.sm))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { if (it.length <= 60) { name = it; showError = null } },
                        placeholder = { Text("e.g. Friday Coding Setup") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline),
                        supportingText = { Text("${name.length}/60", style = MaterialTheme.typography.labelSmall) },
                    )
                }
            }

            item {
                Column {
                    Text("Description (optional)", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = NexusSpacing.sm))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { if (it.length <= 200) description = it },
                        placeholder = { Text("Brief description of this snapshot") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline),
                        supportingText = { Text("${description.length}/200", style = MaterialTheme.typography.labelSmall) },
                    )
                }
            }
        }
    }
}
