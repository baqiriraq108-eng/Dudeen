package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.notebook.BackupNotebookEntry
import com.example.data.notebook.BackupNotebookSourceType
import com.example.data.notebook.Notebook
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.AppThemePackage

private enum class DropdownNotebookTab {
    PRIMARY,
    BACKUP_FOLDER
}

/**
 * Top bar notebook switcher dropdown button.
 * Replaces the static title on the main screen.
 */
@Composable
fun NotebookSwitcher(
    activeNotebook: Notebook?,
    allNotebooks: List<Notebook>,
    onSelectNotebook: (String) -> Unit,
    onCreateNotebookClick: () -> Unit,
    onManageNotebooksClick: () -> Unit,
    modifier: Modifier = Modifier,
    appLanguage: String = "de",
    backupNotebooks: List<BackupNotebookEntry> = emptyList(),
    onPromoteBackupNotebook: (BackupNotebookEntry) -> Unit = {},
    onOpenDropdown: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(DropdownNotebookTab.PRIMARY) }
    val strings = remember(appLanguage) { AppStrings.get(appLanguage) }
    val palette = rememberNotebookDialogPalette()

    CompositionLocalProvider(LocalLayoutDirection provides strings.language.layoutDirection) {
        Box(modifier = modifier) {
            Surface(
                onClick = {
                    onOpenDropdown()
                    expanded = true
                },
                shape = RoundedCornerShape(12.dp),
                color = palette.cardSurfaceSubtle,
                border = BorderStroke(1.dp, palette.cardBorder),
                modifier = Modifier.testTag("notebook_switcher_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = strings.notebookLabelPrefix,
                        tint = palette.primary,
                        modifier = Modifier.size(18.dp)
                    )

                    Text(
                        text = activeNotebook?.name?.takeIf { it.isNotBlank() } ?: "",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.onSurface,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                shape = RoundedCornerShape(14.dp),
                containerColor = palette.dialogSurface,
                border = BorderStroke(1.dp, palette.cardBorder),
                modifier = Modifier
                    .widthIn(min = 240.dp, max = 300.dp)
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides strings.language.layoutDirection) {
                    Text(
                        text = strings.myNotebooksTitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    // Two Icon-Only Segmented Tabs: Primary Notebooks vs. User-Selected Backup Folder Notebooks
                    UnifiedSegmentedPillBar(
                        items = listOf(
                            SegmentedPillItem(
                                id = DropdownNotebookTab.PRIMARY.name,
                                label = strings.primaryNotebooksTab,
                                count = null,
                                selectedIcon = Icons.Filled.Bookmark,
                                unselectedIcon = Icons.Outlined.BookmarkBorder,
                                testTag = "dropdown_tab_primary"
                            ),
                            SegmentedPillItem(
                                id = DropdownNotebookTab.BACKUP_FOLDER.name,
                                label = strings.backupNotebooksTab,
                                count = null,
                                selectedIcon = Icons.Filled.Folder,
                                unselectedIcon = Icons.Outlined.Folder,
                                testTag = "dropdown_tab_backup"
                            )
                        ),
                        selectedId = selectedTab.name,
                        onSelect = { id ->
                            selectedTab = DropdownNotebookTab.valueOf(id)
                        },
                        palette = palette,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )

                    HorizontalDivider(thickness = 0.8.dp, color = palette.divider)

                    val duplicateSuffix = remember { Regex("""(?:\s*_\d+|\s*\(\d+\))+$""") }

                    when (selectedTab) {
                        DropdownNotebookTab.PRIMARY -> {
                            val orderedPrimaryNotebooks = remember(allNotebooks, activeNotebook?.id) {
                                val activeId = activeNotebook?.id
                                allNotebooks
                                    .map { nb ->
                                        val cleanName = com.example.util.ExportSharingManager.stripNotebookPrefix(nb.name)
                                            .replace(duplicateSuffix, "")
                                            .trim()
                                            .ifBlank { nb.name }
                                        if (cleanName != nb.name) nb.copy(name = cleanName) else nb
                                    }
                                    .groupBy { it.name.trim().lowercase(java.util.Locale.ROOT) }
                                    .values
                                    .mapNotNull { candidates ->
                                        if (candidates.size == 1) candidates.first()
                                        else candidates.maxByOrNull { nb ->
                                            val activeScore = if (nb.id == activeId) 1_000_000_000L else 0L
                                            val unsuffixedScore = if (!duplicateSuffix.containsMatchIn(nb.id)) 50_000_000L else 0L
                                            activeScore + unsuffixedScore + nb.wordCount.toLong() * 1000L + (nb.lastModified / 1000L)
                                        }
                                    }
                                    .sortedWith(
                                        compareByDescending<Notebook> { it.id == activeId }
                                            .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name.trim() }
                                    )
                            }

                            orderedPrimaryNotebooks.forEach { notebook ->
                                val isActive = notebook.id == activeNotebook?.id
                                val themeColor = remember(notebook.settings.themeColorId) {
                                    AppThemePackage.fromId(notebook.settings.themeColorId).primary
                                }

                                DropdownMenuItem(
                                    modifier = Modifier.testTag("dropdown_notebook_item_${notebook.id}"),
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(themeColor)
                                            )

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = notebook.name,
                                                    fontSize = 14.sp,
                                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isActive) palette.primary else palette.onSurface,
                                                    maxLines = 1,
                                                    softWrap = false,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = strings.wordsCount(notebook.wordCount),
                                                    fontSize = 11.sp,
                                                    color = palette.onSurfaceVariant,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }

                                            if (isActive) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = strings.activeNotebookBadge,
                                                    tint = palette.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        expanded = false
                                        onSelectNotebook(notebook.id)
                                    }
                                )
                            }
                        }

                        DropdownNotebookTab.BACKUP_FOLDER -> {
                            // Strictly only notebooks from user-selected device backup folders (excluding SYSTEM_BACKUP)
                            val externalFolderBackups = remember(backupNotebooks, activeNotebook?.id) {
                                val activeId = activeNotebook?.id ?: ""
                                backupNotebooks
                                    .filter { it.sourceType == BackupNotebookSourceType.EXTERNAL_FOLDER }
                                    .map { entry ->
                                        val cleanName = com.example.util.ExportSharingManager.stripNotebookPrefix(entry.displayName)
                                            .replace(duplicateSuffix, "")
                                            .trim()
                                            .ifBlank { entry.displayName }
                                        if (cleanName != entry.displayName) entry.copy(displayName = cleanName) else entry
                                    }
                                    .groupBy { it.displayName.trim().lowercase(java.util.Locale.ROOT) }
                                    .values
                                    .mapNotNull { candidates ->
                                        candidates.maxByOrNull { it.wordCount.toLong() * 1000L + (it.lastModified / 1000L) }
                                    }
                                    .sortedWith(
                                        compareByDescending<BackupNotebookEntry> {
                                            it.notebookId.equals(activeId, ignoreCase = true) ||
                                                it.displayName.equals(activeId, ignoreCase = true)
                                        }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.displayName.trim() }
                                    )
                            }

                            if (externalFolderBackups.isEmpty()) {
                                Text(
                                    text = strings.backupNotebooksEmpty,
                                    fontSize = 12.sp,
                                    color = palette.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                )
                            } else {
                                externalFolderBackups.forEach { entry ->
                                    val themeColor = remember(entry.themeColorId) {
                                        AppThemePackage.fromId(entry.themeColorId).primary
                                    }

                                    DropdownMenuItem(
                                        modifier = Modifier.testTag("dropdown_backup_notebook_item_${entry.displayName}"),
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(10.dp)
                                                        .clip(CircleShape)
                                                        .background(themeColor)
                                                )

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = entry.displayName,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = palette.onSurface,
                                                        maxLines = 1,
                                                        softWrap = false,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = strings.wordsCount(entry.wordCount),
                                                        fontSize = 11.sp,
                                                        color = palette.onSurfaceVariant,
                                                        maxLines = 1,
                                                        softWrap = false
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            expanded = false
                                            onPromoteBackupNotebook(entry)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(thickness = 0.8.dp, color = palette.divider)

                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = palette.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = strings.newNotebookButton,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = palette.primary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        },
                        onClick = {
                            expanded = false
                            onCreateNotebookClick()
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = palette.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = strings.manageNotebooksButton,
                                    fontSize = 14.sp,
                                    color = palette.onSurface,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        },
                        onClick = {
                            expanded = false
                            onManageNotebooksClick()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Dialog to create a new notebook.
 */
@Composable
fun CreateNotebookDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, themeColorId: String, themeMode: String) -> Unit,
    appLanguage: String = "de"
) {
    var notebookName by remember { mutableStateOf("") }
    var selectedThemeId by remember { mutableStateOf("Dunkelblau") }
    var selectedThemeMode by remember { mutableStateOf("auto") }
    val strings = remember(appLanguage) { AppStrings.get(appLanguage) }
    val palette = rememberNotebookDialogPalette()

    CompositionLocalProvider(LocalLayoutDirection provides strings.language.layoutDirection) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = palette.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = strings.createNotebookTitle,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.onSurface,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = strings.createNotebookPrompt,
                        fontSize = 13.sp,
                        color = palette.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )

                    OutlinedTextField(
                        value = notebookName,
                        onValueChange = { notebookName = it },
                        label = {
                            Text(
                                text = strings.notebookNameFieldLabel,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        singleLine = true,
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create_notebook_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = palette.primary,
                            unfocusedBorderColor = palette.cardBorder,
                            focusedLabelColor = palette.primary,
                            cursorColor = palette.primary
                        )
                    )

                    // Theme Color Options
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = strings.notebookColorLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.onSurface,
                            maxLines = 1,
                            softWrap = false
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppThemePackage.M3_THEMES.forEach { theme ->
                                val isSelected = theme.id.equals(selectedThemeId, ignoreCase = true)
                                val checkTint = if (theme.swatch.luminance() > 0.48f) {
                                    palette.onSurface
                                } else {
                                    palette.onPrimary
                                }
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(theme.swatch)
                                        .border(
                                            width = if (isSelected) 2.5.dp else 1.dp,
                                            color = if (isSelected) palette.primary else palette.cardBorder,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedThemeId = theme.id },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = checkTint,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Theme Mode Selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = strings.themeModeTitle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.onSurface,
                            maxLines = 1,
                            softWrap = false
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                Triple("auto", strings.themeModeAuto, "System"),
                                Triple("light", strings.themeModeLight, "Hell"),
                                Triple("dark", strings.themeModeDark, "Dunkel")
                            ).forEach { (modeKey, modeTitle, _) ->
                                val isSelected = when (modeKey) {
                                    "light" -> selectedThemeMode.equals("light", ignoreCase = true) || selectedThemeMode.equals("hell", ignoreCase = true)
                                    "dark" -> selectedThemeMode.equals("dark", ignoreCase = true) || selectedThemeMode.equals("dunkel", ignoreCase = true)
                                    else -> selectedThemeMode.equals("auto", ignoreCase = true) || selectedThemeMode.equals("system", ignoreCase = true) || selectedThemeMode.isBlank()
                                }
                                Surface(
                                    onClick = { selectedThemeMode = modeKey },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) palette.primaryContainer else palette.cardSurface,
                                    border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) palette.primary else palette.cardBorder),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                                    ) {
                                        Text(
                                            text = modeTitle,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) palette.onPrimaryContainer else palette.onSurface,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                UnifiedTwoButtonBar(
                    cancelLabel = strings.cancel,
                    confirmLabel = strings.confirm,
                    onCancel = onDismiss,
                    onConfirm = {
                        val name = notebookName.trim().ifBlank { "Neues Notizbuch" }
                        onCreate(name, selectedThemeId, selectedThemeMode)
                    },
                    palette = palette,
                    confirmEnabled = notebookName.isNotBlank()
                )
            },
            containerColor = palette.dialogSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

