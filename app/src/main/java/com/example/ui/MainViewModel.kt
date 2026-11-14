package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AiModelInfo
import com.example.data.ApiKeyManager
import com.example.data.GeminiModelInfo
import com.example.data.ModelProvider
import com.example.data.local.AppDatabase
import com.example.data.local.ExternalHistoryStorage
import com.example.data.local.SearchQueryEntity
import com.example.data.local.WordHistoryEntity
import com.example.data.model.GrammarResult
import com.example.data.model.GrammarType
import com.example.data.model.VerbConjugationResult
import com.example.data.model.WordDeclensionResult
import com.example.data.notebook.Notebook
import com.example.data.notebook.NotebookRepository
import com.example.data.notebook.NotebookSettings
import com.example.data.notebook.ImportedJsonResult
import com.example.data.notebook.ImportedNotebookPackage
import com.example.data.notebook.BackupFolder
import com.example.data.notebook.NotebookBackupManager
import com.example.data.remote.GeminiDeclensionService
import com.example.data.repository.WordRepository
import com.example.ui.theme.AppThemePackage
import com.example.ui.theme.ThemePreferences
import com.example.util.HistorySortOption
import com.example.util.HistorySortField
import com.example.util.SortDirection
import com.example.util.ExportSharingManager
import com.example.util.GermanTtsManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private val VIEWMODEL_WORD_SPLIT_REGEX = Regex("[\\s,;]+")

data class JsonExportTarget(
    val fileName: String,
    val jsonContent: String,
    val title: String
)

data class ImportSingleChoice(
    val detectedName: String,
    val words: List<WordHistoryEntity>,
    val settings: NotebookSettings? = null
)

data class ImportMultiChoice(
    val packages: List<ImportedNotebookPackage>,
    val fallbackName: String
) {
    val notebooksMap: Map<String, List<WordHistoryEntity>>
        get() = packages.associate { it.name to it.words }
}

data class PostImportApiKeyPrompt(
    val notebookName: String,
    val provider: String = "GEMINI",
    val modelId: String = "gemini-2.5-flash"
)

