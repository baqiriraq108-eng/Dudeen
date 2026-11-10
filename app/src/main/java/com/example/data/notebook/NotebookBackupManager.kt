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

        fun formatBackupTimestamp(timestamp: Long): String {
            if (timestamp <= 0L) return "Noch nicht gesichert"
            val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMAN)
            return "Zuletzt gesichert: ${sdf.format(Date(timestamp))}"
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
     * Designates a specific folder as the Primary storage location.
     * All other locations become backup locations.
     */
    fun setPrimaryFolder(uriString: String) {
        val currentList = _backupFolders.value
        val updated = currentList.map { folder ->
            folder.copy(isPrimary = (folder.uriString == uriString))
        }
        saveFoldersToPrefs(updated)

        val newPrimary = updated.find { it.isPrimary }
        if (newPrimary != null) {
            scope.launch {
                backupFolderInternal(newPrimary)
            }
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
            updated = updated.mapIndexed { idx, f -> if (idx == 0) f.copy(isPrimary = true) else f }
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
     * Handles notebook renaming: removes old JSON file from backup folders and writes new one.
     */
    fun onNotebookRenamed(oldName: String, newNotebookId: String) {
        scope.launch {
            val safeOld = ExportSharingManager.sanitizeFileName(oldName, defaultName = "Notizbuch")
            val oldFileName = "$safeOld.json"
            val folders = _backupFolders.value

            for (folder in folders) {
                try {
                    val docDir = DocumentFile.fromTreeUri(context, folder.uri)
                    if (docDir != null && docDir.exists() && docDir.canWrite()) {
                        val oldFile = docDir.findFile(oldFileName)
                        oldFile?.delete()
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
        scope.launch {
            val safeName = ExportSharingManager.sanitizeFileName(deletedNotebookName, defaultName = "Notizbuch")
            val fileName = "$safeName.json"
            val folders = _backupFolders.value

            for (folder in folders) {
                try {
                    val docDir = DocumentFile.fromTreeUri(context, folder.uri)
                    if (docDir != null && docDir.exists() && docDir.canWrite()) {
                        val file = docDir.findFile(fileName)
                        file?.delete()
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error deleting file in ${folder.displayName}", e)
                }
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
     * Reads existing words for a notebook from a registered backup folder.
     */
    fun readNotebookWordsFromFolder(folder: BackupFolder, fileName: String): List<WordHistoryEntity>? {
        if (folder.uri.scheme == "file") {
            return try {
                val dir = java.io.File(folder.uri.path ?: return null)
                val targetFile = java.io.File(dir, fileName)
                if (!targetFile.exists() || !targetFile.canRead()) return null
                val text = targetFile.readText(Charsets.UTF_8)
                val root = JSONObject(text)
                val historyArray = root.optJSONArray("words") ?: root.optJSONArray("history")
                if (historyArray != null) {
                    notebookRepository.parseJsonHistory(historyArray.toString())
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed reading words from file ${folder.displayName}", e)
                null
            }
        }

        val docDir = getDocumentDir(context, folder.uri) ?: return null
        if (!docDir.exists() || !docDir.canRead()) return null
        val targetDoc = docDir.findFile(fileName) ?: return null
        return try {
            context.contentResolver.openInputStream(targetDoc.uri)?.use { stream ->
                val text = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                val root = JSONObject(text)
                val historyArray = root.optJSONArray("words") ?: root.optJSONArray("history")
                if (historyArray != null) {
                    notebookRepository.parseJsonHistory(historyArray.toString())
                } else {
                    emptyList()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed reading words from ${folder.displayName}", e)
            null
        }
    }

    /**
     * Writes formatted notebook JSON content to a specific registered folder.
     */
    fun writeNotebookJsonToFolder(folder: BackupFolder, fileName: String, jsonString: String): Boolean {
        if (folder.uri.scheme == "file") {
            return try {
                val dir = java.io.File(folder.uri.path ?: return false)
                if (!dir.exists()) dir.mkdirs()
                if (!dir.canWrite()) return false
                val targetFile = java.io.File(dir, fileName)
                targetFile.writeText(jsonString, Charsets.UTF_8)
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed writing file to ${folder.displayName}", e)
                false
            }
        }

        val docDir = getDocumentDir(context, folder.uri) ?: return false
        if (!docDir.exists() || !docDir.canWrite()) return false

        var targetDoc = docDir.findFile(fileName)
        if (targetDoc != null && !targetDoc.canWrite()) {
            targetDoc.delete()
            targetDoc = null
        }
        if (targetDoc == null) {
            targetDoc = docDir.createFile("application/json", fileName)
        }
        if (targetDoc == null) return false

        return try {
            context.contentResolver.openOutputStream(targetDoc.uri, "wt")?.use { os ->
                os.write(jsonString.toByteArray(Charsets.UTF_8))
                true
            } ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Failed writing notebook JSON to ${folder.displayName}", e)
            false
        }
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
        val safeBase = ExportSharingManager.sanitizeFileName(nb.name, defaultName = "Notizbuch")
        val fileName = "$safeBase.json"

        return readNotebookWordsFromFolder(primaryFolder, fileName)
    }

    /**
     * Adds a new entry: writes to Primary storage AND to all registered Backups.
     * If a backup location is unreachable, it is skipped and marked as unreachable.
     */
    fun syncEntryAdded(
        notebookId: String,
        item: WordHistoryEntity,
        fullUpdatedItems: List<WordHistoryEntity>? = null
    ) {
        val folders = _backupFolders.value
        if (folders.isEmpty()) return

        val nb = notebookRepository.listAllNotebooks().find { it.id == notebookId } ?: return
        val safeBase = ExportSharingManager.sanitizeFileName(nb.name, defaultName = "Notizbuch")
        val fileName = "$safeBase.json"
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
                // If primary: write current updated notebook
                // If backup: read existing words, prepend/update this item, and write back!
                val jsonToWrite = if (folder.isPrimary) {
                    if (fullUpdatedItems != null) {
                        notebookRepository.buildNotebookJsonString(notebookId, fullUpdatedItems)
                    } else {
                        val existingWords = readNotebookWordsFromFolder(folder, fileName)?.toMutableList() ?: mutableListOf()
                        existingWords.removeAll {
                            it.word.equals(item.word, ignoreCase = true) && it.type.equals(item.type, ignoreCase = true)
                        }
                        existingWords.add(0, item)
                        notebookRepository.buildNotebookJsonString(notebookId, existingWords)
                    }
                } else {
                    val existingWords = readNotebookWordsFromFolder(folder, fileName)?.toMutableList() ?: mutableListOf()
                    existingWords.removeAll {
                        it.word.equals(item.word, ignoreCase = true) && it.type.equals(item.type, ignoreCase = true)
                    }
                    existingWords.add(0, item)
                    notebookRepository.buildNotebookJsonString(notebookId, existingWords)
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
        val safeBase = ExportSharingManager.sanitizeFileName(nb.name, defaultName = "Notizbuch")
        val fileName = "$safeBase.json"
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
                                it.word.equals(word, ignoreCase = true) &&
                                it.type.equals(type, ignoreCase = true))
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
     * Clears the entire log (Clear All):
     * ONLY the Primary storage is completely emptied.
     * Backups remain UNTOUCHED - they are not deleted, not emptied, not overwritten.
     */
    fun syncClearAllPrimary(notebookId: String) {
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
        val safeBase = ExportSharingManager.sanitizeFileName(nb.name, defaultName = "Notizbuch")
        val fileName = "$safeBase.json"
        val updatedFolders = folders.toMutableList()

        try {
            // Write cleared notebook (empty words/history) to the primary storage location ONLY
            val jsonToWrite = notebookRepository.buildNotebookJsonString(notebookId, emptyList())
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
            Log.e(TAG, "Error clearing primary storage in ${primaryFolder.displayName}", e)
            updatedFolders[primaryIndex] = primaryFolder.copy(isReachable = false, lastErrorMessage = e.localizedMessage)
            saveFoldersToPrefs(updatedFolders)
        }
        // NOTE: ALL BACKUP FOLDERS (isPrimary == false) REMAIN COMPLETELY UNTOUCHED!
    }

    private fun backupSingleNotebookInternal(notebookId: String) {
        val folders = _backupFolders.value
        if (folders.isEmpty()) return

        val allNotebooks = notebookRepository.listAllNotebooks()
        val nb = allNotebooks.find { it.id == notebookId } ?: return
        val jsonString = try {
            notebookRepository.exportNotebookToJsonString(notebookId)
        } catch (e: Exception) {
            Log.e(TAG, "Error serializing notebook $notebookId", e)
            return
        }

        val safeBase = ExportSharingManager.sanitizeFileName(nb.name, defaultName = "Notizbuch")
        val fileName = "$safeBase.json"
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
                val jsonString = notebookRepository.exportNotebookToJsonString(nb.id)
                val safeBase = ExportSharingManager.sanitizeFileName(nb.name, defaultName = "Notizbuch")
                val fileName = "$safeBase.json"
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
                    val jsonString = notebookRepository.exportNotebookToJsonString(nb.id)
                    val safeBase = ExportSharingManager.sanitizeFileName(nb.name, defaultName = "Notizbuch")
                    val fileName = "$safeBase.json"
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
}
