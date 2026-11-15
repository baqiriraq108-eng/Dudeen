package com.example.data.local

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import org.json.JSONArray
import org.json.JSONObject
import com.example.data.model.extractNounQuickDetails
import com.example.data.model.extractVerbQuickDetails
import com.example.data.model.optSafeString
import com.example.data.model.sanitizeHistoryWord
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Manages persistent storage of search history in a public external storage directory:
 * Documents/Deklination/history.json or a user-selected SAF folder.
 *
 * This allows search history (nouns and verbs) to survive a "Clear App Data" action,
 * since the external folder is not wiped by app data deletion.
 */
class ExternalHistoryStorage(private val context: Context) {

    companion object {
        private const val TAG = "ExternalHistoryStorage"
        const val SUBDIR_NAME = "Deklination"
        const val SYSTEM_BACKUPS_SUBFOLDER = "System_Backups"
        const val FILE_NAME = "history.json"
        const val PREFS_NAME = "history_folder_prefs"
        const val PREF_FOLDER_URI = "folder_tree_uri"
        const val PREF_FOLDER_NAME = "folder_display_name"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    init {
        try {
            migrateLegacyRootHistoryIfNeeded()
        } catch (e: Exception) {
            Log.w(TAG, "Initial history migration check note: ${e.message}")
        }
    }

    fun hasFolderUri(): Boolean {
        val uriString = prefs.getString(PREF_FOLDER_URI, null)
        return !uriString.isNullOrBlank()
    }

    fun getFolderUri(): Uri? {
        val uriString = prefs.getString(PREF_FOLDER_URI, null) ?: return null
        return try {
            Uri.parse(uriString)
        } catch (e: Exception) {
            null
        }
    }

    fun getFolderName(): String {
        return prefs.getString(PREF_FOLDER_NAME, null) ?: "Kein Ordner ausgewählt"
    }

    fun saveFolderUri(uri: Uri, preferredName: String? = null): String {
        try {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
        } catch (e: Exception) {
            Log.w(TAG, "takePersistableUriPermission note: ${e.message}")
        }

        val docName = try {
            DocumentFile.fromTreeUri(context, uri)?.name
        } catch (e: Exception) {
            null
        }

        val displayName = preferredName?.takeIf { it.isNotBlank() }
            ?: docName?.takeIf { it.isNotBlank() }
            ?: uri.lastPathSegment?.substringAfterLast(':')?.takeIf { it.isNotBlank() }
            ?: "Deklination"

        prefs.edit()
            .putString(PREF_FOLDER_URI, uri.toString())
            .putString(PREF_FOLDER_NAME, displayName)
            .apply()

        return displayName
    }

    fun clearFolderPreference() {
        prefs.edit()
            .remove(PREF_FOLDER_URI)
            .remove(PREF_FOLDER_NAME)
            .apply()
    }

    /**
     * Resolves the primary public external file: Documents/Deklination/System_Backups/history.json
     */
    fun getHistoryFile(): File {
        val publicDocs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val systemBackupsDir = File(File(publicDocs, SUBDIR_NAME), SYSTEM_BACKUPS_SUBFOLDER)
        return File(systemBackupsDir, FILE_NAME)
    }

    /**
     * Legacy root file location: Documents/Deklination/history.json
     */
    fun getLegacyRootHistoryFile(): File {
        val publicDocs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val deklinationDir = File(publicDocs, SUBDIR_NAME)
        return File(deklinationDir, FILE_NAME)
    }

    /**
     * Fallback file in external storage root if DIRECTORY_DOCUMENTS is inaccessible.
     */
    private fun getFallbackFile(): File {
        val extDir = Environment.getExternalStorageDirectory()
        return File(File(File(extDir, SUBDIR_NAME), SYSTEM_BACKUPS_SUBFOLDER), FILE_NAME)
    }

    /**
     * Safely migrates legacy root `Documents/Deklination/history.json` into
     * `Documents/Deklination/System_Backups/history.json`, verifies the written file is valid JSON,
     * and cleans up the root file so no clutter remains in the root directory.
     */
    @Synchronized
    fun migrateLegacyRootHistoryIfNeeded() {
        try {
            val legacyFile = getLegacyRootHistoryFile()
            if (!legacyFile.exists() || !legacyFile.isFile) return

            val targetFile = getHistoryFile()
            val systemBackupsDir = targetFile.parentFile ?: return
            if (!systemBackupsDir.exists()) {
                systemBackupsDir.mkdirs()
            }

            val legacyText = try {
                legacyFile.readText(Charsets.UTF_8).trim()
            } catch (_: Exception) {
                ""
            }

            if (legacyText.isEmpty()) {
                legacyFile.delete()
                return
            }

            val destFile = if (!targetFile.exists() || targetFile.length() == 0L) {
                targetFile
            } else {
                // Target already exists; check if target is empty JSON "[]" while legacy has real items
                val currentTargetText = try { targetFile.readText(Charsets.UTF_8).trim() } catch (_: Exception) { "" }
                if ((currentTargetText.isEmpty() || currentTargetText == "[]") && legacyText != "[]") {
                    targetFile
                } else {
                    File(systemBackupsDir, "legacy_root_history.json")
                }
            }

            val tempFile = File(systemBackupsDir, ".tmp_migrate_${System.nanoTime()}.json")
            tempFile.writeText(legacyText, Charsets.UTF_8)

            // Verify written content is complete and valid
            val verifyText = tempFile.readText(Charsets.UTF_8).trim()
            val isValidJson = verifyText.isNotEmpty() && try {
                if (verifyText.startsWith("[")) {
                    JSONArray(verifyText)
                    true
                } else if (verifyText.startsWith("{")) {
                    JSONObject(verifyText)
                    true
                } else false
            } catch (_: Exception) {
                false
            }

            if (isValidJson) {
                tempFile.copyTo(destFile, overwrite = true)
                tempFile.delete()
                if (destFile.exists() && destFile.length() > 0L) {
                    legacyFile.delete()
                }
            } else {
                tempFile.delete()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error during legacy root history migration: ${e.message}")
        }
    }

    /**
     * Loads the search history from the user-selected folder or Documents/Deklination/System_Backups/history.json.
     * Supports fallback reading from legacy root history.json if needed.
     */
    @Synchronized
    fun loadHistory(): List<WordHistoryEntity> {
        migrateLegacyRootHistoryIfNeeded()

        // 1. Try reading from the user-selected SAF folder if available
        val folderUri = getFolderUri()
        if (folderUri != null) {
            try {
                val docDir = DocumentFile.fromTreeUri(context, folderUri)
                if (docDir != null && docDir.exists()) {
                    // Check System_Backups/history.json first
                    val sysBackupDir = docDir.findFile(SYSTEM_BACKUPS_SUBFOLDER)
                    val sysHistoryDoc = sysBackupDir?.findFile(FILE_NAME)
                    if (sysHistoryDoc != null && sysHistoryDoc.exists() && sysHistoryDoc.length() > 0L) {
                        val content = context.contentResolver.openInputStream(sysHistoryDoc.uri)?.bufferedReader()?.use { it.readText() }
                        if (!content.isNullOrBlank()) {
                            val items = parseJsonHistory(content)
                            if (items.isNotEmpty()) return items
                        }
                    }

                    // Fallback to legacy root history.json in SAF folder
                    val legacyRootDoc = docDir.findFile(FILE_NAME)
                    if (legacyRootDoc != null && legacyRootDoc.exists() && legacyRootDoc.length() > 0L) {
                        val content = context.contentResolver.openInputStream(legacyRootDoc.uri)?.bufferedReader()?.use { it.readText() }
                        if (!content.isNullOrBlank()) {
                            val items = parseJsonHistory(content)
                            // Safely migrate to System_Backups/ inside SAF folder if writable
                            if (docDir.canWrite()) {
                                try {
                                    val targetSub = docDir.findFile(SYSTEM_BACKUPS_SUBFOLDER)
                                        ?: docDir.createDirectory(SYSTEM_BACKUPS_SUBFOLDER)
                                    val migratedDoc = targetSub?.findFile(FILE_NAME)
                                        ?: targetSub?.createFile("application/json", FILE_NAME)
                                    if (migratedDoc != null) {
                                        context.contentResolver.openOutputStream(migratedDoc.uri, "wt")?.bufferedWriter()?.use { w ->
                                            w.write(content)
                                            w.flush()
                                        }
                                        if (migratedDoc.length() > 0L) {
                                            legacyRootDoc.delete()
                                        }
                                    }
                                } catch (_: Exception) {}
                            }
                            if (items.isNotEmpty()) return items
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed reading history from SAF folder: ${e.message}", e)
            }
        }

        // 2. Fallback to direct file on public external storage (System_Backups/history.json or legacy root)
        val file = getHistoryFile()
        val legacyRoot = getLegacyRootHistoryFile()
        val targetFile = when {
            file.exists() && file.length() > 0L -> file
            legacyRoot.exists() && legacyRoot.length() > 0L -> legacyRoot
            else -> {
                val fallback = getFallbackFile()
                if (fallback.exists() && fallback.length() > 0L) fallback else file
            }
        }

        if (!targetFile.exists() || targetFile.length() == 0L) {
            val mediaStoreContent = readFromMediaStore()
            if (!mediaStoreContent.isNullOrBlank()) {
                return parseJsonHistory(mediaStoreContent)
            }
            return emptyList()
        }

        return try {
            val jsonString = FileInputStream(targetFile).bufferedReader().use { it.readText() }
            parseJsonHistory(jsonString)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read external history file: ${targetFile.absolutePath}", e)
            val mediaStoreContent = readFromMediaStore()
            if (!mediaStoreContent.isNullOrBlank()) {
                parseJsonHistory(mediaStoreContent)
            } else {
                emptyList()
            }
        }
    }

    /**
     * Reads history items from any JSON file selected via ACTION_OPEN_DOCUMENT.
     * Supports both legacy/raw JSON arrays (`history.json`) and unified notebook objects (`Notebook_<Name>.json`).
     */
    fun readHistoryFromUri(uri: Uri): List<WordHistoryEntity> {
        return try {
            val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            if (!content.isNullOrBlank()) {
                parseJsonHistory(content)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed reading history from URI: $uri", e)
            emptyList()
        }
    }

    /**
     * Saves the entire list of WordHistoryEntity to the user-selected folder (under System_Backups/)
     * and to Documents/Deklination/System_Backups/history.json.
     */
    @Synchronized
    fun saveHistory(items: List<WordHistoryEntity>): Boolean {
        val jsonArray = serializeHistoryToJson(items)
        val jsonContent = jsonArray.toString(2)

        // 1. Write to user-selected SAF folder (under System_Backups/) if configured
        var writtenToSaf = false
        val folderUri = getFolderUri()
        if (folderUri != null) {
            try {
                val docDir = DocumentFile.fromTreeUri(context, folderUri)
                if (docDir != null && docDir.exists() && docDir.canWrite()) {
                    val sysBackupDir = docDir.findFile(SYSTEM_BACKUPS_SUBFOLDER)
                        ?: docDir.createDirectory(SYSTEM_BACKUPS_SUBFOLDER)
                        ?: docDir
                    var historyDoc = sysBackupDir.findFile(FILE_NAME)
                    if (historyDoc == null) {
                        historyDoc = sysBackupDir.createFile("application/json", FILE_NAME)
                    }
                    historyDoc?.let { target ->
                        context.contentResolver.openOutputStream(target.uri, "wt")?.bufferedWriter()?.use { writer ->
                            writer.write(jsonContent)
                            writer.flush()
                        }
                        writtenToSaf = true
                    }
                    // Clean up legacy root history.json in SAF folder if it was migrated to System_Backups
                    if (writtenToSaf && sysBackupDir != docDir) {
                        docDir.findFile(FILE_NAME)?.delete()
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Writing to SAF folder failed: ${e.message}")
            }
        }

        // 2. Also write to direct file in Documents/Deklination/System_Backups/history.json
        var writtenDirectly = false
        try {
            val file = getHistoryFile()
            file.parentFile?.let { parent ->
                if (!parent.exists()) {
                    parent.mkdirs()
                }
            }
            FileOutputStream(file, false).bufferedWriter().use { writer ->
                writer.write(jsonContent)
                writer.flush()
            }
            writtenDirectly = true
            // Clean up legacy root history.json if present
            val legacyRoot = getLegacyRootHistoryFile()
            if (legacyRoot.exists() && file.exists() && file.length() > 0L) {
                legacyRoot.delete()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Direct write to Documents/Deklination/System_Backups/history.json failed, trying fallback: ${e.message}")
            try {
                val fallback = getFallbackFile()
                fallback.parentFile?.let { if (!it.exists()) it.mkdirs() }
                FileOutputStream(fallback, false).bufferedWriter().use { writer ->
                    writer.write(jsonContent)
                    writer.flush()
                }
                writtenDirectly = true
            } catch (ex2: Exception) {
                Log.w(TAG, "Fallback direct write failed: ${ex2.message}")
            }
        }

        // On Android 10+ (Q+), also sync to MediaStore if available
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                writeToMediaStore(jsonContent)
            } catch (e: Exception) {
                Log.d(TAG, "MediaStore write note: ${e.message}")
            }
        }

        return writtenToSaf || writtenDirectly
    }

    /**
     * Appends a new item or updates an existing item in-place in the persistent history file.
     * Deduplicates by word and type (case-insensitive).
     * If the item already exists, it stays in its original position with its original timestamp
     * (updating fields such as manualTranslation/meaningEnglish in-place without duplicating).
     */
    @Synchronized
    fun appendOrUpdateItem(item: WordHistoryEntity): Boolean {
        val currentItems = loadHistory().toMutableList()
        val existingIndex = currentItems.indexOfFirst {
            it.word.trim().equals(item.word.trim(), ignoreCase = true) &&
            it.type.trim().equals(item.type.trim(), ignoreCase = true)
        }
        if (existingIndex >= 0) {
            val existing = currentItems[existingIndex]
            val effectiveManual = if (item.manualTranslation.isNotBlank()) {
                item.manualTranslation
            } else {
                existing.manualTranslation
            }
            val updatedInPlace = existing.copy(
                meaningEnglish = item.meaningEnglish.ifBlank { existing.meaningEnglish },
                data = item.data.ifBlank { existing.data },
                rawJsonResult = item.rawJsonResult.ifBlank { existing.rawJsonResult },
                gender = item.gender.ifBlank { existing.gender },
                genderArticle = item.genderArticle.ifBlank { existing.genderArticle },
                manualTranslation = effectiveManual
            )
            if (updatedInPlace == existing) {
                return true
            }
            currentItems[existingIndex] = updatedInPlace
            return saveHistory(currentItems)
        } else {
            currentItems.add(0, item)
            return saveHistory(currentItems)
        }
    }

    /**
     * Cumulatively adds a truly new entry to the backup history ONLY if it does not already exist.
     * Existing entries are never duplicated, overwritten, or reordered.
     */
    @Synchronized
    fun appendIfAbsent(item: WordHistoryEntity): Boolean {
        val currentItems = loadHistory().toMutableList()
        val exists = currentItems.any {
            it.word.trim().equals(item.word.trim(), ignoreCase = true) &&
            it.type.trim().equals(item.type.trim(), ignoreCase = true)
        }
        if (exists) {
            return true
        }
        currentItems.add(0, item)
        return saveHistory(currentItems)
    }

    /**
     * Cumulatively merges a list of items into the backup history without losing older backup items
     * and without duplicating or reordering existing entries.
     */
    @Synchronized
    fun mergeHistoryCumulatively(incomingItems: List<WordHistoryEntity>): Boolean {
        if (incomingItems.isEmpty()) return true
        val currentItems = loadHistory().toMutableList()
        val existingKeys = currentItems.mapTo(HashSet()) {
            "${it.word.trim().lowercase()}_${it.type.trim().lowercase()}"
        }
        val newToPrepend = mutableListOf<WordHistoryEntity>()
        for (candidate in incomingItems) {
            val key = "${candidate.word.trim().lowercase()}_${candidate.type.trim().lowercase()}"
            if (existingKeys.add(key)) {
                newToPrepend.add(candidate)
            } else {
                // If manualTranslation was added on candidate, preserve it in-place on existing item
                if (candidate.manualTranslation.isNotBlank()) {
                    val idx = currentItems.indexOfFirst {
                        "${it.word.trim().lowercase()}_${it.type.trim().lowercase()}" == key
                    }
                    if (idx >= 0 && currentItems[idx].manualTranslation != candidate.manualTranslation) {
                        currentItems[idx] = currentItems[idx].copy(manualTranslation = candidate.manualTranslation)
                    }
                }
            }
        }
        if (newToPrepend.isNotEmpty()) {
            currentItems.addAll(0, newToPrepend)
        }
        return saveHistory(currentItems)
    }

    /**
     * Deletes an item from the persistent history file by its ID or by word & type.
     */
    @Synchronized
    fun deleteItem(id: Long, word: String? = null, type: String? = null): Boolean {
        val currentItems = loadHistory().toMutableList()
        val removed = currentItems.removeAll {
            it.id == id || (word != null && type != null &&
                it.word.equals(word, ignoreCase = true) &&
                it.type.equals(type, ignoreCase = true))
        }
        return if (removed) {
            saveHistory(currentItems)
        } else {
            true
        }
    }

    /**
     * Clears the persistent history file content when user taps "Leeren".
     */
    @Synchronized
    fun clearHistory(): Boolean {
        return saveHistory(emptyList())
    }

    private fun serializeHistoryToJson(items: List<WordHistoryEntity>): JSONArray {
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

    private fun parseJsonHistory(jsonString: String): List<WordHistoryEntity> {
        val result = mutableListOf<WordHistoryEntity>()
        val trimmed = jsonString.trim()
        if (trimmed.isEmpty() || trimmed == "[]" || trimmed == "{}") return result

        try {
            val jsonArray = if (trimmed.startsWith("{")) {
                val rootObj = JSONObject(trimmed)
                rootObj.optJSONArray("words")
                    ?: rootObj.optJSONArray("history")
                    ?: rootObj.optJSONArray("notebooks")?.optJSONObject(0)?.let {
                        it.optJSONArray("words") ?: it.optJSONArray("history")
                    }
                    ?: return result
            } else {
                JSONArray(trimmed)
            }
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
                    // Strip wrapper fields if falling back to obj.toString() to prevent recursive bloat
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
                    continue // Skip and purge corrupted null records
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

    private fun readFromMediaStore(): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        return try {
            val resolver = context.contentResolver
            val uri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val projection = arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DISPLAY_NAME)
            val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND ${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?"
            val selectionArgs = arrayOf(FILE_NAME, "Documents/$SUBDIR_NAME%")

            resolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                    val fileUri = Uri.withAppendedPath(uri, id.toString())
                    resolver.openInputStream(fileUri)?.bufferedReader()?.use { it.readText() }
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun writeToMediaStore(content: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        try {
            val resolver = context.contentResolver
            val collectionUri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val projection = arrayOf(MediaStore.MediaColumns._ID)
            val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND ${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?"
            val selectionArgs = arrayOf(FILE_NAME, "Documents/$SUBDIR_NAME/$SYSTEM_BACKUPS_SUBFOLDER%")

            var targetUri: Uri? = null
            resolver.query(collectionUri, projection, selection, selectionArgs, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                    targetUri = Uri.withAppendedPath(collectionUri, id.toString())
                }
            }

            if (targetUri == null) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, FILE_NAME)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Documents/$SUBDIR_NAME/$SYSTEM_BACKUPS_SUBFOLDER/")
                }
                targetUri = resolver.insert(collectionUri, contentValues)
            }

            targetUri?.let { dest ->
                resolver.openOutputStream(dest, "wt")?.bufferedWriter()?.use { writer ->
                    writer.write(content)
                    writer.flush()
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "writeToMediaStore error: ${e.message}")
        }
    }
}
