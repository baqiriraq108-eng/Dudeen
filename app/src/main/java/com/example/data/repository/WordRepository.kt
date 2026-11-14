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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class WordRepository(
    private val historyDao: WordHistoryDao,
    private val geminiService: GeminiDeclensionService,
    private val apiKeyManager: ApiKeyManager,
    private val searchQueryDao: SearchQueryDao? = null,
    val externalHistoryStorage: ExternalHistoryStorage? = null,
    val notebookRepository: com.example.data.notebook.NotebookRepository? = null
) {
    private val historyMutex = Mutex()

    val searchHistory: Flow<List<WordHistoryEntity>> = historyDao.getAllHistory()
        .catch { emit(emptyList()) }

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

    private val memoryGrammarCache = java.util.concurrent.ConcurrentHashMap<String, com.example.data.model.GrammarResult>()

    private fun buildCacheKey(word: String, type: com.example.data.model.GrammarType, translationLang: String): String {
        return "${word.trim().lowercase()}|${type.displayName.lowercase()}|${translationLang.trim().lowercase()}"
    }

    suspend fun searchGrammar(
        word: String,
        type: com.example.data.model.GrammarType,
        translationLang: String = "en"
    ): com.example.data.model.GrammarResult = withContext(Dispatchers.IO) {
        val trimmedInput = word.trim()
        val startingNotebookId = notebookRepository?.getActiveNotebookId()
        val cacheKey = buildCacheKey("${startingNotebookId.orEmpty()}_$trimmedInput", type, translationLang)

        // 1. Check fast in-memory cache first (<1ms)
        memoryGrammarCache[cacheKey]?.let { cachedResult ->
            val existingEntity = historyMutex.withLock {
                runCatching {
                    historyDao.findByWordAndType(cachedResult.word.ifBlank { trimmedInput }, type.displayName)
                        ?: historyDao.findByWordAndType(trimmedInput, type.displayName)
                }.getOrNull()
            }
            if (existingEntity != null) {
                historyMutex.withLock {
                    if (startingNotebookId == null || notebookRepository?.getActiveNotebookId() == startingNotebookId) {
                        val refreshed = existingEntity.copy(timestamp = System.currentTimeMillis())
                        runCatching { historyDao.insert(refreshed) }
                        externalHistoryStorage?.appendOrUpdateItem(refreshed)
                        notebookRepository?.appendOrUpdateItemInActiveHistory(refreshed)
                        runCatching { recordSearchQuery(existingEntity.word, type) }
                    }
                }
                return@withContext cachedResult
            }
        }

        // 2. Check local Room history database for an existing analyzed entry matching (word, type, translationLang)
        val localExisting = historyMutex.withLock {
            runCatching { historyDao.findByWordAndType(trimmedInput, type.displayName) }.getOrNull()
        }
        if (localExisting != null && localExisting.translationLanguage.equals(translationLang, ignoreCase = true)) {
            val rawJson = localExisting.data.ifBlank { localExisting.rawJsonResult }
            if (rawJson.isNotBlank()) {
                val parsedLocal = runCatching {
                    com.example.data.model.GrammarResult.fromJson(
                        jsonStr = rawJson,
                        fallbackType = type,
                        fallbackWord = localExisting.word
                    )
                }.getOrNull()

                if (parsedLocal != null) {
                    memoryGrammarCache[cacheKey] = parsedLocal
                    memoryGrammarCache[buildCacheKey("${startingNotebookId.orEmpty()}_${localExisting.word}", type, translationLang)] = parsedLocal
                    historyMutex.withLock {
                        if (startingNotebookId == null || notebookRepository?.getActiveNotebookId() == startingNotebookId) {
                            val refreshed = localExisting.copy(timestamp = System.currentTimeMillis())
                            runCatching { historyDao.insert(refreshed) }
                            externalHistoryStorage?.appendOrUpdateItem(refreshed)
                            notebookRepository?.appendOrUpdateItemInActiveHistory(refreshed)
                            runCatching { recordSearchQuery(localExisting.word, type) }
                        }
                    }
                    return@withContext parsedLocal
                }
            }
        }

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

        // Increment daily usage on successful network lookup
        apiKeyManager.incrementDailyUsage(provider)

        val cleanResultWord = com.example.data.model.sanitizeHistoryWord(
            result.word.takeUnless { it.isBlank() || it.equals("null", ignoreCase = true) } ?: word,
            result.rawJson,
            type.displayName
        ).ifBlank { word.trim() }

        memoryGrammarCache[cacheKey] = result
        if (cleanResultWord.isNotBlank()) {
            memoryGrammarCache[buildCacheKey("${startingNotebookId.orEmpty()}_$cleanResultWord", type, translationLang)] = result
        }

        if (cleanResultWord.isBlank() || cleanResultWord.equals("null", ignoreCase = true)) {
            return@withContext result
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

        historyMutex.withLock {
            // Only persist to Room & active notebook if the active notebook did not change while network request was in flight
            if (startingNotebookId == null || notebookRepository?.getActiveNotebookId() == startingNotebookId) {
                val existing = runCatching { historyDao.findByWordAndType(cleanResultWord, type.displayName) }.getOrNull()
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

                runCatching { historyDao.deleteByWordAndType(cleanResultWord, type.displayName) }
                val insertedId = runCatching { historyDao.insert(entityToSave) }.getOrDefault(0L)
                val savedEntity = entityToSave.copy(id = insertedId)
                externalHistoryStorage?.appendOrUpdateItem(savedEntity)
                notebookRepository?.appendOrUpdateItemInActiveHistory(savedEntity)
                runCatching { recordSearchQuery(cleanResultWord, type) }
            }
        }

        return@withContext result
    }

    /**
     * Synchronizes persistent notebook history / external history with Room database.
     * Merges items intelligently so manual translations and word records are never lost.
     * Skips redundant disk/Room rewrites when Room and notebook storage are already in sync.
     */
    suspend fun syncHistoryOnStartup() = withContext(Dispatchers.IO) {
        historyMutex.withLock {
            runCatching { historyDao.deleteCorruptedNullEntries() }

            val rawRoomItems = runCatching { historyDao.getAllHistoryList() }.getOrElse { emptyList() }
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
                    withContext(Dispatchers.Default) {
                        com.example.ui.components.HistoryQuickDetailsCache.prewarm(roomItems)
                    }
                } else if (notebookItems.isNotEmpty()) {
                    val merged = mergeHistoryLists(notebookItems, roomItems)
                    if (!areHistoryListsSynced(roomItems, merged)) {
                        runCatching { historyDao.replaceAllHistory(merged) }
                    }
                    if (!areHistoryListsSynced(notebookItems, merged)) {
                        notebookRepository.saveActiveHistory(merged)
                    }
                    withContext(Dispatchers.Default) {
                        com.example.ui.components.HistoryQuickDetailsCache.prewarm(merged)
                    }
                }
                return@withLock
            }

            val storage = externalHistoryStorage ?: return@withLock
            val rawExternalItems = storage.loadHistory()
            val externalItems = rawExternalItems.mapNotNull { item ->
                val sanitized = com.example.data.model.sanitizeHistoryWord(item.word, item.data.ifBlank { item.rawJsonResult }, item.type)
                if (sanitized.isBlank() || sanitized.equals("null", ignoreCase = true)) null
                else if (sanitized != item.word) item.copy(word = sanitized)
                else item
            }

            if (externalItems.isNotEmpty() && roomItems.isEmpty()) {
                runCatching { historyDao.replaceAllHistory(externalItems) }
                withContext(Dispatchers.Default) {
                    com.example.ui.components.HistoryQuickDetailsCache.prewarm(externalItems)
                }
            } else if (roomItems.isNotEmpty() && externalItems.isEmpty()) {
                storage.saveHistory(roomItems)
                withContext(Dispatchers.Default) {
                    com.example.ui.components.HistoryQuickDetailsCache.prewarm(roomItems)
                }
            } else if (externalItems.isNotEmpty() || roomItems.isNotEmpty()) {
                val merged = mergeHistoryLists(externalItems, roomItems)
                if (!areHistoryListsSynced(roomItems, merged)) {
                    runCatching { historyDao.replaceAllHistory(merged) }
                }
                if (!areHistoryListsSynced(externalItems, merged)) {
                    storage.saveHistory(merged)
                }
                withContext(Dispatchers.Default) {
                    com.example.ui.components.HistoryQuickDetailsCache.prewarm(merged)
                }
            }
        }
    }

    private fun areHistoryListsSynced(
        first: List<WordHistoryEntity>,
        second: List<WordHistoryEntity>
    ): Boolean {
        if (first.size != second.size) return false
        for (i in first.indices) {
            val a = first[i]
            val b = second[i]
            if (!a.word.equals(b.word, ignoreCase = false) ||
                !a.type.equals(b.type, ignoreCase = false) ||
                a.timestamp != b.timestamp ||
                a.manualTranslation != b.manualTranslation ||
                a.meaningEnglish != b.meaningEnglish
            ) {
                return false
            }
        }
        return true
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
        historyMutex.withLock {
            memoryGrammarCache.clear()
            runCatching { historyDao.replaceAllHistory(items) }
        }
        withContext(Dispatchers.Default) {
            com.example.ui.components.HistoryQuickDetailsCache.prewarm(items)
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
        historyMutex.withLock {
            val entity = runCatching { historyDao.findById(id) }.getOrNull()
            if (entity != null) {
                val segment = "_${entity.word.trim().lowercase()}|${entity.type.trim().lowercase()}|"
                val prefix = "${entity.word.trim().lowercase()}|${entity.type.trim().lowercase()}|"
                memoryGrammarCache.keys.removeIf { it.startsWith(prefix) || it.contains(segment) }
            }
            runCatching { historyDao.deleteById(id) }
            externalHistoryStorage?.deleteItem(id, entity?.word, entity?.type)
            notebookRepository?.deleteItemInActiveHistory(id, entity?.word, entity?.type)
        }
    }

    suspend fun clearAllHistory() = withContext(Dispatchers.IO) {
        historyMutex.withLock {
            memoryGrammarCache.clear()
            runCatching { historyDao.clearAll() }
            externalHistoryStorage?.clearHistory()
            notebookRepository?.clearActiveHistory()
        }
    }

    suspend fun clearHistoryByType(type: com.example.data.model.GrammarType) = withContext(Dispatchers.IO) {
        historyMutex.withLock {
            val typeSegment = "|${type.displayName.lowercase()}|"
            memoryGrammarCache.keys.removeIf { it.contains(typeSegment) }
            runCatching { historyDao.clearByType(type.displayName) }
            val remaining = runCatching { historyDao.getAllHistoryList() }.getOrElse { emptyList() }
            externalHistoryStorage?.saveHistory(remaining)
            notebookRepository?.saveActiveHistory(remaining)
        }
    }

    fun hasHistoryFolder(): Boolean {
        return externalHistoryStorage?.hasFolderUri() == true
    }

    fun getHistoryFolderName(): String {
        return externalHistoryStorage?.getFolderName() ?: "Kein Ordner ausgewählt"
    }

    suspend fun onFolderSelected(uri: android.net.Uri): Int = withContext(Dispatchers.IO) {
        val storage = externalHistoryStorage ?: return@withContext 0
        storage.saveFolderUri(uri)
        // Automatically import history from history.json if it exists in the selected folder
        syncHistoryOnStartup()
        runCatching { historyDao.getAllHistoryList().size }.getOrDefault(0)
    }

    suspend fun importHistoryFromFile(fileUri: android.net.Uri, replace: Boolean): Int = withContext(Dispatchers.IO) {
        val storage = externalHistoryStorage ?: return@withContext 0
        val importedItems = storage.readHistoryFromUri(fileUri)
        if (importedItems.isEmpty()) return@withContext 0

        val updatedList = historyMutex.withLock {
            val currentNotebookItems = notebookRepository?.loadActiveHistory()
                ?: runCatching { historyDao.getAllHistoryList() }.getOrElse { emptyList() }
            val list = if (replace) {
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
            notebookRepository?.saveActiveHistory(list)

            // Reload Room with active notebook items only
            runCatching { historyDao.replaceAllHistory(list) }

            // Update external storage for active notebook if configured
            storage.saveHistory(list)
            list
        }

        withContext(Dispatchers.Default) {
            com.example.ui.components.HistoryQuickDetailsCache.prewarm(updatedList)
        }
        importedItems.size
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

        historyMutex.withLock {
            if (!resolvedType.isNullOrBlank()) {
                runCatching { historyDao.updateManualTranslationByWordAndType(cleanWord, resolvedType, cleanManual) }
            } else {
                // Strictly check if only one type exists to prevent polluting different parts of speech
                val matches = runCatching { historyDao.getAllHistoryList() }.getOrElse { emptyList() }
                    .filter { it.word.equals(cleanWord, ignoreCase = true) }
                if (matches.size == 1) {
                    runCatching { historyDao.updateManualTranslationByWordAndType(cleanWord, matches[0].type, cleanManual) }
                }
            }

            var updated = if (!resolvedType.isNullOrBlank()) {
                runCatching { historyDao.findByWordAndType(cleanWord, resolvedType) }.getOrNull()
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
                    val id = runCatching { historyDao.insert(entity) }.getOrDefault(0L)
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
}
