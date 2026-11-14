package com.example.data.model

import org.json.JSONObject
import java.util.Locale

/**
 * Grammatical Type toggle: Nomen (Noun) or Verb
 */
enum class GrammarType(val displayName: String, val apiPromptType: String) {
    NOMEN("Nomen", "Nomen (Deklination & Übersetzung)"),
    VERB("Verb", "Verb (Konjugation)"),
    SENTENCE("Satz", "Satz (Übersetzung)"),
    NOMEN_LIST("Wörter", "Wörter (Übersetzung)"),
    ADJEKTIV("Adjektive", "Adjektive (Komparation & Deklination)");

    companion object {
        fun fromString(str: String?): GrammarType = when {
            str?.trim()?.equals("Verb", ignoreCase = true) == true -> VERB
            str?.trim()?.equals("Satz", ignoreCase = true) == true ||
            str?.trim()?.equals("Sentence", ignoreCase = true) == true -> SENTENCE
            str?.trim()?.equals("Adjektiv", ignoreCase = true) == true ||
            str?.trim()?.equals("Adjektive", ignoreCase = true) == true ||
            str?.trim()?.equals("Adjective", ignoreCase = true) == true -> ADJEKTIV
            str?.trim()?.equals("Wörter", ignoreCase = true) == true ||
            str?.trim()?.equals("Wort", ignoreCase = true) == true ||
            str?.trim()?.equals("Nomen (Liste)", ignoreCase = true) == true ||
            str?.trim()?.equals("NomenList", ignoreCase = true) == true -> NOMEN_LIST
            str?.trim()?.equals("Nomen", ignoreCase = true) == true ||
            str?.trim()?.equals("Nomen & Sätze", ignoreCase = true) == true -> NOMEN
            else -> NOMEN
        }
    }
}

private val DECLENSION_WHITESPACE_REGEX = Regex("\\s+")
private val DECLENSION_WHITESPACE_COMMA_REGEX = Regex("[\\s,;]+")
private val DECLENSION_WORD_LIST_SPLIT_REGEX = Regex("[,;،/\\n]+")
private val LEADING_NULL_REGEX = Regex("(?i)^null\\s+")
private val TRAILING_NULL_REGEX = Regex("(?i)\\s+null$")
private val ARABIC_STRIP_REGEX = Regex("[\\u0600-\\u06FF]")

/**
 * Utility to determine if input text represents multiple words or a single word.
 */
fun isMultipleWordsInput(text: String?): Boolean {
    if (text.isNullOrBlank()) return false
    val trimmed = text.trim()
    if (trimmed.contains(",") || trimmed.contains("،") || trimmed.contains("/") || trimmed.contains(";") || trimmed.contains("-")) return true
    val words = trimmed.split(DECLENSION_WHITESPACE_REGEX).filter { it.isNotBlank() }
    return words.size > 1
}

/**
 * Represents a single declension case row (Nominativ, Akkusativ, Dativ, Genitiv).
 */
data class DeclensionCaseRow(
    val caseKey: String,          // nominativ, akkusativ, dativ, genitiv
    val caseNameDe: String,       // Nominativ, Akkusativ, Dativ, Genitiv
    val caseQuestionDe: String,   // Wer/Was?, Wen?, Wem?, Wessen?
    val definite: String,         // e.g. "der Tisch"
    val indefinite: String,       // e.g. "ein Tisch" / "keine Tische"
    val noArticle: String         // e.g. "Tisch"
)

/**
 * Group of declensions for a specific grammatical number (Singular or Plural).
 */
data class DeclensionTableGroup(
    val numberDe: String,         // "Singular", "Plural"
    val rows: List<DeclensionCaseRow>
)

/**
 * An example sentence demonstrating usage of a specific German word.
 */
data class WordExample(
    val word: String,
    val sentence: String
)

/**
 * Full declension result for a German noun.
 */
fun JSONObject.optSafeString(name: String, fallback: String = ""): String {
    if (!has(name) || isNull(name)) return fallback
    val optVal = opt(name)
    if (optVal == null || optVal == JSONObject.NULL) return fallback
    val v = optVal.toString().trim()
    return if (v.equals("null", ignoreCase = true) || (v.isEmpty() && fallback.isNotEmpty())) fallback else v
}

fun sanitizeHistoryWord(rawWord: String?, rawData: String? = null, type: String? = null): String {
    val trimmed = rawWord?.trim().orEmpty()
    if (trimmed.isNotBlank() && !trimmed.equals("null", ignoreCase = true)) {
        return trimmed
    }
    if (!rawData.isNullOrBlank()) {
        try {
            val root = JSONObject(rawData)
            val candidates = listOf(
                "word", "infinitiv", "verb", "adjektiv", "sentence", "satz",
                "grundform", "nomen", "singular_word"
            )
            for (key in candidates) {
                val candidate = root.optSafeString(key, "")
                if (candidate.isNotBlank() && !candidate.equals("null", ignoreCase = true)) {
                    return candidate
                }
            }
            val singularObj = root.optJSONObject("singular")
            if (singularObj != null) {
                val nomObj = singularObj.optJSONObject("nominativ")
                val noArt = nomObj?.optSafeString("noArticle", "")
                if (!noArt.isNullOrBlank() && !noArt.equals("null", ignoreCase = true)) return noArt
                val nomDef = nomObj?.optSafeString("definite", "")
                if (!nomDef.isNullOrBlank() && !nomDef.equals("null", ignoreCase = true)) {
                    val cleanDef = nomDef.removePrefix("der ").removePrefix("die ").removePrefix("das ").trim()
                    if (cleanDef.isNotBlank() && !cleanDef.equals("null", ignoreCase = true)) return cleanDef
                }
                val nomStr = singularObj.optSafeString("nominativ", "")
                if (nomStr.isNotBlank() && !nomStr.equals("null", ignoreCase = true) && !nomStr.startsWith("{")) return nomStr
            }
            val pluralNoun = root.optSafeString("pluralNoun", "")
            if (pluralNoun.isNotBlank() && !pluralNoun.equals("null", ignoreCase = true)) {
                val cleanPlural = pluralNoun.removePrefix("die ").removePrefix("Die ").trim()
                if (cleanPlural.isNotBlank() && !cleanPlural.equals("null", ignoreCase = true)) return cleanPlural
            }
            val pluralObj = root.optJSONObject("plural")
            if (pluralObj != null) {
                val nomObj = pluralObj.optJSONObject("nominativ")
                val noArt = nomObj?.optSafeString("noArticle", "")
                if (!noArt.isNullOrBlank() && !noArt.equals("null", ignoreCase = true)) return noArt
                val nomDef = nomObj?.optSafeString("definite", "")
                if (!nomDef.isNullOrBlank() && !nomDef.equals("null", ignoreCase = true)) {
                    val cleanDef = nomDef.removePrefix("die ").removePrefix("Die ").trim()
                    if (cleanDef.isNotBlank() && !cleanDef.equals("null", ignoreCase = true)) return cleanDef
                }
                val nomStr = pluralObj.optSafeString("nominativ", "")
                if (nomStr.isNotBlank() && !nomStr.equals("null", ignoreCase = true) && !nomStr.startsWith("{")) return nomStr
            }
        } catch (_: Exception) {}
    }
    return ""
}

