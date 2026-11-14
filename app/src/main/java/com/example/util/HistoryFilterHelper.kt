package com.example.util

import com.example.data.local.WordHistoryEntity
import java.util.Locale

/**
 * Sort criteria for German history items.
 */
enum class HistorySortField(val labelDe: String, val shortLabel: String) {
    DATE("Datum", "Datum"),
    NAME("Name", "Name"),
    GENDER("Genus", "Genus"),
    TYPE("Typ", "Typ");

    fun label(appLanguage: String): String = when (appLanguage.lowercase(Locale.ROOT).trim()) {
        "en", "english" -> when (this) {
            DATE -> "Date"
            NAME -> "Name"
            GENDER -> "Gender"
            TYPE -> "Type"
        }
        "ar", "arabic" -> when (this) {
            DATE -> "التاريخ"
            NAME -> "الاسم"
            GENDER -> "الجنس"
            TYPE -> "النوع"
        }
        else -> labelDe
    }
}

/**
 * Mode for date sorting.
 */
enum class DateSortMode(val labelDe: String) {
    AUTOMATIC("Automatisch"),
    MANUAL("Manuelles Datum");

    fun label(appLanguage: String): String = when (appLanguage.lowercase(Locale.ROOT).trim()) {
        "en", "english" -> when (this) {
            AUTOMATIC -> "Automatic"
            MANUAL -> "Manual Date"
        }
        "ar", "arabic" -> when (this) {
            AUTOMATIC -> "تلقائي"
            MANUAL -> "تاريخ يدوي"
        }
        else -> labelDe
    }
}

/**
 * Target gender when Genus sorting is active.
 */
enum class GenderSortTarget(val labelDe: String, val article: String) {
    ALL("Alle Genus", "Alle"),
    MASKULIN("Maskulin (der)", "der"),
    FEMININ("Feminin (die)", "die"),
    NEUTRUM("Neutrum (das)", "das");

    fun label(appLanguage: String): String = when (appLanguage.lowercase(Locale.ROOT).trim()) {
        "en", "english" -> when (this) {
            ALL -> "All Genders"
            MASKULIN -> "Masculine (der)"
            FEMININ -> "Feminine (die)"
            NEUTRUM -> "Neuter (das)"
        }
        "ar", "arabic" -> when (this) {
            ALL -> "كل الأجناس"
            MASKULIN -> "مذكر (der)"
            FEMININ -> "مؤنث (die)"
            NEUTRUM -> "محايد (das)"
        }
        else -> labelDe
    }
}

/**
 * Sort direction.
 */
enum class SortDirection(val labelDe: String, val shortLabel: String) {
    ASCENDING("Aufsteigend", "Auf"),
    DESCENDING("Absteigend", "Ab");

    fun label(appLanguage: String): String = when (appLanguage.lowercase(Locale.ROOT).trim()) {
        "en", "english" -> when (this) {
            ASCENDING -> "Ascending"
            DESCENDING -> "Descending"
        }
        "ar", "arabic" -> when (this) {
            ASCENDING -> "تصاعدي"
            DESCENDING -> "تنازلي"
        }
        else -> labelDe
    }
}

/**
 * Independent sort configuration per history category tab.
 */
data class HistorySortOption(
    val field: HistorySortField = HistorySortField.DATE,
    val direction: SortDirection = SortDirection.DESCENDING,
    val dateSortMode: DateSortMode = DateSortMode.AUTOMATIC,
    val manualDateMillis: Long? = null,
    val genderTarget: GenderSortTarget = GenderSortTarget.ALL
) {
    val isDefault: Boolean = (
        field == HistorySortField.DATE &&
        direction == SortDirection.DESCENDING &&
        dateSortMode == DateSortMode.AUTOMATIC &&
        genderTarget == GenderSortTarget.ALL
    )
}

/**
 * Represents how closely a history log item matches a search query.
 * Lower values indicate a closer match:
 * - [tier]: 0 = exact base word, 1 = exact article+word, 2 = exact token/translation,
 *           3 = base word prefix, 4 = article+word prefix, 5 = phrase/translation token prefix,
 *           6 = substring match
 * - [effectiveWordLength]: length of the item's primary word/phrase (prioritizes short words over long phrases)
 * - [matchedTargetLength]: length of the specific matched token or field
 * - [matchIndex]: character offset where the match starts
 */
