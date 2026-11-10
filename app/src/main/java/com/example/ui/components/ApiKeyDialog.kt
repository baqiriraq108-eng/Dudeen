package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.AiModelInfo
import com.example.data.ModelProvider
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.AppThemePackage
import com.example.ui.theme.GeoBorder
import com.example.ui.theme.GeoOnPrimary
import com.example.ui.theme.GeoOnSurface
import com.example.ui.theme.GeoOnSurfaceVariant
import com.example.ui.theme.GeoPrimary
import com.example.ui.theme.GeoPrimaryContainer
import com.example.ui.theme.GeoSurface
import com.example.ui.theme.GeoSurfaceVariant

/**
 * Reorganized AI Window.
 * Dedicated solely to AI settings with the specified order:
 * 1. AI provider selection (At the top)
 * 2. Model selection (Below provider)
 * 3. API key field (Below model)
 * 4. Test key button (Below field)
 * 5. Remaining usage (Below test button)
 * 6. Save and Close buttons (At the bottom)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiKeyDialog(
    selectedProvider: ModelProvider,
    geminiKey: String,
    openRouterKey: String,
    selectedModel: String,
    availableModels: List<AiModelInfo>,
    selectedTheme: AppThemePackage = AppThemePackage.SCHIEFER,
    isTestingKey: Boolean,
    testKeyResult: Pair<Boolean, String>?,
    isRefreshingModels: Boolean = false,
    refreshModelsStatus: String? = null,
    remainingDailyRequests: Int,
    totalDailyQuota: Int,
    savedHistoryCount: Int = 0,
    selectedHistoryFolderName: String = "Kein Ordner ausgewählt",
    replaceHistoryOnImport: Boolean = false,
    importStatusMessage: String? = null,
    onDismiss: () -> Unit,
    onSelectProvider: (ModelProvider) -> Unit,
    onGeminiKeyChange: (String) -> Unit,
    onOpenRouterKeyChange: (String) -> Unit,
    onSelectModel: (String) -> Unit,
    onRefreshOpenRouterModels: () -> Unit = {},
    onRefreshGeminiModels: () -> Unit = {},
    onSelectTheme: ((AppThemePackage) -> Unit)? = null,
    onTestKey: () -> Unit,
    onSaveAll: () -> Unit,
    onClearKey: () -> Unit,
    onOpenExport: () -> Unit = {},
    onClearHistory: () -> Unit = {},
    onSelectHistoryFolder: ((android.net.Uri) -> Unit)? = null,
    onImportHistoryFile: ((android.net.Uri) -> Unit)? = null,
    onReplaceHistoryOnImportChange: ((Boolean) -> Unit)? = null,
    onDismissImportStatus: (() -> Unit)? = null,
    activeNotebookName: String = "Allgemein",
    onRenameNotebook: ((String) -> Unit)? = null,
    themeMode: String = "auto",
    onThemeModeChange: ((String) -> Unit)? = null,
    onOpenNotebookManager: (() -> Unit)? = null,
    appLanguage: String = "de",
    onAppLanguageSelected: ((String) -> Unit)? = null,
    onLanguageChange: ((String) -> Unit)? = null,
    translationLanguage: String = "en",
    onTranslationLanguageSelected: ((String) -> Unit)? = null
) {
    var isKeyVisible by remember { mutableStateOf(false) }
    var modelDropdownExpanded by remember { mutableStateOf(false) }
    var modelSearchQuery by remember { mutableStateOf("") }
    var showFreeOnly by remember { mutableStateOf(true) }
    val strings = remember(appLanguage) { AppStrings.get(appLanguage) }

    CompositionLocalProvider(LocalLayoutDirection provides strings.language.layoutDirection) {
        BackHandler(enabled = true) {
            onDismiss()
        }

        AlertDialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            ),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 680.dp)
                .testTag("api_key_dialog"),
            shape = RoundedCornerShape(16.dp),
            containerColor = GeoSurface,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.settingsBackDesc,
                            tint = GeoOnSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(GeoPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = GeoPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = strings.settingsTitle,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = GeoOnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = strings.aiSettingsSubtitle,
                            fontSize = 10.sp,
                            color = GeoOnSurfaceVariant
                        )
                    }
                }
            },
            text = {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // ==============================================================
                    // 1. AI Provider Selection (At the Top)
                    // ==============================================================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GeoBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = strings.aiProviderTitle,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GeoOnSurface
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ModelProvider.entries.forEach { provider ->
                                    val isSelected = provider == selectedProvider
                                    OutlinedButton(
                                        onClick = { onSelectProvider(provider) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                            .testTag(if (provider == selectedProvider) "provider_dropdown_button" else "provider_${provider.id}_button"),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) GeoPrimary else GeoBorder
                                        ),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = if (isSelected) GeoPrimaryContainer.copy(alpha = 0.5f) else GeoSurface,
                                            contentColor = if (isSelected) GeoPrimary else GeoOnSurface
                                        ),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            when (provider) {
                                                ModelProvider.GEMINI -> {
                                                    Icon(
                                                        imageVector = Icons.Default.AutoAwesome,
                                                        contentDescription = null,
                                                        tint = if (isSelected) GeoPrimary else GeoOnSurfaceVariant,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                                ModelProvider.OPENROUTER -> {
                                                    Icon(
                                                        imageVector = Icons.Default.Hub,
                                                        contentDescription = null,
                                                        tint = if (isSelected) GeoPrimary else GeoOnSurfaceVariant,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = provider.displayName,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ==============================================================
                    // 2. Model Selection (Below Provider)
                    // ==============================================================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GeoBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val activeModelInfo = availableModels.find { it.id == selectedModel }
                            val effectiveId = if (selectedModel.isNotBlank()) {
                                selectedModel
                            } else if (availableModels.isNotEmpty()) {
                                availableModels.first().id
                            } else {
                                when (selectedProvider) {
                                    ModelProvider.OPENROUTER -> "openrouter/free"
                                    else -> strings.noModelsFound
                                }
                            }
                            val isFreeRouter = selectedProvider == ModelProvider.OPENROUTER &&
                                    (effectiveId == "openrouter/free" || activeModelInfo?.tier?.contains("Free", ignoreCase = true) == true)

                            // Header row with Label, Provider hint, and action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = strings.activeModelTitle,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GeoOnSurface
                                    )
                                    Text(
                                        text = if (isFreeRouter) "(Free Router)" else "(${selectedProvider.displayName})",
                                        fontSize = 10.sp,
                                        color = GeoOnSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val onRefresh = when (selectedProvider) {
                                        ModelProvider.GEMINI -> onRefreshGeminiModels
                                        ModelProvider.OPENROUTER -> onRefreshOpenRouterModels
                                    }
                                    IconButton(
                                        onClick = onRefresh,
                                        enabled = !isRefreshingModels,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .testTag("refresh_models_button")
                                    ) {
                                        if (isRefreshingModels) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(12.dp),
                                                strokeWidth = 2.dp,
                                                color = GeoPrimary
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = strings.refreshModelsButton,
                                                tint = GeoPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    // Button to open model picker
                                    OutlinedButton(
                                        onClick = { modelDropdownExpanded = true },
                                        modifier = Modifier
                                            .height(28.dp)
                                            .testTag("model_dropdown_button"),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, GeoBorder),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = GeoSurface,
                                            contentColor = GeoPrimary
                                        ),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                    ) {
                                        Text(text = "${strings.selectModelButton} ▼", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }

                            // Active Model ID and Provider Icon
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                when (selectedProvider) {
                                    ModelProvider.GEMINI -> {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = GeoPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    ModelProvider.OPENROUTER -> {
                                        Icon(
                                            imageVector = Icons.Default.Hub,
                                            contentDescription = null,
                                            tint = GeoPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = effectiveId,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GeoPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (activeModelInfo != null && activeModelInfo.functionalDescription.isNotBlank()) {
                                Text(
                                    text = activeModelInfo.functionalDescription,
                                    fontSize = 11.sp,
                                    color = GeoOnSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Status message if models refreshed
                            refreshModelsStatus?.let { status ->
                                Text(
                                    text = status,
                                    fontSize = 10.sp,
                                    color = GeoPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // DropdownMenu for model selection
                            DropdownMenu(
                                expanded = modelDropdownExpanded,
                                onDismissRequest = {
                                    modelDropdownExpanded = false
                                    modelSearchQuery = ""
                                },
                                modifier = Modifier
                                    .fillMaxWidth(0.90f)
                                    .heightIn(max = 360.dp)
                                    .background(GeoSurface)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    OutlinedTextField(
                                        value = modelSearchQuery,
                                        onValueChange = { modelSearchQuery = it },
                                        placeholder = { Text(strings.searchPlaceholder, fontSize = 11.sp) },
                                        singleLine = true,
                                        leadingIcon = {
                                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(14.dp))
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(6.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = GeoSurfaceVariant,
                                            unfocusedContainerColor = GeoSurfaceVariant
                                        )
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        FilterChip(
                                            selected = showFreeOnly,
                                            onClick = { showFreeOnly = !showFreeOnly },
                                            label = {
                                                Text(
                                                    when (selectedProvider) {
                                                        ModelProvider.GEMINI -> strings.filterFlashOnly
                                                        ModelProvider.OPENROUTER -> strings.filterFreeOnly
                                                    },
                                                    fontSize = 10.sp
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(12.dp))
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = GeoPrimaryContainer,
                                                selectedLabelColor = GeoPrimary
                                            )
                                        )
                                        Text(
                                            text = "${availableModels.size} ${strings.words}",
                                            fontSize = 10.sp,
                                            color = GeoOnSurfaceVariant
                                        )
                                    }
                                }

                                val filteredModels = availableModels.filter { model ->
                                    val matchesSearch = modelSearchQuery.isBlank() ||
                                            model.name.contains(modelSearchQuery, ignoreCase = true) ||
                                            model.id.contains(modelSearchQuery, ignoreCase = true)
                                    val matchesFree = when {
                                        selectedProvider == ModelProvider.OPENROUTER && showFreeOnly -> model.isFreeTier
                                        selectedProvider == ModelProvider.GEMINI && showFreeOnly -> model.id.contains("flash", ignoreCase = true)
                                        else -> true
                                    }
                                    matchesSearch && matchesFree
                                }

                                if (availableModels.isEmpty()) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                if (selectedProvider == ModelProvider.GEMINI)
                                                    strings.loadingModelsStatus
                                                else
                                                    strings.noModelsFound,
                                                fontSize = 11.sp,
                                                color = GeoOnSurfaceVariant
                                            )
                                        },
                                        onClick = {}
                                    )
                                } else if (filteredModels.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text(strings.noModelsFound, fontSize = 11.sp, color = GeoOnSurfaceVariant) },
                                        onClick = {}
                                    )
                                } else {
                                    filteredModels.forEach { model ->
                                        val isSelected = model.id == selectedModel
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            when (model.provider) {
                                                                ModelProvider.GEMINI -> {
                                                                    Icon(
                                                                        imageVector = Icons.Default.AutoAwesome,
                                                                        contentDescription = null,
                                                                        tint = if (isSelected) GeoPrimary else GeoOnSurfaceVariant,
                                                                        modifier = Modifier.size(14.dp)
                                                                    )
                                                                }
                                                                ModelProvider.OPENROUTER -> {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Hub,
                                                                        contentDescription = null,
                                                                        tint = if (isSelected) GeoPrimary else GeoOnSurfaceVariant,
                                                                        modifier = Modifier.size(14.dp)
                                                                    )
                                                                }
                                                            }
                                                            Text(
                                                                text = model.name,
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                                fontSize = 12.sp,
                                                                color = GeoOnSurface,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                            if (model.isFreeTier) {
                                                                FreeBadge()
                                                            }
                                                        }
                                                        Text(
                                                            text = model.id,
                                                            fontSize = 10.sp,
                                                            fontFamily = FontFamily.Monospace,
                                                            color = GeoOnSurfaceVariant,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        if (model.functionalDescription.isNotBlank()) {
                                                            Text(
                                                                text = model.functionalDescription,
                                                                fontSize = 11.sp,
                                                                color = GeoOnSurfaceVariant,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis,
                                                                modifier = Modifier.padding(top = 1.dp)
                                                            )
                                                        }
                                                    }
                                                    if (isSelected) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = null,
                                                            tint = GeoPrimary,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            },
                                            onClick = {
                                                onSelectModel(model.id)
                                                modelDropdownExpanded = false
                                                modelSearchQuery = ""
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ==============================================================
                    // 3. API Key Field (Below Model)
                    // ==============================================================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GeoBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val keyLabel = when (selectedProvider) {
                                ModelProvider.GEMINI -> strings.geminiKeyLabel
                                ModelProvider.OPENROUTER -> strings.openRouterKeyLabel
                            }
                            val keyHint = when (selectedProvider) {
                                ModelProvider.GEMINI -> strings.geminiKeyHint
                                ModelProvider.OPENROUTER -> strings.openRouterKeyHint
                            }
                            val currentKey = when (selectedProvider) {
                                ModelProvider.GEMINI -> geminiKey
                                ModelProvider.OPENROUTER -> openRouterKey
                            }
                            val onKeyChange = when (selectedProvider) {
                                ModelProvider.GEMINI -> onGeminiKeyChange
                                ModelProvider.OPENROUTER -> onOpenRouterKeyChange
                            }
                            val inputTestTag = when (selectedProvider) {
                                ModelProvider.GEMINI -> "gemini_key_input"
                                ModelProvider.OPENROUTER -> "openrouter_key_input"
                            }
                            val placeholderText = when (selectedProvider) {
                                ModelProvider.GEMINI -> "AIzaSy..."
                                ModelProvider.OPENROUTER -> "sk-or-..."
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = keyLabel,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GeoOnSurface
                                )
                                Text(
                                    text = keyHint,
                                    fontSize = 10.sp,
                                    color = GeoOnSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            OutlinedTextField(
                                value = currentKey,
                                onValueChange = onKeyChange,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag(inputTestTag),
                                placeholder = {
                                    Text(
                                        text = placeholderText,
                                        fontSize = 12.sp,
                                        color = GeoOnSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                },
                                singleLine = true,
                                visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = { onSaveAll() }),
                                trailingIcon = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { isKeyVisible = !isKeyVisible },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = if (isKeyVisible) "Hide" else "Show",
                                                tint = GeoOnSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        if (currentKey.isNotEmpty()) {
                                            IconButton(
                                                onClick = onClearKey,
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Clear,
                                                    contentDescription = "Clear",
                                                    tint = GeoOnSurfaceVariant,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GeoPrimary,
                                    unfocusedBorderColor = GeoBorder,
                                    focusedContainerColor = GeoSurface,
                                    unfocusedContainerColor = GeoSurface
                                )
                            )

                            // ==============================================================
                            // 4. Test Key Button (Below Field)
                            // ==============================================================
                            val testButtonTag = when (selectedProvider) {
                                ModelProvider.GEMINI -> "test_gemini_key_button"
                                ModelProvider.OPENROUTER -> "test_openrouter_key_button"
                            }
                            OutlinedButton(
                                onClick = onTestKey,
                                enabled = !isTestingKey && currentKey.trim().isNotEmpty(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                                    .testTag(testButtonTag),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, GeoPrimary),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = GeoSurface,
                                    contentColor = GeoPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                if (isTestingKey) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = GeoPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = strings.testingKeyStatus, fontSize = 11.sp)
                                } else {
                                    Text(text = strings.testKeyButton, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Test Key Result Banner
                            AnimatedVisibility(visible = testKeyResult != null) {
                                testKeyResult?.let { (isSuccess, message) ->
                                    val bgColor = if (isSuccess) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                    val textColor = if (isSuccess) Color(0xFF2E7D32) else Color(0xFFC62828)
                                    val icon = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(bgColor)
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = textColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = message,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = textColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ==============================================================
                    // 5. Remaining Usage (Below Test Button)
                    // ==============================================================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GeoBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.remainingUsageLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GeoOnSurface
                            )
                            Text(
                                text = strings.remainingDailyRequests(remainingDailyRequests, totalDailyQuota),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GeoPrimary
                            )
                        }
                    }

                    // ==============================================================
                    // 6. Application Language & Translation Language Selection
                    // ==============================================================
                    if (onAppLanguageSelected != null || onLanguageChange != null || onTranslationLanguageSelected != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, GeoBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = strings.languageSectionTitle,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GeoOnSurface
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    AppLanguage.entries.forEach { lang ->
                                        val isLangSelected = appLanguage.equals(lang.code, ignoreCase = true)
                                        OutlinedButton(
                                            onClick = {
                                                onAppLanguageSelected?.invoke(lang.code)
                                                onLanguageChange?.invoke(lang.code)
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(34.dp)
                                                .testTag("app_lang_${lang.code}_button"),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(
                                                width = if (isLangSelected) 1.5.dp else 1.dp,
                                                color = if (isLangSelected) GeoPrimary else GeoBorder
                                            ),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = if (isLangSelected) GeoPrimaryContainer.copy(alpha = 0.5f) else GeoSurface,
                                                contentColor = if (isLangSelected) GeoPrimary else GeoOnSurface
                                            ),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = lang.nativeLabel,
                                                fontSize = 11.sp,
                                                fontWeight = if (isLangSelected) FontWeight.Bold else FontWeight.Normal,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                if (onTranslationLanguageSelected != null) {
                                    Text(
                                        text = strings.translationLanguageSectionTitle,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = GeoOnSurfaceVariant
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf(
                                            "en" to strings.translationEnglishLabel,
                                            "ar" to strings.translationArabicLabel
                                        ).forEach { (code, label) ->
                                            val isTransSelected = translationLanguage.equals(code, ignoreCase = true)
                                            OutlinedButton(
                                                onClick = { onTranslationLanguageSelected(code) },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(32.dp)
                                                    .testTag("trans_lang_${code}_button"),
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(
                                                    width = if (isTransSelected) 1.5.dp else 1.dp,
                                                    color = if (isTransSelected) GeoPrimary else GeoBorder
                                                ),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    containerColor = if (isTransSelected) GeoPrimaryContainer.copy(alpha = 0.5f) else GeoSurface,
                                                    contentColor = if (isTransSelected) GeoPrimary else GeoOnSurface
                                                ),
                                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = label,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isTransSelected) FontWeight.Bold else FontWeight.Normal,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            // ==============================================================
            // 6. Save and Close Buttons (At the Bottom)
            // ==============================================================
            confirmButton = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("close_api_key_dialog"),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, GeoBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GeoOnSurface)
                    ) {
                        Text(
                            text = strings.close,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = onSaveAll,
                        modifier = Modifier
                            .weight(1.2f)
                            .height(40.dp)
                            .testTag("save_api_key_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GeoPrimary,
                            contentColor = GeoOnPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = strings.save,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            dismissButton = null
        )
    }
}

@Composable
private fun FreeBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF2E7D32))
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        Text(
            text = "Free",
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
