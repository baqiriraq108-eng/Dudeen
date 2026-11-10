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

/**
 * Helper object providing robust classification, closeness-ranked search logic,
 * and independent multi-criteria sorting for history items.
 */
object HistoryFilterHelper {

    /**
     * Ensures a history log entry actually contains a valid, non-empty term.
     */
    fun isValidLogEntry(item: WordHistoryEntity): Boolean {
        val raw = item.word.trim()
        return raw.isNotBlank() && !raw.equals("null", ignoreCase = true)
    }

    fun isAdjektivItem(item: WordHistoryEntity): Boolean {
        val t = item.type.lowercase(Locale.ROOT)
        val g = item.gender.lowercase(Locale.ROOT)
        return t == "adjektiv" || t == "adjektive" || t == "adjective" ||
               g == "adjektiv" || g == "adjektive" ||
               item.rawJsonResult.contains("\"Adjektiv\"") ||
               (item.rawJsonResult.contains("\"komparativ\"") && item.rawJsonResult.contains("\"superlativ\""))
    }

    fun isVerbItem(item: WordHistoryEntity): Boolean {
        if (isAdjektivItem(item)) return false
        return item.type.equals("Verb", ignoreCase = true) ||
               item.type.equals("Verben", ignoreCase = true) ||
               item.rawJsonResult.contains("\"infinitiv\"")
    }

    fun isNomenListItem(item: WordHistoryEntity): Boolean {
        if (isAdjektivItem(item) || isVerbItem(item)) return false
        val t = item.type.lowercase(Locale.ROOT)
        val g = item.gender.lowercase(Locale.ROOT)
        return t == "wörter" || t == "woerter" || t == "nomenlist" || t == "nomen (liste)" ||
               g == "wort" || g == "wörter" || g == "woerter" ||
               item.rawJsonResult.contains("\"Wörter\"") || item.rawJsonResult.contains("\"Woerter\"") ||
               item.rawJsonResult.contains("\"NomenList\"")
    }

    fun isSentenceItem(item: WordHistoryEntity): Boolean {
        if (isAdjektivItem(item) || isVerbItem(item) || isNomenListItem(item)) return false
        val t = item.type.lowercase(Locale.ROOT)
        val g = item.gender.lowercase(Locale.ROOT)
        return t == "satz" || t == "sentence" || t == "sätze" || t == "saetze" ||
               g == "satz" || g == "–" ||
               item.rawJsonResult.contains("\"Satz\"") || item.rawJsonResult.contains("\"Sentence\"") ||
               (item.word.trim().split(Regex("[\\s,;]+")).filter { it.isNotBlank() }.size >= 3 &&
                g != "maskulin" && g != "feminin" && g != "neutrum")
    }

    fun isNounItem(item: WordHistoryEntity): Boolean {
        return !isAdjektivItem(item) && !isVerbItem(item) && !isNomenListItem(item) && !isSentenceItem(item)
    }

    fun normalizeGerman(s: String): String {
        return s.lowercase(Locale.GERMAN)
            .replace("ä", "a")
            .replace("ö", "o")
            .replace("ü", "u")
            .replace("ß", "ss")
    }

    /**
     * Extracts the effective word for sorting, stripping leading definite/indefinite
     * articles (der/die/das/dem/den/des/ein/eine/einen/einem/einer/eines) and reflexive pronouns (sich).
     */
    fun getEffectiveWordForLetterSort(item: WordHistoryEntity): String {
        val raw = item.word.trim().trimStart('"', '„', '“', '\'', '«', '»', '-', '–', '(', '[', '{', ' ')
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
        return withoutArticle.trimStart('"', '„', '“', '\'', '«', '»', '-', '–', '(', '[', '{', ' ')
    }