data class SearchMatchRank(
    val tier: Int,
    val effectiveWordLength: Int,
    val matchedTargetLength: Int,
    val matchIndex: Int
) : Comparable<SearchMatchRank> {
    override fun compareTo(other: SearchMatchRank): Int {
        if (this.tier != other.tier) return this.tier.compareTo(other.tier)
        if (this.effectiveWordLength != other.effectiveWordLength) {
            return this.effectiveWordLength.compareTo(other.effectiveWordLength)
        }
        if (this.matchedTargetLength != other.matchedTargetLength) {
            return this.matchedTargetLength.compareTo(other.matchedTargetLength)
        }
        return this.matchIndex.compareTo(other.matchIndex)
    }
}

internal enum class HistoryItemCategory(val sortOrder: Int) {
    NOUN(1),
    VERB(2),
    SENTENCE(3),
    NOMEN_LIST(4),
    ADJEKTIV(5)
}

internal class IndexedSearchTarget(
    val clean: String,
    val lower: String,
    val normGerman: String,
    val hasArabic: Boolean,
    val normArabic: String,
    val targetLen: Int
)

internal class IndexedHistoryItem(
    val category: HistoryItemCategory,
    val rawWord: String,
    val wordWithoutArticle: String,
    val effectiveSortKeyLower: String,
    val rawWordLower: String,
    val genderLower: String,
    val article: String,
    val effectiveLen: Int,
    val isSingleWordEntry: Boolean,
    val baseTarget: IndexedSearchTarget?,
    val rawTarget: IndexedSearchTarget?,
    val articleTarget: IndexedSearchTarget?,
    val listTokenTargets: List<Pair<IndexedSearchTarget?, IndexedSearchTarget?>>,
    val phraseWordTargets: List<IndexedSearchTarget?>,
    val cleanedTranslationTarget: IndexedSearchTarget?,
    val translationTokenTargets: List<Pair<IndexedSearchTarget?, IndexedSearchTarget?>>,
    val manualEntryTargets: List<Pair<IndexedSearchTarget?, List<IndexedSearchTarget?>>>
)

/**
 * Helper object providing robust classification, closeness-ranked search logic,
 * and independent multi-criteria sorting for history items.
 */
object HistoryFilterHelper {

    private val WHITESPACE_COMMA_SPLIT_REGEX = Regex("[\\s,;]+")
    private val LIST_SEPARATOR_SPLIT_REGEX = Regex("[,;،]+")
    private val PHRASE_WORD_SPLIT_REGEX = Regex("[\\s/,;،.!?()\\[\\]\"„“'«»–-]+")
    private val TRANSLATION_TOKEN_SPLIT_REGEX = Regex("[,;/،\n()]+")
    private val MANUAL_SUBTOKEN_SPLIT_REGEX = Regex("[,;/،]+")

    private val TRIM_START_CHARS = charArrayOf('"', '„', '“', '\'', '«', '»', '-', '–', '(', '[', '{', ' ')
    private val TRIM_END_CHARS = charArrayOf('"', '„', '“', '\'', '«', '»', '!', '?', '.', ',', ';', ')', ']', '}', ' ')