/**
 * Single-line text that automatically decreases its font size down to [minFontSize]
 * if the text would otherwise overflow its container width, preventing line wrapping or abrupt truncation.
 */
@Composable
fun AutoSizingText(
    text: String,
    modifier: Modifier = Modifier,
    maxFontSize: TextUnit = 12.sp,
    minFontSize: TextUnit = 10.sp,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    step: Float = 0.5f
) {
    var scaledFontSize by remember(text, maxFontSize) { mutableStateOf(maxFontSize) }
    var shrinkSteps by remember(text, maxFontSize) { mutableStateOf(0) }
    var readyToDraw by remember(text, maxFontSize) { mutableStateOf(false) }

    Text(
        text = text,
        modifier = modifier.drawWithContent {
            if (readyToDraw) drawContent()
        },
        fontSize = scaledFontSize,
        color = color,
        fontWeight = fontWeight,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { textLayoutResult ->
            if (!readyToDraw && textLayoutResult.didOverflowWidth && scaledFontSize.value > minFontSize.value && shrinkSteps < 8) {
                val nextVal = (scaledFontSize.value - step).coerceAtLeast(minFontSize.value)
                if (nextVal < scaledFontSize.value) {
                    shrinkSteps += 1
                    scaledFontSize = nextVal.sp
                } else {
                    readyToDraw = true
                }
            } else if (!readyToDraw) {
                readyToDraw = true
            }
        }
    )
}