    /**
     * Computes how closely [item] matches [query] across German words, phrases, and translations.
     * Returns null if [item] is invalid/empty or does not match [query] at all.
     * Prioritizes exact matches and shorter terms over long phrases or sentences.
     */
    fun computeSearchCloseness(item: WordHistoryEntity, query: String): SearchMatchRank? {
        if (!isValidLogEntry(item)) return null

        val q = query.trim()
            .trimStart('"', '„', '“', '\'', '«', '»', '-', '–', '(', '[', '{', ' ')
            .trimEnd('"', '„', '“', '\'', '«', '»', '!', '?', '.', ',', ';', ')', ']', '}', ' ')
            .lowercase(Locale.GERMAN)
        if (q.isBlank()) return SearchMatchRank(0, 0, 0, 0)

        val qNormGerman = normalizeGerman(q)
        val qNormArabic = normalizeArabic(q).lowercase(Locale.ROOT)
        val isArabicQuery = isArabicText(q)

        val rawWord = item.word.trim()
        val wordWithoutArticle = getEffectiveWordForLetterSort(item)
        val effectiveLen = wordWithoutArticle.length.coerceAtLeast(1)
        val article = item.genderArticle.trim().takeUnless { it.equals("null", ignoreCase = true) }.orEmpty()

        var bestRank: SearchMatchRank? = null

        fun considerRank(candidate: SearchMatchRank?) {
            if (candidate == null) return
            if (bestRank == null || candidate < bestRank!!) {
                bestRank = candidate
            }
        }

        fun cleanTarget(text: String): String {
            return text.trim()
                .trimStart('"', '„', '“', '\'', '«', '»', '-', '–', '(', '[', '{', ' ')
                .trimEnd('"', '„', '“', '\'', '«', '»', '!', '?', '.', ',', ';', ')', ']', '}', ' ')
        }

        fun evaluateTarget(
            text: String,
            exactTier: Int,
            prefixTier: Int,
            substringTier: Int
        ): SearchMatchRank? {
            val clean = cleanTarget(text)
            if (clean.isBlank()) return null

            val lower = clean.lowercase(Locale.GERMAN)
            val targetLen = clean.length

            // 1. Direct case-insensitive check
            if (lower == q) {
                return SearchMatchRank(exactTier, effectiveLen, targetLen, 0)
            }
            if (lower.startsWith(q)) {
                return SearchMatchRank(prefixTier, effectiveLen, targetLen, 0)
            }

            // 2. Normalized German check (ä->a, ö->o, ü->u, ß->ss)
            if (qNormGerman.isNotBlank()) {
                val norm = normalizeGerman(clean)
                if (norm == qNormGerman) {
                    return SearchMatchRank(exactTier, effectiveLen, targetLen, 0)
                }
                if (norm.startsWith(qNormGerman)) {
                    return SearchMatchRank(prefixTier, effectiveLen, targetLen, 0)
                }
            }

            // 3. Normalized Arabic check (strips Tashkeel/diacritics)
            if (isArabicQuery || isArabicText(clean)) {
                val normAr = normalizeArabic(clean).lowercase(Locale.ROOT)
                if (qNormArabic.isNotBlank()) {
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
            }

            // 4. Substring check (direct or normalized German)
            val directIdx = lower.indexOf(q)
            if (directIdx >= 0) {
                return SearchMatchRank(substringTier, effectiveLen, targetLen, directIdx)
            }
            if (qNormGerman.isNotBlank()) {
                val norm = normalizeGerman(clean)
                val normIdx = norm.indexOf(qNormGerman)
                if (normIdx >= 0) {
                    return SearchMatchRank(substringTier, effectiveLen, targetLen, normIdx)
                }
            }

            return null
        }

        val isSingleWordEntry = !wordWithoutArticle.contains(' ') && !isSentenceItem(item)
        val basePrefixTier = if (isSingleWordEntry) 2 else 4

        // A. Base German word (without leading article) and raw word
        considerRank(evaluateTarget(wordWithoutArticle, exactTier = 0, prefixTier = basePrefixTier, substringTier = 6))
        considerRank(evaluateTarget(rawWord, exactTier = 0, prefixTier = basePrefixTier, substringTier = 6))

        // B. Full German phrase with article
        if (article.isNotBlank()) {
            val fullWithArticle = if (!rawWord.startsWith("$article ", ignoreCase = true)) {
                "$article $rawWord"
            } else {
                rawWord
            }
            val articlePrefixTier = if (isSingleWordEntry) 3 else 4
            considerRank(evaluateTarget(fullWithArticle, exactTier = 1, prefixTier = articlePrefixTier, substringTier = 6))
        }

        // C. Explicit article query match ("der", "die", "das")
        if ((q == "der" || q == "die" || q == "das") && article.equals(q, ignoreCase = true)) {
            considerRank(SearchMatchRank(tier = 4, effectiveWordLength = effectiveLen, matchedTargetLength = effectiveLen, matchIndex = 0))
        }

        // D. Individual items in comma/semicolon-separated word lists
        if (rawWord.contains(",") || rawWord.contains(";") || rawWord.contains("،")) {
            val tokens = rawWord.split(Regex("[,;،]+")).map { it.trim() }.filter { it.isNotBlank() }
            tokens.forEach { token ->
                val cleanToken = when {
                    token.startsWith("der ", ignoreCase = true) ||
                    token.startsWith("die ", ignoreCase = true) ||
                    token.startsWith("das ", ignoreCase = true) -> token.substring(4).trim()
                    token.startsWith("ein ", ignoreCase = true) -> token.substring(4).trim()
                    token.startsWith("eine ", ignoreCase = true) -> token.substring(5).trim()
                    else -> token
                }
                considerRank(evaluateTarget(cleanToken, exactTier = 2, prefixTier = 4, substringTier = 6))
                considerRank(evaluateTarget(token, exactTier = 2, prefixTier = 4, substringTier = 6))
            }
        }

        // E. Word-boundary tokens inside multi-word German phrases/sentences
        val phraseWords = wordWithoutArticle.split(Regex("[\\s/,;،.!?()\\[\\]\"„“'«»–-]+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
        if (phraseWords.size > 1) {
            phraseWords.forEachIndexed { idx, token ->
                val rank = evaluateTarget(token, exactTier = 4, prefixTier = 5, substringTier = 6)
                if (rank != null) {
                    // Add word position offset so matches on the first word of a phrase outrank later words
                    considerRank(rank.copy(matchIndex = rank.matchIndex + idx))
                }
            }
        }

        // F. AI Translation match (cleaned of "Eng.:" / Arabic label prefixes)
        val rawTranslation = item.meaningEnglish.trim()
        if (rawTranslation.isNotBlank()) {
            val cleanedTranslation = cleanTranslationText(rawTranslation, isArabicText(rawTranslation))
            val transExactTier = if (isSingleWordEntry) 1 else 3
            val transPrefixTier = if (isSingleWordEntry) 3 else 5
            considerRank(evaluateTarget(cleanedTranslation, exactTier = transExactTier, prefixTier = transPrefixTier, substringTier = 6))

            val transTokens = cleanedTranslation.split(Regex("[,;/،\n()]+"))
                .map { it.trim() }
                .filter { it.isNotBlank() }
            transTokens.forEachIndexed { idx, token ->
                val cleanToken = if (token.startsWith("to ", ignoreCase = true) && q != "to" && !q.startsWith("to ")) {
                    token.substring(3).trim()
                } else {
                    token
                }
                val r1 = evaluateTarget(cleanToken, exactTier = transExactTier, prefixTier = transPrefixTier, substringTier = 6)
                if (r1 != null) considerRank(r1.copy(matchIndex = r1.matchIndex + idx))
                val r2 = evaluateTarget(token, exactTier = transExactTier, prefixTier = transPrefixTier, substringTier = 6)
                if (r2 != null) considerRank(r2.copy(matchIndex = r2.matchIndex + idx))
            }
        }

        // G. Manual translation match (cleaned via parseManualTranslations so "Alt.:" is never falsely matched)
        val manual = item.manualTranslation.trim()
        if (manual.isNotBlank()) {
            val manualExactTier = if (isSingleWordEntry) 1 else 3
            val manualPrefixTier = if (isSingleWordEntry) 3 else 5
            val manualItems = parseManualTranslations(manual)
            manualItems.forEachIndexed { idx, manualEntry ->
                val r = evaluateTarget(manualEntry, exactTier = manualExactTier, prefixTier = manualPrefixTier, substringTier = 6)
                if (r != null) considerRank(r.copy(matchIndex = r.matchIndex + idx))
                val subTokens = manualEntry.split(Regex("[,;/،]+")).map { it.trim() }.filter { it.isNotBlank() }
                subTokens.forEach { subToken ->
                    val sr = evaluateTarget(subToken, exactTier = manualExactTier, prefixTier = manualPrefixTier, substringTier = 6)
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
            1 -> validItems.filter { isNounItem(it) }
            2 -> validItems.filter { isVerbItem(it) }
            3 -> validItems.filter { isSentenceItem(it) }
            4 -> validItems.filter { isNomenListItem(it) }
            5 -> validItems.filter { isAdjektivItem(it) }
            else -> validItems
        }

        if (query.isBlank()) {
            return categoryFiltered
        }

        return categoryFiltered
            .mapNotNull { item ->
                val rank = computeSearchCloseness(item, query)
                if (rank != null) item to rank else null
            }
            .sortedBy { it.second }
            .map { it.first }
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
                        kotlin.math.abs(it.timestamp - target)
                    }.thenByDescending { it.timestamp }
                } else {
                    compareBy { it.timestamp }
                }
            }
            HistorySortField.NAME -> {
                compareBy<WordHistoryEntity> {
                    getEffectiveWordForLetterSort(it).lowercase(Locale.GERMAN)
                }.thenBy { it.word.lowercase(Locale.GERMAN) }
            }
            HistorySortField.GENDER -> {
                when (sortOption.genderTarget) {
                    GenderSortTarget.MASKULIN -> {
                        compareBy<WordHistoryEntity> {
                            val g = it.gender.lowercase(Locale.ROOT)
                            if (g == "maskulin" || g == "der") 0 else 1
                        }.thenBy {
                            getEffectiveWordForLetterSort(it).lowercase(Locale.GERMAN)
                        }
                    }
                    GenderSortTarget.FEMININ -> {
                        compareBy<WordHistoryEntity> {
                            val g = it.gender.lowercase(Locale.ROOT)
                            if (g == "feminin" || g == "die") 0 else 1
                        }.thenBy {
                            getEffectiveWordForLetterSort(it).lowercase(Locale.GERMAN)
                        }
                    }
                    GenderSortTarget.NEUTRUM -> {
                        compareBy<WordHistoryEntity> {
                            val g = it.gender.lowercase(Locale.ROOT)
                            if (g == "neutrum" || g == "das") 0 else 1
                        }.thenBy {
                            getEffectiveWordForLetterSort(it).lowercase(Locale.GERMAN)
                        }
                    }
                    GenderSortTarget.ALL -> {
                        compareBy<WordHistoryEntity> {
                            val g = it.gender.lowercase(Locale.ROOT)
                            when {
                                g == "maskulin" || g == "der" -> 1
                                g == "feminin" || g == "die" -> 2
                                g == "neutrum" || g == "das" -> 3
                                g == "plural" || g == "pl" -> 4
                                else -> 5
                            }
                        }.thenBy {
                            getEffectiveWordForLetterSort(it).lowercase(Locale.GERMAN)
                        }
                    }
                }
            }
            HistorySortField.TYPE -> {
                compareBy<WordHistoryEntity> {
                    when {
                        isNounItem(it) -> 1
                        isVerbItem(it) -> 2
                        isSentenceItem(it) -> 3
                        isNomenListItem(it) -> 4
                        isAdjektivItem(it) -> 5
                        else -> 6
                    }
                }.thenBy {
                    getEffectiveWordForLetterSort(it).lowercase(Locale.GERMAN)
                }
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
            1 -> validItems.filter { isNounItem(it) }
            2 -> validItems.filter { isVerbItem(it) }
            3 -> validItems.filter { isSentenceItem(it) }
            4 -> validItems.filter { isNomenListItem(it) }
            5 -> validItems.filter { isAdjektivItem(it) }
            else -> validItems
        }

        if (query.isBlank()) {
            return sortHistory(categoryFiltered, sortOption)
        }

        val tieBreaker = buildSortComparator(sortOption)
        return categoryFiltered
            .mapNotNull { item ->
                val rank = computeSearchCloseness(item, query)
                if (rank != null) item to rank else null
            }
            .sortedWith { a, b ->
                val rankCmp = a.second.compareTo(b.second)
                if (rankCmp != 0) rankCmp else tieBreaker.compare(a.first, b.first)
            }
            .map { it.first }
    }
}
