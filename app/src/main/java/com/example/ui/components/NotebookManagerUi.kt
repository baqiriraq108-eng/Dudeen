package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.notebook.BackupFolder
import com.example.data.notebook.BackupNotebookEntry
import com.example.data.notebook.Notebook
import com.example.data.notebook.NotebookBackupManager
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.SettingsStrings
import com.example.ui.theme.AppThemePackage
import com.example.ui.theme.GeoBannerGradient
import com.example.ui.theme.GeoCardRibbonColor
import com.example.ui.theme.GeoIsBoldTheme
import com.example.ui.theme.GeoOnTier1Container
import com.example.ui.theme.GeoOnTier2Container
import com.example.ui.theme.GeoTier1Container
import com.example.ui.theme.GeoTier2Container
import com.example.ui.theme.GeoTier4Border
import com.example.ui.theme.GeoTierPrimary
import com.example.ui.theme.GeoTierTertiary
import com.example.ui.theme.LocalIsDarkTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Single unified palette & styling system for the Notebook Manager and its sub-dialogs.
 * Resolves every surface, border, pill tab, badge, and status role strictly from
 * Deklo's active Material 3 ColorScheme & 4-Tier Color Hierarchy to prevent visual anomalies.
 */
@Immutable
data class NotebookDialogPalette(
    val dialogSurface: Color,
    val dialogBorder: Color,
    val dialogBorderWidth: Dp,
    val sectionSurface: Color,
    val sectionBorder: Color,
    val cardSurface: Color,
    val cardSurfaceSubtle: Color,
    val cardBorder: Color,
    val pillTrackSurface: Color,
    val pillTrackBorder: Color,
    val pillSelectedSurface: Color,
    val pillSelectedBorder: Color,
    val pillSelectedContent: Color,
    val pillSelectedIcon: Color,
    val pillSelectedBadgeSurface: Color,
    val pillSelectedBadgeText: Color,
    val pillUnselectedContent: Color,
    val pillUnselectedBadgeSurface: Color,
    val pillUnselectedBadgeText: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val surface: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val divider: Color,
    val activeStatus: Color,
    val error: Color,
    val errorContainer: Color,
    val onError: Color,
    val isBoldTheme: Boolean,
    val bannerGradient: List<Color>
)

@Composable
fun rememberNotebookDialogPalette(): NotebookDialogPalette {
    val cs = MaterialTheme.colorScheme
    val isBold = GeoIsBoldTheme
    val ribbon = GeoCardRibbonColor
    val gradient = GeoBannerGradient
    val tier1Primary = GeoTierPrimary
    val tier3Tertiary = GeoTierTertiary
    val tier1Container = GeoTier1Container
    val onTier1Container = GeoOnTier1Container
    val tier2Container = GeoTier2Container
    val onTier2Container = GeoOnTier2Container
    val tier4Border = GeoTier4Border

    return NotebookDialogPalette(
        dialogSurface = cs.surfaceContainer,
        dialogBorder = if (isBold) ribbon.copy(alpha = 0.55f) else tier4Border,
        dialogBorderWidth = if (isBold) 1.5.dp else 1.dp,
        sectionSurface = cs.surfaceContainerLow,
        sectionBorder = tier4Border,
        cardSurface = cs.surfaceContainerHigh,
        cardSurfaceSubtle = cs.surfaceContainer,
        cardBorder = tier4Border,
        pillTrackSurface = cs.surfaceContainerLowest,
        pillTrackBorder = tier4Border.copy(alpha = 0.8f),
        pillSelectedSurface = tier1Container,
        pillSelectedBorder = tier1Primary.copy(alpha = 0.28f),
        pillSelectedContent = onTier1Container,
        pillSelectedIcon = tier1Primary,
        pillSelectedBadgeSurface = cs.surface,
        pillSelectedBadgeText = tier1Primary,
        pillUnselectedContent = cs.onSurfaceVariant,
        pillUnselectedBadgeSurface = cs.surfaceContainerHigh,
        pillUnselectedBadgeText = cs.onSurfaceVariant,
        primary = tier1Primary,
        onPrimary = cs.onPrimary,
        primaryContainer = tier1Container,
        onPrimaryContainer = onTier1Container,
        secondaryContainer = tier2Container,
        onSecondaryContainer = onTier2Container,
        surface = cs.surface,
        onSurface = cs.onSurface,
        onSurfaceVariant = cs.onSurfaceVariant,
        divider = tier4Border.copy(alpha = 0.45f),
        activeStatus = tier3Tertiary,
        error = cs.error,
        errorContainer = cs.errorContainer,
        onError = cs.onError,
        isBoldTheme = isBold,
        bannerGradient = gradient
    )
}

private enum class NotebookManagerSection {
    NOTEBOOKS,
    BACKUPS,
    EXPORT
}

private enum class NotebookStorageTab {
    PRIMARY,
    RESERVE,
    BACKUP
}

