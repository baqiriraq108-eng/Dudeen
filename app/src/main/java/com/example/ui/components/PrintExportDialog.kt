package com.example.ui.components

import android.net.Uri
import android.view.View
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.example.data.model.WordDeclensionResult
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.LocalAppLanguage
import com.example.ui.theme.GeoBorder
import com.example.ui.theme.GeoOnSurface
import com.example.ui.theme.GeoOnSurfaceVariant
import com.example.ui.theme.GeoPrimary
import com.example.ui.theme.GeoSurface
import com.example.ui.theme.GeoSurfaceVariant
import com.example.util.CustomColorManager
import com.example.util.DeclensionHistoryExportHelper
import com.example.util.ExportSharingManager
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Modern, compact export dialog:
 * - Header: [<-] Exportieren [X]
 * - Scope selector: [ Nomen ] [ Verb ] [ Alle ]
 * - Format selector: [ Excel (.xlsx) ] [ PDF ]
 * - Paper size & orientation selector (when PDF selected): [A4 v] [ Hochformat / Querformat ]
 * - Advanced Print Customization:
 *   - Body Text: Font type (10 fonts), Font size (8-72pt + custom), Color (20 colors + hex)
 *   - Headers: Font type (10 fonts), Font size (8-72pt + custom), Color (20 colors + hex)
 * - Single-line file name input field pre-filled with "Suchverlauf_Deutsch"
 * - Two equal-weight action buttons side-by-side: [Speichern] & [Teilen]
 * - Strictly German/English only (no Arabic text, no text wrapping)
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

    var selectedScope by remember { mutableStateOf(initialScope) }
    var selectedFormat by remember { mutableStateOf(ExportSharingManager.ExportFormat.XLSX) }
    val defaultBaseName = remember(effectiveNotebookName) {
        val cleanNb = ExportSharingManager.sanitizeFileName(effectiveNotebookName, defaultName = "")
        if (cleanNb.isNotBlank()) "Suchverlauf_$cleanNb" else "Suchverlauf_Deutsch"
    }
    var fileNameInput by remember(defaultBaseName) { mutableStateOf(defaultBaseName) }

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
    var bodyFontColorHex by remember { mutableStateOf("#000000") }

    // Typography customization: Headers
    var headerFontFamily by remember { mutableStateOf("Arial") }
    var headerFontSize by remember { mutableStateOf(14f) }
    var headerFontColorHex by remember { mutableStateOf("#000000") }

    // Dialogs for color picker & font size bottom sheet
    var activeColorTarget by remember { mutableStateOf<String?>(null) } // "body" or "header"
    var activeFontSizeTarget by remember { mutableStateOf<String?>(null) } // "body" or "header"

    val currentTypography = remember(
        bodyFontSize, bodyFontFamily, bodyFontColorHex,
        headerFontSize, headerFontFamily, headerFontColorHex
    ) {
        DeclensionHistoryExportHelper.ExportTypographyOptions(
            bodyFontSize = bodyFontSize,
            bodyFontFamily = bodyFontFamily,
            bodyFontColorHex = bodyFontColorHex,
            headerFontSize = headerFontSize,
            headerFontFamily = headerFontFamily,
            headerFontColorHex = headerFontColorHex
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
                    Toast.makeText(context, "Datei erfolgreich gespeichert!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                } catch (e: Exception) {
                    Toast.makeText(context, "Fehler: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    CompositionLocalProvider(LocalAppLanguage provides effectiveLang) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BackHandler(enabled = true) {
            onDismiss()
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 18.dp)
                .heightIn(max = 700.dp)
                .testTag("export_dialog_surface"),
            shape = RoundedCornerShape(16.dp),
            color = GeoSurface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header: Back button + Title + [X] Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("export_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = strings.back,
                                tint = GeoOnSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = strings.exportDialogTitle,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = GeoOnSurface,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("dismiss_export_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = strings.close,
                            tint = GeoOnSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Scope Selector: [ Nomen ] [ Verb ] [ Alle ]
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = strings.exportScopeLabel,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = GeoOnSurfaceVariant,
                        maxLines = 1,
                        softWrap = false
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(GeoSurfaceVariant)
                            .padding(2.5.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        listOf(
                            DeclensionHistoryExportHelper.ExportScope.NOMEN to strings.tabExpressions,
                            DeclensionHistoryExportHelper.ExportScope.VERB to strings.tabVerb,
                            DeclensionHistoryExportHelper.ExportScope.ADJEKTIV to strings.tabAdjectives,
                            DeclensionHistoryExportHelper.ExportScope.ALLE to strings.all
                        ).forEach { (scopeItem, label) ->
                            val isSelected = selectedScope == scopeItem
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(30.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Transparent)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) GeoPrimary else Color.Transparent,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable { selectedScope = scopeItem }
                                    .testTag("scope_tab_${scopeItem.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) GeoPrimary else GeoOnSurfaceVariant,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }

                // Format Selector (Excel vs PDF)
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = strings.exportFormatSectionLabel,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = GeoOnSurfaceVariant,
                        maxLines = 1,
                        softWrap = false
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(GeoSurfaceVariant)
                            .padding(2.5.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        // Excel Option
                        val isExcel = selectedFormat == ExportSharingManager.ExportFormat.XLSX
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Transparent)
                                .border(
                                    width = if (isExcel) 1.5.dp else 1.dp,
                                    color = if (isExcel) GeoPrimary else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable { selectedFormat = ExportSharingManager.ExportFormat.XLSX }
                                .padding(horizontal = 6.dp)
                                .testTag("format_excel_tab"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TableChart,
                                    contentDescription = null,
                                    tint = if (isExcel) GeoPrimary else GeoOnSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Excel (.xlsx)",
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isExcel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isExcel) GeoPrimary else GeoOnSurfaceVariant,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        // PDF Option
                        val isPdf = selectedFormat == ExportSharingManager.ExportFormat.PDF
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Transparent)
                                .border(
                                    width = if (isPdf) 1.5.dp else 1.dp,
                                    color = if (isPdf) GeoPrimary else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable { selectedFormat = ExportSharingManager.ExportFormat.PDF }
                                .padding(horizontal = 6.dp)
                                .testTag("format_pdf_tab"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = if (isPdf) GeoPrimary else GeoOnSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "PDF",
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isPdf) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isPdf) GeoPrimary else GeoOnSurfaceVariant,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }

                // Paper Size & Orientation Selector (Visible for PDF format)
                if (selectedFormat == ExportSharingManager.ExportFormat.PDF) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = strings.paperSizeOrientationLabel,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = GeoOnSurfaceVariant,
                            maxLines = 1,
                            softWrap = false
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Paper size dropdown selector
                            var paperDropdownExpanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(0.9f)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(30.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(GeoSurfaceVariant)
                                        .clickable { paperDropdownExpanded = true }
                                        .padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = selectedBaseSize,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = GeoOnSurface,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = GeoOnSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                DropdownMenu(
                                    expanded = paperDropdownExpanded,
                                    onDismissRequest = { paperDropdownExpanded = false }
                                ) {
                                    listOf("A4", "A3", "A5", "Letter").forEach { size ->
                                        DropdownMenuItem(
                                            text = { Text(size, fontSize = 12.sp) },
                                            onClick = {
                                                selectedBaseSize = size
                                                paperDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Orientation Segmented Buttons
                            Row(
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(30.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GeoSurfaceVariant)
                                    .padding(2.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                listOf(false to strings.portraitOrientation, true to strings.landscapeOrientation).forEach { (landscape, label) ->
                                    val isSelected = isLandscape == landscape
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(26.dp)
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(Color.Transparent)
                                            .border(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) GeoPrimary else Color.Transparent,
                                                shape = RoundedCornerShape(5.dp)
                                            )
                                            .clickable { isLandscape = landscape }
                                            .testTag(if (landscape) "paper_landscape" else "paper_portrait"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) GeoPrimary else GeoOnSurfaceVariant,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Customization: Body Text
                TypographyConfigCard(
                    title = strings.typographyBodyTitle,
                    currentFont = bodyFontFamily,
                    onFontSelected = { bodyFontFamily = it },
                    currentSize = bodyFontSize,
                    onSizeSelectorClicked = { activeFontSizeTarget = "body" },
                    currentColorHex = bodyFontColorHex,
                    onColorPickerRequested = { activeColorTarget = "body" },
                    testTagPrefix = "body"
                )

                // Customization: Headers
                TypographyConfigCard(
                    title = strings.typographyHeaderTitle,
                    currentFont = headerFontFamily,
                    onFontSelected = { headerFontFamily = it },
                    currentSize = headerFontSize,
                    onSizeSelectorClicked = { activeFontSizeTarget = "header" },
                    currentColorHex = headerFontColorHex,
                    onColorPickerRequested = { activeColorTarget = "header" },
                    testTagPrefix = "header"
                )

                // File Name Input Section
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = strings.fileNameLabel,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = GeoOnSurfaceVariant,
                        maxLines = 1,
                        softWrap = false
                    )
                    OutlinedTextField(
                        value = fileNameInput,
                        onValueChange = { input -> fileNameInput = input.trimStart('.') },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("file_name_input"),
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = "Suchverlauf_Deutsch",
                                fontSize = 13.sp,
                                color = GeoOnSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        trailingIcon = {
                            Text(
                                text = ".${selectedFormat.extension}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = GeoOnSurfaceVariant,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GeoPrimary,
                            unfocusedBorderColor = GeoBorder,
                            focusedContainerColor = GeoSurface,
                            unfocusedContainerColor = GeoSurface
                        )
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Action Buttons Side by Side (Confirm / Save & Share / Cancel)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Share Button (Secondary Outline)
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
                                .height(42.dp)
                                .testTag("export_share_button"),
                            border = BorderStroke(1.dp, GeoPrimary.copy(alpha = 0.7f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = GeoPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    tint = GeoPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = strings.share,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GeoPrimary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        // Confirm / Save Button (Primary)
                        Button(
                            onClick = {
                                val sanitizedBase = ExportSharingManager.sanitizeFileName(fileNameInput)
                                val targetFileName = ExportSharingManager.buildFileName(sanitizedBase, selectedFormat, effectiveNotebookName)
                                try {
                                    saveFileLauncher.launch(targetFileName)
                                } catch (e: Exception) {
                                    // Fallback directly to Downloads folder if picker unavailable
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
                                .height(42.dp)
                                .testTag("export_save_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GeoPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = strings.confirm,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    // Cancel Button (Uniform 42.dp height and 10.dp radius)
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, GeoBorder)
                    ) {
                        Text(
                            text = strings.cancel,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GeoOnSurface,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }

    // Color Picker Modal Dialog
    if (activeColorTarget != null) {
        val initialColor = if (activeColorTarget == "body") bodyFontColorHex else headerFontColorHex
        NatureGradientSphereColorPicker(
            initialColorHex = initialColor,
            onColorChosen = { chosenHex ->
                if (activeColorTarget == "body") {
                    bodyFontColorHex = chosenHex
                } else {
                    headerFontColorHex = chosenHex
                }
                activeColorTarget = null
            },
            onDismiss = { activeColorTarget = null }
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

/**
 * Compact card container displaying font family, font size, and color controls.
 */
@Composable
private fun TypographyConfigCard(
    title: String,
    currentFont: String,
    onFontSelected: (String) -> Unit,
    currentSize: Float,
    onSizeSelectorClicked: () -> Unit,
    currentColorHex: String,
    onColorPickerRequested: () -> Unit,
    testTagPrefix: String
) {
    var fontDropdownExpanded by remember { mutableStateOf(false) }

    val colorInt = remember(currentColorHex) {
        DeclensionHistoryExportHelper.parseHexColor(currentColorHex)
    }
    val colorLabel = remember(currentColorHex) {
        val found = DeclensionHistoryExportHelper.PREDEFINED_COLORS.find {
            it.second.equals(currentColorHex, ignoreCase = true)
        }
        if (found != null) "${found.first} ($currentColorHex)" else currentColorHex
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(GeoSurfaceVariant)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = GeoOnSurface,
            maxLines = 1,
            softWrap = false
        )

        // Row 1: Font Type & Font Size side by side
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Font Type Dropdown
            Box(modifier = Modifier.weight(1.4f)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(GeoSurface)
                        .border(0.5.dp, GeoBorder, RoundedCornerShape(6.dp))
                        .clickable { fontDropdownExpanded = true }
                        .padding(horizontal = 8.dp)
                        .testTag("${testTagPrefix}_font_selector"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = currentFont,
                        fontSize = 11.5.sp,
                        color = GeoOnSurface,
                        maxLines = 1,
                        softWrap = false
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = GeoOnSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = fontDropdownExpanded,
                    onDismissRequest = { fontDropdownExpanded = false }
                ) {
                    DeclensionHistoryExportHelper.AVAILABLE_FONTS.forEach { font ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = font,
                                    fontSize = 12.sp,
                                    fontWeight = if (font == currentFont) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onFontSelected(font)
                                fontDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Font Size Selector (Triggers Bottom Sheet)
            val sizeDisplay = if (currentSize % 1.0f == 0.0f) "${currentSize.toInt()} pt" else "$currentSize pt"
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(30.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(GeoSurface)
                    .border(0.5.dp, GeoBorder, RoundedCornerShape(6.dp))
                    .clickable { onSizeSelectorClicked() }
                    .padding(horizontal = 8.dp)
                    .testTag("${testTagPrefix}_size_selector"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = sizeDisplay,
                    fontSize = 11.5.sp,
                    color = GeoOnSurface,
                    maxLines = 1,
                    softWrap = false
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = GeoOnSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Row 2: Color Picker Trigger
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(GeoSurface)
                .border(0.5.dp, GeoBorder, RoundedCornerShape(6.dp))
                .clickable { onColorPickerRequested() }
                .padding(horizontal = 8.dp)
                .testTag("${testTagPrefix}_color_selector"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(colorInt))
                        .border(1.dp, GeoBorder, RoundedCornerShape(4.dp))
                )
                Text(
                    text = colorLabel,
                    fontSize = 11.5.sp,
                    color = GeoOnSurface,
                    maxLines = 1,
                    softWrap = false
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = GeoOnSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
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
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 6.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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
                        // [-] Button
                        FilledTonalIconButton(
                            onClick = {
                                val current = customInputText.replace(',', '.').toFloatOrNull() ?: selectedSize
                                val newSz = maxOf(1f, current - 1f)
                                selectedSize = newSz
                                customInputText = if (newSz % 1f == 0f) newSz.toInt().toString() else newSz.toString()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("font_size_minus"),
                            shape = RoundedCornerShape(6.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = strings.decreaseFontSizeDesc,
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

                        // [+] Button
                        FilledTonalIconButton(
                            onClick = {
                                val current = customInputText.replace(',', '.').toFloatOrNull() ?: selectedSize
                                val newSz = minOf(1638f, current + 1f)
                                selectedSize = newSz
                                customInputText = if (newSz % 1f == 0f) newSz.toInt().toString() else newSz.toString()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("font_size_plus"),
                            shape = RoundedCornerShape(6.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = strings.increaseFontSizeDesc,
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
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Text(
                                text = strings.cancel,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                softWrap = false
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
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = strings.confirm,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }
    }
}

