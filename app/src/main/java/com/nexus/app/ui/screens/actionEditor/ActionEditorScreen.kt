package com.nexus.app.ui.screens.actionEditor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nexus.app.R
import com.nexus.app.domain.model.ActionPayload
import com.nexus.app.domain.model.ActionType
import com.nexus.app.domain.model.ActionValidator
import com.nexus.app.ui.components.GlassSurface
import com.nexus.app.ui.theme.NexusSpacing

/**
 * Screen for creating or editing an Action.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ActionEditorScreen(
    contextId: String,
    editingActionId: String?,
    viewModel: ActionViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isEditing = editingActionId != null
    val uiState by viewModel.uiState.collectAsState()

    // Form state
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ActionType.OPEN_APP) }
    var payloadPackageName by remember { mutableStateOf("") }
    var payloadUrl by remember { mutableStateOf("") }
    var payloadDelayMs by remember { mutableLongStateOf(1000L) }
    var showError by remember { mutableStateOf<String?>(null) }
    var hasLoaded by remember { mutableStateOf(!isEditing) }

    // Load existing action for editing
    LaunchedEffect(editingActionId) {
        if (editingActionId != null) {
            viewModel.getById(editingActionId) { action ->
                if (action != null) {
                    name = action.name
                    description = action.description
                    selectedType = action.type
                    when (val payload = ActionPayload.fromJson(action.type, action.payload)) {
                        is ActionPayload.OpenApp -> payloadPackageName = payload.packageName
                        is ActionPayload.OpenUrl -> payloadUrl = payload.url
                        is ActionPayload.Delay -> payloadDelayMs = payload.durationMs
                        null -> {}
                    }
                    hasLoaded = true
                }
            }
        }
    }

    // React to save success
    LaunchedEffect(uiState) {
        when (uiState) {
            is ActionUiState.Saved -> {
                viewModel.clearUiState()
                onNavigateBack()
            }
            is ActionUiState.Error -> {
                showError = (uiState as ActionUiState.Error).message
                viewModel.clearUiState()
            }
            else -> {}
        }
    }

    // Build payload string
    fun buildPayload(): String {
        return when (selectedType) {
            ActionType.OPEN_APP -> ActionPayload.OpenApp(payloadPackageName).toJson()
            ActionType.OPEN_URL -> ActionPayload.OpenUrl(payloadUrl).toJson()
            ActionType.DELAY -> ActionPayload.Delay(payloadDelayMs).toJson()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Edit Action" else "New Action",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Go back",
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
        floatingActionButton = {
            val saveDesc = stringResource(R.string.editor_save)
            FloatingActionButton(
                onClick = {
                    val payload = buildPayload()
                    if (isEditing && editingActionId != null) {
                        viewModel.updateAction(editingActionId, name, description, selectedType, payload)
                    } else {
                        viewModel.createAction(contextId, name, description, selectedType, payload)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.semantics { contentDescription = saveDesc },
            ) {
                if (uiState is ActionUiState.Saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = NexusSpacing.screenPadding,
                end = NexusSpacing.screenPadding,
                bottom = NexusSpacing.xxxxl + 80.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(NexusSpacing.sectionGap),
        ) {
            // Error banner
            if (showError != null) {
                item {
                    GlassSurface(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                        borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                    ) {
                        Text(
                            text = showError!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }

            // Action type selector
            item {
                Column {
                    Text(
                        text = "Action Type",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = NexusSpacing.md),
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(NexusSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(NexusSpacing.md),
                    ) {
                        ActionType.entries.forEach { type ->
                            val label = when (type) {
                                ActionType.OPEN_APP -> "Open App"
                                ActionType.OPEN_URL -> "Open URL"
                                ActionType.DELAY -> "Delay"
                            }
                            TypeChip(
                                label = label,
                                selected = type == selectedType,
                                onClick = { selectedType = type },
                            )
                        }
                    }
                }
            }

            // Name
            item {
                Column {
                    Text(
                        text = "Name",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = NexusSpacing.sm),
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            if (it.length <= ActionValidator.MAX_NAME_LENGTH) {
                                name = it
                                showError = null
                            }
                        },
                        placeholder = {
                            Text(when (selectedType) {
                                ActionType.OPEN_APP -> "e.g. Open Termux"
                                ActionType.OPEN_URL -> "e.g. Open Documentation"
                                ActionType.DELAY -> "e.g. Wait before next"
                            })
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        ),
                        supportingText = {
                            Text(
                                text = "${name.length}/${ActionValidator.MAX_NAME_LENGTH}",
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                    )
                }
            }

            // Description
            item {
                Column {
                    Text(
                        text = "Description (optional)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = NexusSpacing.sm),
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = {
                            if (it.length <= ActionValidator.MAX_DESCRIPTION_LENGTH) {
                                description = it
                            }
                        },
                        placeholder = { Text("Brief description") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        ),
                    )
                }
            }

            // Type-specific configuration
            item {
                GlassSurface {
                    Column {
                        Text(
                            text = "Configuration",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = NexusSpacing.md),
                        )

                        when (selectedType) {
                            ActionType.OPEN_APP -> {
                                Text(
                                    text = "Package name",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = NexusSpacing.sm),
                                )
                                OutlinedTextField(
                                    value = payloadPackageName,
                                    onValueChange = {
                                        payloadPackageName = it
                                        showError = null
                                    },
                                    placeholder = { Text("com.termux") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    ),
                                )
                            }
                            ActionType.OPEN_URL -> {
                                Text(
                                    text = "URL",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = NexusSpacing.sm),
                                )
                                OutlinedTextField(
                                    value = payloadUrl,
                                    onValueChange = {
                                        payloadUrl = it
                                        showError = null
                                    },
                                    placeholder = { Text("https://developer.android.com") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    ),
                                )
                            }
                            ActionType.DELAY -> {
                                Text(
                                    text = "Duration (milliseconds)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = NexusSpacing.sm),
                                )
                                OutlinedTextField(
                                    value = payloadDelayMs.toString(),
                                    onValueChange = {
                                        val parsed = it.toLongOrNull()
                                        if (parsed != null) {
                                            payloadDelayMs = parsed
                                            showError = null
                                        }
                                    },
                                    placeholder = { Text("1000") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    ),
                                    supportingText = {
                                        Text(
                                            text = "Max ${ActionValidator.MAX_DELAY_MS / 1000} seconds",
                                            style = MaterialTheme.typography.labelSmall,
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TypeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bgColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    val borderColor = if (selected) MaterialTheme.colorScheme.primary
        else Color.Transparent

    GlassSurface(
        containerColor = bgColor,
        borderColor = borderColor,
        contentPadding = NexusSpacing.md,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