/**
 * Dialog offering Save or Share choice for exported JSON.
 */
/**
 * Dialog offering Save or Share choice for exported JSON.
 * Redesigned for high visual elegance, balanced layout, and full trilingual localization.
 */
@Composable
fun JsonExportActionDialog(
    fileName: String,
    title: String,
    onSaveToFile: () -> Unit,
    onShare: () -> Unit,
    onDismiss: () -> Unit,
    appLanguage: String = "de"
) {
    val strings = remember(appLanguage) { AppStrings.get(appLanguage) }
    val palette = rememberNotebookDialogPalette()

    // Dynamic clean localized title
    val displayTitle = when {
        title.contains("Alle", ignoreCase = true) || title.contains("All", ignoreCase = true) || title.contains("جميع", ignoreCase = true) ->
            strings.exportAllCombinedTitle
        title.contains("Ausgewählte", ignoreCase = true) || title.contains("Selected", ignoreCase = true) || title.contains("المحددة", ignoreCase = true) ->
            strings.exportSelectedTitle
        title.contains("Notizbuch exportieren", ignoreCase = true) || title.contains("Export Notebook", ignoreCase = true) || title.contains("تصدير دفتر", ignoreCase = true) -> {
            val nbName = title.substringAfter(":", "").trim().ifBlank { fileName.removeSuffix(".json") }
            "${strings.exportNotebookJsonAction}: $nbName"
        }
        else -> title.ifBlank { strings.jsonExportDialogTitle }
    }

    CompositionLocalProvider(LocalLayoutDirection provides strings.language.layoutDirection) {
        AlertDialog(
            onDismissRequest = onDismiss,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("json_export_action_dialog"),
            shape = RoundedCornerShape(16.dp),
            containerColor = palette.dialogSurface,
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(palette.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = null,
                            tint = palette.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(
                        text = displayTitle,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.onSurface,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // File summary card
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = palette.cardSurface,
                        border = BorderStroke(1.dp, palette.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(palette.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = palette.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = fileName,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = palette.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "JSON • UTF-8",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = palette.primary
                                )
                            }
                        }
                    }

                    // Guidance text
                    Text(
                        text = strings.exportActionDialogMessage,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = palette.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onShare,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, palette.cardBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = palette.onSurface
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = palette.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            AutoSizingText(
                                text = strings.share,
                                maxFontSize = 12.5.sp,
                                minFontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = palette.onSurface
                            )
                        }

                        Button(
                            onClick = onSaveToFile,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.primary,
                                contentColor = palette.onPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                tint = palette.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            AutoSizingText(
                                text = strings.confirm,
                                maxFontSize = 12.5.sp,
                                minFontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = palette.onPrimary
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, palette.cardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.onSurface),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        AutoSizingText(
                            text = strings.cancel,
                            maxFontSize = 12.5.sp,
                            minFontSize = 9.5.sp,
                            color = palette.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            dismissButton = null
        )
    }
}

/**
 * Dialog for selecting specific notebooks to export (Option 3).
 */
@Composable
fun SelectNotebooksExportDialog(
    notebooks: List<Notebook>,
    onDismiss: () -> Unit,
    onExportCombined: (selectedIds: List<String>) -> Unit,
    onExportSeparate: (selectedIds: List<String>) -> Unit,
    appLanguage: String = "de"
) {
    var selectedIds by remember { mutableStateOf(notebooks.map { it.id }.toSet()) }
    var exportMode by remember { mutableStateOf("combined") } // "combined" or "separate"
    val strings = remember(appLanguage) { AppStrings.get(appLanguage) }
    val palette = rememberNotebookDialogPalette()

    CompositionLocalProvider(LocalLayoutDirection provides strings.language.layoutDirection) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Checklist,
                        contentDescription = null,
                        tint = palette.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = strings.selectedNotebooksDialogTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.onSurface
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val areAllSelected = selectedIds.size == notebooks.size && notebooks.isNotEmpty()
                        OutlinedButton(
                            onClick = {
                                selectedIds = if (areAllSelected) emptySet() else notebooks.map { it.id }.toSet()
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, palette.cardBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.onSurface),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            AutoSizingText(
                                text = strings.selectAll,
                                maxFontSize = 12.sp,
                                minFontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = palette.onSurface
                            )
                        }
                    }

                    // List of notebooks with checkboxes inside bordered container
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = palette.sectionSurface,
                        border = BorderStroke(1.dp, palette.sectionBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(notebooks, key = { it.id }) { nb ->
                                val isChecked = selectedIds.contains(nb.id)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isChecked) palette.primaryContainer.copy(alpha = 0.35f) else Color.Transparent)
                                        .border(
                                            width = if (isChecked) 1.5.dp else 1.dp,
                                            color = if (isChecked) palette.primary else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            selectedIds = if (isChecked) {
                                                selectedIds - nb.id
                                            } else {
                                                selectedIds + nb.id
                                            }
                                        }
                                        .padding(vertical = 6.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            selectedIds = if (checked) {
                                                selectedIds + nb.id
                                            } else {
                                                selectedIds - nb.id
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = palette.primary),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = nb.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = palette.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = strings.wordsCount(nb.wordCount),
                                            fontSize = 10.5.sp,
                                            color = palette.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Export mode selection
                    Text(
                        text = strings.exportFormatLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.onSurface,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Surface(
                        onClick = { exportMode = "combined" },
                        shape = RoundedCornerShape(10.dp),
                        color = if (exportMode == "combined") palette.primaryContainer.copy(alpha = 0.3f) else palette.cardSurface,
                        border = BorderStroke(
                            if (exportMode == "combined") 1.5.dp else 1.dp,
                            if (exportMode == "combined") palette.primary else palette.cardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = exportMode == "combined",
                                onClick = { exportMode = "combined" },
                                colors = RadioButtonDefaults.colors(selectedColor = palette.primary),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = strings.exportFormatCombined,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = palette.onSurface
                                )
                                Text(
                                    text = strings.exportFormatCombinedSubtitle,
                                    fontSize = 10.sp,
                                    color = palette.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Surface(
                        onClick = { exportMode = "separate" },
                        shape = RoundedCornerShape(10.dp),
                        color = if (exportMode == "separate") palette.primaryContainer.copy(alpha = 0.3f) else palette.cardSurface,
                        border = BorderStroke(
                            if (exportMode == "separate") 1.5.dp else 1.dp,
                            if (exportMode == "separate") palette.primary else palette.cardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = exportMode == "separate",
                                onClick = { exportMode = "separate" },
                                colors = RadioButtonDefaults.colors(selectedColor = palette.primary),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = strings.exportFormatSeparate,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = palette.onSurface
                                )
                                Text(
                                    text = strings.exportFormatSeparateSubtitle,
                                    fontSize = 10.sp,
                                    color = palette.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                UnifiedTwoButtonBar(
                    cancelLabel = strings.cancel,
                    confirmLabel = strings.confirm,
                    onCancel = onDismiss,
                    onConfirm = {
                        val list = selectedIds.toList()
                        if (exportMode == "combined") {
                            onExportCombined(list)
                        } else {
                            onExportSeparate(list)
                        }
                    },
                    palette = palette,
                    confirmEnabled = selectedIds.isNotEmpty()
                )
            },
            containerColor = palette.dialogSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

/**
 * Dialog when importing a single notebook from JSON.
 */
@Composable
fun ImportSingleChoiceDialog(
    detectedName: String,
    wordCount: Int,
    onMergeIntoActive: () -> Unit,
    onCreateAsNew: (name: String) -> Unit,
    onDismiss: () -> Unit,
    appLanguage: String = "de"
) {
    var notebookName by remember { mutableStateOf(detectedName) }
    val strings = remember(appLanguage) { AppStrings.get(appLanguage) }
    val palette = rememberNotebookDialogPalette()

    CompositionLocalProvider(LocalLayoutDirection provides strings.language.layoutDirection) {
        AlertDialog(
            onDismissRequest = onDismiss,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("import_single_choice_dialog"),
            icon = {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = null,
                    tint = palette.primary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = strings.importSingleTitle,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.onSurface,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = strings.importSingleMessage(wordCount),
                        fontSize = 14.sp,
                        color = palette.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )

                    OutlinedTextField(
                        value = notebookName,
                        onValueChange = { notebookName = it },
                        label = {
                            Text(
                                text = strings.nameForNewNotebookLabel,
                                maxLines = 1,
                                softWrap = false
                            )
                        },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 16.sp,
                            letterSpacing = 0.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        singleLine = true,
                        maxLines = 1,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = palette.primary,
                            unfocusedBorderColor = palette.cardBorder,
                            focusedTextColor = palette.onSurface,
                            unfocusedTextColor = palette.onSurface,
                            focusedLabelColor = palette.primary,
                            cursorColor = palette.primary,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onCreateAsNew(notebookName.trim().ifBlank { detectedName }) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, palette.cardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = palette.onSurface
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        AutoSizingText(
                            text = strings.importAsNewNotebookButton,
                            maxFontSize = 14.sp,
                            minFontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.onSurface
                        )
                    }

                    OutlinedButton(
                        onClick = onMergeIntoActive,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, palette.cardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = palette.onSurface
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        AutoSizingText(
                            text = strings.mergeIntoActiveNotebookButton,
                            maxFontSize = 14.sp,
                            minFontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.onSurface
                        )
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, palette.cardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.onSurface),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        AutoSizingText(
                            text = strings.cancel,
                            maxFontSize = 13.sp,
                            minFontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.onSurface
                        )
                    }
                }
            },
            containerColor = palette.dialogSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

/**
 * Dialog when importing multiple notebooks from a combined JSON.
 */
@Composable
fun ImportMultiChoiceDialog(
    notebookNames: List<String>,
    totalWordCount: Int,
    fallbackName: String,
    onImportAllSeparate: () -> Unit,
    onImportAsSingle: (name: String) -> Unit,
    onDismiss: () -> Unit,
    appLanguage: String = "de"
) {
    var singleNotebookName by remember { mutableStateOf(fallbackName) }
    val strings = remember(appLanguage) { AppStrings.get(appLanguage) }
    val palette = rememberNotebookDialogPalette()

    CompositionLocalProvider(LocalLayoutDirection provides strings.language.layoutDirection) {
        AlertDialog(
            onDismissRequest = onDismiss,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("import_multi_choice_dialog"),
            icon = {
                Icon(
                    imageVector = Icons.Default.FolderZip,
                    contentDescription = null,
                    tint = palette.primary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = strings.importMultiTitle,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.onSurface,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AutoSizingText(
                        text = strings.importMultiMessage(notebookNames.size, totalWordCount, notebookNames.take(4)),
                        maxFontSize = 13.sp,
                        minFontSize = 10.5.sp,
                        color = palette.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = singleNotebookName,
                        onValueChange = { singleNotebookName = it },
                        label = {
                            Text(
                                text = strings.nameIfSingleCombinedLabel,
                                maxLines = 1,
                                softWrap = false
                            )
                        },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 16.sp,
                            letterSpacing = 0.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        singleLine = true,
                        maxLines = 1,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = palette.primary,
                            unfocusedBorderColor = palette.cardBorder,
                            focusedTextColor = palette.onSurface,
                            unfocusedTextColor = palette.onSurface,
                            focusedLabelColor = palette.primary,
                            cursorColor = palette.primary,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onImportAllSeparate,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, palette.cardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = palette.onSurface
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        AutoSizingText(
                            text = strings.importAllSeparateButton(notebookNames.size),
                            maxFontSize = 14.sp,
                            minFontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.onSurface
                        )
                    }

                    OutlinedButton(
                        onClick = { onImportAsSingle(singleNotebookName.trim().ifBlank { fallbackName }) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, palette.cardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = palette.onSurface
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        AutoSizingText(
                            text = strings.importAsOneCombinedButton,
                            maxFontSize = 14.sp,
                            minFontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.onSurface
                        )
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, palette.cardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.onSurface),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        AutoSizingText(
                            text = strings.cancel,
                            maxFontSize = 13.sp,
                            minFontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.onSurface
                        )
                    }
                }
            },
            containerColor = palette.dialogSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

/**
 * Dialog presented after importing a notebook with settings, prompting the user
 * to configure their API key for the AI service or skip.
 */
@Composable
fun PostImportApiKeyDialog(
    notebookName: String,
    provider: String,
    modelId: String,
    onSaveApiKey: (apiKey: String) -> Unit,
    onOpenFullSettings: () -> Unit,
    onSkip: () -> Unit,
    appLanguage: String = "de"
) {
    var keyInput by remember { mutableStateOf("") }
    val strings = remember(appLanguage) { AppStrings.get(appLanguage) }
    val palette = rememberNotebookDialogPalette()

    CompositionLocalProvider(LocalLayoutDirection provides strings.language.layoutDirection) {
        AlertDialog(
            onDismissRequest = onSkip,
            icon = {
                Icon(
                    imageVector = Icons.Default.VpnKey,
                    contentDescription = null,
                    tint = palette.primary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = strings.postImportKeyTitle,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.onSurface
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = strings.postImportKeyPrompt(notebookName),
                        fontSize = 12.5.sp,
                        color = palette.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = palette.cardSurface,
                        border = BorderStroke(1.dp, palette.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = palette.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "$provider • $modelId",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = palette.onSurface
                            )
                        }
                    }

                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        label = { Text(strings.apiKeyPlaceholder) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = palette.primary,
                            unfocusedBorderColor = palette.cardBorder,
                            focusedLabelColor = palette.primary,
                            cursorColor = palette.primary
                        )
                    )

                    Text(
                        text = strings.postImportSecurityNote,
                        fontSize = 10.5.sp,
                        color = palette.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                UnifiedTwoButtonBar(
                    cancelLabel = strings.cancel,
                    confirmLabel = strings.confirm,
                    onCancel = onSkip,
                    onConfirm = {
                        if (keyInput.isNotBlank()) {
                            onSaveApiKey(keyInput.trim())
                        } else {
                            onOpenFullSettings()
                        }
                    },
                    palette = palette
                )
            },
            containerColor = palette.dialogSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

