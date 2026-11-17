package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.data.model.WordDeclensionResult
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.LocalAppLanguage
import com.example.ui.theme.GeoBorder
import com.example.util.CustomColorManager
import com.example.util.DeclensionHistoryExportHelper
import com.example.util.ExportSharingManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Export Bottom Sheet:
 * - Drag handle + clean header ("Exportieren" + Close) on a fully darkened backdrop
 * - 3 distinct dark surface levels (sheet -> section cards -> interactive controls)
 * - Consistent 12.dp / 10.dp corner geometry, 1 accent color, borders only on selected/focused states
 * - Combined font card (Text & Überschriften) separated by soft divider + uniform previews
 * - Table color card with 4-tier strip marking Kopfzeile + hex matching the header swatch
 * - Proportional filename field with middle-ellipsis display and attached fixed extension suffix
 * - Fixed bottom action bar (Abbrechen | Teilen | Bestätigen) clear of system gesture bar
 */
@Composable
fun PrintExportDialog(
    currentWord: WordDeclensionResult? = null,
    historyWords: List<WordDeclensionResult> = emptyList(),
    currentGrammarResult: com.example.data.model.GrammarResult? = null,
    nounHistory: List<WordDeclensionResult> = emptyList(),
    verbHistory: List<com.example.data.model.VerbConjugationResult> = emptyList(),
    adjektivHistory: List<com.example.data.model.AdjektivResult> = emptyList(),
    initialScope: DeclensionHistoryExportHelper.ExportScope = DeclensionHistoryExportHelper.ExportScope.ALLE,
    notebookName: String = "",
    registerName: String = notebookName,
    appLanguage: String = "",
    onDismiss: () -> Unit
) {
    val effectiveNotebookName = notebookName.ifBlank { registerName }
    val effectiveLang = appLanguage.ifBlank { LocalAppLanguage.current }
    val strings = remember(effectiveLang) { AppStrings.get(effectiveLang) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var selectedScope by remember { mutableStateOf(initialScope) }
    var selectedFormat by remember { mutableStateOf(ExportSharingManager.ExportFormat.XLSX) }
    var hasUserEditedFileName by remember { mutableStateOf(false) }
    val defaultBaseName = remember(effectiveNotebookName, selectedFormat) {
        ExportSharingManager.buildDefaultExportBaseName(selectedFormat, effectiveNotebookName)
    }
    var fileNameInput by remember(effectiveNotebookName) {
        mutableStateOf(
            ExportSharingManager.buildDefaultExportBaseName(selectedFormat, effectiveNotebookName)
        )
    }

    // Paper size selection for PDF (A3, A4, A5, Letter) and orientation (Landscape/Portrait)
    var selectedBaseSize by remember { mutableStateOf("A4") }
    var isLandscape by remember { mutableStateOf(false) }

    val selectedPaperSize = remember(selectedBaseSize, isLandscape) {
        when (selectedBaseSize) {
            "A3" -> if (isLandscape) DeclensionHistoryExportHelper.PaperSizeOption.A3_LANDSCAPE else DeclensionHistoryExportHelper.PaperSizeOption.A3_PORTRAIT
            "A5" -> if (isLandscape) DeclensionHistoryExportHelper.PaperSizeOption.A5_LANDSCAPE else DeclensionHistoryExportHelper.PaperSizeOption.A5_PORTRAIT
            "Letter" -> if (isLandscape) DeclensionHistoryExportHelper.PaperSizeOption.LETTER_LANDSCAPE else DeclensionHistoryExportHelper.PaperSizeOption.LETTER_PORTRAIT
            else -> if (isLandscape) DeclensionHistoryExportHelper.PaperSizeOption.A4_LANDSCAPE else DeclensionHistoryExportHelper.PaperSizeOption.A4_PORTRAIT
        }
    }

    // Typography customization: Body Text
    var bodyFontFamily by remember { mutableStateOf("Arial") }
    var bodyFontSize by remember { mutableStateOf(12f) }
    var bodyFontColorHex by remember {
        mutableStateOf(CustomColorManager.getExportBodyFontColor(context))
    }

    // Typography customization: Headers
    var headerFontFamily by remember { mutableStateOf("Arial") }
    var headerFontSize by remember { mutableStateOf(14f) }
    var headerFontColorHex by remember {
        mutableStateOf(CustomColorManager.getExportHeaderFontColor(context))
    }

    // Dedicated Export Table Color (4-Tier Hierarchical System: Calm Mode vs Actual Vivid Mode)
    var exportTableSeedColorHex by remember {
        mutableStateOf(CustomColorManager.getExportTableSeedColor(context))
    }
    var isTableColorVivid by remember {
        mutableStateOf(CustomColorManager.getExportTableColorVividMode(context))
    }

    // Unified Color Wheel target ("body", "header", or "table") & font size dialog target
    var activeColorTarget by remember { mutableStateOf<String?>(null) }
    var activeFontSizeTarget by remember { mutableStateOf<String?>(null) } // "body" or "header"

    LaunchedEffect(Unit) {
        DeclensionHistoryExportHelper.initFonts(context)
    }

    val currentTypography = remember(
        bodyFontSize, bodyFontFamily, bodyFontColorHex,
        headerFontSize, headerFontFamily, headerFontColorHex,
        exportTableSeedColorHex, isTableColorVivid
    ) {
        DeclensionHistoryExportHelper.ExportTypographyOptions(
            bodyFontSize = bodyFontSize,
            bodyFontFamily = bodyFontFamily,
            bodyFontColorHex = bodyFontColorHex,
            headerFontSize = headerFontSize,
            headerFontFamily = headerFontFamily,
            headerFontColorHex = headerFontColorHex,
            tableSeedColorHex = exportTableSeedColorHex,
            isTableColorVivid = isTableColorVivid
        )
    }

    // Resolve Nouns to export
    val effectiveNouns = remember(nounHistory, historyWords, currentWord, currentGrammarResult) {
        if (nounHistory.isNotEmpty()) {
            nounHistory
        } else if (historyWords.isNotEmpty()) {
            historyWords
        } else if (currentGrammarResult is com.example.data.model.GrammarResult.Noun) {
            listOf(currentGrammarResult.declension)
        } else if (currentWord != null) {
            listOf(currentWord)
        } else {
            emptyList()
        }
    }

    // Resolve Verbs to export
    val effectiveVerbs = remember(verbHistory, currentGrammarResult) {
        if (verbHistory.isNotEmpty()) {
            verbHistory
        } else if (currentGrammarResult is com.example.data.model.GrammarResult.Verb) {
            listOf(currentGrammarResult.conjugation)
        } else {
            emptyList()
        }
    }

    // Resolve Adjectives to export
    val effectiveAdjektive = remember(adjektivHistory, currentGrammarResult) {
        if (adjektivHistory.isNotEmpty()) {
            adjektivHistory
        } else if (currentGrammarResult is com.example.data.model.GrammarResult.Adjektiv) {
            listOf(currentGrammarResult.result)
        } else {
            emptyList()
        }
    }

    // Launcher for creating document at user's chosen folder location
    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(selectedFormat.mimeType)
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { os ->
                            ExportSharingManager.writeUnifiedToStream(
                                context = context,
                                nouns = effectiveNouns,
                                verbs = effectiveVerbs,
                                adjectives = effectiveAdjektive,
                                scope = selectedScope,
                                format = selectedFormat,
                                outputStream = os,
                                customBaseName = ExportSharingManager.sanitizeFileName(fileNameInput),
                                paperSize = selectedPaperSize,
                                typography = currentTypography,
                                notebookName = effectiveNotebookName
                            )
                        }
                    }
                    Toast.makeText(context, strings.fileSavedToast, Toast.LENGTH_SHORT).show()
                    onDismiss()
                } catch (e: Exception) {
                    Toast.makeText(context, "Fehler: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // 3 Distinct Dark/Light Surface Levels & Contrast-Safe Tokens
    val sheetSurface = MaterialTheme.colorScheme.surfaceContainerLow
    val cardSurface = MaterialTheme.colorScheme.surfaceContainer
    val controlSurface = MaterialTheme.colorScheme.surfaceContainerHigh
    val accentColor = MaterialTheme.colorScheme.primary
    val primaryText = MaterialTheme.colorScheme.onSurface
    val mutedText = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f)
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)

    CompositionLocalProvider(LocalAppLanguage provides effectiveLang) {
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

            // Fallback read of the host Activity's navigation/gesture bar inset when Dialog does not receive bottom insets
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

            // Full-bleed tap backdrop kept strictly inside safe status/navigation/IME bounds
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
                        .testTag("export_dialog_surface"),
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
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = strings.exportDialogTitle,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = primaryText,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(start = 2.dp)
                                )
                                IconButton(
                                    onClick = onDismiss,
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("dismiss_export_dialog")
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

                        // Scrollable Content (Includes all sections, filename field, and uniform bottom action buttons without a visible scrollbar)
                        Column(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 32.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // 1. Scope Selector
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = strings.exportScopeLabel,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = mutedText,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                ExportSegmentedRow(
                                    items = listOf(
                                        ExportSegmentItem(
                                            value = DeclensionHistoryExportHelper.ExportScope.NOMEN,
                                            label = strings.tabExpressions,
                                            testTag = "scope_tab_nomen"
                                        ),
                                        ExportSegmentItem(
                                            value = DeclensionHistoryExportHelper.ExportScope.VERB,
                                            label = strings.tabVerb,
                                            testTag = "scope_tab_verb"
                                        ),
                                        ExportSegmentItem(
                                            value = DeclensionHistoryExportHelper.ExportScope.ADJEKTIV,
                                            label = strings.tabAdjectives,
                                            testTag = "scope_tab_adjektiv"
                                        ),
                                        ExportSegmentItem(
                                            value = DeclensionHistoryExportHelper.ExportScope.ALLE,
                                            label = strings.all,
                                            testTag = "scope_tab_alle"
                                        )
                                    ),
                                    selectedValue = selectedScope,
                                    onSelected = { selectedScope = it },
                                    trackColor = controlSurface,
                                    accentColor = accentColor,
                                    primaryText = primaryText,
                                    mutedText = mutedText
                                )
                            }

                            // 2. Format Selector (Excel vs PDF)
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = strings.exportFormatSectionLabel,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = mutedText,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                ExportSegmentedRow(
                                    items = listOf(
                                        ExportSegmentItem(
                                            value = ExportSharingManager.ExportFormat.XLSX,
                                            label = "Excel (.xlsx)",
                                            icon = Icons.Default.TableChart,
                                            testTag = "format_excel_tab"
                                        ),
                                        ExportSegmentItem(
                                            value = ExportSharingManager.ExportFormat.PDF,
                                            label = "PDF",
                                            icon = Icons.Default.Description,
                                            testTag = "format_pdf_tab"
                                        )
                                    ),
                                    selectedValue = selectedFormat,
                                    onSelected = { newFormat ->
                                        selectedFormat = newFormat
                                        if (!hasUserEditedFileName) {
                                            fileNameInput = ExportSharingManager.buildDefaultExportBaseName(
                                                newFormat,
                                                effectiveNotebookName
                                            )
                                        }
                                    },
                                    trackColor = controlSurface,
                                    accentColor = accentColor,
                                    primaryText = primaryText,
                                    mutedText = mutedText
                                )
                            }

                            // Paper Size & Orientation Selector (Visible for PDF format)
                            if (selectedFormat == ExportSharingManager.ExportFormat.PDF) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = strings.paperSizeOrientationLabel,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = mutedText,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        var paperDropdownExpanded by remember { mutableStateOf(false) }
                                        Box(modifier = Modifier.weight(0.85f)) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(40.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(controlSurface)
                                                    .clickable { paperDropdownExpanded = true }
                                                    .padding(horizontal = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = selectedBaseSize,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = primaryText,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                                Icon(
                                                    imageVector = Icons.Default.KeyboardArrowDown,
                                                    contentDescription = null,
                                                    tint = mutedText,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            DropdownMenu(
                                                expanded = paperDropdownExpanded,
                                                onDismissRequest = { paperDropdownExpanded = false },
                                                shape = RoundedCornerShape(12.dp),
                                                containerColor = cardSurface,
                                                border = BorderStroke(1.dp, dividerColor)
                                            ) {
                                                listOf("A4", "A3", "A5", "Letter").forEach { size ->
                                                    DropdownMenuItem(
                                                        text = {
                                                            Text(
                                                                text = size,
                                                                fontSize = 13.sp,
                                                                fontWeight = if (size == selectedBaseSize) FontWeight.SemiBold else FontWeight.Normal,
                                                                color = if (size == selectedBaseSize) accentColor else primaryText
                                                            )
                                                        },
                                                        onClick = {
                                                            selectedBaseSize = size
                                                            paperDropdownExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        Box(modifier = Modifier.weight(1.45f)) {
                                            ExportSegmentedRow(
                                                items = listOf(
                                                    ExportSegmentItem(
                                                        value = false,
                                                        label = strings.portraitOrientation,
                                                        testTag = "paper_portrait"
                                                    ),
                                                    ExportSegmentItem(
                                                        value = true,
                                                        label = strings.landscapeOrientation,
                                                        testTag = "paper_landscape"
                                                    )
                                                ),
                                                selectedValue = isLandscape,
                                                onSelected = { isLandscape = it },
                                                trackColor = controlSurface,
                                                accentColor = accentColor,
                                                primaryText = primaryText,
                                                mutedText = mutedText
                                            )
                                        }
                                    }
                                }
                            }

                            // 3 & 4. Combined Font Card (Text & Überschriften separated by a soft divider)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(cardSurface)
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                FontConfigSubsection(
                                    title = strings.typographyBodyTitle,
                                    sectionIcon = Icons.AutoMirrored.Filled.FormatAlignLeft,
                                    fontColorLabel = strings.exportFontColorLabel,
                                    currentFont = bodyFontFamily,
                                    onFontSelected = { bodyFontFamily = it },
                                    currentSize = bodyFontSize,
                                    onSizeSelectorClicked = { activeFontSizeTarget = "body" },
                                    currentColorHex = bodyFontColorHex,
                                    onColorPickerRequested = { activeColorTarget = "body" },
                                    isBoldPreview = false,
                                    testTagPrefix = "body",
                                    controlSurface = controlSurface,
                                    menuSurface = cardSurface,
                                    dividerColor = dividerColor,
                                    accentColor = accentColor,
                                    primaryText = primaryText,
                                    mutedText = mutedText
                                )

                                HorizontalDivider(
                                    color = dividerColor,
                                    thickness = 1.dp
                                )

                                FontConfigSubsection(
                                    title = strings.typographyHeaderTitle,
                                    sectionIcon = Icons.Default.Title,
                                    fontColorLabel = strings.exportFontColorLabel,
                                    currentFont = headerFontFamily,
                                    onFontSelected = { headerFontFamily = it },
                                    currentSize = headerFontSize,
                                    onSizeSelectorClicked = { activeFontSizeTarget = "header" },
                                    currentColorHex = headerFontColorHex,
                                    onColorPickerRequested = { activeColorTarget = "header" },
                                    isBoldPreview = true,
                                    testTagPrefix = "header",
                                    controlSurface = controlSurface,
                                    menuSurface = cardSurface,
                                    dividerColor = dividerColor,
                                    accentColor = accentColor,
                                    primaryText = primaryText,
                                    mutedText = mutedText
                                )
                            }

                            // 5. Table Color Card (4-Tier Hierarchical System)
                            ExportTableColorConfigCard(
                                title = strings.exportTableColorTitle,
                                subtitle = strings.exportTableColorUntouchedTextHint,
                                headerSwatchCaption = strings.exportTableHeaderCaption,
                                defaultNeutralLabel = strings.exportTableColorDefaultNeutral,
                                colorWheelActionLabel = strings.exportTableColorWheelAction,
                                calmModeLabel = strings.exportTableColorModeCalm,
                                vividModeLabel = strings.exportTableColorModeVivid,
                                seedColorHex = exportTableSeedColorHex,
                                isVividMode = isTableColorVivid,
                                onModeChanged = { vivid ->
                                    isTableColorVivid = vivid
                                    CustomColorManager.setExportTableColorVividMode(context, vivid)
                                    if (exportTableSeedColorHex.isBlank()) {
                                        exportTableSeedColorHex = "#2979FF"
                                        CustomColorManager.setExportTableSeedColor(context, "#2979FF")
                                    }
                                },
                                onOpenColorWheel = { activeColorTarget = "table" },
                                cardSurface = cardSurface,
                                controlSurface = controlSurface,
                                accentColor = accentColor,
                                primaryText = primaryText,
                                mutedText = mutedText
                            )

                            // 6. File Name Input Section (Proportional text, middle-ellipsis display, attached fixed suffix)
                            FileNameFieldWithSuffix(
                                label = strings.fileNameLabel,
                                value = fileNameInput,
                                placeholder = defaultBaseName,
                                extensionSuffix = ".${selectedFormat.extension}",
                                onValueChange = { input ->
                                    hasUserEditedFileName = true
                                    fileNameInput = input.trimStart('.')
                                },
                                controlSurface = controlSurface,
                                dividerColor = dividerColor,
                                accentColor = accentColor,
                                primaryText = primaryText,
                                mutedText = mutedText
                            )

                            HorizontalDivider(color = dividerColor, thickness = 1.dp)

                            // 7. Uniform Bottom Action Buttons (Consistent equal width, height, corner shape, and layout inside scroll flow)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Cancel button
                                OutlinedButton(
                                    onClick = onDismiss,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("export_cancel_button"),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = controlSurface,
                                        contentColor = primaryText
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        tint = mutedText,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    AutoSizingText(
                                        text = strings.cancel,
                                        maxFontSize = 13.sp,
                                        minFontSize = 9.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = primaryText
                                    )
                                }

                                // Share button
                                OutlinedButton(
                                    onClick = {
                                        val sanitizedBase = ExportSharingManager.sanitizeFileName(fileNameInput)
                                        ExportSharingManager.shareUnifiedSearchHistory(
                                            context = context,
                                            nouns = effectiveNouns,
                                            verbs = effectiveVerbs,
                                            adjectives = effectiveAdjektive,
                                            scope = selectedScope,
                                            format = selectedFormat,
                                            customBaseName = sanitizedBase,
                                            paperSize = selectedPaperSize,
                                            typography = currentTypography,
                                            notebookName = effectiveNotebookName
                                        )
                                        onDismiss()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("export_share_button"),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = controlSurface,
                                        contentColor = primaryText
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = null,
                                        tint = primaryText,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    AutoSizingText(
                                        text = strings.share,
                                        maxFontSize = 13.sp,
                                        minFontSize = 9.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = primaryText
                                    )
                                }

                                // Confirm button
                                Button(
                                    onClick = {
                                        val sanitizedBase = ExportSharingManager.sanitizeFileName(fileNameInput)
                                        val targetFileName = ExportSharingManager.buildFileName(sanitizedBase, selectedFormat, effectiveNotebookName)
                                        try {
                                            saveFileLauncher.launch(targetFileName)
                                        } catch (e: Exception) {
                                            ExportSharingManager.saveUnifiedToDownloads(
                                                context = context,
                                                nouns = effectiveNouns,
                                                verbs = effectiveVerbs,
                                                adjectives = effectiveAdjektive,
                                                scope = selectedScope,
                                                format = selectedFormat,
                                                customBaseName = sanitizedBase,
                                                paperSize = selectedPaperSize,
                                                typography = currentTypography,
                                                notebookName = effectiveNotebookName
                                            )
                                            onDismiss()
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("export_save_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = accentColor,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Save,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
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

        // Unified Color Wheel Modal Dialog (Serves Body Font, Header Font, and Table Color)
        if (activeColorTarget != null) {
            val target = activeColorTarget
            val isTableTarget = (target == "table")
            val initialWheelHex = when (target) {
                "body" -> bodyFontColorHex
                "header" -> headerFontColorHex
                else -> if (exportTableSeedColorHex.isNotBlank()) exportTableSeedColorHex else "#2979FF"
            }
            NatureGradientSphereColorPicker(
                initialColorHex = initialWheelHex,
                onColorChosen = { chosenHex ->
                    when (target) {
                        "body" -> {
                            bodyFontColorHex = chosenHex
                            CustomColorManager.setExportBodyFontColor(context, chosenHex)
                        }
                        "header" -> {
                            headerFontColorHex = chosenHex
                            CustomColorManager.setExportHeaderFontColor(context, chosenHex)
                        }
                        "table" -> {
                            exportTableSeedColorHex = chosenHex
                            CustomColorManager.setExportTableSeedColor(context, chosenHex)
                        }
                    }
                    activeColorTarget = null
                },
                onDismiss = { activeColorTarget = null },
                isExportTableColorMode = isTableTarget,
                isVividTableColorMode = isTableColorVivid,
                onResetDefaultNeutral = if (isTableTarget) {
                    {
                        exportTableSeedColorHex = ""
                        CustomColorManager.setExportTableSeedColor(context, "")
                        activeColorTarget = null
                    }
                } else null,
                previewBodyFontFamily = bodyFontFamily,
                previewBodyFontColorHex = bodyFontColorHex,
                previewHeaderFontFamily = headerFontFamily,
                previewHeaderFontColorHex = headerFontColorHex
            )
        }

        // Font Size Picker Centered Dialog
        if (activeFontSizeTarget != null) {
            val currentSz = if (activeFontSizeTarget == "body") bodyFontSize else headerFontSize
            FontSizePickerDialog(
                initialSize = currentSz,
                onSizeSelected = { newSz ->
                    if (activeFontSizeTarget == "body") {
                        bodyFontSize = newSz
                    } else {
                        headerFontSize = newSz
                    }
                    activeFontSizeTarget = null
                },
                onDismiss = { activeFontSizeTarget = null }
            )
        }
    }
}

private data class ExportSegmentItem<T>(
    val value: T,
    val label: String,
    val icon: ImageVector? = null,
    val testTag: String? = null
)

/**
 * Unified segmented selector across all toggle controls:
 * - Selected: filled accent tint + 1.dp accent border + high-contrast accent text
 * - Unselected: flat & muted on the control track
 */
@Composable
private fun <T> ExportSegmentedRow(
    items: List<ExportSegmentItem<T>>,
    selectedValue: T,
    onSelected: (T) -> Unit,
    trackColor: Color,
    accentColor: Color,
    primaryText: Color,
    mutedText: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(trackColor)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val isSelected = item.value == selectedValue
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) accentColor.copy(alpha = 0.18f) else Color.Transparent)
                    .border(
                        width = if (isSelected) 1.dp else 0.dp,
                        color = if (isSelected) accentColor else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelected(item.value) }
                    .padding(horizontal = 6.dp)
                    .let { mod -> if (item.testTag != null) mod.testTag(item.testTag) else mod },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (item.icon != null) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = if (isSelected) accentColor else mutedText,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = item.label,
                    fontSize = 12.5.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) accentColor else mutedText,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Shared Font & Color Subsection inside the Combined Font Card.
 * Fixed internal rhythm: Title (alone on line) -> Controls (Font + Size) -> Color Row -> Full-Width Preview.
 */
@Composable
private fun FontConfigSubsection(
    title: String,
    sectionIcon: ImageVector,
    fontColorLabel: String,
    currentFont: String,
    onFontSelected: (String) -> Unit,
    currentSize: Float,
    onSizeSelectorClicked: () -> Unit,
    currentColorHex: String,
    onColorPickerRequested: () -> Unit,
    isBoldPreview: Boolean,
    testTagPrefix: String,
    controlSurface: Color,
    menuSurface: Color,
    dividerColor: Color,
    accentColor: Color,
    primaryText: Color,
    mutedText: Color
) {
    var fontDropdownExpanded by remember { mutableStateOf(false) }

    val colorInt = remember(currentColorHex) {
        DeclensionHistoryExportHelper.parseHexColor(currentColorHex)
    }
    val colorName = remember(currentColorHex) {
        val found = DeclensionHistoryExportHelper.PREDEFINED_COLORS.find {
            it.second.equals(currentColorHex, ignoreCase = true)
        }
        found?.first ?: fontColorLabel
    }
    val selectedComposeFont = remember(currentFont) {
        DeclensionHistoryExportHelper.getComposeFontFamily(currentFont)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Title alone on its line with distinct monochrome icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = sectionIcon,
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
                softWrap = false
            )
        }

        // 2. Controls: Font Type & Font Size (identical height, text style, alignment & affordance cues)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Font Type Selector
            Box(modifier = Modifier.weight(1.4f)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(controlSurface)
                        .clickable { fontDropdownExpanded = true }
                        .padding(horizontal = 12.dp)
                        .testTag("${testTagPrefix}_font_selector"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = currentFont,
                        fontFamily = selectedComposeFont,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = primaryText,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = mutedText,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = fontDropdownExpanded,
                    onDismissRequest = { fontDropdownExpanded = false },
                    shape = RoundedCornerShape(12.dp),
                    containerColor = menuSurface,
                    border = BorderStroke(1.dp, dividerColor)
                ) {
                    DeclensionHistoryExportHelper.AVAILABLE_FONTS.forEach { font ->
                        val itemComposeFont = remember(font) {
                            DeclensionHistoryExportHelper.getComposeFontFamily(font)
                        }
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = font,
                                        fontFamily = itemComposeFont,
                                        fontSize = 13.sp,
                                        fontWeight = if (font == currentFont) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (font == currentFont) accentColor else primaryText
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Aa Ää",
                                        fontFamily = itemComposeFont,
                                        fontSize = 12.sp,
                                        color = mutedText
                                    )
                                }
                            },
                            onClick = {
                                onFontSelected(font)
                                fontDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Font Size Selector (identical style, alignment, and tappable cue)
            val sizeDisplay = if (currentSize % 1.0f == 0.0f) "${currentSize.toInt()} pt" else "$currentSize pt"
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(controlSurface)
                    .clickable { onSizeSelectorClicked() }
                    .padding(horizontal = 12.dp)
                    .testTag("${testTagPrefix}_size_selector"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = sizeDisplay,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = primaryText,
                    maxLines = 1,
                    softWrap = false
                )
                Icon(
                    imageVector = Icons.Default.UnfoldMore,
                    contentDescription = null,
                    tint = mutedText,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // 3. Color Row: Swatch, Full Color Name (never truncated), plain #HEX, Palette button
        UnifiedExportColorRow(
            colorName = colorName,
            hexText = currentColorHex.uppercase(),
            onOpenColorWheel = onColorPickerRequested,
            selectorTestTag = "${testTagPrefix}_color_selector",
            controlSurface = controlSurface,
            accentColor = accentColor,
            primaryText = primaryText,
            mutedText = mutedText,
            leadingSwatch = {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(colorInt))
                        .border(1.dp, dividerColor, RoundedCornerShape(6.dp))
                )
            }
        )

        // 4. Compact sample preview pill sized appropriately to its content on a permanent white container
        UnifiedTypographyPreview(
            fontFamily = selectedComposeFont,
            colorInt = colorInt,
            isBold = isBoldPreview,
            dividerColor = dividerColor,
            testTag = "${testTagPrefix}_typography_preview",
            modifier = Modifier.wrapContentWidth()
        )
    }
}

/**
 * Clean bidirectional sample preview ("Aa Bb Ää العربية") rendered on a permanent
 * crisp white paper container sized to its content for both Body Text and Headers.
 */
@Composable
private fun UnifiedTypographyPreview(
    fontFamily: FontFamily,
    colorInt: Int,
    isBold: Boolean,
    dividerColor: Color,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val inkColor = Color(colorInt)
    val luminance = (0.299f * inkColor.red + 0.587f * inkColor.green + 0.114f * inkColor.blue)
    val ultraLightInkShadow = if (luminance > 0.88f) {
        Shadow(
            color = Color(0x991E293B),
            offset = Offset(0f, 1f),
            blurRadius = 3f
        )
    } else {
        null
    }

    Box(
        modifier = modifier
            .wrapContentWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.dp, dividerColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag(testTag),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "Aa Bb Ää العربية",
            style = TextStyle(
                fontFamily = fontFamily,
                fontSize = 13.5.sp,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                color = inkColor,
                shadow = ultraLightInkShadow,
                textDirection = TextDirection.Ltr,
                lineHeight = 18.sp,
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.None
                )
            ),
            maxLines = 1,
            softWrap = false
        )
    }
}

