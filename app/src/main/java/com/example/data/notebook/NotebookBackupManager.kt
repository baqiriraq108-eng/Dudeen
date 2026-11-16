package com.example.data.notebook

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.example.data.local.WordHistoryEntity
import com.example.util.ExportSharingManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

data class BackupFolder(
    val uriString: String,
    val displayName: String,
    val isPrimary: Boolean = false,
    val isReachable: Boolean = true,
    val lastBackupTimestamp: Long = 0L,
    val lastErrorMessage: String? = null
) {
    val uri: Uri get() = Uri.parse(uriString)
}

data class BackupResult(
    val totalWritten: Int,
    val successfulFolders: Int,
    val totalFolders: Int
)

/**
 * Manages multiple backup folders for all notebooks as JSON files.
 * Provides automatic mirror backups on notebook modifications and manual "Jetzt sichern".
 */
class NotebookBackupManager(
    private val context: Context,
    private val notebookRepository: NotebookRepository
) {
    companion object {
        private const val TAG = "NotebookBackupManager"
        private const val PREFS_NAME = "notebook_backup_folders_prefs"
        private const val KEY_FOLDERS_JSON = "registered_backup_folders_json"

        fun formatBackupTimestamp(timestamp: Long, appLanguage: String = "de"): String {
            if (timestamp <= 0L) {
                return when (appLanguage.lowercase().trim()) {
                    "en" -> "Not backed up yet"
                    "ar" -> "لم يتم النسخ الاحتياطي بعد"
                    else -> "Noch nicht gesichert"
                }
            }
            val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMAN)
            val dateStr = sdf.format(Date(timestamp))
            return when (appLanguage.lowercase().trim()) {
                "en" -> "Last backed up: $dateStr"
                "ar" -> "آخر نسخ احتياطي: $dateStr"
                else -> "Zuletzt gesichert: $dateStr"
            }
        }

        fun shortenPathMiddle(path: String, maxLength: Int = 30): String {
            val clean = path.trim()
            if (clean.length <= maxLength) return clean
            val partLen = (maxLength - 3) / 2
            return clean.take(partLen) + "..." + clean.takeLast(partLen)
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _backupFolders = MutableStateFlow<List<BackupFolder>>(emptyList())
    val backupFolders: StateFlow<List<BackupFolder>> = _backupFolders.asStateFlow()

    private val _isBackingUp = MutableStateFlow(false)
    val isBackingUp: StateFlow<Boolean> = _isBackingUp.asStateFlow()

    private val notebookDebounceJobs = ConcurrentHashMap<String, Job>()
    private var allBackupDebounceJob: Job? = null

    init {
        loadFoldersFromPrefs()
    }

    private fun loadFoldersFromPrefs() {
        val rawJson = prefs.getString(KEY_FOLDERS_JSON, null)
        if (rawJson.isNullOrBlank()) {
            _backupFolders.value = emptyList()
            return
        }
        try {
            val jsonArray = JSONArray(rawJson)
            val list = mutableListOf<BackupFolder>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val uriStr = obj.optString("uriString")
                if (uriStr.isNotBlank()) {
                    list.add(
                        BackupFolder(
                            uriString = uriStr,
                            displayName = obj.optString("displayName", "Backup-Ordner"),
                            isPrimary = obj.optBoolean("isPrimary", false),
                            isReachable = obj.optBoolean("isReachable", true),
                            lastBackupTimestamp = obj.optLong("lastBackupTimestamp", 0L),
                            lastErrorMessage = if (obj.has("lastErrorMessage")) obj.optString("lastErrorMessage") else null
                        )
                    )
                }
            }
            // Ensure at most one primary, or if none is designated and list is not empty, designate the first
            val hasPrimary = list.any { it.isPrimary }
            val normalizedList = if (!hasPrimary && list.isNotEmpty()) {
                list.mapIndexed { idx, item -> if (idx == 0) item.copy(isPrimary = true) else item }
            } else {
                list
            }
            _backupFolders.value = normalizedList
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load backup folders from prefs", e)
            _backupFolders.value = emptyList()
        }
    }

    private fun saveFoldersToPrefs(folders: List<BackupFolder>) {
        try {
            val jsonArray = JSONArray()
            folders.forEach { f ->
                val obj = JSONObject().apply {
                    put("uriString", f.uriString)
                    put("displayName", f.displayName)
                    put("isPrimary", f.isPrimary)
                    put("isReachable", f.isReachable)
                    put("lastBackupTimestamp", f.lastBackupTimestamp)
                    if (f.lastErrorMessage != null) put("lastErrorMessage", f.lastErrorMessage)
                }
                jsonArray.put(obj)
            }
            prefs.edit().putString(KEY_FOLDERS_JSON, jsonArray.toString()).apply()
            _backupFolders.value = folders
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save backup folders to prefs", e)
        }
    }

    /**
     * Resolves a clean, user-friendly display name from a Document tree URI.
     */
    fun resolveFolderDisplayName(uri: Uri): String {
        val docFile = try {
            DocumentFile.fromTreeUri(context, uri)
        } catch (e: Exception) {
            null
        }

        val treeDocId = try {
            DocumentsContract.getTreeDocumentId(uri)
        } catch (e: Exception) {
            null
        }

        return when {
            treeDocId != null && treeDocId.contains(":") -> {
                val parts = treeDocId.split(":", limit = 2)
                val volume = if (parts[0].equals("primary", ignoreCase = true)) "" else "/${parts[0]}"
                val subPath = parts[1].trimStart('/')
                val result = if (volume.isEmpty()) "/$subPath" else "$volume/$subPath"
                result.ifBlank { docFile?.name?.let { "/$it" } ?: "/Backup" }
            }
            !docFile?.name.isNullOrBlank() -> "/${docFile!!.name}"
            else -> uri.lastPathSegment?.let { "/$it" } ?: "/Backup"
        }
    }

    /**
     * Adds a new backup folder, takes persistable URI permissions, and immediately triggers an initial backup.
     */
    suspend fun addFolder(uri: Uri): BackupFolder = withContext(Dispatchers.IO) {
        val uriStr = uri.toString()

        // Take persistent read/write permissions
        try {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to take persistable URI permission for $uri", e)
        }

        val resolvedName = resolveFolderDisplayName(uri)
        val currentList = _backupFolders.value.toMutableList()

        // Check if already registered
        val existingIndex = currentList.indexOfFirst { it.uriString == uriStr }
        val isFirst = currentList.isEmpty()
        val newFolder = if (existingIndex >= 0) {
            val existing = currentList[existingIndex]
            existing.copy(displayName = resolvedName, isReachable = true)
        } else {
            BackupFolder(
                uriString = uriStr,
                displayName = resolvedName,
                isPrimary = isFirst,
                isReachable = true,
                lastBackupTimestamp = 0L
            )
        }

        if (existingIndex >= 0) {
            currentList[existingIndex] = newFolder
        } else {
            currentList.add(newFolder)
        }

        // Ensure at least one primary exists
        if (!currentList.any { it.isPrimary } && currentList.isNotEmpty()) {
            currentList[0] = currentList[0].copy(isPrimary = true)
        }

        saveFoldersToPrefs(currentList)

        // Perform initial backup of all notebooks to this new folder
        backupFolderInternal(newFolder)
        newFolder
    }

    /**
     * Designates a specific folder as the Primary storage location (Fresh Baseline Rule).
     * All other locations become backup locations.
     *
     * Rules enforced at the exact moment of switch:
     * 1. No Retroactive Deletions: Past bulk deletions ("Clear All") or state differences from the
     *    previous Primary Storage NEVER overwrite or delete items from the newly promoted Primary folder.
     * 2. Clean Baseline: The promoted folder's current contents immediately become the clean baseline
     *    for both the new Primary Storage (including local Primary storage) and the corresponding
     *    Backup Storages (including the previous Primary folder, which is now a Backup folder).
     */
    fun setPrimaryFolder(uriString: String) {
        val currentList = _backupFolders.value
        val updated = currentList.map { folder ->
            folder.copy(isPrimary = (folder.uriString == uriString))
        }
        saveFoldersToPrefs(updated)

        val newPrimary = updated.find { it.isPrimary } ?: return
        if (!isFolderReachable(newPrimary)) return

        val allNotebooks = notebookRepository.listAllNotebooks()
        val updatedFolders = updated.toMutableList()
        var foldersChanged = false

        for (nb in allNotebooks) {
            try {
                val fileName = ExportSharingManager.buildNotebookFileName(nb.name, defaultName = "Notizbuch")
                val promotedWords = readNotebookWordsFromFolder(newPrimary, fileName)

                val baselineWords = if (promotedWords != null) {
                    // The promoted backup has an existing record: use its exact contents as the clean baseline!
                    notebookRepository.saveNotebookHistoryLocalOnly(nb.id, promotedWords)
                    // Ensure standardized Notebooks/Notebook_<Name>.json is up to date in newPrimary
                    val primaryJson = notebookRepository.buildNotebookJsonString(nb.id, promotedWords)
                    writeNotebookJsonToFolder(newPrimary, fileName, primaryJson)
                    promotedWords
                } else {
                    // File does not exist yet in the newly designated primary folder: initialize from local primary
                    val localWords = notebookRepository.loadNotebookHistoryLocalOnly(nb.id)
                    val primaryJson = notebookRepository.buildNotebookJsonString(nb.id, localWords)
                    writeNotebookJsonToFolder(newPrimary, fileName, primaryJson)
                    localWords
                }

                // Sync baseline cumulatively into Internal System Backup
                notebookRepository.syncInternalBackupCumulatively(nb.id, baselineWords)

                // Sync baseline cumulatively into all other Backup folders (including the old Primary folder)
                for (i in updatedFolders.indices) {
                    val folder = updatedFolders[i]
                    if (folder.isPrimary) {
                        updatedFolders[i] = folder.copy(
                            isReachable = true,
                            lastBackupTimestamp = System.currentTimeMillis(),
                            lastErrorMessage = null
                        )
                        foldersChanged = true
                        continue
                    }
                    if (!isFolderReachable(folder)) continue
                    val existingInBackup = readNotebookWordsFromFolder(folder, fileName).orEmpty()
                    val mergedForBackup = notebookRepository.mergeItemsCumulatively(existingInBackup, baselineWords)
                    val backupJson = notebookRepository.buildNotebookJsonString(nb.id, mergedForBackup)
                    if (writeNotebookJsonToFolder(folder, fileName, backupJson)) {
                        updatedFolders[i] = folder.copy(
                            isReachable = true,
                            lastBackupTimestamp = System.currentTimeMillis(),
                            lastErrorMessage = null
                        )
                        foldersChanged = true
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error promoting baseline for notebook ${nb.name}: ${e.message}")
            }
        }

        if (foldersChanged) {
            saveFoldersToPrefs(updatedFolders)
        }
    }

    /**
     * Unregisters a backup folder and releases persistable URI permissions.
     */
    fun removeFolder(uriString: String) {
        try {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.releasePersistableUriPermission(Uri.parse(uriString), flags)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to release persistable URI permission for $uriString", e)
        }

        var updated = _backupFolders.value.filterNot { it.uriString == uriString }
        if (updated.isNotEmpty() && !updated.any { it.isPrimary }) {
            val newPrimaryUri = updated[0].uriString
            saveFoldersToPrefs(updated)
            setPrimaryFolder(newPrimaryUri)
            return
        }
        saveFoldersToPrefs(updated)
    }

    /**
     * Schedules a debounced backup for a specific notebook.
     * Called whenever a notebook is modified (word added/deleted/edited, settings changed, etc.).
     */
    fun scheduleBackup(notebookId: String) {
        if (_backupFolders.value.isEmpty()) return
        notebookDebounceJobs[notebookId]?.cancel()
        notebookDebounceJobs[notebookId] = scope.launch {
            delay(400) // Debounce rapid keystrokes/imports
            backupSingleNotebookInternal(notebookId)
        }
    }

    /**
     * Immediately writes/merges a single notebook to all registered Primary and Backup folders
     * without waiting for the 400ms debounce delay.
     */
    fun backupSingleNotebookNow(notebookId: String) {
        if (_backupFolders.value.isEmpty()) return
        notebookDebounceJobs[notebookId]?.cancel()
        try {
            backupSingleNotebookInternal(notebookId)
        } catch (e: Exception) {
            Log.w(TAG, "Error in immediate single-notebook backup for $notebookId: ${e.message}")
        }
    }

    /**
     * Schedules a debounced backup for all notebooks.
     */
    fun scheduleAllBackup() {
        if (_backupFolders.value.isEmpty()) return
        allBackupDebounceJob?.cancel()
        allBackupDebounceJob = scope.launch {
            delay(500)
            backupAllNotebooksInternal()
        }
    }

    /**
     * Handles notebook renaming: preserves cumulative backup entries from the old file in each backup folder,
     * writes the new file, and removes the old JSON file.
     */
    fun onNotebookRenamed(oldName: String, newNotebookId: String) {
        scope.launch {
            val cleanOld = ExportSharingManager.stripNotebookPrefix(oldName)
            val safeOld = ExportSharingManager.sanitizeFileName(cleanOld, defaultName = "Notizbuch")
            val oldLegacyFileName = "$safeOld.json"
            val oldPrefixedFileName = ExportSharingManager.buildNotebookFileName(cleanOld, defaultName = "Notizbuch")
            val nb = notebookRepository.listAllNotebooks().find { it.id == newNotebookId }
            val newFileName = if (nb != null) {
                ExportSharingManager.buildNotebookFileName(nb.name, defaultName = "Notizbuch")
            } else null
            val primaryWords = notebookRepository.loadNotebookHistory(newNotebookId)
            val folders = _backupFolders.value

            for (folder in folders) {
                try {
                    // Read existing backup words from the old file before deleting it so backup-only entries are preserved
                    if (!folder.isPrimary && newFileName != null && isFolderReachable(folder)) {
                        val existingOldWords = readNotebookWordsFromFolder(folder, oldPrefixedFileName).orEmpty()
                        val mergedWords = notebookRepository.mergeItemsCumulatively(existingOldWords, primaryWords)
                        val jsonToWrite = notebookRepository.buildNotebookJsonString(newNotebookId, mergedWords)
                        writeNotebookJsonToFolder(folder, newFileName, jsonToWrite)
                    }

                    if (folder.uri.scheme == "file") {
                        val dir = java.io.File(folder.uri.path ?: continue)
                        if (dir.exists() && dir.canWrite()) {
                            val nbSub = java.io.File(dir, ExportSharingManager.SUBFOLDER_NOTEBOOKS)
                            if (newFileName == null || !oldPrefixedFileName.equals(newFileName, ignoreCase = true)) {
                                java.io.File(nbSub, oldPrefixedFileName).delete()
                                java.io.File(dir, oldPrefixedFileName).delete()
                            }
                            if (newFileName == null || !oldLegacyFileName.equals(newFileName, ignoreCase = true)) {
                                java.io.File(nbSub, oldLegacyFileName).delete()
                                java.io.File(dir, oldLegacyFileName).delete()
                            }
                        }
                    } else {
                        val docDir = DocumentFile.fromTreeUri(context, folder.uri)
                        if (docDir != null && docDir.exists() && docDir.canWrite()) {
                            val nbSub = docDir.findFile(ExportSharingManager.SUBFOLDER_NOTEBOOKS)
                            if (newFileName == null || !oldPrefixedFileName.equals(newFileName, ignoreCase = true)) {
                                nbSub?.findFile(oldPrefixedFileName)?.delete()
                                docDir.findFile(oldPrefixedFileName)?.delete()
                            }
                            if (newFileName == null || !oldLegacyFileName.equals(newFileName, ignoreCase = true)) {
                                nbSub?.findFile(oldLegacyFileName)?.delete()
                                docDir.findFile(oldLegacyFileName)?.delete()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error cleaning up renamed notebook in ${folder.displayName}", e)
                }
            }
            backupSingleNotebookInternal(newNotebookId)
        }
    }

    /**
     * Handles notebook deletion: removes the corresponding JSON file from all reachable backup folders.
     */
    fun onNotebookDeleted(deletedNotebookName: String) {
        val cleanDeleted = ExportSharingManager.stripNotebookPrefix(deletedNotebookName)
        val safeName = ExportSharingManager.sanitizeFileName(cleanDeleted, defaultName = "Notizbuch")
        val legacyFileName = "$safeName.json"
        val prefixedFileName = ExportSharingManager.buildNotebookFileName(cleanDeleted, defaultName = "Notizbuch")
        val folders = _backupFolders.value

        for (folder in folders) {
            try {
                if (folder.uri.scheme == "file") {
                    val dir = java.io.File(folder.uri.path ?: continue)
                    if (dir.exists() && dir.canWrite()) {
                        val nbSub = java.io.File(dir, ExportSharingManager.SUBFOLDER_NOTEBOOKS)
                        java.io.File(nbSub, prefixedFileName).delete()
                        java.io.File(nbSub, legacyFileName).delete()
                        java.io.File(dir, prefixedFileName).delete()
                        java.io.File(dir, legacyFileName).delete()
                    }
                } else {
                    val docDir = DocumentFile.fromTreeUri(context, folder.uri)
                    if (docDir != null && docDir.exists() && docDir.canWrite()) {
                        val nbSub = docDir.findFile(ExportSharingManager.SUBFOLDER_NOTEBOOKS)
                        nbSub?.findFile(prefixedFileName)?.delete()
                        nbSub?.findFile(legacyFileName)?.delete()
                        docDir.findFile(prefixedFileName)?.delete()
                        docDir.findFile(legacyFileName)?.delete()
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error deleting file in ${folder.displayName}", e)
            }
        }
    }

    /**
     * Manually triggers an immediate backup to all registered folders ("Jetzt sichern").
     */
    suspend fun backupAllNotebooks(): BackupResult = withContext(Dispatchers.IO) {
        _isBackingUp.value = true
        try {
            backupAllNotebooksInternal()
        } finally {
            _isBackingUp.value = false
        }
    }

    /**
     * Resolves a DocumentFile from a tree URI or a file URI.
     */
    fun getDocumentDir(context: Context, uri: Uri): DocumentFile? {
        return try {
            if (uri.scheme == "file") {
                val path = uri.path ?: return null
                val file = java.io.File(path)
                if (!file.exists()) file.mkdirs()
                DocumentFile.fromFile(file)
            } else {
                DocumentFile.fromTreeUri(context, uri)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Checks if a registered folder is currently accessible and writable.
     */
    fun isFolderReachable(folder: BackupFolder): Boolean {
        return try {
            if (folder.uri.scheme == "file") {
                val path = folder.uri.path ?: return false
                val file = java.io.File(path)
                file.exists() && file.canWrite()
            } else {
                val docDir = DocumentFile.fromTreeUri(context, folder.uri)
                docDir != null && docDir.exists() && docDir.canWrite()
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Parses words from either a unified notebook JSON object or a raw JSON array.
     */
    private fun parseWordsFromNotebookOrHistoryJson(text: String): List<WordHistoryEntity> {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return emptyList()
        if (trimmed.startsWith("[")) {
            return notebookRepository.parseJsonHistory(trimmed)
        }
        val root = JSONObject(trimmed)
        val historyArray = root.optJSONArray("words") ?: root.optJSONArray("history")
        return if (historyArray != null) {
            notebookRepository.parseJsonHistory(historyArray.toString())
        } else {
            emptyList()
        }
    }

    /**
     * Reads existing words for a notebook from a registered backup folder.
     * Seamlessly supports both the new `Notebooks/Notebook_<Name>.json` layout and legacy `<Name>.json` files.
     */
    fun readNotebookWordsFromFolder(folder: BackupFolder, fileName: String): List<WordHistoryEntity>? {
        val cleanName = ExportSharingManager.stripNotebookPrefix(fileName).ifBlank { "Notizbuch" }
        val prefixedName = ExportSharingManager.buildNotebookFileName(cleanName, defaultName = "Notizbuch")
        val legacyName = "${ExportSharingManager.sanitizeFileName(cleanName, defaultName = "Notizbuch")}.json"

        if (folder.uri.scheme == "file") {
            return try {
                val dir = java.io.File(folder.uri.path ?: return null)
                val notebooksSub = java.io.File(dir, ExportSharingManager.SUBFOLDER_NOTEBOOKS)
                val sysBackupsSub = java.io.File(dir, ExportSharingManager.SUBFOLDER_SYSTEM_BACKUPS)
                val candidates = listOf(
                    java.io.File(notebooksSub, prefixedName),
                    java.io.File(notebooksSub, fileName),
                    java.io.File(notebooksSub, legacyName),
                    java.io.File(dir, prefixedName),
                    java.io.File(dir, fileName),
                    java.io.File(dir, legacyName),
                    java.io.File(sysBackupsSub, prefixedName),
                    java.io.File(sysBackupsSub, legacyName)
                )
                val targetFile = candidates.firstOrNull { it.exists() && it.isFile && it.canRead() } ?: return null
                val text = targetFile.readText(Charsets.UTF_8)
                parseWordsFromNotebookOrHistoryJson(text)
            } catch (e: Exception) {
                Log.w(TAG, "Failed reading words from file ${folder.displayName}", e)
                null
            }
        }

        val docDir = getDocumentDir(context, folder.uri) ?: return null
        if (!docDir.exists() || !docDir.canRead()) return null
        val notebooksSub = docDir.findFile(ExportSharingManager.SUBFOLDER_NOTEBOOKS)
        val sysBackupsSub = docDir.findFile(ExportSharingManager.SUBFOLDER_SYSTEM_BACKUPS)
        val targetDoc = notebooksSub?.findFile(prefixedName)
            ?: notebooksSub?.findFile(fileName)
            ?: notebooksSub?.findFile(legacyName)
            ?: docDir.findFile(prefixedName)
            ?: docDir.findFile(fileName)
            ?: docDir.findFile(legacyName)
            ?: sysBackupsSub?.findFile(prefixedName)
            ?: sysBackupsSub?.findFile(legacyName)
            ?: return null

        return try {
            context.contentResolver.openInputStream(targetDoc.uri)?.use { stream ->
                val text = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                parseWordsFromNotebookOrHistoryJson(text)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed reading words from ${folder.displayName}", e)
            null
        }
    }

    /**
     * Writes formatted notebook JSON content into the `Notebooks/` subfolder of a registered folder
     * using the standardized `Notebook_<Name>.json` naming convention, and cleans up any legacy root copy
     * by moving it into `System_Backups/`.
     */
    fun writeNotebookJsonToFolder(folder: BackupFolder, fileName: String, jsonString: String): Boolean {
        val cleanName = ExportSharingManager.stripNotebookPrefix(fileName).ifBlank { "Notizbuch" }
        val prefixedFileName = ExportSharingManager.buildNotebookFileName(cleanName, defaultName = "Notizbuch")
        val legacyFileName = "${ExportSharingManager.sanitizeFileName(cleanName, defaultName = "Notizbuch")}.json"

        if (folder.uri.scheme == "file") {
            return try {
                val dir = java.io.File(folder.uri.path ?: return false)
                if (!dir.exists()) dir.mkdirs()
                if (!dir.canWrite()) return false

                val notebooksSub = java.io.File(dir, ExportSharingManager.SUBFOLDER_NOTEBOOKS).apply { mkdirs() }
                val exportsSub = java.io.File(dir, ExportSharingManager.SUBFOLDER_EXPORTS).apply { mkdirs() }
                val sysBackupsSub = java.io.File(dir, ExportSharingManager.SUBFOLDER_SYSTEM_BACKUPS).apply { mkdirs() }
                if (!exportsSub.exists()) exportsSub.mkdirs()

                val targetFile = java.io.File(notebooksSub, prefixedFileName)
                val tempFile = java.io.File(notebooksSub, ".tmp_${System.nanoTime()}.json")
                tempFile.writeText(jsonString, Charsets.UTF_8)
                tempFile.copyTo(targetFile, overwrite = true)
                tempFile.delete()

                // Migrate any legacy root copy into System_Backups/ and remove from root
                val legacyRootFile = java.io.File(dir, legacyFileName)
                if (legacyRootFile.exists() && legacyRootFile.isFile && targetFile.exists() && targetFile.length() > 0L) {
                    try {
                        legacyRootFile.copyTo(java.io.File(sysBackupsSub, legacyFileName), overwrite = true)
                        legacyRootFile.delete()
                    } catch (_: Exception) {}
                }
                val unprefixedInNotebooks = java.io.File(notebooksSub, legacyFileName)
                if (!legacyFileName.equals(prefixedFileName, ignoreCase = true) &&
                    unprefixedInNotebooks.exists() && targetFile.exists() && targetFile.length() > 0L
                ) {
                    try {
                        unprefixedInNotebooks.copyTo(java.io.File(sysBackupsSub, legacyFileName), overwrite = true)
                        unprefixedInNotebooks.delete()
                    } catch (_: Exception) {}
                }
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed writing file to ${folder.displayName}", e)
                false
            }
        }

        val docDir = getDocumentDir(context, folder.uri) ?: return false
        if (!docDir.exists() || !docDir.canWrite()) return false

        try {
            if (docDir.findFile(ExportSharingManager.SUBFOLDER_EXPORTS) == null) {
                docDir.createDirectory(ExportSharingManager.SUBFOLDER_EXPORTS)
            }
            if (docDir.findFile(ExportSharingManager.SUBFOLDER_SYSTEM_BACKUPS) == null) {
                docDir.createDirectory(ExportSharingManager.SUBFOLDER_SYSTEM_BACKUPS)
            }
        } catch (_: Exception) {}

        val notebooksSub = if (docDir.name?.equals(ExportSharingManager.SUBFOLDER_NOTEBOOKS, ignoreCase = true) == true) {
            docDir
        } else {
            docDir.findFile(ExportSharingManager.SUBFOLDER_NOTEBOOKS)
                ?: docDir.createDirectory(ExportSharingManager.SUBFOLDER_NOTEBOOKS)
                ?: docDir
        }

        var targetDoc = notebooksSub.findFile(prefixedFileName)
        if (targetDoc != null && !targetDoc.canWrite()) {
            targetDoc.delete()
            targetDoc = null
        }
        if (targetDoc == null) {
            targetDoc = notebooksSub.createFile("application/json", prefixedFileName)
        }
        if (targetDoc == null) return false

        val written = try {
            context.contentResolver.openOutputStream(targetDoc.uri, "wt")?.use { os ->
                os.write(jsonString.toByteArray(Charsets.UTF_8))
                true
            } ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Failed writing notebook JSON to ${folder.displayName}", e)
            false
        }

        // Clean up legacy root file in SAF folder by archiving to System_Backups/
        if (written && notebooksSub != docDir) {
            try {
                val legacyRootDoc = docDir.findFile(legacyFileName)
                if (legacyRootDoc != null && legacyRootDoc.exists()) {
                    val sysBackupDir = docDir.findFile(ExportSharingManager.SUBFOLDER_SYSTEM_BACKUPS)
                        ?: docDir.createDirectory(ExportSharingManager.SUBFOLDER_SYSTEM_BACKUPS)
                    if (sysBackupDir != null) {
                        val content = context.contentResolver.openInputStream(legacyRootDoc.uri)?.bufferedReader()?.use { it.readText() }
                        if (!content.isNullOrBlank()) {
                            val archiveDoc = sysBackupDir.findFile(legacyFileName)
                                ?: sysBackupDir.createFile("application/json", legacyFileName)
                            if (archiveDoc != null) {
                                context.contentResolver.openOutputStream(archiveDoc.uri, "wt")?.bufferedWriter()?.use { w ->
                                    w.write(content)
                                    w.flush()
                                }
                                if (archiveDoc.length() > 0L) {
                                    legacyRootDoc.delete()
                                }
                            }
                        } else {
                            legacyRootDoc.delete()
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        return written
    }

    /**
     * Loads the history for a notebook from the designated Primary storage location.
     * Returns null if no reachable primary storage is designated or the file is missing.
     */
    fun loadHistoryFromPrimary(notebookId: String): List<WordHistoryEntity>? {
        val folders = _backupFolders.value
        val primaryFolder = folders.find { it.isPrimary } ?: return null
        if (!isFolderReachable(primaryFolder)) return null

        val nb = notebookRepository.listAllNotebooks().find { it.id == notebookId } ?: return null
        val fileName = ExportSharingManager.buildNotebookFileName(nb.name, defaultName = "Notizbuch")

        return readNotebookWordsFromFolder(primaryFolder, fileName)
    }

    /**
     * Adds a new entry (or updates manual translation in-place):
     * - Primary storage receives the updated active list (preserving existing positions/timestamps).
     * - Backup storages cumulatively merge without overwriting older preserved entries and without duplicating.
     * - If a backup location is unreachable, it is skipped and marked as unreachable.
     */
    fun syncEntryAdded(
        notebookId: String,
        item: WordHistoryEntity,
        fullUpdatedItems: List<WordHistoryEntity>? = null
    ) {
        val folders = _backupFolders.value
        if (folders.isEmpty()) return

        val nb = notebookRepository.listAllNotebooks().find { it.id == notebookId } ?: return
        val fileName = ExportSharingManager.buildNotebookFileName(nb.name, defaultName = "Notizbuch")
        val updatedFolders = folders.toMutableList()
        var hasChanges = false

        for (i in updatedFolders.indices) {
            val folder = updatedFolders[i]
            if (!isFolderReachable(folder)) {
                if (folder.isReachable) {
                    updatedFolders[i] = folder.copy(isReachable = false, lastErrorMessage = "Ordner nicht erreichbar")
                    hasChanges = true
                }
                continue
            }

            try {
                val jsonToWrite = if (folder.isPrimary) {
                    if (fullUpdatedItems != null) {
                        notebookRepository.buildNotebookJsonString(notebookId, fullUpdatedItems)
                    } else {
                        val existingWords = readNotebookWordsFromFolder(folder, fileName).orEmpty()
                        val mergedPrimary = notebookRepository.mergeItemsCumulatively(existingWords, listOf(item))
                        notebookRepository.buildNotebookJsonString(notebookId, mergedPrimary)
                    }
                } else {
                    // Backup folder: cumulatively merge without losing older backup items and without duplicating
                    val existingBackupWords = readNotebookWordsFromFolder(folder, fileName).orEmpty()
                    val itemsToMerge = fullUpdatedItems ?: listOf(item)
                    val mergedBackupWords = notebookRepository.mergeItemsCumulatively(existingBackupWords, itemsToMerge)
                    notebookRepository.buildNotebookJsonString(notebookId, mergedBackupWords)
                }

                val success = writeNotebookJsonToFolder(folder, fileName, jsonToWrite)
                if (success) {
                    updatedFolders[i] = folder.copy(
                        isReachable = true,
                        lastBackupTimestamp = System.currentTimeMillis(),
                        lastErrorMessage = null
                    )
                    hasChanges = true
                } else {
                    updatedFolders[i] = folder.copy(isReachable = false, lastErrorMessage = "Schreibfehler")
                    hasChanges = true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing added entry in ${folder.displayName}", e)
                updatedFolders[i] = folder.copy(isReachable = false, lastErrorMessage = e.localizedMessage)
                hasChanges = true
            }
        }

        if (hasChanges) {
            saveFoldersToPrefs(updatedFolders)
        }
    }

    /**
     * Deletes a single entry: removed from Primary storage AND from all Backups.
     * Ensures consistency across all copies.
     */
    fun syncEntryDeleted(
        notebookId: String,
        itemId: Long,
        word: String? = null,
        type: String? = null,
        fullUpdatedItems: List<WordHistoryEntity>? = null
    ) {
        val folders = _backupFolders.value
        if (folders.isEmpty()) return

        val nb = notebookRepository.listAllNotebooks().find { it.id == notebookId } ?: return
        val fileName = ExportSharingManager.buildNotebookFileName(nb.name, defaultName = "Notizbuch")
        val updatedFolders = folders.toMutableList()
        var hasChanges = false

        for (i in updatedFolders.indices) {
            val folder = updatedFolders[i]
            if (!isFolderReachable(folder)) {
                if (folder.isReachable) {
                    updatedFolders[i] = folder.copy(isReachable = false, lastErrorMessage = "Ordner nicht erreichbar")
                    hasChanges = true
                }
                continue
            }

            try {
                val jsonToWrite = if (folder.isPrimary) {
                    if (fullUpdatedItems != null) {
                        notebookRepository.buildNotebookJsonString(notebookId, fullUpdatedItems)
                    } else {
                        notebookRepository.exportNotebookToJsonString(notebookId)
                    }
                } else {
                    val existingWords = readNotebookWordsFromFolder(folder, fileName)?.toMutableList()
                    if (existingWords != null) {
                        existingWords.removeAll {
                            it.id == itemId || (word != null && type != null &&
                                it.word.trim().equals(word.trim(), ignoreCase = true) &&
                                it.type.trim().equals(type.trim(), ignoreCase = true))
                        }
                        notebookRepository.buildNotebookJsonString(notebookId, existingWords)
                    } else {
                        null
                    }
                }

                if (jsonToWrite != null) {
                    val success = writeNotebookJsonToFolder(folder, fileName, jsonToWrite)
                    if (success) {
                        updatedFolders[i] = folder.copy(
                            isReachable = true,
                            lastBackupTimestamp = System.currentTimeMillis(),
                            lastErrorMessage = null
                        )
                        hasChanges = true
                    } else {
                        updatedFolders[i] = folder.copy(isReachable = false, lastErrorMessage = "Schreibfehler")
                        hasChanges = true
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing deleted entry in ${folder.displayName}", e)
                updatedFolders[i] = folder.copy(isReachable = false, lastErrorMessage = e.localizedMessage)
                hasChanges = true
            }
        }

        if (hasChanges) {
            saveFoldersToPrefs(updatedFolders)
        }
    }

    /**
     * Updates ONLY the designated Primary folder (e.g. during partial type-clear or primary-only clear),
     * leaving all non-primary Backup folders completely untouched.
     */
    fun syncPrimaryOnly(notebookId: String, primaryItems: List<WordHistoryEntity>) {
        val folders = _backupFolders.value
        val primaryIndex = folders.indexOfFirst { it.isPrimary }
        if (primaryIndex < 0) return

        val primaryFolder = folders[primaryIndex]
        if (!isFolderReachable(primaryFolder)) {
            val updatedFolders = folders.toMutableList()
            updatedFolders[primaryIndex] = primaryFolder.copy(isReachable = false, lastErrorMessage = "Ordner nicht erreichbar")
            saveFoldersToPrefs(updatedFolders)
            return
        }

        val nb = notebookRepository.listAllNotebooks().find { it.id == notebookId } ?: return
        val fileName = ExportSharingManager.buildNotebookFileName(nb.name, defaultName = "Notizbuch")
        val updatedFolders = folders.toMutableList()

        try {
            val jsonToWrite = notebookRepository.buildNotebookJsonString(notebookId, primaryItems)
            val success = writeNotebookJsonToFolder(primaryFolder, fileName, jsonToWrite)
            if (success) {
                updatedFolders[primaryIndex] = primaryFolder.copy(
                    isReachable = true,
                    lastBackupTimestamp = System.currentTimeMillis(),
                    lastErrorMessage = null
                )
            } else {
                updatedFolders[primaryIndex] = primaryFolder.copy(isReachable = false, lastErrorMessage = "Schreibfehler")
            }
            saveFoldersToPrefs(updatedFolders)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating primary storage in ${primaryFolder.displayName}", e)
            updatedFolders[primaryIndex] = primaryFolder.copy(isReachable = false, lastErrorMessage = e.localizedMessage)
            saveFoldersToPrefs(updatedFolders)
        }
    }

    /**
     * Syncs a promoted baseline to the Primary folder and cumulatively merges it into all Backup folders.
     */
    fun syncPromotedBaselineToAllFolders(notebookId: String, baselineItems: List<WordHistoryEntity>) {
        val folders = _backupFolders.value
        if (folders.isEmpty()) return

        val nb = notebookRepository.listAllNotebooks().find { it.id == notebookId } ?: return
        val fileName = ExportSharingManager.buildNotebookFileName(nb.name, defaultName = "Notizbuch")
        val updatedFolders = folders.toMutableList()
        var hasChanges = false

        for (i in updatedFolders.indices) {
            val folder = updatedFolders[i]
            if (!isFolderReachable(folder)) continue
            try {
                val itemsToWrite = if (folder.isPrimary) {
                    baselineItems
                } else {
                    val existingInBackup = readNotebookWordsFromFolder(folder, fileName).orEmpty()
                    notebookRepository.mergeItemsCumulatively(existingInBackup, baselineItems)
                }
                val json = notebookRepository.buildNotebookJsonString(notebookId, itemsToWrite)
                if (writeNotebookJsonToFolder(folder, fileName, json)) {
                    updatedFolders[i] = folder.copy(
                        isReachable = true,
                        lastBackupTimestamp = System.currentTimeMillis(),
                        lastErrorMessage = null
                    )
                    hasChanges = true
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error syncing promoted baseline to ${folder.displayName}: ${e.message}")
            }
        }

        if (hasChanges) {
            saveFoldersToPrefs(updatedFolders)
        }
    }

    /**
     * Clears the entire log (Clear All):
     * ONLY the Primary storage is completely emptied.
     * Backups remain UNTOUCHED - they are not deleted, not emptied, not overwritten.
     */
    fun syncClearAllPrimary(notebookId: String) {
        syncPrimaryOnly(notebookId, emptyList())
        // NOTE: ALL BACKUP FOLDERS (isPrimary == false) REMAIN COMPLETELY UNTOUCHED!
    }

    private fun backupSingleNotebookInternal(notebookId: String) {
        val folders = _backupFolders.value
        if (folders.isEmpty()) return

        val allNotebooks = notebookRepository.listAllNotebooks()
        val nb = allNotebooks.find { it.id == notebookId } ?: return
        val primaryWords = try {
            notebookRepository.loadNotebookHistory(notebookId)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading notebook $notebookId for backup", e)
            return
        }

        val fileName = ExportSharingManager.buildNotebookFileName(nb.name, defaultName = "Notizbuch")
        val updatedFolders = folders.toMutableList()
        var hasChanges = false

        for (i in updatedFolders.indices) {
            val folder = updatedFolders[i]
            try {
                val docDir = getDocumentDir(context, folder.uri)
                if (docDir == null || !docDir.exists() || !docDir.canWrite()) {
                    if (folder.isReachable) {
                        updatedFolders[i] = folder.copy(isReachable = false, lastErrorMessage = "Ordner nicht erreichbar")
                        hasChanges = true
                    }
                    continue
                }

                // Primary writes primaryWords; Backup cumulatively merges primaryWords into existing backup words
                val wordsForFolder = if (folder.isPrimary) {
                    primaryWords
                } else {
                    val existingBackupWords = readNotebookWordsFromFolder(folder, fileName).orEmpty()
                    notebookRepository.mergeItemsCumulatively(existingBackupWords, primaryWords)
                }
                val jsonString = notebookRepository.buildNotebookJsonString(notebookId, wordsForFolder)

                val success = writeNotebookJsonToFolder(folder, fileName, jsonString)
                if (success) {
                    updatedFolders[i] = folder.copy(
                        isReachable = true,
                        lastBackupTimestamp = System.currentTimeMillis(),
                        lastErrorMessage = null
                    )
                    hasChanges = true
                } else {
                    updatedFolders[i] = folder.copy(isReachable = false, lastErrorMessage = "Datei konnte nicht erstellt werden")
                    hasChanges = true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error writing to backup folder ${folder.displayName}", e)
                updatedFolders[i] = folder.copy(isReachable = false, lastErrorMessage = e.localizedMessage)
                hasChanges = true
            }
        }

        if (hasChanges) {
            saveFoldersToPrefs(updatedFolders)
        }
    }

    private fun backupFolderInternal(folder: BackupFolder) {
        val allNotebooks = notebookRepository.listAllNotebooks()
        val updatedFolders = _backupFolders.value.toMutableList()
        val index = updatedFolders.indexOfFirst { it.uriString == folder.uriString }
        if (index < 0) return

        try {
            val docDir = getDocumentDir(context, folder.uri)
            if (docDir == null || !docDir.exists() || !docDir.canWrite()) {
                updatedFolders[index] = folder.copy(isReachable = false, lastErrorMessage = "Ordner nicht erreichbar")
                saveFoldersToPrefs(updatedFolders)
                return
            }

            var writtenCount = 0
            for (nb in allNotebooks) {
                val fileName = ExportSharingManager.buildNotebookFileName(nb.name, defaultName = "Notizbuch")
                val existingInFolder = readNotebookWordsFromFolder(folder, fileName).orEmpty()
                val localPrimaryWords = notebookRepository.loadNotebookHistoryLocalOnly(nb.id)
                val mergedWords = if (folder.isPrimary) {
                    val merged = notebookRepository.mergeItemsCumulatively(existingInFolder, localPrimaryWords)
                    if (merged != localPrimaryWords) {
                        notebookRepository.saveNotebookHistoryLocalOnly(nb.id, merged)
                    }
                    notebookRepository.syncInternalBackupCumulatively(nb.id, merged)
                    merged
                } else {
                    val currentPrimaryWords = notebookRepository.loadNotebookHistory(nb.id)
                    notebookRepository.mergeItemsCumulatively(existingInFolder, currentPrimaryWords)
                }

                val jsonString = notebookRepository.buildNotebookJsonString(nb.id, mergedWords)
                val success = writeNotebookJsonToFolder(folder, fileName, jsonString)
                if (success) writtenCount++
            }

            updatedFolders[index] = folder.copy(
                isReachable = true,
                lastBackupTimestamp = System.currentTimeMillis(),
                lastErrorMessage = null
            )
            saveFoldersToPrefs(updatedFolders)
        } catch (e: Exception) {
            Log.e(TAG, "Error writing initial backup to ${folder.displayName}", e)
            updatedFolders[index] = folder.copy(isReachable = false, lastErrorMessage = e.localizedMessage)
            saveFoldersToPrefs(updatedFolders)
        }
    }

    private fun backupAllNotebooksInternal(): BackupResult {
        val folders = _backupFolders.value
        val allNotebooks = notebookRepository.listAllNotebooks()
        var totalWritten = 0
        var successfulFolders = 0
        val updatedFolders = folders.toMutableList()
        var hasChanges = false

        for (i in updatedFolders.indices) {
            val folder = updatedFolders[i]
            try {
                val docDir = getDocumentDir(context, folder.uri)
                if (docDir == null || !docDir.exists() || !docDir.canWrite()) {
                    updatedFolders[i] = folder.copy(isReachable = false, lastErrorMessage = "Ordner nicht erreichbar")
                    hasChanges = true
                    continue
                }

                var folderWritten = 0
                for (nb in allNotebooks) {
                    val fileName = ExportSharingManager.buildNotebookFileName(nb.name, defaultName = "Notizbuch")
                    val primaryWords = notebookRepository.loadNotebookHistory(nb.id)
                    val wordsForFolder = if (folder.isPrimary) {
                        primaryWords
                    } else {
                        val existingBackupWords = readNotebookWordsFromFolder(folder, fileName).orEmpty()
                        notebookRepository.mergeItemsCumulatively(existingBackupWords, primaryWords)
                    }
                    val jsonString = notebookRepository.buildNotebookJsonString(nb.id, wordsForFolder)
                    val success = writeNotebookJsonToFolder(folder, fileName, jsonString)
                    if (success) folderWritten++
                }

                totalWritten += folderWritten
                successfulFolders++
                updatedFolders[i] = folder.copy(
                    isReachable = true,
                    lastBackupTimestamp = System.currentTimeMillis(),
                    lastErrorMessage = null
                )
                hasChanges = true
            } catch (e: Exception) {
                Log.e(TAG, "Error backing up to folder ${folder.displayName}", e)
                updatedFolders[i] = folder.copy(isReachable = false, lastErrorMessage = e.localizedMessage)
                hasChanges = true
            }
        }

        if (hasChanges) {
            saveFoldersToPrefs(updatedFolders)
        }

        return BackupResult(
            totalWritten = totalWritten,
            successfulFolders = successfulFolders,
            totalFolders = folders.size
        )
    }

    /**
     * Discovers all notebook snapshots stored across BOTH internal `System_Backups/`
     * and all reachable non-primary registered external Backup folders.
     */
    suspend fun listAllBackupNotebooks(): List<BackupNotebookEntry> = withContext(Dispatchers.IO) {
        val result = mutableListOf<BackupNotebookEntry>()
        try {
            result.addAll(notebookRepository.listSystemBackupNotebooks())
        } catch (e: Exception) {
            Log.w(TAG, "Error listing system backup notebooks: ${e.message}")
        }

        val nonPrimaryFolders = _backupFolders.value.filter { !it.isPrimary }
        val externalBackupFolders = nonPrimaryFolders.ifEmpty { _backupFolders.value }
        for (folder in externalBackupFolders) {
            if (!isFolderReachable(folder)) continue
            try {
                val folderEntries = scanExternalBackupFolder(folder)
                result.addAll(folderEntries)
            } catch (e: Exception) {
                Log.w(TAG, "Error scanning backup folder ${folder.displayName}: ${e.message}")
            }
        }

        result.sortedWith(String.CASE_INSENSITIVE_ORDER.let { cmp ->
            Comparator { a, b -> cmp.compare(a.displayName.trim(), b.displayName.trim()) }
        })
    }

    private fun scanExternalBackupFolder(folder: BackupFolder): List<BackupNotebookEntry> {
        val shortFolderLabel = shortenPathMiddle(folder.displayName, 22)
        val rawEntries = mutableListOf<BackupNotebookEntry>()

        if (folder.uri.scheme == "file") {
            val dir = java.io.File(folder.uri.path ?: return emptyList())
            val nbSub = java.io.File(dir, ExportSharingManager.SUBFOLDER_NOTEBOOKS)
            val dirsToScan = listOfNotNull(
                nbSub.takeIf { it.exists() && it.isDirectory },
                dir.takeIf { it.exists() && it.isDirectory }
            )
            for (scanDir in dirsToScan) {
                val files = scanDir.listFiles { f ->
                    f.isFile &&
                        f.name.endsWith(".json", ignoreCase = true) &&
                        !f.name.startsWith(".") &&
                        !f.name.equals(NotebookRepository.HISTORY_FILE_NAME, ignoreCase = true) &&
                        !f.name.equals(NotebookRepository.SETTINGS_FILE_NAME, ignoreCase = true)
                } ?: continue
                for (f in files) {
                    try {
                        val text = f.readText(Charsets.UTF_8).trim()
                        parseBackupEntryMetadata(
                            text = text,
                            fileName = f.name,
                            lastModified = f.lastModified(),
                            folder = folder,
                            folderLabel = shortFolderLabel
                        )?.let { rawEntries.add(it) }
                    } catch (_: Exception) {}
                }
            }
        } else {
            val docDir = getDocumentDir(context, folder.uri) ?: return emptyList()
            val nbSub = docDir.findFile(ExportSharingManager.SUBFOLDER_NOTEBOOKS)
            val dirsToScan = listOfNotNull(nbSub, docDir)
            for (scanDoc in dirsToScan) {
                val docs = try { scanDoc.listFiles() } catch (_: Exception) { emptyArray() }
                for (doc in docs) {
                    val name = doc.name ?: continue
                    if (!doc.isFile ||
                        !name.endsWith(".json", ignoreCase = true) ||
                        name.startsWith(".") ||
                        name.equals(NotebookRepository.HISTORY_FILE_NAME, ignoreCase = true) ||
                        name.equals(NotebookRepository.SETTINGS_FILE_NAME, ignoreCase = true)
                    ) continue
                    try {
                        val text = context.contentResolver.openInputStream(doc.uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }?.trim()
                            ?: continue
                        parseBackupEntryMetadata(
                            text = text,
                            fileName = name,
                            lastModified = doc.lastModified(),
                            folder = folder,
                            folderLabel = shortFolderLabel
                        )?.let { rawEntries.add(it) }
                    } catch (_: Exception) {}
                }
            }
        }

        // Deduplicate within a single external folder (prefer Notebook_<Name>.json over legacy <Name>.json)
        return rawEntries
            .groupBy { it.displayName.trim().lowercase(Locale.ROOT) }
            .values
            .mapNotNull { group ->
                group.maxByOrNull { entry ->
                    val prefixScore = if (entry.fileName.startsWith(ExportSharingManager.NOTEBOOK_FILE_PREFIX, ignoreCase = true)) 1_000_000_000L else 0L
                    val wordScore = entry.wordCount.toLong() * 1000L
                    prefixScore + wordScore + (entry.lastModified / 1000L)
                }
            }
    }

    private fun parseBackupEntryMetadata(
        text: String,
        fileName: String,
        lastModified: Long,
        folder: BackupFolder,
        folderLabel: String
    ): BackupNotebookEntry? {
        if (text.isEmpty()) return null
        val fallbackName = ExportSharingManager.stripNotebookPrefix(
            fileName.removeSuffix(".json").removeSuffix(".JSON")
        ).trim().ifBlank { NotebookRepository.DEFAULT_NOTEBOOK_ID }

        var displayName = fallbackName
        var wordCount = 0
        var themeColorId = "Dunkelblau"

        if (text.startsWith("[")) {
            wordCount = JSONArray(text).length()
        } else if (text.startsWith("{")) {
            val root = JSONObject(text)
            val arr = root.optJSONArray("words") ?: root.optJSONArray("history") ?: return null
            wordCount = arr.length()
            val rawName = root.optString("notebook_name").ifBlank { root.optString("notebookName") }.trim()
            if (rawName.isNotBlank()) {
                displayName = ExportSharingManager.stripNotebookPrefix(rawName).trim().ifBlank { fallbackName }
            }
            val settingsObj = root.optJSONObject("settings")
            if (settingsObj != null) {
                themeColorId = NotebookSettings.parseFromExportSettingsJson(settingsObj, displayName).themeColorId
            }
        } else {
            return null
        }

        return BackupNotebookEntry(
            uniqueKey = "folder::${folder.uriString}::$fileName",
            notebookId = ExportSharingManager.sanitizeFileName(displayName, defaultName = NotebookRepository.DEFAULT_NOTEBOOK_ID),
            displayName = displayName,
            wordCount = wordCount,
            lastModified = lastModified,
            themeColorId = themeColorId,
            sourceType = BackupNotebookSourceType.EXTERNAL_FOLDER,
            sourceLabel = folderLabel,
            sourceFolderUri = folder.uriString,
            fileName = fileName
        )
    }

    /**
     * Reads full words and optional settings for a selected [BackupNotebookEntry].
     */
    suspend fun readBackupNotebookSnapshot(entry: BackupNotebookEntry): Pair<List<WordHistoryEntity>, NotebookSettings?> =
        withContext(Dispatchers.IO) {
            if (entry.sourceType == BackupNotebookSourceType.SYSTEM_BACKUP) {
                return@withContext notebookRepository.readSystemBackupSnapshot(entry.fileName)
            }
            val raw = readBackupNotebookRawJson(entry) ?: return@withContext emptyList<WordHistoryEntity>() to null
            notebookRepository.parseBackupSnapshotContent(raw, entry.fileName)
        }

    /**
     * Reads raw JSON text for a selected [BackupNotebookEntry] for export/promotion.
     */
    suspend fun readBackupNotebookRawJson(entry: BackupNotebookEntry): String? =
        withContext(Dispatchers.IO) {
            if (entry.sourceType == BackupNotebookSourceType.SYSTEM_BACKUP) {
                return@withContext notebookRepository.readSystemBackupRawJson(entry.fileName)
            }
            val folderUri = entry.sourceFolderUri ?: return@withContext null
            val folder = _backupFolders.value.find { it.uriString == folderUri }
                ?: BackupFolder(uriString = folderUri, displayName = entry.sourceLabel)
            try {
                if (folder.uri.scheme == "file") {
                    val dir = java.io.File(folder.uri.path ?: return@withContext null)
                    val nbSub = java.io.File(dir, ExportSharingManager.SUBFOLDER_NOTEBOOKS)
                    val targetFile = listOf(
                        java.io.File(nbSub, entry.fileName),
                        java.io.File(dir, entry.fileName)
                    ).firstOrNull { it.exists() && it.isFile && it.canRead() }
                    targetFile?.readText(Charsets.UTF_8)
                } else {
                    val docDir = getDocumentDir(context, folder.uri) ?: return@withContext null
                    val nbSub = docDir.findFile(ExportSharingManager.SUBFOLDER_NOTEBOOKS)
                    val targetDoc = nbSub?.findFile(entry.fileName) ?: docDir.findFile(entry.fileName) ?: return@withContext null
                    context.contentResolver.openInputStream(targetDoc.uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed reading backup notebook raw JSON for ${entry.displayName}: ${e.message}")
                null
            }
        }

    /**
     * Deletes a specific [BackupNotebookEntry] snapshot from either internal `System_Backups/`
     * (Reserve tab) or a registered external Backup folder (Backup tab).
     */
    suspend fun deleteBackupNotebookEntry(entry: BackupNotebookEntry): Boolean =
        withContext(Dispatchers.IO) {
            if (entry.sourceType == BackupNotebookSourceType.SYSTEM_BACKUP) {
                return@withContext notebookRepository.deleteSystemBackupNotebook(entry.fileName, entry.displayName)
            }

            val folderUri = entry.sourceFolderUri ?: return@withContext false
            val folder = _backupFolders.value.find { it.uriString == folderUri }
                ?: BackupFolder(uriString = folderUri, displayName = entry.sourceLabel)
            val cleanName = ExportSharingManager.stripNotebookPrefix(entry.displayName).ifBlank { "Notizbuch" }
            val prefixedName = ExportSharingManager.buildNotebookFileName(cleanName, defaultName = "Notizbuch")
            val legacyName = "${ExportSharingManager.sanitizeFileName(cleanName, defaultName = "Notizbuch")}.json"

            try {
                if (folder.uri.scheme == "file") {
                    val dir = java.io.File(folder.uri.path ?: return@withContext false)
                    val nbSub = java.io.File(dir, ExportSharingManager.SUBFOLDER_NOTEBOOKS)
                    var deleted = false
                    listOf(
                        java.io.File(nbSub, entry.fileName),
                        java.io.File(nbSub, prefixedName),
                        java.io.File(nbSub, legacyName),
                        java.io.File(dir, entry.fileName),
                        java.io.File(dir, prefixedName),
                        java.io.File(dir, legacyName)
                    ).forEach { target ->
                        if (target.exists() && target.isFile) {
                            if (target.delete()) deleted = true
                        }
                    }
                    deleted
                } else {
                    val docDir = getDocumentDir(context, folder.uri) ?: return@withContext false
                    val nbSub = docDir.findFile(ExportSharingManager.SUBFOLDER_NOTEBOOKS)
                    var deleted = false
                    listOfNotNull(
                        nbSub?.findFile(entry.fileName),
                        nbSub?.findFile(prefixedName),
                        nbSub?.findFile(legacyName),
                        docDir.findFile(entry.fileName),
                        docDir.findFile(prefixedName),
                        docDir.findFile(legacyName)
                    ).forEach { doc ->
                        if (doc.exists()) {
                            if (doc.delete()) deleted = true
                        }
                    }
                    deleted
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed deleting backup notebook entry ${entry.displayName}: ${e.message}")
                false
            }
        }
}