data class WordDeclensionResult(
    val word: String,             // Base noun, capitalized
    val gender: String,           // Maskulin, Feminin, Neutrum, Plural
    val genderArticle: String,    // der, die, das
    val pluralNoun: String,       // e.g. "die Tische"
    val meaningEnglish: String,   // Meaning in English
    val singular: DeclensionTableGroup,
    val plural: DeclensionTableGroup,
    val rawJson: String = "",
    val examples: List<WordExample> = emptyList(),
    val ipa: String = ""
) {
    companion object {
        fun fromJson(jsonStr: String, fallbackWord: String = ""): WordDeclensionResult {
            val root = JSONObject(jsonStr)
            val ipa = root.optSafeString("ipa").ifBlank {
                root.optSafeString("pronunciation", root.optSafeString("aussprache", ""))
            }
            val typeStr = root.optSafeString("type", "")
            var rawWord = root.optSafeString("word").ifBlank {
                root.optSafeString("sentence").ifBlank {
                    root.optSafeString("nomen").ifBlank {
                        val singObj = root.optJSONObject("singular")
                        val nomObj = singObj?.optJSONObject("nominativ")
                        nomObj?.optSafeString("noArticle")?.ifBlank {
                            nomObj?.optSafeString("definite")?.removePrefix("der ")?.removePrefix("die ")?.removePrefix("das ")?.trim()
                        } ?: ""
                    }
                }
            }
            if (rawWord.isBlank() || rawWord.equals("null", ignoreCase = true)) {
                val pluralCandidate = root.optSafeString("pluralNoun")
                if (pluralCandidate.isNotBlank() && !pluralCandidate.equals("null", ignoreCase = true)) {
                    rawWord = pluralCandidate.removePrefix("die ").removePrefix("Die ").trim()
                }
            }
            if (rawWord.isBlank() || rawWord.equals("null", ignoreCase = true)) {
                val nomPlural = root.optJSONObject("plural")?.optJSONObject("nominativ")?.optSafeString("definite", "")
                if (!nomPlural.isNullOrBlank() && !nomPlural.equals("null", ignoreCase = true)) {
                    rawWord = nomPlural.removePrefix("die ").removePrefix("Die ").trim()
                }
            }
            if (rawWord.isBlank() || rawWord.equals("null", ignoreCase = true)) {
                rawWord = fallbackWord.trim()
            }
            val word = if (rawWord.equals("null", ignoreCase = true) || rawWord.isBlank()) fallbackWord.trim() else rawWord
            val rawGender = root.optSafeString("gender", "Maskulin").takeUnless { it.equals("null", ignoreCase = true) } ?: "Maskulin"
            val isNomenList = typeStr.equals("Wörter", ignoreCase = true) ||
                    typeStr.equals("Wort", ignoreCase = true) ||
                    typeStr.equals("NomenList", ignoreCase = true) ||
                    typeStr.equals("Nomen (Liste)", ignoreCase = true) ||
                    rawGender.equals("Wörter", ignoreCase = true) ||
                    rawGender.equals("Wort", ignoreCase = true)
            val isSentence = !isNomenList && (
                    typeStr.equals("Satz", ignoreCase = true) ||
                    typeStr.equals("Sentence", ignoreCase = true) ||
                    rawGender.equals("Satz", ignoreCase = true) ||
                    rawGender == "–" ||
                    rawWord.split(DECLENSION_WHITESPACE_COMMA_REGEX).filter { it.isNotBlank() }.size >= 2
            )

            val gender = if (isSentence) "–" else rawGender
            val defaultArt = when (gender.lowercase(Locale.ROOT)) {
                "maskulin" -> "der"
                "feminin" -> "die"
                "neutrum" -> "das"
                else -> ""
            }
            val rawGenderArticle = root.optSafeString("genderArticle").trim()
            val genderArticle = if (isSentence) "" else {
                if (rawGenderArticle.isNotBlank() && !rawGenderArticle.equals("null", ignoreCase = true)) {
                    rawGenderArticle
                } else {
                    defaultArt
                }
            }
            val pluralNoun = if (isSentence) "–" else root.optSafeString("pluralNoun", "").trim()
            val meaningEnglish = root.optSafeString("meaningEnglish",
                root.optSafeString("meaning", root.optSafeString("meaningArabic", ""))
            ).trim()

            val singularObj = root.optJSONObject("singular") ?: JSONObject()
            val pluralObj = root.optJSONObject("plural") ?: JSONObject()

            val singularRows = mutableListOf(
                createRow("nominativ", "Nominativ", "Wer oder was?", singularObj),
                createRow("akkusativ", "Akkusativ", "Wen oder was?", singularObj),
                createRow("dativ", "Dativ", "Wem?", singularObj),
                createRow("genitiv", "Genitiv", "Wessen?", singularObj)
            )
            if (isSentence) {
                singularRows.add(createRow("ablativ", "Ablativ", "Woher? Womit?", singularObj))
            }

            val pluralRows = mutableListOf(
                createRow("nominativ", "Nominativ", "Wer oder was?", pluralObj),
                createRow("akkusativ", "Akkusativ", "Wen oder was?", pluralObj),
                createRow("dativ", "Dativ", "Wem?", pluralObj),
                createRow("genitiv", "Genitiv", "Wessen?", pluralObj)
            )
            if (isSentence) {
                pluralRows.add(createRow("ablativ", "Ablativ", "Woher? Womit?", pluralObj))
            }

            val examplesList = mutableListOf<WordExample>()
            val examplesArray = root.optJSONArray("examples")
            if (examplesArray != null) {
                for (i in 0 until examplesArray.length()) {
                    val obj = examplesArray.optJSONObject(i)
                    if (obj != null) {
                        val w = obj.optString("word", "").trim()
                        val s = obj.optString("sentence", obj.optString("text", obj.optString("example", ""))).trim()
                        if (s.isNotBlank()) {
                            examplesList.add(WordExample(w, s))
                        }
                    } else {
                        val s = examplesArray.optString(i, "").trim()
                        if (s.isNotBlank()) {
                            examplesList.add(WordExample("", s))
                        }
                    }
                }
            }

            return WordDeclensionResult(
                word = word,
                gender = gender,
                genderArticle = genderArticle,
                pluralNoun = pluralNoun,
                meaningEnglish = meaningEnglish,
                singular = DeclensionTableGroup("Singular", singularRows),
                plural = DeclensionTableGroup("Plural", pluralRows),
                rawJson = jsonStr,
                examples = examplesList,
                ipa = ipa
            )
        }

        private fun createRow(
            key: String,
            nameDe: String,
            questionDe: String,
            container: JSONObject
        ): DeclensionCaseRow {
            val caseObj = container.optJSONObject(key) ?: JSONObject()
            val definite = caseObj.optSafeString("definite", "-")
            val indefinite = caseObj.optSafeString("indefinite", "-")
            val noArticle = caseObj.optSafeString("noArticle", "-")

            return DeclensionCaseRow(
                caseKey = key,
                caseNameDe = nameDe,
                caseQuestionDe = questionDe,
                definite = definite,
                indefinite = indefinite,
                noArticle = noArticle
            )
        }
    }
}

/**
 * Resolves or provides natural German example sentences for words in a Wörter result.
 */
fun resolveWordExamples(result: WordDeclensionResult): List<WordExample> {
    if (result.examples.isNotEmpty()) {
        return result.examples
    }
    return resolveWordExamples(result.word, result.rawJson, result.examples)
}

fun resolveWordExamples(
    word: String,
    rawJson: String = "",
    existingExamples: List<WordExample> = emptyList()
): List<WordExample> {
    if (existingExamples.isNotEmpty()) {
        return existingExamples
    }
    if (rawJson.isNotBlank()) {
        try {
            val root = JSONObject(rawJson)
            val examplesArray = root.optJSONArray("examples")
            if (examplesArray != null && examplesArray.length() > 0) {
                val list = mutableListOf<WordExample>()
                for (i in 0 until examplesArray.length()) {
                    val obj = examplesArray.optJSONObject(i)
                    if (obj != null) {
                        val w = obj.optString("word", "").trim()
                        val s = obj.optString("sentence", obj.optString("text", "")).trim()
                        if (s.isNotBlank()) {
                            list.add(WordExample(w, s))
                        }
                    } else {
                        val s = examplesArray.optString(i, "").trim()
                        if (s.isNotBlank()) {
                            list.add(WordExample("", s))
                        }
                    }
                }
                if (list.isNotEmpty()) return list
            }
        } catch (_: Exception) {}
    }

    val curatedMap = mapOf(
        "apfel" to "Der Apfel schmeckt sehr süß und frisch.",
        "banane" to "Die Banane ist gelb und reich an Kalium.",
        "kirsche" to "Die Kirsche blüht im Frühling und reift im Sommer.",
        "äpfel" to "Die frischen Äpfel liegen im Obstkorb.",
        "bananen" to "Bananen schmecken besonders gut zum Frühstück.",
        "kirschen" to "Im Sommer pflücken wir süße rote Kirschen.",
        "tisch" to "Der Tisch im Wohnzimmer ist aus massivem Holz gebaut.",
        "stuhl" to "Auf dem bequemen Stuhl kann man stundenlang lesen.",
        "haus" to "Das schöne Haus hat einen großen, grünen Garten.",
        "wohnung" to "Die moderne Wohnung liegt zentral in der Innenstadt.",
        "auto" to "Das neue Auto fährt sehr leise und sparsam.",
        "buch" to "Das spannende Buch fesselt den Leser bis zur letzten Seite.",
        "wasser" to "Kühles Wasser ist an heißen Sommertagen die beste Erfrischung.",
        "brot" to "Das frisch gebackene Brot schmeckt noch herrlich warm.",
        "kaffee" to "Am Morgen trinke ich gerne eine Tasse heißen Kaffee.",
        "tee" to "Eine Tasse warmer Tee sorgt für gemütliche Entspannung.",
        "hund" to "Der treue Hund läuft freudig bellend durch den Park.",
        "katze" to "Die kleine Katze schnurrt zufrieden auf dem Sofa.",
        "kind" to "Das lachende Kind spielt mit seinen Freunden im Garten.",
        "schule" to "Die Schule beginnt jeden Werktag pünktlich um acht Uhr.",
        "arbeit" to "Gute Arbeit verlangt Konzentration und Geduld.",
        "freund" to "Ein wahrer Freund steht einem in jeder Lebenslage bei.",
        "sonne" to "Die helle Sonne scheint vom wolkenlosen blauen Himmel.",
        "mond" to "Der silberne Mond erhellt die stille Sommernacht.",
        "blume" to "Die bunte Blume duftet herrlich im Blumenbeet.",
        "baum" to "Der alte Baum spendet angenehmen kühlen Schatten."
    )

    val words = word.split(DECLENSION_WORD_LIST_SPLIT_REGEX)
        .map { it.trim().trim('.', '!', '?', '-', '•') }
        .filter { it.isNotBlank() }

    if (words.isEmpty()) {
        return emptyList()
    }

    return words.map { w ->
        val key = w.lowercase(Locale.ROOT)
        val sentence = curatedMap[key] ?: "Das Wort '${w.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }}' wird häufig in alltäglichen Sätzen gebraucht."
        WordExample(
            word = w.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
            sentence = sentence
        )
    }
}

