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
    TYPE("Typ", "Typ")
}

/**
 * Mode for date sorting.
 */
enum class DateSortMode(val labelDe: String) {
    AUTOMATIC("Automatisch"),
    MANUAL("Manuelles Datum")
}

/**
 * Target gender when Genus sorting is active.
 */
enum class GenderSortTarget(val labelDe: String, val article: String) {
    ALL("Alle Genus", "Alle"),
    MASKULIN("Maskulin (der)", "der"),
    FEMININ("Feminin (die)", "die"),
    NEUTRUM("Neutrum (das)", "das")
}

/**
 * Sort direction.
 */
enum class SortDirection(val labelDe: String, val shortLabel: String) {
    ASCENDING("Aufsteigend", "Auf"),
    DESCENDING("Absteigend", "Ab")
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
 * Helper object providing robust classification, prefix-matching search logic,
 * and independent multi-criteria sorting for history items.
 */
object HistoryFilterHelper {

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
     * Determines whether [item] matches [query] based on prefix or substring match.
     */
    fun matchesSearchPrefix(item: WordHistoryEntity, query: String): Boolean {
        val q = query.trim().lowercase(Locale.GERMAN)
        if (q.isBlank()) return true

        val qNormGerman = normalizeGerman(q)
        val qNormArabic = normalizeArabic(q).lowercase(Locale.ROOT)
        val isArabicQuery = isArabicText(q)
        val isSingleLetter = q.length == 1

        fun checkMatch(text: String): Boolean {
            if (text.isBlank()) return false
            val clean = text.trim().trimStart('"', '„', '“', '\'', '«', '»', '-', '–', '(', '[', '{', ' ')
            val lower = clean.lowercase(Locale.GERMAN)
            if (lower.contains(q)) return true

            if (qNormGerman.isNotBlank()) {
                val norm = normalizeGerman(clean)
                if (norm.contains(qNormGerman)) return true
            }

            if (isArabicQuery || isArabicText(clean)) {
                val normAr = normalizeArabic(clean).lowercase(Locale.ROOT)
                if (normAr.contains(qNormArabic)) return true
            }

            return false
        }

        val rawWord = item.word.trim()
        val article = item.genderArticle.trim()

        // 1. Direct word match
        if (checkMatch(rawWord)) return true

        // 2. Word without leading articles or reflexive pronoun
        val wordWithoutArticle = getEffectiveWordForLetterSort(item)
        if (checkMatch(wordWithoutArticle)) return true

        // 3. Full phrase with article
        if (article.isNotBlank()) {
            val fullWithArticle = if (!rawWord.startsWith(article, ignoreCase = true)) {
                "$article $rawWord"
            } else {
                rawWord
            }
            if (checkMatch(fullWithArticle)) return true
        }

        // 4. For list items (comma/semicolon/arabic comma separated), check if any individual token matches
        if (rawWord.contains(",") || rawWord.contains(";") || rawWord.contains("،")) {
            val tokens = rawWord.split(Regex("[,;،]+")).map { it.trim() }.filter { it.isNotBlank() }
            if (tokens.any { token ->
                val cleanToken = when {
                    token.startsWith("der ", ignoreCase = true) || token.startsWith("die ", ignoreCase = true) || token.startsWith("das ", ignoreCase = true) -> token.substring(4).trim()
                    token.startsWith("ein ", ignoreCase = true) -> token.substring(4).trim()
                    token.startsWith("eine ", ignoreCase = true) -> token.substring(5).trim()
                    else -> token
                }
                checkMatch(token) || checkMatch(cleanToken)
            }) {
                return true
            }
        }

        // 5. Translation match (Arabic or English)
        val translation = item.meaningEnglish.trim()
        if (checkMatch(translation)) return true
        if (translation.contains(",") || translation.contains(";") || translation.contains("/") || translation.contains("،") || translation.contains("\n")) {
            val transTokens = translation.split(Regex("[,;/،\n]+")).map { it.trim() }.filter { it.isNotBlank() }
            if (transTokens.any { checkMatch(it) }) return true
        }

        // 6. Manual translation match (whether partial or complete for any manual translation)
        val manual = item.manualTranslation.trim()
        if (manual.isNotBlank()) {
            if (checkMatch(manual)) return true
            val manualItems = parseManualTranslations(manual)
            if (manualItems.any { checkMatch(it) }) return true
        }

        // 7. Explicit article query match ("der", "die", "das")
        if (q == "der" || q == "die" || q == "das") {
            if (article.equals(q, ignoreCase = true)) return true
        }

        return false
    }

    /**
     * Filters [items] by category tab and prefix query.
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
        val categoryFiltered = when (filterIndex) {
            1 -> items.filter { isNounItem(it) }
            2 -> items.filter { isVerbItem(it) }
            3 -> items.filter { isSentenceItem(it) }
            4 -> items.filter { isNomenListItem(it) }
            5 -> items.filter { isAdjektivItem(it) }
            else -> items
        }

        if (query.isBlank()) {
            return categoryFiltered
        }

        return categoryFiltered.filter { matchesSearchPrefix(it, query) }
    }

    /**
     * Sorts [items] according to the selected [HistorySortOption].
     */
    fun sortHistory(
        items: List<WordHistoryEntity>,
        sortOption: HistorySortOption
    ): List<WordHistoryEntity> {
        val comparator: Comparator<WordHistoryEntity> = when (sortOption.field) {
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
            items.sortedWith(comparator)
        } else {
            items.sortedWith(comparator.reversed())
        }
    }

    /**
     * Filters and sorts history items atomically.
     */
    fun filterAndSortHistory(
        items: List<WordHistoryEntity>,
        filterIndex: Int,
        query: String,
        sortOption: HistorySortOption = HistorySortOption()
    ): List<WordHistoryEntity> {
        val filtered = filterHistory(items, filterIndex, query)
        return sortHistory(filtered, sortOption)
    }
}

