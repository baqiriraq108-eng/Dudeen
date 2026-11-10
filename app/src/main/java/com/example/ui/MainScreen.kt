package com.example.ui

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Translate
import com.example.data.local.WordHistoryEntity
import com.example.ui.components.HistoryFormatterCache
import com.example.ui.components.HistorySortBottomSheet
import com.example.ui.components.GermanFlagGIcon
import com.example.util.HistorySortOption
import com.example.ui.components.NomenListResultView
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import kotlin.math.roundToInt
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.GrammarResult
import com.example.data.model.GrammarType
import com.example.ui.components.ApiKeyDialog
import com.example.ui.components.ColorBottomSheet
import com.example.ui.components.AdjektivResultView
import com.example.ui.components.ClearHistoryConfirmDialog
import com.example.ui.components.CreateNotebookDialog
import com.example.ui.components.DeclensionResultView
import com.example.ui.components.DeleteHistoryItemConfirmDialog
import com.example.ui.components.FolderSelectionOverlay
import com.example.ui.components.GrammarTablesSheet
import com.example.ui.components.HistorySection
import com.example.ui.components.ImportMultiChoiceDialog
import com.example.ui.components.ImportSingleChoiceDialog
import com.example.ui.components.JsonExportActionDialog
import com.example.ui.components.NotebookManagerDialog
import com.example.ui.components.NotebookSwitcher
import com.example.ui.components.PostImportApiKeyDialog
import com.example.ui.components.PrintExportDialog
import com.example.ui.components.SelectNotebooksExportDialog
import com.example.ui.components.SentenceResultView
import com.example.ui.components.SprechTempoControl
import com.example.ui.components.VerbConjugationResultView
import com.example.ui.components.historySectionItems
import com.example.util.ExportSharingManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import androidx.compose.ui.graphics.Brush
import com.example.ui.theme.GeoBannerGradient
import com.example.ui.theme.GeoCardRibbonColor
import com.example.ui.theme.GeoGlowColor
import com.example.ui.theme.GeoIsBoldTheme
import com.example.ui.theme.GeoBackground
import com.example.ui.theme.GeoBorder
import com.example.ui.theme.GeoOnPrimaryContainer
import com.example.ui.theme.GeoOnSurface
import com.example.ui.theme.GeoOnSurfaceVariant
import com.example.ui.theme.GeoOutline
import com.example.ui.theme.GeoPrimary
import com.example.ui.theme.GeoPrimaryContainer
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.LocalAppLanguage
import com.example.ui.theme.GeoSecondary
import com.example.ui.theme.GeoSurface
import com.example.ui.theme.GeoSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()
    val ttsSpeed by viewModel.ttsSpeedRate.collectAsStateWithLifecycle()
    val currentlySpokenText by viewModel.currentlySpokenText.collectAsStateWithLifecycle()
    val currentlySpokenId by viewModel.currentlySpokenId.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val strings = remember(uiState.appLanguage) { AppStrings.get(uiState.appLanguage) }

    val activeModelInfo = remember(uiState.selectedModel, uiState.availableModels) {
        uiState.availableModels.find { it.id == uiState.selectedModel }
            ?: uiState.availableModels.firstOrNull()
    }

    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var historyItemToDeleteId by remember { mutableStateOf<Long?>(null) }
    var historySearchQuery by rememberSaveable { mutableStateOf("") }
    var isSortSheetOpen by rememberSaveable { mutableStateOf(false) }
    var voiceSearchTarget by rememberSaveable { mutableStateOf<String?>(null) }

    val currentSortOption = uiState.historySortOptions[uiState.selectedHistoryFilter] ?: HistorySortOption()
    val isSortActive = !currentSortOption.isDefault

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                if (voiceSearchTarget == "history") {
                    historySearchQuery = spokenText
                } else {
                    viewModel.onQueryChange(spokenText)
                }
            }
        }
    }

    val launchVoiceSearch: (String) -> Unit = { target ->
        voiceSearchTarget = target
        val (langCode, prompt) = when (uiState.appLanguage.lowercase().take(2)) {
            "de" -> "de-DE" to strings.voiceSearchPromptGerman
            "ar" -> "ar-SA" to "تحدث الآن للبحث..."
            else -> "en-US" to strings.voiceSearchPromptEnglish
        }
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, langCode)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, langCode)
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, langCode)
                putExtra(RecognizerIntent.EXTRA_PROMPT, prompt)
            }
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, strings.voiceSearchNotAvailable, Toast.LENGTH_SHORT).show()
        }
    }

    val saveJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.executeSaveJsonToUri(uri, context)
        }
    }

    val treeFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            val pendingIds = uiState.pendingTreeExportNotebookIds ?: emptyList()
            viewModel.executeTreeExport(uri, context, pendingIds)
        }
    }

    val filteredHistoryList by produceState(
        initialValue = historyList,
        keys = arrayOf(historyList, uiState.selectedHistoryFilter, historySearchQuery, currentSortOption)
    ) {
        withContext(Dispatchers.Default) {
            value = com.example.util.HistoryFilterHelper.filterAndSortHistory(
                items = historyList,
                filterIndex = uiState.selectedHistoryFilter,
                query = historySearchQuery,
                sortOption = currentSortOption
            )
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { errorMsg ->
            snackbarHostState.showSnackbar(errorMsg)
            viewModel.onDismissError()
        }
    }

    // Text direction is strictly English / Latin LTR (No Arabic RTL)
    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Ltr,
        LocalAppLanguage provides uiState.appLanguage,
        com.example.ui.theme.LocalTranslationLanguage provides uiState.translationLanguage
    ) {
        BackHandler(enabled = true) {
            if (isSortSheetOpen) {
                isSortSheetOpen = false
            } else if (showClearHistoryDialog) {
                showClearHistoryDialog = false
            } else if (historyItemToDeleteId != null) {
                historyItemToDeleteId = null
            } else {
                val handled = viewModel.handleBackPress()
                if (!handled) {
                    (context as? Activity)?.moveTaskToBack(true)
                }
            }
        }

        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = GeoBackground,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        NotebookSwitcher(
                            activeNotebook = uiState.activeNotebook,
                            allNotebooks = uiState.allNotebooks,
                            onSelectNotebook = { viewModel.selectNotebook(it) },
                            onCreateNotebookClick = { viewModel.openCreateNotebookDialog() },
                            onManageNotebooksClick = { viewModel.openManageNotebooksDialog() },
                            appLanguage = uiState.appLanguage
                        )
                    },
                    navigationIcon = {
                        val showBack = uiState.currentGrammarResult != null ||
                                       uiState.currentResult != null ||
                                       uiState.selectedHistoryFilter != 0
                        if (showBack) {
                            IconButton(
                                onClick = { viewModel.handleBackPress() },
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("toolbar_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = strings.back,
                                    tint = GeoOnSurface
                                )
                            }
                        }
                    },
                    actions = {
                        // Model Provider Icon (Clean icon without box, border or background)
                        IconButton(
                            onClick = { viewModel.onOpenApiKeyDialog() },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("active_model_pill")
                        ) {
                            when (uiState.selectedProvider) {
                                com.example.data.ModelProvider.GEMINI -> {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "${uiState.selectedProvider.displayName} - ${activeModelInfo?.name ?: uiState.selectedModel}",
                                        modifier = Modifier.size(24.dp),
                                        tint = GeoPrimary
                                    )
                                }
                                com.example.data.ModelProvider.OPENROUTER -> {
                                    Icon(
                                        imageVector = Icons.Default.Hub,
                                        contentDescription = "${uiState.selectedProvider.displayName} - ${activeModelInfo?.name ?: uiState.selectedModel}",
                                        modifier = Modifier.size(24.dp),
                                        tint = GeoPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(2.dp))

                        // Palette Button (Opens separate 100-Color Window BottomSheet, next to AI button)
                        IconButton(
                            onClick = { viewModel.onOpenColorWindow() },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("color_palette_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = strings.colorSchemeButtonDesc,
                                modifier = Modifier.size(24.dp),
                                tint = GeoPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(2.dp))

                        // Grammar Tables Button (Flat German Flag 'G' Button between ✨ and ⬆️)
                        IconButton(
                            onClick = { viewModel.onOpenGrammarTables() },
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("grammar_tables_button")
                        ) {
                            GermanFlagGIcon(
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(2.dp))

                        // Export Button (Icon: 📤 / File Upload, compact ~44-48dp, opens modern export dialog)
                        IconButton(
                            onClick = { viewModel.onOpenExportDialog() },
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("export_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = strings.exportButtonDesc,
                                tint = GeoPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = GeoBackground
                    )
                )
            }
        ) { innerPadding ->
            val lazyListState = rememberLazyListState()
            val coroutineScope = rememberCoroutineScope()

            // Quick Scroll conditions: stable and independent of layout jitter
            val showScrollToTop by remember {
                derivedStateOf {
                    lazyListState.firstVisibleItemIndex >= 4
                }
            }

            val showScrollToBottom by remember {
                derivedStateOf {
                    val total = lazyListState.layoutInfo.totalItemsCount
                    total > 6 && lazyListState.firstVisibleItemIndex < total - 4
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = lazyListState
                ) {
                    item(key = "top_spacer") {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                // Warning Banner if API Key is not configured (in English for settings)
                if (!uiState.hasApiKey) {
                    item(key = "api_key_warning_banner") {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, GeoBorder),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WarningAmber,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column {
                                        Text(
                                            text = strings.apiKeyRequiredTitle,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Text(
                                            text = strings.apiKeyRequiredSubtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                                Button(
                                    onClick = { viewModel.onOpenApiKeyDialog() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("setup_api_key_banner_button")
                                ) {
                                    Text(strings.settingsButtonLabel, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Nomen / Verb / Wörter Toggle
                item(key = "grammar_type_toggle") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val searchTypes = listOf(
                            Triple(GrammarType.NOMEN, strings.tabExpressions, 1.25f),
                            Triple(GrammarType.VERB, strings.tabVerb, 0.75f),
                            Triple(GrammarType.NOMEN_LIST, strings.tabWords, 0.95f),
                            Triple(GrammarType.ADJEKTIV, strings.tabAdjectives, 1.15f)
                        )
                        searchTypes.forEach { (type, label, weightVal) ->
                            val isSelected = uiState.selectedGrammarType == type ||
                                (type == GrammarType.NOMEN && uiState.selectedGrammarType == GrammarType.SENTENCE)
                            val textColor = if (isSelected) Color.White else GeoOnSurfaceVariant
                            val icon = when (type) {
                                GrammarType.NOMEN -> Icons.Default.Description
                                GrammarType.VERB -> Icons.Default.Bolt
                                GrammarType.ADJEKTIV -> Icons.Default.Sell
                                else -> Icons.Default.Translate
                            }

                            Box(
                                modifier = Modifier
                                    .weight(weightVal)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .then(
                                        if (isSelected) {
                                            if (GeoIsBoldTheme) Modifier.background(Brush.horizontalGradient(GeoBannerGradient))
                                            else Modifier.background(GeoPrimary)
                                        } else {
                                            Modifier.background(GeoSurfaceVariant)
                                        }
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) {
                                            if (GeoIsBoldTheme) GeoCardRibbonColor else GeoPrimary
                                        } else GeoBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.onGrammarTypeSelected(type) }
                                    .padding(horizontal = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = textColor,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.5.dp))
                                    AutoShrinkTabLabel(
                                        text = label,
                                        isSelected = isSelected,
                                        textColor = textColor
                                    )
                                }
                            }
                        }
                    }
                }

                // Search Bar Container: match_parent with 16dp side margins
                item(key = "search_bar_container") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Search Input Field Container (match_parent remaining width, inner left/right padding)
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(GeoSurfaceVariant)
                                .border(
                                    if (GeoIsBoldTheme) 1.5.dp else 1.dp,
                                    if (GeoIsBoldTheme) GeoGlowColor.copy(alpha = 0.5f) else GeoBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(start = 8.dp, end = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Leading search icon
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = GeoPrimary,
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(modifier = Modifier.width(5.dp))

                            // Text Field + Placeholder Area
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (uiState.searchQuery.isEmpty()) {
                                    Text(
                                        text = when (uiState.selectedGrammarType) {
                                            GrammarType.VERB -> strings.searchPlaceholderVerbs
                                            GrammarType.NOMEN_LIST -> strings.searchPlaceholderWords
                                            GrammarType.ADJEKTIV -> strings.searchPlaceholderAdjectives
                                            else -> strings.searchPlaceholderExpressions
                                        },
                                        fontSize = 11.sp,
                                        color = GeoOnSurfaceVariant,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Clip
                                    )
                                }

                                BasicTextField(
                                    value = uiState.searchQuery,
                                    onValueChange = { viewModel.onQueryChange(it) },
                                    singleLine = true,
                                    maxLines = 1,
                                    textStyle = TextStyle(
                                        fontSize = 13.sp,
                                        color = GeoOnSurface
                                    ),
                                    cursorBrush = SolidColor(GeoPrimary),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(onSearch = { viewModel.onSearch() }),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("search_input_field")
                                )
                            }

                            // Voice Search Microphone Button inside search field
                            IconButton(
                                onClick = { launchVoiceSearch("main") },
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("main_voice_search_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = strings.voiceSearchDesc,
                                    tint = GeoPrimary,
                                    modifier = Modifier.size(17.dp)
                                )
                            }

                            // Clear Button if search query is not empty
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.onQueryChange("") },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = strings.clearSearchInputDesc,
                                        tint = GeoOutline,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Margin between elements: 8dp between the search field and the search button
                        Spacer(modifier = Modifier.width(8.dp))

                        // Search Submit Button (Changes shape and triggers cancellation when a search is running)
                        Button(
                            onClick = { viewModel.onSearch() },
                            enabled = true,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.isSearching) MaterialTheme.colorScheme.error else (if (GeoIsBoldTheme) GeoCardRibbonColor else GeoPrimary),
                                contentColor = Color.White
                            ),
                            shape = if (uiState.isSearching) RoundedCornerShape(24.dp) else RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("search_submit_button"),
                            contentPadding = PaddingValues(horizontal = if (uiState.isSearching) 12.dp else 14.dp)
                        ) {
                            if (uiState.isSearching) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Stop,
                                        contentDescription = strings.cancelSearchDesc,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = strings.stopButton,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Text(
                                    text = strings.searchButton,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Sprech-Tempo Speech Rate Control Slider Container
                // Placed below search field & search button, centered horizontally, and directly above Suchverlauf & results
                item(key = "speech_speed_control_container") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        SprechTempoControl(
                            speed = ttsSpeed,
                            onSpeedChange = { viewModel.setTtsSpeed(it) }
                        )
                    }
                }

                // Searching Indicator
                if (uiState.isSearching) {
                    item(key = "searching_indicator") {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, GeoBorder),
                            colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.5.dp,
                                    color = GeoPrimary
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = if (uiState.selectedGrammarType == GrammarType.NOMEN_LIST) {
                                        strings.translatingWordsStatus
                                    } else if (uiState.selectedGrammarType == GrammarType.ADJEKTIV) {
                                        strings.analyzingAdjectivesStatus
                                    } else if (uiState.searchQuery.trim().split(Regex("[\\s,;]+")).filter { it.isNotBlank() }.size >= 2) {
                                        strings.translatingSentenceStatus
                                    } else if (uiState.selectedGrammarType == GrammarType.VERB) {
                                        strings.analyzingConjugationStatus
                                    } else {
                                        strings.analyzingDeclensionStatus
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = GeoOnSurface
                                )
                            }
                        }
                    }
                }

                // Grammar Result View: Noun, Verb, or Sentence
                val hasActiveResult = (uiState.currentGrammarResult != null || uiState.currentResult != null) && !uiState.isSearching
                if (hasActiveResult) {
                    item(key = "active_grammar_result") {
                        when (val grammarRes = uiState.currentGrammarResult) {
                            is GrammarResult.Adjektiv -> {
                                AdjektivResultView(
                                    result = grammarRes.result,
                                    onSpeak = { text, id -> viewModel.speakGerman(text, id) },
                                    ttsSpeed = ttsSpeed,
                                    onTtsSpeedChange = { viewModel.setTtsSpeed(it) },
                                    currentlySpokenText = currentlySpokenText,
                                    currentlySpokenId = currentlySpokenId,
                                    currentManualTranslation = uiState.currentManualTranslation,
                                    onSaveManualTranslation = { viewModel.saveManualTranslation(it, GrammarType.ADJEKTIV.displayName, grammarRes.result.word) },
                                    inputQuery = uiState.searchQuery
                                )
                            }
                            is GrammarResult.Verb -> {
                                VerbConjugationResultView(
                                    result = grammarRes.conjugation,
                                    onSpeak = { text, id -> viewModel.speakGerman(text, id) },
                                    ttsSpeed = ttsSpeed,
                                    onTtsSpeedChange = { viewModel.setTtsSpeed(it) },
                                    currentlySpokenText = currentlySpokenText,
                                    currentlySpokenId = currentlySpokenId,
                                    currentManualTranslation = uiState.currentManualTranslation,
                                    onSaveManualTranslation = { viewModel.saveManualTranslation(it, GrammarType.VERB.displayName, grammarRes.conjugation.word) },
                                    inputQuery = uiState.searchQuery
                                )
                            }
                            is GrammarResult.Noun -> {
                                DeclensionResultView(
                                    result = grammarRes.declension,
                                    selectedTab = uiState.selectedNumberTab,
                                    onTabSelected = { viewModel.onSelectTab(it) },
                                    onSpeak = { text, id -> viewModel.speakGerman(text, id) },
                                    onExport = { viewModel.onOpenExportDialog() },
                                    ttsSpeed = ttsSpeed,
                                    onTtsSpeedChange = { viewModel.setTtsSpeed(it) },
                                    currentlySpokenText = currentlySpokenText,
                                    currentlySpokenId = currentlySpokenId,
                                    currentManualTranslation = uiState.currentManualTranslation,
                                    onSaveManualTranslation = { viewModel.saveManualTranslation(it, GrammarType.NOMEN.displayName, grammarRes.declension.word) },
                                    inputQuery = uiState.searchQuery
                                )
                            }
                            is GrammarResult.Sentence -> {
                                SentenceResultView(
                                    result = grammarRes.declension,
                                    onSpeak = { text, id -> viewModel.speakGerman(text, id) },
                                    currentlySpokenText = currentlySpokenText,
                                    currentlySpokenId = currentlySpokenId,
                                    currentManualTranslation = uiState.currentManualTranslation,
                                    onSaveManualTranslation = { viewModel.saveManualTranslation(it, GrammarType.SENTENCE.displayName, grammarRes.declension.word) },
                                    inputQuery = uiState.searchQuery
                                )
                            }
                            is GrammarResult.NomenList -> {
                                NomenListResultView(
                                    result = grammarRes.declension,
                                    onSpeak = { text, id -> viewModel.speakGerman(text, id) },
                                    currentlySpokenText = currentlySpokenText,
                                    currentlySpokenId = currentlySpokenId,
                                    currentManualTranslation = uiState.currentManualTranslation,
                                    onSaveManualTranslation = { viewModel.saveManualTranslation(it, GrammarType.NOMEN_LIST.displayName, grammarRes.declension.word) },
                                    inputQuery = uiState.searchQuery
                                )
                            }
                            null -> {
                                uiState.currentResult?.let { nounRes ->
                                    val isNomenList = uiState.selectedGrammarType == GrammarType.NOMEN_LIST ||
                                        nounRes.gender.equals("Wörter", ignoreCase = true) ||
                                        nounRes.gender.equals("Wort", ignoreCase = true) ||
                                        nounRes.gender.equals("Nomen", ignoreCase = true)
                                    val isSentence = !isNomenList && (nounRes.gender.equals("Satz", ignoreCase = true) ||
                                        nounRes.gender == "–" ||
                                        nounRes.word.trim().split(Regex("[\\s,;]+")).filter { it.isNotBlank() }.size >= 2)
                                    if (isNomenList) {
                                        NomenListResultView(
                                            result = nounRes,
                                            onSpeak = { text, id -> viewModel.speakGerman(text, id) },
                                            currentlySpokenText = currentlySpokenText,
                                            currentlySpokenId = currentlySpokenId,
                                            currentManualTranslation = uiState.currentManualTranslation,
                                            onSaveManualTranslation = { viewModel.saveManualTranslation(it, GrammarType.NOMEN_LIST.displayName, nounRes.word) },
                                            inputQuery = uiState.searchQuery
                                        )
                                    } else if (isSentence) {
                                        SentenceResultView(
                                            result = nounRes,
                                            onSpeak = { text, id -> viewModel.speakGerman(text, id) },
                                            currentlySpokenText = currentlySpokenText,
                                            currentlySpokenId = currentlySpokenId,
                                            currentManualTranslation = uiState.currentManualTranslation,
                                            onSaveManualTranslation = { viewModel.saveManualTranslation(it, GrammarType.SENTENCE.displayName, nounRes.word) },
                                            inputQuery = uiState.searchQuery
                                        )
                                    } else {
                                        DeclensionResultView(
                                            result = nounRes,
                                            selectedTab = uiState.selectedNumberTab,
                                            onTabSelected = { viewModel.onSelectTab(it) },
                                            onSpeak = { text, id -> viewModel.speakGerman(text, id) },
                                            onExport = { viewModel.onOpenExportDialog() },
                                            ttsSpeed = ttsSpeed,
                                            onTtsSpeedChange = { viewModel.setTtsSpeed(it) },
                                            currentlySpokenText = currentlySpokenText,
                                            currentlySpokenId = currentlySpokenId,
                                            currentManualTranslation = uiState.currentManualTranslation,
                                            onSaveManualTranslation = { viewModel.saveManualTranslation(it, GrammarType.NOMEN.displayName, nounRes.word) },
                                            inputQuery = uiState.searchQuery
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item(key = "spacer_after_result") {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                if (!uiState.isSearching) {
                    historySectionItems(
                        historyList = historyList,
                        filteredList = filteredHistoryList,
                        selectedFilterIndex = uiState.selectedHistoryFilter,
                        historySearchQuery = historySearchQuery,
                        onSearchQueryChange = { historySearchQuery = it },
                        onFilterChanged = { viewModel.onSelectHistoryFilter(it) },
                        onSelectWord = { viewModel.onSelectHistoryItem(it) },
                        onDeleteItem = { historyItemToDeleteId = it },
                        onRequestClearAll = { showClearHistoryDialog = true },
                        onOpenSortSheet = { isSortSheetOpen = true },
                        isSortActive = isSortActive,
                        onSpeak = { text, id -> viewModel.speakGerman(text, id) },
                        currentlySpokenText = currentlySpokenText,
                        currentlySpokenId = currentlySpokenId,
                        onVoiceSearchClick = {
                            launchVoiceSearch("history")
                        }
                    )
                }

                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            val totalCount = lazyListState.layoutInfo.totalItemsCount
            val visibleCount = lazyListState.layoutInfo.visibleItemsInfo.size

            // Interactive Draggable Fast Scrollbar (synchronized with touch, completely independent of arrows)
            FastScrollbar(
                lazyListState = lazyListState,
                totalCount = totalCount,
                visibleCount = visibleCount,
                historyList = filteredHistoryList,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .fillMaxHeight(0.48f)
                    .padding(end = 2.dp, bottom = 28.dp)
            )

            // Refined, stable Floating Dock for Quick Scroll navigation
            QuickScrollFloatingDock(
                showScrollToTop = showScrollToTop,
                showScrollToBottom = showScrollToBottom,
                onScrollToTop = {
                    coroutineScope.launch {
                        lazyListState.animateScrollToItem(0)
                    }
                },
                onScrollToBottom = {
                    coroutineScope.launch {
                        val total = lazyListState.layoutInfo.totalItemsCount
                        if (total > 0) {
                            lazyListState.animateScrollToItem(total - 1)
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 24.dp)
            )
        }
    }

    if (showClearHistoryDialog) {
        ClearHistoryConfirmDialog(
            onConfirm = {
                viewModel.onClearHistory()
                showClearHistoryDialog = false
            },
            onDismiss = { showClearHistoryDialog = false }
        )
    }

    if (isSortSheetOpen) {
        HistorySortBottomSheet(
            currentSortOption = currentSortOption,
            selectedCategoryIndex = uiState.selectedHistoryFilter,
            onSortOptionChanged = { newOption ->
                viewModel.onUpdateHistorySort(uiState.selectedHistoryFilter, newOption)
            },
            onDismissRequest = { isSortSheetOpen = false }
        )
    }

    historyItemToDeleteId?.let { id ->
        DeleteHistoryItemConfirmDialog(
            onConfirm = {
                viewModel.onDeleteHistoryItem(id)
                historyItemToDeleteId = null
            },
            onDismiss = { historyItemToDeleteId = null }
        )
    }

    // Settings & AI Models Dialog
    if (uiState.isApiKeyDialogOpen) {
        ApiKeyDialog(
            selectedProvider = uiState.selectedProvider,
            geminiKey = uiState.geminiApiKeyInput,
            openRouterKey = uiState.openRouterApiKeyInput,
            selectedModel = uiState.selectedModel,
            availableModels = uiState.availableModels,
            selectedTheme = uiState.selectedTheme,
            isTestingKey = uiState.isTestingKey,
            testKeyResult = uiState.testKeyResult,
            isRefreshingModels = uiState.isRefreshingModels,
            refreshModelsStatus = uiState.refreshModelsStatus,
            remainingDailyRequests = uiState.remainingDailyRequests,
            totalDailyQuota = uiState.totalDailyQuota,
            savedHistoryCount = historyList.size,
            selectedHistoryFolderName = uiState.selectedHistoryFolderName,
            replaceHistoryOnImport = uiState.replaceHistoryOnImport,
            importStatusMessage = uiState.importStatusMessage,
            onDismiss = { viewModel.onCloseApiKeyDialog() },
            onSelectProvider = { provider -> viewModel.onSelectProvider(provider) },
            onGeminiKeyChange = { key -> viewModel.onGeminiApiKeyInputChange(key) },
            onOpenRouterKeyChange = { key -> viewModel.onOpenRouterApiKeyInputChange(key) },
            onSelectModel = { modelId -> viewModel.onSelectModel(modelId) },
            onRefreshOpenRouterModels = { viewModel.onRefreshOpenRouterModels() },
            onRefreshGeminiModels = { viewModel.onRefreshGeminiModels() },
            onSelectTheme = { theme -> viewModel.onThemeSelected(theme) },
            onTestKey = { viewModel.onTestApiKey() },
            onSaveAll = { viewModel.onSaveApiKey() },
            onClearKey = { viewModel.onClearApiKey() },
            onOpenExport = { viewModel.onOpenExportDialog(fromSettings = true) },
            onClearHistory = { viewModel.onClearHistory() },
            onSelectHistoryFolder = { uri -> viewModel.onSelectHistoryFolder(uri) },
            onImportHistoryFile = { uri -> viewModel.onImportHistoryFile(uri) },
            onReplaceHistoryOnImportChange = { replace -> viewModel.setReplaceHistoryOnImport(replace) },
            onDismissImportStatus = { viewModel.clearImportStatusMessage() },
            activeNotebookName = uiState.activeNotebook?.name ?: "Allgemein",
            onRenameNotebook = { newName ->
                uiState.activeNotebook?.let { viewModel.renameNotebook(it.id, newName) }
            },
            themeMode = uiState.themeMode,
            onThemeModeChange = { mode -> viewModel.onThemeModeSelected(mode) },
            onOpenNotebookManager = { viewModel.onOpenNotebookManagerFromSettings() },
            appLanguage = uiState.appLanguage,
            onAppLanguageSelected = { lang -> viewModel.onAppLanguageSelected(lang) },
            translationLanguage = uiState.translationLanguage,
            onTranslationLanguageSelected = { lang -> viewModel.onTranslationLanguageSelected(lang) }
        )
    }

    // Separate 100-Color Window (Bottom Sheet)
    if (uiState.isColorWindowOpen) {
        ColorBottomSheet(
            currentTheme = uiState.selectedTheme,
            currentThemeMode = uiState.themeMode,
            onDismiss = { viewModel.onCloseColorWindow() },
            onApplyTheme = { theme, mode ->
                viewModel.onApplyColorTheme(theme, mode)
            },
            appLanguage = uiState.appLanguage
        )
    }

    // Create Notebook Dialog
    if (uiState.isCreateNotebookDialogOpen) {
        CreateNotebookDialog(
            onDismiss = { viewModel.closeCreateNotebookDialog() },
            onCreate = { name, themeColorId, themeMode ->
                viewModel.createNotebook(name, themeColorId, themeMode)
            },
            appLanguage = uiState.appLanguage
        )
    }

    // Manage Notebooks Dialog
    if (uiState.isManageNotebooksDialogOpen) {
        NotebookManagerDialog(
            notebooks = uiState.allNotebooks,
            activeNotebookId = uiState.activeNotebook?.id ?: "",
            onDismiss = { viewModel.closeManageNotebooksDialog() },
            onSelectNotebook = { id ->
                viewModel.selectNotebook(id)
                viewModel.closeManageNotebooksDialog()
            },
            onCreateNotebook = {
                viewModel.closeManageNotebooksDialog()
                viewModel.openCreateNotebookDialog()
            },
            onRenameNotebook = { id, newName ->
                viewModel.renameNotebook(id, newName)
            },
            onDeleteNotebook = { id ->
                viewModel.deleteNotebook(id)
            },
            onExportJson = { id ->
                viewModel.prepareSingleNotebookExport(id)
            },
            onImportJson = { uri ->
                viewModel.handleImportJsonUri(uri, context)
            },
            onExportAllCombined = {
                viewModel.prepareAllNotebooksCombinedExport()
            },
            onExportAllSeparate = {
                viewModel.setPendingTreeExport(null)
                treeFolderLauncher.launch(null)
            },
            onExportSelectedClick = {
                viewModel.openSelectNotebooksExportDialog()
            },
            backupFolders = uiState.backupFolders,
            isBackingUp = uiState.isBackingUp,
            onAddBackupFolder = { uri ->
                viewModel.addBackupFolder(uri)
            },
            onSetPrimaryBackupFolder = { uriString ->
                viewModel.setPrimaryBackupFolder(uriString)
            },
            onRemoveBackupFolder = { uriString ->
                viewModel.removeBackupFolder(uriString)
            },
            onBackupNow = {
                viewModel.backupNow { success, message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            },
            appLanguage = uiState.appLanguage
        )
    }

    // JSON Action Dialog (Speichern unter / Teilen)
    uiState.activeJsonExportTarget?.let { target ->
        JsonExportActionDialog(
            fileName = target.fileName,
            title = target.title,
            onSaveToFile = {
                saveJsonLauncher.launch(target.fileName)
            },
            onShare = {
                viewModel.shareActiveJsonExport(context)
            },
            onDismiss = {
                viewModel.dismissJsonExportDialog()
            },
            appLanguage = uiState.appLanguage
        )
    }

    // Select Notebooks Export Dialog (Option 3)
    if (uiState.isSelectNotebooksExportDialogOpen) {
        SelectNotebooksExportDialog(
            notebooks = uiState.allNotebooks,
            onDismiss = { viewModel.closeSelectNotebooksExportDialog() },
            onExportCombined = { selectedIds ->
                viewModel.closeSelectNotebooksExportDialog()
                viewModel.prepareSelectedNotebooksCombinedExport(selectedIds)
            },
            onExportSeparate = { selectedIds ->
                viewModel.closeSelectNotebooksExportDialog()
                viewModel.setPendingTreeExport(selectedIds)
                treeFolderLauncher.launch(null)
            },
            appLanguage = uiState.appLanguage
        )
    }

    // Single Notebook Import Choice Dialog
    uiState.importSingleChoice?.let { singleChoice ->
        ImportSingleChoiceDialog(
            detectedName = singleChoice.detectedName,
            wordCount = singleChoice.words.size,
            onMergeIntoActive = {
                viewModel.executeImportSingleMerge()
            },
            onCreateAsNew = { newName ->
                viewModel.executeImportSingleAsNew(newName)
            },
            onDismiss = {
                viewModel.dismissImportSingleChoice()
            },
            appLanguage = uiState.appLanguage
        )
    }

    // Multi-Notebook Import Choice Dialog
    uiState.importMultiChoice?.let { multiChoice ->
        ImportMultiChoiceDialog(
            notebookNames = multiChoice.notebooksMap.keys.toList(),
            totalWordCount = multiChoice.notebooksMap.values.sumOf { it.size },
            fallbackName = multiChoice.fallbackName,
            onImportAllSeparate = {
                viewModel.executeImportMultiSeparate()
            },
            onImportAsSingle = { name ->
                viewModel.executeImportMultiAsSingle(name)
            },
            onDismiss = {
                viewModel.dismissImportMultiChoice()
            },
            appLanguage = uiState.appLanguage
        )
    }

    // Post-Import API Key Configuration Dialog
    uiState.postImportApiKeyPrompt?.let { prompt ->
        PostImportApiKeyDialog(
            notebookName = prompt.notebookName,
            provider = prompt.provider,
            modelId = prompt.modelId,
            onSaveApiKey = { key ->
                viewModel.savePostImportApiKey(key)
            },
            onOpenFullSettings = {
                viewModel.openSettingsFromPostImport()
            },
            onSkip = {
                viewModel.dismissPostImportApiKeyPrompt()
            },
            appLanguage = uiState.appLanguage
        )
    }

    // Grammar Tables & Pronunciation Sheet
    if (uiState.isGrammarTablesOpen) {
        GrammarTablesSheet(
            onDismiss = { viewModel.onCloseGrammarTables() },
            onSpeak = { text, id -> viewModel.speakGerman(text, id) },
            currentlySpokenId = currentlySpokenId,
            ttsSpeed = ttsSpeed,
            onTtsSpeedChange = { viewModel.setTtsSpeed(it) },
            verbResult = uiState.grammarVerbResult,
            isVerbLoading = uiState.isGrammarVerbLoading,
            verbError = uiState.grammarVerbError,
            onSearchVerb = { viewModel.searchGrammarVerb(it) },
            onClearVerb = { viewModel.clearGrammarVerb() },
            appLanguage = uiState.appLanguage
        )
    }

    // Print & Export Dialog (Unified PDF / Excel)
    if (uiState.isExportDialogOpen) {
        val (nouns, verbs, adjectives) = viewModel.getUnifiedHistoryForExport()
        val initialScope = when (uiState.selectedGrammarType) {
            GrammarType.NOMEN -> com.example.util.DeclensionHistoryExportHelper.ExportScope.NOMEN
            GrammarType.VERB -> com.example.util.DeclensionHistoryExportHelper.ExportScope.VERB
            GrammarType.ADJEKTIV -> com.example.util.DeclensionHistoryExportHelper.ExportScope.ADJEKTIV
            GrammarType.SENTENCE -> com.example.util.DeclensionHistoryExportHelper.ExportScope.NOMEN
            GrammarType.NOMEN_LIST -> com.example.util.DeclensionHistoryExportHelper.ExportScope.NOMEN
        }
        PrintExportDialog(
            currentGrammarResult = uiState.currentGrammarResult,
            nounHistory = nouns,
            verbHistory = verbs,
            adjektivHistory = adjectives,
            initialScope = initialScope,
            notebookName = uiState.activeNotebook?.name ?: "",
            appLanguage = uiState.appLanguage,
            onDismiss = { viewModel.onCloseExportDialog() }
        )
    }

    // Mandatory Folder Selection Overlay (on first install or after clear app data)
    if (uiState.isFolderSelectionRequired) {
        FolderSelectionOverlay(
            onFolderSelected = { uri ->
                viewModel.onSelectHistoryFolder(uri)
            }
        )
    }

    }
}

@Composable
private fun AutoShrinkTabLabel(
    text: String,
    isSelected: Boolean,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    var fontSize by remember(text, isSelected) { mutableStateOf(if (text.length > 8) 10.5.sp else 11.sp) }
    var readyToDraw by remember(text, isSelected) { mutableStateOf(false) }

    Text(
        text = text,
        modifier = modifier.drawWithContent {
            if (readyToDraw) drawContent()
        },
        fontSize = fontSize,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        color = textColor,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Clip,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.hasVisualOverflow && fontSize > 8.sp) {
                fontSize = (fontSize.value - 0.5f).sp
            } else {
                readyToDraw = true
            }
        }
    )
}

@Composable
private fun FastScrollbar(
    lazyListState: LazyListState,
    totalCount: Int,
    visibleCount: Int = 0,
    historyList: List<WordHistoryEntity> = emptyList(),
    modifier: Modifier = Modifier
) {
    if (totalCount <= 5) return

    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    var trackHeightPx by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }

    // Completely fixed thumb height: exactly 48.dp, NEVER fluctuates, stretches, or shrinks!
    val fixedThumbHeightDp = 48.dp
    val fixedThumbHeightPx = with(density) { fixedThumbHeightDp.toPx() }

    val usableTrackPx = (trackHeightPx - fixedThumbHeightPx).coerceAtLeast(1f)

    val listProgress by remember(totalCount) {
        derivedStateOf {
            val total = totalCount
            if (total <= 1) 0f
            else {
                val index = lazyListState.firstVisibleItemIndex
                val maxIndex = (total - 1).coerceAtLeast(1)
                (index.toFloat() / maxIndex.toFloat()).coerceIn(0f, 1f)
            }
        }
    }

    LaunchedEffect(isDragging, listProgress) {
        if (!isDragging) {
            dragProgress = listProgress
        }
    }

    // Safety: ensure dragging state is reset if Composable leaves composition
    DisposableEffect(Unit) {
        onDispose {
            isDragging = false
        }
    }

    val effectiveProgress = if (isDragging) dragProgress else listProgress
    val thumbOffsetPx = usableTrackPx * effectiveProgress
    val thumbOffsetDp = with(density) { thumbOffsetPx.toDp() }

    // Alpha animation: responsive to dragging and scrolling, subtle hint when resting
    val isScrollInProgress = lazyListState.isScrollInProgress
    val targetAlpha = when {
        isDragging -> 1f
        isScrollInProgress -> 0.9f
        else -> 0.22f
    }
    val scrollbarAlpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 200),
        label = "fast_scrollbar_alpha"
    )

    val thumbWidth by animateDpAsState(
        targetValue = if (isDragging) 7.dp else 4.5.dp,
        animationSpec = tween(durationMillis = 150),
        label = "thumb_width"
    )

    val currentTotalCount by rememberUpdatedState(totalCount)
    val currentUsableTrackPx by rememberUpdatedState(usableTrackPx)

    // Outer container: purely a narrow, transparent layout anchor along the right screen edge.
    // Has NO pointerInput on the container so it NEVER intercepts clicks for items or speaker buttons!
    Box(
        modifier = modifier
            .width(26.dp)
            .onSizeChanged { size ->
                trackHeightPx = size.height.toFloat()
            },
        contentAlignment = Alignment.TopEnd
    ) {
        // Track background line (purely visual)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp)
                .fillMaxHeight()
                .width(3.dp)
                .graphicsLayer(alpha = scrollbarAlpha * 0.45f)
                .clip(CircleShape)
                .background(GeoOnSurfaceVariant.copy(alpha = 0.15f))
        )

        // Draggable Thumb Handle:
        // Placed ONLY at thumbOffsetDp with fixedThumbHeightDp.
        // pointerInput is ONLY on this handle, so the rest of the screen is 100% free of touch interception!
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(y = thumbOffsetDp)
                .size(width = 26.dp, height = fixedThumbHeightDp)
                .pointerInput(Unit) {
                    try {
                        detectVerticalDragGestures(
                            onDragStart = {
                                isDragging = true
                            },
                            onDragEnd = { isDragging = false },
                            onDragCancel = { isDragging = false },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                val usable = currentUsableTrackPx
                                if (usable > 0f) {
                                    val currentY = dragProgress * usable
                                    val newY = (currentY + dragAmount).coerceIn(0f, usable)
                                    val newProgress = (newY / usable).coerceIn(0f, 1f)
                                    dragProgress = newProgress
                                    val total = currentTotalCount
                                    if (total > 0) {
                                        val targetItem = (newProgress * (total - 1)).roundToInt().coerceIn(0, total - 1)
                                        coroutineScope.launch {
                                            lazyListState.scrollToItem(targetItem)
                                        }
                                    }
                                }
                            }
                        )
                    } finally {
                        // GUARANTEED to execute on release, cancel, or coroutine cancellation!
                        isDragging = false
                    }
                },
            contentAlignment = Alignment.CenterEnd
        ) {
            // Visual Thumb Pill
            Box(
                modifier = Modifier
                    .padding(end = 3.dp)
                    .size(width = thumbWidth, height = fixedThumbHeightDp)
                    .graphicsLayer(alpha = scrollbarAlpha)
                    .shadow(elevation = if (isDragging) 4.dp else 1.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(if (isDragging) GeoPrimary else GeoPrimary.copy(alpha = 0.85f))
            )
        }

        // Horizontal floating date badge pill:
        // Appears ONLY while the user is actively grabbing/dragging the thumb!
        // Immediately disappears when released.
        if (isDragging) {
            val itemDateText = if (historyList.isNotEmpty()) {
                val idx = (effectiveProgress * (historyList.size - 1)).roundToInt().coerceIn(0, historyList.size - 1)
                HistoryFormatterCache.formatShortDate(historyList[idx].timestamp)
            } else ""

            if (itemDateText.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-32).dp, y = (thumbOffsetDp + (fixedThumbHeightDp - 28.dp) / 2).coerceAtLeast(0.dp))
                        .wrapContentSize(unbounded = true)
                        .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (GeoIsBoldTheme) GeoSurfaceVariant else MaterialTheme.colorScheme.surface)
                        .border(1.dp, GeoPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = itemDateText,
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GeoPrimary
                        ),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickScrollFloatingDock(
    showScrollToTop: Boolean,
    showScrollToBottom: Boolean,
    onScrollToTop: () -> Unit,
    onScrollToBottom: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isVisible = showScrollToTop || showScrollToBottom

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.85f, animationSpec = tween(200)),
        exit = fadeOut(tween(150)) + scaleOut(targetScale = 0.85f, animationSpec = tween(150)),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = if (GeoIsBoldTheme) GeoSurfaceVariant else MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, if (GeoIsBoldTheme) GeoCardRibbonColor else GeoPrimary.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier.padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (showScrollToTop) {
                    IconButton(
                        onClick = onScrollToTop,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("scroll_to_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Nach ganz oben scrollen",
                            tint = if (GeoIsBoldTheme) GeoPrimary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                if (showScrollToTop && showScrollToBottom) {
                    HorizontalDivider(
                        modifier = Modifier.width(24.dp),
                        thickness = 1.dp,
                        color = GeoOnSurfaceVariant.copy(alpha = 0.2f)
                    )
                }

                if (showScrollToBottom) {
                    IconButton(
                        onClick = onScrollToBottom,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("scroll_to_bottom_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Nach ganz unten scrollen",
                            tint = if (GeoIsBoldTheme) GeoPrimary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