/**
 * Resolves or provides natural German example sentences for a sentence (Satz) result.
 */
fun resolveSentenceExamples(result: WordDeclensionResult): List<WordExample> {
    if (result.examples.isNotEmpty()) {
        return result.examples
    }
    return resolveSentenceExamples(result.word, result.rawJson, result.examples)
}

fun resolveSentenceExamples(
    sentence: String,
    rawJson: String = "",
    existingExamples: List<WordExample> = emptyList()
): List<WordExample> {
    if (existingExamples.isNotEmpty()) {
        return existingExamples
    }
    if (rawJson.isNotBlank()) {
        try {
            val root = JSONObject(rawJson)
            val examplesArray = root.optJSONArray("examples")
            if (examplesArray != null && examplesArray.length() > 0) {
                val list = mutableListOf<WordExample>()
                for (i in 0 until examplesArray.length()) {
                    val obj = examplesArray.optJSONObject(i)
                    if (obj != null) {
                        val w = obj.optString("word", "").trim()
                        val s = obj.optString("sentence", obj.optString("text", "")).trim()
                        if (s.isNotBlank()) {
                            list.add(WordExample(w, s))
                        }
                    } else {
                        val s = examplesArray.optString(i, "").trim()
                        if (s.isNotBlank()) {
                            list.add(WordExample("", s))
                        }
                    }
                }
                if (list.isNotEmpty()) return list
            }
        } catch (_: Exception) {}
    }

    val trimmed = sentence.trim().trim('.', '!', '?')
    val lower = trimmed.lowercase(Locale.ROOT)

    // Curated examples for common sentences
    if (lower.contains("hobby") && (lower.contains("kochen") || lower.contains("koche"))) {
        return listOf(
            WordExample("Lesen", "Mein Hobby ist Lesen."),
            WordExample("Schwimmen", "Mein Hobby ist Schwimmen."),
            WordExample("Musik hören", "Mein Hobby ist Musik hören.")
        )
    }

    if (lower.contains("hobby")) {
        return listOf(
            WordExample("Lesen", "Mein Hobby ist Lesen und Entspannen."),
            WordExample("Schwimmen", "Mein Hobby ist Schwimmen im Sommer."),
            WordExample("Reisen", "Mein Hobby ist Reisen in fremde Länder.")
        )
    }

    if (lower.startsWith("guten morgen") || lower == "morgen") {
        return listOf(
            WordExample("Guten Tag", "Guten Tag, wie kann ich Ihnen heute helfen?"),
            WordExample("Guten Abend", "Guten Abend allerseits, schön Sie zu sehen!"),
            WordExample("Gute Nacht", "Gute Nacht und schlaf gut!")
        )
    }

    if (lower.contains("wie geht es") || lower.contains("wie geht's")) {
        return listOf(
            WordExample("sehr gut", "Mir geht es sehr gut, danke der Nachfrage!"),
            WordExample("Ihnen", "Wie geht es Ihnen heute im Alltag?"),
            WordExample("bestens", "Alles läuft bestens bei mir, und bei dir?")
        )
    }

    if (lower.contains("deutsch") && (lower.contains("lernen") || lower.contains("lerne") || lower.contains("sprechen"))) {
        return listOf(
            WordExample("sprechen", "Ich lerne jeden Tag fleißig Deutsch sprechen."),
            WordExample("verstehen", "Ich verstehe die deutsche Sprache immer besser."),
            WordExample("üben", "Wir üben zusammen neue deutsche Sätze.")
        )
    }

    if (lower.contains("danke") || lower.contains("vielen dank")) {
        return listOf(
            WordExample("Gern geschehen", "Gern geschehen, ich helfe immer wieder gerne!"),
            WordExample("Kein Problem", "Kein Problem, das war mir ein echtes Vergnügen."),
            WordExample("Herzlichen Dank", "Herzlichen Dank für Ihre freundliche Unterstützung!")
        )
    }

    if (lower.contains("wiedersehen") || lower.contains("tschüss")) {
        return listOf(
            WordExample("bis bald", "Auf Wiedersehen und bis zum nächsten Mal!"),
            WordExample("schönen Tag", "Tschüss und hab noch einen wunderschönen Tag!"),
            WordExample("gute Reise", "Gute Reise und pass gut auf dich auf!")
        )
    }

    // Dynamic generation for general sentences
    val words = trimmed.split(DECLENSION_WHITESPACE_REGEX).filter { it.length > 2 }
    val primaryWord = words.firstOrNull { it.first().isUpperCase() } ?: words.firstOrNull() ?: trimmed

    return listOf(
        WordExample(primaryWord, "$trimmed und wir freuen uns sehr darauf."),
        WordExample(primaryWord, "Natürlich gilt: $trimmed im täglichen Leben."),
        WordExample(primaryWord, "Man sagt oft: $trimmed bei dieser Gelegenheit.")
    )
}

/**
 * Returns a copy of WordDeclensionResult with resolved examples and updated rawJson.
 */
fun WordDeclensionResult.withResolvedWordExamples(): WordDeclensionResult {
    val resolved = resolveWordExamples(this)
    if (this.examples.isNotEmpty()) return this
    val updatedJson = try {
        val root = if (this.rawJson.isNotBlank()) JSONObject(this.rawJson) else JSONObject()
        val arr = org.json.JSONArray()
        resolved.forEach { ex ->
            val obj = JSONObject()
            obj.put("word", ex.word)
            obj.put("sentence", ex.sentence)
            arr.put(obj)
        }
        root.put("examples", arr)
        root.toString()
    } catch (_: Exception) {
        this.rawJson
    }
    return this.copy(examples = resolved, rawJson = updatedJson)
}

fun WordDeclensionResult.withResolvedSentenceExamples(): WordDeclensionResult {
    val resolved = resolveSentenceExamples(this)
    if (this.examples.isNotEmpty()) return this
    val updatedJson = try {
        val root = if (this.rawJson.isNotBlank()) JSONObject(this.rawJson) else JSONObject()
        val arr = org.json.JSONArray()
        resolved.forEach { ex ->
            val obj = JSONObject()
            obj.put("word", ex.word)
            obj.put("sentence", ex.sentence)
            arr.put(obj)
        }
        root.put("examples", arr)
        root.toString()
    } catch (_: Exception) {
        this.rawJson
    }
    return this.copy(examples = resolved, rawJson = updatedJson)
}

/**
 * Conjugation of a verb for a specific tense (ich, du, er/sie/es, wir, ihr, sie/Sie).
 */
data class VerbTenseConjugation(
    val tenseKey: String,         // praesens, praeteritum, perfekt, plusquamperfekt, futur1, futur2
    val tenseNameDe: String,      // Präsens, Präteritum, Perfekt, Plusquamperfekt, Futur I, Futur II
    val ich: String,
    val du: String,
    val erSieEs: String,
    val wir: String,
    val ihr: String,
    val sieSie: String
)

/**
 * Imperative forms of a verb.
 */
data class VerbImperativ(
    val du: String,               // e.g. "arbeite!"
    val ihr: String,              // e.g. "arbeitet!"
    val sie: String               // e.g. "arbeiten Sie!"
)

/**
 * Full conjugation result for a German verb.
 */
