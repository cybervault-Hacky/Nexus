package com.nexus.app.ui.screens.contextEditor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nexus.app.R
import com.nexus.app.ui.components.GlassSurface
import com.nexus.app.ui.components.IconOption
import com.nexus.app.ui.components.availableAccentColors
import com.nexus.app.ui.components.availableContextIcons
import com.nexus.app.ui.screens.contexts.ContextUiState
import com.nexus.app.ui.screens.contexts.ContextViewModel
import com.nexus.app.ui.theme.NexusSpacing

/**
 * Unified screen for creating and editing a Context.
 *
 * When [editingContextId] is non-null the screen loads the existing
 * context and operates in edit mode; otherwise it creates a new one.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ContextEditorScreen(
    viewModel: ContextViewModel,
    editingContextId: String? = null,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isEditing = editingContextId != null
    val allContexts by viewModel.contexts.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // Find the existing context when editing
    val existingContext = remember(allContexts, editingContextId) {
        editingContextId?.let { id -> allContexts.find { it.id == id } }
    }

    // Local form state
    var name by remember(existingContext) { mutableStateOf(existingContext?.name ?: "") }
    var description by remember(existingContext) { mutableStateOf(existingContext?.description ?: "") }
    var selectedIcon by remember(existingContext) { mutableStateOf(existingContext?.iconId ?: "grid") }
    var selectedColor by remember(existingContext) { mutableLongStateOf(existingContext?.accentColor ?: 0xFF6366F1) }
    var showError by remember { mutableStateOf<String?>(null) }
    var hasLoaded by remember { mutableStateOf(existingContext != null || !isEditing) }

    // Wait for context to load when editing
    LaunchedEffect(existingContext, isEditing) {
        if (isEditing && existingContext != null) {
            name = existingContext.name
            description = existingContext.description
            selectedIcon = existingContext.iconId
            selectedColor = existingContext.accentColor
            hasLoaded = true
        }
    }

    // React to save success
    LaunchedEffect(uiState) {
        when (uiState) {
            is ContextUiState.Saved -> {
                viewModel.clearUiState()
                onNavigateBack()
            }
            is ContextUiState.Error -> {
                showError = (uiState as ContextUiState.Error).message
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
                title = {
                    Text(
                        text = if (isEditing) "Edit Context" else "New Context",
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
            val saveDescription = stringResource(R.string.editor_save)
            FloatingActionButton(
                onClick = {
                    val validation = ContextViewModel.validateInput(name.trim(), description.trim())
                    if (validation != null) {
                        showError = validation
                    } else if (isEditing && editingContextId != null) {
                        viewModel.updateContext(editingContextId, name, description, selectedIcon, selectedColor)
                    } else {
                        viewModel.createContext(name, description, selectedIcon, selectedColor)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.semantics { contentDescription = saveDescription },
            ) {
                if (uiState is ContextUiState.Saving) {
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
            // ── Error banner ─────────────────────────────────
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

            // ── Name field ───────────────────────────────────
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
                            if (it.length <= ContextViewModel.MAX_NAME_LENGTH) {
                                name = it
                                showError = null
                            }
                        },
                        placeholder = { Text("e.g. Coding, Study, Travel") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        ),
                        supportingText = {
                            Text(
                                text = "${name.length}/${ContextViewModel.MAX_NAME_LENGTH}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                    )
                }
            }

            // ── Description field ────────────────────────────
            item {
                Column {
                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = NexusSpacing.sm),
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = {
                            if (it.length <= ContextViewModel.MAX_DESCRIPTION_LENGTH) {
                                description = it
                                showError = null
                            }
                        },
                        placeholder = { Text("Brief description of this context") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        ),
                        supportingText = {
                            Text(
                                text = "${description.length}/${ContextViewModel.MAX_DESCRIPTION_LENGTH}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                    )
                }
            }

            // ── Icon picker ──────────────────────────────────
            item {
                Column {
                    Text(
                        text = "Icon",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = NexusSpacing.md),
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(NexusSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(NexusSpacing.md),
                    ) {
                        availableContextIcons.forEach { option ->
                            IconPickerItem(
                                option = option,
                                selected = option.id == selectedIcon,
                                accentColor = Color(selectedColor),
                                onClick = { selectedIcon = option.id },
                            )
                        }
                    }
                }
            }

            // ── Colour picker ────────────────────────────────
            item {
                Column {
                    Text(
                        text = "Accent Color",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = NexusSpacing.md),
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(NexusSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(NexusSpacing.md),
                    ) {
                        availableAccentColors.forEach { colorValue ->
                            ColorPickerItem(
                                color = Color(colorValue),
                                selected = colorValue == selectedColor,
                                onClick = { selectedColor = colorValue },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IconPickerItem(
    option: IconOption,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) accentColor else Color.Transparent
    val bgColor = if (selected) accentColor.copy(alpha = 0.12f) else
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

    GlassSurface(
        containerColor = bgColor,
        borderColor = borderColor,
        contentPadding = NexusSpacing.md,
        modifier = Modifier
            .size(60.dp)
            .clickable(onClick = onClick)
            .semantics { contentDescription = option.label },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = option.icon,
                contentDescription = null,
                tint = if (selected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun ColorPickerItem(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .then(
                if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onBackground, CircleShape)
                else Modifier
            )
            .clickable(onClick = onClick)
            .semantics { contentDescription = "Accent color" },
    )
}