/**
 * Unified flat color row shared by Text, Überschriften, and Tabellenfarbe:
 * [Swatch] + [Full Color Name (never an ellipsis)] + [Plain readable #HEX] + [Palette button].
 */
@Composable
private fun UnifiedExportColorRow(
    colorName: String,
    hexText: String,
    onOpenColorWheel: () -> Unit,
    selectorTestTag: String,
    controlSurface: Color,
    accentColor: Color,
    primaryText: Color,
    mutedText: Color,
    leadingSwatch: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 42.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(controlSurface)
            .clickable { onOpenColorWheel() }
            .padding(start = 12.dp, end = 6.dp, top = 8.dp, bottom = 8.dp)
            .testTag(selectorTestTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        leadingSwatch()

        Text(
            text = colorName,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = primaryText,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = hexText,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = mutedText,
                fontFeatureSettings = "tnum"
            ),
            maxLines = 1,
            softWrap = false
        )

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(accentColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = colorName,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Dedicated Export Table Color Card:
 * 1. Title alone on line 1 (with monochrome TableChart icon)
 * 2. Subtitle on line 2 ("4-Stufen-Hierarchie")
 * 3. Segmented toggle ("Sanfte Töne" / "Originalfarbe")
 * 4. Unified Color Row with a single continuous 4-tier strip (Kopfzeile caption above + accent highlight on header segment) & matching #HEX
 */
@Composable
private fun ExportTableColorConfigCard(
    title: String,
    subtitle: String,
    headerSwatchCaption: String,
    defaultNeutralLabel: String,
    colorWheelActionLabel: String,
    calmModeLabel: String,
    vividModeLabel: String,
    seedColorHex: String,
    isVividMode: Boolean,
    onModeChanged: (Boolean) -> Unit,
    onOpenColorWheel: () -> Unit,
    cardSurface: Color,
    controlSurface: Color,
    accentColor: Color,
    primaryText: Color,
    mutedText: Color
) {
    val palette = remember(seedColorHex, isVividMode) {
        DeclensionHistoryExportHelper.resolveExportTableColorPalette(seedColorHex, isVividMode)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cardSurface)
            .padding(14.dp)
            .testTag("export_table_color_card"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Title alone on its line + 2. Quieter subtitle below it
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TableChart,
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
                    softWrap = false
                )
            }
            Text(
                text = subtitle,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = mutedText,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.padding(start = 26.dp)
            )
        }

        // 3. Controls: Segmented 2-Option Mode Switcher ("Sanfte Töne" / "Originalfarbe")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(controlSurface)
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isCalmSelected = !isVividMode
            val isVividSelected = isVividMode

            listOf(
                Triple(false, calmModeLabel, "export_table_mode_calm"),
                Triple(true, vividModeLabel, "export_table_mode_vivid")
            ).forEach { (vividOption, label, tag) ->
                val isSelected = if (vividOption) isVividSelected else isCalmSelected
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) accentColor.copy(alpha = 0.18f) else Color.Transparent)
                        .border(
                            width = if (isSelected) 1.dp else 0.dp,
                            color = if (isSelected) accentColor else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onModeChanged(vividOption) }
                        .padding(horizontal = 8.dp)
                        .testTag(tag),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 12.5.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) accentColor else mutedText,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        // 4. Unified Color Row with single continuous 4-tier tonal strip + clean "Kopfzeile" caption above
        val activeColorLabel = if (palette.isDefaultNeutral) {
            defaultNeutralLabel
        } else {
            val foundName = DeclensionHistoryExportHelper.PREDEFINED_COLORS.find {
                it.second.equals(seedColorHex, ignoreCase = true)
            }?.first
            foundName ?: colorWheelActionLabel
        }
        // Always display the exact HEX of the highlighted Tier 1 Kopfzeile tone so strip & HEX match
        val displayedHeaderHex = if (palette.isDefaultNeutral) {
            "#D9D9D9"
        } else {
            palette.tier1HeaderFillHex.uppercase()
        }

        UnifiedExportColorRow(
            colorName = activeColorLabel,
            hexText = displayedHeaderHex,
            onOpenColorWheel = onOpenColorWheel,
            selectorTestTag = "export_table_color_selector",
            controlSurface = controlSurface,
            accentColor = accentColor,
            primaryText = primaryText,
            mutedText = mutedText,
            leadingSwatch = {
                Row(
                    modifier = Modifier
                        .width(64.dp)
                        .height(22.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .border(
                            width = 0.75.dp,
                            color = Color(palette.tier3MajorBorderColorInt).copy(alpha = 0.75f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .testTag("export_table_color_strip"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // First segment: Tier 1 Header, subtly ringed with accentColor
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .fillMaxHeight()
                            .background(Color(palette.tier1HeaderFillColorInt))
                            .border(
                                width = 1.5.dp,
                                color = accentColor,
                                shape = RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)
                            )
                    )
                    // Remaining 3 tones joined seamlessly as one continuous hierarchy strip
                    listOf(
                        palette.tier2SecondaryBlockFillColorInt,
                        palette.tier2PrimaryBlockFillColorInt,
                        palette.tier3MajorBorderColorInt
                    ).forEach { cInt ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(Color(cInt))
                        )
                    }
                }
            }
        )
    }
}

