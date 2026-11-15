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

    @Test
    fun testSubfolderSeparationAndPrefixStripping() {
        // Create notebook with and without "Notebook_" prefix
        val nb1 = notebookRepository.createNotebook("Notebook_A1.1")
        assertEquals("A1.1", nb1.name)

        // Verify subfolders exist on disk
        val notebooksDir = notebookRepository.getNotebooksDirectory()
        val exportsDir = notebookRepository.getExportsDirectory()
        val systemBackupsDir = notebookRepository.getSystemBackupsDirectory()

        assertTrue(notebooksDir.exists() && notebooksDir.isDirectory)
        assertTrue(exportsDir.exists() && exportsDir.isDirectory)
        assertTrue(systemBackupsDir.exists() && systemBackupsDir.isDirectory)

        // Verify active notebook file is saved as Notebooks/Notebook_A1.1.json
        val activeNotebookFile = File(notebooksDir, "Notebook_A1.1.json")
        assertTrue("Active notebook file Notebook_A1.1.json must exist in Notebooks/", activeNotebookFile.exists())

        // Verify universal import strips Notebook_ prefix from both filename and JSON metadata
        val parsedNew = notebookRepository.parseImportedJson(activeNotebookFile.readText(), "Notebook_A1.1.json")
        assertTrue(parsedNew is com.example.data.notebook.ImportedJsonResult.SingleNotebook)
        assertEquals(
            "A1.1",
            (parsedNew as com.example.data.notebook.ImportedJsonResult.SingleNotebook).detectedName
        )

        val legacyArrayJson = """[{"id":1,"word":"Haus","type":"Nomen","data":"{}"}]"""
        val parsedLegacy = notebookRepository.parseImportedJson(legacyArrayJson, "B1.2.json")
        assertTrue(parsedLegacy is com.example.data.notebook.ImportedJsonResult.SingleNotebook)
        assertEquals(
            "B1.2",
            (parsedLegacy as com.example.data.notebook.ImportedJsonResult.SingleNotebook).detectedName
        )
    }

    @Test
    fun testSafeStartupMigrationFromRootToSubfolders() {
        val baseDir = notebookRepository.getBaseDeklinationDirectory()
        val legacyRootNotebook = File(baseDir, "C1_Kurs.json")
        val legacyRootHistory = File(baseDir, "history.json")

        legacyRootNotebook.writeText(
            """{"version":2,"notebook_name":"C1_Kurs","words":[{"id":1,"word":"Wissen","type":"Nomen","data":"{}"}]}"""
        )
        legacyRootHistory.writeText(
            """[{"id":2,"word":"lernen","type":"Verb","data":"{}"}]"""
        )

        assertTrue(legacyRootNotebook.exists())
        assertTrue(legacyRootHistory.exists())

        // Run startup migration
        notebookRepository.migrateLegacyStorageIfNeeded()

        // Root files must be moved out and cleaned up after verification
        assertFalse("Legacy root notebook file should be cleaned from root", legacyRootNotebook.exists())
        assertFalse("Legacy root history.json should be cleaned from root", legacyRootHistory.exists())

        // Verify migrated files exist in Notebooks/ and System_Backups/
        val migratedNotebookFile = File(notebookRepository.getNotebooksDirectory(), "Notebook_C1_Kurs.json")
        assertTrue("Migrated Notebook_C1_Kurs.json must exist in Notebooks/", migratedNotebookFile.exists())

        val archivedNotebookBackup = File(notebookRepository.getSystemBackupsDirectory(), "C1_Kurs.json")
        assertTrue("Archived C1_Kurs.json must exist in System_Backups/", archivedNotebookBackup.exists())

        val migratedSysHistory = File(notebookRepository.getSystemBackupsDirectory(), "history.json")
        assertTrue("Migrated history.json must exist in System_Backups/", migratedSysHistory.exists())

        // Verify C1_Kurs is listed with clean in-app display name "C1_Kurs"
        val allNotebooks = notebookRepository.listAllNotebooks()
        assertTrue(allNotebooks.any { it.name == "C1_Kurs" })
    }

    @Test
    fun testRule1CumulativeDualSaveAndNoDuplicatePreservesOriginalOrderAndTimestamp() {
        val primaryDir = File(application.cacheDir, "test_rule1_primary_${System.nanoTime()}").apply { mkdirs() }
        val backupDir = File(application.cacheDir, "test_rule1_backup_${System.nanoTime()}").apply { mkdirs() }

        val primaryFolder = BackupFolder(
            uriString = Uri.fromFile(primaryDir).toString(),
            displayName = "/Rule1Primary",
            isPrimary = true,
            isReachable = true
        )
        val backupFolder = BackupFolder(
            uriString = Uri.fromFile(backupDir).toString(),
            displayName = "/Rule1Backup",
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
            id = 601L,
            word = "Buch",
            type = "Nomen",
            data = "{}",
            timestamp = 1000L,
            rawJsonResult = "{}"
        )
        val item2 = WordHistoryEntity(
            id = 602L,
            word = "Tisch",
            type = "Nomen",
            data = "{}",
            timestamp = 2000L,
            rawJsonResult = "{}"
        )

        notebookRepository.appendOrUpdateItemInActiveHistory(item1)
        notebookRepository.appendOrUpdateItemInActiveHistory(item2)

        // Order in Primary and Backup is: ["Tisch" (2000L), "Buch" (1000L)]
        val initialPrimary = notebookRepository.loadActiveHistory()
        assertEquals(2, initialPrimary.size)
        assertEquals("Tisch", initialPrimary[0].word)
        assertEquals("Buch", initialPrimary[1].word)
        assertEquals(1000L, initialPrimary[1].timestamp)

        // Re-adding "Buch" with a newer timestamp must NOT duplicate it and must keep its original position & timestamp
        val duplicateAttempt = item1.copy(id = 699L, timestamp = 9999L)
        notebookRepository.appendOrUpdateItemInActiveHistory(duplicateAttempt)

        val afterDuplicatePrimary = notebookRepository.loadActiveHistory()
        assertEquals("Must not duplicate existing word in Primary", 2, afterDuplicatePrimary.size)
        assertEquals("Tisch must remain at index 0", "Tisch", afterDuplicatePrimary[0].word)
        assertEquals("Buch must remain at its original index 1", "Buch", afterDuplicatePrimary[1].word)
        assertEquals("Buch must retain its original timestamp 1000L", 1000L, afterDuplicatePrimary[1].timestamp)

        val activeNb = notebookRepository.getActiveNotebook()
        val backupWords = backupManager.readNotebookWordsFromFolder(backupFolder, activeNb.name)
        assertNotNull(backupWords)
        assertEquals("Must not duplicate existing word in Backup", 2, backupWords!!.size)
        assertEquals("Tisch", backupWords[0].word)
        assertEquals("Buch", backupWords[1].word)
        assertEquals(1000L, backupWords[1].timestamp)

        val internalSysBackup = notebookRepository.loadSystemBackupHistory(activeNb.id)
        assertEquals(2, internalSysBackup.size)
        assertEquals("Tisch", internalSysBackup[0].word)
        assertEquals("Buch", internalSysBackup[1].word)
        assertEquals(1000L, internalSysBackup[1].timestamp)
    }

    @Test
    fun testRule2ClearAllFollowedByCumulativeMergeAndSingleDelete() {
        val primaryDir = File(application.cacheDir, "test_rule2_primary_${System.nanoTime()}").apply { mkdirs() }
        val backupDir = File(application.cacheDir, "test_rule2_backup_${System.nanoTime()}").apply { mkdirs() }

        val primaryFolder = BackupFolder(
            uriString = Uri.fromFile(primaryDir).toString(),
            displayName = "/Rule2Primary",
            isPrimary = true,
            isReachable = true
        )
        val backupFolder = BackupFolder(
            uriString = Uri.fromFile(backupDir).toString(),
            displayName = "/Rule2Backup",
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

        val word1 = WordHistoryEntity(id = 701L, word = "Sonne", type = "Nomen", data = "{}", timestamp = 1000L, rawJsonResult = "{}")
        val word2 = WordHistoryEntity(id = 702L, word = "schreiben", type = "Verb", data = "{}", timestamp = 2000L, rawJsonResult = "{}")
        notebookRepository.appendOrUpdateItemInActiveHistory(word1)
        notebookRepository.appendOrUpdateItemInActiveHistory(word2)

        // 1. Clear All History: clears Primary ONLY, leaving Backups untouched
        notebookRepository.clearActiveHistory()
        assertTrue(notebookRepository.loadActiveHistory().isEmpty())

        val activeNb = notebookRepository.getActiveNotebook()
        assertEquals(2, backupManager.readNotebookWordsFromFolder(backupFolder, activeNb.name)!!.size)
        assertEquals(2, notebookRepository.loadSystemBackupHistory(activeNb.id).size)

        // 2. Adding a truly new word after Clear All merges cumulatively into Backup without losing older items!
        val word3 = WordHistoryEntity(id = 703L, word = "Mond", type = "Nomen", data = "{}", timestamp = 3000L, rawJsonResult = "{}")
        notebookRepository.appendOrUpdateItemInActiveHistory(word3)

        val primaryAfterNew = notebookRepository.loadActiveHistory()
        assertEquals("Primary should have only the 1 new word added after Clear All", 1, primaryAfterNew.size)
        assertEquals("Mond", primaryAfterNew[0].word)

        val backupAfterNew = backupManager.readNotebookWordsFromFolder(backupFolder, activeNb.name)!!
        assertEquals("Backup must cumulatively contain all 3 words (Mond + preserved schreiben & Sonne)", 3, backupAfterNew.size)
        assertEquals("Mond", backupAfterNew[0].word)
        assertTrue(backupAfterNew.any { it.word == "schreiben" })
        assertTrue(backupAfterNew.any { it.word == "Sonne" })

        val sysBackupAfterNew = notebookRepository.loadSystemBackupHistory(activeNb.id)
        assertEquals(3, sysBackupAfterNew.size)

        // 3. Single-Item Deletion deletes from BOTH Primary and Backup
        notebookRepository.deleteItemInActiveHistory(word3.id, word3.word, word3.type)
        assertTrue("Mond must be deleted from Primary", notebookRepository.loadActiveHistory().isEmpty())

        val backupAfterSingleDelete = backupManager.readNotebookWordsFromFolder(backupFolder, activeNb.name)!!
        assertEquals("Mond must also be deleted from Backup, leaving 2 preserved words", 2, backupAfterSingleDelete.size)
        assertFalse(backupAfterSingleDelete.any { it.word == "Mond" })
        assertTrue(backupAfterSingleDelete.any { it.word == "schreiben" })
        assertTrue(backupAfterSingleDelete.any { it.word == "Sonne" })
    }

    @Test
    fun testRule3PromoteBackupToPrimaryEstablishesFreshBaselineWithoutRetroactiveDeletions() {
        val folderADir = File(application.cacheDir, "test_rule3_folderA_${System.nanoTime()}").apply { mkdirs() }
        val folderBDir = File(application.cacheDir, "test_rule3_folderB_${System.nanoTime()}").apply { mkdirs() }

        val folderA = BackupFolder(
            uriString = Uri.fromFile(folderADir).toString(),
            displayName = "/FolderA",
            isPrimary = true,
            isReachable = true
        )
        val folderB = BackupFolder(
            uriString = Uri.fromFile(folderBDir).toString(),
            displayName = "/FolderB",
            isPrimary = false,
            isReachable = true
        )

        val prefs = application.getSharedPreferences("notebook_backup_folders_prefs", android.content.Context.MODE_PRIVATE)
        val jsonArray = org.json.JSONArray().apply {
            put(org.json.JSONObject().apply {
                put("uriString", folderA.uriString)
                put("displayName", folderA.displayName)
                put("isPrimary", true)
                put("isReachable", true)
            })
            put(org.json.JSONObject().apply {
                put("uriString", folderB.uriString)
                put("displayName", folderB.displayName)
                put("isPrimary", false)
                put("isReachable", true)
            })
        }
        prefs.edit().putString("registered_backup_folders_json", jsonArray.toString()).commit()
        backupManager = NotebookBackupManager(application, notebookRepository)
        notebookRepository.backupManager = backupManager

        // Add 3 entries
        val w1 = WordHistoryEntity(id = 801L, word = "Schule", type = "Nomen", data = "{}", timestamp = 1000L, rawJsonResult = "{}")
        val w2 = WordHistoryEntity(id = 802L, word = "lernen", type = "Verb", data = "{}", timestamp = 2000L, rawJsonResult = "{}")
        val w3 = WordHistoryEntity(id = 803L, word = "klug", type = "Adjektive", data = "{}", timestamp = 3000L, rawJsonResult = "{}")
        notebookRepository.appendOrUpdateItemInActiveHistory(w1)
        notebookRepository.appendOrUpdateItemInActiveHistory(w2)
        notebookRepository.appendOrUpdateItemInActiveHistory(w3)

        // Clear All on Folder A (Primary)
        notebookRepository.clearActiveHistory()
        assertTrue("Old Primary (Folder A) is now empty", notebookRepository.loadActiveHistory().isEmpty())

        val activeNb = notebookRepository.getActiveNotebook()
        assertEquals("Backup (Folder B) still has all 3 words", 3, backupManager.readNotebookWordsFromFolder(folderB, activeNb.name)!!.size)

        // Now switch/promote Folder B to become the NEW Primary Storage!
        backupManager.setPrimaryFolder(folderB.uriString)

        // 1. No Retroactive Deletions: New Primary (Folder B & active history) has all 3 words intact!
        val promotedPrimaryHistory = notebookRepository.loadActiveHistory()
        assertEquals("Promoted Primary must have all 3 words from Folder B as its clean baseline", 3, promotedPrimaryHistory.size)
        assertEquals("klug", promotedPrimaryHistory[0].word)
        assertEquals("lernen", promotedPrimaryHistory[1].word)
        assertEquals("Schule", promotedPrimaryHistory[2].word)

        // 2. Folder A (now Backup) has also been established with the clean baseline of 3 words
        val newBackupWordsInFolderA = backupManager.readNotebookWordsFromFolder(folderA, activeNb.name)
        assertNotNull(newBackupWordsInFolderA)
        assertEquals("New Backup (Folder A) must have the clean baseline of 3 words", 3, newBackupWordsInFolderA!!.size)

        // 3. From the moment of switch forward, Rules 1 & 2 apply to new Primary (Folder B) and new Backup (Folder A):
        // Adding a new word "Lehrer"
        val w4 = WordHistoryEntity(id = 804L, word = "Lehrer", type = "Nomen", data = "{}", timestamp = 4000L, rawJsonResult = "{}")
        notebookRepository.appendOrUpdateItemInActiveHistory(w4)
        assertEquals(4, notebookRepository.loadActiveHistory().size)
        assertEquals(4, backupManager.readNotebookWordsFromFolder(folderA, activeNb.name)!!.size)

        // Clear All on new Primary (Folder B): clears Folder B ONLY, while Folder A (new Backup) keeps all 4 words!
        notebookRepository.clearActiveHistory()
        assertTrue("New Primary (Folder B) must be empty after Clear All", notebookRepository.loadActiveHistory().isEmpty())
        assertEquals("New Backup (Folder A) must retain all 4 words untouched", 4, backupManager.readNotebookWordsFromFolder(folderA, activeNb.name)!!.size)
    }

    @Test
    fun testPrimaryDirectoryDeduplicationMergesWordsAndPreventsDuplicateCards() {
        notebookRepository.ensureInitialized()
        val root = notebookRepository.getRootDirectory()
        val unprefixed = File(root, "A1.1").apply { mkdirs() }
        val prefixed = File(root, "Notebook_A1.1").apply { mkdirs() }

        File(unprefixed, NotebookRepository.SETTINGS_FILE_NAME).writeText(
            com.example.data.notebook.NotebookSettings(notebookName = "A1.1").toJson()
        )
        File(unprefixed, NotebookRepository.HISTORY_FILE_NAME).writeText(
            """[{"id":1,"word":"Apfel","type":"Nomen","data":"{}","timestamp":1000}]"""
        )

        File(prefixed, NotebookRepository.SETTINGS_FILE_NAME).writeText(
            com.example.data.notebook.NotebookSettings(notebookName = "Notebook_A1.1").toJson()
        )
        File(prefixed, NotebookRepository.HISTORY_FILE_NAME).writeText(
            """[{"id":2,"word":"Birne","type":"Nomen","data":"{}","timestamp":2000}]"""
        )

        // Run migration / consolidation
        notebookRepository.migrateLegacyStorageIfNeeded()

        val allNotebooks = notebookRepository.listAllNotebooks()
        val a11Notebooks = allNotebooks.filter { it.name.equals("A1.1", ignoreCase = true) }
        assertEquals("Primary list must show only ONE A1.1 notebook card", 1, a11Notebooks.size)

        val mergedWords = notebookRepository.loadNotebookHistoryLocalOnly(a11Notebooks.first().id)
        assertEquals("Both Apfel and Birne must be preserved during consolidation", 2, mergedWords.size)
        assertTrue(mergedWords.any { it.word == "Apfel" })
        assertTrue(mergedWords.any { it.word == "Birne" })
    }

    @Test
    fun testDiscoverBackupNotebooksAcrossSystemAndExternalFoldersAndPromoteBaseline() = kotlinx.coroutines.runBlocking {
        val primaryDir = File(application.cacheDir, "test_disc_primary_${System.nanoTime()}").apply { mkdirs() }
        val externalBackupDir = File(application.cacheDir, "test_disc_extbackup_${System.nanoTime()}").apply { mkdirs() }

        val primaryFolder = BackupFolder(
            uriString = Uri.fromFile(primaryDir).toString(),
            displayName = "/PrimaryFolder",
            isPrimary = true,
            isReachable = true
        )
        val extFolder = BackupFolder(
            uriString = Uri.fromFile(externalBackupDir).toString(),
            displayName = "/ExtBackupFolder",
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
                put("uriString", extFolder.uriString)
                put("displayName", extFolder.displayName)
                put("isPrimary", false)
                put("isReachable", true)
            })
        }
        prefs.edit().putString("registered_backup_folders_json", jsonArray.toString()).commit()
        backupManager = NotebookBackupManager(application, notebookRepository)
        notebookRepository.backupManager = backupManager

        // Populate a backup snapshot in externalBackupDir/Notebooks/Notebook_B2_Kurs.json
        val extNotebooksSub = File(externalBackupDir, "Notebooks").apply { mkdirs() }
        File(extNotebooksSub, "Notebook_B2_Kurs.json").writeText(
            """
            {
              "version": 2,
              "notebook_name": "B2_Kurs",
              "settings": { "themeColorId": "Dunkelblau", "themeMode": "dark" },
              "words": [
                {"id": 901, "word": "Verantwortung", "type": "Nomen", "data": "{}", "timestamp": 5000},
                {"id": 902, "word": "entscheiden", "type": "Verb", "data": "{}", "timestamp": 6000}
              ]
            }
            """.trimIndent()
        )

        // Also seed a System_Backups snapshot for active Allgemein
        val w1 = WordHistoryEntity(id = 903L, word = "Fenster", type = "Nomen", data = "{}", timestamp = 3000L, rawJsonResult = "{}")
        notebookRepository.appendOrUpdateItemInActiveHistory(w1)

        val discovered = backupManager.listAllBackupNotebooks()
        assertTrue(
            "Should discover snapshot from System_Backups/",
            discovered.any {
                it.sourceType == com.example.data.notebook.BackupNotebookSourceType.SYSTEM_BACKUP &&
                    it.displayName == "Allgemein"
            }
        )
        val extEntry = discovered.find {
            it.sourceType == com.example.data.notebook.BackupNotebookSourceType.EXTERNAL_FOLDER &&
                it.displayName == "B2_Kurs"
        }
        assertNotNull("Should discover B2_Kurs snapshot from external non-primary Backup folder", extEntry)
        assertEquals(2, extEntry!!.wordCount)

        // Promote external snapshot as fresh Primary baseline
        val (snapshotWords, snapshotSettings) = backupManager.readBackupNotebookSnapshot(extEntry)
        val promoted = notebookRepository.promoteBackupSnapshot(
            displayName = extEntry.displayName,
            words = snapshotWords,
            importedSettings = snapshotSettings
        )
        assertEquals("B2_Kurs", promoted.name)
        assertEquals(2, promoted.wordCount)

        val activeHistory = notebookRepository.loadActiveHistory()
        assertEquals(2, activeHistory.size)
        assertTrue(activeHistory.any { it.word == "Verantwortung" })
        assertTrue(activeHistory.any { it.word == "entscheiden" })
    }
}


