package com.example.data.repository

import com.example.data.model.GrammarReferenceTable
import com.example.data.model.GrammarTableCategory
import com.example.data.model.GrammarTablesData
import com.example.data.model.PrepositionGroup
import com.example.data.model.PronunciationRule
import com.example.data.model.TwoWayPrepositionItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * High-performance, in-memory repository for German Grammar Tables.
 * Pre-warms and caches all grammar data, localized strings, and search indexes in RAM.
 * Preserves tab states and per-tab scroll positions for instant ≤200ms re-opening.
 */
object GrammarTablesRepository {

    @Volatile
    private var isWarmed = false

    // Cached references in RAM
    val categories: List<GrammarTableCategory> get() = GrammarTablesData.CATEGORIES
    val fixedPrepositionGroups: List<PrepositionGroup> get() = GrammarTablesData.FIXED_PREPOSITION_GROUPS
    val twoWayPrepositions: List<TwoWayPrepositionItem> get() = GrammarTablesData.TWO_WAY_PREPOSITIONS
    val pronunciationGuide: List<PronunciationRule> get() = GrammarTablesData.PRONUNCIATION_GUIDE

    // In-Memory Tab State & Scroll Preservation
    var lastSelectedTabIndex: Int = 0
    private val tabScrollPositions = mutableMapOf<Int, Pair<Int, Int>>() // tabIndex -> Pair(firstVisibleIndex, scrollOffset)

    // Pre-calculated search index maps for sub-millisecond filtering
    private val tableSearchIndex = mutableMapOf<String, String>()
    private val ruleSearchIndex = mutableMapOf<String, String>()

    /**
     * Asynchronously pre-warms all grammar tables, strings, and search tokens on a background thread.
     */
    fun warmCache(scope: CoroutineScope) {
        if (isWarmed) return
        scope.launch(Dispatchers.Default) {
            preloadAndIndexData()
            isWarmed = true
        }
    }

    private fun preloadAndIndexData() {
        // Pre-index categories and tables for instant zero-latency filtering
        categories.forEach { category ->
            category.tables.forEach { table ->
                val sb = StringBuilder()
                sb.append(table.titleDe).append(" ").append(table.titleEn).append(" ").append(table.titleAr).append(" ")
                sb.append(table.subtitleDe).append(" ").append(table.subtitleEn).append(" ").append(table.subtitleAr).append(" ")
                table.rows.forEach { row ->
                    sb.append(row.label).append(" ").append(row.labelEn).append(" ").append(row.labelAr).append(" ")
                    row.cells.forEach { sb.append(it).append(" ") }
                }
                table.examples.forEach { ex ->
                    sb.append(ex.german).append(" ").append(ex.translation).append(" ").append(ex.translationAr).append(" ")
                }
                tableSearchIndex[table.id] = sb.toString().lowercase()
            }
        }

        pronunciationGuide.forEach { rule ->
            val sb = StringBuilder()
            sb.append(rule.soundLetter).append(" ").append(rule.ipa).append(" ")
            sb.append(rule.explanationDe).append(" ").append(rule.explanationEn).append(" ").append(rule.explanationAr).append(" ")
            rule.exampleWords.forEach { sb.append(it).append(" ") }
            ruleSearchIndex[rule.soundLetter] = sb.toString().lowercase()
        }
    }

    fun saveScrollPosition(tabIndex: Int, firstVisibleIndex: Int, scrollOffset: Int) {
        tabScrollPositions[tabIndex] = Pair(firstVisibleIndex, scrollOffset)
    }

    fun getScrollPosition(tabIndex: Int): Pair<Int, Int>? {
        return tabScrollPositions[tabIndex]
    }

    fun filterTables(category: GrammarTableCategory, query: String): List<GrammarReferenceTable> {
        val q = query.trim().lowercase()
        if (q.isBlank()) return category.tables

        return category.tables.filter { table ->
            val indexStr = tableSearchIndex[table.id]
            if (indexStr != null) {
                indexStr.contains(q)
            } else {
                table.titleDe.contains(q, ignoreCase = true) ||
                table.titleEn.contains(q, ignoreCase = true) ||
                table.rows.any { row ->
                    row.label.contains(q, ignoreCase = true) ||
                    row.cells.any { cell -> cell.contains(q, ignoreCase = true) }
                }
            }
        }
    }

    fun filterPronunciationRules(query: String): List<PronunciationRule> {
        val q = query.trim().lowercase()
        if (q.isBlank()) return pronunciationGuide

        return pronunciationGuide.filter { rule ->
            val indexStr = ruleSearchIndex[rule.soundLetter]
            if (indexStr != null) {
                indexStr.contains(q)
            } else {
                rule.soundLetter.contains(q, ignoreCase = true) ||
                rule.explanationDe.contains(q, ignoreCase = true) ||
                rule.exampleWords.any { word -> word.contains(q, ignoreCase = true) }
            }
        }
    }
}