@Immutable
data class SegmentedPillItem(
    val id: String,
    val label: String,
    val count: Int? = null,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

/**
 * Reusable Deklo-adapted segmented pill tab bar (rounded outer track, elevated tonal active pill,
 * strictly icon-only with zero text labels and zero numeric badges, preserving accessibility contentDescription).
 */
@Composable
fun UnifiedSegmentedPillBar(
    items: List<SegmentedPillItem>,
    selectedId: String,
    onSelect: (String) -> Unit,
    palette: NotebookDialogPalette,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(50),
        color = palette.pillTrackSurface,
        border = BorderStroke(1.dp, palette.pillTrackBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = item.id == selectedId
                val bgColor = if (isSelected) palette.pillSelectedSurface else Color.Transparent
                val borderStroke = if (isSelected) BorderStroke(1.dp, palette.pillSelectedBorder) else null
                val iconTint = if (isSelected) palette.pillSelectedIcon else palette.pillUnselectedContent

                Surface(
                    onClick = { onSelect(item.id) },
                    shape = RoundedCornerShape(50),
                    color = bgColor,
                    border = borderStroke,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp)
                        .testTag(item.testTag)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                            tint = iconTint,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Redesigned Notebook Manager Dialog:
 * - Top section tabs cleanly separate **Notebooks**, **Backups**, and **Export** (icon-only, zero vertical stacking).
 * - Inside **Notebooks**, a compact quick-action row sits above a dedicated bordered Notebooks window
 *   with three icon-only segmented storage tabs (**Primary**, **Reserve**, and **Backup**) styled in Deklo's 4-tier color palette.
 * - Preserves the exact 72.dp notebook item card design cleanly across all listings.
 */
@Composable
fun NotebookManagerDialog(
    notebooks: List<Notebook>,
    activeNotebookId: String,
    onDismiss: () -> Unit,
    onSelectNotebook: (String) -> Unit,
    onCreateNotebook: () -> Unit,
    onRenameNotebook: (id: String, newName: String) -> Unit,
    onDeleteNotebook: (id: String) -> Unit,
    onExportJson: (notebookId: String) -> Unit,
    onImportJson: (uri: Uri) -> Unit,
    onExportAllCombined: () -> Unit,
    onExportAllSeparate: () -> Unit,
    onExportSelectedClick: () -> Unit,
    backupFolders: List<BackupFolder> = emptyList(),
    backupNotebooks: List<BackupNotebookEntry> = emptyList(),
    isLoadingBackupNotebooks: Boolean = false,
    isBackingUp: Boolean = false,
    onAddBackupFolder: (Uri) -> Unit = {},
    onSetPrimaryBackupFolder: (String) -> Unit = {},
    onRemoveBackupFolder: (String) -> Unit = {},
    onBackupNow: () -> Unit = {},
    onPromoteBackupNotebook: (BackupNotebookEntry) -> Unit = {},
    onExportBackupNotebookJson: (BackupNotebookEntry) -> Unit = {},
    appLanguage: String = "de"
) {
    var selectedSection by remember { mutableStateOf(NotebookManagerSection.NOTEBOOKS) }
    var selectedStorageTab by remember { mutableStateOf(NotebookStorageTab.PRIMARY) }

    var notebookToRename by remember { mutableStateOf<Notebook?>(null) }
    var notebookToDelete by remember { mutableStateOf<Notebook?>(null) }
    var folderToSetPrimary by remember { mutableStateOf<BackupFolder?>(null) }
    var backupToPromote by remember { mutableStateOf<BackupNotebookEntry?>(null) }

    val strings = remember(appLanguage) { AppStrings.get(appLanguage) }
    val palette = rememberNotebookDialogPalette()

    val jsonPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onImportJson(uri)
        }
    }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            onAddBackupFolder(uri)
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides strings.language.layoutDirection) {
        val configuration = LocalConfiguration.current
        val maxDialogHeight = remember(configuration.screenHeightDp) { (configuration.screenHeightDp * 0.88f).dp }

        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides strings.language.layoutDirection) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .heightIn(max = maxDialogHeight)
                        .testTag("notebook_manager_dialog"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = palette.dialogSurface),
                    border = BorderStroke(palette.dialogBorderWidth, palette.dialogBorder)
                ) {
                    if (palette.isBoldTheme) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.5.dp)
                                .background(Brush.horizontalGradient(palette.bannerGradient))
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Dialog Header (No notebook/storage count numbers)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(palette.primaryContainer)
                                        .border(1.dp, palette.primary.copy(alpha = 0.25f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = palette.primary,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }

                                Text(
                                    text = strings.notebookManagerTitle,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.onSurface,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("close_notebook_manager_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = strings.close,
                                    tint = palette.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Top-Level Section Navigation (Notebooks | Backups | Export) - Strictly Icon-Only
                        UnifiedSegmentedPillBar(
                            items = listOf(
                                SegmentedPillItem(
                                    id = NotebookManagerSection.NOTEBOOKS.name,
                                    label = strings.managerTabNotebooks,
                                    count = null,
                                    selectedIcon = Icons.AutoMirrored.Filled.MenuBook,
                                    unselectedIcon = Icons.AutoMirrored.Outlined.MenuBook,
                                    testTag = "manager_section_notebooks"
                                ),
                                SegmentedPillItem(
                                    id = NotebookManagerSection.BACKUPS.name,
                                    label = strings.managerTabBackups,
                                    count = null,
                                    selectedIcon = Icons.Filled.FolderZip,
                                    unselectedIcon = Icons.Outlined.FolderZip,
                                    testTag = "manager_section_backups"
                                ),
                                SegmentedPillItem(
                                    id = NotebookManagerSection.EXPORT.name,
                                    label = strings.managerTabExport,
                                    count = null,
                                    selectedIcon = Icons.Filled.UploadFile,
                                    unselectedIcon = Icons.Outlined.UploadFile,
                                    testTag = "manager_section_export"
                                )
                            ),
                            selectedId = selectedSection.name,
                            onSelect = { id ->
                                selectedSection = NotebookManagerSection.valueOf(id)
                            },
                            palette = palette
                        )

                        // Dedicated Workspace Body (No stacked piling)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                        ) {
                            AnimatedContent(
                                targetState = selectedSection,
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(150))
                                },
                                label = "notebook_manager_section"
                            ) { section ->
                                when (section) {
                                    NotebookManagerSection.NOTEBOOKS -> {
                                        NotebooksWorkspaceSection(
                                            notebooks = notebooks,
                                            activeNotebookId = activeNotebookId,
                                            backupNotebooks = backupNotebooks,
                                            isLoadingBackupNotebooks = isLoadingBackupNotebooks,
                                            selectedStorageTab = selectedStorageTab,
                                            onSelectStorageTab = { selectedStorageTab = it },
                                            onCreateNotebook = onCreateNotebook,
                                            onImportJsonClick = {
                                                jsonPickerLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                                            },
                                            onSelectNotebook = onSelectNotebook,
                                            onRenameRequest = { notebookToRename = it },
                                            onExportJson = onExportJson,
                                            onDeleteRequest = { notebookToDelete = it },
                                            onPromoteBackupRequest = { backupToPromote = it },
                                            onExportBackupNotebookJson = onExportBackupNotebookJson,
                                            palette = palette,
                                            strings = strings
                                        )
                                    }

                                    NotebookManagerSection.BACKUPS -> {
                                        BackupsWorkspaceSection(
                                            backupFolders = backupFolders,
                                            isBackingUp = isBackingUp,
                                            onAddFolderClick = { folderPickerLauncher.launch(null) },
                                            onSetPrimaryRequest = { folderToSetPrimary = it },
                                            onRemoveBackupFolder = onRemoveBackupFolder,
                                            onBackupNow = onBackupNow,
                                            palette = palette,
                                            strings = strings,
                                            appLanguage = appLanguage
                                        )
                                    }

                                    NotebookManagerSection.EXPORT -> {
                                        ExportWorkspaceSection(
                                            onExportAllCombined = onExportAllCombined,
                                            onExportAllSeparate = onExportAllSeparate,
                                            onExportSelectedClick = onExportSelectedClick,
                                            palette = palette,
                                            strings = strings
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

    // Rename Notebook Dialog
    if (notebookToRename != null) {
        val target = notebookToRename!!
        var newTitle by remember(target.id) { mutableStateOf(target.name) }

        CompositionLocalProvider(LocalLayoutDirection provides strings.language.layoutDirection) {
            AlertDialog(
                onDismissRequest = { notebookToRename = null },
                shape = RoundedCornerShape(16.dp),
                containerColor = palette.dialogSurface,
                title = {
                    Text(
                        text = strings.renameDialogTitle,
                        fontWeight = FontWeight.Bold,
                        color = palette.onSurface,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                text = {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = {
                            Text(
                                text = strings.renameDialogFieldLabel,
                                maxLines = 1,
                                softWrap = false
                            )
                        },
                        singleLine = true,
                        maxLines = 1,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = palette.primary,
                            unfocusedBorderColor = palette.cardBorder,
                            focusedLabelColor = palette.primary,
                            cursorColor = palette.primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    UnifiedTwoButtonBar(
                        cancelLabel = strings.cancel,
                        confirmLabel = strings.confirm,
                        onCancel = { notebookToRename = null },
                        onConfirm = {
                            val name = newTitle.trim()
                            if (name.isNotBlank()) {
                                onRenameNotebook(target.id, name)
                            }
                            notebookToRename = null
                        },
                        palette = palette
                    )
                }
            )
        }
    }

    // Delete Notebook Confirmation Dialog
    if (notebookToDelete != null) {
        val target = notebookToDelete!!
        CompositionLocalProvider(LocalLayoutDirection provides strings.language.layoutDirection) {
            AlertDialog(
                onDismissRequest = { notebookToDelete = null },
                shape = RoundedCornerShape(16.dp),
                containerColor = palette.dialogSurface,
                title = {
                    Text(
                        text = strings.deleteNotebookDialogTitle,
                        fontWeight = FontWeight.Bold,
                        color = palette.error,
                        maxLines = 1,
                        softWrap = false
                    )
                },
                text = {
                    Text(
                        text = strings.deleteNotebookDialogMessage(target.name),
                        fontSize = 13.sp,
                        color = palette.onSurfaceVariant
                    )
                },
                confirmButton = {
                    UnifiedTwoButtonBar(
                        cancelLabel = strings.cancel,
                        confirmLabel = strings.confirm,
                        onCancel = { notebookToDelete = null },
                        onConfirm = {
                            onDeleteNotebook(target.id)
                            notebookToDelete = null
                        },
                        palette = palette,
                        confirmIsDestructive = true
                    )
                }
            )
        }
    }

    // Set Primary Storage Confirmation Dialog
    if (folderToSetPrimary != null) {
        val targetFolder = folderToSetPrimary!!
        CompositionLocalProvider(LocalLayoutDirection provides strings.language.layoutDirection) {
            AlertDialog(
                onDismissRequest = { folderToSetPrimary = null },
                shape = RoundedCornerShape(16.dp),
                containerColor = palette.dialogSurface,
                title = {
                    Text(
                        text = strings.setPrimaryConfirmTitle,
                        fontWeight = FontWeight.Bold,
                        color = palette.onSurface,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                text = {
                    Text(
                        text = strings.setPrimaryConfirmMessage(targetFolder.displayName),
                        fontSize = 13.sp,
                        color = palette.onSurfaceVariant
                    )
                },
                confirmButton = {
                    UnifiedTwoButtonBar(
                        cancelLabel = strings.cancel,
                        confirmLabel = strings.confirm,
                        onCancel = { folderToSetPrimary = null },
                        onConfirm = {
                            onSetPrimaryBackupFolder(targetFolder.uriString)
                            folderToSetPrimary = null
                        },
                        palette = palette
                    )
                }
            )
        }
    }

    // Promote Backup Notebook Confirmation Dialog (Fresh Baseline Rule)
    if (backupToPromote != null) {
        val targetEntry = backupToPromote!!
        CompositionLocalProvider(LocalLayoutDirection provides strings.language.layoutDirection) {
            AlertDialog(
                onDismissRequest = { backupToPromote = null },
                shape = RoundedCornerShape(16.dp),
                containerColor = palette.dialogSurface,
                icon = {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(palette.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = null,
                            tint = palette.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = strings.promoteBackupConfirmTitle,
                        fontWeight = FontWeight.Bold,
                        color = palette.onSurface,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                text = {
                    Text(
                        text = strings.promoteBackupConfirmMessage(
                            targetEntry.displayName,
                            targetEntry.sourceLabel,
                            targetEntry.wordCount
                        ),
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = palette.onSurfaceVariant
                    )
                },
                confirmButton = {
                    UnifiedTwoButtonBar(
                        cancelLabel = strings.cancel,
                        confirmLabel = strings.confirm,
                        onCancel = { backupToPromote = null },
                        onConfirm = {
                            onPromoteBackupNotebook(targetEntry)
                            backupToPromote = null
                        },
                        palette = palette
                    )
                }
            )
        }
    }
}

@Composable
private fun NotebooksWorkspaceSection(
    notebooks: List<Notebook>,
    activeNotebookId: String,
    backupNotebooks: List<BackupNotebookEntry>,
    isLoadingBackupNotebooks: Boolean,
    selectedStorageTab: NotebookStorageTab,
    onSelectStorageTab: (NotebookStorageTab) -> Unit,
    onCreateNotebook: () -> Unit,
    onImportJsonClick: () -> Unit,
    onSelectNotebook: (String) -> Unit,
    onRenameRequest: (Notebook) -> Unit,
    onExportJson: (String) -> Unit,
    onDeleteRequest: (Notebook) -> Unit,
    onPromoteBackupRequest: (BackupNotebookEntry) -> Unit,
    onExportBackupNotebookJson: (BackupNotebookEntry) -> Unit,
    palette: NotebookDialogPalette,
    strings: SettingsStrings
) {
    // Separate backup snapshots into Secondary / Reserve Storage (internal System_Backups)
    // and System / Folder Backups (external backup folders) with normalized titles and clean ordering
    val duplicateSuffix = remember { Regex("""(?:\s*_\d+|\s*\(\d+\))+$""") }
    val reserveNotebooks = remember(backupNotebooks, activeNotebookId) {
        backupNotebooks
            .filter { it.sourceType == com.example.data.notebook.BackupNotebookSourceType.SYSTEM_BACKUP }
            .map { entry ->
                val cleanName = com.example.util.ExportSharingManager.stripNotebookPrefix(entry.displayName)
                    .replace(duplicateSuffix, "")
                    .trim()
                    .ifBlank { entry.displayName }
                if (cleanName != entry.displayName) entry.copy(displayName = cleanName) else entry
            }
            .groupBy { it.displayName.trim().lowercase(Locale.ROOT) }
            .values
            .mapNotNull { candidates ->
                candidates.maxByOrNull { it.wordCount.toLong() * 1000L + (it.lastModified / 1000L) }
            }
            .sortedWith(
                compareByDescending<BackupNotebookEntry> {
                    it.notebookId.equals(activeNotebookId, ignoreCase = true) ||
                        it.displayName.equals(activeNotebookId, ignoreCase = true)
                }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.displayName.trim() }
            )
    }
    val folderBackupNotebooks = remember(backupNotebooks, activeNotebookId) {
        backupNotebooks
            .filter { it.sourceType == com.example.data.notebook.BackupNotebookSourceType.EXTERNAL_FOLDER }
            .map { entry ->
                val cleanName = com.example.util.ExportSharingManager.stripNotebookPrefix(entry.displayName)
                    .replace(duplicateSuffix, "")
                    .trim()
                    .ifBlank { entry.displayName }
                if (cleanName != entry.displayName) entry.copy(displayName = cleanName) else entry
            }
            .groupBy { it.displayName.trim().lowercase(Locale.ROOT) }
            .values
            .mapNotNull { candidates ->
                candidates.maxByOrNull { it.wordCount.toLong() * 1000L + (it.lastModified / 1000L) }
            }
            .sortedWith(
                compareByDescending<BackupNotebookEntry> {
                    it.notebookId.equals(activeNotebookId, ignoreCase = true) ||
                        it.displayName.equals(activeNotebookId, ignoreCase = true)
                }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.displayName.trim() }
            )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Compact Horizontal Quick-Action Strip (New Book | JSON Import)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuickActionPillCard(
                icon = Icons.Default.Add,
                label = strings.newNotebookButton,
                onClick = onCreateNotebook,
                palette = palette,
                modifier = Modifier
                    .weight(1f)
                    .testTag("quick_new_notebook_button")
            )
            QuickActionPillCard(
                icon = Icons.Default.FileDownload,
                label = strings.loadFromJsonButton,
                onClick = onImportJsonClick,
                palette = palette,
                modifier = Modifier
                    .weight(1f)
                    .testTag("quick_import_json_button")
            )
        }

        // Dedicated Clearly Bordered Notebooks Window with Three Top Segmented Tabs:
        // 1. Primary Storage (Primär)
        // 2. Secondary / Reserve Storage (التخزين الاحتياطي - System_Backups - Shield Icon)
        // 3. System / Folder Backups (الباكاب - External Backup Folders - Local Folder Icon)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .testTag("bordered_notebooks_container"),
            shape = RoundedCornerShape(16.dp),
            color = palette.sectionSurface,
            border = BorderStroke(1.25.dp, palette.sectionBorder)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Three Icon-Only Segmented Pill Tabs at Top of Bordered Container
                UnifiedSegmentedPillBar(
                    items = listOf(
                        SegmentedPillItem(
                            id = NotebookStorageTab.PRIMARY.name,
                            label = strings.primaryNotebooksTab,
                            count = null,
                            selectedIcon = Icons.Filled.Book,
                            unselectedIcon = Icons.Outlined.Book,
                            testTag = "notebooks_tab_primary"
                        ),
                        SegmentedPillItem(
                            id = NotebookStorageTab.RESERVE.name,
                            label = strings.reserveNotebooksTab,
                            count = null,
                            selectedIcon = Icons.Filled.Shield,
                            unselectedIcon = Icons.Outlined.Shield,
                            testTag = "notebooks_tab_reserve"
                        ),
                        SegmentedPillItem(
                            id = NotebookStorageTab.BACKUP.name,
                            label = strings.backupNotebooksTab,
                            count = null,
                            selectedIcon = Icons.Filled.FolderSpecial,
                            unselectedIcon = Icons.Outlined.Folder,
                            testTag = "notebooks_tab_backup"
                        )
                    ),
                    selectedId = selectedStorageTab.name,
                    onSelect = { id ->
                        onSelectStorageTab(NotebookStorageTab.valueOf(id))
                    },
                    palette = palette,
                    modifier = Modifier.padding(10.dp)
                )

                HorizontalDivider(
                    thickness = 0.8.dp,
                    color = palette.divider
                )

                // Bounded Scrollable List Inside Container
                when (selectedStorageTab) {
                    NotebookStorageTab.PRIMARY -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false),
                            contentPadding = PaddingValues(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(notebooks, key = { it.id }) { notebook ->
                                PrimaryNotebookItemCard(
                                    notebook = notebook,
                                    isActive = notebook.id == activeNotebookId,
                                    canDelete = notebooks.size > 1,
                                    onSelect = { onSelectNotebook(notebook.id) },
                                    onRename = { onRenameRequest(notebook) },
                                    onExportJson = { onExportJson(notebook.id) },
                                    onDelete = { onDeleteRequest(notebook) },
                                    palette = palette,
                                    strings = strings
                                )
                            }
                        }
                    }

                    NotebookStorageTab.RESERVE -> {
                        val activeList = reserveNotebooks
                        if (isLoadingBackupNotebooks && activeList.isEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 28.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = palette.primary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = strings.backupNotebooksLoading,
                                    fontSize = 13.sp,
                                    color = palette.onSurfaceVariant
                                )
                            }
                        } else if (activeList.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp, vertical = 26.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Shield,
                                    contentDescription = null,
                                    tint = palette.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    text = strings.reserveNotebooksEmpty,
                                    fontSize = 12.5.sp,
                                    lineHeight = 18.sp,
                                    color = palette.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f, fill = false),
                                contentPadding = PaddingValues(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(activeList, key = { it.uniqueKey }) { entry ->
                                    BackupNotebookItemCard(
                                        entry = entry,
                                        onActivate = { onPromoteBackupRequest(entry) },
                                        onExportJson = { onExportBackupNotebookJson(entry) },
                                        palette = palette,
                                        strings = strings
                                    )
                                }
                            }
                        }
                    }

                    NotebookStorageTab.BACKUP -> {
                        val activeList = folderBackupNotebooks
                        if (isLoadingBackupNotebooks && activeList.isEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 28.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = palette.primary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = strings.backupNotebooksLoading,
                                    fontSize = 13.sp,
                                    color = palette.onSurfaceVariant
                                )
                            }
                        } else if (activeList.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp, vertical = 26.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.FolderOpen,
                                    contentDescription = null,
                                    tint = palette.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    text = strings.backupNotebooksEmpty,
                                    fontSize = 12.5.sp,
                                    lineHeight = 18.sp,
                                    color = palette.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f, fill = false),
                                contentPadding = PaddingValues(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(activeList, key = { it.uniqueKey }) { entry ->
                                    BackupNotebookItemCard(
                                        entry = entry,
                                        onActivate = { onPromoteBackupRequest(entry) },
                                        onExportJson = { onExportBackupNotebookJson(entry) },
                                        palette = palette,
                                        strings = strings
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
private fun QuickActionPillCard(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    palette: NotebookDialogPalette,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = palette.cardSurface,
        border = BorderStroke(1.dp, palette.cardBorder),
        modifier = modifier.heightIn(min = 48.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(palette.secondaryContainer)
                    .border(1.dp, palette.primary.copy(alpha = 0.45f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = palette.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            AutoSizingText(
                text = label,
                maxFontSize = 13.sp,
                minFontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Preserves the exact 72.dp Notebook Item Card design cleanly with unified theme resolution.
 */
@Composable
private fun PrimaryNotebookItemCard(
    notebook: Notebook,
    isActive: Boolean,
    canDelete: Boolean,
    onSelect: () -> Unit,
    onRename: () -> Unit,
    onExportJson: () -> Unit,
    onDelete: () -> Unit,
    palette: NotebookDialogPalette,
    strings: SettingsStrings
) {
    val isDark = LocalIsDarkTheme.current
    val theme = remember(notebook.settings.themeColorId) {
        AppThemePackage.fromId(notebook.settings.themeColorId)
    }
    val formattedDate = remember(notebook.lastModified) {
        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMAN).format(Date(notebook.lastModified))
    }
    var showMenu by remember { mutableStateOf(false) }

    val boldGradColors = remember(theme) {
        listOf(Color(theme.bannerGradientStartLightHex), Color(theme.bannerGradientEndLightHex))
    }
    val notebookAccent = remember(theme, isDark, palette.primary) {
        if (theme.isBoldTheme) Color(theme.cardAccentRibbonLightHex)
        else theme.tier1Primary(isDark)
    }
    val badgeContainer = remember(theme, isDark) {
        theme.tier2Container(isDark)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .then(
                if (!isActive) Modifier.clickable { onSelect() }
                else Modifier
            )
            .testTag("primary_notebook_card_${notebook.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = palette.cardSurface),
        border = BorderStroke(
            if (isActive) 2.dp else 1.dp,
            if (isActive) notebookAccent else palette.cardBorder
        )
    ) {
        if (theme.isBoldTheme) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .background(Brush.horizontalGradient(boldGradColors))
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Leading 40.dp Icon Badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(badgeContainer)
                    .border(
                        width = if (isActive) 1.5.dp else 1.dp,
                        color = notebookAccent.copy(alpha = if (isActive) 0.9f else 0.55f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = notebookAccent,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Center 2-Line Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = notebook.name,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) palette.primary else palette.onSurface,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "📚 ${strings.wordsCount(notebook.wordCount)}",
                    fontSize = 12.sp,
                    color = palette.onSurfaceVariant,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Trailing Action Slot: [Aktiv / Aktivieren Pill] + [3-dot Menu]
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isActive) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.5.dp, notebookAccent)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = notebookAccent,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = strings.activeNotebookBadge,
                                color = notebookAccent,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                } else {
                    Surface(
                        onClick = onSelect,
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, palette.cardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = strings.activateNotebookAction,
                                color = palette.primary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = null,
                            tint = palette.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        shape = RoundedCornerShape(14.dp),
                        containerColor = palette.dialogSurface,
                        border = BorderStroke(1.dp, palette.cardBorder)
                    ) {
                        DropdownMenuItem(
                            text = { Text(strings.renameNotebookAction, fontSize = 13.sp, color = palette.onSurface) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = palette.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            onClick = {
                                showMenu = false
                                onRename()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text(strings.exportNotebookJsonAction, fontSize = 13.sp, color = palette.onSurface) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.UploadFile,
                                    contentDescription = null,
                                    tint = palette.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            onClick = {
                                showMenu = false
                                onExportJson()
                            }
                        )

                        if (canDelete) {
                            DropdownMenuItem(
                                text = { Text(strings.delete, fontSize = 13.sp, color = palette.error) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = palette.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Backup Tab Item Card using the same 72.dp card blueprint, showing the snapshot's storage source
 * (`System_Backups` or selected backup folder) and allowing Fresh Baseline promotion via Activate.
 */
@Composable
private fun BackupNotebookItemCard(
    entry: BackupNotebookEntry,
    onActivate: () -> Unit,
    onExportJson: () -> Unit,
    palette: NotebookDialogPalette,
    strings: SettingsStrings
) {
    val isDark = LocalIsDarkTheme.current
    val theme = remember(entry.themeColorId) {
        AppThemePackage.fromId(entry.themeColorId)
    }
    val formattedDate = remember(entry.lastModified) {
        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMAN).format(Date(entry.lastModified))
    }
    var showMenu by remember { mutableStateOf(false) }

    val boldGradColors = remember(theme) {
        listOf(Color(theme.bannerGradientStartLightHex), Color(theme.bannerGradientEndLightHex))
    }
    val notebookAccent = remember(theme, isDark) {
        if (theme.isBoldTheme) Color(theme.cardAccentRibbonLightHex)
        else theme.tier1Primary(isDark)
    }
    val badgeContainer = remember(theme, isDark) {
        theme.tier2Container(isDark)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clickable { onActivate() }
            .testTag("backup_notebook_card_${entry.uniqueKey.hashCode()}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = palette.cardSurface),
        border = BorderStroke(1.dp, palette.cardBorder)
    ) {
        if (theme.isBoldTheme) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .background(Brush.horizontalGradient(boldGradColors))
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Leading 40.dp Icon Badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(badgeContainer)
                    .border(1.dp, notebookAccent.copy(alpha = 0.55f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = notebookAccent,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Center 2-Line Column (matches PrimaryNotebookItemCard layout cleanly without competing inline pills)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = entry.displayName,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.onSurface,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "📚 ${strings.wordsCount(entry.wordCount)}",
                    fontSize = 12.sp,
                    color = palette.onSurfaceVariant,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Trailing Action Slot: [Aktivieren Pill] + [3-dot Menu]
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    onClick = onActivate,
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, palette.cardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = strings.activateNotebookAction,
                            color = palette.primary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = null,
                            tint = palette.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        shape = RoundedCornerShape(14.dp),
                        containerColor = palette.dialogSurface,
                        border = BorderStroke(1.dp, palette.cardBorder)
                    ) {
                        DropdownMenuItem(
                            text = { Text(strings.activateNotebookAction, fontSize = 13.sp, color = palette.onSurface) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Restore,
                                    contentDescription = null,
                                    tint = palette.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            onClick = {
                                showMenu = false
                                onActivate()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text(strings.exportNotebookJsonAction, fontSize = 13.sp, color = palette.onSurface) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.UploadFile,
                                    contentDescription = null,
                                    tint = palette.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            onClick = {
                                showMenu = false
                                onExportJson()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BackupsWorkspaceSection(
    backupFolders: List<BackupFolder>,
    isBackingUp: Boolean,
    onAddFolderClick: () -> Unit,
    onSetPrimaryRequest: (BackupFolder) -> Unit,
    onRemoveBackupFolder: (String) -> Unit,
    onBackupNow: () -> Unit,
    palette: NotebookDialogPalette,
    strings: SettingsStrings,
    appLanguage: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = palette.sectionSurface,
        border = BorderStroke(1.25.dp, palette.sectionBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Section Title + Live Sync Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderZip,
                        contentDescription = null,
                        tint = palette.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = strings.backupFoldersTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.onSurface
                    )
                }

                if (isBackingUp) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = palette.primary
                        )
                        Text(
                            text = strings.backingUpStatus,
                            fontSize = 11.5.sp,
                            color = palette.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            HorizontalDivider(thickness = 0.8.dp, color = palette.divider)

            if (backupFolders.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = palette.cardSurface,
                    border = BorderStroke(1.dp, palette.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = palette.onSurfaceVariant.copy(alpha = 0.75f),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = strings.backupEmptyNotice,
                            fontSize = 12.sp,
                            color = palette.onSurfaceVariant,
                            lineHeight = 17.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(backupFolders, key = { it.uriString }) { folder ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = palette.cardSurface),
                            border = BorderStroke(
                                if (folder.isPrimary) 1.5.dp else 1.dp,
                                when {
                                    folder.isPrimary -> palette.primary
                                    folder.isReachable -> palette.cardBorder
                                    else -> palette.error.copy(alpha = 0.6f)
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        if (folder.isPrimary) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color.Transparent,
                                                contentColor = palette.primary,
                                                border = BorderStroke(1.5.dp, palette.primary),
                                                modifier = Modifier.testTag("primary_storage_badge_${folder.uriString.hashCode()}")
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Star,
                                                        contentDescription = null,
                                                        tint = palette.primary,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Text(
                                                        text = strings.primaryStorageBadge,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = palette.primary,
                                                        maxLines = 1,
                                                        softWrap = false
                                                    )
                                                }
                                            }
                                        }

                                        Icon(
                                            imageVector = Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = if (folder.isPrimary) palette.primary else palette.onSurfaceVariant,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        AutoSizingText(
                                            text = NotebookBackupManager.shortenPathMiddle(folder.displayName, 28),
                                            maxFontSize = 13.5.sp,
                                            minFontSize = 11.sp,
                                            fontWeight = if (folder.isPrimary) FontWeight.Bold else FontWeight.SemiBold,
                                            color = palette.onSurface,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (!folder.isPrimary) {
                                            Surface(
                                                onClick = { onSetPrimaryRequest(folder) },
                                                shape = RoundedCornerShape(10.dp),
                                                color = palette.surface,
                                                border = BorderStroke(1.dp, palette.cardBorder),
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .testTag("set_primary_button_${folder.uriString.hashCode()}")
                                            ) {
                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier.padding(3.dp)
                                                ) {
                                                    Image(
                                                        painter = painterResource(id = R.drawable.ic_primary_storage_log),
                                                        contentDescription = strings.setAsPrimaryAction,
                                                        modifier = Modifier.size(28.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Surface(
                                            onClick = { onRemoveBackupFolder(folder.uriString) },
                                            shape = RoundedCornerShape(10.dp),
                                            color = palette.surface,
                                            border = BorderStroke(1.dp, palette.error.copy(alpha = 0.4f)),
                                            modifier = Modifier
                                                .size(38.dp)
                                                .testTag("delete_folder_button_${folder.uriString.hashCode()}")
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(palette.errorContainer.copy(alpha = 0.25f))
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DeleteOutline,
                                                    contentDescription = strings.removeFolderDesc,
                                                    tint = palette.error,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (folder.isReachable) strings.backupStatusActive else strings.backupStatusUnreachable,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (folder.isReachable) palette.activeStatus else palette.error,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Text(
                                        text = "·",
                                        fontSize = 11.sp,
                                        color = palette.onSurfaceVariant
                                    )
                                    AutoSizingText(
                                        text = NotebookBackupManager.formatBackupTimestamp(folder.lastBackupTimestamp, appLanguage),
                                        maxFontSize = 11.sp,
                                        minFontSize = 9.5.sp,
                                        color = palette.onSurfaceVariant,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Actions Row (+ Add Folder & Backup Now)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onAddFolderClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, palette.cardBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.onSurface),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = palette.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        AutoSizingText(
                            text = strings.addFolderButton,
                            maxFontSize = 12.5.sp,
                            minFontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.onSurface
                        )
                    }
                }

                OutlinedButton(
                    onClick = onBackupNow,
                    enabled = !isBackingUp && backupFolders.isNotEmpty(),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        1.dp,
                        if (!isBackingUp && backupFolders.isNotEmpty()) palette.cardBorder
                        else palette.cardBorder.copy(alpha = 0.5f)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = palette.onSurface,
                        disabledContentColor = palette.onSurfaceVariant.copy(alpha = 0.5f)
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    if (isBackingUp) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = palette.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        AutoSizingText(
                            text = strings.backingUpStatus,
                            maxFontSize = 12.sp,
                            minFontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.onSurface
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = if (backupFolders.isNotEmpty()) palette.primary else palette.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                            AutoSizingText(
                                text = strings.backupNowButton,
                                maxFontSize = 12.5.sp,
                                minFontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (backupFolders.isNotEmpty()) palette.onSurface else palette.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportWorkspaceSection(
    onExportAllCombined: () -> Unit,
    onExportAllSeparate: () -> Unit,
    onExportSelectedClick: () -> Unit,
    palette: NotebookDialogPalette,
    strings: SettingsStrings
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = palette.sectionSurface,
        border = BorderStroke(1.25.dp, palette.sectionBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Security Info Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = palette.primary,
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        text = strings.exportOptionsSectionTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = palette.secondaryContainer,
                    border = BorderStroke(0.8.dp, palette.cardBorder)
                ) {
                    Text(
                        text = strings.exportSecurityNote,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = palette.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            HorizontalDivider(thickness = 0.8.dp, color = palette.divider)

            ExportOptionCard(
                icon = Icons.Default.Description,
                title = strings.exportAllCombinedTitle,
                subtitle = strings.exportAllCombinedSubtitle,
                onClick = onExportAllCombined,
                palette = palette
            )

            ExportOptionCard(
                icon = Icons.Default.Folder,
                title = strings.exportAllSeparateTitle,
                subtitle = strings.exportAllSeparateSubtitle,
                onClick = onExportAllSeparate,
                palette = palette
            )

            ExportOptionCard(
                icon = Icons.Default.Checklist,
                title = strings.exportSelectedTitle,
                subtitle = strings.exportSelectedSubtitle,
                onClick = onExportSelectedClick,
                palette = palette
            )
        }
    }
}

@Composable
private fun ExportOptionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    palette: NotebookDialogPalette
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = palette.cardSurface),
        border = BorderStroke(1.dp, palette.cardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 68.dp)
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
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(palette.secondaryContainer)
                    .border(1.dp, palette.primary.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = palette.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.onSurface,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontSize = 11.5.sp,
                    color = palette.onSurfaceVariant,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = palette.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun UnifiedTwoButtonBar(
    cancelLabel: String,
    confirmLabel: String,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    palette: NotebookDialogPalette,
    confirmEnabled: Boolean = true,
    confirmIsDestructive: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .weight(1f)
                .height(44.dp),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, palette.cardBorder),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.onSurface),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
        ) {
            AutoSizingText(
                text = cancelLabel,
                maxFontSize = 13.sp,
                minFontSize = 9.5.sp,
                color = palette.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        }

        val confirmBorderColor = when {
            !confirmEnabled -> palette.cardBorder.copy(alpha = 0.5f)
            confirmIsDestructive -> palette.error.copy(alpha = 0.65f)
            else -> palette.cardBorder
        }
        val confirmTextColor = when {
            !confirmEnabled -> palette.onSurfaceVariant.copy(alpha = 0.5f)
            confirmIsDestructive -> palette.error
            else -> palette.onSurface
        }

        OutlinedButton(
            onClick = onConfirm,
            enabled = confirmEnabled,
            modifier = Modifier
                .weight(1f)
                .height(44.dp),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, confirmBorderColor),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = confirmTextColor,
                disabledContentColor = palette.onSurfaceVariant.copy(alpha = 0.5f)
            ),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
        ) {
            AutoSizingText(
                text = confirmLabel,
                maxFontSize = 13.sp,
                minFontSize = 9.5.sp,
                color = confirmTextColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