/**
 * Proportional filename field matching control text size, with obvious editable affordance,
 * middle-ellipsis truncation when unfocused, and a fixed attached extension compartment.
 */
@Composable
private fun FileNameFieldWithSuffix(
    label: String,
    value: String,
    placeholder: String,
    extensionSuffix: String,
    onValueChange: (String) -> Unit,
    controlSurface: Color,
    dividerColor: Color,
    accentColor: Color,
    primaryText: Color,
    mutedText: Color
) {
    var isFocused by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = mutedText,
            maxLines = 1,
            softWrap = false
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(controlSurface)
                .border(
                    width = if (isFocused) 1.5.dp else 0.dp,
                    color = if (isFocused) accentColor else Color.Transparent,
                    shape = RoundedCornerShape(10.dp)
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = if (isFocused) accentColor else mutedText,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))

            val textMeasurer = rememberTextMeasurer()
            val fieldStyle = remember(primaryText) {
                TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = primaryText
                )
            }

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isFocused) primaryText else Color.Transparent
                ),
                cursorBrush = SolidColor(accentColor),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
                    .onFocusChanged { focusState -> isFocused = focusState.isFocused }
                    .testTag("file_name_input"),
                decorationBox = { innerTextField ->
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        val maxWidthPx = constraints.maxWidth
                        val rawDisplay = value.ifEmpty { placeholder }
                        val ellipsizedDisplay = remember(rawDisplay, maxWidthPx, fieldStyle) {
                            ellipsizeMiddleToFit(
                                text = rawDisplay,
                                maxWidthPx = maxWidthPx,
                                textMeasurer = textMeasurer,
                                style = fieldStyle
                            )
                        }
                        if (isFocused) {
                            if (value.isEmpty()) {
                                Text(
                                    text = ellipsizedDisplay,
                                    fontSize = 13.sp,
                                    color = mutedText.copy(alpha = 0.6f),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                            innerTextField()
                        } else {
                            Text(
                                text = ellipsizedDisplay,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (value.isEmpty()) mutedText.copy(alpha = 0.6f) else primaryText,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight(0.55f)
                    .background(dividerColor)
            )

            Box(
                modifier = Modifier.padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = extensionSuffix,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = mutedText,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

private fun ellipsizeMiddleToFit(
    text: String,
    maxWidthPx: Int,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    style: TextStyle
): String {
    if (text.isEmpty() || maxWidthPx <= 0) return text
    val fullWidth = textMeasurer.measure(text = text, style = style, maxLines = 1, softWrap = false).size.width
    if (fullWidth <= maxWidthPx) return text

    var low = 1
    var high = (text.length - 1) / 2
    var best = "…"
    while (low <= high) {
        val mid = (low + high) / 2
        val candidate = text.take(mid) + "…" + text.takeLast(mid)
        val w = textMeasurer.measure(text = candidate, style = style, maxLines = 1, softWrap = false).size.width
        if (w <= maxWidthPx) {
            best = candidate
            low = mid + 1
        } else {
            high = mid - 1
        }
    }
    return best
}

/**
 * Modern Centered Dialog for selecting font size:
 * 1. Compact header with title "Schriftgröße wählen" and [X] close button
 * 2. 4x3 Grid of 12 common preset sizes (9, 10, 11, 12 / 14, 16, 18, 20 / 24, 28, 36, 48 pt)
 * 3. Dedicated "Eigene Größe" section with [-], numeric input field, and [+] buttons
 * 4. Confirmation [Übernehmen] and cancel [Abbrechen] buttons at the bottom
 */
@Composable
private fun FontSizePickerDialog(
    initialSize: Float,
    onSizeSelected: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val appLanguage = LocalAppLanguage.current
    val strings = remember(appLanguage) { AppStrings.get(appLanguage) }
    var selectedSize by remember { mutableFloatStateOf(initialSize) }
    var customInputText by remember {
        mutableStateOf(if (initialSize % 1f == 0f) initialSize.toInt().toString() else initialSize.toString())
    }

    val standardSizes = remember {
        listOf(
            listOf(9f, 10f, 11f, 12f),
            listOf(14f, 16f, 18f, 20f),
            listOf(24f, 28f, 36f, 48f)
        )
    }

    val focusManager = LocalFocusManager.current
    val windowInsetsIme = WindowInsets.ime
    val windowInsetsNav = WindowInsets.navigationBars

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect {
            dialogWindow?.let { win ->
                win.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
                win.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusManager.clearFocus()
                    onDismiss()
                }
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(min = 280.dp, max = 340.dp)
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight()
                    .graphicsLayer {
                        // Read insets inside graphicsLayer scope to execute on RenderNode GPU pass without recomposition
                        val imeBottomPx = windowInsetsIme.getBottom(this)
                        val navBottomPx = windowInsetsNav.getBottom(this)
                        val imeHeightPx = maxOf(0, imeBottomPx - navBottomPx)
                        val extraMarginPx = if (imeHeightPx > 0) 24.dp.toPx() else 0f
                        translationY = -(imeHeightPx / 2f + extraMarginPx)
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { focusManager.clearFocus() }
                    .testTag("font_size_dialog")
                    .testTag("font_size_sheet"),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 6.dp,
                border = BorderStroke(1.dp, GeoBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header: Title + [X] Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.fontSizeDialogTitle,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("font_size_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = strings.close,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        thickness = 1.dp
                    )

                    // Section 1: Standardgrößen
                    Text(
                        text = strings.standardSizesLabel,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        standardSizes.forEach { rowSizes ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                rowSizes.forEach { sz ->
                                    val isSelected = (selectedSize == sz)
                                    val label = "${sz.toInt()} pt"
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(30.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .clickable {
                                                selectedSize = sz
                                                customInputText = if (sz % 1f == 0f) sz.toInt().toString() else sz.toString()
                                            }
                                            .testTag("preset_size_${sz.toInt()}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        thickness = 1.dp
                    )

                    // Section 2: Eigene Größe
                    Text(
                        text = strings.customSizeLabel,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // [-] Button (with Hold-to-Repeat)
                        HoldRepeatStepButton(
                            onStep = {
                                val current = customInputText.replace(',', '.').toFloatOrNull() ?: selectedSize
                                val newSz = maxOf(1f, current - 1f)
                                selectedSize = newSz
                                customInputText = if (newSz % 1f == 0f) newSz.toInt().toString() else newSz.toString()
                            },
                            shape = RoundedCornerShape(8.dp),
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            activeContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                            borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("font_size_minus")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = strings.decreaseFontSizeDesc,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Direct numeric input field - compact, centered, and guaranteed no clipping
                        BasicTextField(
                            value = customInputText,
                            onValueChange = { input ->
                                val filtered = input.filter { it.isDigit() || it == '.' || it == ',' }
                                customInputText = filtered
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                autoCorrectEnabled = false,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    val parsed = customInputText.replace(',', '.').toFloatOrNull()
                                    if (parsed != null && parsed >= 1f && parsed <= 1638f) {
                                        selectedSize = (Math.round(parsed * 2.0) / 2.0).toFloat()
                                    }
                                }
                            ),
                            singleLine = true,
                            textStyle = TextStyle(
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            decorationBox = { innerTextField ->
                                Row(
                                    modifier = Modifier
                                        .width(100.dp)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier.weight(1f, fill = false),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        innerTextField()
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "pt",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            modifier = Modifier
                                .onFocusChanged { focusState ->
                                    if (!focusState.isFocused) {
                                        val parsed = customInputText.replace(',', '.').toFloatOrNull()
                                        if (parsed != null && parsed >= 1f && parsed <= 1638f) {
                                            selectedSize = (Math.round(parsed * 2.0) / 2.0).toFloat()
                                        }
                                    }
                                }
                                .testTag("font_size_custom_input")
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        // [+] Button (with Hold-to-Repeat)
                        HoldRepeatStepButton(
                            onStep = {
                                val current = customInputText.replace(',', '.').toFloatOrNull() ?: selectedSize
                                val newSz = minOf(1638f, current + 1f)
                                selectedSize = newSz
                                customInputText = if (newSz % 1f == 0f) newSz.toInt().toString() else newSz.toString()
                            },
                            shape = RoundedCornerShape(8.dp),
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            activeContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                            borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("font_size_plus")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = strings.increaseFontSizeDesc,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        thickness = 1.dp
                    )

                    // Bottom Action Buttons: [ Cancel / Abbrechen ] [ Confirm / Bestätigen ]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("font_size_cancel_button"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                        ) {
                            AutoSizingText(
                                text = strings.cancel,
                                maxFontSize = 13.sp,
                                minFontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Button(
                            onClick = {
                                val parsed = customInputText.replace(',', '.').toFloatOrNull()
                                val finalSz = if (parsed != null && parsed >= 1f && parsed <= 1638f) {
                                    (Math.round(parsed * 2.0) / 2.0).toFloat()
                                } else {
                                    selectedSize
                                }
                                onSizeSelected(finalSz)
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("font_size_confirm_button"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
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

private tailrec fun Context.findHostActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findHostActivity()
    else -> null
}
