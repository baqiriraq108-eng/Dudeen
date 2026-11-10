package com.example

import android.app.Application
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.WordHistoryEntity
import com.example.data.notebook.BackupFolder
import com.example.data.notebook.NotebookBackupManager
import com.example.data.notebook.NotebookRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NotebookRenameAndBackupTest {

    private lateinit var application: Application
    private lateinit var notebookRepository: NotebookRepository
    private lateinit var backupManager: NotebookBackupManager

    @Before
    fun setup() {
        application = ApplicationProvider.getApplicationContext()
        notebookRepository = NotebookRepository(application)
        backupManager = NotebookBackupManager(application, notebookRepository)
        notebookRepository.backupManager = backupManager
    }

    @Test
    fun testRenameDefaultNotebookNeverCrashes() {
        val initialActive = notebookRepository.getActiveNotebook()
        assertNotNull(initialActive)
        assertEquals("Allgemein", initialActive.id)

        // 1. Rename default notebook to a new name
        val renamed = notebookRepository.renameNotebook("Allgemein", "Mein Neues Notizbuch")
        assertNotNull(renamed)
        assertEquals("Mein Neues Notizbuch", renamed.name)

        // 2. Rename with special characters
        val specialRenamed = notebookRepository.renameNotebook(renamed.id, "Deutsch & Grammatik 100%!")
        assertNotNull(specialRenamed)
        assertEquals("Deutsch & Grammatik 100%!", specialRenamed.name)

        // 3. Rename with very long name
        val longName = "A".repeat(120)
        val longRenamed = notebookRepository.renameNotebook(specialRenamed.id, longName)
        assertNotNull(longRenamed)
        assertEquals(longName, longRenamed.name)

        // 4. Rename with blank/empty name (should safely retain without crashing)
        val safeBlank = notebookRepository.renameNotebook(longRenamed.id, "   ")
        assertNotNull(safeBlank)
    }

    @Test
    fun testRenameCustomNotebook() {
        val custom = notebookRepository.createNotebook("A1.1")
        assertNotNull(custom)
        assertEquals("A1.1", custom.name)

        val renamed = notebookRepository.renameNotebook(custom.id, "B2.2 Fortgeschritten")
        assertNotNull(renamed)
        assertEquals("B2.2 Fortgeschritten", renamed.name)
    }

    @Test
    fun testAddEntryWritesToPrimaryAndAllBackups() {
        val primaryDir = File(application.cacheDir, "test_primary_storage_${System.currentTimeMillis()}").apply { mkdirs() }
        val backupDir = File(application.cacheDir, "test_backup_storage_${System.currentTimeMillis()}").apply { mkdirs() }

        val primaryFolder = BackupFolder(
            uriString = Uri.fromFile(primaryDir).toString(),
            displayName = "/PrimaryStorage",
            isPrimary = true,
            isReachable = true
        )
        val backupFolder = BackupFolder(
            uriString = Uri.fromFile(backupDir).toString(),
            displayName = "/BackupStorage",
            isPrimary = false,
            isReachable = true
        )

        // Inject folders into manager
        val prefs = application.getSharedPreferences("notebook_backup_folders_prefs", android.content.Context.MODE_PRIVATE)
        val jsonArray = org.json.JSONArray().apply {
            put(org.json.JSONObject().apply {
                put("uriString", primaryFolder.uriString)
                put("displayName", primaryFolder.displayName)
                put("isPrimary", true)
                put("isReachable", true)
            })
            put(org.json.JSONObject().apply {
                put("uriString", backupFolder.uriString)
                put("displayName", backupFolder.displayName)
                put("isPrimary", false)
                put("isReachable", true)
            })
        }
        prefs.edit().putString("registered_backup_folders_json", jsonArray.toString()).commit()
        backupManager = NotebookBackupManager(application, notebookRepository)
        notebookRepository.backupManager = backupManager

        val item = WordHistoryEntity(
            id = 201L,
            word = "Buch",
            type = "Nomen",
            data = "{}",
            timestamp = 1000L,
            rawJsonResult = "{}"
        )

        // Add entry
        notebookRepository.appendOrUpdateItemInActiveHistory(item)

        // Verify primary storage has the item
        val primaryItems = notebookRepository.loadActiveHistory()
        assertEquals(1, primaryItems.size)
        assertEquals("Buch", primaryItems[0].word)

        // Verify backup storage has the item written
        val activeNb = notebookRepository.getActiveNotebook()
        val safeName = com.example.util.ExportSharingManager.sanitizeFileName(activeNb.name)
        val backupWords = backupManager.readNotebookWordsFromFolder(backupFolder, "$safeName.json")
        assertNotNull(backupWords)
        assertEquals(1, backupWords!!.size)
        assertEquals("Buch", backupWords[0].word)
    }

    @Test
    fun testDeleteSingleEntryRemovesFromPrimaryAndAllBackups() {
        val primaryDir = File(application.cacheDir, "test_primary_del_${System.currentTimeMillis()}").apply { mkdirs() }
        val backupDir = File(application.cacheDir, "test_backup_del_${System.currentTimeMillis()}").apply { mkdirs() }

        val primaryFolder = BackupFolder(
            uriString = Uri.fromFile(primaryDir).toString(),
            displayName = "/PrimaryDel",
            isPrimary = true,
            isReachable = true
        )
        val backupFolder = BackupFolder(
            uriString = Uri.fromFile(backupDir).toString(),
            displayName = "/BackupDel",
            isPrimary = false,
            isReachable = true
        )

        val prefs = application.getSharedPreferences("notebook_backup_folders_prefs", android.content.Context.MODE_PRIVATE)
        val jsonArray = org.json.JSONArray().apply {
            put(org.json.JSONObject().apply {
                put("uriString", primaryFolder.uriString)
                put("displayName", primaryFolder.displayName)
                put("isPrimary", true)
                put("isReachable", true)
            })
            put(org.json.JSONObject().apply {
                put("uriString", backupFolder.uriString)
                put("displayName", backupFolder.displayName)
                put("isPrimary", false)
                put("isReachable", true)
            })
        }
        prefs.edit().putString("registered_backup_folders_json", jsonArray.toString()).commit()
        backupManager = NotebookBackupManager(application, notebookRepository)
        notebookRepository.backupManager = backupManager

        val item1 = WordHistoryEntity(
            id = 301L,
            word = "Baum",
            type = "Nomen",
            data = "{}",
            timestamp = 1000L,
            rawJsonResult = "{}"
        )
        val item2 = WordHistoryEntity(
            id = 302L,
            word = "gehen",
            type = "Verb",
            data = "{}",
            timestamp = 2000L,
            rawJsonResult = "{}"
        )

        // Add both entries
        notebookRepository.appendOrUpdateItemInActiveHistory(item1)
        notebookRepository.appendOrUpdateItemInActiveHistory(item2)

        // Delete single entry (item1: Baum)
        notebookRepository.deleteItemInActiveHistory(item1.id, item1.word, item1.type)

        // Verify primary storage only has item2 (gehen)
        val primaryItems = notebookRepository.loadActiveHistory()
        assertEquals(1, primaryItems.size)
        assertEquals("gehen", primaryItems[0].word)

        // Verify backup storage also has item1 removed and only item2 remains
        val activeNb = notebookRepository.getActiveNotebook()
        val safeName = com.example.util.ExportSharingManager.sanitizeFileName(activeNb.name)
        val backupWords = backupManager.readNotebookWordsFromFolder(backupFolder, "$safeName.json")
        assertNotNull(backupWords)
        assertEquals(1, backupWords!!.size)
        assertEquals("gehen", backupWords[0].word)
    }

    @Test
    fun testClearAllOnlyEmptiesPrimaryWithoutTouchingBackups() {
        val primaryDir = File(application.cacheDir, "test_primary_clear_${System.currentTimeMillis()}").apply { mkdirs() }
        val backupDir = File(application.cacheDir, "test_backup_clear_${System.currentTimeMillis()}").apply { mkdirs() }

        val primaryFolder = BackupFolder(
            uriString = Uri.fromFile(primaryDir).toString(),
            displayName = "/PrimaryClear",
            isPrimary = true,
            isReachable = true
        )
        val backupFolder = BackupFolder(
            uriString = Uri.fromFile(backupDir).toString(),
            displayName = "/BackupClear",
            isPrimary = false,
            isReachable = true
        )

        val prefs = application.getSharedPreferences("notebook_backup_folders_prefs", android.content.Context.MODE_PRIVATE)
        val jsonArray = org.json.JSONArray().apply {
            put(org.json.JSONObject().apply {
                put("uriString", primaryFolder.uriString)
                put("displayName", primaryFolder.displayName)
                put("isPrimary", true)
                put("isReachable", true)
            })
            put(org.json.JSONObject().apply {
                put("uriString", backupFolder.uriString)
                put("displayName", backupFolder.displayName)
                put("isPrimary", false)
                put("isReachable", true)
            })
        }
        prefs.edit().putString("registered_backup_folders_json", jsonArray.toString()).commit()
        backupManager = NotebookBackupManager(application, notebookRepository)
        notebookRepository.backupManager = backupManager

        val item1 = WordHistoryEntity(
            id = 401L,
            word = "Haus",
            type = "Nomen",
            data = "{}",
            timestamp = 1000L,
            rawJsonResult = "{}"
        )
        val item2 = WordHistoryEntity(
            id = 402L,
            word = "laufen",
            type = "Verb",
            data = "{}",
            timestamp = 2000L,
            rawJsonResult = "{}"
        )

        // Add items: mirrored to primary and backup
        notebookRepository.appendOrUpdateItemInActiveHistory(item1)
        notebookRepository.appendOrUpdateItemInActiveHistory(item2)
        assertEquals(2, notebookRepository.loadActiveHistory().size)

        // Clear entire log (Clear All)
        notebookRepository.clearActiveHistory()

        // 1. Primary storage is completely emptied
        val primaryItems = notebookRepository.loadActiveHistory()
        assertTrue("Primary storage should be completely cleared", primaryItems.isEmpty())

        // 2. Backups remain UNTOUCHED with their full historical records!
        val activeNb = notebookRepository.getActiveNotebook()
        val safeName = com.example.util.ExportSharingManager.sanitizeFileName(activeNb.name)
        val backupWords = backupManager.readNotebookWordsFromFolder(backupFolder, "$safeName.json")
        assertNotNull("Backup file should still exist and not be deleted", backupWords)
        assertEquals("Backup must retain all 2 words untouched", 2, backupWords!!.size)
        assertTrue(backupWords.any { it.word == "Haus" })
        assertTrue(backupWords.any { it.word == "laufen" })
    }

    @Test
    fun testUnreachableLocationHandledAndRetried() {
        val reachableDir = File(application.cacheDir, "test_reachable_${System.currentTimeMillis()}").apply { mkdirs() }
        val unreachableDir = File(application.cacheDir, "test_non_existent_${System.currentTimeMillis()}/subfolder/non_existent")

        val folderReachable = BackupFolder(
            uriString = Uri.fromFile(reachableDir).toString(),
            displayName = "/Reachable",
            isPrimary = true,
            isReachable = true
        )
        val folderUnreachable = BackupFolder(
            uriString = Uri.fromFile(unreachableDir).toString(),
            displayName = "/Unreachable",
            isPrimary = false,
            isReachable = true
        )

        val prefs = application.getSharedPreferences("notebook_backup_folders_prefs", android.content.Context.MODE_PRIVATE)
        val jsonArray = org.json.JSONArray().apply {
            put(org.json.JSONObject().apply {
                put("uriString", folderReachable.uriString)
                put("displayName", folderReachable.displayName)
                put("isPrimary", true)
                put("isReachable", true)
            })
            put(org.json.JSONObject().apply {
                put("uriString", folderUnreachable.uriString)
                put("displayName", folderUnreachable.displayName)
                put("isPrimary", false)
                put("isReachable", true)
            })
        }
        prefs.edit().putString("registered_backup_folders_json", jsonArray.toString()).commit()
        backupManager = NotebookBackupManager(application, notebookRepository)
        notebookRepository.backupManager = backupManager

        val item = WordHistoryEntity(
            id = 501L,
            word = "Wasser",
            type = "Nomen",
            data = "{}",
            timestamp = 1000L,
            rawJsonResult = "{}"
        )

        // Adding entry should not crash even if one location is unreachable
        notebookRepository.appendOrUpdateItemInActiveHistory(item)

        // Verify unreachable location is marked unreachable
        val foldersAfter = backupManager.backupFolders.value
        val unreachableStatus = foldersAfter.find { it.uriString == folderUnreachable.uriString }
        assertNotNull(unreachableStatus)
        assertFalse(unreachableStatus!!.isReachable)

        // Now make directory reachable and retry
        unreachableDir.mkdirs()
        notebookRepository.appendOrUpdateItemInActiveHistory(item.copy(word = "Tee"))

        val foldersAfterRetry = backupManager.backupFolders.value
        val retriedStatus = foldersAfterRetry.find { it.uriString == folderUnreachable.uriString }
        assertNotNull(retriedStatus)
        assertTrue(retriedStatus!!.isReachable)
    }

    @Test
    fun testPrimaryStorageDesignation() {
        val folder1 = BackupFolder(
            uriString = "content://com.android.externalstorage.documents/tree/primary%3ABackup1",
            displayName = "/Backup1",
            isPrimary = true,
            isReachable = true,
            lastBackupTimestamp = 1000L
        )
        val folder2 = BackupFolder(
            uriString = "content://com.android.externalstorage.documents/tree/primary%3ABackup2",
            displayName = "/Backup2",
            isPrimary = false,
            isReachable = true,
            lastBackupTimestamp = 2000L
        )
        val folder3 = BackupFolder(
            uriString = "content://com.android.externalstorage.documents/tree/primary%3ABackup3",
            displayName = "/Backup3",
            isPrimary = false,
            isReachable = true,
            lastBackupTimestamp = 3000L
        )

        val prefs = application.getSharedPreferences("notebook_backup_folders_prefs", android.content.Context.MODE_PRIVATE)
        val jsonArray = org.json.JSONArray().apply {
            put(org.json.JSONObject().apply {
                put("uriString", folder1.uriString)
                put("displayName", folder1.displayName)
                put("isPrimary", true)
                put("isReachable", true)
            })
            put(org.json.JSONObject().apply {
                put("uriString", folder2.uriString)
                put("displayName", folder2.displayName)
                put("isPrimary", false)
                put("isReachable", true)
            })
            put(org.json.JSONObject().apply {
                put("uriString", folder3.uriString)
                put("displayName", folder3.displayName)
                put("isPrimary", false)
                put("isReachable", true)
            })
        }
        prefs.edit().putString("registered_backup_folders_json", jsonArray.toString()).commit()
        backupManager = NotebookBackupManager(application, notebookRepository)

        // Only 1 primary initially
        var primaries = backupManager.backupFolders.value.filter { it.isPrimary }
        assertEquals(1, primaries.size)
        assertEquals(folder1.uriString, primaries[0].uriString)

        // Change primary to folder2
        backupManager.setPrimaryFolder(folder2.uriString)
        primaries = backupManager.backupFolders.value.filter { it.isPrimary }
        assertEquals(1, primaries.size)
        assertEquals(folder2.uriString, primaries[0].uriString)

        // Change primary to folder3
        backupManager.setPrimaryFolder(folder3.uriString)
        primaries = backupManager.backupFolders.value.filter { it.isPrimary }
        assertEquals(1, primaries.size)
        assertEquals(folder3.uriString, primaries[0].uriString)

        // Remove folder3: primary should automatically fall back to remaining
        backupManager.removeFolder(folder3.uriString)
        primaries = backupManager.backupFolders.value.filter { it.isPrimary }
        assertEquals(1, primaries.size)
    }
}
