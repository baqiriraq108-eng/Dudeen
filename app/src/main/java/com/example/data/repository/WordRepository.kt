package com.example.data.repository

import com.example.data.AiModelInfo
import com.example.data.ApiKeyManager
import com.example.data.ModelProvider
import com.example.data.local.ExternalHistoryStorage
import com.example.data.local.SearchQueryDao
import com.example.data.local.SearchQueryEntity
import com.example.data.local.WordHistoryDao
import com.example.data.local.WordHistoryEntity
import com.example.data.model.WordDeclensionResult
import com.example.data.model.withResolvedSentenceExamples
import com.example.data.model.withResolvedWordExamples
import com.example.data.remote.GeminiDeclensionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext

class WordRepository(
    private val historyDao: WordHistoryDao,
    private val geminiService: GeminiDeclensionService,
    private val apiKeyManager: ApiKeyManager,
    private val searchQueryDao: SearchQueryDao? = null,
    val externalHistoryStorage: ExternalHistoryStorage? = null,
    val notebookRepository: com.example.data.notebook.NotebookRepository? = null
) {
    val searchHistory: Flow<List<WordHistoryEntity>> = historyDao.getAllHistory()

    val recentSearchQueries: Flow<List<SearchQueryEntity>> =
        searchQueryDao?.getAllRecentQueries() ?: flowOf(emptyList())

    fun getRecentQueriesByType(type: com.example.data.model.GrammarType): Flow<List<SearchQueryEntity>> =
        searchQueryDao?.getRecentQueriesByType(type.displayName) ?: flowOf(emptyList())

    suspend fun recordSearchQuery(query: String, type: com.example.data.model.GrammarType) {
        val trimmed = query.trim()
        if (trimmed.isBlank() || searchQueryDao == null) return
        val existing = searchQueryDao.findQuery(trimmed, type.displayName)
        if (existing != null) {
            searchQueryDao.insertOrUpdate(
                existing.copy(
                    timestamp = System.currentTimeMillis(),
                    lookupCount = existing.lookupCount + 1
                )
            )
        } else {
            searchQueryDao.insertOrUpdate(
                SearchQueryEntity(
                    query = trimmed,
                    grammarType = type.displayName,
                    timestamp = System.currentTimeMillis(),
                    lookupCount = 1
                )
            )
        }
    }

    suspend fun deleteSearchQuery(id: Long) {
        searchQueryDao?.deleteById(id)
    }

    suspend fun clearAllSearchQueries() {
        searchQueryDao?.clearAllQueries()
    }

    fun getHistoryByType(type: com.example.data.model.GrammarType): Flow<List<WordHistoryEntity>> =
        historyDao.getHistoryByType(type.displayName)

    fun isApiKeyConfigured(): Boolean = apiKeyManager.isKeyConfigured()

    fun getSelectedProvider(): ModelProvider = apiKeyManager.getSelectedProvider()

    fun saveSelectedProvider(provider: ModelProvider) = apiKeyManager.saveSelectedProvider(provider)

    fun getGeminiApiKey(): String = apiKeyManager.getGeminiApiKey()

    fun getUserEnteredGeminiKey(): String = apiKeyManager.getUserEnteredGeminiKey()

    fun saveGeminiApiKey(key: String) = apiKeyManager.saveGeminiApiKey(key)

    fun clearGeminiApiKey() = apiKeyManager.clearGeminiApiKey()

    fun getOpenRouterApiKey(): String = apiKeyManager.getOpenRouterApiKey()

    fun saveOpenRouterApiKey(key: String) = apiKeyManager.saveOpenRouterApiKey(key)

    fun clearOpenRouterApiKey() = apiKeyManager.clearOpenRouterApiKey()

    fun getOtherApiKey(): String = apiKeyManager.getOpenRouterApiKey()

    fun saveOtherApiKey(key: String) = apiKeyManager.saveOpenRouterApiKey(key)

    fun clearOtherApiKey() = apiKeyManager.clearOpenRouterApiKey()

    fun getActiveApiKey(): String = apiKeyManager.getActiveApiKey()

    fun getApiKey(): String = getActiveApiKey()

    fun getUserEnteredKey(): String = getUserEnteredGeminiKey()

    fun saveApiKey(key: String) = saveGeminiApiKey(key)

    fun clearApiKey() = apiKeyManager.clearApiKey()

    fun getSelectedModel(provider: ModelProvider = getSelectedProvider()): String =
        apiKeyManager.getSelectedModel(provider)

    fun saveSelectedModel(modelId: String, provider: ModelProvider = getSelectedProvider()) =
        apiKeyManager.saveSelectedModel(modelId, provider)

    fun getAvailableModelsForProvider(provider: ModelProvider = getSelectedProvider()): List<AiModelInfo> =
        apiKeyManager.getModelsForActiveProvider(provider)

    suspend fun refreshGeminiModels(apiKey: String = getGeminiApiKey()): Result<List<AiModelInfo>> {
        val result = geminiService.fetchGeminiModelsFromApi(apiKey)
        return if (result.isSuccess) {
            val json = result.getOrNull().orEmpty()
            apiKeyManager.saveCachedGeminiModels(json)
            val parsedModels = apiKeyManager.parseGeminiModelsJson(json)
            Result.success(parsedModels)
        } else {
            // Fallback to cached models if available
            val cached = apiKeyManager.getCachedGeminiModels()
            val exception = result.exceptionOrNull() ?: Exception("Invalid Gemini API Key. Please check your key at ai.google.dev.")
            if (cached.isNotEmpty()) {
                Result.success(cached)
            } else {
                Result.failure(exception)
            }
        }
    }

    suspend fun refreshOpenRouterModels(apiKey: String = getOpenRouterApiKey()): Result<List<AiModelInfo>> {
        val result = geminiService.fetchOpenRouterModelsFromApi(apiKey)
        return if (result.isSuccess) {
            val json = result.getOrNull().orEmpty()
            apiKeyManager.saveCachedOpenRouterModels(json)
            val parsedModels = apiKeyManager.parseOpenRouterModelsJson(json)
            Result.success(parsedModels)
        } else {
            // Fallback to cached models if available
            val cached = apiKeyManager.getCachedOpenRouterModels()
            val exception = result.exceptionOrNull() ?: Exception("Modelle konnten nicht online geladen werden")
            if (cached.isNotEmpty()) {
                Result.success(cached)
            } else {
                Result.failure(exception)
            }
        }
    }

    fun getDailyUsage(provider: ModelProvider = getSelectedProvider(), modelId: String = getSelectedModel(provider)): Pair<Int, Int> =
        apiKeyManager.getDailyUsage(provider, modelId)

    suspend fun testApiKey(provider: ModelProvider, key: String, modelId: String): Result<String> {
        return geminiService.testApiKey(provider, key, modelId)
    }

    suspend fun searchWord(word: String, translationLang: String = "en"): WordDeclensionResult {
        val grammarResult = searchGrammar(word, com.example.data.model.GrammarType.NOMEN, translationLang)
        return when (grammarResult) {
            is com.example.data.model.GrammarResult.Noun -> grammarResult.declension
            is com.example.data.model.GrammarResult.Sentence -> grammarResult.declension
            is com.example.data.model.GrammarResult.NomenList -> grammarResult.declension
            is com.example.data.model.GrammarResult.Verb -> throw IllegalStateException("Erwartetes Nomen, aber Verb erhalten.")
            is com.example.data.model.GrammarResult.Adjektiv -> throw IllegalStateException("Erwartetes Nomen, aber Adjektiv erhalten.")
        }
    }

    suspend fun searchVerb(word: String, translationLang: String = "en"): com.example.data.model.VerbConjugationResult {
        val grammarResult = searchGrammar(word, com.example.data.model.GrammarType.VERB, translationLang)
        return when (grammarResult) {
            is com.example.data.model.GrammarResult.Verb -> grammarResult.conjugation
            is com.example.data.model.GrammarResult.Noun -> throw IllegalStateException("Erwartetes Verb, aber Nomen erhalten.")
            is com.example.data.model.GrammarResult.Sentence -> throw IllegalStateException("Erwartetes Verb, aber Satz erhalten.")
            is com.example.data.model.GrammarResult.NomenList -> throw IllegalStateException("Erwartetes Verb, aber Nomen erhalten.")
            is com.example.data.model.GrammarResult.Adjektiv -> throw IllegalStateException("Erwartetes Verb, aber Adjektiv erhalten.")
        }
    }

    suspend fun searchAdjektiv(word: String, translationLang: String = "en"): com.example.data.model.AdjektivResult {
        val grammarResult = searchGrammar(word, com.example.data.model.GrammarType.ADJEKTIV, translationLang)
        return when (grammarResult) {
            is com.example.data.model.GrammarResult.Adjektiv -> grammarResult.result
            else -> throw IllegalStateException("Erwartetes Adjektiv, aber anderer Typ erhalten.")
        }
    }

    suspend fun searchGrammar(
        word: String,
        type: com.example.data.model.GrammarType,
        translationLang: String = "en"
    ): com.example.data.model.GrammarResult {
        val provider = apiKeyManager.getSelectedProvider()
        val apiKey = apiKeyManager.getActiveApiKey()
        val modelId = apiKeyManager.getSelectedModel(provider)

        val result = geminiService.fetchGrammar(
            word = word,
            type = type,
            apiKey = apiKey,
            provider = provider,
            selectedModelId = modelId,
            translationLang = translationLang
        )

        // Increment daily usage on successful lookup
        apiKeyManager.incrementDailyUsage(provider)

        val cleanResultWord = com.example.data.model.sanitizeHistoryWord(
            result.word.takeUnless { it.isBlank() || it.equals("null", ignoreCase = true) } ?: word,
            result.rawJson,
            type.displayName
        ).ifBlank { word.trim() }

        if (cleanResultWord.isBlank() || cleanResultWord.equals("null", ignoreCase = true)) {
            return result
        }

        // Save or update in search history
        val entity = when (result) {
            is com.example.data.model.GrammarResult.Noun -> {
                val declension = result.declension
                WordHistoryEntity(
                    word = cleanResultWord,
                    type = com.example.data.model.GrammarType.NOMEN.displayName,
                    data = declension.rawJson,
                    gender = declension.gender.takeUnless { it.equals("null", ignoreCase = true) }.orEmpty(),
                    genderArticle = declension.genderArticle.takeUnless { it.equals("null", ignoreCase = true) }.orEmpty(),
                    meaningEnglish = declension.meaningEnglish.takeUnless { it.equals("null", ignoreCase = true) }.orEmpty(),
                    rawJsonResult = declension.rawJson,
                    timestamp = System.currentTimeMillis(),
                    translationLanguage = translationLang
                )
            }
            is com.example.data.model.GrammarResult.Verb -> {
                val conjugation = result.conjugation
                WordHistoryEntity(
                    word = cleanResultWord,
                    type = com.example.data.model.GrammarType.VERB.displayName,
                    data = conjugation.rawJson,
                    gender = conjugation.hilfsverb.takeUnless { it.equals("null", ignoreCase = true) }.orEmpty(),
                    genderArticle = conjugation.partizip2.takeUnless { it.equals("null", ignoreCase = true) }.orEmpty(),
                    meaningEnglish = conjugation.meaningEnglish.takeUnless { it.equals("null", ignoreCase = true) }.orEmpty(),
                    rawJsonResult = conjugation.rawJson,
                    timestamp = System.currentTimeMillis(),
                    translationLanguage = translationLang
                )
            }
            is com.example.data.model.GrammarResult.Sentence -> {
                val declension = result.declension.withResolvedSentenceExamples()
                WordHistoryEntity(
                    word = cleanResultWord,
                    type = com.example.data.model.GrammarType.SENTENCE.displayName,
                    data = declension.rawJson,
                    gender = "–",
                    genderArticle = "",
                    meaningEnglish = declension.meaningEnglish.takeUnless { it.equals("null", ignoreCase = true) }.orEmpty(),
                    rawJsonResult = declension.rawJson,
                    timestamp = System.currentTimeMillis(),
                    translationLanguage = translationLang
                )
            }
            is com.example.data.model.GrammarResult.NomenList -> {
                val declension = result.declension.withResolvedWordExamples()
                val isMultiple = com.example.data.model.isMultipleWordsInput(declension.word)
                WordHistoryEntity(
                    word = cleanResultWord,
                    type = com.example.data.model.GrammarType.NOMEN_LIST.displayName,
                    data = declension.rawJson,
                    gender = if (isMultiple) "Wörter" else "Wort",
                    genderArticle = "",
                    meaningEnglish = declension.meaningEnglish.takeUnless { it.equals("null", ignoreCase = true) }.orEmpty(),
                    rawJsonResult = declension.rawJson,
                    timestamp = System.currentTimeMillis(),
                    translationLanguage = translationLang
                )
            }
            is com.example.data.model.GrammarResult.Adjektiv -> {
                val adj = result.result
                WordHistoryEntity(
                    word = cleanResultWord,
                    type = com.example.data.model.GrammarType.ADJEKTIV.displayName,
                    data = adj.rawJson,
                    gender = "Adjektiv",
                    genderArticle = "",
                    meaningEnglish = adj.meaningEnglish.takeUnless { it.equals("null", ignoreCase = true) }.orEmpty(),
                    rawJsonResult = adj.rawJson,
                    timestamp = System.currentTimeMillis(),
                    translationLanguage = translationLang
                )
            }
        }

        val existing = historyDao.findByWordAndType(cleanResultWord, type.displayName)
        var preservedManualTranslation = existing?.manualTranslation.orEmpty()
        if (preservedManualTranslation.isBlank()) {
            preservedManualTranslation = notebookRepository?.loadActiveHistory()
                ?.find { it.word.equals(cleanResultWord, ignoreCase = true) && it.type.equals(type.displayName, ignoreCase = true) }
                ?.manualTranslation.orEmpty()
        }
        if (preservedManualTranslation.isBlank()) {
            preservedManualTranslation = externalHistoryStorage?.loadHistory()
                ?.find { it.word.equals(cleanResultWord, ignoreCase = true) && it.type.equals(type.displayName, ignoreCase = true) }
                ?.manualTranslation.orEmpty()
        }
        val entityToSave = if (preservedManualTranslation.isNotBlank()) {
            entity.copy(manualTranslation = preservedManualTranslation)
        } else {
            entity
        }

        historyDao.deleteByWordAndType(cleanResultWord, type.displayName)
        val insertedId = historyDao.insert(entityToSave)
        val savedEntity = entityToSave.copy(id = insertedId)
        externalHistoryStorage?.appendOrUpdateItem(savedEntity)
        notebookRepository?.appendOrUpdateItemInActiveHistory(savedEntity)

        // Track user search query in local Room storage
        recordSearchQuery(cleanResultWord, type)

        return result
    }

    /**
     * Synchronizes persistent notebook history / external history with Room database.
     * Merges items intelligently so manual translations and word records are never lost.
     */
    suspend fun syncHistoryOnStartup() = withContext(Dispatchers.IO) {
        historyDao.deleteCorruptedNullEntries()

        val rawRoomItems = historyDao.getAllHistoryList()
        val roomItems = rawRoomItems.mapNotNull { item ->
            val sanitized = com.example.data.model.sanitizeHistoryWord(item.word, item.data.ifBlank { item.rawJsonResult }, item.type)
            if (sanitized.isBlank() || sanitized.equals("null", ignoreCase = true)) null
            else {
                var repaired = item
                if (sanitized != item.word) repaired = repaired.copy(word = sanitized)
                if (repaired.genderArticle.equals("null", ignoreCase = true)) repaired = repaired.copy(genderArticle = "")
                if (repaired.gender.equals("null", ignoreCase = true)) repaired = repaired.copy(gender = "")
                repaired
            }
        }

        if (notebookRepository != null) {
            val rawNotebookItems = notebookRepository.loadActiveHistory()
            val notebookItems = rawNotebookItems.mapNotNull { item ->
                val sanitized = com.example.data.model.sanitizeHistoryWord(item.word, item.data.ifBlank { item.rawJsonResult }, item.type)
                if (sanitized.isBlank() || sanitized.equals("null", ignoreCase = true)) null
                else {
                    var repaired = item
                    if (sanitized != item.word) repaired = repaired.copy(word = sanitized)
                    if (repaired.genderArticle.equals("null", ignoreCase = true)) repaired = repaired.copy(genderArticle = "")
                    if (repaired.gender.equals("null", ignoreCase = true)) repaired = repaired.copy(gender = "")
                    repaired
                }
            }

            if (notebookItems.isEmpty() && roomItems.isNotEmpty()) {
                notebookRepository.saveActiveHistory(roomItems)
            } else if (notebookItems.isNotEmpty()) {
                val merged = mergeHistoryLists(notebookItems, roomItems)
                historyDao.clearAll()
                historyDao.insertAll(merged)
                notebookRepository.saveActiveHistory(merged)
            }
            return@withContext
        }

        val storage = externalHistoryStorage ?: return@withContext
        val rawExternalItems = storage.loadHistory()
        val externalItems = rawExternalItems.mapNotNull { item ->
            val sanitized = com.example.data.model.sanitizeHistoryWord(item.word, item.data.ifBlank { item.rawJsonResult }, item.type)
            if (sanitized.isBlank() || sanitized.equals("null", ignoreCase = true)) null
            else if (sanitized != item.word) item.copy(word = sanitized)
            else item
        }

        if (externalItems.isNotEmpty() && roomItems.isEmpty()) {
            historyDao.insertAll(externalItems)
        } else if (roomItems.isNotEmpty() && externalItems.isEmpty()) {
            storage.saveHistory(roomItems)
        } else if (externalItems.isNotEmpty() || roomItems.isNotEmpty()) {
            val merged = mergeHistoryLists(externalItems, roomItems)
            historyDao.clearAll()
            historyDao.insertAll(merged)
            storage.saveHistory(merged)
        }
    }

    private fun mergeHistoryLists(
        primary: List<WordHistoryEntity>,
        secondary: List<WordHistoryEntity>
    ): List<WordHistoryEntity> {
        val keyMap = mutableMapOf<String, WordHistoryEntity>()

        for (item in primary) {
            val key = "${item.word.trim().lowercase()}_${item.type.trim().lowercase()}"
            keyMap[key] = item
        }

        for (item in secondary) {
            val key = "${item.word.trim().lowercase()}_${item.type.trim().lowercase()}"
            val existing = keyMap[key]
            if (existing == null) {
                keyMap[key] = item
            } else {
                // If one has a non-empty manualTranslation, preserve it!
                val manual = when {
                    existing.manualTranslation.isNotBlank() -> existing.manualTranslation
                    item.manualTranslation.isNotBlank() -> item.manualTranslation
                    else -> ""
                }
                val base = if (item.timestamp > existing.timestamp) item else existing
                val merged = base.copy(
                    manualTranslation = manual,
                    data = base.data.ifBlank { existing.data }.ifBlank { item.data },
                    rawJsonResult = base.rawJsonResult.ifBlank { existing.rawJsonResult }.ifBlank { item.rawJsonResult }
                )
                keyMap[key] = merged
            }
        }

        return keyMap.values.sortedByDescending { it.timestamp }
    }

    suspend fun reloadRoomWithItems(items: List<WordHistoryEntity>) = withContext(Dispatchers.IO) {
        historyDao.clearAll()
        if (items.isNotEmpty()) {
            historyDao.insertAll(items)
        }
    }

    suspend fun getGrammarFromHistory(entity: WordHistoryEntity): com.example.data.model.GrammarResult = withContext(Dispatchers.Default) {
        val json = if (entity.data.isNotBlank()) entity.data else entity.rawJsonResult
        val type = com.example.data.model.GrammarType.fromString(entity.type)
        try {
            com.example.data.model.GrammarResult.fromJson(json, fallbackType = type, fallbackWord = entity.word)
        } catch (e: Exception) {
            searchGrammar(entity.word, type)
        }
    }

    suspend fun getWordFromHistory(entity: WordHistoryEntity): WordDeclensionResult {
        val result = getGrammarFromHistory(entity)
        return when (result) {
            is com.example.data.model.GrammarResult.Noun -> result.declension
            is com.example.data.model.GrammarResult.Sentence -> result.declension
            is com.example.data.model.GrammarResult.NomenList -> result.declension
            is com.example.data.model.GrammarResult.Verb -> throw IllegalStateException("Eintrag ist ein Verb")
            is com.example.data.model.GrammarResult.Adjektiv -> throw IllegalStateException("Eintrag ist ein Adjektiv")
        }
    }

    suspend fun deleteHistoryItem(id: Long) = withContext(Dispatchers.IO) {
        val entity = historyDao.findById(id)
        historyDao.deleteById(id)
        externalHistoryStorage?.deleteItem(id)
        notebookRepository?.deleteItemInActiveHistory(id, entity?.word, entity?.type)
    }

    suspend fun clearAllHistory() = withContext(Dispatchers.IO) {
        historyDao.clearAll()
        externalHistoryStorage?.clearHistory()
        notebookRepository?.clearActiveHistory()
    }

    suspend fun clearHistoryByType(type: com.example.data.model.GrammarType) = withContext(Dispatchers.IO) {
        historyDao.clearByType(type.displayName)
        val remaining = historyDao.getAllHistoryList()
        externalHistoryStorage?.saveHistory(remaining)
        notebookRepository?.saveActiveHistory(remaining)
    }

    fun hasHistoryFolder(): Boolean {
        return externalHistoryStorage?.hasFolderUri() == true
    }

    fun getHistoryFolderName(): String {
        return externalHistoryStorage?.getFolderName() ?: "Kein Ordner ausgewählt"
    }

    suspend fun onFolderSelected(uri: android.net.Uri): Int {
        val storage = externalHistoryStorage ?: return 0
        storage.saveFolderUri(uri)
        // Automatically import history from history.json if it exists in the selected folder
        syncHistoryOnStartup()
        return historyDao.getAllHistoryList().size
    }

    suspend fun importHistoryFromFile(fileUri: android.net.Uri, replace: Boolean): Int {
        val storage = externalHistoryStorage ?: return 0
        val importedItems = storage.readHistoryFromUri(fileUri)
        if (importedItems.isEmpty()) return 0

        val currentNotebookItems = notebookRepository?.loadActiveHistory() ?: historyDao.getAllHistoryList()
        val updatedList = if (replace) {
            importedItems
        } else {
            // Merge into active notebook ONLY
            val currentKeyMap = currentNotebookItems.associateBy { "${it.word.trim().lowercase()}_${it.type.trim().lowercase()}" }.toMutableMap()
            for (item in importedItems) {
                val key = "${item.word.trim().lowercase()}_${item.type.trim().lowercase()}"
                currentKeyMap[key] = item
            }
            currentKeyMap.values.sortedByDescending { it.timestamp }
        }

        // Save strictly to the active notebook without touching any other notebooks
        notebookRepository?.saveActiveHistory(updatedList)

        // Reload Room with active notebook items only
        historyDao.clearAll()
        if (updatedList.isNotEmpty()) {
            historyDao.insertAll(updatedList)
        }

        // Update external storage for active notebook if configured
        storage.saveHistory(updatedList)
        return importedItems.size
    }

    suspend fun updateManualTranslation(
        word: String,
        manualTranslation: String,
        type: String? = null,
        grammarResult: com.example.data.model.GrammarResult? = null
    ) = withContext(Dispatchers.IO) {
        val cleanManual = manualTranslation.trim()
        val cleanWord = com.example.data.model.sanitizeHistoryWord(word, null, type).ifBlank { word.trim() }
        if (cleanWord.isBlank() || cleanWord.equals("null", ignoreCase = true)) return@withContext

        val resolvedType = type?.trim()?.ifBlank { null } ?: when (grammarResult) {
            is com.example.data.model.GrammarResult.Noun -> com.example.data.model.GrammarType.NOMEN.displayName
            is com.example.data.model.GrammarResult.Verb -> com.example.data.model.GrammarType.VERB.displayName
            is com.example.data.model.GrammarResult.Adjektiv -> com.example.data.model.GrammarType.ADJEKTIV.displayName
            is com.example.data.model.GrammarResult.Sentence -> com.example.data.model.GrammarType.SENTENCE.displayName
            is com.example.data.model.GrammarResult.NomenList -> com.example.data.model.GrammarType.NOMEN_LIST.displayName
            null -> null
        }

        if (!resolvedType.isNullOrBlank()) {
            historyDao.updateManualTranslationByWordAndType(cleanWord, resolvedType, cleanManual)
        } else {
            // Strictly check if only one type exists to prevent polluting different parts of speech
            val matches = historyDao.getAllHistoryList().filter { it.word.equals(cleanWord, ignoreCase = true) }
            if (matches.size == 1) {
                historyDao.updateManualTranslationByWordAndType(cleanWord, matches[0].type, cleanManual)
            }
        }

        var updated = if (!resolvedType.isNullOrBlank()) {
            historyDao.findByWordAndType(cleanWord, resolvedType)
        } else null

        // If not in database yet, create and insert it so the user's manual translation is permanently saved
        if (updated == null && grammarResult != null) {
            val rawGrWord = grammarResult.word.takeUnless { it.isBlank() || it.equals("null", ignoreCase = true) } ?: cleanWord
            val grWord = com.example.data.model.sanitizeHistoryWord(rawGrWord, grammarResult.rawJson, type).ifBlank { cleanWord }
            if (grWord.isNotBlank() && !grWord.equals("null", ignoreCase = true)) {
                val entity = when (grammarResult) {
                    is com.example.data.model.GrammarResult.Noun -> {
                        val declension = grammarResult.declension
                        WordHistoryEntity(
                            word = grWord,
                            type = com.example.data.model.GrammarType.NOMEN.displayName,
                            data = declension.rawJson,
                            gender = declension.gender.takeUnless { it.equals("null", ignoreCase = true) }.orEmpty(),
                            genderArticle = declension.genderArticle.takeUnless { it.equals("null", ignoreCase = true) }.orEmpty(),
                            meaningEnglish = declension.meaningEnglish.takeUnless { it.equals("null", ignoreCase = true) }.orEmpty(),
                            rawJsonResult = declension.rawJson,
                            timestamp = System.currentTimeMillis(),
                            manualTranslation = cleanManual
                        )
                    }
                    is com.example.data.model.GrammarResult.Verb -> {
                        val conjugation = grammarResult.conjugation
                        WordHistoryEntity(
                            word = grWord,
                            type = com.example.data.model.GrammarType.VERB.displayName,
                            data = conjugation.rawJson,
                            gender = conjugation.hilfsverb.takeUnless { it.equals("null", ignoreCase = true) }.orEmpty(),
                            genderArticle = conjugation.partizip2.takeUnless { it.equals("null", ignoreCase = true) }.orEmpty(),
                            meaningEnglish = conjugation.meaningEnglish.takeUnless { it.equals("null", ignoreCase = true) }.orEmpty(),
                            rawJsonResult = conjugation.rawJson,
                            timestamp = System.currentTimeMillis(),
                            manualTranslation = cleanManual
                        )
                    }
                    is com.example.data.model.GrammarResult.Sentence -> {
                        val declension = grammarResult.declension
                        WordHistoryEntity(
                            word = grWord,
                            type = com.example.data.model.GrammarType.SENTENCE.displayName,
                            data = declension.rawJson,
                            gender = "–",
                            genderArticle = "",
                            meaningEnglish = declension.meaningEnglish.takeUnless { it.equals("null", ignoreCase = true) }.orEmpty(),
                            rawJsonResult = declension.rawJson,
                            timestamp = System.currentTimeMillis(),
                            manualTranslation = cleanManual
                        )
                    }
                    is com.example.data.model.GrammarResult.NomenList -> {
                        val declension = grammarResult.declension
                        WordHistoryEntity(
                            word = grWord,
                            type = com.example.data.model.GrammarType.NOMEN_LIST.displayName,
                            data = declension.rawJson,
                            gender = if (com.example.data.model.isMultipleWordsInput(declension.word)) "Wörter" else "Wort",
                            genderArticle = "",
                            meaningEnglish = declension.meaningEnglish.takeUnless { it.equals("null", ignoreCase = true) }.orEmpty(),
                            rawJsonResult = declension.rawJson,
                            timestamp = System.currentTimeMillis(),
                            manualTranslation = cleanManual
                        )
                    }
                    is com.example.data.model.GrammarResult.Adjektiv -> {
                        val adj = grammarResult.result
                        WordHistoryEntity(
                            word = grWord,
                            type = com.example.data.model.GrammarType.ADJEKTIV.displayName,
                            data = adj.rawJson,
                            gender = "Adjektiv",
                            genderArticle = "",
                            meaningEnglish = adj.meaningEnglish.takeUnless { it.equals("null", ignoreCase = true) }.orEmpty(),
                            rawJsonResult = adj.rawJson,
                            timestamp = System.currentTimeMillis(),
                            manualTranslation = cleanManual
                        )
                    }
                }
                val id = historyDao.insert(entity)
                updated = entity.copy(id = id)
            }
        }

        if (updated != null) {
            val finalEntity = updated.copy(manualTranslation = cleanManual)
            externalHistoryStorage?.appendOrUpdateItem(finalEntity)
            notebookRepository?.appendOrUpdateItemInActiveHistory(finalEntity)
        }
    }
}