data class MainUiState(
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val selectedGrammarType: GrammarType = GrammarType.NOMEN,
    val currentResult: WordDeclensionResult? = null,
    val currentGrammarResult: GrammarResult? = null,
    val errorMessage: String? = null,
    val isStartupSettingsLoaded: Boolean = false,
    val hasApiKey: Boolean = false,
    val isApiKeyDialogOpen: Boolean = false,
    val selectedProvider: ModelProvider = ModelProvider.GEMINI,
    val apiKeyInput: String = "", // Gemini key
    val geminiApiKeyInput: String = "",
    val openRouterApiKeyInput: String = "",
    val otherApiKeyInput: String = "",
    val selectedModel: String = ApiKeyManager.DEFAULT_MODEL,
    val availableModels: List<AiModelInfo> = emptyList(),
    val isRefreshingModels: Boolean = false,
    val refreshModelsStatus: String? = null,
    val selectedNumberTab: Int = 0, // 0: Singular, 1: Plural, 2: Both
    val selectedHistoryFilter: Int = 0, // 0: Alle, 1: Nomen, 2: Verben, 3: Sätze, 4: Wörter, 5: Adjektive
    val historySortOptions: Map<Int, HistorySortOption> = mapOf(
        0 to HistorySortOption(),
        1 to HistorySortOption(),
        2 to HistorySortOption(),
        3 to HistorySortOption(),
        4 to HistorySortOption(),
        5 to HistorySortOption()
    ),
    val isExportDialogOpen: Boolean = false,
    val exportOpenedFromSettings: Boolean = false,
    val isColorWindowOpen: Boolean = false,
    val selectedTheme: AppThemePackage = AppThemePackage.SCHIEFER,
    val isTestingKey: Boolean = false,
    val testKeyResult: Pair<Boolean, String>? = null, // Pair(isSuccess, message)
    val remainingDailyRequests: Int = 20,
    val totalDailyQuota: Int = 20,
    val selectedHistoryFolderName: String = "Kein Ordner ausgewählt",
    val replaceHistoryOnImport: Boolean = false,
    val importStatusMessage: String? = null,
    val allNotebooks: List<Notebook> = emptyList(),
    val activeNotebook: Notebook? = null,
    val themeMode: String = "auto", // "auto", "light", "dark"
    val appLanguage: String = "de", // "de", "en", "ar"
    val translationLanguage: String = "en", // "en", "ar"
    val isCreateNotebookDialogOpen: Boolean = false,
    val isManageNotebooksDialogOpen: Boolean = false,
    val backupFolders: List<BackupFolder> = emptyList(),
    val isBackingUp: Boolean = false,
    val activeJsonExportTarget: JsonExportTarget? = null,
    val isSelectNotebooksExportDialogOpen: Boolean = false,
    val pendingTreeExportNotebookIds: List<String>? = null,
    val isGrammarTablesOpen: Boolean = false,
    val grammarVerbResult: com.example.data.model.VerbConjugationResult? = null,
    val isGrammarVerbLoading: Boolean = false,
    val grammarVerbError: String? = null,
    val importSingleChoice: ImportSingleChoice? = null,
    val importMultiChoice: ImportMultiChoice? = null,
    val postImportApiKeyPrompt: PostImportApiKeyPrompt? = null,
    val currentManualTranslation: String = "",
    val searchQueriesPerType: Map<GrammarType, String> = mapOf(
        GrammarType.NOMEN to "",
        GrammarType.VERB to "",
        GrammarType.NOMEN_LIST to "",
        GrammarType.ADJEKTIV to ""
    )
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val notebookRepository: NotebookRepository = NotebookRepository(application)
    val notebookBackupManager: NotebookBackupManager = NotebookBackupManager(application, notebookRepository)
    private val database: AppDatabase = AppDatabase.getDatabase(application)
    private val apiKeyManager: ApiKeyManager = ApiKeyManager(application)
    private val geminiService: GeminiDeclensionService = GeminiDeclensionService(application)
    private val externalStorage: ExternalHistoryStorage = ExternalHistoryStorage(application)
    private val repository: WordRepository = WordRepository(
        historyDao = database.wordHistoryDao(),
        geminiService = geminiService,
        apiKeyManager = apiKeyManager,
        searchQueryDao = database.searchQueryDao(),
        externalHistoryStorage = externalStorage,
        notebookRepository = notebookRepository
    )
    private val themePreferences: ThemePreferences = ThemePreferences(application)
    private val ttsManager: GermanTtsManager = GermanTtsManager(application)

    val isSpeaking: StateFlow<Boolean> = ttsManager.isSpeaking
    val currentlySpokenText: StateFlow<String?> = ttsManager.currentlySpokenText
    val currentlySpokenId: StateFlow<String?> = ttsManager.currentlySpokenId
    val ttsSpeedRate: StateFlow<Float> = ttsManager.speechRate

    private var ttsSpeedSaveJob: Job? = null

    fun setTtsSpeed(speed: Float) {
        ttsManager.setSpeedRate(speed)
        ttsSpeedSaveJob?.cancel()
        ttsSpeedSaveJob = viewModelScope.launch(Dispatchers.IO) {
            delay(350)
            val currentSettings = _uiState.value.activeNotebook?.settings
            if (currentSettings != null && currentSettings.ttsSpeed != speed) {
                val updated = currentSettings.copy(ttsSpeed = speed)
                notebookRepository.saveActiveSettings(updated)
                _uiState.update { state ->
                    val active = state.activeNotebook
                    if (active != null) state.copy(activeNotebook = active.copy(settings = updated))
                    else state
                }
            }
        }
    }

    private val initialProvider: ModelProvider = repository.getSelectedProvider()

    private val _uiState = MutableStateFlow(
        MainUiState(
            hasApiKey = repository.isApiKeyConfigured(),
            selectedProvider = initialProvider,
            selectedModel = repository.getSelectedModel(initialProvider),
            selectedTheme = themePreferences.getSelectedTheme(),
            themeMode = themePreferences.getThemeMode(),
            appLanguage = themePreferences.getAppLanguage(),
            translationLanguage = themePreferences.getTranslationLanguage()
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val historyList: StateFlow<List<WordHistoryEntity>> = repository.searchHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )
    val recentQueries: StateFlow<List<SearchQueryEntity>> = repository.recentSearchQueries.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    init {
        // Wire up notebook backup manager for auto-mirroring
        notebookRepository.backupManager = notebookBackupManager

        viewModelScope.launch {
            notebookBackupManager.backupFolders.collect { folders ->
                _uiState.update { it.copy(backupFolders = folders) }
            }
        }

        viewModelScope.launch {
            notebookBackupManager.isBackingUp.collect { backingUp ->
                _uiState.update { it.copy(isBackingUp = backingUp) }
            }
        }

        // Pre-warm Grammar Tables in background RAM cache on app start for instant opening
        com.example.data.repository.GrammarTablesRepository.warmCache(viewModelScope)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val allNotebooks = notebookRepository.listAllNotebooks()
                val activeNotebook = notebookRepository.getActiveNotebook()
                val notebookSettings = activeNotebook.settings

                // Apply notebook settings
                val activeTheme = AppThemePackage.fromId(notebookSettings.themeColorId)
                val activeThemeMode = notebookSettings.themeMode
                ttsManager.setSpeedRate(notebookSettings.ttsSpeed)

                val provider = if (notebookSettings.selectedProvider.isNotBlank()) {
                    ModelProvider.fromId(notebookSettings.selectedProvider).also {
                        repository.saveSelectedProvider(it)
                    }
                } else {
                    repository.getSelectedProvider()
                }
                if (notebookSettings.customApiKey.isNotBlank() && repository.getUserEnteredGeminiKey().isBlank()) {
                    repository.saveGeminiApiKey(notebookSettings.customApiKey)
                }
                if (notebookSettings.openRouterApiKey.isNotBlank() && repository.getOpenRouterApiKey().isBlank()) {
                    repository.saveOpenRouterApiKey(notebookSettings.openRouterApiKey)
                }

                val isConfigured = notebookSettings.customApiKey.isNotBlank() ||
                    notebookSettings.openRouterApiKey.isNotBlank() ||
                    repository.isApiKeyConfigured()
                val currentModel = notebookSettings.selectedModel.ifBlank { repository.getSelectedModel(provider) }
                val geminiKey = notebookSettings.customApiKey.ifBlank { repository.getUserEnteredGeminiKey() }
                val openRouterKey = notebookSettings.openRouterApiKey.ifBlank { repository.getOpenRouterApiKey() }
                val (remaining, total) = repository.getDailyUsage(provider, currentModel)
                val folderName = if (notebookSettings.storageFolderUri.isNotBlank()) {
                    notebookSettings.storageFolderUri
                } else {
                    "Standard (${activeNotebook.name})"
                }

                val currentAppLanguage = themePreferences.getAppLanguage()
                val activeLanguage = if (currentAppLanguage.isNotBlank()) currentAppLanguage else notebookSettings.appLanguage.ifBlank { "de" }
                themePreferences.saveAppLanguage(activeLanguage)

                val currentTranslationLanguage = themePreferences.getTranslationLanguage()
                val activeTranslationLanguage = if (currentTranslationLanguage.isNotBlank()) currentTranslationLanguage else notebookSettings.translationLanguage.ifBlank { "en" }
                themePreferences.saveTranslationLanguage(activeTranslationLanguage)

                _uiState.update { current ->
                    current.copy(
                        allNotebooks = allNotebooks,
                        activeNotebook = activeNotebook,
                        themeMode = activeThemeMode,
                        appLanguage = activeLanguage,
                        translationLanguage = activeTranslationLanguage,
                        hasApiKey = isConfigured,
                        isStartupSettingsLoaded = true,
                        selectedProvider = provider,
                        apiKeyInput = geminiKey,
                        geminiApiKeyInput = geminiKey,
                        openRouterApiKeyInput = openRouterKey,
                        otherApiKeyInput = openRouterKey,
                        selectedModel = currentModel,
                        availableModels = repository.getAvailableModelsForProvider(provider),
                        selectedTheme = activeTheme,
                        remainingDailyRequests = remaining,
                        totalDailyQuota = total,
                        selectedHistoryFolderName = folderName
                    )
                }
            } catch (e: Throwable) {
                Log.e("MainViewModel", "Failed to load initial notebook settings", e)
            } finally {
                _uiState.update { current ->
                    if (current.isStartupSettingsLoaded) current
                    else current.copy(
                        isStartupSettingsLoaded = true,
                        hasApiKey = repository.isApiKeyConfigured()
                    )
                }
            }

            try {
                repository.syncHistoryOnStartup()
            } catch (e: Throwable) {
                Log.e("MainViewModel", "Failed to sync history on startup", e)
            }
        }
    }

    private fun refreshUsageQuota() {
        val provider = _uiState.value.selectedProvider
        val model = _uiState.value.selectedModel
        val (remaining, total) = repository.getDailyUsage(provider, model)
        _uiState.update {
            it.copy(
                remainingDailyRequests = remaining,
                totalDailyQuota = total
            )
        }
    }

    fun onOpenColorWindow() {
        _uiState.update { it.copy(isColorWindowOpen = true) }
    }

    fun onCloseColorWindow() {
        _uiState.update { it.copy(isColorWindowOpen = false) }
    }

    fun onApplyColorTheme(theme: AppThemePackage, mode: String) {
        themePreferences.saveSelectedTheme(theme)
        themePreferences.saveThemeMode(mode)
        _uiState.update {
            it.copy(
                selectedTheme = theme,
                themeMode = mode,
                isColorWindowOpen = false
            )
        }
        val currentSettings = _uiState.value.activeNotebook?.settings
        if (currentSettings != null) {
            saveActiveNotebookSettings(currentSettings.copy(themeColorId = theme.id, themeMode = mode))
        }
    }

    fun onThemeSelected(theme: AppThemePackage) {
        themePreferences.saveSelectedTheme(theme)
        _uiState.update { it.copy(selectedTheme = theme) }
        val currentSettings = _uiState.value.activeNotebook?.settings
        if (currentSettings != null) {
            saveActiveNotebookSettings(currentSettings.copy(themeColorId = theme.id))
        }
    }

    fun onThemeModeChange(mode: String) {
        themePreferences.saveThemeMode(mode)
        _uiState.update { it.copy(themeMode = mode) }
        val currentSettings = _uiState.value.activeNotebook?.settings
        if (currentSettings != null) {
            saveActiveNotebookSettings(currentSettings.copy(themeMode = mode))
        }
    }

    fun onGrammarTypeSelected(type: GrammarType) {
        _uiState.update { current ->
            if (current.selectedGrammarType != type) {
                val keyType = if (type == GrammarType.SENTENCE) GrammarType.NOMEN else type
                val savedQuery = current.searchQueriesPerType[keyType] ?: ""
                current.copy(
                    selectedGrammarType = type,
                    searchQuery = savedQuery,
                    errorMessage = null
                )
            } else {
                current
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _uiState.update { current ->
            val currentType = current.selectedGrammarType
            val keyType = if (currentType == GrammarType.SENTENCE) GrammarType.NOMEN else currentType
            val updatedQueries = current.searchQueriesPerType.toMutableMap()
            updatedQueries[keyType] = newQuery
            current.copy(
                searchQuery = newQuery,
                searchQueriesPerType = updatedQueries
            )
        }
    }

    private var activeSearchJob: Job? = null

    fun cancelSearch() {
        activeSearchJob?.cancel()
        activeSearchJob = null
        _uiState.update {
            it.copy(
                isSearching = false
            )
        }
    }

    fun onSearch(specificQuery: String? = null) {
        if (_uiState.value.isSearching) {
            cancelSearch()
            return
        }

        val targetQuery = specificQuery ?: _uiState.value.searchQuery
        val trimmed = targetQuery.trim()
        val isWörterTab = _uiState.value.selectedGrammarType == GrammarType.NOMEN_LIST

        if (trimmed.isEmpty()) {
            val emptyMsg = when (_uiState.value.selectedGrammarType) {
                GrammarType.VERB -> "Bitte geben Sie ein deutsches Verb ein (z. B. 'arbeiten')."
                GrammarType.NOMEN_LIST -> "Bitte geben Sie ein oder mehrere deutsche Wörter ein."
                GrammarType.ADJEKTIV -> "Bitte geben Sie ein deutsches Adjektiv ein (z. B. 'schön')."
                else -> "Bitte geben Sie ein deutsches Wort oder einen Satz ein (z. B. 'Tisch')."
            }
            _uiState.update { it.copy(errorMessage = emptyMsg) }
            return
        }

        val hasKey = repository.isApiKeyConfigured()
        if (!hasKey) {
            val providerName = _uiState.value.selectedProvider.displayName
            _uiState.update {
                it.copy(
                    isApiKeyDialogOpen = true,
                    errorMessage = "Bitte konfigurieren Sie Ihren API-Schlüssel für $providerName in den Einstellungen."
                )
            }
            return
        }

        activeSearchJob?.cancel()
        activeSearchJob = viewModelScope.launch {
            val transLang = _uiState.value.translationLanguage
            val selectedTypeAtStart = _uiState.value.selectedGrammarType
            _uiState.update {
                it.copy(
                    isSearching = true,
                    errorMessage = null,
                    searchQuery = trimmed
                )
            }

            try {
                if (isWörterTab) {
                    // Wörter field is dedicated to word(s) translation - NEVER treated as sentence
                    val listResult = repository.searchGrammar(trimmed, GrammarType.NOMEN_LIST, transLang)
                    refreshUsageQuota()
                    val declension = when (listResult) {
                        is GrammarResult.NomenList -> listResult.declension
                        is GrammarResult.Noun -> listResult.declension
                        is GrammarResult.Sentence -> listResult.declension
                        is GrammarResult.Verb -> throw IllegalStateException("Unerwartetes Ergebnis")
                        is GrammarResult.Adjektiv -> throw IllegalStateException("Unerwartetes Ergebnis")
                    }
                    val initialManual = findExistingManualTranslation(trimmed, GrammarType.NOMEN_LIST.displayName)
                    _uiState.update {
                        it.copy(
                            isSearching = false,
                            currentResult = declension,
                            currentGrammarResult = GrammarResult.NomenList(declension),
                            currentManualTranslation = initialManual,
                            errorMessage = null
                        )
                    }
                } else if (selectedTypeAtStart == GrammarType.ADJEKTIV) {
                    val adjResult = repository.searchAdjektiv(trimmed, transLang)
                    refreshUsageQuota()
                    val initialManual = findExistingManualTranslation(trimmed, GrammarType.ADJEKTIV.displayName)
                    _uiState.update {
                        it.copy(
                            isSearching = false,
                            currentGrammarResult = GrammarResult.Adjektiv(adjResult),
                            currentResult = null,
                            currentManualTranslation = initialManual,
                            errorMessage = null
                        )
                    }
                } else if (selectedTypeAtStart == GrammarType.VERB) {
                    val verbResult = repository.searchVerb(trimmed, transLang)
                    refreshUsageQuota()
                    val initialManual = findExistingManualTranslation(trimmed, GrammarType.VERB.displayName)
                    _uiState.update {
                        it.copy(
                            isSearching = false,
                            currentGrammarResult = GrammarResult.Verb(verbResult),
                            currentResult = null,
                            currentManualTranslation = initialManual,
                            errorMessage = null
                        )
                    }
                } else {
                    // Nomen & Sätze: 2 or more words = Sentence, 1 word = Noun (supporting spaces, commas, etc.)
                    val words = trimmed.split(VIEWMODEL_WORD_SPLIT_REGEX).filter { it.isNotBlank() }
                    val isSentence = words.size >= 2
                    if (isSentence) {
                        val sentenceResult = repository.searchGrammar(trimmed, GrammarType.SENTENCE, transLang)
                        refreshUsageQuota()
                        val declension = when (sentenceResult) {
                            is GrammarResult.Sentence -> sentenceResult.declension
                            is GrammarResult.Noun -> sentenceResult.declension
                            is GrammarResult.NomenList -> sentenceResult.declension
                            is GrammarResult.Verb -> throw IllegalStateException("Unerwartetes Ergebnis")
                            is GrammarResult.Adjektiv -> throw IllegalStateException("Unerwartetes Ergebnis")
                        }
                        val initialManual = findExistingManualTranslation(trimmed, GrammarType.SENTENCE.displayName)
                        _uiState.update {
                            it.copy(
                                isSearching = false,
                                currentResult = declension,
                                currentGrammarResult = GrammarResult.Sentence(declension),
                                currentManualTranslation = initialManual,
                                errorMessage = null
                            )
                        }
                    } else {
                        val declensionResult = repository.searchWord(trimmed, transLang)
                        refreshUsageQuota()
                        val initialManual = findExistingManualTranslation(trimmed, GrammarType.NOMEN.displayName)
                        _uiState.update {
                            it.copy(
                                isSearching = false,
                                currentResult = declensionResult,
                                currentGrammarResult = GrammarResult.Noun(declensionResult),
                                currentManualTranslation = initialManual,
                                errorMessage = null
                            )
                        }
                    }
                }
            } catch (e: CancellationException) {
                _uiState.update {
                    it.copy(
                        isSearching = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSearching = false,
                        errorMessage = e.message ?: "Fehler beim Abrufen der Grammatikdaten."
                    )
                }
            } finally {
                if (activeSearchJob == coroutineContext[Job]) {
                    activeSearchJob = null
                }
            }
        }
    }

    fun onSelectHistoryItem(entity: WordHistoryEntity) {
        viewModelScope.launch {
            try {
                val isAdjektiv = com.example.util.HistoryFilterHelper.isAdjektivItem(entity)
                val isVerb = com.example.util.HistoryFilterHelper.isVerbItem(entity)
                val isNomenList = com.example.util.HistoryFilterHelper.isNomenListItem(entity)
                val isSentence = com.example.util.HistoryFilterHelper.isSentenceItem(entity)

                if (isAdjektiv) {
                    val cachedAdjektiv = withContext(Dispatchers.Default) {
                        com.example.data.model.AdjektivResult.fromJson(entity.rawJsonResult, fallbackWord = entity.word)
                    }
                    _uiState.update {
                        it.copy(
                            selectedGrammarType = GrammarType.ADJEKTIV,
                            currentGrammarResult = GrammarResult.Adjektiv(cachedAdjektiv),
                            currentResult = null,
                            currentManualTranslation = entity.manualTranslation,
                            searchQuery = entity.word,
                            errorMessage = null
                        )
                    }
                } else if (isVerb) {
                    val cachedVerb = withContext(Dispatchers.Default) {
                        VerbConjugationResult.fromJson(entity.rawJsonResult, fallbackWord = entity.word)
                    }
                    _uiState.update {
                        it.copy(
                            selectedGrammarType = GrammarType.VERB,
                            currentGrammarResult = GrammarResult.Verb(cachedVerb),
                            currentResult = null,
                            currentManualTranslation = entity.manualTranslation,
                            searchQuery = entity.word,
                            errorMessage = null
                        )
                    }
                } else if (isNomenList) {
                    val cachedResult = withContext(Dispatchers.Default) {
                        WordDeclensionResult.fromJson(entity.rawJsonResult, fallbackWord = entity.word)
                    }
                    _uiState.update {
                        it.copy(
                            selectedGrammarType = GrammarType.NOMEN_LIST,
                            currentResult = cachedResult,
                            currentGrammarResult = GrammarResult.NomenList(cachedResult),
                            currentManualTranslation = entity.manualTranslation,
                            searchQuery = entity.word,
                            errorMessage = null
                        )
                    }
                } else if (isSentence) {
                    val cachedResult = withContext(Dispatchers.Default) {
                        WordDeclensionResult.fromJson(entity.rawJsonResult, fallbackWord = entity.word)
                    }
                    _uiState.update {
                        it.copy(
                            selectedGrammarType = GrammarType.NOMEN,
                            currentResult = cachedResult,
                            currentGrammarResult = GrammarResult.Sentence(cachedResult),
                            currentManualTranslation = entity.manualTranslation,
                            searchQuery = entity.word,
                            errorMessage = null
                        )
                    }
                } else {
                    val cachedResult = withContext(Dispatchers.Default) {
                        WordDeclensionResult.fromJson(entity.rawJsonResult, fallbackWord = entity.word)
                    }
                    _uiState.update {
                        it.copy(
                            selectedGrammarType = GrammarType.NOMEN,
                            currentResult = cachedResult,
                            currentGrammarResult = GrammarResult.Noun(cachedResult),
                            currentManualTranslation = entity.manualTranslation,
                            searchQuery = entity.word,
                            errorMessage = null
                        )
                    }
                }
            } catch (e: Exception) {
                onSearch(entity.word)
            }
        }
    }

    fun onDeleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteHistoryItem(id)
        }
    }

    fun onClearHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
        }
    }

    suspend fun syncExternalHistory() {
        repository.syncHistoryOnStartup()
    }

    fun getWordRepository(): WordRepository = repository

    fun onOpenApiKeyDialog() {
        val provider = repository.getSelectedProvider()
        val currentModel = repository.getSelectedModel(provider)
        val geminiKey = repository.getUserEnteredGeminiKey()
        val openRouterKey = repository.getOpenRouterApiKey()
        val (remaining, total) = repository.getDailyUsage(provider, currentModel)

        val models = repository.getAvailableModelsForProvider(provider)
        val currentLanguage = themePreferences.getAppLanguage()

        _uiState.value = _uiState.value.copy(
            isApiKeyDialogOpen = true,
            appLanguage = currentLanguage,
            selectedProvider = provider,
            apiKeyInput = geminiKey,
            geminiApiKeyInput = geminiKey,
            openRouterApiKeyInput = openRouterKey,
            otherApiKeyInput = openRouterKey,
            selectedModel = currentModel,
            availableModels = models,
            testKeyResult = null,
            isTestingKey = false,
            refreshModelsStatus = null,
            remainingDailyRequests = remaining,
            totalDailyQuota = total
        )

        if (provider == ModelProvider.GEMINI && models.isEmpty() && repository.getGeminiApiKey().isNotBlank()) {
            onRefreshGeminiModels()
        }
    }

    fun onCloseApiKeyDialog() {
        _uiState.value = _uiState.value.copy(
            isApiKeyDialogOpen = false,
            exportOpenedFromSettings = false,
            testKeyResult = null,
            isTestingKey = false,
            refreshModelsStatus = null
        )
    }

    fun onSelectProvider(provider: ModelProvider) {
        repository.saveSelectedProvider(provider)
        val modelForProvider = repository.getSelectedModel(provider)
        val models = repository.getAvailableModelsForProvider(provider)
        val (remaining, total) = repository.getDailyUsage(provider, modelForProvider)

        _uiState.value = _uiState.value.copy(
            selectedProvider = provider,
            selectedModel = modelForProvider,
            availableModels = models,
            testKeyResult = null,
            refreshModelsStatus = null,
            remainingDailyRequests = remaining,
            totalDailyQuota = total,
            hasApiKey = repository.isApiKeyConfigured()
        )

        // If switching to OpenRouter and models list is empty or minimal, initiate a background fetch
        if (provider == ModelProvider.OPENROUTER && models.size <= 2) {
            onRefreshOpenRouterModels()
        } else if (provider == ModelProvider.GEMINI && models.isEmpty() && repository.getGeminiApiKey().isNotBlank()) {
            onRefreshGeminiModels()
        }
    }

    fun onSelectModel(modelId: String) {
        val provider = _uiState.value.selectedProvider
        repository.saveSelectedModel(modelId, provider)
        val (remaining, total) = repository.getDailyUsage(provider, modelId)
        _uiState.value = _uiState.value.copy(
            selectedModel = modelId,
            remainingDailyRequests = remaining,
            totalDailyQuota = total
        )
    }

    fun onGeminiApiKeyInputChange(input: String) {
        _uiState.value = _uiState.value.copy(
            apiKeyInput = input,
            geminiApiKeyInput = input,
            testKeyResult = null
        )
    }

    fun onOpenRouterApiKeyInputChange(input: String) {
        _uiState.value = _uiState.value.copy(
            openRouterApiKeyInput = input,
            otherApiKeyInput = input,
            testKeyResult = null
        )
    }

    fun onOtherApiKeyInputChange(input: String) {
        onOpenRouterApiKeyInputChange(input)
    }

    fun onApiKeyInputChange(input: String) {
        onGeminiApiKeyInputChange(input)
    }

    /**
     * Dynamically fetches OpenRouter models and updates the available list
     */
    fun onRefreshOpenRouterModels() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRefreshingModels = true,
                refreshModelsStatus = null
            )
            val key = _uiState.value.openRouterApiKeyInput.ifBlank { repository.getOpenRouterApiKey() }
            val result = repository.refreshOpenRouterModels(key)
            if (result.isSuccess) {
                val models = result.getOrNull().orEmpty()
                val currentModel = _uiState.value.selectedModel
                val updatedModel = if (models.any { it.id == currentModel }) currentModel else models.firstOrNull()?.id ?: "openrouter/free"
                _uiState.value = _uiState.value.copy(
                    isRefreshingModels = false,
                    availableModels = models,
                    selectedModel = updatedModel,
                    refreshModelsStatus = "✓ ${models.size} Modelle geladen"
                )
            } else {
                val cached = repository.getAvailableModelsForProvider(ModelProvider.OPENROUTER)
                _uiState.value = _uiState.value.copy(
                    isRefreshingModels = false,
                    availableModels = cached,
                    refreshModelsStatus = "Offline: Lokale Modelle geladen"
                )
            }
        }
    }

    /**
     * Dynamically fetches Gemini models and updates the available list
     */
    fun onRefreshGeminiModels() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRefreshingModels = true,
                refreshModelsStatus = null
            )
            val key = _uiState.value.geminiApiKeyInput.ifBlank { repository.getGeminiApiKey() }
            val result = repository.refreshGeminiModels(key)
            if (result.isSuccess) {
                val models = result.getOrNull().orEmpty()
                val currentModel = _uiState.value.selectedModel
                val updatedModel = if (models.any { it.id == currentModel }) {
                    currentModel
                } else {
                    models.firstOrNull()?.id ?: ""
                }
                if (updatedModel.isNotBlank() && updatedModel != currentModel) {
                    repository.saveSelectedModel(updatedModel, ModelProvider.GEMINI)
                }
                _uiState.value = _uiState.value.copy(
                    isRefreshingModels = false,
                    availableModels = models,
                    selectedModel = updatedModel,
                    refreshModelsStatus = "✓ ${models.size} Modelle geladen"
                )
            } else {
                val cached = repository.getAvailableModelsForProvider(ModelProvider.GEMINI)
                _uiState.value = _uiState.value.copy(
                    isRefreshingModels = false,
                    availableModels = cached,
                    refreshModelsStatus = if (cached.isNotEmpty()) "Offline: Lokale Modelle geladen" else (result.exceptionOrNull()?.message ?: "Fehler beim Laden der Modelle")
                )
            }
        }
    }

    /**
     * Tests the API key of the selected provider with an actual network ping
     */
    fun onTestApiKey() {
        val provider = _uiState.value.selectedProvider
        val keyToTest = when (provider) {
            ModelProvider.GEMINI -> _uiState.value.geminiApiKeyInput.trim().ifEmpty { repository.getGeminiApiKey() }
            ModelProvider.OPENROUTER -> _uiState.value.openRouterApiKeyInput.trim().ifEmpty { repository.getOpenRouterApiKey() }
        }

        if (keyToTest.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                testKeyResult = Pair(false, "Bitte ${provider.displayName}-Key eingeben")
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTestingKey = true, testKeyResult = null)
            val result = repository.testApiKey(provider, keyToTest, _uiState.value.selectedModel)
            val isSuccess = result.isSuccess
            val message = if (isSuccess) {
                result.getOrNull() ?: "✓ Schlüssel gültig"
            } else {
                result.exceptionOrNull()?.message ?: "Verbindung fehlgeschlagen"
            }
            _uiState.value = _uiState.value.copy(
                isTestingKey = false,
                testKeyResult = Pair(isSuccess, message)
            )
            if (isSuccess) {
                when (provider) {
                    ModelProvider.GEMINI -> onRefreshGeminiModels()
                    ModelProvider.OPENROUTER -> onRefreshOpenRouterModels()
                }
            }
        }
    }

    fun onSaveApiKey() {
        val geminiKey = _uiState.value.geminiApiKeyInput.trim()
        val openRouterKey = _uiState.value.openRouterApiKeyInput.trim()

        if (geminiKey.isNotEmpty()) {
            repository.saveGeminiApiKey(geminiKey)
        } else {
            repository.clearGeminiApiKey()
        }

        if (openRouterKey.isNotEmpty()) {
            repository.saveOpenRouterApiKey(openRouterKey)
        } else {
            repository.clearOpenRouterApiKey()
        }

        val isConfigured = repository.isApiKeyConfigured()
        _uiState.value = _uiState.value.copy(
            hasApiKey = isConfigured,
            isApiKeyDialogOpen = false,
            errorMessage = if (isConfigured) null else _uiState.value.errorMessage
        )

        val currentSettings = _uiState.value.activeNotebook?.settings
        if (currentSettings != null) {
            saveActiveNotebookSettings(
                currentSettings.copy(
                    customApiKey = geminiKey,
                    openRouterApiKey = openRouterKey,
                    selectedProvider = _uiState.value.selectedProvider.name,
                    selectedModel = _uiState.value.selectedModel
                )
            )
        }

        if (geminiKey.isNotEmpty() && repository.getSelectedProvider() == ModelProvider.GEMINI) {
            onRefreshGeminiModels()
        }
    }

    fun onClearApiKey() {
        val provider = _uiState.value.selectedProvider
        when (provider) {
            ModelProvider.GEMINI -> {
                repository.clearGeminiApiKey()
                _uiState.value = _uiState.value.copy(
                    geminiApiKeyInput = "",
                    apiKeyInput = ""
                )
            }
            ModelProvider.OPENROUTER -> {
                repository.clearOpenRouterApiKey()
                _uiState.value = _uiState.value.copy(
                    openRouterApiKeyInput = "",
                    otherApiKeyInput = ""
                )
            }
        }
        _uiState.value = _uiState.value.copy(
            hasApiKey = repository.isApiKeyConfigured(),
            testKeyResult = null
        )
    }

    fun onSelectTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedNumberTab = tabIndex) }
    }

    fun onSelectHistoryFilter(filterIndex: Int) {
        _uiState.update { it.copy(selectedHistoryFilter = filterIndex) }
    }

    fun onUpdateHistorySort(filterIndex: Int, sortOption: HistorySortOption) {
        _uiState.update { current ->
            val currentMap = current.historySortOptions.toMutableMap()
            currentMap[filterIndex] = sortOption
            current.copy(historySortOptions = currentMap)
        }
    }

    fun onOpenExportDialog(fromSettings: Boolean = false) {
        _uiState.update {
            it.copy(
                isExportDialogOpen = true,
                exportOpenedFromSettings = fromSettings
            )
        }
    }

    fun onCloseExportDialog() {
        _uiState.update { current ->
            val fromSettings = current.exportOpenedFromSettings
            current.copy(
                isExportDialogOpen = false,
                exportOpenedFromSettings = false,
                isApiKeyDialogOpen = fromSettings || current.isApiKeyDialogOpen
            )
        }
    }

    fun onDismissExportDialog() {
        onCloseExportDialog()
    }

    fun onOpenGrammarTables() {
        _uiState.update { it.copy(isGrammarTablesOpen = true) }
    }

    fun onCloseGrammarTables() {
        _uiState.update {
            it.copy(
                isGrammarTablesOpen = false,
                grammarVerbResult = null,
                isGrammarVerbLoading = false,
                grammarVerbError = null
            )
        }
    }

    fun searchGrammarVerb(verb: String) {
        val trimmed = verb.trim().lowercase()
        if (trimmed.isBlank()) {
            clearGrammarVerb()
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isGrammarVerbLoading = true,
                grammarVerbError = null
            )

            // First check built-in dictionary for instant offline response
            val localMatch = com.example.data.VerbFallbackDictionary.lookup(trimmed)
            if (localMatch != null && !repository.isApiKeyConfigured()) {
                _uiState.value = _uiState.value.copy(
                    grammarVerbResult = localMatch,
                    isGrammarVerbLoading = false,
                    grammarVerbError = null
                )
                return@launch
            }

            try {
                val transLang = _uiState.value.appLanguage.ifBlank { "en" }
                val result = repository.searchVerb(trimmed, transLang)
                _uiState.value = _uiState.value.copy(
                    grammarVerbResult = result,
                    isGrammarVerbLoading = false,
                    grammarVerbError = null
                )
            } catch (e: Exception) {
                // If API failed or quota reached, fallback to local dictionary if available
                if (localMatch != null) {
                    _uiState.value = _uiState.value.copy(
                        grammarVerbResult = localMatch,
                        isGrammarVerbLoading = false,
                        grammarVerbError = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        grammarVerbResult = null,
                        isGrammarVerbLoading = false,
                        grammarVerbError = e.message ?: "Verb '$verb' konnte nicht geladen werden."
                    )
                }
            }
        }
    }

    fun clearGrammarVerb() {
        _uiState.value = _uiState.value.copy(
            grammarVerbResult = null,
            isGrammarVerbLoading = false,
            grammarVerbError = null
        )
    }

    /**
     * Handles the Back action in accordance with the application's strict back-stack rules:
     * 1. If Grammar Tables Sheet is open:
     *    - Closes Grammar Tables.
     * 2. If Print/Export dialog is open:
     *    - Closes Export dialog. If opened from Settings, returns to Settings. Otherwise returns to previous view.
     * 3. If Settings (ApiKeyDialog) is open:
     *    - Closes Settings dialog and returns to the previous screen (Word Detail View or Main Screen).
     * 4. If Word Detail View is active (currentGrammarResult != null || currentResult != null):
     *    - Clears the active word result and returns to the Main Screen.
     * 5. If a History sub-tab is active (selectedHistoryFilter != 0, e.g. Nomen or Verben filter):
     *    - Returns to the base "Alle" (all history) tab on the Main Screen.
     * 6. If on the Main Screen (root of the app, no sub-screens or dialogs active):
     *    - Returns false so the caller can move the task to the back (exit immediately to home screen).
     *
     * Returns true if the back event was consumed/handled within the app.
     */
    fun handleBackPress(): Boolean {
        val state = _uiState.value
        return when {
            state.isGrammarTablesOpen -> {
                onCloseGrammarTables()
                true
            }
            state.isCreateNotebookDialogOpen -> {
                closeCreateNotebookDialog()
                true
            }
            state.isManageNotebooksDialogOpen -> {
                closeManageNotebooksDialog()
                true
            }
            state.isExportDialogOpen -> {
                onCloseExportDialog()
                true
            }
            state.isColorWindowOpen -> {
                onCloseColorWindow()
                true
            }
            state.isApiKeyDialogOpen -> {
                onCloseApiKeyDialog()
                true
            }
            state.currentGrammarResult != null || state.currentResult != null -> {
                onClearResult()
                true
            }
            state.selectedHistoryFilter != 0 -> {
                onSelectHistoryFilter(0)
                true
            }
            else -> {
                false
            }
        }
    }

    private fun combineTranslations(aiTranslation: String, manualTranslation: String): String {
        val cleanAi = aiTranslation.trim()
        val items = com.example.util.parseManualTranslations(manualTranslation)
        if (items.isEmpty()) return cleanAi
        val manualBlock = if (items.size == 1) {
            "Alt.: ${items[0]}"
        } else {
            "Alt.: " + items.joinToString("\n      ")
        }
        return if (cleanAi.isNotBlank()) {
            "$cleanAi\n$manualBlock"
        } else {
            manualBlock
        }
    }

    /**
     * Retrieves all history items deduplicated by base word, ordered chronologically
     * by the earliest search timestamp (oldest first).
     */
    fun getHistoryForExport(): List<WordDeclensionResult> {
        val history = historyList.value
        if (history.isEmpty()) {
            return _uiState.value.currentResult?.let { listOf(it) } ?: emptyList()
        }

        val grouped = history.groupBy { it.word.trim().lowercase(java.util.Locale.GERMAN) }
        val earliestEntries = grouped.values.mapNotNull { list ->
            list.minByOrNull { it.timestamp }
        }.sortedBy { it.timestamp }

        val results = earliestEntries.mapNotNull { entity ->
            try {
                val parsed = WordDeclensionResult.fromJson(entity.rawJsonResult, fallbackWord = entity.word)
                val aiTranslation = entity.meaningEnglish.ifBlank { parsed.meaningEnglish }
                val translation = combineTranslations(aiTranslation, entity.manualTranslation)
                parsed.copy(meaningEnglish = translation)
            } catch (e: Exception) {
                null
            }
        }

        val current = _uiState.value.currentResult
        if (current != null && results.none { it.word.equals(current.word, ignoreCase = true) }) {
            return results + current
        }

        return results
    }

    fun getHistoryAsDeclensionResults(): List<WordDeclensionResult> {
        return getHistoryForExport()
    }

    data class UnifiedExportData(
        val nouns: List<WordDeclensionResult>,
        val verbs: List<VerbConjugationResult>,
        val adjectives: List<com.example.data.model.AdjektivResult>
    )

    /**
     * Retrieves deduplicated lists of Nouns, Verbs, and Adjectives for unified export (PDF / Excel).
     */
    fun getUnifiedHistoryForExport(): UnifiedExportData {
        val history = historyList.value.sortedBy { it.timestamp }
        val nouns = mutableListOf<WordDeclensionResult>()
        val verbs = mutableListOf<VerbConjugationResult>()
        val adjectives = mutableListOf<com.example.data.model.AdjektivResult>()

        val seenNouns = mutableSetOf<String>()
        val seenVerbs = mutableSetOf<String>()
        val seenAdjectives = mutableSetOf<String>()

        history.forEach { entity ->
            val isAdjektiv = entity.type.equals("Adjektiv", ignoreCase = true) ||
                    entity.gender.equals("Adjektiv", ignoreCase = true)
            val isVerb = entity.type.equals("Verb", ignoreCase = true)
            val key = entity.word.trim().lowercase(java.util.Locale.GERMAN)
            if (isAdjektiv) {
                if (seenAdjectives.add(key)) {
                    try {
                        val parsed = com.example.data.model.AdjektivResult.fromJson(entity.rawJsonResult, fallbackWord = entity.word)
                        val translation = combineTranslations(parsed.meaningEnglish, entity.manualTranslation)
                        adjectives.add(parsed.copy(meaningEnglish = translation))
                    } catch (_: Exception) {}
                }
            } else if (isVerb) {
                if (seenVerbs.add(key)) {
                    try {
                        val parsed = VerbConjugationResult.fromJson(entity.rawJsonResult, fallbackWord = entity.word)
                        val translation = combineTranslations(parsed.meaningEnglish, entity.manualTranslation)
                        verbs.add(parsed.copy(meaningEnglish = translation))
                    } catch (_: Exception) {}
                }
            } else {
                if (seenNouns.add(key)) {
                    try {
                        val parsed = WordDeclensionResult.fromJson(entity.rawJsonResult, fallbackWord = entity.word)
                        val isWordsItem = entity.type.equals("Wörter", ignoreCase = true) ||
                                entity.type.equals("NomenList", ignoreCase = true) ||
                                entity.type.equals("Nomen (Liste)", ignoreCase = true) ||
                                entity.gender.equals("Wort", ignoreCase = true) ||
                                entity.gender.equals("Wörter", ignoreCase = true) ||
                                entity.gender.equals("Woerter", ignoreCase = true) ||
                                parsed.gender.equals("Wort", ignoreCase = true) ||
                                parsed.gender.equals("Wörter", ignoreCase = true) ||
                                parsed.gender.equals("Woerter", ignoreCase = true) ||
                                parsed.rawJson.contains("\"Wörter\"") ||
                                parsed.rawJson.contains("\"NomenList\"")

                        val effectiveGender = if (isWordsItem) {
                            if (com.example.data.model.isMultipleWordsInput(entity.word)) "Wörter" else "Wort"
                        } else if (entity.type.equals("Satz", ignoreCase = true) ||
                            entity.type.equals("Sentence", ignoreCase = true) ||
                            entity.gender.equals("Satz", ignoreCase = true) ||
                            entity.gender == "–" ||
                            parsed.gender.equals("Satz", ignoreCase = true) ||
                            parsed.gender == "–"
                        ) {
                            "Satz"
                        } else {
                            entity.gender.ifBlank { parsed.gender }
                        }

                        val aiTranslation = entity.meaningEnglish.ifBlank { parsed.meaningEnglish }
                        val translation = combineTranslations(aiTranslation, entity.manualTranslation)
                        nouns.add(
                            parsed.copy(
                                gender = effectiveGender,
                                meaningEnglish = translation
                            )
                        )
                    } catch (_: Exception) {}
                }
            }
        }

        // Also include currently displayed result if not in list
        when (val curr = _uiState.value.currentGrammarResult) {
            is GrammarResult.Noun -> {
                val k = curr.declension.word.trim().lowercase(java.util.Locale.GERMAN)
                if (seenNouns.add(k)) {
                    val currentTrans = combineTranslations(curr.declension.meaningEnglish, _uiState.value.currentManualTranslation)
                    nouns.add(curr.declension.copy(meaningEnglish = currentTrans))
                }
            }
            is GrammarResult.Sentence -> {
                val k = curr.declension.word.trim().lowercase(java.util.Locale.GERMAN)
                if (seenNouns.add(k)) {
                    val currentTrans = combineTranslations(curr.declension.meaningEnglish, _uiState.value.currentManualTranslation)
                    nouns.add(curr.declension.copy(meaningEnglish = currentTrans))
                }
            }
            is GrammarResult.NomenList -> {
                val k = curr.declension.word.trim().lowercase(java.util.Locale.GERMAN)
                if (seenNouns.add(k)) {
                    val g = if (com.example.data.model.isMultipleWordsInput(curr.declension.word)) "Wörter" else "Wort"
                    val currentTrans = combineTranslations(curr.declension.meaningEnglish, _uiState.value.currentManualTranslation)
                    nouns.add(curr.declension.copy(gender = g, meaningEnglish = currentTrans))
                }
            }
            is GrammarResult.Verb -> {
                val k = curr.conjugation.infinitiv.trim().lowercase(java.util.Locale.GERMAN)
                if (seenVerbs.add(k)) {
                    val currentTrans = combineTranslations(curr.conjugation.meaningEnglish, _uiState.value.currentManualTranslation)
                    verbs.add(curr.conjugation.copy(meaningEnglish = currentTrans))
                }
            }
            is GrammarResult.Adjektiv -> {
                val k = curr.result.word.trim().lowercase(java.util.Locale.GERMAN)
                if (seenAdjectives.add(k)) {
                    val currentTrans = combineTranslations(curr.result.meaningEnglish, _uiState.value.currentManualTranslation)
                    adjectives.add(curr.result.copy(meaningEnglish = currentTrans))
                }
            }
            null -> {
                _uiState.value.currentResult?.let { res ->
                    val k = res.word.trim().lowercase(java.util.Locale.GERMAN)
                    if (seenNouns.add(k)) {
                        val isWordsItem = _uiState.value.selectedGrammarType == GrammarType.NOMEN_LIST ||
                                res.gender.equals("Wort", ignoreCase = true) ||
                                res.gender.equals("Wörter", ignoreCase = true) ||
                                res.rawJson.contains("\"Wörter\"") ||
                                res.rawJson.contains("\"NomenList\"")
                        val g = if (isWordsItem) {
                            if (com.example.data.model.isMultipleWordsInput(res.word)) "Wörter" else "Wort"
                        } else {
                            res.gender
                        }
                        val currentTrans = combineTranslations(res.meaningEnglish, _uiState.value.currentManualTranslation)
                        nouns.add(res.copy(gender = g, meaningEnglish = currentTrans))
                    }
                }
            }
        }

        return UnifiedExportData(nouns, verbs, adjectives)
    }

    fun onClearResult() {
        _uiState.update {
            it.copy(
                currentResult = null,
                currentGrammarResult = null,
                currentManualTranslation = "",
                searchQuery = "",
                errorMessage = null
            )
        }
    }

    private suspend fun findExistingManualTranslation(word: String, type: String? = null): String {
        return withContext(Dispatchers.IO) {
            val fromRoom = if (!type.isNullOrBlank()) {
                database.wordHistoryDao().findByWordAndType(word, type)?.manualTranslation
            } else null
            if (!fromRoom.isNullOrBlank()) return@withContext fromRoom

            val fromRoomByWord = database.wordHistoryDao().findByWord(word)?.manualTranslation
            if (!fromRoomByWord.isNullOrBlank()) return@withContext fromRoomByWord

            val fromNotebook = notebookRepository.loadActiveHistory()
                .find { it.word.equals(word, ignoreCase = true) && (type == null || it.type.equals(type, ignoreCase = true)) }
                ?.manualTranslation
            if (!fromNotebook.isNullOrBlank()) return@withContext fromNotebook

            val fromExternal = externalStorage.loadHistory()
                .find { it.word.equals(word, ignoreCase = true) && (type == null || it.type.equals(type, ignoreCase = true)) }
                ?.manualTranslation
            fromExternal.orEmpty()
        }
    }

    fun saveManualTranslation(manualTranslation: String, targetType: String? = null, targetWord: String? = null) {
        val currResult = _uiState.value.currentGrammarResult
        val (word, type) = if (!targetWord.isNullOrBlank() && !targetType.isNullOrBlank()) {
            Pair(targetWord, targetType)
        } else {
            when (currResult) {
                is GrammarResult.Noun -> Pair(currResult.declension.word, GrammarType.NOMEN.displayName)
                is GrammarResult.Verb -> Pair(currResult.conjugation.word, GrammarType.VERB.displayName)
                is GrammarResult.Adjektiv -> Pair(currResult.result.word, GrammarType.ADJEKTIV.displayName)
                is GrammarResult.Sentence -> Pair(currResult.declension.word, GrammarType.SENTENCE.displayName)
                is GrammarResult.NomenList -> Pair(currResult.declension.word, GrammarType.NOMEN_LIST.displayName)
                null -> Pair(_uiState.value.currentResult?.word ?: return, _uiState.value.selectedGrammarType.displayName)
            }
        }

        val clean = manualTranslation.trim()
        if (targetWord == null || targetWord.equals(word, ignoreCase = true)) {
            _uiState.value = _uiState.value.copy(currentManualTranslation = clean)
        }
        viewModelScope.launch {
            repository.updateManualTranslation(word, clean, type, currResult)
        }
    }

    fun setGrammarResultForTest(result: GrammarResult?) {
        _uiState.value = _uiState.value.copy(
            currentGrammarResult = result,
            currentResult = (result as? GrammarResult.Noun)?.declension
        )
    }

    fun onDismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Pronounces the given phrase or word in [languageCode] ("de", "en", or "ar") using TextToSpeech.
     * Ensures only the specific [speakerId] icon is active, preventing any
     * other icons with identical or similar words/verbs from triggering or animating.
     * Toggles play/stop if the exact same [speakerId] is already speaking.
     */
    fun speakText(text: String, speakerId: String? = null, languageCode: String = "de") {
        val clean = text.trim()
        if (clean.isBlank()) return
        val targetId = speakerId ?: clean
        val currentId = ttsManager.currentlySpokenId.value
        if (ttsManager.isSpeaking.value && currentId == targetId) {
            ttsManager.stop()
        } else {
            ttsManager.speak(clean, targetId, languageCode)
        }
    }

    fun speakGerman(text: String, speakerId: String? = null) {
        speakText(text, speakerId, "de")
    }

    fun stopSpeaking() {
        ttsManager.stop()
    }

    fun onSelectHistoryFolder(uri: android.net.Uri) {
        viewModelScope.launch {
            repository.onFolderSelected(uri)
            val newName = repository.getHistoryFolderName()
            _uiState.value = _uiState.value.copy(
                selectedHistoryFolderName = newName,
                importStatusMessage = "Speicherort festgelegt: $newName"
            )
        }
    }

    fun setReplaceHistoryOnImport(replace: Boolean) {
        _uiState.value = _uiState.value.copy(replaceHistoryOnImport = replace)
    }

    fun onImportHistoryFile(fileUri: android.net.Uri) {
        viewModelScope.launch {
            val replace = _uiState.value.replaceHistoryOnImport
            val count = repository.importHistoryFromFile(fileUri, replace)
            val modeText = if (replace) "Ersetzt" else "Zusammengeführt"
            val message = if (count > 0) {
                "$count Einträge erfolgreich importiert ($modeText)"
            } else {
                "Keine gültigen Einträge in der Datei gefunden"
            }
            _uiState.value = _uiState.value.copy(importStatusMessage = message)
        }
    }

    fun clearImportStatusMessage() {
        _uiState.value = _uiState.value.copy(importStatusMessage = null)
    }

    // ==============================================================
    // Notebooks (Notizbücher) Management
    // ==============================================================

    fun selectNotebook(notebookId: String) {
        cancelSearch()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                notebookRepository.setActiveNotebookId(notebookId)
                val updatedAll = notebookRepository.listAllNotebooks()
                val newActive = notebookRepository.getActiveNotebook()
                val settings = newActive.settings

                // Hydrate Room with the selected notebook's history
                val historyItems = notebookRepository.loadNotebookHistory(notebookId)
                repository.reloadRoomWithItems(historyItems)

                // Update TTS speed rate
                ttsManager.setSpeedRate(settings.ttsSpeed)

                // Update theme
                val theme = AppThemePackage.fromId(settings.themeColorId)
                themePreferences.saveSelectedTheme(theme)
                themePreferences.saveThemeMode(settings.themeMode)
                val globalLanguage = themePreferences.getAppLanguage()
                val effectiveLanguage = if (globalLanguage.isNotBlank()) globalLanguage else settings.appLanguage.ifBlank { "de" }
                themePreferences.saveAppLanguage(effectiveLanguage)
                val globalTranslationLanguage = themePreferences.getTranslationLanguage()
                val effectiveTranslationLanguage = if (globalTranslationLanguage.isNotBlank()) globalTranslationLanguage else settings.translationLanguage.ifBlank { "en" }
                themePreferences.saveTranslationLanguage(effectiveTranslationLanguage)

                // Sync provider & API key state to repository
                val provider = ModelProvider.fromId(settings.selectedProvider)
                repository.saveSelectedProvider(provider)
                if (settings.customApiKey.isNotBlank()) {
                    repository.saveGeminiApiKey(settings.customApiKey)
                }
                if (settings.openRouterApiKey.isNotBlank()) {
                    repository.saveOpenRouterApiKey(settings.openRouterApiKey)
                }
                val effectiveGeminiKey = settings.customApiKey.ifBlank { repository.getUserEnteredGeminiKey() }
                val effectiveOpenRouterKey = settings.openRouterApiKey.ifBlank { repository.getOpenRouterApiKey() }
                val effectiveModel = settings.selectedModel.ifBlank { repository.getSelectedModel(provider) }
                val isConfigured = settings.customApiKey.isNotBlank() ||
                    settings.openRouterApiKey.isNotBlank() ||
                    repository.isApiKeyConfigured()
                val (remaining, total) = repository.getDailyUsage(provider, effectiveModel)

                _uiState.update { current ->
                    current.copy(
                        allNotebooks = updatedAll,
                        activeNotebook = newActive,
                        selectedTheme = theme,
                        themeMode = settings.themeMode,
                        appLanguage = effectiveLanguage,
                        translationLanguage = effectiveTranslationLanguage,
                        selectedProvider = provider,
                        selectedModel = effectiveModel,
                        availableModels = repository.getAvailableModelsForProvider(provider),
                        hasApiKey = isConfigured,
                        isStartupSettingsLoaded = true,
                        remainingDailyRequests = remaining,
                        totalDailyQuota = total,
                        apiKeyInput = effectiveGeminiKey,
                        geminiApiKeyInput = effectiveGeminiKey,
                        openRouterApiKeyInput = effectiveOpenRouterKey,
                        otherApiKeyInput = effectiveOpenRouterKey,
                        currentResult = null,
                        currentGrammarResult = null,
                        searchQuery = "",
                        selectedHistoryFolderName = if (settings.storageFolderUri.isNotBlank()) settings.storageFolderUri else "Standard (${newActive.name})"
                    )
                }
            } catch (e: Throwable) {
                Log.e("MainViewModel", "Error selecting notebook $notebookId", e)
            }
        }
    }

    fun createNotebook(name: String, themeColorId: String = "Dunkelblau", themeMode: String = "auto") {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val notebook = notebookRepository.createNotebook(name, themeColorId, themeMode)
                _uiState.update { it.copy(isCreateNotebookDialogOpen = false) }
                selectNotebook(notebook.id)
            } catch (e: Throwable) {
                Log.e("MainViewModel", "Error creating notebook '$name'", e)
            }
        }
    }

    fun renameNotebook(notebookId: String, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                notebookRepository.renameNotebook(notebookId, trimmed)
                val updatedAll = notebookRepository.listAllNotebooks()
                val active = notebookRepository.getActiveNotebook()
                _uiState.update {
                    it.copy(
                        allNotebooks = updatedAll,
                        activeNotebook = active
                    )
                }
            } catch (e: Throwable) {
                Log.e("MainViewModel", "Error renaming notebook $notebookId to '$newName'", e)
            }
        }
    }

    fun deleteNotebook(notebookId: String) {
        cancelSearch()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val success = notebookRepository.deleteNotebook(notebookId)
                if (success) {
                    val active = notebookRepository.getActiveNotebook()
                    selectNotebook(active.id)
                }
            } catch (e: Throwable) {
                Log.e("MainViewModel", "Error deleting notebook $notebookId", e)
            }
        }
    }

    // =========================================================================
    // Direct JSON Export & Import Logic
    // =========================================================================

    fun prepareSingleNotebookExport(notebookId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val notebook = notebookRepository.listAllNotebooks().find { it.id == notebookId } ?: return@launch
            val json = notebookRepository.exportNotebookToJsonString(notebookId)
            val fileName = "${notebook.name}.json"
            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(
                    activeJsonExportTarget = JsonExportTarget(
                        fileName = fileName,
                        jsonContent = json,
                        title = "Notizbuch exportieren: ${notebook.name}"
                    )
                )
            }
        }
    }

    fun prepareAllNotebooksCombinedExport() {
        viewModelScope.launch(Dispatchers.IO) {
            val json = notebookRepository.exportNotebooksCombinedJsonString()
            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(
                    activeJsonExportTarget = JsonExportTarget(
                        fileName = "Alle Notizbücher.json",
                        jsonContent = json,
                        title = "Alle Notizbücher exportieren"
                    )
                )
            }
        }
    }

    fun prepareSelectedNotebooksCombinedExport(selectedIds: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val json = notebookRepository.exportNotebooksCombinedJsonString(selectedIds)
            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(
                    isSelectNotebooksExportDialogOpen = false,
                    activeJsonExportTarget = JsonExportTarget(
                        fileName = "Ausgewählte Notizbücher.json",
                        jsonContent = json,
                        title = "Ausgewählte Notizbücher exportieren"
                    )
                )
            }
        }
    }

    fun openSelectNotebooksExportDialog() {
        _uiState.value = _uiState.value.copy(isSelectNotebooksExportDialogOpen = true)
    }

    fun closeSelectNotebooksExportDialog() {
        _uiState.value = _uiState.value.copy(isSelectNotebooksExportDialogOpen = false)
    }

    fun dismissJsonExportDialog() {
        _uiState.value = _uiState.value.copy(activeJsonExportTarget = null)
    }

    fun executeSaveJsonToUri(uri: Uri, context: Context) {
        val target = _uiState.value.activeJsonExportTarget ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val success = ExportSharingManager.saveJsonToUri(context, uri, target.jsonContent)
            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(activeJsonExportTarget = null)
                if (success) {
                    _uiState.value = _uiState.value.copy(
                        importStatusMessage = "Datei '${target.fileName}' erfolgreich gespeichert!"
                    )
                }
            }
        }
    }

    fun shareActiveJsonExport(context: Context) {
        val target = _uiState.value.activeJsonExportTarget ?: return
        ExportSharingManager.shareJsonContent(
            context = context,
            fileName = target.fileName,
            jsonContent = target.jsonContent,
            title = target.title
        )
        _uiState.value = _uiState.value.copy(activeJsonExportTarget = null)
    }

    fun setPendingTreeExport(notebookIds: List<String>?) {
        _uiState.value = _uiState.value.copy(pendingTreeExportNotebookIds = notebookIds)
    }

    fun executeTreeExport(folderUri: Uri, context: Context, notebookIds: List<String> = emptyList()) {
        viewModelScope.launch(Dispatchers.IO) {
            val count = notebookRepository.exportNotebooksToTreeUri(context, folderUri, notebookIds)
            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(
                    pendingTreeExportNotebookIds = null,
                    isSelectNotebooksExportDialogOpen = false,
                    importStatusMessage = "$count Notizbücher als separate JSON-Dateien exportiert!"
                )
            }
        }
    }

    // ==============================================================
    // Backup Folders (Auto-Mirroring JSON to Multiple Folders)
    // ==============================================================

    fun addBackupFolder(uri: Uri) {
        viewModelScope.launch {
            notebookBackupManager.addFolder(uri)
        }
    }

    fun setPrimaryBackupFolder(uriString: String) {
        viewModelScope.launch(Dispatchers.IO) {
            notebookBackupManager.setPrimaryFolder(uriString)
            // Reload history from the new primary source of truth
            val primaryHistory = notebookRepository.loadActiveHistory()
            repository.reloadRoomWithItems(primaryHistory)
        }
    }

    fun removeBackupFolder(uriString: String) {
        notebookBackupManager.removeFolder(uriString)
    }

    fun backupNow(onResult: ((Boolean, String) -> Unit)? = null) {
        viewModelScope.launch {
            val result = notebookBackupManager.backupAllNotebooks()
            withContext(Dispatchers.Main) {
                if (result.totalFolders == 0) {
                    onResult?.invoke(false, "Keine Backup-Ordner registriert")
                } else if (result.successfulFolders == result.totalFolders) {
                    onResult?.invoke(true, "${result.totalWritten} Notizbücher in ${result.successfulFolders} Ordner(n) gesichert")
                } else {
                    onResult?.invoke(false, "${result.successfulFolders}/${result.totalFolders} Ordner erfolgreich gesichert")
                }
            }
        }
    }

    fun handleImportJsonUri(uri: Uri, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val fileName = getFileNameFromUri(uri, context) ?: "Importiertes_Notizbuch.json"
                val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                if (content.isNullOrBlank()) {
                    withContext(Dispatchers.Main) {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Die ausgewählte Datei ist leer oder ungültig."
                        )
                    }
                    return@launch
                }

                when (val result = notebookRepository.parseImportedJson(content, fileName)) {
                    is ImportedJsonResult.SingleNotebook -> {
                        withContext(Dispatchers.Main) {
                            _uiState.value = _uiState.value.copy(
                                importSingleChoice = ImportSingleChoice(
                                    detectedName = result.detectedName,
                                    words = result.words,
                                    settings = result.settings
                                )
                            )
                        }
                    }
                    is ImportedJsonResult.MultipleNotebooks -> {
                        withContext(Dispatchers.Main) {
                            _uiState.value = _uiState.value.copy(
                                importMultiChoice = ImportMultiChoice(
                                    packages = result.packages,
                                    fallbackName = fileName.removeSuffix(".json").removeSuffix(".JSON")
                                )
                            )
                        }
                    }
                    is ImportedJsonResult.EmptyOrInvalid -> {
                        withContext(Dispatchers.Main) {
                            _uiState.value = _uiState.value.copy(
                                errorMessage = "Keine gültigen Wörter in der JSON-Datei gefunden."
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error reading imported JSON", e)
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Fehler beim Lesen der Datei: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun dismissImportSingleChoice() {
        _uiState.value = _uiState.value.copy(importSingleChoice = null)
    }

    fun dismissImportMultiChoice() {
        _uiState.value = _uiState.value.copy(importMultiChoice = null)
    }

    fun dismissPostImportApiKeyPrompt() {
        _uiState.value = _uiState.value.copy(postImportApiKeyPrompt = null)
    }

    fun savePostImportApiKey(apiKey: String) {
        val trimmed = apiKey.trim()
        if (trimmed.isNotBlank()) {
            apiKeyManager.saveApiKey(trimmed)
            val currentSettings = _uiState.value.activeNotebook?.settings
            if (currentSettings != null) {
                val updated = currentSettings.copy(customApiKey = trimmed)
                saveActiveNotebookSettings(updated)
            }
        }
        _uiState.value = _uiState.value.copy(
            postImportApiKeyPrompt = null,
            hasApiKey = apiKeyManager.isKeyConfigured()
        )
    }

    fun openSettingsFromPostImport() {
        _uiState.value = _uiState.value.copy(
            postImportApiKeyPrompt = null,
            isApiKeyDialogOpen = true
        )
    }

    fun executeImportSingleMerge() {
        val choice = _uiState.value.importSingleChoice ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val merged = notebookRepository.importSingleNotebookMergeActive(choice.words)
            repository.reloadRoomWithItems(merged)
            val updatedAll = notebookRepository.listAllNotebooks()
            val active = notebookRepository.getActiveNotebook()
            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(
                    importSingleChoice = null,
                    allNotebooks = updatedAll,
                    activeNotebook = active,
                    importStatusMessage = "${choice.words.size} Wörter in '${active.name}' zusammengeführt!"
                )
            }
        }
    }

    fun executeImportSingleAsNew(notebookName: String) {
        val choice = _uiState.value.importSingleChoice ?: return
        val nameToUse = notebookName.trim().ifBlank { choice.detectedName }
        viewModelScope.launch(Dispatchers.IO) {
            val newNotebook = notebookRepository.importSingleNotebookAsNew(
                name = nameToUse,
                words = choice.words,
                importedSettings = choice.settings
            )
            val activeHistory = notebookRepository.loadActiveHistory()
            repository.reloadRoomWithItems(activeHistory)
            val updatedAll = notebookRepository.listAllNotebooks()

            // Apply theme/language from imported settings if present
            if (choice.settings != null) {
                themePreferences.saveSelectedTheme(AppThemePackage.fromId(choice.settings.themeColorId))
                themePreferences.saveThemeMode(choice.settings.themeMode)
                themePreferences.saveAppLanguage(choice.settings.appLanguage)
            }

            withContext(Dispatchers.Main) {
                val promptKey = if (!apiKeyManager.isKeyConfigured() || choice.settings != null) {
                    PostImportApiKeyPrompt(
                        notebookName = newNotebook.name,
                        provider = newNotebook.settings.selectedProvider,
                        modelId = newNotebook.settings.selectedModel
                    )
                } else null

                _uiState.value = _uiState.value.copy(
                    importSingleChoice = null,
                    allNotebooks = updatedAll,
                    activeNotebook = newNotebook,
                    selectedTheme = AppThemePackage.fromId(newNotebook.settings.themeColorId),
                    themeMode = newNotebook.settings.themeMode,
                    appLanguage = newNotebook.settings.appLanguage,
                    importStatusMessage = "Notizbuch '${newNotebook.name}' mit ${choice.words.size} Wörtern erstellt!",
                    postImportApiKeyPrompt = promptKey
                )
            }
        }
    }

    fun executeImportMultiSeparate() {
        val choice = _uiState.value.importMultiChoice ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val importedList = notebookRepository.importMultipleNotebooksSeparate(choice.packages)
            val active = notebookRepository.getActiveNotebook()
            val activeHistory = notebookRepository.loadActiveHistory()
            repository.reloadRoomWithItems(activeHistory)
            val updatedAll = notebookRepository.listAllNotebooks()
            withContext(Dispatchers.Main) {
                val promptKey = if (!apiKeyManager.isKeyConfigured() || importedList.any { it.settings.selectedProvider.isNotBlank() }) {
                    PostImportApiKeyPrompt(
                        notebookName = active.name,
                        provider = active.settings.selectedProvider,
                        modelId = active.settings.selectedModel
                    )
                } else null

                _uiState.value = _uiState.value.copy(
                    importMultiChoice = null,
                    allNotebooks = updatedAll,
                    activeNotebook = active,
                    selectedTheme = AppThemePackage.fromId(active.settings.themeColorId),
                    themeMode = active.settings.themeMode,
                    appLanguage = active.settings.appLanguage,
                    importStatusMessage = "${importedList.size} Notizbücher erfolgreich importiert!",
                    postImportApiKeyPrompt = promptKey
                )
            }
        }
    }

    fun executeImportMultiAsSingle(notebookName: String) {
        val choice = _uiState.value.importMultiChoice ?: return
        val nameToUse = notebookName.trim().ifBlank { choice.fallbackName }
        viewModelScope.launch(Dispatchers.IO) {
            val allWords = choice.packages.flatMap { it.words }
            val firstSettings = choice.packages.firstOrNull { it.settings != null }?.settings
            val newNotebook = notebookRepository.importMultipleNotebooksAsSingle(
                name = nameToUse,
                allWords = allWords,
                firstSettings = firstSettings
            )
            val activeHistory = notebookRepository.loadActiveHistory()
            repository.reloadRoomWithItems(activeHistory)
            val updatedAll = notebookRepository.listAllNotebooks()

            if (firstSettings != null) {
                themePreferences.saveSelectedTheme(AppThemePackage.fromId(firstSettings.themeColorId))
                themePreferences.saveThemeMode(firstSettings.themeMode)
                themePreferences.saveAppLanguage(firstSettings.appLanguage)
            }

            withContext(Dispatchers.Main) {
                val promptKey = if (!apiKeyManager.isKeyConfigured() || firstSettings != null) {
                    PostImportApiKeyPrompt(
                        notebookName = newNotebook.name,
                        provider = newNotebook.settings.selectedProvider,
                        modelId = newNotebook.settings.selectedModel
                    )
                } else null

                _uiState.value = _uiState.value.copy(
                    importMultiChoice = null,
                    allNotebooks = updatedAll,
                    activeNotebook = newNotebook,
                    selectedTheme = AppThemePackage.fromId(newNotebook.settings.themeColorId),
                    themeMode = newNotebook.settings.themeMode,
                    appLanguage = newNotebook.settings.appLanguage,
                    importStatusMessage = "Notizbuch '${newNotebook.name}' mit ${allWords.size} Wörtern erstellt!",
                    postImportApiKeyPrompt = promptKey
                )
            }
        }
    }

    private fun getFileNameFromUri(uri: Uri, context: Context): String? {
        var name: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        name = it.getString(index)
                    }
                }
            }
        }
        return name ?: uri.lastPathSegment
    }

    fun openCreateNotebookDialog() {
        val currentLanguage = themePreferences.getAppLanguage()
        _uiState.update {
            it.copy(
                appLanguage = currentLanguage,
                isCreateNotebookDialogOpen = true
            )
        }
    }

    fun closeCreateNotebookDialog() {
        _uiState.update { it.copy(isCreateNotebookDialogOpen = false) }
    }

    fun openManageNotebooksDialog() {
        val currentLanguage = themePreferences.getAppLanguage()
        _uiState.update {
            it.copy(
                appLanguage = currentLanguage,
                isManageNotebooksDialogOpen = true
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            val all = notebookRepository.listAllNotebooks()
            _uiState.update { it.copy(allNotebooks = all) }
        }
    }

    fun closeManageNotebooksDialog() {
        _uiState.update { it.copy(isManageNotebooksDialogOpen = false) }
    }

    fun onOpenNotebookManagerFromSettings() {
        val currentLanguage = themePreferences.getAppLanguage()
        _uiState.update {
            it.copy(
                isApiKeyDialogOpen = false,
                appLanguage = currentLanguage,
                isManageNotebooksDialogOpen = true
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            val all = notebookRepository.listAllNotebooks()
            _uiState.update { it.copy(allNotebooks = all) }
        }
    }

    fun onThemeModeSelected(mode: String) {
        themePreferences.saveThemeMode(mode)
        _uiState.update { it.copy(themeMode = mode) }
        val currentSettings = _uiState.value.activeNotebook?.settings
        if (currentSettings != null) {
            val updated = currentSettings.copy(themeMode = mode)
            saveActiveNotebookSettings(updated)
        }
    }

    fun onAppLanguageSelected(languageCode: String) {
        val cleanCode = when (languageCode.lowercase().trim()) {
            "en" -> "en"
            "ar" -> "ar"
            else -> "de"
        }
        themePreferences.saveAppLanguage(cleanCode)
        _uiState.update { it.copy(appLanguage = cleanCode) }
        val currentSettings = _uiState.value.activeNotebook?.settings
        if (currentSettings != null) {
            val updated = currentSettings.copy(appLanguage = cleanCode)
            saveActiveNotebookSettings(updated)
        }
    }

    fun onTranslationLanguageSelected(languageCode: String) {
        val cleanCode = when (languageCode.lowercase().trim()) {
            "ar" -> "ar"
            else -> "en"
        }
        themePreferences.saveTranslationLanguage(cleanCode)
        _uiState.update { it.copy(translationLanguage = cleanCode) }
        val currentSettings = _uiState.value.activeNotebook?.settings
        if (currentSettings != null) {
            val updated = currentSettings.copy(translationLanguage = cleanCode)
            saveActiveNotebookSettings(updated)
        }
    }

    fun saveActiveNotebookSettings(settings: NotebookSettings) {
        _uiState.update { current ->
            val updatedActive = current.activeNotebook?.copy(settings = settings)
            if (updatedActive != null) current.copy(activeNotebook = updatedActive) else current
        }
        viewModelScope.launch(Dispatchers.IO) {
            notebookRepository.saveActiveSettings(settings)
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}