data class VerbConjugationResult(
    val word: String,             // Infinitiv
    val infinitiv: String,        // e.g. "arbeiten"
    val hilfsverb: String,        // "haben" or "sein"
    val auxiliary: String = "",   // "(hat)" or "(ist)"
    val praeteritum: String = "", // e.g. "nahm" / "bildete"
    val partizip1: String,        // e.g. "arbeitend"
    val partizip2: String,        // e.g. "gearbeitet"
    val verbType: String = "",    // "stark" / "schwach" / "gemischt"
    val separable: String = "",   // "trennbar" / "untrennbar"
    val caseObject: String = "",  // "Akk." / "Dat." / "Gen."
    val meaningEnglish: String,   // English translation
    val imperativ: VerbImperativ,
    val tenses: List<VerbTenseConjugation>,
    val rawJson: String = "",
    val examples: List<WordExample> = emptyList(),
    val ipa: String = ""
) {
    companion object {
        fun fromJson(jsonStr: String, fallbackWord: String = ""): VerbConjugationResult {
            val root = JSONObject(jsonStr)
            val ipa = root.optSafeString("ipa").ifBlank {
                root.optSafeString("pronunciation", root.optSafeString("aussprache", ""))
            }
            val rawInf = root.optSafeString("infinitiv").ifBlank { root.optSafeString("verb", "") }
            var rawWord = root.optSafeString("word").ifBlank { rawInf }
            if (rawWord.isBlank() || rawWord.equals("null", ignoreCase = true)) {
                rawWord = fallbackWord.trim()
            }
            val word = if (rawWord.equals("null", ignoreCase = true) || rawWord.isBlank()) fallbackWord.trim() else rawWord
            val infinitiv = rawInf.takeUnless { it.isBlank() || it.equals("null", ignoreCase = true) } ?: word
            val hilfsverb = root.optSafeString("hilfsverb", "haben").takeUnless { it.equals("null", ignoreCase = true) } ?: "haben"
            val auxiliary = root.optSafeString("auxiliary", "").ifBlank {
                if (hilfsverb.contains("sein", true) || hilfsverb.equals("ist", true)) "(ist)" else "(hat)"
            }
            val partizip1 = root.optSafeString("partizip1", "").takeUnless { it.equals("null", ignoreCase = true) }.orEmpty()
            val partizip2 = root.optSafeString("partizip2", "").takeUnless { it.equals("null", ignoreCase = true) }.orEmpty()
            val verbType = root.optSafeString("verbType").ifBlank { root.optSafeString("verb_type", "") }.takeUnless { it.equals("null", ignoreCase = true) }.orEmpty()
            val separable = root.optSafeString("separable", "").takeUnless { it.equals("null", ignoreCase = true) }.orEmpty()
            var caseObject = root.optSafeString("caseObject").ifBlank { root.optSafeString("case_object", "") }.takeUnless { it.equals("null", ignoreCase = true) }.orEmpty()
            if (caseObject.isBlank() && word.isNotBlank()) {
                caseObject = getFallbackVerbCaseObject(word)
            }
            val meaningEnglish = root.optSafeString("meaningEnglish",
                root.optSafeString("english", root.optSafeString("meaning", ""))
            ).trim()

            val examplesList = mutableListOf<WordExample>()
            val examplesArray = root.optJSONArray("examples")
            if (examplesArray != null) {
                for (i in 0 until examplesArray.length()) {
                    val exObj = examplesArray.optJSONObject(i)
                    if (exObj != null) {
                        val w = exObj.optSafeString("word", word).trim()
                        val s = exObj.optSafeString("sentence", exObj.optSafeString("text", exObj.optSafeString("example", ""))).trim()
                        if (s.isNotBlank()) {
                            examplesList.add(WordExample(w, s))
                        }
                    } else {
                        val s = examplesArray.optString(i, "").trim().takeUnless { it.equals("null", ignoreCase = true) }.orEmpty()
                        if (s.isNotBlank()) {
                            examplesList.add(WordExample(word, s))
                        }
                    }
                }
            }

            val impObj = root.optJSONObject("imperativ") ?: JSONObject()
            val imperativ = VerbImperativ(
                du = impObj.optSafeString("du", "-").trim(),
                ihr = impObj.optSafeString("ihr", "-").trim(),
                sie = impObj.optSafeString("sie", "-").trim()
            )

            val tensesObj = root.optJSONObject("tenses") ?: JSONObject()

            val tensesList = listOf(
                createTenseRow("praesens", "Präsens", tensesObj),
                createTenseRow("praeteritum", "Präteritum", tensesObj),
                createTenseRow("perfekt", "Perfekt", tensesObj),
                createTenseRow("plusquamperfekt", "Plusquamperfekt", tensesObj),
                createTenseRow("futur1", "Futur I", tensesObj),
                createTenseRow("futur2", "Futur II", tensesObj)
            )

            var praeteritum = root.optSafeString("praeteritum", "").trim()
            if (praeteritum.isBlank() || praeteritum.startsWith("{")) {
                val praetObj = tensesObj.optJSONObject("praeteritum")
                praeteritum = praetObj?.optSafeString("erSieEs", praetObj.optSafeString("ich", "")).orEmpty().trim()
            }
            if (praeteritum.contains(" ")) {
                val tokens = praeteritum.split(DECLENSION_WHITESPACE_REGEX)
                if (tokens.size >= 2 && (tokens[0].equals("er", true) || tokens[0].contains("/"))) {
                    praeteritum = tokens.last()
                }
            }

            return VerbConjugationResult(
                word = word,
                infinitiv = infinitiv,
                hilfsverb = hilfsverb,
                auxiliary = auxiliary,
                praeteritum = praeteritum,
                partizip1 = partizip1,
                partizip2 = partizip2,
                verbType = verbType,
                separable = separable,
                caseObject = caseObject,
                meaningEnglish = meaningEnglish,
                imperativ = imperativ,
                tenses = tensesList,
                rawJson = jsonStr,
                examples = examplesList,
                ipa = ipa
            )
        }

        private fun createTenseRow(
            key: String,
            nameDe: String,
            container: JSONObject
        ): VerbTenseConjugation {
            val tenseObj = container.optJSONObject(key) ?: JSONObject()
            return VerbTenseConjugation(
                tenseKey = key,
                tenseNameDe = nameDe,
                ich = tenseObj.optSafeString("ich", "-").trim(),
                du = tenseObj.optSafeString("du", "-").trim(),
                erSieEs = tenseObj.optSafeString("erSieEs", tenseObj.optSafeString("er_sie_es", "-")).trim(),
                wir = tenseObj.optSafeString("wir", "-").trim(),
                ihr = tenseObj.optSafeString("ihr", "-").trim(),
                sieSie = tenseObj.optSafeString("sieSie", tenseObj.optSafeString("sie_Sie", "-")).trim()
            )
        }
    }
}

/**
 * Fast glanceable quick details for a German verb:
 * 1. Auxiliary Verb: (hat) or (ist)
 * 2. Präteritum: simple past form (e.g. "nahm" or "bildete")
 * 3. Partizip II: past participle (e.g. "genommen" or "gebildet")
 * 4. Verb Type: "stark", "schwach", "gemischt"
 * 5. Separability: "trennbar" or "untrennbar" (if applicable, otherwise omit)
 * 6. Case object: "Akk.", "Dat.", "Gen." (if applicable, otherwise omit)
 * 7. English Translation: (e.g. "to form, to educate, to build")
 */
