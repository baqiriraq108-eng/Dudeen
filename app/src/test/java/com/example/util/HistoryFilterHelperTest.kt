package com.example.util

import com.example.data.local.WordHistoryEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryFilterHelperTest {

    private fun createEntity(
        id: Long = 1L,
        word: String,
        type: String,
        gender: String = "",
        genderArticle: String = "",
        meaning: String = ""
    ) = WordHistoryEntity(
        id = id,
        word = word,
        type = type,
        gender = gender,
        genderArticle = genderArticle,
        meaningEnglish = meaning,
        timestamp = System.currentTimeMillis()
    )

    @Test
    fun testClassification_exactCategories() {
        val noun = createEntity(id = 1, word = "Tisch", type = "Nomen", gender = "Maskulin", genderArticle = "der")
        val verb = createEntity(id = 2, word = "gehen", type = "Verb")
        val sentence = createEntity(id = 3, word = "Wie geht es dir?", type = "Satz", gender = "–")
        val wordList = createEntity(id = 4, word = "Apfel, Birne", type = "Wörter", gender = "Wörter")
        val singleWord = createEntity(id = 5, word = "Auto", type = "Wörter", gender = "Wort")

        assertTrue(HistoryFilterHelper.isNounItem(noun))
        assertFalse(HistoryFilterHelper.isVerbItem(noun))
        assertFalse(HistoryFilterHelper.isSentenceItem(noun))
        assertFalse(HistoryFilterHelper.isNomenListItem(noun))

        assertTrue(HistoryFilterHelper.isVerbItem(verb))
        assertFalse(HistoryFilterHelper.isNounItem(verb))

        assertTrue(HistoryFilterHelper.isSentenceItem(sentence))
        assertFalse(HistoryFilterHelper.isNounItem(sentence))

        assertTrue(HistoryFilterHelper.isNomenListItem(wordList))
        assertTrue(HistoryFilterHelper.isNomenListItem(singleWord))
    }

    @Test
    fun testPrefixMatching_firstLetterOnlyMatchesStarts() {
        val noun = createEntity(id = 1, word = "Tisch", type = "Nomen", genderArticle = "der")
        val verb = createEntity(id = 2, word = "gehen", type = "Verb")
        val sentence = createEntity(id = 3, word = "Wo bin ich?", type = "Satz")

        // First letter 'g': only 'gehen' has 'g'
        assertTrue(HistoryFilterHelper.matchesSearchPrefix(verb, "g"))
        assertFalse(HistoryFilterHelper.matchesSearchPrefix(noun, "g"))
        assertFalse(HistoryFilterHelper.matchesSearchPrefix(sentence, "g"))

        // First letter 't': noun 'Tisch' starts with 't'
        assertTrue(HistoryFilterHelper.matchesSearchPrefix(noun, "t"))
        assertFalse(HistoryFilterHelper.matchesSearchPrefix(verb, "t"))
        assertFalse(HistoryFilterHelper.matchesSearchPrefix(sentence, "t"))

        // First letter 'w': sentence starts with 'w'
        assertTrue(HistoryFilterHelper.matchesSearchPrefix(sentence, "w"))
        assertFalse(HistoryFilterHelper.matchesSearchPrefix(noun, "w"))
        assertFalse(HistoryFilterHelper.matchesSearchPrefix(verb, "w"))
    }

    @Test
    fun testPrefixMatching_progressiveNarrowing() {
        val verb1 = createEntity(id = 1, word = "gehen", type = "Verb")
        val verb2 = createEntity(id = 2, word = "glauben", type = "Verb")

        // Both match 'g'
        assertTrue(HistoryFilterHelper.matchesSearchPrefix(verb1, "g"))
        assertTrue(HistoryFilterHelper.matchesSearchPrefix(verb2, "g"))

        // Narrowing to 'ge'
        assertTrue(HistoryFilterHelper.matchesSearchPrefix(verb1, "ge"))
        assertFalse(HistoryFilterHelper.matchesSearchPrefix(verb2, "ge"))

        // Exact match
        assertTrue(HistoryFilterHelper.matchesSearchPrefix(verb1, "gehen"))
        assertFalse(HistoryFilterHelper.matchesSearchPrefix(verb2, "gehen"))
    }

    @Test
    fun testPrefixMatching_nounsWithOrWithoutArticle() {
        val noun = createEntity(id = 1, word = "Schloss", type = "Nomen", genderArticle = "das")

        // Matches 's', 'sc', 'sch', 'schloss'
        assertTrue(HistoryFilterHelper.matchesSearchPrefix(noun, "s"))
        assertTrue(HistoryFilterHelper.matchesSearchPrefix(noun, "sch"))
        assertTrue(HistoryFilterHelper.matchesSearchPrefix(noun, "schloss"))

        // Also matches with article: 'd', 'das', 'das s', 'das schloss'
        assertTrue(HistoryFilterHelper.matchesSearchPrefix(noun, "d"))
        assertTrue(HistoryFilterHelper.matchesSearchPrefix(noun, "das"))
        assertTrue(HistoryFilterHelper.matchesSearchPrefix(noun, "das sch"))
    }

    @Test
    fun testPrefixMatching_wordListsAndTranslations() {
        val wordList = createEntity(id = 1, word = "Apfel, Birne, Zitrone", type = "Wörter", meaning = "تفاح، إجاص، ليمون")

        assertTrue(HistoryFilterHelper.matchesSearchPrefix(wordList, "a"))
        assertTrue(HistoryFilterHelper.matchesSearchPrefix(wordList, "b"))
        assertTrue(HistoryFilterHelper.matchesSearchPrefix(wordList, "z"))
        assertFalse(HistoryFilterHelper.matchesSearchPrefix(wordList, "m"))

        // Arabic translation prefix
        assertTrue(HistoryFilterHelper.matchesSearchPrefix(wordList, "ت"))
        assertTrue(HistoryFilterHelper.matchesSearchPrefix(wordList, "تفاح"))
    }

    @Test
    fun testFilterHistory_categories() {
        val noun = createEntity(id = 1, word = "Buch", type = "Nomen")
        val verb = createEntity(id = 2, word = "backen", type = "Verb")
        val sentence = createEntity(id = 3, word = "Guten Morgen", type = "Satz", gender = "–")
        val words = createEntity(id = 4, word = "Stuhl, Tisch", type = "Wörter", gender = "Wörter")

        val all = listOf(noun, verb, sentence, words)

        // Tab 0: Alle
        assertEquals(4, HistoryFilterHelper.filterHistory(all, 0, "").size)

        // Tab 1: Nomen
        val nounsOnly = HistoryFilterHelper.filterHistory(all, 1, "")
        assertEquals(1, nounsOnly.size)
        assertEquals("Buch", nounsOnly[0].word)

        // Tab 2: Verben
        val verbsOnly = HistoryFilterHelper.filterHistory(all, 2, "")
        assertEquals(1, verbsOnly.size)
        assertEquals("backen", verbsOnly[0].word)

        // Tab 3: Sätze
        val sentencesOnly = HistoryFilterHelper.filterHistory(all, 3, "")
        assertEquals(1, sentencesOnly.size)
        assertEquals("Guten Morgen", sentencesOnly[0].word)

        // Tab 4: Wörter
        val wordsOnly = HistoryFilterHelper.filterHistory(all, 4, "")
        assertEquals(1, wordsOnly.size)
        assertEquals("Stuhl, Tisch", wordsOnly[0].word)

        // Search with query "b" under "Alle" matches Buch and backen
        val queryB = HistoryFilterHelper.filterHistory(all, 0, "b")
        assertEquals(2, queryB.size)

        // Search with query "b" under "Verben" matches only backen
        val queryBVerbs = HistoryFilterHelper.filterHistory(all, 2, "b")
        assertEquals(1, queryBVerbs.size)
        assertEquals("backen", queryBVerbs[0].word)
    }

    @Test
    fun testNameSort_alphabeticalIgnoringArticles() {
        val n1 = createEntity(id = 1, word = "der Apfel", type = "Nomen")
        val n2 = createEntity(id = 2, word = "die Banane", type = "Nomen")
        val n3 = createEntity(id = 3, word = "das Auto", type = "Nomen")

        val sortedAsc = HistoryFilterHelper.sortHistory(
            listOf(n1, n2, n3),
            HistorySortOption(field = HistorySortField.NAME, direction = SortDirection.ASCENDING)
        )
        // Auto (A-u-t-o) comes before Apfel (A-p-f-e-l)? No: Apfel (A-p) comes before Auto (A-u), then Banane (B)
        assertEquals("der Apfel", sortedAsc[0].word)
        assertEquals("das Auto", sortedAsc[1].word)
        assertEquals("die Banane", sortedAsc[2].word)

        val sortedDesc = HistoryFilterHelper.sortHistory(
            listOf(n1, n2, n3),
            HistorySortOption(field = HistorySortField.NAME, direction = SortDirection.DESCENDING)
        )
        assertEquals("die Banane", sortedDesc[0].word)
        assertEquals("das Auto", sortedDesc[1].word)
        assertEquals("der Apfel", sortedDesc[2].word)
    }

    @Test
    fun testGenderSort_specificTargets() {
        val masc = createEntity(id = 1, word = "Tisch", type = "Nomen", gender = "Maskulin")
        val fem = createEntity(id = 2, word = "Sonne", type = "Nomen", gender = "Feminin")
        val neut = createEntity(id = 3, word = "Buch", type = "Nomen", gender = "Neutrum")

        // Prioritize Maskulin
        val sortedMasc = HistoryFilterHelper.sortHistory(
            listOf(fem, neut, masc),
            HistorySortOption(
                field = HistorySortField.GENDER,
                direction = SortDirection.ASCENDING,
                genderTarget = GenderSortTarget.MASKULIN
            )
        )
        assertEquals("Tisch", sortedMasc[0].word)

        // Prioritize Feminin
        val sortedFem = HistoryFilterHelper.sortHistory(
            listOf(masc, neut, fem),
            HistorySortOption(
                field = HistorySortField.GENDER,
                direction = SortDirection.ASCENDING,
                genderTarget = GenderSortTarget.FEMININ
            )
        )
        assertEquals("Sonne", sortedFem[0].word)

        // Prioritize Neutrum
        val sortedNeut = HistoryFilterHelper.sortHistory(
            listOf(masc, fem, neut),
            HistorySortOption(
                field = HistorySortField.GENDER,
                direction = SortDirection.ASCENDING,
                genderTarget = GenderSortTarget.NEUTRUM
            )
        )
        assertEquals("Buch", sortedNeut[0].word)
    }

    @Test
    fun testDateSort_automaticAndManual() {
        val itemOld = createEntity(id = 1, word = "alt", type = "Nomen").copy(timestamp = 1000L)
        val itemMid = createEntity(id = 2, word = "mittel", type = "Nomen").copy(timestamp = 5000L)
        val itemNew = createEntity(id = 3, word = "neu", type = "Nomen").copy(timestamp = 10000L)

        // Automatic descending (newest first)
        val sortedAutoDesc = HistoryFilterHelper.sortHistory(
            listOf(itemOld, itemMid, itemNew),
            HistorySortOption(field = HistorySortField.DATE, direction = SortDirection.DESCENDING)
        )
        assertEquals("neu", sortedAutoDesc[0].word)
        assertEquals("mittel", sortedAutoDesc[1].word)
        assertEquals("alt", sortedAutoDesc[2].word)

        // Manual date close to 5000L
        val sortedManual = HistoryFilterHelper.sortHistory(
            listOf(itemOld, itemMid, itemNew),
            HistorySortOption(
                field = HistorySortField.DATE,
                direction = SortDirection.ASCENDING,
                dateSortMode = DateSortMode.MANUAL,
                manualDateMillis = 5100L
            )
        )
        assertEquals("mittel", sortedManual[0].word)
    }

    @Test
    fun testSearchMatching_partialAndFull_andNoUnrelatedResults() {
        val verbGehen = createEntity(id = 1, word = "gehen", type = "Verb", meaning = "يمشي / يذهب")
        val nounWasser = createEntity(id = 2, word = "Wasser", type = "Nomen", gender = "Neutrum", genderArticle = "das", meaning = "ماء")
        val nounKatze = createEntity(id = 3, word = "Katze", type = "Nomen", gender = "Feminin", genderArticle = "die", meaning = "قطة").copy(manualTranslation = "سنور\nهرة")

        // 1. Partial and full German matching:
        // "ass" is partially in "Wasser"
        assertTrue(HistoryFilterHelper.matchesSearchPrefix(nounWasser, "ass"))
        assertTrue(HistoryFilterHelper.matchesSearchPrefix(nounWasser, "wasser"))
        assertFalse(HistoryFilterHelper.matchesSearchPrefix(verbGehen, "ass"))

        // 2. Query "er":
        // Should match "Wasser" because "Wasser" contains "er"!
        // Should NOT match "gehen" just because its type is "Verb"!
        assertTrue(HistoryFilterHelper.matchesSearchPrefix(nounWasser, "er"))
        assertFalse(HistoryFilterHelper.matchesSearchPrefix(verbGehen, "er"))

        // 3. Query "in":
        // Should NOT match "Katze" just because its gender is "Feminin"!
        assertFalse(HistoryFilterHelper.matchesSearchPrefix(nounKatze, "in"))

        // 4. Searching manual translations:
        // "سنور" or "هرة" in manual translations matches Katze
        assertTrue(HistoryFilterHelper.matchesSearchPrefix(nounKatze, "سنور"))
        assertTrue(HistoryFilterHelper.matchesSearchPrefix(nounKatze, "هرة"))
        assertTrue(HistoryFilterHelper.matchesSearchPrefix(nounKatze, "هر")) // partial match!
        assertFalse(HistoryFilterHelper.matchesSearchPrefix(verbGehen, "سنور"))

        // 5. Arabic search with and without Tashkeel
        val nounHubb = createEntity(id = 4, word = "Liebe", type = "Nomen", meaning = "حُبّ")
        assertTrue(HistoryFilterHelper.matchesSearchPrefix(nounHubb, "حب"))
        assertTrue(HistoryFilterHelper.matchesSearchPrefix(nounHubb, "حُب"))
    }

    @Test
    fun testMultipleManualTranslationsParsingAndFormatting() {
        val raw = "منزل\nAlt.: بيت\nدار"
        val parsed = parseManualTranslations(raw)
        assertEquals(3, parsed.size)
        assertEquals("منزل", parsed[0])
        assertEquals("بيت", parsed[1])
        assertEquals("دار", parsed[2])

        val formatted = formatManualTranslations(parsed)
        assertEquals("منزل\nبيت\nدار", formatted)
    }
}
