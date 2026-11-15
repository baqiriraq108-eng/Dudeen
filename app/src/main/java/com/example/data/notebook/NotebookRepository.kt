package com.example.data.notebook

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.example.data.local.WordHistoryEntity
import com.example.data.model.extractNounQuickDetails
import com.example.data.model.extractVerbQuickDetails
import com.example.data.model.optSafeString
import com.example.data.model.sanitizeHistoryWord
import com.example.util.ExportSharingManager
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Locale

/**
 * Repository managing the "Notizbücher" (Autonomous Learning Workbooks) hierarchy.
 * Organized cleanly on disk under `Documents/Deklination/`:
 *   - `Notebooks/`: Active notebook files named `Notebook_<Name>.json`
 *   - `Exports/`: User PDF & Excel exports (`PDF_<Name>_YYYY-MM-DD.pdf`, `Excel_<Name>_YYYY-MM-DD.xlsx`)
 *   - `System_Backups/`: Internal state (`history.json`, `settings.json`, per-notebook state, and migrated root backups)
 */
class NotebookRepository(private val context: Context) {

    companion object {
        private const val TAG = "NotebookRepository"
        const val BASE_DEKLINATION_FOLDER = "Deklination"
        const val NOTEBOOKS_SUBFOLDER = "Notebooks"
        const val EXPORTS_SUBFOLDER = "Exports"
        const val SYSTEM_BACKUPS_SUBFOLDER = "System_Backups"
        const val INTERNAL_NOTEBOOKS_DIR = "Notizbücher"
        const val ROOT_FOLDER_NAME = "Deklination/System_Backups/Notizbücher"
        const val LEGACY_ROOT_FOLDER_NAME = "Deklination/Notizbücher"
        const val DEFAULT_NOTEBOOK_ID = "Allgemein"
        const val SETTINGS_FILE_NAME = "settings.json"
        const val HISTORY_FILE_NAME = "history.json"
        private const val PREFS_NAME = "notebook_system_prefs"
        private const val PREF_ACTIVE_NOTEBOOK = "active_notebook_id"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var backupManager: NotebookBackupManager? = null

    @Volatile
    private var cachedBaseDir: File? = null

    @Volatile
    private var cachedRootDir: File? = null

    @Volatile
    private var isInitialized: Boolean = false

    private val wordCountCache = java.util.concurrent.ConcurrentHashMap<String, Pair<Long, Int>>()
    private val fileNameSanitizeRegex = Regex("[\\\\/:*?\"<>|]")

    /**
     * Checks if a directory exists or can be created, and is writable and readable.
     */
    private fun isDirWritableAndReadable(dir: File): Boolean {
        return try {
            if (!dir.exists()) {
                if (!dir.mkdirs()) return false
            }
            if (dir.isDirectory && dir.canRead() && dir.canWrite()) {
                val testProbe = File(dir, ".probe_${System.nanoTime()}")
                testProbe.writeText("ok")
                val readOk = testProbe.readText() == "ok"
                testProbe.delete()
                readOk
            } else {
                false
            }
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Resolves the base `Deklination` directory (`Documents/Deklination/`), falling back to
     * app-specific external or internal storage if public Documents is restricted.
     */
    fun getBaseDeklinationDirectory(): File {
        cachedBaseDir?.let { cached ->
            if (cached.exists() && cached.isDirectory) {
                return cached
            }
        }

        try {
            val publicDocs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val primaryBase = File(publicDocs, BASE_DEKLINATION_FOLDER)
            if (isDirWritableAndReadable(primaryBase)) {
                cachedBaseDir = primaryBase
                return primaryBase
            }
        } catch (_: Throwable) {}

        try {
            val appExtDocs = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            if (appExtDocs != null) {
                val fallbackBase = File(appExtDocs, BASE_DEKLINATION_FOLDER)
                if (isDirWritableAndReadable(fallbackBase)) {
                    cachedBaseDir = fallbackBase
                    return fallbackBase
                }
            }
        } catch (_: Throwable) {}

        val internalBase = File(context.filesDir, BASE_DEKLINATION_FOLDER)
        try { internalBase.mkdirs() } catch (_: Throwable) {}
        cachedBaseDir = internalBase
        return internalBase
    }

    /**
     * Active notebooks subfolder: `Documents/Deklination/Notebooks/`
     */
    fun getNotebooksDirectory(): File {
        val dir = File(getBaseDeklinationDirectory(), NOTEBOOKS_SUBFOLDER)
        if (!dir.exists()) {
            try { dir.mkdirs() } catch (_: Throwable) {}
        }
        return dir
    }

    /**
     * User exports subfolder: `Documents/Deklination/Exports/`
     */
    fun getExportsDirectory(): File {
        val dir = File(getBaseDeklinationDirectory(), EXPORTS_SUBFOLDER)
        if (!dir.exists()) {
            try { dir.mkdirs() } catch (_: Throwable) {}
        }
        return dir
    }

    /**
     * Internal state and backups subfolder: `Documents/Deklination/System_Backups/`
     */
    fun getSystemBackupsDirectory(): File {
        val dir = File(getBaseDeklinationDirectory(), SYSTEM_BACKUPS_SUBFOLDER)
        if (!dir.exists()) {
            try { dir.mkdirs() } catch (_: Throwable) {}
        }
        return dir
    }

    /**
     * Resolves the internal notebook state root directory: `Documents/Deklination/System_Backups/Notizbücher/`
     */
    fun getRootDirectory(): File {
        cachedRootDir?.let { cached ->
            if (cached.exists() && cached.isDirectory) {
                return cached
            }
        }

        val dir = File(getSystemBackupsDirectory(), INTERNAL_NOTEBOOKS_DIR)
        try { dir.mkdirs() } catch (_: Throwable) {}
        cachedRootDir = dir
        return dir
    }

    /**
     * Synchronizes a single notebook's unified JSON (`Notebook_<Name>.json`) into `Notebooks/`.
     */
    @Synchronized
    fun syncNotebookFileToNotebooksSubfolder(notebookId: String) {
        try {
            val root = getRootDirectory()
            val folder = File(root, notebookId)
            if (!folder.exists() || !folder.isDirectory) return

            val settingsFile = File(folder, SETTINGS_FILE_NAME)
            val rawSettings = if (settingsFile.exists()) {
                NotebookSettings.fromJson(settingsFile.readText(), fallbackName = notebookId)
            } else {
                NotebookSettings(notebookName = notebookId)
            }
            val cleanName = ExportSharingManager.stripNotebookPrefix(rawSettings.notebookName.ifBlank { notebookId })
                .ifBlank { DEFAULT_NOTEBOOK_ID }
            val settings = rawSettings.copy(notebookName = cleanName)

            val historyFile = File(folder, HISTORY_FILE_NAME)
            val items = if (historyFile.exists()) {
                parseJsonHistory(historyFile.readText())
            } else {
                emptyList()
            }

            val jsonObj = JSONObject().apply {
                put("version", 2)
                put("notebook_name", cleanName)
                put("notebookName", cleanName)
                put("exported_at", System.currentTimeMillis())
                put("settings", settings.toExportSettingsJson())
                val serializedWords = serializeHistoryToJson(items)
                put("words", serializedWords)
                put("history", serializedWords)
            }

            val notebooksDir = getNotebooksDirectory()
            val targetFileName = ExportSharingManager.buildNotebookFileName(cleanName, defaultName = DEFAULT_NOTEBOOK_ID)
            val targetFile = File(notebooksDir, targetFileName)
            val tempFile = File(notebooksDir, ".tmp_${System.nanoTime()}.json")
            tempFile.writeText(jsonObj.toString(2), Charsets.UTF_8)
            tempFile.copyTo(targetFile, overwrite = true)
            tempFile.delete()
        } catch (e: Exception) {
            Log.w(TAG, "Failed syncing notebook $notebookId to Notebooks/ subfolder: ${e.message}")
        }
    }

    /**
     * Safely migrates legacy files and folders from the root `Documents/Deklination/` directory
     * into `Notebooks/` and `System_Backups/` only after verifying the new files are written
     * completely and validly, then cleans the root directory.
     */
    @Synchronized
    fun migrateLegacyStorageIfNeeded() {
        try {
            val baseDir = getBaseDeklinationDirectory()
            val notebooksDir = getNotebooksDirectory()
            val exportsDir = getExportsDirectory()
            val systemBackupsDir = getSystemBackupsDirectory()
            val internalRoot = getRootDirectory()

            notebooksDir.mkdirs()
            exportsDir.mkdirs()
            systemBackupsDir.mkdirs()
            internalRoot.mkdirs()

            // 1. Migrate legacy `Deklination/Notizbücher/` directory into `System_Backups/Notizbücher/` and `Notebooks/`
            val legacyNotizbuecherDir = File(baseDir, INTERNAL_NOTEBOOKS_DIR)
            if (legacyNotizbuecherDir.exists() && legacyNotizbuecherDir.isDirectory &&
                legacyNotizbuecherDir.absolutePath != internalRoot.absolutePath
            ) {
                val legacySubDirs = legacyNotizbuecherDir.listFiles { f -> f.isDirectory } ?: emptyArray()
                var allSubDirsMigrated = true
                for (subDir in legacySubDirs) {
                    try {
                        val cleanId = sanitizeFileName(ExportSharingManager.stripNotebookPrefix(subDir.name))
                            .ifBlank { DEFAULT_NOTEBOOK_ID }
                        val targetSubDir = File(internalRoot, cleanId).apply { mkdirs() }

                        val legacySettings = File(subDir, SETTINGS_FILE_NAME)
                        val targetSettings = File(targetSubDir, SETTINGS_FILE_NAME)
                        if (legacySettings.exists() && legacySettings.length() > 0L) {
                            if (!targetSettings.exists() || targetSettings.length() == 0L) {
                                legacySettings.copyTo(targetSettings, overwrite = true)
                            }
                        }

                        val legacyHistory = File(subDir, HISTORY_FILE_NAME)
                        val targetHistory = File(targetSubDir, HISTORY_FILE_NAME)
                        if (legacyHistory.exists() && legacyHistory.length() > 0L) {
                            val currentTargetText = if (targetHistory.exists()) targetHistory.readText().trim() else ""
                            if (currentTargetText.isEmpty() || currentTargetText == "[]") {
                                legacyHistory.copyTo(targetHistory, overwrite = true)
                            }
                        }

                        // Sync to active `Notebooks/Notebook_<Name>.json`
                        syncNotebookFileToNotebooksSubfolder(cleanId)
                    } catch (e: Exception) {
                        allSubDirsMigrated = false
                        Log.w(TAG, "Error migrating legacy notebook dir ${subDir.name}: ${e.message}")
                    }
                }
                if (allSubDirsMigrated) {
                    legacyNotizbuecherDir.deleteRecursively()
                }
            }

            // 2. Migrate root `.json` files in `Documents/Deklination/`
            val rootJsonFiles = baseDir.listFiles { f ->
                f.isFile && f.name.endsWith(".json", ignoreCase = true)
            } ?: emptyArray()

            for (rootFile in rootJsonFiles) {
                try {
                    val fileName = rootFile.name
                    val rawText = rootFile.readText(Charsets.UTF_8).trim()
                    if (rawText.isEmpty()) {
                        rootFile.delete()
                        continue
                    }

                    val isValidJson = try {
                        if (rawText.startsWith("[")) {
                            JSONArray(rawText)
                            true
                        } else if (rawText.startsWith("{")) {
                            JSONObject(rawText)
                            true
                        } else false
                    } catch (_: Exception) {
                        false
                    }

                    if (!isValidJson) continue

                    if (fileName.equals(HISTORY_FILE_NAME, ignoreCase = true) ||
                        fileName.equals(SETTINGS_FILE_NAME, ignoreCase = true)
                    ) {
                        // Internal state file -> move to System_Backups/
                        val targetSysFile = File(systemBackupsDir, fileName)
                        val archiveDest = if (!targetSysFile.exists() || targetSysFile.length() == 0L) {
                            targetSysFile
                        } else {
                            val existingText = try { targetSysFile.readText(Charsets.UTF_8).trim() } catch (_: Exception) { "" }
                            if ((existingText.isEmpty() || existingText == "[]") && rawText != "[]") {
                                targetSysFile
                            } else {
                                File(systemBackupsDir, "legacy_root_$fileName")
                            }
                        }
                        val tmp = File(systemBackupsDir, ".tmp_${System.nanoTime()}.json")
                        tmp.writeText(rawText, Charsets.UTF_8)
                        if (tmp.exists() && tmp.length() > 0L) {
                            tmp.copyTo(archiveDest, overwrite = true)
                            tmp.delete()
                            if (archiveDest.exists() && archiveDest.length() > 0L) {
                                rootFile.delete()
                            }
                        } else {
                            tmp.delete()
                        }
                    } else {
                        // Root notebook `.json` file (e.g. `A1.1.json` or `Notebook_A1.1.json`)
                        val parsed = parseImportedJson(rawText, fileName)
                        if (parsed is ImportedJsonResult.SingleNotebook) {
                            val cleanName = ExportSharingManager.stripNotebookPrefix(parsed.detectedName)
                                .ifBlank { ExportSharingManager.stripNotebookPrefix(fileName) }
                                .ifBlank { DEFAULT_NOTEBOOK_ID }
                            val safeFolderId = sanitizeFileName(cleanName).ifBlank { DEFAULT_NOTEBOOK_ID }
                            val nbDir = File(internalRoot, safeFolderId).apply { mkdirs() }
                            val nbSettingsFile = File(nbDir, SETTINGS_FILE_NAME)
                            val nbHistoryFile = File(nbDir, HISTORY_FILE_NAME)

                            if (!nbSettingsFile.exists() || nbSettingsFile.length() == 0L) {
                                val finalSettings = (parsed.settings ?: NotebookSettings(notebookName = cleanName))
                                    .copy(notebookName = cleanName)
                                nbSettingsFile.writeText(finalSettings.toJson(), Charsets.UTF_8)
                            }
                            val existingHistoryText = if (nbHistoryFile.exists()) nbHistoryFile.readText().trim() else ""
                            if (existingHistoryText.isEmpty() || existingHistoryText == "[]") {
                                nbHistoryFile.writeText(serializeHistoryToJson(parsed.words).toString(2), Charsets.UTF_8)
                            }

                            syncNotebookFileToNotebooksSubfolder(safeFolderId)
                        }

                        // Move old root copy into System_Backups/ and verify before deleting from root
                        val backupArchive = File(systemBackupsDir, fileName)
                        val tmp = File(systemBackupsDir, ".tmp_${System.nanoTime()}.json")
                        tmp.writeText(rawText, Charsets.UTF_8)
                        if (tmp.exists() && tmp.length() > 0L) {
                            tmp.copyTo(backupArchive, overwrite = true)
                            tmp.delete()
                            val targetNotebookFile = File(
                                notebooksDir,
                                ExportSharingManager.buildNotebookFileName(fileName)
                            )
                            if (backupArchive.exists() && backupArchive.length() > 0L &&
                                (!targetNotebookFile.exists() || targetNotebookFile.length() > 0L)
                            ) {
                                rootFile.delete()
                            }
                        } else {
                            tmp.delete()
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error migrating root file ${rootFile.name}: ${e.message}")
                }
            }

            // 3. Check `Notebooks/` subfolder for any existing `.json` files (both `Notebook_<Name>.json` and legacy `<Name>.json`)
            val notebookJsonFiles = notebooksDir.listFiles { f ->
                f.isFile && f.name.endsWith(".json", ignoreCase = true) && !f.name.startsWith(".")
            } ?: emptyArray()

            for (nbFile in notebookJsonFiles) {
                try {
                    val rawText = nbFile.readText(Charsets.UTF_8).trim()
                    if (rawText.isEmpty()) continue
                    val parsed = parseImportedJson(rawText, nbFile.name)
                    if (parsed is ImportedJsonResult.SingleNotebook) {
                        val cleanName = ExportSharingManager.stripNotebookPrefix(parsed.detectedName)
                            .ifBlank { ExportSharingManager.stripNotebookPrefix(nbFile.name) }
                            .ifBlank { DEFAULT_NOTEBOOK_ID }
                        val safeFolderId = sanitizeFileName(cleanName).ifBlank { DEFAULT_NOTEBOOK_ID }
                        val nbDir = File(internalRoot, safeFolderId).apply { mkdirs() }
                        val nbSettingsFile = File(nbDir, SETTINGS_FILE_NAME)
                        val nbHistoryFile = File(nbDir, HISTORY_FILE_NAME)

                        if (!nbSettingsFile.exists() || nbSettingsFile.length() == 0L) {
                            val finalSettings = (parsed.settings ?: NotebookSettings(notebookName = cleanName))
                                .copy(notebookName = cleanName)
                            nbSettingsFile.writeText(finalSettings.toJson(), Charsets.UTF_8)
                        }
                        val existingHistoryText = if (nbHistoryFile.exists()) nbHistoryFile.readText().trim() else ""
                        if ((existingHistoryText.isEmpty() || existingHistoryText == "[]") && parsed.words.isNotEmpty()) {
                            nbHistoryFile.writeText(serializeHistoryToJson(parsed.words).toString(2), Charsets.UTF_8)
                        }

                        val expectedFileName = ExportSharingManager.buildNotebookFileName(cleanName, defaultName = DEFAULT_NOTEBOOK_ID)
                        if (!nbFile.name.equals(expectedFileName, ignoreCase = true)) {
                            // Legacy unprefixed file inside Notebooks/ -> create Notebook_<Name>.json and move legacy copy to System_Backups/
                            syncNotebookFileToNotebooksSubfolder(safeFolderId)
                            val newPrefixedFile = File(notebooksDir, expectedFileName)
                            if (newPrefixedFile.exists() && newPrefixedFile.length() > 0L) {
                                nbFile.copyTo(File(systemBackupsDir, nbFile.name), overwrite = true)
                                nbFile.delete()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error inspecting notebook file ${nbFile.name}: ${e.message}")
                }
            }
            // 4. Consolidate any duplicate notebook folders inside System_Backups/Notizbücher/
            // (e.g. if both `A1.1` and `Notebook_A1.1` exist with the same stripped display name)
            consolidateDuplicateNotebookDirectories(internalRoot, systemBackupsDir)
        } catch (e: Exception) {
            Log.w(TAG, "Storage migration note: ${e.message}")
        }
    }

    private val duplicateSuffixRegex = Regex("""(?:\s*_\d+|\s*\(\d+\))+$""")

    /**
     * Strips trailing auto-generated duplicate suffixes such as `_1`, `_2`, `(2)`, `(3)`
     * from a clean notebook name so accidental duplicate copies (`A1.1_1`, `A1.2_1`, `A1.1 (2)`)
     * are recognized as the same logical notebook as `A1.1` / `A1.2`.
     */
    fun stripDuplicateNotebookSuffix(rawName: String): String {
        val clean = ExportSharingManager.stripNotebookPrefix(rawName).trim()
        val stripped = clean.replace(duplicateSuffixRegex, "").trim()
        return stripped.ifBlank { clean }
    }

    /**
     * Consolidates any duplicate directories inside `internalRoot` that normalize to the same
     * clean notebook name (including `Notebook_A1.1`, `A1.1_1`, `A1.1 (2)` alongside `A1.1`),
     * merging missing words safely into the canonical directory and archiving duplicate copies
     * into `System_Backups/` so they remain accessible in the Backup tabs without cluttering Primary.
     */
    private fun consolidateDuplicateNotebookDirectories(internalRoot: File, systemBackupsDir: File) {
        val dirs = internalRoot.listFiles { f -> f.isDirectory } ?: return
        if (dirs.size <= 1) return

        val savedActiveId = prefs.getString(PREF_ACTIVE_NOTEBOOK, null)
        val grouped = dirs.groupBy { dir ->
            val rawSettingsName = try {
                val sf = File(dir, SETTINGS_FILE_NAME)
                if (sf.exists()) NotebookSettings.fromJson(sf.readText(), fallbackName = dir.name).notebookName
                else dir.name
            } catch (_: Throwable) {
                dir.name
            }
            val baseFromSettings = stripDuplicateNotebookSuffix(rawSettingsName.ifBlank { dir.name })
            val baseFromDir = stripDuplicateNotebookSuffix(dir.name)
            val canonicalKey = if (
                duplicateSuffixRegex.containsMatchIn(dir.name) &&
                baseFromDir.isNotBlank() &&
                rawSettingsName.trim().equals(dir.name.trim(), ignoreCase = true)
            ) {
                baseFromDir
            } else {
                baseFromSettings.ifBlank { baseFromDir }.ifBlank { dir.name }
            }
            canonicalKey.trim().lowercase(Locale.ROOT)
        }

        for ((_, group) in grouped) {
            if (group.size <= 1) continue
            val canonicalDir = group.maxByOrNull { dir ->
                val isActiveBonus = if (dir.name == savedActiveId) 1_000_000_000L else 0L
                val isUnprefixedBonus = if (!dir.name.startsWith(ExportSharingManager.NOTEBOOK_FILE_PREFIX, ignoreCase = true)) 10_000_000L else 0L
                val isUnsuffixedBonus = if (!duplicateSuffixRegex.containsMatchIn(dir.name)) 50_000_000L else 0L
                val hf = File(dir, HISTORY_FILE_NAME)
                val sizeBonus = if (hf.exists()) hf.length().coerceAtMost(1_000_000L) else 0L
                isActiveBonus + isUnsuffixedBonus + isUnprefixedBonus + sizeBonus + (dir.lastModified() / 1000L)
            } ?: continue

            val canonicalWords = loadNotebookHistoryLocalOnly(canonicalDir.name).toMutableList()
            var mergedWords = canonicalWords.toList()
            for (dupDir in group) {
                if (dupDir.absolutePath == canonicalDir.absolutePath) continue
                try {
                    val dupWords = loadNotebookHistoryLocalOnly(dupDir.name)
                    if (dupWords.isNotEmpty()) {
                        mergedWords = mergeItemsCumulatively(mergedWords, dupWords)
                        val archiveFile = File(systemBackupsDir, "archived_${dupDir.name}_$HISTORY_FILE_NAME")
                        File(dupDir, HISTORY_FILE_NAME).takeIf { it.exists() && it.length() > 0L }?.copyTo(archiveFile, overwrite = true)
                    }
                    if (savedActiveId == dupDir.name) {
                        prefs.edit().putString(PREF_ACTIVE_NOTEBOOK, canonicalDir.name).apply()
                    }
                    dupDir.deleteRecursively()
                } catch (e: Exception) {
                    Log.w(TAG, "Failed consolidating duplicate dir ${dupDir.name}: ${e.message}")
                }
            }

            // Ensure canonical directory's settings.json uses the clean unsuffixed name if it had a suffix
            try {
                val sf = File(canonicalDir, SETTINGS_FILE_NAME)
                if (sf.exists()) {
                    val currentSettings = NotebookSettings.fromJson(sf.readText(), fallbackName = canonicalDir.name)
                    val cleanCanonicalName = stripDuplicateNotebookSuffix(currentSettings.notebookName.ifBlank { canonicalDir.name })
                        .ifBlank { canonicalDir.name }
                    if (cleanCanonicalName != currentSettings.notebookName) {
                        sf.writeText(currentSettings.copy(notebookName = cleanCanonicalName).toJson(), Charsets.UTF_8)
                    }
                }
            } catch (_: Throwable) {}

            if (mergedWords.size > canonicalWords.size) {
                saveNotebookHistoryLocalOnly(canonicalDir.name, mergedWords)
            } else {
                syncNotebookFileToNotebooksSubfolder(canonicalDir.name)
            }
            syncInternalBackupCumulatively(canonicalDir.name, mergedWords)
        }
    }

    /**
     * Ensures the root directory and the default "Allgemein" notebook exist.
     * Also migrates legacy history and preferences if present.
     */
    @Synchronized
    fun ensureInitialized() {
        if (isInitialized) return
        migrateLegacyStorageIfNeeded()
        val root = getRootDirectory()
        val notebookDirs = try {
            root.listFiles { file -> file.isDirectory }
        } catch (_: Exception) {
            null
        }

        if (notebookDirs.isNullOrEmpty()) {
            val defaultFolder = File(root, DEFAULT_NOTEBOOK_ID)
            try {
                defaultFolder.mkdirs()
            } catch (_: Throwable) {}

            // Initialize default settings.json
            val defaultSettings = NotebookSettings(
                notebookName = DEFAULT_NOTEBOOK_ID,
                themeColorId = "Dunkelblau",
                themeMode = "auto"
            )
            val settingsFile = File(defaultFolder, SETTINGS_FILE_NAME)
            try {
                settingsFile.writeText(defaultSettings.toJson())
            } catch (e: Exception) {
                Log.e(TAG, "Error creating initial settings.json", e)
            }

            // Check if System_Backups/history.json or legacy Documents/Deklination/history.json exists and migrate
            val defaultHistoryFile = File(defaultFolder, HISTORY_FILE_NAME)
            try {
                val sysBackupHistory = File(getSystemBackupsDirectory(), HISTORY_FILE_NAME)
                val legacyFile = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    "Deklination/history.json"
                )
                val sourceHistory = when {
                    sysBackupHistory.exists() && sysBackupHistory.length() > 0L -> sysBackupHistory
                    legacyFile.exists() && legacyFile.length() > 0L -> legacyFile
                    else -> null
                }
                if (sourceHistory != null) {
                    try {
                        sourceHistory.copyTo(defaultHistoryFile, overwrite = true)
                    } catch (e: Exception) {
                        defaultHistoryFile.writeText("[]")
                    }
                } else {
                    defaultHistoryFile.writeText("[]")
                }
            } catch (_: Exception) {
                try {
                    if (!defaultHistoryFile.exists()) {
                        defaultHistoryFile.writeText("[]")
                    }
                } catch (_: Exception) {}
            }

            prefs.edit().putString(PREF_ACTIVE_NOTEBOOK, DEFAULT_NOTEBOOK_ID).apply()
            syncNotebookFileToNotebooksSubfolder(DEFAULT_NOTEBOOK_ID)
        } else {
            for (dir in notebookDirs) {
                syncNotebookFileToNotebooksSubfolder(dir.name)
            }
        }
        isInitialized = true
    }

    fun getActiveNotebookId(): String {
        ensureInitialized()
        val saved = prefs.getString(PREF_ACTIVE_NOTEBOOK, null)
        if (!saved.isNullOrBlank()) {
            val folder = File(getRootDirectory(), saved)
            if (folder.exists() && folder.isDirectory) {
                return saved
            }
        }
        val firstAvailable = listAllNotebooks().firstOrNull()?.id ?: DEFAULT_NOTEBOOK_ID
        prefs.edit().putString(PREF_ACTIVE_NOTEBOOK, firstAvailable).apply()
        return firstAvailable
    }

    fun setActiveNotebookId(id: String) {
        val folder = File(getRootDirectory(), id)
        if (folder.exists() && folder.isDirectory) {
            prefs.edit().putString(PREF_ACTIVE_NOTEBOOK, id).apply()
        }
    }

    /**
     * Lists all Primary notebooks found in the root directory.
     * Strips any "Notebook_" prefix and duplicate suffixes (`_1`, `_2`, `(2)`) so in-app titles remain clean,
     * deduplicates identical notebooks, and orders them consistently (active notebook first, then clean alphabetical order)
     * across both the Main Screen dropdown menu and the Notebook Manager Primary tab.
     */
    @Synchronized
    fun listAllNotebooks(): List<Notebook> {
        ensureInitialized()
        val root = getRootDirectory()
        val dirs = try {
            root.listFiles { file -> file.isDirectory } ?: emptyArray()
        } catch (_: Throwable) {
            emptyArray()
        }

        if (dirs.size > 1) {
            consolidateDuplicateNotebookDirectories(root, getSystemBackupsDirectory())
        }

        val cleanedDirs = try {
            root.listFiles { file -> file.isDirectory } ?: emptyArray()
        } catch (_: Throwable) {
            emptyArray()
        }

        var list = cleanedDirs.mapNotNull { dir ->
            try {
                val cleanDirName = stripDuplicateNotebookSuffix(dir.name).ifBlank { dir.name }
                val settingsFile = File(dir, SETTINGS_FILE_NAME)
                val rawSettings = if (settingsFile.exists()) {
                    NotebookSettings.fromJson(settingsFile.readText(), fallbackName = cleanDirName)
                } else {
                    NotebookSettings(notebookName = cleanDirName)
                }
                val cleanDisplayName = stripDuplicateNotebookSuffix(
                    rawSettings.notebookName.ifBlank { cleanDirName }
                ).ifBlank { cleanDirName }
                val settings = rawSettings.copy(notebookName = cleanDisplayName)

                val historyFile = File(dir, HISTORY_FILE_NAME)
                val wordCount = if (historyFile.exists()) {
                    val stamp = historyFile.lastModified() xor (historyFile.length() shl 16)
                    val cacheKey = historyFile.absolutePath
                    val cachedEntry = wordCountCache[cacheKey]
                    if (cachedEntry != null && cachedEntry.first == stamp) {
                        cachedEntry.second
                    } else {
                        val content = historyFile.readText().trim()
                        val count = if (content.isNotEmpty() && content != "[]") {
                            try {
                                JSONArray(content).length()
                            } catch (e: Exception) {
                                0
                            }
                        } else 0
                        wordCountCache[cacheKey] = stamp to count
                        count
                    }
                } else 0

                Notebook(
                    id = dir.name,
                    name = cleanDisplayName,
                    lastModified = maxOf(dir.lastModified(), settingsFile.lastModified(), historyFile.lastModified()),
                    wordCount = wordCount,
                    settings = settings
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error reading notebook directory: ${dir.name}", e)
                null
            }
        }

        if (list.isEmpty()) {
            isInitialized = false
            ensureInitialized()
            val recheckedDirs = try {
                root.listFiles { file -> file.isDirectory } ?: emptyArray()
            } catch (_: Throwable) {
                emptyArray()
            }
            list = recheckedDirs.mapNotNull { dir ->
                try {
                    val settingsFile = File(dir, SETTINGS_FILE_NAME)
                    val settings = if (settingsFile.exists()) {
                        NotebookSettings.fromJson(settingsFile.readText(), fallbackName = dir.name)
                    } else {
                        NotebookSettings(notebookName = dir.name)
                    }
                    val cleanDisplayName = stripDuplicateNotebookSuffix(settings.notebookName.ifBlank { dir.name })
                        .ifBlank { dir.name }
                    Notebook(
                        id = dir.name,
                        name = cleanDisplayName,
                        lastModified = dir.lastModified(),
                        wordCount = 0,
                        settings = settings.copy(notebookName = cleanDisplayName)
                    )
                } catch (_: Throwable) {
                    null
                }
            }
        }

        // Defensive deduplication by normalized stripped name so identical Primary notebooks never stack in UI
        val activePrefId = prefs.getString(PREF_ACTIVE_NOTEBOOK, null)
        val deduplicatedList = list
            .groupBy { stripDuplicateNotebookSuffix(it.name).trim().lowercase(Locale.ROOT) }
            .values
            .mapNotNull { candidates ->
                if (candidates.size == 1) candidates.first()
                else candidates.maxByOrNull { nb ->
                    val activeScore = if (nb.id == activePrefId) 1_000_000_000L else 0L
                    val unsuffixedScore = if (!duplicateSuffixRegex.containsMatchIn(nb.id)) 50_000_000L else 0L
                    val unprefixedScore = if (!nb.id.startsWith(ExportSharingManager.NOTEBOOK_FILE_PREFIX, ignoreCase = true)) 10_000_000L else 0L
                    activeScore + unsuffixedScore + unprefixedScore + nb.wordCount.toLong() * 1000L + (nb.lastModified / 1000L)
                }
            }
            .sortedWith(
                compareByDescending<Notebook> { it.id == activePrefId }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name.trim() }
            )

        return if (deduplicatedList.isEmpty()) {
            listOf(
                Notebook(
                    id = DEFAULT_NOTEBOOK_ID,
                    name = DEFAULT_NOTEBOOK_ID,
                    lastModified = System.currentTimeMillis(),
                    wordCount = 0,
                    settings = NotebookSettings(notebookName = DEFAULT_NOTEBOOK_ID)
                )
            )
        } else {
            deduplicatedList
        }
    }

    fun getActiveNotebook(): Notebook {
        val activeId = getActiveNotebookId()
        val all = listAllNotebooks()
        return all.find { it.id == activeId }
            ?: all.firstOrNull()
            ?: Notebook(
                id = DEFAULT_NOTEBOOK_ID,
                name = DEFAULT_NOTEBOOK_ID,
                lastModified = System.currentTimeMillis(),
                wordCount = 0,
                settings = NotebookSettings(notebookName = DEFAULT_NOTEBOOK_ID)
            )
    }

    /**
     * Creates a new notebook directory with settings.json and empty history.json,
     * and writes `Notebooks/Notebook_<Name>.json`.
     */
    @Synchronized
    fun createNotebook(name: String, initialThemeColorId: String = "Schiefer", themeMode: String = "auto"): Notebook {
        ensureInitialized()
        val strippedDisplay = ExportSharingManager.stripNotebookPrefix(name.trim()).ifBlank { "Neues Notizbuch" }
        val cleanName = sanitizeFileName(strippedDisplay).ifBlank { "Neues_Notizbuch" }
        var folderName = cleanName
        var counter = 1
        val root = getRootDirectory()

        while (File(root, folderName).exists()) {
            folderName = "${cleanName}_$counter"
            counter++
        }

        val folder = File(root, folderName)
        folder.mkdirs()

        val settings = NotebookSettings(
            notebookName = strippedDisplay,
            themeColorId = initialThemeColorId,
            themeMode = themeMode
        )

        File(folder, SETTINGS_FILE_NAME).writeText(settings.toJson())
        File(folder, HISTORY_FILE_NAME).writeText("[]")
        syncNotebookFileToNotebooksSubfolder(folderName)

        val notebook = Notebook(
            id = folderName,
            name = settings.notebookName,
            lastModified = System.currentTimeMillis(),
            wordCount = 0,
            settings = settings
        )

        setActiveNotebookId(folderName)
        backupManager?.scheduleBackup(folderName)
        return notebook
    }

    /**
     * Renames a notebook (both directory and display name in settings.json, plus Notebooks/Notebook_<Name>.json).
     * Protected against nulls, blank inputs, invalid file characters, and crashes.
     */
    @Synchronized
    fun renameNotebook(notebookId: String, newName: String): Notebook {
        ensureInitialized()
        val root = getRootDirectory()
        val trimmedNew = ExportSharingManager.stripNotebookPrefix(newName.trim())
        if (trimmedNew.isBlank()) {
            return getActiveNotebook()
        }

        val oldFolder = File(root, notebookId)
        if (!oldFolder.exists() || !oldFolder.isDirectory) {
            Log.w(TAG, "Old notebook folder $notebookId does not exist, creating directory")
            try { oldFolder.mkdirs() } catch (_: Throwable) {}
        }

        val cleanName = sanitizeFileName(trimmedNew).ifBlank { "Notizbuch" }

        val oldSettingsFile = File(oldFolder, SETTINGS_FILE_NAME)
        val oldDisplayName = if (oldSettingsFile.exists()) {
            try {
                ExportSharingManager.stripNotebookPrefix(
                    NotebookSettings.fromJson(oldSettingsFile.readText(), fallbackName = notebookId).notebookName
                ).ifBlank { notebookId }
            } catch (_: Throwable) { notebookId }
        } else notebookId

        // Attempt folder rename if new clean name differs and does not collide
        val targetFolder = if (cleanName != oldFolder.name && !File(root, cleanName).exists()) {
            val newFile = File(root, cleanName)
            val renamed = try {
                oldFolder.renameTo(newFile)
            } catch (e: Throwable) {
                Log.w(TAG, "Failed to rename folder from ${oldFolder.name} to $cleanName", e)
                false
            }
            if (renamed) {
                if (getActiveNotebookId() == notebookId) {
                    setActiveNotebookId(cleanName)
                }
                newFile
            } else {
                oldFolder
            }
        } else {
            oldFolder
        }

        val effectiveId = targetFolder.name
        val settingsFile = File(targetFolder, SETTINGS_FILE_NAME)
        val currentSettings = if (settingsFile.exists()) {
            try {
                NotebookSettings.fromJson(settingsFile.readText(), fallbackName = trimmedNew)
            } catch (_: Throwable) {
                NotebookSettings(notebookName = trimmedNew)
            }
        } else {
            NotebookSettings(notebookName = trimmedNew)
        }

        val updatedSettings = currentSettings.copy(notebookName = trimmedNew)
        try {
            settingsFile.writeText(updatedSettings.toJson())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write updated settings.json on rename", e)
        }

        // Update Notebooks/Notebook_<Name>.json and System_Backups/Notebook_<Name>.json on rename
        try {
            val notebooksDir = getNotebooksDirectory()
            val systemBackupsDir = getSystemBackupsDirectory()
            val oldNotebookFileName = ExportSharingManager.buildNotebookFileName(oldDisplayName, defaultName = DEFAULT_NOTEBOOK_ID)
            val newNotebookFileName = ExportSharingManager.buildNotebookFileName(trimmedNew, defaultName = DEFAULT_NOTEBOOK_ID)
            val oldLegacyBackupName = "${ExportSharingManager.sanitizeFileName(oldDisplayName, defaultName = DEFAULT_NOTEBOOK_ID)}.json"

            // Preserve cumulative backup words in System_Backups/ across rename
            val existingBackupWords = loadSystemBackupHistoryByName(oldDisplayName)
            val currentPrimaryWords = loadNotebookHistoryLocalOnly(effectiveId)
            val mergedBackupWords = mergeItemsCumulatively(existingBackupWords, currentPrimaryWords)

            syncNotebookFileToNotebooksSubfolder(effectiveId)
            saveSystemBackupHistory(effectiveId, mergedBackupWords)

            if (!oldNotebookFileName.equals(newNotebookFileName, ignoreCase = true)) {
                File(notebooksDir, oldNotebookFileName).delete()
                File(systemBackupsDir, oldNotebookFileName).delete()
            }
            if (!oldLegacyBackupName.equals(newNotebookFileName, ignoreCase = true)) {
                File(systemBackupsDir, oldLegacyBackupName).delete()
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Failed updating Notebooks/ file on rename", e)
        }

        try {
            backupManager?.onNotebookRenamed(oldDisplayName, effectiveId)
        } catch (e: Throwable) {
            Log.w(TAG, "Failed to mirror notebook rename in backup manager", e)
        }

        return listAllNotebooks().find { it.id == effectiveId } ?: getActiveNotebook()
    }

    /**
     * Deletes a notebook and its directory completely.
     * Protected: Cannot delete the last remaining notebook.
     */
    @Synchronized
    fun deleteNotebook(notebookId: String): Boolean {
        val all = listAllNotebooks()
        if (all.size <= 1) {
            return false // Cannot delete the only existing notebook
        }

        val targetNb = all.find { it.id == notebookId }
        val nbName = targetNb?.name ?: notebookId

        val folder = File(getRootDirectory(), notebookId)
        if (folder.exists()) {
            folder.deleteRecursively()
        }

        try {
            val notebooksDir = getNotebooksDirectory()
            val systemBackupsDir = getSystemBackupsDirectory()
            val nbFileName = ExportSharingManager.buildNotebookFileName(nbName, defaultName = DEFAULT_NOTEBOOK_ID)
            File(notebooksDir, nbFileName).delete()
            File(systemBackupsDir, nbFileName).delete()
        } catch (_: Throwable) {}

        if (getActiveNotebookId() == notebookId) {
            val remaining = listAllNotebooks()
            setActiveNotebookId(remaining.firstOrNull()?.id ?: DEFAULT_NOTEBOOK_ID)
        }

        backupManager?.onNotebookDeleted(nbName)

        return true
    }

    /**
     * Saves settings for the currently active notebook.
     */
    @Synchronized
    fun saveActiveSettings(settings: NotebookSettings): Boolean {
        return saveNotebookSettings(getActiveNotebookId(), settings)
    }

    @Synchronized
    fun loadNotebookSettings(notebookId: String): NotebookSettings {
        val cleanFallback = ExportSharingManager.stripNotebookPrefix(notebookId).ifBlank { notebookId }
        val folder = File(getRootDirectory(), notebookId)
        val file = File(folder, SETTINGS_FILE_NAME)
        if (!file.exists()) return NotebookSettings(notebookName = cleanFallback)
        return try {
            val parsed = NotebookSettings.fromJson(file.readText(), fallbackName = cleanFallback)
            parsed.copy(
                notebookName = ExportSharingManager.stripNotebookPrefix(parsed.notebookName).ifBlank { cleanFallback }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error loading notebook settings for $notebookId", e)
            NotebookSettings(notebookName = cleanFallback)
        }
    }

    @Synchronized
    fun saveNotebookSettings(notebookId: String, settings: NotebookSettings): Boolean {
        return try {
            val cleanName = ExportSharingManager.stripNotebookPrefix(settings.notebookName).ifBlank {
                ExportSharingManager.stripNotebookPrefix(notebookId).ifBlank { DEFAULT_NOTEBOOK_ID }
            }
            val cleanSettings = settings.copy(notebookName = cleanName)
            val folder = File(getRootDirectory(), notebookId)
            folder.mkdirs()
            val file = File(folder, SETTINGS_FILE_NAME)
            file.writeText(cleanSettings.toJson())
            syncNotebookFileToNotebooksSubfolder(notebookId)
            backupManager?.scheduleBackup(notebookId)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving notebook settings", e)
            false
        }
    }

    /**
     * Normalized deduplication key for comparing history entries by word/sentence and grammatical type.
     */
    fun normalizeHistoryKey(word: String, type: String): String {
        return "${word.trim().lowercase()}_${type.trim().lowercase()}"
    }

    /**
     * Cumulatively merges `incomingItems` into `existingItems` without losing or reordering
     * any existing entries, and without creating duplicate entries.
     * Existing entries keep their original position and original timestamp; only `manualTranslation`
     * is updated in-place if newly provided on an incoming item.
     */
    fun mergeItemsCumulatively(
        existingItems: List<WordHistoryEntity>,
        incomingItems: List<WordHistoryEntity>
    ): List<WordHistoryEntity> {
        if (incomingItems.isEmpty()) return existingItems
        val result = existingItems.toMutableList()
        val seenKeys = result.mapTo(HashSet()) { normalizeHistoryKey(it.word, it.type) }
        val newToPrepend = mutableListOf<WordHistoryEntity>()

        for (candidate in incomingItems) {
            val cleanWord = candidate.word.trim()
            if (cleanWord.isBlank() || cleanWord.equals("null", ignoreCase = true)) continue
            val key = normalizeHistoryKey(cleanWord, candidate.type)
            if (seenKeys.add(key)) {
                newToPrepend.add(candidate)
            } else {
                val existingIdx = result.indexOfFirst { normalizeHistoryKey(it.word, it.type) == key }
                if (existingIdx >= 0) {
                    val current = result[existingIdx]
                    val updatedManual = if (candidate.manualTranslation.isNotBlank()) {
                        candidate.manualTranslation
                    } else {
                        current.manualTranslation
                    }
                    if (updatedManual != current.manualTranslation) {
                        result[existingIdx] = current.copy(manualTranslation = updatedManual)
                    }
                }
            }
        }

        if (newToPrepend.isNotEmpty()) {
            result.addAll(0, newToPrepend)
        }
        return result
    }

    /**
     * Resolves the display name of a notebook by ID without triggering full recursive initialization.
     */
    private fun resolveCleanNotebookDisplayName(notebookId: String): String {
        val folder = File(getRootDirectory(), notebookId)
        val settingsFile = File(folder, SETTINGS_FILE_NAME)
        val rawName = if (settingsFile.exists()) {
            try {
                NotebookSettings.fromJson(settingsFile.readText(), fallbackName = notebookId).notebookName
            } catch (_: Throwable) {
                notebookId
            }
        } else {
            notebookId
        }
        return ExportSharingManager.stripNotebookPrefix(rawName.ifBlank { notebookId }).ifBlank { DEFAULT_NOTEBOOK_ID }
    }

    private fun loadSystemBackupHistoryByName(displayName: String): List<WordHistoryEntity> {
        val cleanName = ExportSharingManager.stripNotebookPrefix(displayName).ifBlank { DEFAULT_NOTEBOOK_ID }
        val sysDir = getSystemBackupsDirectory()
        val prefixedFile = File(sysDir, ExportSharingManager.buildNotebookFileName(cleanName, defaultName = DEFAULT_NOTEBOOK_ID))
        val legacyFile = File(sysDir, "${ExportSharingManager.sanitizeFileName(cleanName, defaultName = DEFAULT_NOTEBOOK_ID)}.json")
        val targetFile = when {
            prefixedFile.exists() && prefixedFile.length() > 0L -> prefixedFile
            legacyFile.exists() && legacyFile.length() > 0L -> legacyFile
            else -> return emptyList()
        }
        return try {
            val text = targetFile.readText(Charsets.UTF_8).trim()
            if (text.startsWith("[")) {
                parseJsonHistory(text)
            } else if (text.startsWith("{")) {
                val root = JSONObject(text)
                val arr = root.optJSONArray("words") ?: root.optJSONArray("history")
                if (arr != null) parseJsonHistory(arr.toString()) else emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error loading system backup history for $cleanName: ${e.message}")
            emptyList()
        }
    }

    /**
     * Loads the cumulative internal backup history for a notebook from `System_Backups/Notebook_<Name>.json`
     * (with fallback to legacy `System_Backups/<Name>.json`).
     */
    @Synchronized
    fun loadSystemBackupHistory(notebookId: String = getActiveNotebookId()): List<WordHistoryEntity> {
        val cleanName = resolveCleanNotebookDisplayName(notebookId)
        return loadSystemBackupHistoryByName(cleanName)
    }

    /**
     * Writes the cumulative internal backup file `System_Backups/Notebook_<Name>.json` for a notebook.
     */
    @Synchronized
    fun saveSystemBackupHistory(notebookId: String, items: List<WordHistoryEntity>): Boolean {
        return try {
            val cleanName = resolveCleanNotebookDisplayName(notebookId)
            val settings = loadNotebookSettings(notebookId).copy(notebookName = cleanName)
            val serializedWords = serializeHistoryToJson(items)
            val jsonObj = JSONObject().apply {
                put("version", 2)
                put("notebook_name", cleanName)
                put("notebookName", cleanName)
                put("exported_at", System.currentTimeMillis())
                put("settings", settings.toExportSettingsJson())
                put("words", serializedWords)
                put("history", serializedWords)
            }
            val sysDir = getSystemBackupsDirectory()
            val fileName = ExportSharingManager.buildNotebookFileName(cleanName, defaultName = DEFAULT_NOTEBOOK_ID)
            val targetFile = File(sysDir, fileName)
            val tempFile = File(sysDir, ".tmp_sysbackup_${System.nanoTime()}.json")
            tempFile.writeText(jsonObj.toString(2), Charsets.UTF_8)
            tempFile.copyTo(targetFile, overwrite = true)
            tempFile.delete()
            true
        } catch (e: Exception) {
            Log.w(TAG, "Failed saving system backup history for $notebookId: ${e.message}")
            false
        }
    }

    /**
     * Cumulatively merges `incomingItems` into the internal system backup `System_Backups/Notebook_<Name>.json`.
     */
    @Synchronized
    fun syncInternalBackupCumulatively(notebookId: String, incomingItems: List<WordHistoryEntity>) {
        if (incomingItems.isEmpty()) return
        val existingBackup = loadSystemBackupHistory(notebookId)
        val merged = mergeItemsCumulatively(existingBackup, incomingItems)
        saveSystemBackupHistory(notebookId, merged)
    }

    /**
     * Deletes a single specific entry from the internal system backup `System_Backups/Notebook_<Name>.json`
     * (and any legacy `System_Backups/<Name>.json` archive).
     */
    @Synchronized
    private fun deleteFromInternalSystemBackup(
        notebookId: String,
        id: Long,
        word: String? = null,
        type: String? = null
    ) {
        try {
            val existingBackup = loadSystemBackupHistory(notebookId).toMutableList()
            if (existingBackup.isEmpty()) return
            val removed = existingBackup.removeAll {
                it.id == id || (word != null && type != null &&
                    it.word.trim().equals(word.trim(), ignoreCase = true) &&
                    it.type.trim().equals(type.trim(), ignoreCase = true))
            }
            if (removed) {
                saveSystemBackupHistory(notebookId, existingBackup)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error deleting item from internal system backup: ${e.message}")
        }
    }

    /**
     * Loads the search history items for the active notebook.
     */
    @Synchronized
    fun loadActiveHistory(): List<WordHistoryEntity> {
        return loadNotebookHistory(getActiveNotebookId())
    }

    /**
     * Loads the local Primary storage history for a notebook without querying external SAF folders.
     */
    @Synchronized
    fun loadNotebookHistoryLocalOnly(notebookId: String): List<WordHistoryEntity> {
        val folder = File(getRootDirectory(), notebookId)
        val file = File(folder, HISTORY_FILE_NAME)
        if (file.exists() && file.length() > 0L) {
            return try {
                val content = file.readText().trim()
                parseJsonHistory(content)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading notebook history for $notebookId", e)
                emptyList()
            }
        }

        try {
            val cleanName = resolveCleanNotebookDisplayName(notebookId)
            val nbFile = File(getNotebooksDirectory(), ExportSharingManager.buildNotebookFileName(cleanName, defaultName = DEFAULT_NOTEBOOK_ID))
            if (nbFile.exists() && nbFile.length() > 0L) {
                val rootObj = JSONObject(nbFile.readText())
                val arr = rootObj.optJSONArray("words") ?: rootObj.optJSONArray("history")
                if (arr != null) {
                    return parseJsonHistory(arr.toString())
                }
            }
        } catch (_: Exception) {}

        return emptyList()
    }

    /**
     * Saves history items directly to local Primary storage (`System_Backups/Notizbücher/<id>/history.json`
     * and `Notebooks/Notebook_<Name>.json`) without triggering external backup jobs.
     */
    @Synchronized
    fun saveNotebookHistoryLocalOnly(notebookId: String, items: List<WordHistoryEntity>): Boolean {
        val folder = File(getRootDirectory(), notebookId)
        folder.mkdirs()
        val file = File(folder, HISTORY_FILE_NAME)
        return try {
            val jsonArray = serializeHistoryToJson(items)
            file.writeText(jsonArray.toString(2))
            val stamp = file.lastModified() xor (file.length() shl 16)
            wordCountCache[file.absolutePath] = stamp to items.size
            syncNotebookFileToNotebooksSubfolder(notebookId)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving local notebook history for $notebookId", e)
            false
        }
    }

    @Synchronized
    fun loadNotebookHistory(notebookId: String): List<WordHistoryEntity> {
        // 1. If designated primary location is registered and reachable, read from it as source of truth
        val primaryWords = backupManager?.loadHistoryFromPrimary(notebookId)
        if (primaryWords != null) {
            return primaryWords
        }

        // 2. Fall back to local internal primary storage
        return loadNotebookHistoryLocalOnly(notebookId)
    }

    /**
     * Saves the search history items to the active notebook.
     * When triggerBackup is true, also merges cumulatively into internal and external backup locations.
     * When triggerBackup is false (e.g. during "Clear All / Leeren"), only the primary
     * storage is updated/cleared, leaving all backup locations untouched with their historical data.
     */
    @Synchronized
    fun saveActiveHistory(items: List<WordHistoryEntity>, triggerBackup: Boolean = true): Boolean {
        val activeId = getActiveNotebookId()
        val savedLocal = saveNotebookHistoryLocalOnly(activeId, items)
        if (triggerBackup) {
            if (items.isNotEmpty()) {
                syncInternalBackupCumulatively(activeId, items)
                backupManager?.scheduleBackup(activeId)
            } else {
                backupManager?.syncClearAllPrimary(activeId)
            }
        } else if (items.isEmpty()) {
            backupManager?.syncClearAllPrimary(activeId)
        }
        return savedLocal
    }

    /**
     * Updates ONLY the Primary Storage for the active notebook (used by `clearHistoryByType` / partial primary clear),
     * leaving all Backup Storages (`System_Backups/` and non-primary BackupFolders) untouched.
     */
    @Synchronized
    fun saveActiveHistoryPrimaryOnly(items: List<WordHistoryEntity>): Boolean {
        val activeId = getActiveNotebookId()
        val savedLocal = saveNotebookHistoryLocalOnly(activeId, items)
        try {
            backupManager?.syncPrimaryOnly(activeId, items)
        } catch (e: Exception) {
            Log.w(TAG, "Error syncing primary-only update: ${e.message}")
        }
        return savedLocal
    }

    /**
     * Adds a truly new entry to Primary Storage and merges it cumulatively into Backup Storage.
     * If the entry (matching word/sentence and type) ALREADY exists in Primary Storage:
     *   - It is NOT duplicated in Primary Storage or Backup Storage.
     *   - It stays in its original position with its original timestamp (updating manualTranslation in-place if changed).
     */
    @Synchronized
    fun appendOrUpdateItemInActiveHistory(item: WordHistoryEntity): Boolean {
        val activeId = getActiveNotebookId()
        val items = loadActiveHistory().toMutableList()
        val targetKey = normalizeHistoryKey(item.word, item.type)
        val existingIndex = items.indexOfFirst {
            normalizeHistoryKey(it.word, it.type) == targetKey
        }

        val effectiveItem: WordHistoryEntity
        val saved: Boolean
        if (existingIndex >= 0) {
            // Item already exists in Primary Storage: keep its original position and original timestamp!
            val existing = items[existingIndex]
            val effectiveManual = if (item.manualTranslation.isNotBlank()) {
                item.manualTranslation
            } else {
                existing.manualTranslation
            }
            effectiveItem = existing.copy(
                meaningEnglish = item.meaningEnglish.ifBlank { existing.meaningEnglish },
                data = item.data.ifBlank { existing.data },
                rawJsonResult = item.rawJsonResult.ifBlank { existing.rawJsonResult },
                gender = item.gender.ifBlank { existing.gender },
                genderArticle = item.genderArticle.ifBlank { existing.genderArticle },
                manualTranslation = effectiveManual
            )
            if (effectiveItem != existing) {
                items[existingIndex] = effectiveItem
                saved = saveNotebookHistoryLocalOnly(activeId, items)
            } else {
                saved = true
            }
        } else {
            // Truly new entry: add to Primary Storage
            effectiveItem = item
            items.add(0, effectiveItem)
            saved = saveNotebookHistoryLocalOnly(activeId, items)
        }

        // Merge cumulatively into Internal System Backup without overwriting older entries or duplicating
        try {
            syncInternalBackupCumulatively(activeId, items)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing internal system backup", e)
        }

        // Sync to external Primary and Backup folders
        try {
            backupManager?.syncEntryAdded(activeId, effectiveItem, fullUpdatedItems = items)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing added entry to backups", e)
        }
        return saved
    }

    /**
     * Single-Item Deletion: deletes the specific entry from BOTH Primary Storage and ALL Backup Storages
     * (Internal System Backup and all External Backup folders).
     */
    @Synchronized
    fun deleteItemInActiveHistory(id: Long, word: String? = null, type: String? = null): Boolean {
        val activeId = getActiveNotebookId()
        val items = loadActiveHistory().toMutableList()
        val removed = items.removeAll {
            it.id == id || (word != null && type != null &&
                it.word.trim().equals(word.trim(), ignoreCase = true) &&
                it.type.trim().equals(type.trim(), ignoreCase = true))
        }
        if (removed) {
            saveNotebookHistoryLocalOnly(activeId, items)
        }
        deleteFromInternalSystemBackup(activeId, id, word, type)
        try {
            backupManager?.syncEntryDeleted(activeId, id, word, type, fullUpdatedItems = items)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing deleted entry to backups", e)
        }
        return true
    }

    /**
     * Clears the active notebook's history from the Primary Storage ONLY ("Clear All").
     * All Backup Storages (Internal System Backup and External Backup folders) remain completely untouched!
     */
    @Synchronized
    fun clearActiveHistory(): Boolean {
        val activeId = getActiveNotebookId()
        val cleared = saveNotebookHistoryLocalOnly(activeId, emptyList())
        try {
            backupManager?.syncClearAllPrimary(activeId)
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing primary backup storage", e)
        }
        return cleared
    }

    /**
     * Promotes the internal system backup (`System_Backups/Notebook_<Name>.json`) for a notebook
     * to become the new clean baseline for Primary Storage and all corresponding Backup Storages
     * (Fresh Baseline Rule — no retroactive deletions from previous Primary).
     */
    @Synchronized
    fun promoteSystemBackupToPrimary(notebookId: String = getActiveNotebookId()): List<WordHistoryEntity> {
        val backupBaseline = loadSystemBackupHistory(notebookId)
        saveNotebookHistoryLocalOnly(notebookId, backupBaseline)
        saveSystemBackupHistory(notebookId, backupBaseline)
        try {
            backupManager?.syncPromotedBaselineToAllFolders(notebookId, backupBaseline)
        } catch (e: Exception) {
            Log.w(TAG, "Error syncing promoted system backup baseline to folders: ${e.message}")
        }
        return backupBaseline
    }

    /**
     * Scans `Documents/Deklination/System_Backups/` for notebook backup `.json` files
     * (excluding raw internal state/archive files like `history.json`, `settings.json`,
     * `legacy_root_*`, and `archived_*`).
     */
    @Synchronized
    fun listSystemBackupNotebooks(): List<BackupNotebookEntry> {
        val sysDir = getSystemBackupsDirectory()
        val files = try {
            sysDir.listFiles { f ->
                f.isFile &&
                    f.name.endsWith(".json", ignoreCase = true) &&
                    !f.name.startsWith(".") &&
                    !f.name.equals(HISTORY_FILE_NAME, ignoreCase = true) &&
                    !f.name.equals(SETTINGS_FILE_NAME, ignoreCase = true) &&
                    !f.name.startsWith("legacy_root_", ignoreCase = true) &&
                    !f.name.startsWith("archived_", ignoreCase = true)
            } ?: emptyArray()
        } catch (_: Throwable) {
            emptyArray()
        }

        val entries = files.mapNotNull { file ->
            try {
                val text = file.readText(Charsets.UTF_8).trim()
                if (text.isEmpty()) return@mapNotNull null

                val fallbackName = stripDuplicateNotebookSuffix(
                    ExportSharingManager.stripNotebookPrefix(
                        file.name.removeSuffix(".json").removeSuffix(".JSON")
                    )
                ).ifBlank { DEFAULT_NOTEBOOK_ID }

                var displayName = fallbackName
                var wordCount = 0
                var themeColorId = "Dunkelblau"

                if (text.startsWith("[")) {
                    val arr = JSONArray(text)
                    wordCount = arr.length()
                } else if (text.startsWith("{")) {
                    val root = JSONObject(text)
                    val arr = root.optJSONArray("words") ?: root.optJSONArray("history") ?: return@mapNotNull null
                    wordCount = arr.length()
                    val rawName = root.optString("notebook_name").ifBlank { root.optString("notebookName") }.trim()
                    if (rawName.isNotBlank()) {
                        displayName = stripDuplicateNotebookSuffix(
                            ExportSharingManager.stripNotebookPrefix(rawName)
                        ).ifBlank { fallbackName }
                    }
                    val settingsObj = root.optJSONObject("settings")
                    if (settingsObj != null) {
                        themeColorId = NotebookSettings.parseFromExportSettingsJson(settingsObj, displayName).themeColorId
                    }
                } else {
                    return@mapNotNull null
                }

                BackupNotebookEntry(
                    uniqueKey = "system::${file.name}",
                    notebookId = sanitizeFileName(displayName).ifBlank { DEFAULT_NOTEBOOK_ID },
                    displayName = displayName,
                    wordCount = wordCount,
                    lastModified = file.lastModified(),
                    themeColorId = themeColorId,
                    sourceType = BackupNotebookSourceType.SYSTEM_BACKUP,
                    sourceLabel = SYSTEM_BACKUPS_SUBFOLDER,
                    sourceFolderUri = null,
                    fileName = file.name
                )
            } catch (e: Exception) {
                Log.w(TAG, "Error inspecting system backup file ${file.name}: ${e.message}")
                null
            }
        }

        // Prefer prefixed `Notebook_<Name>.json` over legacy `<Name>.json` if both exist in System_Backups
        return entries
            .groupBy { it.displayName.trim().lowercase(Locale.ROOT) }
            .values
            .mapNotNull { group ->
                group.maxByOrNull { entry ->
                    val prefixScore = if (entry.fileName.startsWith(ExportSharingManager.NOTEBOOK_FILE_PREFIX, ignoreCase = true)) 1_000_000_000L else 0L
                    val wordScore = entry.wordCount.toLong() * 1000L
                    prefixScore + wordScore + (entry.lastModified / 1000L)
                }
            }
            .sortedWith(String.CASE_INSENSITIVE_ORDER.let { cmp ->
                Comparator { a, b -> cmp.compare(a.displayName.trim(), b.displayName.trim()) }
            })
    }

    /**
     * Reads full words and optional settings from an internal `System_Backups/` JSON file.
     */
    fun readSystemBackupSnapshot(fileName: String): Pair<List<WordHistoryEntity>, NotebookSettings?> {
        return try {
            val file = File(getSystemBackupsDirectory(), fileName)
            if (!file.exists() || !file.isFile) return emptyList<WordHistoryEntity>() to null
            val text = file.readText(Charsets.UTF_8).trim()
            parseBackupSnapshotContent(text, fileName)
        } catch (e: Exception) {
            Log.w(TAG, "Failed reading system backup snapshot $fileName: ${e.message}")
            emptyList<WordHistoryEntity>() to null
        }
    }

    /**
     * Parses a JSON snapshot string into (words, optional settings).
     */
    fun parseBackupSnapshotContent(text: String, fallbackFileName: String): Pair<List<WordHistoryEntity>, NotebookSettings?> {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return emptyList<WordHistoryEntity>() to null
        return when (val parsed = parseImportedJson(trimmed, fallbackFileName)) {
            is ImportedJsonResult.SingleNotebook -> parsed.words to parsed.settings
            is ImportedJsonResult.MultipleNotebooks -> {
                val first = parsed.packages.firstOrNull()
                if (first != null) first.words to first.settings else emptyList<WordHistoryEntity>() to null
            }
            ImportedJsonResult.EmptyOrInvalid -> emptyList<WordHistoryEntity>() to null
        }
    }

    /**
     * Returns raw JSON text for a backup snapshot stored in `System_Backups/`.
     */
    fun readSystemBackupRawJson(fileName: String): String? {
        return try {
            val file = File(getSystemBackupsDirectory(), fileName)
            if (file.exists() && file.isFile) file.readText(Charsets.UTF_8) else null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Promotes any backup notebook snapshot (from `System_Backups/` or an external Backup folder)
     * to become the active Primary notebook using the Fresh Baseline Rule:
     * - If a Primary notebook with the same clean name exists, its contents are replaced cleanly
     *   by the promoted snapshot (preserving existing API keys and folder URI).
     * - Otherwise, a new Primary notebook is created with those contents.
     * - Both `System_Backups/` and all registered folders receive this snapshot as the fresh baseline.
     */
    @Synchronized
    fun promoteBackupSnapshot(
        displayName: String,
        words: List<WordHistoryEntity>,
        importedSettings: NotebookSettings? = null
    ): Notebook {
        ensureInitialized()
        val cleanDisplay = ExportSharingManager.stripNotebookPrefix(displayName.trim()).ifBlank { DEFAULT_NOTEBOOK_ID }
        val existingMatch = listAllNotebooks().find {
            it.name.trim().equals(cleanDisplay, ignoreCase = true) ||
                it.id.trim().equals(sanitizeFileName(cleanDisplay), ignoreCase = true)
        }

        val targetId: String
        val effectiveSettings: NotebookSettings
        if (existingMatch != null) {
            targetId = existingMatch.id
            val localSettings = loadNotebookSettings(targetId)
            effectiveSettings = if (importedSettings != null) {
                localSettings.copy(
                    notebookName = cleanDisplay,
                    themeColorId = importedSettings.themeColorId.ifBlank { localSettings.themeColorId },
                    themeMode = importedSettings.themeMode.ifBlank { localSettings.themeMode }
                )
            } else {
                localSettings.copy(notebookName = cleanDisplay)
            }
            val folder = File(getRootDirectory(), targetId).apply { mkdirs() }
            File(folder, SETTINGS_FILE_NAME).writeText(effectiveSettings.toJson(), Charsets.UTF_8)
        } else {
            val cleanBase = sanitizeFileName(cleanDisplay).ifBlank { DEFAULT_NOTEBOOK_ID }
            val root = getRootDirectory()
            var candidateId = cleanBase
            var counter = 1
            while (File(root, candidateId).exists()) {
                candidateId = "${cleanBase}_$counter"
                counter++
            }
            targetId = candidateId
            val folder = File(root, targetId).apply { mkdirs() }
            effectiveSettings = (importedSettings ?: NotebookSettings(notebookName = cleanDisplay)).copy(
                notebookName = cleanDisplay,
                customApiKey = "",
                openRouterApiKey = "",
                storageFolderUri = ""
            )
            File(folder, SETTINGS_FILE_NAME).writeText(effectiveSettings.toJson(), Charsets.UTF_8)
        }

        // Write exact baseline to Primary storage, System_Backups baseline, and all registered folders
        saveNotebookHistoryLocalOnly(targetId, words)
        saveSystemBackupHistory(targetId, words)
        setActiveNotebookId(targetId)

        try {
            backupManager?.syncPromotedBaselineToAllFolders(targetId, words)
        } catch (e: Exception) {
            Log.w(TAG, "Error syncing promoted backup snapshot to folders: ${e.message}")
        }

        return getActiveNotebook()
    }

    // =========================================================================
    // Direct JSON Export and Import (Direct JSON, zero ZIP compression)
    // =========================================================================

    /**
     * Builds a formatted JSON string for a notebook with the given words list.
     */
    fun buildNotebookJsonString(notebookId: String, items: List<WordHistoryEntity>): String {
        val notebook = listAllNotebooks().find { it.id == notebookId }
        val name = ExportSharingManager.stripNotebookPrefix(notebook?.name ?: notebookId).ifBlank { DEFAULT_NOTEBOOK_ID }
        val settings = loadNotebookSettings(notebookId)

        val obj = JSONObject().apply {
            put("version", 2)
            put("notebook_name", name)
            put("notebookName", name)
            put("exported_at", System.currentTimeMillis())
            put("settings", settings.toExportSettingsJson())
            put("words", serializeHistoryToJson(items))
            put("history", serializeHistoryToJson(items))
        }
        return obj.toString(2)
    }

    /**
     * Exports a single notebook to a formatted JSON string.
     * Contains notebook name, full settings (excluding keys and folder URI), and word history.
     */
    fun exportNotebookToJsonString(notebookId: String): String {
        return buildNotebookJsonString(notebookId, loadNotebookHistory(notebookId))
    }

    /**
     * Exports all or selected notebooks into a single combined JSON string.
     * Includes metadata, full settings per notebook, and history.
     */
    fun exportNotebooksCombinedJsonString(notebookIds: List<String> = emptyList()): String {
        val all = listAllNotebooks()
        val targets = if (notebookIds.isEmpty()) all else all.filter { it.id in notebookIds }
        val rootObj = JSONObject().apply {
            put("version", 2)
            put("exported_at", System.currentTimeMillis())
        }

        val notebooksArray = JSONArray()
        for (nb in targets) {
            val cleanName = ExportSharingManager.stripNotebookPrefix(nb.name).ifBlank { nb.name }
            val items = loadNotebookHistory(nb.id)
            val settings = loadNotebookSettings(nb.id)
            val sectionObj = JSONObject().apply {
                put("notebook_name", cleanName)
                put("notebookName", cleanName)
                put("settings", settings.toExportSettingsJson())
                put("words", serializeHistoryToJson(items))
                put("history", serializeHistoryToJson(items))
            }
            notebooksArray.put(sectionObj)
            rootObj.put(cleanName, sectionObj)
        }
        rootObj.put("notebooks", notebooksArray)
        return rootObj.toString(2)
    }

    /**
     * Exports all or selected notebooks as individual JSON files (`Notebook_<Name>.json`)
     * into the `Notebooks/` subfolder of a SAF folder (treeUri).
     * Returns the count of successfully created JSON files.
     */
    fun exportNotebooksToTreeUri(context: Context, treeUri: Uri, notebookIds: List<String> = emptyList()): Int {
        val all = listAllNotebooks()
        val targets = if (notebookIds.isEmpty()) all else all.filter { it.id in notebookIds }
        val documentDir = androidx.documentfile.provider.DocumentFile.fromTreeUri(context, treeUri) ?: return 0
        if (!documentDir.canWrite()) return 0

        val targetSubDir = if (documentDir.name?.equals(NOTEBOOKS_SUBFOLDER, ignoreCase = true) == true) {
            documentDir
        } else {
            // Also ensure Exports and System_Backups subfolders exist in the selected folder
            try {
                if (documentDir.findFile(EXPORTS_SUBFOLDER) == null) {
                    documentDir.createDirectory(EXPORTS_SUBFOLDER)
                }
                if (documentDir.findFile(SYSTEM_BACKUPS_SUBFOLDER) == null) {
                    documentDir.createDirectory(SYSTEM_BACKUPS_SUBFOLDER)
                }
            } catch (_: Exception) {}
            documentDir.findFile(NOTEBOOKS_SUBFOLDER)
                ?: documentDir.createDirectory(NOTEBOOKS_SUBFOLDER)
                ?: documentDir
        }

        var count = 0
        for (nb in targets) {
            try {
                val cleanName = ExportSharingManager.stripNotebookPrefix(nb.name).ifBlank { DEFAULT_NOTEBOOK_ID }
                val items = loadNotebookHistory(nb.id)
                val settings = loadNotebookSettings(nb.id)
                val jsonString = JSONObject().apply {
                    put("version", 2)
                    put("notebook_name", cleanName)
                    put("notebookName", cleanName)
                    put("exported_at", System.currentTimeMillis())
                    put("settings", settings.toExportSettingsJson())
                    put("words", serializeHistoryToJson(items))
                    put("history", serializeHistoryToJson(items))
                }.toString(2)

                val fileName = ExportSharingManager.buildNotebookFileName(cleanName, defaultName = "Notizbuch")

                // Delete existing file in folder to overwrite cleanly
                val existing = targetSubDir.findFile(fileName)
                existing?.delete()

                val newDoc = targetSubDir.createFile("application/json", fileName)
                if (newDoc != null) {
                    context.contentResolver.openOutputStream(newDoc.uri)?.use { os ->
                        os.write(jsonString.toByteArray(Charsets.UTF_8))
                    }
                    count++
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error writing notebook ${nb.name} to tree URI", e)
            }
        }
        return count
    }

    /**
     * Resolves a non-conflicting notebook name by appending a suffix like (2), (3) if it already exists.
     */
    fun resolveUniqueNotebookName(baseName: String): String {
        val existingNames = listAllNotebooks().map { it.name.trim().lowercase() }.toSet()
        val cleanBase = ExportSharingManager.stripNotebookPrefix(baseName).trim().ifBlank { "Notizbuch" }
        if (!existingNames.contains(cleanBase.lowercase())) {
            return cleanBase
        }
        var counter = 2
        while (true) {
            val candidate = "$cleanBase ($counter)"
            if (!existingNames.contains(candidate.lowercase())) {
                return candidate
            }
            counter++
        }
    }

    /**
     * Parses an arbitrary JSON string into either a single notebook or multiple notebooks representation.
     * Supports both new formats (`Notebook_<Name>.json` with settings) and legacy formats (`<Name>.json`, `history.json`).
     * Automatically strips any `Notebook_` prefix from detected notebook names.
     */
    fun parseImportedJson(jsonString: String, fallbackFileName: String = "Importiertes_Notizbuch"): ImportedJsonResult {
        val trimmed = jsonString.trim()
        if (trimmed.isEmpty()) return ImportedJsonResult.EmptyOrInvalid

        val cleanFallback = ExportSharingManager.stripNotebookPrefix(
            fallbackFileName.removeSuffix(".json").removeSuffix(".JSON").trim()
        ).ifBlank { "Importiertes_Notizbuch" }

        try {
            // Case 1: Root is a JSON Array of words (legacy format)
            if (trimmed.startsWith("[")) {
                val items = parseJsonHistory(trimmed)
                return if (items.isNotEmpty()) {
                    ImportedJsonResult.SingleNotebook(cleanFallback, items, null)
                } else {
                    ImportedJsonResult.EmptyOrInvalid
                }
            }

            val rootObj = JSONObject(trimmed)

            // Case 2: Root object has "words" or "history" array directly (single notebook)
            if (rootObj.has("words") || rootObj.has("history")) {
                val wordsArray = rootObj.optJSONArray("words") ?: rootObj.optJSONArray("history")
                val rawName = rootObj.optString("notebook_name").ifBlank { rootObj.optString("notebookName") }.trim()
                val name = ExportSharingManager.stripNotebookPrefix(rawName).ifBlank { cleanFallback }
                val items = if (wordsArray != null) parseJsonHistory(wordsArray.toString()) else emptyList()
                val settingsObj = rootObj.optJSONObject("settings")
                val settings = if (settingsObj != null) NotebookSettings.parseFromExportSettingsJson(settingsObj, name) else null
                return if (items.isNotEmpty() || settings != null) {
                    ImportedJsonResult.SingleNotebook(name, items, settings)
                } else {
                    ImportedJsonResult.EmptyOrInvalid
                }
            }

            // Case 3: Root object contains "notebooks" array
            val notebooksArray = rootObj.optJSONArray("notebooks")
            if (notebooksArray != null && notebooksArray.length() > 0) {
                val packages = mutableListOf<ImportedNotebookPackage>()
                for (i in 0 until notebooksArray.length()) {
                    val itemObj = notebooksArray.getJSONObject(i)
                    val rawName = itemObj.optString("notebook_name").ifBlank { itemObj.optString("notebookName", "Notizbuch ${i + 1}") }
                    val name = ExportSharingManager.stripNotebookPrefix(rawName).ifBlank { "Notizbuch ${i + 1}" }
                    val wordsArray = itemObj.optJSONArray("words") ?: itemObj.optJSONArray("history")
                    val items = if (wordsArray != null) parseJsonHistory(wordsArray.toString()) else emptyList()
                    val settingsObj = itemObj.optJSONObject("settings")
                    val settings = if (settingsObj != null) NotebookSettings.parseFromExportSettingsJson(settingsObj, name) else null
                    packages.add(ImportedNotebookPackage(name, items, settings))
                }
                return when {
                    packages.size == 1 -> ImportedJsonResult.SingleNotebook(packages[0].name, packages[0].words, packages[0].settings)
                    packages.size > 1 -> ImportedJsonResult.MultipleNotebooks(packages)
                    else -> ImportedJsonResult.EmptyOrInvalid
                }
            }

            // Case 4: Key-value map: { "Notebook1": { "settings": ..., "words": [...] }, ... }
            val packages = mutableListOf<ImportedNotebookPackage>()
            val keys = rootObj.keys()
            val metadataKeys = setOf("version", "exported_at", "type", "description")

            while (keys.hasNext()) {
                val key = keys.next()
                if (key in metadataKeys) continue
                val cleanKey = ExportSharingManager.stripNotebookPrefix(key).ifBlank { key }

                val value = rootObj.opt(key)
                if (value is JSONObject) {
                    val wordsArray = value.optJSONArray("words") ?: value.optJSONArray("history")
                    val items = if (wordsArray != null) parseJsonHistory(wordsArray.toString()) else emptyList()
                    val settingsObj = value.optJSONObject("settings")
                    val settings = if (settingsObj != null) NotebookSettings.parseFromExportSettingsJson(settingsObj, cleanKey) else null
                    if (items.isNotEmpty() || settings != null) {
                        packages.add(ImportedNotebookPackage(cleanKey, items, settings))
                    }
                } else if (value is JSONArray) {
                    val items = parseJsonHistory(value.toString())
                    if (items.isNotEmpty()) {
                        packages.add(ImportedNotebookPackage(cleanKey, items, null))
                    }
                }
            }

            return when {
                packages.size > 1 -> ImportedJsonResult.MultipleNotebooks(packages)
                packages.size == 1 -> ImportedJsonResult.SingleNotebook(packages[0].name, packages[0].words, packages[0].settings)
                else -> ImportedJsonResult.EmptyOrInvalid
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing imported JSON", e)
        }

        return ImportedJsonResult.EmptyOrInvalid
    }

    /**
     * Imports a single notebook with full settings restoration.
     * Automatically strips any "Notebook_" prefix and duplicate suffixes (`_1`, `_2`, `(2)`).
     * If a Primary notebook with the same clean name already exists, cumulatively merges the
     * imported words into it without creating duplicate `A1.1_1` or `A1.1 (2)` notebooks in Primary.
     */
    fun importSingleNotebookAsNew(
        name: String,
        words: List<WordHistoryEntity>,
        importedSettings: NotebookSettings? = null
    ): Notebook {
        ensureInitialized()
        val root = getRootDirectory()
        val cleanTargetName = stripDuplicateNotebookSuffix(name).ifBlank { "Notizbuch" }
        val existingMatch = listAllNotebooks().find {
            stripDuplicateNotebookSuffix(it.name).equals(cleanTargetName, ignoreCase = true) ||
                stripDuplicateNotebookSuffix(it.id).equals(sanitizeFileName(cleanTargetName), ignoreCase = true)
        }

        if (existingMatch != null) {
            val targetId = existingMatch.id
            val existingWords = loadNotebookHistoryLocalOnly(targetId)
            val mergedWords = mergeItemsCumulatively(existingWords, words)
            val currentSettings = loadNotebookSettings(targetId)
            val finalSettings = if (importedSettings != null) {
                currentSettings.copy(
                    notebookName = cleanTargetName,
                    themeColorId = importedSettings.themeColorId.ifBlank { currentSettings.themeColorId },
                    themeMode = importedSettings.themeMode.ifBlank { currentSettings.themeMode },
                    appLanguage = importedSettings.appLanguage.ifBlank { currentSettings.appLanguage },
                    translationLanguage = importedSettings.translationLanguage.ifBlank { currentSettings.translationLanguage }
                )
            } else {
                currentSettings.copy(notebookName = cleanTargetName)
            }
            val targetDir = File(root, targetId).apply { mkdirs() }
            File(targetDir, SETTINGS_FILE_NAME).writeText(finalSettings.toJson())
            saveNotebookHistoryLocalOnly(targetId, mergedWords)
            syncInternalBackupCumulatively(targetId, mergedWords)
            setActiveNotebookId(targetId)
            backupManager?.scheduleBackup(targetId)
            return listAllNotebooks().find { it.id == targetId } ?: getActiveNotebook()
        }

        val cleanFolderBase = sanitizeFileName(cleanTargetName).ifBlank { "Notizbuch" }
        var folderName = cleanFolderBase
        var counter = 1
        while (File(root, folderName).exists()) {
            folderName = "${cleanFolderBase}_$counter"
            counter++
        }

        val targetDir = File(root, folderName).apply { mkdirs() }
        val finalSettings = (importedSettings?.copy(
            notebookName = cleanTargetName,
            customApiKey = "",
            openRouterApiKey = "",
            storageFolderUri = ""
        ) ?: NotebookSettings(notebookName = cleanTargetName))

        File(targetDir, SETTINGS_FILE_NAME).writeText(finalSettings.toJson())
        File(targetDir, HISTORY_FILE_NAME).writeText(serializeHistoryToJson(words).toString(2))
        syncNotebookFileToNotebooksSubfolder(folderName)
        syncInternalBackupCumulatively(folderName, words)

        val notebook = Notebook(
            id = folderName,
            name = finalSettings.notebookName,
            lastModified = System.currentTimeMillis(),
            wordCount = words.size,
            settings = finalSettings
        )
        setActiveNotebookId(folderName)
        backupManager?.scheduleBackup(folderName)
        return notebook
    }

    /**
     * Merges imported words into the currently active notebook without losing existing items.
     */
    fun importSingleNotebookMergeActive(newWords: List<WordHistoryEntity>): List<WordHistoryEntity> {
        val currentItems = loadActiveHistory()
        val currentKeyMap = currentItems.associateBy { "${it.word.trim().lowercase()}_${it.type.trim().lowercase()}" }.toMutableMap()

        // Merge: update existing or append new
        for (item in newWords) {
            val key = "${item.word.trim().lowercase()}_${item.type.trim().lowercase()}"
            val existing = currentKeyMap[key]
            val manual = if (item.manualTranslation.isNotBlank()) {
                item.manualTranslation
            } else {
                existing?.manualTranslation.orEmpty()
            }
            currentKeyMap[key] = item.copy(manualTranslation = manual)
        }

        val merged = currentKeyMap.values.sortedByDescending { it.timestamp }
        saveActiveHistory(merged)
        return merged
    }

    /**
     * Imports multiple notebooks as separate individual notebooks.
     */
    fun importMultipleNotebooksSeparate(packages: List<ImportedNotebookPackage>): List<Notebook> {
        val importedList = mutableListOf<Notebook>()
        for (pkg in packages) {
            val nb = importSingleNotebookAsNew(pkg.name, pkg.words, pkg.settings)
            importedList.add(nb)
        }
        if (importedList.isNotEmpty()) {
            setActiveNotebookId(importedList.first().id)
        }
        return importedList
    }

    /**
     * Imports multiple notebooks combined as a single notebook.
     */
    fun importMultipleNotebooksAsSingle(
        name: String,
        allWords: List<WordHistoryEntity>,
        firstSettings: NotebookSettings? = null
    ): Notebook {
        return importSingleNotebookAsNew(name, allWords, firstSettings)
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(fileNameSanitizeRegex, "_").trim()
    }

    fun serializeHistoryToJson(items: List<WordHistoryEntity>): JSONArray {
        val jsonArray = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("word", item.word)
                put("type", item.type)
                put("data", item.data)
                put("timestamp", item.timestamp)
                put("gender", item.gender)
                put("genderArticle", item.genderArticle)
                put("meaningEnglish", item.meaningEnglish)
                put("rawJsonResult", item.rawJsonResult)
                put("translationLanguage", item.translationLanguage)
                put("manualTranslation", item.manualTranslation)
                put("manual_translation", item.manualTranslation)
                put("subtitles", item.manualTranslation)
                put("custom_translation", item.manualTranslation)
                if (item.type.equals("Verb", ignoreCase = true)) {
                    val details = extractVerbQuickDetails(item)
                    put("verb", details.verb.ifBlank { item.word })
                    put("auxiliary", details.auxiliary.removeSurrounding("(", ")"))
                    put("praeteritum", details.praeteritum)
                    put("partizip1", details.partizip1)
                    put("partizip2", details.partizip2)
                    put("verb_type", details.verbType)
                    put("separable", details.separable)
                    put("case_object", details.caseObject)
                    put("english", details.englishMeaning)
                } else {
                    val details = extractNounQuickDetails(item)
                    put("singular", details.singular)
                    put("plural", details.plural)
                    put("pluralNoun", details.plural)
                    put("genitive", details.genitiveSingular)
                    put("genitiveSingular", details.genitiveSingular)
                    put("dativePlural", details.dativePlural)
                    put("english", details.englishMeaning)
                }
            }
            jsonArray.put(obj)
        }
        return jsonArray
    }

    fun parseJsonHistory(jsonString: String): List<WordHistoryEntity> {
        val result = mutableListOf<WordHistoryEntity>()
        val trimmed = jsonString.trim()
        if (trimmed.isEmpty() || trimmed == "[]") return result

        try {
            val jsonArray = JSONArray(trimmed)
            val usedIds = HashSet<Long>(jsonArray.length())
            var nextFallbackId = 1L
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.optJSONObject(i) ?: continue
                val rawWord = obj.optSafeString("word").ifBlank {
                    obj.optSafeString("verb").ifBlank {
                        obj.optSafeString("infinitiv")
                    }
                }
                val explicitData = obj.optSafeString("data")
                val explicitRaw = obj.optSafeString("rawJsonResult")
                val baseJson = explicitData.ifBlank { explicitRaw }.ifBlank {
                    val clone = JSONObject(obj.toString())
                    clone.remove("data")
                    clone.remove("rawJsonResult")
                    clone.toString()
                }
                val data = explicitData.ifBlank { baseJson }
                val rawJsonResult = explicitRaw.ifBlank { data }
                val type = obj.optSafeString("type", "Nomen")
                val cleanWord = sanitizeHistoryWord(rawWord, data.ifBlank { rawJsonResult }, type)
                if (cleanWord.isBlank() || cleanWord.equals("null", ignoreCase = true)) {
                    continue // Skip and remove corrupted null records
                }
                val meaningEnglish = obj.optSafeString("meaningEnglish").ifBlank {
                    obj.optSafeString("english")
                }
                val gender = obj.optSafeString("gender").ifBlank {
                    obj.optSafeString("auxiliary")
                }
                val genderArticle = obj.optSafeString("genderArticle").ifBlank {
                    obj.optSafeString("partizip2")
                }
                val translationLanguage = obj.optSafeString("translationLanguage", "en")
                val manualTranslation = obj.optSafeString("manualTranslation")
                    .ifBlank { obj.optSafeString("manual_translation") }
                    .ifBlank { obj.optSafeString("subtitles") }
                    .ifBlank { obj.optSafeString("custom_translation") }

                var parsedId = obj.optLong("id", 0L)
                if (parsedId <= 0L || !usedIds.add(parsedId)) {
                    while (!usedIds.add(nextFallbackId)) {
                        nextFallbackId++
                    }
                    parsedId = nextFallbackId
                }

                result.add(
                    WordHistoryEntity(
                        id = parsedId,
                        word = cleanWord,
                        type = type,
                        data = data,
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        gender = gender,
                        genderArticle = genderArticle,
                        meaningEnglish = meaningEnglish,
                        rawJsonResult = rawJsonResult,
                        translationLanguage = translationLanguage,
                        manualTranslation = manualTranslation
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing history json", e)
        }
        return result
    }
}

sealed class ImportedJsonResult {
    data class SingleNotebook(
        val detectedName: String,
        val words: List<WordHistoryEntity>,
        val settings: NotebookSettings? = null
    ) : ImportedJsonResult()

    data class MultipleNotebooks(
        val packages: List<ImportedNotebookPackage>
    ) : ImportedJsonResult() {
        val notebooks: Map<String, List<WordHistoryEntity>>
            get() = packages.associate { it.name to it.words }
    }

    object EmptyOrInvalid : ImportedJsonResult()
}