data class VerbQuickDetails(
    val verb: String = "",
    val auxiliary: String = "",       // "(hat)" or "(ist)"
    val praeteritum: String = "",     // e.g. "nahm" or "bildete"
    val partizip1: String = "",       // e.g. "bildend"
    val partizip2: String = "",       // e.g. "genommen" or "gebildet"
    val verbType: String = "",        // e.g. "stark" / "schwach" / "gemischt"
    val separable: String = "",       // e.g. "trennbar" / "untrennbar"
    val caseObject: String = "",      // e.g. "Akk." / "Dat." / "Gen."
    val englishMeaning: String = ""   // e.g. "to form, to educate, to build"
) {
    /**
     * Formats the 5 core details + auxiliary + case object in exact order:
     * Details: (hat) · bildete · Part I: bildend · Part II: gebildet · schwach · trennbar · Akk.
     */
    fun formatCoreDetailsString(): String {
        val items = mutableListOf<String>()
        if (auxiliary.isNotBlank()) items.add(auxiliary)
        if (praeteritum.isNotBlank()) items.add(praeteritum)
        if (partizip1.isNotBlank()) {
            val p1Formatted = if (partizip1.startsWith("Part I", ignoreCase = true)) partizip1 else "Part I: $partizip1"
            items.add(p1Formatted)
        }
        if (partizip2.isNotBlank()) {
            val p2Formatted = if (partizip2.startsWith("Part II", ignoreCase = true)) partizip2 else "Part II: $partizip2"
            items.add(p2Formatted)
        }
        if (verbType.isNotBlank()) items.add(verbType)
        if (separable.isNotBlank()) items.add(separable)
        if (caseObject.isNotBlank()) items.add(caseObject)

        val content = items.joinToString(" · ")
        return if (content.isNotBlank()) "Details: $content" else ""
    }

    /**
     * Formats the top-row right side string:
     * e.g. "(hat)  nahm  genommen  ·  stark  ·  Akk."
     * or "(hat)  bildete  gebildet  ·  schwach  ·  Akk."
     */
    fun formatQuickDetailsString(): String {
        val formsPart = listOfNotNull(
            auxiliary.takeIf { it.isNotBlank() },
            praeteritum.takeIf { it.isNotBlank() },
            partizip2.takeIf { it.isNotBlank() }
        ).joinToString("  ")

        val tags = mutableListOf<String>()
        if (formsPart.isNotBlank()) tags.add(formsPart)
        if (verbType.isNotBlank()) tags.add(verbType)
        if (separable.isNotBlank()) tags.add(separable)
        if (caseObject.isNotBlank()) tags.add(caseObject)

        return tags.joinToString("  ·  ")
    }
}

/**
 * Extracts the 5 quick details + English meaning from a verb's raw JSON or fallback fields.
 */
fun extractVerbQuickDetails(
    word: String,
    rawJson: String,
    fallbackHilfsverb: String = "",
    fallbackPartizip2: String = "",
    fallbackMeaning: String = ""
): VerbQuickDetails {
    val cleanWord = word.trim().takeUnless { it.equals("null", ignoreCase = true) }.orEmpty()
    val jsonObj = if (rawJson.isNotBlank()) {
        try { JSONObject(rawJson) } catch (_: Exception) { null }
    } else null

    // 1. Auxiliary
    val rawHilfsverb = jsonObj?.optSafeString("auxiliary")?.takeIf { it.isNotBlank() }
        ?: jsonObj?.optSafeString("hilfsverb")?.takeIf { it.isNotBlank() }
        ?: fallbackHilfsverb.takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }
        ?: ""
    val auxiliary = when {
        rawHilfsverb.isBlank() -> ""
        rawHilfsverb.startsWith("(") && rawHilfsverb.endsWith(")") -> rawHilfsverb
        rawHilfsverb.contains("sein", ignoreCase = true) || rawHilfsverb.equals("ist", ignoreCase = true) -> "(ist)"
        rawHilfsverb.contains("haben", ignoreCase = true) || rawHilfsverb.equals("hat", ignoreCase = true) -> "(hat)"
        else -> "($rawHilfsverb)"
    }

    // 2. Präteritum
    var praeteritum = jsonObj?.optSafeString("praeteritum")?.trim().orEmpty()
    if (praeteritum.isBlank() || praeteritum.startsWith("{")) {
        val tensesObj = jsonObj?.optJSONObject("tenses")
        val praetObj = tensesObj?.optJSONObject("praeteritum")
        praeteritum = praetObj?.optSafeString("erSieEs")?.takeIf { it.isNotBlank() && it != "-" }
            ?: praetObj?.optSafeString("ich")?.takeIf { it.isNotBlank() && it != "-" }
            ?: ""
    }
    if (praeteritum.contains(" ")) {
        val tokens = praeteritum.split(DECLENSION_WHITESPACE_REGEX)
        if (tokens.size >= 2 && (tokens[0].equals("er", true) || tokens[0].contains("/"))) {
            praeteritum = tokens.last()
        }
    }

    // 3. Partizip I
    var partizip1 = jsonObj?.optSafeString("partizip1")?.trim()?.takeIf { it.isNotBlank() }
        ?: jsonObj?.optSafeString("partizip_1")?.trim()?.takeIf { it.isNotBlank() }
        ?: ""
    if (partizip1.isBlank() && cleanWord.isNotBlank()) {
        val trimmedWord = cleanWord.trim()
        partizip1 = if (trimmedWord.endsWith("d", ignoreCase = true)) trimmedWord else "${trimmedWord}d"
    }

    // 4. Partizip II
    var partizip2 = jsonObj?.optSafeString("partizip2")?.trim().orEmpty()
    if (partizip2.isBlank()) {
        partizip2 = fallbackPartizip2.trim().takeUnless { it.equals("null", ignoreCase = true) }.orEmpty()
    }
    if (partizip2.isBlank()) {
        val tensesObj = jsonObj?.optJSONObject("tenses")
        val perfektObj = tensesObj?.optJSONObject("perfekt")
        val perfektEr = perfektObj?.optSafeString("erSieEs")?.takeIf { it.isNotBlank() && it != "-" }
            ?: perfektObj?.optSafeString("ich")?.takeIf { it.isNotBlank() && it != "-" }
            ?: ""
        if (perfektEr.contains(" ")) {
            partizip2 = perfektEr.substringAfterLast(" ").trim()
        }
    }

    // 5. Verb Type
    var verbType = jsonObj?.optSafeString("verbType")?.takeIf { it.isNotBlank() }
        ?: jsonObj?.optSafeString("verb_type")?.takeIf { it.isNotBlank() }
        ?: ""
    verbType = when {
        verbType.contains("stark", ignoreCase = true) -> "stark"
        verbType.contains("schwach", ignoreCase = true) -> "schwach"
        verbType.contains("gemischt", ignoreCase = true) -> "gemischt"
        verbType.contains("unregel", ignoreCase = true) -> "unregelmäßig"
        else -> verbType.trim()
    }

    // 6. Separability
    var separable = jsonObj?.optSafeString("separable")?.trim().orEmpty()
    if (separable.isBlank()) {
        val isSep = if (jsonObj?.has("separable") == true) jsonObj.optBoolean("separable") else null
            ?: if (jsonObj?.has("isSeparable") == true) jsonObj.optBoolean("isSeparable") else null
        if (isSep == true) {
            separable = "trennbar"
        }
    } else {
        separable = when {
            separable.equals("true", ignoreCase = true) || (separable.contains("trennbar", ignoreCase = true) && !separable.contains("untrennbar", ignoreCase = true)) -> "trennbar"
            separable.equals("false", ignoreCase = true) || separable.contains("untrennbar", ignoreCase = true) -> "untrennbar"
            separable == "nein" || separable == "no" || separable == "-" -> ""
            else -> separable
        }
    }

    // 7. Case object
    var caseObject = jsonObj?.optSafeString("caseObject")?.takeIf { it.isNotBlank() }
        ?: jsonObj?.optSafeString("case_object")?.takeIf { it.isNotBlank() }
        ?: ""
    caseObject = when {
        caseObject.contains("akk", ignoreCase = true) && caseObject.contains("dat", ignoreCase = true) -> "Akk. / Dat."
        caseObject.contains("akk", ignoreCase = true) -> "Akk."
        caseObject.contains("dat", ignoreCase = true) -> "Dat."
        caseObject.contains("gen", ignoreCase = true) -> "Gen."
        caseObject == "-" || caseObject.equals("none", ignoreCase = true) || caseObject.equals("kein", ignoreCase = true) -> ""
        else -> caseObject.trim()
    }
    if (caseObject.isBlank() && cleanWord.isNotBlank()) {
        caseObject = getFallbackVerbCaseObject(cleanWord)
    }

    // 8. English Meaning
    val englishMeaning = fallbackMeaning.takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }
        ?: jsonObj?.optSafeString("meaningEnglish")?.takeIf { it.isNotBlank() }
        ?: jsonObj?.optSafeString("english")?.takeIf { it.isNotBlank() }
        ?: jsonObj?.optSafeString("meaning")?.takeIf { it.isNotBlank() }
        ?: ""

    return VerbQuickDetails(
        verb = cleanWord,
        auxiliary = auxiliary,
        praeteritum = praeteritum,
        partizip1 = partizip1,
        partizip2 = partizip2,
        verbType = verbType,
        separable = separable,
        caseObject = caseObject,
        englishMeaning = englishMeaning
    )
}

fun extractVerbQuickDetails(entity: com.example.data.local.WordHistoryEntity): VerbQuickDetails =
    extractVerbQuickDetails(
        word = entity.word,
        rawJson = if (entity.rawJsonResult.isNotBlank()) entity.rawJsonResult else entity.data,
        fallbackHilfsverb = entity.gender,
        fallbackPartizip2 = entity.genderArticle,
        fallbackMeaning = entity.meaningEnglish
    )