    private const val MAX_CACHE_SIZE = 2048
    private val indexCache = object : LinkedHashMap<String, IndexedHistoryItem>(256, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, IndexedHistoryItem>?): Boolean {
            return size > MAX_CACHE_SIZE
        }
    }

    private fun cacheKeyFor(item: WordHistoryEntity): String {
        return "${item.id}_${item.timestamp}_${item.word.hashCode()}_${item.type.hashCode()}_${item.gender.hashCode()}_${item.genderArticle.hashCode()}_${item.meaningEnglish.hashCode()}_${item.manualTranslation.hashCode()}_${item.rawJsonResult.hashCode()}"
    }

    private fun cleanTargetString(text: String): String {
        return text.trim()
            .trimStart(*TRIM_START_CHARS)
            .trimEnd(*TRIM_END_CHARS)
    }

    private fun buildTarget(text: String): IndexedSearchTarget? {
        val clean = cleanTargetString(text)
        if (clean.isBlank()) return null
        val lower = clean.lowercase(Locale.GERMAN)
        val normGerman = normalizeGerman(clean)
        val hasAr = isArabicText(clean)
        val normAr = if (hasAr) normalizeArabic(clean).lowercase(Locale.ROOT) else ""
        return IndexedSearchTarget(
            clean = clean,
            lower = lower,
            normGerman = normGerman,
            hasArabic = hasAr,
            normArabic = normAr,
            targetLen = clean.length
        )
    }

    private fun classifyCategorySinglePass(item: WordHistoryEntity): HistoryItemCategory {
        val t = item.type.lowercase(Locale.ROOT).trim()
        val g = item.gender.lowercase(Locale.ROOT).trim()
        val rawJson = item.rawJsonResult

        // 1. Adjektiv check
        if (t == "adjektiv" || t == "adjektive" || t == "adjective" ||
            g == "adjektiv" || g == "adjektive" ||
            rawJson.contains("\"Adjektiv\"") ||
            (rawJson.contains("\"komparativ\"") && rawJson.contains("\"superlativ\""))
        ) {
            return HistoryItemCategory.ADJEKTIV
        }

        // 2. Verb check
        if (t == "verb" || t == "verben" || rawJson.contains("\"infinitiv\"")) {
            return HistoryItemCategory.VERB
        }

        // 3. NomenList / Wörter check
        if (t == "wörter" || t == "woerter" || t == "nomenlist" || t == "nomen (liste)" ||
            g == "wort" || g == "wörter" || g == "woerter" ||
            rawJson.contains("\"Wörter\"") || rawJson.contains("\"Woerter\"") ||
            rawJson.contains("\"NomenList\"")
        ) {
            return HistoryItemCategory.NOMEN_LIST
        }

        // 4. Sentence check
        if (t == "satz" || t == "sentence" || t == "sätze" || t == "saetze" ||
            g == "satz" || g == "–" ||
            rawJson.contains("\"Satz\"") || rawJson.contains("\"Sentence\"") ||
            (g != "maskulin" && g != "feminin" && g != "neutrum" &&
                item.word.trim().split(WHITESPACE_COMMA_SPLIT_REGEX).count { it.isNotBlank() } >= 3)
        ) {
            return HistoryItemCategory.SENTENCE
        }

        return HistoryItemCategory.NOUN
    }

    internal fun getIndexedItem(item: WordHistoryEntity): IndexedHistoryItem {
        val key = cacheKeyFor(item)
        synchronized(indexCache) {
            indexCache[key]?.let { return it }
        }

        val category = classifyCategorySinglePass(item)
        val rawWord = item.word.trim()
        val wordWithoutArticle = computeEffectiveWordForLetterSort(rawWord)
        val effectiveSortKeyLower = wordWithoutArticle.lowercase(Locale.GERMAN)
        val rawWordLower = rawWord.lowercase(Locale.GERMAN)
        val genderLower = item.gender.lowercase(Locale.ROOT).trim()
        val article = item.genderArticle.trim().takeUnless { it.equals("null", ignoreCase = true) }.orEmpty()
        val effectiveLen = wordWithoutArticle.length.coerceAtLeast(1)
        val isSingleWordEntry = !wordWithoutArticle.contains(' ') && category != HistoryItemCategory.SENTENCE

        val baseTarget = buildTarget(wordWithoutArticle)
        val rawTarget = buildTarget(rawWord)
        val articleTarget = if (article.isNotBlank()) {
            val fullWithArticle = if (!rawWord.startsWith("$article ", ignoreCase = true)) {
                "$article $rawWord"
            } else {
                rawWord
            }
            buildTarget(fullWithArticle)
        } else null

        val listTokenTargets = if (rawWord.contains(',') || rawWord.contains(';') || rawWord.contains('،')) {
            rawWord.split(LIST_SEPARATOR_SPLIT_REGEX)
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .map { token ->
                    val cleanToken = when {
                        token.startsWith("der ", ignoreCase = true) ||
                        token.startsWith("die ", ignoreCase = true) ||
                        token.startsWith("das ", ignoreCase = true) -> token.substring(4).trim()
                        token.startsWith("ein ", ignoreCase = true) -> token.substring(4).trim()
                        token.startsWith("eine ", ignoreCase = true) -> token.substring(5).trim()
                        else -> token
                    }
                    buildTarget(cleanToken) to buildTarget(token)
                }
        } else emptyList()

        val phraseWords = wordWithoutArticle.split(PHRASE_WORD_SPLIT_REGEX)
            .map { it.trim() }
            .filter { it.isNotBlank() }
        val phraseWordTargets = if (phraseWords.size > 1) {
            phraseWords.map { buildTarget(it) }
        } else emptyList()

        val rawTranslation = item.meaningEnglish.trim()
        val cleanedTranslation = if (rawTranslation.isNotBlank()) {
            cleanTranslationText(rawTranslation, isArabicText(rawTranslation))
        } else ""
        val cleanedTranslationTarget = if (cleanedTranslation.isNotBlank()) buildTarget(cleanedTranslation) else null
        val translationTokenTargets = if (cleanedTranslation.isNotBlank()) {
            cleanedTranslation.split(TRANSLATION_TOKEN_SPLIT_REGEX)
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .map { token ->
                    val strippedTo = if (token.startsWith("to ", ignoreCase = true)) {
                        token.substring(3).trim()
                    } else token
                    buildTarget(strippedTo) to buildTarget(token)
                }
        } else emptyList()

        val manual = item.manualTranslation.trim()
        val manualEntryTargets = if (manual.isNotBlank()) {
            parseManualTranslations(manual).map { manualEntry ->
                val subTokens = manualEntry.split(MANUAL_SUBTOKEN_SPLIT_REGEX)
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .map { buildTarget(it) }
                buildTarget(manualEntry) to subTokens
            }
        } else emptyList()

        val indexed = IndexedHistoryItem(
            category = category,
            rawWord = rawWord,
            wordWithoutArticle = wordWithoutArticle,
            effectiveSortKeyLower = effectiveSortKeyLower,
            rawWordLower = rawWordLower,
            genderLower = genderLower,
            article = article,
            effectiveLen = effectiveLen,
            isSingleWordEntry = isSingleWordEntry,
            baseTarget = baseTarget,
            rawTarget = rawTarget,
            articleTarget = articleTarget,
            listTokenTargets = listTokenTargets,
            phraseWordTargets = phraseWordTargets,
            cleanedTranslationTarget = cleanedTranslationTarget,
            translationTokenTargets = translationTokenTargets,
            manualEntryTargets = manualEntryTargets
        )

        synchronized(indexCache) {
            indexCache[key] = indexed
        }
        return indexed
    }

    /**
     * Ensures a history log entry actually contains a valid, non-empty term.
     */
    fun isValidLogEntry(item: WordHistoryEntity): Boolean {
        val raw = item.word.trim()
        return raw.isNotBlank() && !raw.equals("null", ignoreCase = true)
    }

    fun isAdjektivItem(item: WordHistoryEntity): Boolean =
        getIndexedItem(item).category == HistoryItemCategory.ADJEKTIV

    fun isVerbItem(item: WordHistoryEntity): Boolean =
        getIndexedItem(item).category == HistoryItemCategory.VERB

    fun isNomenListItem(item: WordHistoryEntity): Boolean =
        getIndexedItem(item).category == HistoryItemCategory.NOMEN_LIST

    fun isSentenceItem(item: WordHistoryEntity): Boolean =
        getIndexedItem(item).category == HistoryItemCategory.SENTENCE

    fun isNounItem(item: WordHistoryEntity): Boolean =
        getIndexedItem(item).category == HistoryItemCategory.NOUN

    fun normalizeGerman(s: String): String {
        return s.lowercase(Locale.GERMAN)
            .replace("ä", "a")
            .replace("ö", "o")
            .replace("ü", "u")
            .replace("ß", "ss")
    }

    private fun computeEffectiveWordForLetterSort(rawInput: String): String {
        val raw = rawInput.trim().trimStart(*TRIM_START_CHARS)
        val lower = raw.lowercase(Locale.GERMAN)
        val withoutArticle = when {
            lower.startsWith("der ") -> raw.substring(4).trim()
            lower.startsWith("die ") -> raw.substring(4).trim()
            lower.startsWith("das ") -> raw.substring(4).trim()
            lower.startsWith("dem ") -> raw.substring(4).trim()
            lower.startsWith("den ") -> raw.substring(4).trim()
            lower.startsWith("des ") -> raw.substring(4).trim()
            lower.startsWith("ein ") -> raw.substring(4).trim()
            lower.startsWith("eine ") -> raw.substring(5).trim()
            lower.startsWith("einen ") -> raw.substring(6).trim()
            lower.startsWith("einem ") -> raw.substring(6).trim()
            lower.startsWith("einer ") -> raw.substring(6).trim()
            lower.startsWith("eines ") -> raw.substring(6).trim()
            lower.startsWith("sich ") -> raw.substring(5).trim()
            else -> raw
        }
        return withoutArticle.trimStart(*TRIM_START_CHARS)
    }

    /**
     * Extracts the effective word for sorting, stripping leading definite/indefinite
     * articles (der/die/das/dem/den/des/ein/eine/einen/einem/einer/eines) and reflexive pronouns (sich).
     */
    fun getEffectiveWordForLetterSort(item: WordHistoryEntity): String {
        return getIndexedItem(item).wordWithoutArticle
    }

    /**
     * Computes how closely [item] matches [query] across German words, phrases, and translations.
     * Returns null if [item] is invalid/empty or does not match [query] at all.
     * Prioritizes exact matches and shorter terms over long phrases or sentences.
     */
    fun computeSearchCloseness(item: WordHistoryEntity, query: String): SearchMatchRank? {
        if (!isValidLogEntry(item)) return null

        val q = query.trim()
            .trimStart(*TRIM_START_CHARS)
            .trimEnd(*TRIM_END_CHARS)
            .lowercase(Locale.GERMAN)
        if (q.isBlank()) return SearchMatchRank(0, 0, 0, 0)

        val qNormGerman = normalizeGerman(q)
        val qNormArabic = normalizeArabic(q).lowercase(Locale.ROOT)
        val isArabicQuery = isArabicText(q)

        return computeSearchClosenessPreNormalized(
            indexed = getIndexedItem(item),
            q = q,
            qNormGerman = qNormGerman,
            qNormArabic = qNormArabic,
            isArabicQuery = isArabicQuery
        )
    }

    private fun computeSearchClosenessPreNormalized(
        indexed: IndexedHistoryItem,
        q: String,
        qNormGerman: String,
        qNormArabic: String,
        isArabicQuery: Boolean
    ): SearchMatchRank? {
        val effectiveLen = indexed.effectiveLen
        var bestRank: SearchMatchRank? = null

        fun considerRank(candidate: SearchMatchRank?) {
            if (candidate == null) return
            if (bestRank == null || candidate < bestRank!!) {
                bestRank = candidate
            }
        }

        fun evaluateIndexedTarget(
            target: IndexedSearchTarget?,
            exactTier: Int,
            prefixTier: Int,
            substringTier: Int
        ): SearchMatchRank? {
            if (target == null) return null
            val lower = target.lower
            val targetLen = target.targetLen

            // 1. Direct case-insensitive check
            if (lower == q) {
                return SearchMatchRank(exactTier, effectiveLen, targetLen, 0)
            }
            if (lower.startsWith(q)) {
                return SearchMatchRank(prefixTier, effectiveLen, targetLen, 0)
            }

            // 2. Normalized German check (ä->a, ö->o, ü->u, ß->ss)
            if (qNormGerman.isNotBlank()) {
                val norm = target.normGerman
                if (norm == qNormGerman) {
                    return SearchMatchRank(exactTier, effectiveLen, targetLen, 0)
                }
                if (norm.startsWith(qNormGerman)) {
                    return SearchMatchRank(prefixTier, effectiveLen, targetLen, 0)
                }
            }

            // 3. Normalized Arabic check (strips Tashkeel/diacritics)
            if ((isArabicQuery || target.hasArabic) && qNormArabic.isNotBlank() && target.normArabic.isNotBlank()) {
                val normAr = target.normArabic
                if (normAr == qNormArabic) {
                    return SearchMatchRank(exactTier, effectiveLen, targetLen, 0)
                }
                if (normAr.startsWith(qNormArabic)) {
                    return SearchMatchRank(prefixTier, effectiveLen, targetLen, 0)
                }
                val arIdx = normAr.indexOf(qNormArabic)
                if (arIdx >= 0) {
                    return SearchMatchRank(substringTier, effectiveLen, targetLen, arIdx)
                }
            }

            // 4. Substring check (direct or normalized German)
            val directIdx = lower.indexOf(q)
            if (directIdx >= 0) {
                return SearchMatchRank(substringTier, effectiveLen, targetLen, directIdx)
            }
            if (qNormGerman.isNotBlank()) {
                val normIdx = target.normGerman.indexOf(qNormGerman)
                if (normIdx >= 0) {
                    return SearchMatchRank(substringTier, effectiveLen, targetLen, normIdx)
                }
            }

            return null
        }

        val isSingleWordEntry = indexed.isSingleWordEntry
        val basePrefixTier = if (isSingleWordEntry) 2 else 4

        // A. Base German word (without leading article) and raw word
        considerRank(evaluateIndexedTarget(indexed.baseTarget, exactTier = 0, prefixTier = basePrefixTier, substringTier = 6))
        considerRank(evaluateIndexedTarget(indexed.rawTarget, exactTier = 0, prefixTier = basePrefixTier, substringTier = 6))

        // B. Full German phrase with article
        if (indexed.articleTarget != null) {
            val articlePrefixTier = if (isSingleWordEntry) 3 else 4
            considerRank(evaluateIndexedTarget(indexed.articleTarget, exactTier = 1, prefixTier = articlePrefixTier, substringTier = 6))
        }

        // C. Explicit article query match ("der", "die", "das")
        if ((q == "der" || q == "die" || q == "das") && indexed.article.equals(q, ignoreCase = true)) {
            considerRank(SearchMatchRank(tier = 4, effectiveWordLength = effectiveLen, matchedTargetLength = effectiveLen, matchIndex = 0))
        }

        // D. Individual items in comma/semicolon-separated word lists
        for ((cleanTokenTarget, rawTokenTarget) in indexed.listTokenTargets) {
            considerRank(evaluateIndexedTarget(cleanTokenTarget, exactTier = 2, prefixTier = 4, substringTier = 6))
            considerRank(evaluateIndexedTarget(rawTokenTarget, exactTier = 2, prefixTier = 4, substringTier = 6))
        }

        // E. Word-boundary tokens inside multi-word German phrases/sentences
        indexed.phraseWordTargets.forEachIndexed { idx, tokenTarget ->
            val rank = evaluateIndexedTarget(tokenTarget, exactTier = 4, prefixTier = 5, substringTier = 6)
            if (rank != null) {
                considerRank(rank.copy(matchIndex = rank.matchIndex + idx))
            }
        }

        // F. AI Translation match
        if (indexed.cleanedTranslationTarget != null) {
            val transExactTier = if (isSingleWordEntry) 1 else 3
            val transPrefixTier = if (isSingleWordEntry) 3 else 5
            considerRank(evaluateIndexedTarget(indexed.cleanedTranslationTarget, exactTier = transExactTier, prefixTier = transPrefixTier, substringTier = 6))

            val useStrippedTo = q != "to" && !q.startsWith("to ")
            indexed.translationTokenTargets.forEachIndexed { idx, (strippedToTarget, rawTokenTarget) ->
                if (useStrippedTo) {
                    val r1 = evaluateIndexedTarget(strippedToTarget, exactTier = transExactTier, prefixTier = transPrefixTier, substringTier = 6)
                    if (r1 != null) considerRank(r1.copy(matchIndex = r1.matchIndex + idx))
                }
                val r2 = evaluateIndexedTarget(rawTokenTarget, exactTier = transExactTier, prefixTier = transPrefixTier, substringTier = 6)
                if (r2 != null) considerRank(r2.copy(matchIndex = r2.matchIndex + idx))
            }
        }

        // G. Manual translation match
        if (indexed.manualEntryTargets.isNotEmpty()) {
            val manualExactTier = if (isSingleWordEntry) 1 else 3
            val manualPrefixTier = if (isSingleWordEntry) 3 else 5
            indexed.manualEntryTargets.forEachIndexed { idx, (entryTarget, subTokenTargets) ->
                val r = evaluateIndexedTarget(entryTarget, exactTier = manualExactTier, prefixTier = manualPrefixTier, substringTier = 6)
                if (r != null) considerRank(r.copy(matchIndex = r.matchIndex + idx))
                for (subTarget in subTokenTargets) {
                    val sr = evaluateIndexedTarget(subTarget, exactTier = manualExactTier, prefixTier = manualPrefixTier, substringTier = 6)
                    if (sr != null) considerRank(sr.copy(matchIndex = sr.matchIndex + idx))
                }
            }
        }

        return bestRank
    }

    /**
     * Determines whether [item] matches [query] and actually exists in the log.
     */
    fun matchesSearchPrefix(item: WordHistoryEntity, query: String): Boolean {
        if (!isValidLogEntry(item)) return false
        if (query.isBlank()) return true
        return computeSearchCloseness(item, query) != null
    }

    /**
     * Filters [items] by category tab and search query.
     * When [query] is non-blank, only items that actually exist and match [query] are returned,
     * ordered with the closest matches first (exact matches prioritized over long phrases).
     *
     * filterIndex:
     * 0 -> Alle (All)
     * 1 -> Nomen (Nouns)
     * 2 -> Verben (Verbs)
     * 3 -> Sätze (Sentences)
     * 4 -> Wörter (Words / Lists)
     * 5 -> Adjektive (Adjectives)
     */
    fun filterHistory(
        items: List<WordHistoryEntity>,
        filterIndex: Int,
        query: String
    ): List<WordHistoryEntity> {
        val validItems = items.filter { isValidLogEntry(it) }
        val categoryFiltered = when (filterIndex) {
            1 -> validItems.filter { getIndexedItem(it).category == HistoryItemCategory.NOUN }
            2 -> validItems.filter { getIndexedItem(it).category == HistoryItemCategory.VERB }
            3 -> validItems.filter { getIndexedItem(it).category == HistoryItemCategory.SENTENCE }
            4 -> validItems.filter { getIndexedItem(it).category == HistoryItemCategory.NOMEN_LIST }
            5 -> validItems.filter { getIndexedItem(it).category == HistoryItemCategory.ADJEKTIV }
            else -> validItems
        }

        if (query.isBlank()) {
            return categoryFiltered
        }

        val q = query.trim()
            .trimStart(*TRIM_START_CHARS)
            .trimEnd(*TRIM_END_CHARS)
            .lowercase(Locale.GERMAN)
        if (q.isBlank()) {
            return categoryFiltered
        }
        val qNormGerman = normalizeGerman(q)
        val qNormArabic = normalizeArabic(q).lowercase(Locale.ROOT)
        val isArabicQuery = isArabicText(q)

        return categoryFiltered
            .mapNotNull { item ->
                val rank = computeSearchClosenessPreNormalized(
                    indexed = getIndexedItem(item),
                    q = q,
                    qNormGerman = qNormGerman,
                    qNormArabic = qNormArabic,
                    isArabicQuery = isArabicQuery
                )
                if (rank != null) item to rank else null
            }
            .sortedBy { it.second }
            .map { it.first }
    }

    private fun safeAbsDiff(a: Long, b: Long): Long {
        val diff = a - b
        return if (diff == Long.MIN_VALUE) Long.MAX_VALUE else kotlin.math.abs(diff)
    }

    /**
     * Builds the comparator for [HistorySortOption].
     */
    private fun buildSortComparator(sortOption: HistorySortOption): Comparator<WordHistoryEntity> {
        val baseComparator: Comparator<WordHistoryEntity> = when (sortOption.field) {
            HistorySortField.DATE -> {
                if (sortOption.dateSortMode == DateSortMode.MANUAL && sortOption.manualDateMillis != null) {
                    val target = sortOption.manualDateMillis
                    compareBy<WordHistoryEntity> {
                        safeAbsDiff(it.timestamp, target)
                    }.thenByDescending { it.timestamp }.thenBy { it.id }
                } else {
                    compareBy<WordHistoryEntity> { it.timestamp }.thenBy { it.id }
                }
            }
            HistorySortField.NAME -> {
                compareBy<WordHistoryEntity> {
                    getIndexedItem(it).effectiveSortKeyLower
                }.thenBy {
                    getIndexedItem(it).rawWordLower
                }.thenBy { it.id }
            }
            HistorySortField.GENDER -> {
                when (sortOption.genderTarget) {
                    GenderSortTarget.MASKULIN -> {
                        compareBy<WordHistoryEntity> {
                            val g = getIndexedItem(it).genderLower
                            if (g == "maskulin" || g == "der") 0 else 1
                        }.thenBy {
                            getIndexedItem(it).effectiveSortKeyLower
                        }.thenBy { it.id }
                    }
                    GenderSortTarget.FEMININ -> {
                        compareBy<WordHistoryEntity> {
                            val g = getIndexedItem(it).genderLower
                            if (g == "feminin" || g == "die") 0 else 1
                        }.thenBy {
                            getIndexedItem(it).effectiveSortKeyLower
                        }.thenBy { it.id }
                    }
                    GenderSortTarget.NEUTRUM -> {
                        compareBy<WordHistoryEntity> {
                            val g = getIndexedItem(it).genderLower
                            if (g == "neutrum" || g == "das") 0 else 1
                        }.thenBy {
                            getIndexedItem(it).effectiveSortKeyLower
                        }.thenBy { it.id }
                    }
                    GenderSortTarget.ALL -> {
                        compareBy<WordHistoryEntity> {
                            val g = getIndexedItem(it).genderLower
                            when {
                                g == "maskulin" || g == "der" -> 1
                                g == "feminin" || g == "die" -> 2
                                g == "neutrum" || g == "das" -> 3
                                g == "plural" || g == "pl" -> 4
                                else -> 5
                            }
                        }.thenBy {
                            getIndexedItem(it).effectiveSortKeyLower
                        }.thenBy { it.id }
                    }
                }
            }
            HistorySortField.TYPE -> {
                compareBy<WordHistoryEntity> {
                    getIndexedItem(it).category.sortOrder
                }.thenBy {
                    getIndexedItem(it).effectiveSortKeyLower
                }.thenBy { it.id }
            }
        }

        return if (sortOption.direction == SortDirection.ASCENDING) {
            baseComparator
        } else {
            baseComparator.reversed()
        }
    }

    /**
     * Sorts [items] according to the selected [HistorySortOption].
     */
    fun sortHistory(
        items: List<WordHistoryEntity>,
        sortOption: HistorySortOption
    ): List<WordHistoryEntity> {
        return items.sortedWith(buildSortComparator(sortOption))
    }

    /**
     * Filters and sorts history items atomically.
     * When [query] is non-blank, closeness to the search query is the primary sort key
     * (so exact matches appear first before long phrases), and [sortOption] is used as the tie-breaker.
     */
    fun filterAndSortHistory(
        items: List<WordHistoryEntity>,
        filterIndex: Int,
        query: String,
        sortOption: HistorySortOption = HistorySortOption()
    ): List<WordHistoryEntity> {
        val validItems = items.filter { isValidLogEntry(it) }
        val categoryFiltered = when (filterIndex) {
            1 -> validItems.filter { getIndexedItem(it).category == HistoryItemCategory.NOUN }
            2 -> validItems.filter { getIndexedItem(it).category == HistoryItemCategory.VERB }
            3 -> validItems.filter { getIndexedItem(it).category == HistoryItemCategory.SENTENCE }
            4 -> validItems.filter { getIndexedItem(it).category == HistoryItemCategory.NOMEN_LIST }
            5 -> validItems.filter { getIndexedItem(it).category == HistoryItemCategory.ADJEKTIV }
            else -> validItems
        }

        if (query.isBlank()) {
            return sortHistory(categoryFiltered, sortOption)
        }

        val q = query.trim()
            .trimStart(*TRIM_START_CHARS)
            .trimEnd(*TRIM_END_CHARS)
            .lowercase(Locale.GERMAN)
        if (q.isBlank()) {
            return sortHistory(categoryFiltered, sortOption)
        }
        val qNormGerman = normalizeGerman(q)
        val qNormArabic = normalizeArabic(q).lowercase(Locale.ROOT)
        val isArabicQuery = isArabicText(q)

        val tieBreaker = buildSortComparator(sortOption)
        return categoryFiltered
            .mapNotNull { item ->
                val rank = computeSearchClosenessPreNormalized(
                    indexed = getIndexedItem(item),
                    q = q,
                    qNormGerman = qNormGerman,
                    qNormArabic = qNormArabic,
                    isArabicQuery = isArabicQuery
                )
                if (rank != null) item to rank else null
            }
            .sortedWith { a, b ->
                val rankCmp = a.second.compareTo(b.second)
                if (rankCmp != 0) rankCmp else tieBreaker.compare(a.first, b.first)
            }
            .map { it.first }
    }
}
