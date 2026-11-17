package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.data.AiModelInfo
import com.example.data.ModelProvider
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.LocalAppLanguage
import com.example.ui.theme.AppThemePackage

/**
 * AI & Language Settings Bottom Sheet styled identically to the Export Bottom Sheet:
 * - Full-width bottom sheet (`topStart = 20.dp, topEnd = 20.dp`) with top drag handle & close `×`
 * - 3-tier tonal M3 hierarchy (`surfaceContainerLow` -> `surfaceContainer` -> `surfaceContainerHigh`)
 * - Export-matched `40.dp` dropdown rows & `12.dp` `DropdownMenu` popups for Provider, Model (searchable & filterable), App Language, and Translation Language
 * - Clean API Key field, Test Key action, Daily Quota summary, and `48.dp` bottom Cancel / Confirm buttons
 */
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
    var providerDropdownExpanded by remember { mutableStateOf(false) }
    var modelDropdownExpanded by remember { mutableStateOf(false) }
    var appLanguageDropdownExpanded by remember { mutableStateOf(false) }
    var translationLanguageDropdownExpanded by remember { mutableStateOf(false) }
    var modelSearchQuery by remember { mutableStateOf("") }
    var showFreeOnly by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val strings = remember(appLanguage) { AppStrings.get(appLanguage) }

    // 3 Distinct Dark/Light Surface Levels matching Export Bottom Sheet
    val sheetSurface = MaterialTheme.colorScheme.surfaceContainerLow
    val cardSurface = MaterialTheme.colorScheme.surfaceContainer
    val controlSurface = MaterialTheme.colorScheme.surfaceContainerHigh
    val accentColor = MaterialTheme.colorScheme.primary
    val primaryText = MaterialTheme.colorScheme.onSurface
    val mutedText = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f)
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)

    CompositionLocalProvider(
        LocalLayoutDirection provides strings.language.layoutDirection,
        LocalAppLanguage provides appLanguage
    ) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = true
            )
        ) {
            val localView = LocalView.current
            val density = LocalDensity.current
            val dialogWindow = (localView.parent as? DialogWindowProvider)?.window
            SideEffect {
                dialogWindow?.let { win ->
                    win.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    win.setBackgroundDrawableResource(android.R.color.transparent)
                    win.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                    win.setDimAmount(0.84f)
                    @Suppress("DEPRECATION")
                    win.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                }
            }

            // Fallback read of host Activity bottom system insets
            val dialogNavBottomPx = WindowInsets.navigationBars.getBottom(density)
            val activityNavBottomPx = remember(context, localView) {
                val targetView = context.findHostActivity()?.window?.decorView ?: localView.rootView
                val rootInsets = ViewCompat.getRootWindowInsets(targetView)
                if (rootInsets != null) {
                    val navBottom = rootInsets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
                    val gestureBottom = rootInsets.getInsets(WindowInsetsCompat.Type.mandatorySystemGestures()).bottom
                    maxOf(navBottom, gestureBottom)
                } else {
                    0
                }
            }
            val unconsumedNavBottomDp = with(density) {
                (activityNavBottomPx - dialogNavBottomPx).coerceAtLeast(0).toDp()
            }

            BackHandler(enabled = true) {
                onDismiss()
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .pointerInput(Unit) {
                        detectTapGestures {
                            focusManager.clearFocus()
                            onDismiss()
                        }
                    }
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding(),
                contentAlignment = Alignment.BottomCenter
            ) {
                val sheetMaxHeight = (maxHeight - 16.dp).coerceAtLeast(220.dp)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = sheetMaxHeight)
                        .pointerInput(Unit) {
                            detectTapGestures { focusManager.clearFocus() }
                        }
                        .testTag("api_key_dialog"),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    color = sheetSurface,
                    tonalElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = unconsumedNavBottomDp)
                    ) {
                        // Top Drag Handle + Sheet Header
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp, bottom = 10.dp, start = 16.dp, end = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(mutedText.copy(alpha = 0.45f))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = strings.settingsTitle,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = primaryText,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 2.dp)
                                )
                                IconButton(
                                    onClick = onDismiss,
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("settings_back_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = strings.close,
                                        tint = mutedText,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = dividerColor, thickness = 1.dp)

                        // Scrollable Body matching Export's rhythm & card nesting
                        Column(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 32.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // 1. Combined AI Configuration Card (Provider -> Model -> API Key & Connection Test)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(cardSurface)
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // A. AI Provider Dropdown
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SheetSectionLabel(
                                        icon = Icons.Default.AutoAwesome,
                                        title = strings.aiProviderTitle,
                                        accentColor = accentColor,
                                        primaryText = primaryText
                                    )

                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        val providerIcon = providerIconFor(selectedProvider)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(40.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(controlSurface)
                                                .clickable { providerDropdownExpanded = true }
                                                .padding(horizontal = 12.dp)
                                                .testTag("provider_dropdown_button"),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = providerIcon,
                                                contentDescription = null,
                                                tint = accentColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = selectedProvider.displayName,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = primaryText,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = null,
                                                tint = mutedText,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        DropdownMenu(
                                            expanded = providerDropdownExpanded,
                                            onDismissRequest = { providerDropdownExpanded = false },
                                            shape = RoundedCornerShape(12.dp),
                                            containerColor = cardSurface,
                                            border = BorderStroke(1.dp, dividerColor)
                                        ) {
                                            ModelProvider.entries.forEach { provider ->
                                                val isSelected = provider == selectedProvider
                                                DropdownMenuItem(
                                                    modifier = Modifier.testTag("provider_${provider.id}_button"),
                                                    text = {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = providerIconFor(provider),
                                                                contentDescription = null,
                                                                tint = if (isSelected) accentColor else mutedText,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                            Text(
                                                                text = provider.displayName,
                                                                fontSize = 13.sp,
                                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                                color = if (isSelected) accentColor else primaryText,
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                            if (isSelected) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Check,
                                                                    contentDescription = null,
                                                                    tint = accentColor,
                                                                    modifier = Modifier.size(16.dp)
                                                                )
                                                            }
                                                        }
                                                    },
                                                    onClick = {
                                                        onSelectProvider(provider)
                                                        providerDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                HorizontalDivider(color = dividerColor, thickness = 1.dp)

                                // B. Active Model Searchable & Filterable Dropdown
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                    SheetSectionLabel(
                                        icon = providerIconFor(selectedProvider),
                                        title = strings.activeModelTitle,
                                        accentColor = accentColor,
                                        primaryText = primaryText
                                    )

                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .defaultMinSize(minHeight = 44.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(controlSurface)
                                                .clickable { modelDropdownExpanded = true }
                                                .padding(horizontal = 12.dp, vertical = 9.dp)
                                                .testTag("model_dropdown_button"),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = providerIconFor(selectedProvider),
                                                contentDescription = null,
                                                tint = accentColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = activeModelInfo?.name ?: effectiveId,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = primaryText,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = null,
                                                tint = mutedText,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        // Searchable & Filterable Model Popup matching Export's menu aesthetic
                                        DropdownMenu(
                                            expanded = modelDropdownExpanded,
                                            onDismissRequest = {
                                                modelDropdownExpanded = false
                                                modelSearchQuery = ""
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            containerColor = cardSurface,
                                            border = BorderStroke(1.dp, dividerColor),
                                            modifier = Modifier
                                                .widthIn(min = 270.dp, max = 380.dp)
                                                .fillMaxWidth(0.90f)
                                                .heightIn(max = 360.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                // Search Box matching Export input surface
                                                BasicTextField(
                                                    value = modelSearchQuery,
                                                    onValueChange = { modelSearchQuery = it },
                                                    singleLine = true,
                                                    textStyle = TextStyle(
                                                        fontSize = 13.sp,
                                                        color = primaryText
                                                    ),
                                                    cursorBrush = SolidColor(accentColor),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .testTag("model_search_input"),
                                                    decorationBox = { innerTextField ->
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .height(38.dp)
                                                                .clip(RoundedCornerShape(10.dp))
                                                                .background(controlSurface)
                                                                .padding(horizontal = 10.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Search,
                                                                contentDescription = null,
                                                                tint = mutedText,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                            Box(
                                                                modifier = Modifier.weight(1f),
                                                                contentAlignment = Alignment.CenterStart
                                                            ) {
                                                                if (modelSearchQuery.isEmpty()) {
                                                                    Text(
                                                                        text = strings.searchPlaceholder,
                                                                        fontSize = 12.sp,
                                                                        color = mutedText,
                                                                        maxLines = 1,
                                                                        overflow = TextOverflow.Ellipsis
                                                                    )
                                                                }
                                                                innerTextField()
                                                            }
                                                            if (modelSearchQuery.isNotEmpty()) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Clear,
                                                                    contentDescription = strings.clearKeyButton,
                                                                    tint = mutedText,
                                                                    modifier = Modifier
                                                                        .size(16.dp)
                                                                        .clickable { modelSearchQuery = "" }
                                                                )
                                                            }
                                                        }
                                                    }
                                                )

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

                                                // Clean Filter Toggle Pill + Count
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    val filterLabel = when (selectedProvider) {
                                                        ModelProvider.GEMINI -> strings.filterFlashOnly
                                                        ModelProvider.OPENROUTER -> strings.filterFreeOnly
                                                    }
                                                    Box(modifier = Modifier.weight(1f)) {
                                                        Row(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(
                                                                    if (showFreeOnly) accentColor.copy(alpha = 0.14f) else controlSurface
                                                                )
                                                                .border(
                                                                    width = 1.dp,
                                                                    color = if (showFreeOnly) accentColor.copy(alpha = 0.45f) else Color.Transparent,
                                                                    shape = RoundedCornerShape(8.dp)
                                                                )
                                                                .clickable { showFreeOnly = !showFreeOnly }
                                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                                                .testTag("model_filter_chip"),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.FilterList,
                                                                contentDescription = null,
                                                                tint = if (showFreeOnly) accentColor else mutedText,
                                                                modifier = Modifier.size(13.dp)
                                                            )
                                                            Text(
                                                                text = filterLabel,
                                                                fontSize = 11.sp,
                                                                fontWeight = if (showFreeOnly) FontWeight.SemiBold else FontWeight.Medium,
                                                                color = if (showFreeOnly) accentColor else mutedText,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }
                                                    }

                                                    Text(
                                                        text = "${filteredModels.size} / ${availableModels.size}",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = mutedText,
                                                        maxLines = 1
                                                    )
                                                }
                                            }

                                            HorizontalDivider(color = dividerColor, thickness = 1.dp)

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
                                                            text = if (selectedProvider == ModelProvider.GEMINI)
                                                                strings.loadingModelsStatus
                                                            else
                                                                strings.noModelsFound,
                                                            fontSize = 12.sp,
                                                            color = mutedText
                                                        )
                                                    },
                                                    onClick = {}
                                                )
                                            } else if (filteredModels.isEmpty()) {
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = strings.noModelsFound,
                                                            fontSize = 12.sp,
                                                            color = mutedText
                                                        )
                                                    },
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
                                                                        Icon(
                                                                            imageVector = providerIconFor(model.provider),
                                                                            contentDescription = null,
                                                                            tint = if (isSelected) accentColor else mutedText,
                                                                            modifier = Modifier.size(14.dp)
                                                                        )
                                                                        Text(
                                                                            text = model.name,
                                                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                                                            fontSize = 12.5.sp,
                                                                            color = if (isSelected) accentColor else primaryText,
                                                                            maxLines = 2,
                                                                            overflow = TextOverflow.Ellipsis,
                                                                            modifier = Modifier.weight(1f, fill = false)
                                                                        )
                                                                        if (model.isFreeTier) {
                                                                            FreeBadge(accentColor = accentColor)
                                                                        }
                                                                    }
                                                                    Text(
                                                                        text = model.id,
                                                                        fontSize = 10.5.sp,
                                                                        fontFamily = FontFamily.Monospace,
                                                                        color = mutedText,
                                                                        maxLines = 1,
                                                                        overflow = TextOverflow.Ellipsis
                                                                    )
                                                                    if (model.functionalDescription.isNotBlank()) {
                                                                        Text(
                                                                            text = model.functionalDescription,
                                                                            fontSize = 11.sp,
                                                                            color = mutedText,
                                                                            maxLines = 2,
                                                                            overflow = TextOverflow.Ellipsis,
                                                                            modifier = Modifier.padding(top = 1.dp)
                                                                        )
                                                                    }
                                                                }
                                                                if (isSelected) {
                                                                    Spacer(modifier = Modifier.width(6.dp))
                                                                    Icon(
                                                                        imageVector = Icons.Default.Check,
                                                                        contentDescription = null,
                                                                        tint = accentColor,
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

                                    // Dedicated uncrowded container for full Model ID & Description
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(controlSurface.copy(alpha = 0.5f))
                                            .padding(horizontal = 10.dp, vertical = 7.dp)
                                            .testTag("active_model_details_box"),
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Text(
                                            text = effectiveId,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = primaryText.copy(alpha = 0.85f),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (activeModelInfo != null && activeModelInfo.functionalDescription.isNotBlank()) {
                                            Text(
                                                text = activeModelInfo.functionalDescription,
                                                fontSize = 11.sp,
                                                color = mutedText,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(color = dividerColor, thickness = 1.dp)

                                // C. API Key Input + Test Action + Result Banner
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
                                val testButtonTag = when (selectedProvider) {
                                    ModelProvider.GEMINI -> "test_gemini_key_button"
                                    ModelProvider.OPENROUTER -> "test_openrouter_key_button"
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SheetSectionLabel(
                                        icon = Icons.Default.Key,
                                        title = keyLabel,
                                        accentColor = accentColor,
                                        primaryText = primaryText
                                    )

                                    // Borderless Export-style input box with focus outline only on active focus
                                    var isKeyFocused by remember { mutableStateOf(false) }
                                    BasicTextField(
                                        value = currentKey,
                                        onValueChange = onKeyChange,
                                        singleLine = true,
                                        visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Password,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(onDone = {
                                            focusManager.clearFocus()
                                            onSaveAll()
                                        }),
                                        textStyle = TextStyle(
                                            fontSize = 13.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = primaryText
                                        ),
                                        cursorBrush = SolidColor(accentColor),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onFocusChanged { isKeyFocused = it.isFocused }
                                            .testTag(inputTestTag),
                                        decorationBox = { innerTextField ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(44.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(controlSurface)
                                                    .border(
                                                        width = if (isKeyFocused) 1.5.dp else 1.dp,
                                                        color = if (isKeyFocused) accentColor else Color.Transparent,
                                                        shape = RoundedCornerShape(10.dp)
                                                    )
                                                    .padding(start = 12.dp, end = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier.weight(1f),
                                                    contentAlignment = Alignment.CenterStart
                                                ) {
                                                    if (currentKey.isEmpty()) {
                                                        Text(
                                                            text = placeholderText,
                                                            fontSize = 13.sp,
                                                            fontFamily = FontFamily.Monospace,
                                                            color = mutedText.copy(alpha = 0.6f),
                                                            maxLines = 1
                                                        )
                                                    }
                                                    innerTextField()
                                                }
                                                IconButton(
                                                    onClick = { isKeyVisible = !isKeyVisible },
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                        contentDescription = strings.showHideKeyDesc,
                                                        tint = mutedText,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                if (currentKey.isNotEmpty()) {
                                                    IconButton(
                                                        onClick = onClearKey,
                                                        modifier = Modifier.size(36.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Clear,
                                                            contentDescription = strings.clearKeyButton,
                                                            tint = mutedText,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    )

                                    // Dedicated uncrowded caption line for source hint
                                    Text(
                                        text = keyHint,
                                        fontSize = 11.sp,
                                        color = mutedText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 2.dp)
                                            .testTag("api_key_hint_text")
                                    )

                                    // Test Connection Button in Export's controlSurface pill
                                    val isTestEnabled = !isTestingKey && currentKey.trim().isNotEmpty()
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(controlSurface)
                                            .clickable(enabled = isTestEnabled) { onTestKey() }
                                            .padding(horizontal = 12.dp)
                                            .testTag(testButtonTag),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        if (isTestingKey) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(15.dp),
                                                strokeWidth = 2.dp,
                                                color = accentColor
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = strings.testingKeyStatus,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = primaryText,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        } else {
                                            Text(
                                                text = strings.testKeyButton,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (isTestEnabled) accentColor else mutedText.copy(alpha = 0.55f),
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    // Result Banner
                                    AnimatedVisibility(visible = testKeyResult != null) {
                                        testKeyResult?.let { (isSuccess, message) ->
                                            val pillBg = if (isSuccess) {
                                                Color(0xFF2E7D32).copy(alpha = 0.14f)
                                            } else {
                                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.72f)
                                            }
                                            val pillFg = if (isSuccess) {
                                                Color(0xFF2E7D32)
                                            } else {
                                                MaterialTheme.colorScheme.onErrorContainer
                                            }
                                            val icon = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(pillBg)
                                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = null,
                                                    tint = pillFg,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = message,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = pillFg,
                                                    maxLines = 3,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // 2. Daily Quota Card (Uncrowded 2-word label + compact counter pill)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(cardSurface)
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = strings.remainingUsageLabel,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = primaryText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(accentColor.copy(alpha = 0.12f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                        .testTag("daily_quota_badge")
                                ) {
                                    Text(
                                        text = strings.remainingDailyRequests(remainingDailyRequests, totalDailyQuota),
                                        style = TextStyle(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = accentColor,
                                            fontFeatureSettings = "tnum"
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }

                            // 3. Combined Language Settings Card (App Language & Translation Language dropdowns)
                            if (onAppLanguageSelected != null || onLanguageChange != null || onTranslationLanguageSelected != null) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(cardSurface)
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    if (onAppLanguageSelected != null || onLanguageChange != null) {
                                        val selectedAppLang = remember(appLanguage) { AppLanguage.fromCode(appLanguage) }
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            SheetSectionLabel(
                                                icon = Icons.Default.Language,
                                                title = strings.languageSectionTitle,
                                                accentColor = accentColor,
                                                primaryText = primaryText
                                            )

                                            Box(modifier = Modifier.fillMaxWidth()) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(40.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(controlSurface)
                                                        .clickable { appLanguageDropdownExpanded = true }
                                                        .padding(horizontal = 12.dp)
                                                        .testTag("app_lang_dropdown_button"),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = selectedAppLang.nativeLabel,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = primaryText,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Icon(
                                                        imageVector = Icons.Default.KeyboardArrowDown,
                                                        contentDescription = null,
                                                        tint = mutedText,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }

                                                DropdownMenu(
                                                    expanded = appLanguageDropdownExpanded,
                                                    onDismissRequest = { appLanguageDropdownExpanded = false },
                                                    shape = RoundedCornerShape(12.dp),
                                                    containerColor = cardSurface,
                                                    border = BorderStroke(1.dp, dividerColor)
                                                ) {
                                                    AppLanguage.entries.forEach { lang ->
                                                        val isSelected = appLanguage.equals(lang.code, ignoreCase = true)
                                                        DropdownMenuItem(
                                                            modifier = Modifier.testTag("app_lang_${lang.code}_button"),
                                                            text = {
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                                ) {
                                                                    Text(
                                                                        text = lang.nativeLabel,
                                                                        fontSize = 13.sp,
                                                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                                        color = if (isSelected) accentColor else primaryText
                                                                    )
                                                                    if (isSelected) {
                                                                        Spacer(modifier = Modifier.width(12.dp))
                                                                        Icon(
                                                                            imageVector = Icons.Default.Check,
                                                                            contentDescription = null,
                                                                            tint = accentColor,
                                                                            modifier = Modifier.size(16.dp)
                                                                        )
                                                                    }
                                                                }
                                                            },
                                                            onClick = {
                                                                onAppLanguageSelected?.invoke(lang.code)
                                                                onLanguageChange?.invoke(lang.code)
                                                                appLanguageDropdownExpanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if ((onAppLanguageSelected != null || onLanguageChange != null) && onTranslationLanguageSelected != null) {
                                        HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                    }

                                    if (onTranslationLanguageSelected != null) {
                                        val translationOptions = listOf(
                                            "en" to strings.translationEnglishLabel,
                                            "ar" to strings.translationArabicLabel
                                        )
                                        val selectedTransLabel = translationOptions
                                            .firstOrNull { it.first.equals(translationLanguage, ignoreCase = true) }
                                            ?.second ?: strings.translationEnglishLabel

                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            SheetSectionLabel(
                                                icon = Icons.Default.Translate,
                                                title = strings.translationLanguageSectionTitle,
                                                accentColor = accentColor,
                                                primaryText = primaryText
                                            )

                                            Box(modifier = Modifier.fillMaxWidth()) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(40.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(controlSurface)
                                                        .clickable { translationLanguageDropdownExpanded = true }
                                                        .padding(horizontal = 12.dp)
                                                        .testTag("trans_lang_dropdown_button"),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = selectedTransLabel,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = primaryText,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Icon(
                                                        imageVector = Icons.Default.KeyboardArrowDown,
                                                        contentDescription = null,
                                                        tint = mutedText,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }

                                                DropdownMenu(
                                                    expanded = translationLanguageDropdownExpanded,
                                                    onDismissRequest = { translationLanguageDropdownExpanded = false },
                                                    shape = RoundedCornerShape(12.dp),
                                                    containerColor = cardSurface,
                                                    border = BorderStroke(1.dp, dividerColor)
                                                ) {
                                                    translationOptions.forEach { (code, label) ->
                                                        val isSelected = translationLanguage.equals(code, ignoreCase = true)
                                                        DropdownMenuItem(
                                                            modifier = Modifier.testTag("trans_lang_${code}_button"),
                                                            text = {
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                                ) {
                                                                    Text(
                                                                        text = label,
                                                                        fontSize = 13.sp,
                                                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                                        color = if (isSelected) accentColor else primaryText
                                                                    )
                                                                    if (isSelected) {
                                                                        Spacer(modifier = Modifier.width(12.dp))
                                                                        Icon(
                                                                            imageVector = Icons.Default.Check,
                                                                            contentDescription = null,
                                                                            tint = accentColor,
                                                                            modifier = Modifier.size(16.dp)
                                                                        )
                                                                    }
                                                                }
                                                            },
                                                            onClick = {
                                                                onTranslationLanguageSelected(code)
                                                                translationLanguageDropdownExpanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(color = dividerColor, thickness = 1.dp)

                            // 4. Bottom Action Buttons (Equal 48.dp height, 10.dp shape inside scroll flow matching Export)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = onDismiss,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("close_api_key_dialog"),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = controlSurface,
                                        contentColor = primaryText
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        tint = mutedText,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    AutoSizingText(
                                        text = strings.cancel,
                                        maxFontSize = 13.sp,
                                        minFontSize = 9.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = primaryText
                                    )
                                }

                                Button(
                                    onClick = onSaveAll,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("save_api_key_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = accentColor,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    AutoSizingText(
                                        text = strings.confirm,
                                        maxFontSize = 13.sp,
                                        minFontSize = 9.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetSectionLabel(
    icon: ImageVector,
    title: String,
    accentColor: Color,
    primaryText: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = primaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun providerIconFor(provider: ModelProvider): ImageVector = when (provider) {
    ModelProvider.GEMINI -> Icons.Default.AutoAwesome
    ModelProvider.OPENROUTER -> Icons.Default.Hub
}

@Composable
private fun FreeBadge(accentColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(accentColor.copy(alpha = 0.14f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "Free",
            color = accentColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private tailrec fun Context.findHostActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findHostActivity()
    else -> null
}