/**
 * Fast glanceable quick details for a German noun:
 * 1. Singular with definite article (e.g. "der Stift")
 * 2. Plural with definite article (e.g. "die Stifte")
 * 3. Gender (e.g. "Maskulin", "Feminin", "Neutrum")
 * 4. Genitive Singular (e.g. "des Stiftes")
 * 5. Dative Plural (e.g. "den Stiften")
 * 6. English Translation (e.g. "pen / pencil")
 */
data class NounQuickDetails(
    val singular: String = "",
    val plural: String = "",
    val gender: String = "",
    val genitiveSingular: String = "",
    val dativePlural: String = "",
    val englishMeaning: String = ""
)

/**
 * Extracts glanceable noun details from AI response JSON or fallback history fields.
 */
fun extractNounQuickDetails(
    word: String,
    rawJson: String,
    fallbackGender: String = "",
    fallbackGenderArticle: String = "",
    fallbackMeaning: String = ""
): NounQuickDetails {
    val cleanInputWord = word.trim().takeUnless { it.equals("null", ignoreCase = true) }.orEmpty()
    val jsonObj = if (rawJson.isNotBlank()) {
        try { JSONObject(rawJson) } catch (_: Exception) { null }
    } else null

    // 1. Gender & Gender Article
    val rawGender = jsonObj?.optSafeString("gender")?.takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }
        ?: fallbackGender.takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }
        ?: "Maskulin"

    val gender = when {
        rawGender.contains("mask", ignoreCase = true) -> "Maskulin"
        rawGender.contains("fem", ignoreCase = true) -> "Feminin"
        rawGender.contains("neut", ignoreCase = true) -> "Neutrum"
        else -> rawGender.trim().takeUnless { it.equals("null", ignoreCase = true) }.orEmpty()
    }

    val rawGenderArticle = jsonObj?.optSafeString("genderArticle")?.takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }
        ?: fallbackGenderArticle.takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }
        ?: when (gender.lowercase(Locale.ROOT)) {
            "maskulin" -> "der"
            "feminin" -> "die"
            "neutrum" -> "das"
            else -> ""
        }
    val genderArticle = rawGenderArticle.trim().takeUnless { it.equals("null", ignoreCase = true) }.orEmpty()

    // 2. Plural first (essential fallback for plural-only or plural-dominant nouns like Essgewohnheiten)
    val pluralObj = jsonObj?.optJSONObject("plural")
    val nomPluralObj = pluralObj?.optJSONObject("nominativ")
    var pluralDefinite = jsonObj?.optSafeString("pluralNoun")?.trim().orEmpty()
    if (pluralDefinite.isBlank() || pluralDefinite == "-" || pluralDefinite.equals("null", ignoreCase = true)) {
        pluralDefinite = nomPluralObj?.optSafeString("definite")?.trim().orEmpty()
    }
    if (pluralDefinite.isBlank() || pluralDefinite == "-" || pluralDefinite.equals("null", ignoreCase = true)) {
        val topPlural = jsonObj?.optSafeString("plural")?.trim().orEmpty()
        if (topPlural.isNotBlank() && !topPlural.startsWith("{") && !topPlural.equals("null", ignoreCase = true) && topPlural != "-") {
            pluralDefinite = topPlural
        }
    }
    if (pluralDefinite.isBlank() || pluralDefinite == "-" || pluralDefinite.equals("null", ignoreCase = true)) {
        val noArt = nomPluralObj?.optSafeString("noArticle")?.trim().orEmpty()
        if (noArt.isNotBlank() && noArt != "-" && !noArt.equals("null", ignoreCase = true)) {
            pluralDefinite = if (noArt.startsWith("die ", ignoreCase = true)) noArt else "die $noArt"
        }
    }
    val plural = (when {
        pluralDefinite.isBlank() || pluralDefinite == "-" || pluralDefinite.equals("null", ignoreCase = true) -> ""
        pluralDefinite.startsWith("die ", ignoreCase = true) -> pluralDefinite
        else -> "die $pluralDefinite"
    }).removePrefix("null ").trim().takeUnless { it.equals("null", ignoreCase = true) }.orEmpty()

    // 3. Singular (with definite article, e.g. "der Stift")
    val singularObj = jsonObj?.optJSONObject("singular")
    val nomSingularObj = singularObj?.optJSONObject("nominativ")
    var singularDefinite = nomSingularObj?.optSafeString("definite")?.trim().orEmpty()
    if (singularDefinite.isBlank() || singularDefinite == "-" || singularDefinite.equals("null", ignoreCase = true)) {
        val topSingular = jsonObj?.optSafeString("singular")?.trim().orEmpty()
        if (topSingular.isNotBlank() && !topSingular.startsWith("{") && !topSingular.equals("null", ignoreCase = true) && topSingular != "-") {
            singularDefinite = topSingular
        }
    }
    singularDefinite = singularDefinite
        .replace(LEADING_NULL_REGEX, "")
        .replace(TRAILING_NULL_REGEX, "")
        .trim()
    if (singularDefinite.equals("null", ignoreCase = true) || singularDefinite == "-") {
        singularDefinite = ""
    }

    val jsonWord = jsonObj?.optSafeString("word")?.trim().orEmpty()
    val cleanWord = (if (jsonWord.isNotBlank() && !jsonWord.equals("null", ignoreCase = true)) {
        jsonWord
    } else {
        cleanInputWord
    }).replace(LEADING_NULL_REGEX, "").replace(TRAILING_NULL_REGEX, "").trim()

    val rawSingular = when {
        singularDefinite.isNotBlank() && !singularDefinite.equals("null", ignoreCase = true) && singularDefinite != "-" -> {
            if (!singularDefinite.contains(" ") && genderArticle.isNotBlank() && !genderArticle.equals("null", ignoreCase = true)) {
                "$genderArticle $singularDefinite"
            } else {
                singularDefinite
            }
        }
        genderArticle.isNotBlank() && !genderArticle.equals("null", ignoreCase = true) && cleanWord.isNotBlank() && !cleanWord.equals("null", ignoreCase = true) -> "$genderArticle $cleanWord"
        cleanWord.isNotBlank() && !cleanWord.equals("null", ignoreCase = true) -> cleanWord
        cleanInputWord.isNotBlank() && !cleanInputWord.equals("null", ignoreCase = true) -> cleanInputWord
        plural.isNotBlank() && !plural.equals("null", ignoreCase = true) -> plural
        else -> ""
    }

    val singular = rawSingular
        .replace(LEADING_NULL_REGEX, "")
        .replace(TRAILING_NULL_REGEX, "")
        .trim()
        .takeUnless { it.equals("null", ignoreCase = true) }
        .orEmpty()

    // 4. Genitive Singular (e.g. "des Stiftes")
    var genitiveSingular = jsonObj?.optSafeString("genitiveSingular")?.trim().orEmpty()
    if (genitiveSingular.isBlank() || genitiveSingular == "-") {
        genitiveSingular = jsonObj?.optSafeString("genitive")?.trim().orEmpty()
    }
    if (genitiveSingular.isBlank() || genitiveSingular == "-") {
        val genSingularObj = singularObj?.optJSONObject("genitiv")
        genitiveSingular = genSingularObj?.optSafeString("definite")?.trim().orEmpty()
        if (genitiveSingular.isBlank() || genitiveSingular == "-") {
            val noArt = genSingularObj?.optSafeString("noArticle")?.trim().orEmpty()
            if (noArt.isNotBlank() && noArt != "-") {
                val art = when (gender.lowercase(Locale.ROOT)) {
                    "feminin" -> "der"
                    else -> "des"
                }
                genitiveSingular = "$art $noArt"
            }
        }
    }
    genitiveSingular = genitiveSingular.removePrefix("Gen.:").removePrefix("Gen:").removePrefix("Gen.").trim()
    if (genitiveSingular.equals("null", ignoreCase = true)) genitiveSingular = ""

    // 5. Dative Plural (e.g. "den Stiften")
    var dativePlural = jsonObj?.optSafeString("dativePlural")?.trim().orEmpty()
    if (dativePlural.isBlank() || dativePlural == "-") {
        val datPluralObj = pluralObj?.optJSONObject("dativ")
        dativePlural = datPluralObj?.optSafeString("definite")?.trim().orEmpty()
        if (dativePlural.isBlank() || dativePlural == "-") {
            val noArt = datPluralObj?.optSafeString("noArticle")?.trim().orEmpty()
            if (noArt.isNotBlank() && noArt != "-") {
                dativePlural = if (noArt.startsWith("den ", ignoreCase = true)) noArt else "den $noArt"
            }
        }
    }
    dativePlural = dativePlural.removePrefix("Dat. Pl.:").removePrefix("Dat.Pl.:").removePrefix("Dat. Pl.").removePrefix("Dat:").trim()
    if (dativePlural.equals("null", ignoreCase = true)) dativePlural = ""

    // 6. English Meaning (strictly no Arabic)
    var englishMeaning = jsonObj?.optSafeString("meaningEnglish")?.trim().orEmpty()
    if (englishMeaning.isBlank()) {
        englishMeaning = jsonObj?.optSafeString("english")?.trim().orEmpty()
    }
    if (englishMeaning.isBlank()) {
        englishMeaning = fallbackMeaning.trim().takeUnless { it.equals("null", ignoreCase = true) }.orEmpty()
    }
    if (englishMeaning.isBlank()) {
        englishMeaning = jsonObj?.optSafeString("meaning")?.trim().orEmpty()
    }
    englishMeaning = englishMeaning.removePrefix("Eng.:").removePrefix("Eng:").removePrefix("Eng.").trim()
    if (englishMeaning.equals("null", ignoreCase = true)) englishMeaning = ""
    // Strip any Arabic characters completely
    englishMeaning = englishMeaning.replace(ARABIC_STRIP_REGEX, "").replace(DECLENSION_WHITESPACE_REGEX, " ").trim()
    if (englishMeaning.startsWith("/") || englishMeaning.endsWith("/")) {
        englishMeaning = englishMeaning.trim('/', ' ')
    }

    return NounQuickDetails(
        singular = singular,
        plural = plural,
        gender = gender,
        genitiveSingular = genitiveSingular,
        dativePlural = dativePlural,
        englishMeaning = englishMeaning
    )
}

