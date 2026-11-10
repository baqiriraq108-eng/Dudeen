package com.example.util

/**
 * Checks if a string contains any Arabic characters (Unicode ranges for Arabic script).
 */
fun isArabicText(text: String?): Boolean {
    if (text.isNullOrBlank()) return false
    for (char in text) {
        if (char in '\u0600'..'\u06FF' ||
            char in '\u0750'..'\u077F' ||
            char in '\u08A0'..'\u08FF' ||
            char in '\uFB50'..'\uFDFF' ||
            char in '\uFE70'..'\uFEFF'
        ) {
            return true
        }
    }
    return false
}

/**
 * Cleans translation text removing redundant prefixes and trimming whitespace.
 */
fun cleanTranslationText(rawMeaning: String?, isArabic: Boolean): String {
    if (rawMeaning.isNullOrBlank()) return ""
    var raw = rawMeaning.trim()
    val prefixes = listOf(
        "الترجمة العربية :", "الترجمة العربية:", "الترجمة:", "الترجمة :", "ترجمة:",
        "Arabisch:", "Arab.:", "Arab:", "Arabic:",
        "English:", "Eng.:", "Eng:"
    )
    for (p in prefixes) {
        if (raw.startsWith(p, ignoreCase = true)) {
            raw = raw.substring(p.length).trim()
        }
    }
    return raw.replace(Regex("\\s+"), " ").trim()
}

/**
 * Normalizes Arabic text for searching and comparison:
 * - Removes Harakat / Tashkeel diacritics
 * - Normalizes Alef forms (أ, إ, آ, ٱ -> ا)
 * - Normalizes Taa Marbuta (ة -> ه)
 * - Normalizes Yaa forms (ى -> ي)
 * - Removes Tatweel / Kashida (ـ)
 */
fun normalizeArabic(text: String?): String {
    if (text.isNullOrBlank()) return ""
    var s = text.trim()
    // Remove diacritics (fatha, damma, kasra, sukun, shadda, tanween, etc.)
    s = s.replace(Regex("[\u064B-\u065F\u0670]"), "")
    // Remove tatweel
    s = s.replace("\u0640", "")
    // Normalize Alef
    s = s.replace(Regex("[أإآٱ]"), "ا")
    // Normalize Taa Marbuta
    s = s.replace("ة", "ه")
    // Normalize Yaa
    s = s.replace("ى", "ي")
    return s.trim()
}

/**
 * Parses a stored manual translation string into a clean list of individual translations.
 * Handles newlines and strips redundant "Alt.: " prefixes from each line.
 */
fun parseManualTranslations(raw: String?): List<String> {
    if (raw.isNullOrBlank()) return emptyList()
    val lines = raw.split("\n", "\r\n")
    val list = mutableListOf<String>()
    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.isNotBlank()) {
            val isAr = isArabicText(trimmed)
            var cleaned = cleanTranslationText(trimmed, isAr)
            val altPrefixes = listOf("Alt.:", "Alt:", "Alt.: ", "Alt: ", "(Alt.:", "(Alt:")
            for (prefix in altPrefixes) {
                if (cleaned.startsWith(prefix, ignoreCase = true)) {
                    cleaned = cleaned.substring(prefix.length).trim().removeSuffix(")")
                }
            }
            if (cleaned.isNotBlank() && !list.contains(cleaned)) {
                list.add(cleaned)
            }
        }
    }
    return list
}

/**
 * Formats a list of manual translations into a persistent string separated by newlines.
 */
fun formatManualTranslations(translations: List<String>): String {
    return translations
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
        .joinToString("\n")
}
