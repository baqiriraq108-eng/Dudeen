package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.data.notebook.BackupFolder
import com.example.data.notebook.BackupNotebookEntry
import com.example.data.notebook.BackupNotebookSourceType
import com.example.data.notebook.Notebook
import com.example.data.notebook.NotebookSettings
import com.example.ui.components.NotebookManagerDialog
import com.example.ui.components.NotebookSwitcher
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class NotebookManagerUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testNotebookManagerThreeStorageTabsShortButtonsAndCardLayout() {
        val primaryNotebooks = listOf(
            Notebook(
                id = "Allgemein",
                name = "Allgemein",
                lastModified = 1_700_000_000_000L,
                wordCount = 12,
                settings = NotebookSettings(notebookName = "Allgemein")
            ),
            Notebook(
                id = "A1.1",
                name = "A1.1",
                lastModified = 1_700_000_500_000L,
                wordCount = 805,
                settings = NotebookSettings(notebookName = "A1.1")
            )
        )

        val backupEntries = listOf(
            BackupNotebookEntry(
                uniqueKey = "sys::Notebook_A1.1.json",
                notebookId = "A1.1",
                displayName = "A1.1",
                wordCount = 805,
                lastModified = 1_700_000_500_000L,
                themeColorId = "Dunkelblau",
                sourceType = BackupNotebookSourceType.SYSTEM_BACKUP,
                sourceLabel = "System_Backups",
                sourceFolderUri = null,
                fileName = "Notebook_A1.1.json"
            ),
            BackupNotebookEntry(
                uniqueKey = "ext::content://example/backup1::Notebook_A1.2.json",
                notebookId = "A1.2",
                displayName = "A1.2",
                wordCount = 410,
                lastModified = 1_700_000_800_000L,
                themeColorId = "Smaragd",
                sourceType = BackupNotebookSourceType.EXTERNAL_FOLDER,
                sourceLabel = "/Momente",
                sourceFolderUri = "content://example/backup1",
                fileName = "Notebook_A1.2.json"
            )
        )

        val backupFolders = listOf(
            BackupFolder(
                uriString = "content://example/backup1",
                displayName = "/Momente",
                isPrimary = true,
                isReachable = true
            )
        )

        var promotedEntry: BackupNotebookEntry? = null

        composeTestRule.setContent {
            MyApplicationTheme {
                NotebookManagerDialog(
                    notebooks = primaryNotebooks,
                    activeNotebookId = "A1.1",
                    onDismiss = {},
                    onSelectNotebook = {},
                    onCreateNotebook = {},
                    onRenameNotebook = { _, _ -> },
                    onDeleteNotebook = {},
                    onExportJson = {},
                    onImportJson = {},
                    onExportAllCombined = {},
                    onExportAllSeparate = {},
                    onExportSelectedClick = {},
                    backupFolders = backupFolders,
                    backupNotebooks = backupEntries,
                    isLoadingBackupNotebooks = false,
                    onPromoteBackupNotebook = { promotedEntry = it },
                    appLanguage = "de"
                )
            }
        }

        // 1. Top section tabs & bordered notebooks container with 3 storage tabs (Primary, Reserve, Backup) are displayed
        composeTestRule.onNodeWithTag("notebook_manager_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("manager_section_notebooks").assertIsDisplayed()
        composeTestRule.onNodeWithTag("manager_section_backups").assertIsDisplayed()
        composeTestRule.onNodeWithTag("manager_section_export").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bordered_notebooks_container").assertIsDisplayed()
        composeTestRule.onNodeWithTag("notebooks_tab_primary").assertIsDisplayed()
        composeTestRule.onNodeWithTag("notebooks_tab_reserve").assertIsDisplayed()
        composeTestRule.onNodeWithTag("notebooks_tab_backup").assertIsDisplayed()

        // Short two-word action buttons are displayed without truncation
        composeTestRule.onNodeWithText("Neues Buch").assertIsDisplayed()
        composeTestRule.onNodeWithText("JSON Import").assertIsDisplayed()

        // Strictly icon-only tabs with zero text labels and zero count numbers
        composeTestRule.onNodeWithContentDescription("Notizbücher").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Backups").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Export").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Primär").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Reserve").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Backup").assertIsDisplayed()

        composeTestRule.onNodeWithText("Notizbücher").assertDoesNotExist()
        composeTestRule.onNodeWithText("Backups").assertDoesNotExist()
        composeTestRule.onNodeWithText("Export").assertDoesNotExist()
        composeTestRule.onNodeWithText("Primär").assertDoesNotExist()
        composeTestRule.onNodeWithText("Reserve").assertDoesNotExist()
        composeTestRule.onNodeWithText("Backup").assertDoesNotExist()
        composeTestRule.onNodeWithText("1").assertDoesNotExist()
        composeTestRule.onNodeWithText("2").assertDoesNotExist()

        // Primary notebooks rendered cleanly (active first, then alphabetical)
        composeTestRule.onNodeWithTag("primary_notebook_card_A1.1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("primary_notebook_card_Allgemein").assertIsDisplayed()
        composeTestRule.onNodeWithText("📚 805 Wörter").assertIsDisplayed()

        // 2. Switch inside bordered container to Reserve (Secondary / System_Backups) tab
        composeTestRule.onNodeWithTag("notebooks_tab_reserve").performClick()
        composeTestRule.waitForIdle()

        val reserveCardTag = "backup_notebook_card_${backupEntries[0].uniqueKey.hashCode()}"
        composeTestRule.onNodeWithTag(reserveCardTag).assertIsDisplayed()
        composeTestRule.onNodeWithText("A1.1").assertIsDisplayed()
        composeTestRule.onNodeWithText("📚 805 Wörter").assertIsDisplayed()

        // 3. Switch inside bordered container to Backup (External Backup Folders) tab
        composeTestRule.onNodeWithTag("notebooks_tab_backup").performClick()
        composeTestRule.waitForIdle()

        val folderBackupCardTag = "backup_notebook_card_${backupEntries[1].uniqueKey.hashCode()}"
        composeTestRule.onNodeWithTag(folderBackupCardTag).assertIsDisplayed()
        composeTestRule.onNodeWithText("A1.2").assertIsDisplayed()
        composeTestRule.onNodeWithText("📚 410 Wörter").assertIsDisplayed()

        // Clicking backup card opens Fresh Baseline confirmation dialog -> confirm promotes snapshot
        composeTestRule.onNodeWithTag(folderBackupCardTag).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Bestätigen").performClick()
        composeTestRule.waitForIdle()
        assertEquals("A1.2", promotedEntry?.displayName)

        // 4. Switch top section tab to Backups -> shows Backup Folders workspace with zero folder count numbers
        composeTestRule.onNodeWithTag("manager_section_backups").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Backup-Ordner").assertIsDisplayed()
        composeTestRule.onNodeWithText("1").assertDoesNotExist()

        // 5. Switch top section tab to Export -> shows Export Options workspace
        composeTestRule.onNodeWithTag("manager_section_export").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Export-Optionen").assertIsDisplayed()
    }

    @Test
    fun testMainScreenNotebookDropdownTwoTabsAndImmediateBackupFolderActivation() {
        val mixedNotebooks = listOf(
            Notebook(
                id = "A1.2",
                name = "A1.2",
                lastModified = 1_700_000_100_000L,
                wordCount = 410,
                settings = NotebookSettings(notebookName = "A1.2")
            ),
            Notebook(
                id = "A1.1_1",
                name = "A1.1_1",
                lastModified = 1_700_000_200_000L,
                wordCount = 805,
                settings = NotebookSettings(notebookName = "A1.1_1")
            ),
            Notebook(
                id = "A1.1",
                name = "A1.1",
                lastModified = 1_700_000_500_000L,
                wordCount = 805,
                settings = NotebookSettings(notebookName = "A1.1")
            ),
            Notebook(
                id = "A1.2_1",
                name = "A1.2_1",
                lastModified = 1_700_000_050_000L,
                wordCount = 410,
                settings = NotebookSettings(notebookName = "A1.2_1")
            )
        )

        val backupEntries = listOf(
            BackupNotebookEntry(
                uniqueKey = "sys::Notebook_InternalOnly.json",
                notebookId = "InternalOnly",
                displayName = "InternalOnly",
                wordCount = 99,
                lastModified = 1_700_000_500_000L,
                themeColorId = "Dunkelblau",
                sourceType = BackupNotebookSourceType.SYSTEM_BACKUP,
                sourceLabel = "System_Backups",
                sourceFolderUri = null,
                fileName = "Notebook_InternalOnly.json"
            ),
            BackupNotebookEntry(
                uniqueKey = "ext::content://example/backup1::Notebook_B1.1.json",
                notebookId = "B1.1",
                displayName = "B1.1",
                wordCount = 320,
                lastModified = 1_700_000_800_000L,
                themeColorId = "Smaragd",
                sourceType = BackupNotebookSourceType.EXTERNAL_FOLDER,
                sourceLabel = "/Momente",
                sourceFolderUri = "content://example/backup1",
                fileName = "Notebook_B1.1.json"
            )
        )

        var promotedFromDropdown: BackupNotebookEntry? = null

        composeTestRule.setContent {
            MyApplicationTheme {
                NotebookSwitcher(
                    activeNotebook = mixedNotebooks[2],
                    allNotebooks = mixedNotebooks,
                    onSelectNotebook = {},
                    onCreateNotebookClick = {},
                    onManageNotebooksClick = {},
                    appLanguage = "de",
                    backupNotebooks = backupEntries,
                    onPromoteBackupNotebook = { promotedFromDropdown = it }
                )
            }
        }

        // Open main screen notebook dropdown
        composeTestRule.onNodeWithTag("notebook_switcher_button").performClick()
        composeTestRule.waitForIdle()

        // Verify the two icon-only segmented tabs are present inside the dropdown
        composeTestRule.onNodeWithTag("dropdown_tab_primary").assertIsDisplayed()
        composeTestRule.onNodeWithTag("dropdown_tab_backup").assertIsDisplayed()

        // Verify canonical primary notebooks are displayed while duplicate _1 entries are filtered out
        composeTestRule.onNodeWithTag("dropdown_notebook_item_A1.1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("dropdown_notebook_item_A1.2").assertIsDisplayed()
        composeTestRule.onNodeWithTag("dropdown_notebook_item_A1.1_1").assertDoesNotExist()
        composeTestRule.onNodeWithTag("dropdown_notebook_item_A1.2_1").assertDoesNotExist()

        // Switch to User-Selected Backup Folder tab inside dropdown
        composeTestRule.onNodeWithTag("dropdown_tab_backup").performClick()
        composeTestRule.waitForIdle()

        // Only EXTERNAL_FOLDER notebooks appear; SYSTEM_BACKUP is strictly excluded
        composeTestRule.onNodeWithTag("dropdown_backup_notebook_item_B1.1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("dropdown_backup_notebook_item_InternalOnly").assertDoesNotExist()

        // Clicking the backup folder notebook activates it immediately without confirmation dialog
        composeTestRule.onNodeWithTag("dropdown_backup_notebook_item_B1.1").performClick()
        composeTestRule.waitForIdle()
        assertEquals("B1.1", promotedFromDropdown?.displayName)
    }
}