fun extractNounQuickDetails(entity: com.example.data.local.WordHistoryEntity): NounQuickDetails =
    extractNounQuickDetails(
        word = entity.word,
        rawJson = if (entity.rawJsonResult.isNotBlank()) entity.rawJsonResult else entity.data,
        fallbackGender = entity.gender,
        fallbackGenderArticle = entity.genderArticle,
        fallbackMeaning = entity.meaningEnglish
    )

/**
 * Unified Grammar Result representing either a Noun Declension or a Verb Conjugation.
 */
sealed class GrammarResult {
    abstract val word: String
    abstract val meaningEnglish: String
    abstract val type: GrammarType
    abstract val rawJson: String

    data class Noun(val declension: WordDeclensionResult) : GrammarResult() {
        override val word: String get() = declension.word
        override val meaningEnglish: String get() = declension.meaningEnglish
        override val type: GrammarType get() = GrammarType.NOMEN
        override val rawJson: String get() = declension.rawJson
    }

    data class Verb(val conjugation: VerbConjugationResult) : GrammarResult() {
        override val word: String get() = conjugation.word
        override val meaningEnglish: String get() = conjugation.meaningEnglish
        override val type: GrammarType get() = GrammarType.VERB
        override val rawJson: String get() = conjugation.rawJson
    }

    data class Sentence(val declension: WordDeclensionResult) : GrammarResult() {
        override val word: String get() = declension.word
        override val meaningEnglish: String get() = declension.meaningEnglish
        override val type: GrammarType get() = GrammarType.SENTENCE
        override val rawJson: String get() = declension.rawJson
    }

    data class NomenList(val declension: WordDeclensionResult) : GrammarResult() {
        override val word: String get() = declension.word
        override val meaningEnglish: String get() = declension.meaningEnglish
        override val type: GrammarType get() = GrammarType.NOMEN_LIST
        override val rawJson: String get() = declension.rawJson
    }

    data class Adjektiv(val result: AdjektivResult) : GrammarResult() {
        override val word: String get() = result.word
        override val meaningEnglish: String get() = result.meaningEnglish
        override val type: GrammarType get() = GrammarType.ADJEKTIV
        override val rawJson: String get() = result.rawJson
    }

    companion object {
        fun fromJson(jsonStr: String, fallbackType: GrammarType = GrammarType.NOMEN, fallbackWord: String = ""): GrammarResult {
            val root = JSONObject(jsonStr)
            val typeStr = root.optSafeString("type", "").trim()
            val genderStr = root.optSafeString("gender", "").trim()
            val rawWord = root.optSafeString("word").ifBlank { root.optSafeString("sentence").ifBlank { fallbackWord } }.trim()
            val effectiveWord = if (rawWord.equals("null", ignoreCase = true) || rawWord.isBlank()) fallbackWord.trim() else rawWord

            val isAdjektiv = typeStr.equals("Adjektiv", ignoreCase = true) ||
                    typeStr.equals("Adjektive", ignoreCase = true) ||
                    typeStr.equals("Adjective", ignoreCase = true) ||
                    genderStr.equals("Adjektiv", ignoreCase = true) ||
                    genderStr.equals("Adjektive", ignoreCase = true) ||
                    (root.has("komparativ") && root.has("superlativ")) ||
                    fallbackType == GrammarType.ADJEKTIV

            val isNomenList = !isAdjektiv && (typeStr.equals("NomenList", ignoreCase = true) ||
                    typeStr.equals("Nomen (Liste)", ignoreCase = true) ||
                    typeStr.equals("Wörter", ignoreCase = true) ||
                    typeStr.equals("Wort", ignoreCase = true) ||
                    genderStr.equals("Wörter", ignoreCase = true) ||
                    genderStr.equals("Wort", ignoreCase = true) ||
                    (genderStr.equals("Nomen", ignoreCase = true) && !genderStr.equals("Maskulin", ignoreCase = true)) ||
                    fallbackType == GrammarType.NOMEN_LIST)

            val isSentence = !isAdjektiv && !isNomenList && fallbackType != GrammarType.NOMEN_LIST &&
                    (typeStr.equals("Satz", ignoreCase = true) ||
                    typeStr.equals("Sentence", ignoreCase = true) ||
                    fallbackType == GrammarType.SENTENCE ||
                    effectiveWord.split(DECLENSION_WHITESPACE_COMMA_REGEX).filter { it.isNotBlank() }.size >= 2)

            val isVerb = !isAdjektiv && (typeStr.equals("Verb", ignoreCase = true) ||
                    root.has("tenses") || root.has("infinitiv") ||
                    fallbackType == GrammarType.VERB)

            return when {
                isAdjektiv -> Adjektiv(AdjektivResult.fromJson(jsonStr, fallbackWord = effectiveWord))
                isNomenList -> NomenList(WordDeclensionResult.fromJson(jsonStr, fallbackWord = effectiveWord))
                isSentence -> Sentence(WordDeclensionResult.fromJson(jsonStr, fallbackWord = effectiveWord))
                isVerb && (root.has("tenses") || fallbackType == GrammarType.VERB) -> Verb(VerbConjugationResult.fromJson(jsonStr, fallbackWord = effectiveWord))
                else -> Noun(WordDeclensionResult.fromJson(jsonStr, fallbackWord = effectiveWord))
            }
        }
    }
}

/**
 * Result data model for a German adjective with comparative and superlative forms.
 */
data class AdjektivResult(
    val word: String,
    val komparativ: String,
    val superlativ: String,
    val meaningEnglish: String,
    val examples: List<WordExample> = emptyList(),
    val rawJson: String = "",
    val ipa: String = ""
) {
    companion object {
        fun fromJson(jsonStr: String, fallbackWord: String = ""): AdjektivResult {
            val root = JSONObject(jsonStr)
            val ipa = root.optSafeString("ipa").ifBlank {
                root.optSafeString("pronunciation", root.optSafeString("aussprache", ""))
            }
            val rawAdj = root.optSafeString("adjektiv").ifBlank { root.optSafeString("grundform", "") }
            var rawWord = root.optSafeString("word").ifBlank { rawAdj }
            if (rawWord.isBlank() || rawWord.equals("null", ignoreCase = true)) {
                rawWord = fallbackWord.trim()
            }
            val word = if (rawWord.equals("null", ignoreCase = true) || rawWord.isBlank()) fallbackWord.trim() else rawWord
            val komparativ = root.optSafeString("komparativ").ifBlank { root.optSafeString("comparative", "") }.takeUnless { it.equals("null", ignoreCase = true) }.orEmpty()
            val superlativ = root.optSafeString("superlativ").ifBlank { root.optSafeString("superlative", "") }.takeUnless { it.equals("null", ignoreCase = true) }.orEmpty()
            var meaning = root.optSafeString("meaningEnglish").ifBlank {
                root.optSafeString("meaning").ifBlank {
                    root.optSafeString("translation", "")
                }
            }
            if (meaning.isBlank()) {
                meaning = root.optSafeString("english", root.optSafeString("arabic", "")).trim()
            }
            val examplesList = mutableListOf<WordExample>()
            val examplesArray = root.optJSONArray("examples")
            if (examplesArray != null) {
                for (i in 0 until examplesArray.length()) {
                    val obj = examplesArray.optJSONObject(i)
                    if (obj != null) {
                        val w = obj.optSafeString("word", word).trim()
                        val s = obj.optSafeString("sentence", obj.optSafeString("text", obj.optSafeString("example", ""))).trim()
                        if (s.isNotBlank()) {
                            examplesList.add(WordExample(w, s))
                        }
                    } else {
                        val s = examplesArray.optString(i, "").trim().takeUnless { it.equals("null", ignoreCase = true) }.orEmpty()
                        if (s.isNotBlank()) {
                            examplesList.add(WordExample(word, s))
                        }
                    }
                }
            }
            return AdjektivResult(
                word = word,
                komparativ = komparativ,
                superlativ = superlativ,
                meaningEnglish = meaning,
                examples = examplesList,
                rawJson = jsonStr,
                ipa = ipa
            )
        }
    }
}

/**
 * Fallback mapping for the case governed by common German verbs (Akkusativ / Dativ / Genitiv).
 */
fun getFallbackVerbCaseObject(verb: String): String {
    val v = verb.trim().lowercase()
    return when (v) {
        "bilden", "sehen", "finden", "nehmen", "geben", "haben", "essen", "trinken", "lesen", "schreiben",
        "kaufen", "fragen", "hören", "kochen", "lernen", "machen", "öffnen", "suchen", "tragen",
        "verstehen", "zahlen", "bezahlen", "lieben", "brauchen", "kennen", "treffen", "schließen", "bringen",
        "verlieren", "gewinnen", "vergessen", "erinnern", "bitten", "erwarten", "erklären", "erzählen",
        "besuchen", "anrufen", "einladen", "beginnen", "anfangen", "beenden", "halten", "lassen", "wählen",
        "zeigen", "drücken", "ziehen", "bauen", "benutzen", "nutzen", "erreichen", "empfehlen", "bekommen",
        "erhalten", "schaffen", "versuchen", "prüfen", "wechseln", "verkaufen", "holen", "tauschen" -> "Akk."

        "helfen", "danken", "folgen", "gehören", "gratulieren", "schmecken", "vertrauen", "antworten",
        "begegnen", "gefallen", "glauben", "passen", "schaden", "verzeihen", "wehtun", "zuhören",
        "zustimmen", "widersprechen", "ähneln", "dienen", "fehlen", "gelingen", "nützen", "gehorchen",
        "genügen", "passieren", "zusehen" -> "Dat."

        "gedenken", "bedürfen", "anklagen", "beschuldigen", "überführen", "entbehren",
        "rühmen", "schämen", "belehren", "verdächtigen", "harren", "brüsten" -> "Gen."

        else -> ""
    }
}

/**
 * Resolves 2-3 short German example sentences for a verb.
 */
fun resolveVerbExamples(
    word: String,
    rawJson: String = "",
    existingExamples: List<WordExample> = emptyList()
): List<WordExample> {
    if (existingExamples.isNotEmpty()) {
        return existingExamples
    }
    if (rawJson.isNotBlank()) {
        try {
            val root = JSONObject(rawJson)
            val examplesArray = root.optJSONArray("examples")
            if (examplesArray != null && examplesArray.length() > 0) {
                val list = mutableListOf<WordExample>()
                for (i in 0 until examplesArray.length()) {
                    val obj = examplesArray.optJSONObject(i)
                    if (obj != null) {
                        val w = obj.optString("word", word).trim()
                        val s = obj.optString("sentence", obj.optString("text", obj.optString("example", ""))).trim()
                        if (s.isNotBlank()) {
                            list.add(WordExample(w, s))
                        }
                    } else {
                        val s = examplesArray.optString(i, "").trim()
                        if (s.isNotBlank()) {
                            list.add(WordExample(word, s))
                        }
                    }
                }
                if (list.isNotEmpty()) return list
            }
        } catch (_: Exception) {}
    }

    val trimmedWord = word.trim().lowercase(Locale.ROOT)

    val curatedMap = mapOf(
        "kennen" to listOf(
            "Ich kenne ihn seit vielen Jahren.",
            "Kennst du diese schöne Stadt?",
            "Wir kennen die Antwort auf diese Frage."
        ),
        "arbeiten" to listOf(
            "Ich arbeite jeden Tag im Büro.",
            "Er arbeitet sehr fleißig an dem Projekt.",
            "Sie haben gestern lange gearbeitet."
        ),
        "gehen" to listOf(
            "Ich gehe jeden Morgen im Park spazieren.",
            "Wohin gehst du heute Abend?",
            "Wir sind gestern ins Kino gegangen."
        ),
        "haben" to listOf(
            "Ich habe heute viel Zeit.",
            "Hast du ein schönes Buch zum Lesen?",
            "Sie hatten gestern ein gutes Gespräch."
        ),
        "sein" to listOf(
            "Ich bin heute sehr glücklich.",
            "Das Wetter ist wunderschön.",
            "Wir waren gestern zusammen zu Hause."
        ),
        "lernen" to listOf(
            "Ich lerne jeden Tag fleißig Deutsch.",
            "Sie lernt neue Wörter für die Prüfung.",
            "Wir haben zusammen viel gelernt."
        ),
        "machen" to listOf(
            "Was machst du heute Nachmittag?",
            "Er macht seine Hausaufgaben sorgfältig.",
            "Wir machen am Wochenende einen Ausflug."
        ),
        "kochen" to listOf(
            "Mein Hobby ist Kochen in der Küche.",
            "Sie kocht abends eine leckere Suppe.",
            "Wir kochen gerne zusammen für Freunde."
        ),
        "fahren" to listOf(
            "Ich fahre morgen mit dem Zug nach Berlin.",
            "Fährst du gerne mit dem Fahrrad?",
            "Wir sind schnell nach Hause gefahren."
        ),
        "lesen" to listOf(
            "Ich lese gerne spannende Bücher.",
            "Liest du jeden Tag die Zeitung?",
            "Er hat den ganzen Abend gelesen."
        ),
        "schreiben" to listOf(
            "Ich schreibe eine E-Mail an meine Freunde.",
            "Er schreibt ein interessantes Buch.",
            "Sie hat eine kurze Nachricht geschrieben."
        ),
        "sprechen" to listOf(
            "Sprechen Sie gut Deutsch?",
            "Er spricht drei verschiedene Sprachen.",
            "Wir haben lange miteinander gesprochen."
        ),
        "sehen" to listOf(
            "Ich sehe einen schönen Regenbogen am Himmel.",
            "Siehst du den großen gelben Vogel dort?",
            "Wir haben einen tollen Film gesehen."
        ),
        "essen" to listOf(
            "Wir essen morgens frisches Brot.",
            "Was isst du am liebsten zum Mittagessen?",
            "Sie haben köstliche Pizza gegessen."
        ),
        "trinken" to listOf(
            "Ich trinke morgens gerne eine Tasse Kaffee.",
            "Trinkst du lieber Tee oder Wasser?",
            "Wir haben kühles Wasser getrunken."
        ),
        "wohnen" to listOf(
            "Ich wohne in einer ruhigen Straße.",
            "Wo wohnst du zurzeit?",
            "Sie wohnen schon lange in Berlin."
        ),
        "kommen" to listOf(
            "Woher kommst du ursprünglich?",
            "Er kommt pünktlich zum Treffen.",
            "Wir sind gestern spät nach Hause gekommen."
        ),
        "bilden" to listOf(
            "Wir bilden zusammen einen Satz.",
            "Er bildet sich jeden Tag weiter.",
            "Sie haben eine neue Gruppe gebildet."
        )
    )

    val curated = curatedMap[trimmedWord]
    if (curated != null) {
        return curated.map { WordExample(word, it) }
    }

    val displayWord = word.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
    return listOf(
        WordExample(word, "Ich möchte gerne '$displayWord' im täglichen Leben üben."),
        WordExample(word, "Er versucht, das Verb '$displayWord' richtig zu gebrauchen."),
        WordExample(word, "Wir haben das Verb '$displayWord' heute gelernt.")
    )
}


