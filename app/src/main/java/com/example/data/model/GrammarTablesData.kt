package com.example.data.model

import androidx.compose.runtime.Immutable

/**
 * Data structures for German Grammar Reference Tables (Grammatik-Tabellen)
 * with full multilingual localization (German, English, Arabic).
 */
@Immutable
data class GrammarTableCategory(
    val id: String,
    val titleDe: String,
    val titleEn: String = "",
    val titleAr: String = "",
    val iconName: String,
    val descriptionDe: String,
    val descriptionEn: String = "",
    val descriptionAr: String = "",
    val tables: List<GrammarReferenceTable>
) {
    fun localizedTitle(lang: String): String = when (lang.lowercase()) {
        "en" -> titleEn.ifBlank { titleDe }
        "ar" -> titleAr.ifBlank { titleDe }
        else -> titleDe
    }

    fun localizedDescription(lang: String): String = when (lang.lowercase()) {
        "en" -> descriptionEn.ifBlank { descriptionDe }
        "ar" -> descriptionAr.ifBlank { descriptionDe }
        else -> descriptionDe
    }
}

@Immutable
data class GrammarReferenceTable(
    val id: String,
    val titleDe: String,
    val titleEn: String = "",
    val titleAr: String = "",
    val subtitleDe: String = "",
    val subtitleEn: String = "",
    val subtitleAr: String = "",
    val headers: List<String>,
    val headersEn: List<String> = emptyList(),
    val headersAr: List<String> = emptyList(),
    val rows: List<GrammarTableRow>,
    val notesDe: String = "",
    val notesEn: String = "",
    val notesAr: String = "",
    val examples: List<GrammarExample> = emptyList()
) {
    fun localizedTitle(lang: String): String = when (lang.lowercase()) {
        "en" -> titleEn.ifBlank { titleDe }
        "ar" -> titleAr.ifBlank { titleDe }
        else -> titleDe
    }

    fun localizedSubtitle(lang: String): String = when (lang.lowercase()) {
        "en" -> subtitleEn.ifBlank { subtitleDe }
        "ar" -> subtitleAr.ifBlank { subtitleDe }
        else -> subtitleDe
    }

    fun localizedNotes(lang: String): String = when (lang.lowercase()) {
        "en" -> notesEn.ifBlank { notesDe }
        "ar" -> notesAr.ifBlank { notesDe }
        else -> notesDe
    }

    fun localizedHeaders(lang: String): List<String> = when (lang.lowercase()) {
        "en" -> if (headersEn.isNotEmpty()) headersEn else headers
        "ar" -> if (headersAr.isNotEmpty()) headersAr else headers
        else -> headers
    }
}

@Immutable
data class GrammarTableRow(
    val label: String,
    val cells: List<String>,
    val speakText: String = "",
    val labelEn: String = "",
    val labelAr: String = ""
) {
    fun localizedLabel(lang: String): String = when (lang.lowercase()) {
        "en" -> labelEn.ifBlank { label }
        "ar" -> labelAr.ifBlank { label }
        else -> label
    }
}

@Immutable
data class GrammarExample(
    val german: String,
    val translation: String,
    val translationAr: String = "",
    val ruleHint: String = "",
    val ruleHintEn: String = "",
    val ruleHintAr: String = ""
) {
    fun localizedTranslation(lang: String): String = when (lang.lowercase()) {
        "ar" -> translationAr.ifBlank { translation }
        else -> translation
    }

    fun localizedRuleHint(lang: String): String = when (lang.lowercase()) {
        "en" -> ruleHintEn.ifBlank { ruleHint }
        "ar" -> ruleHintAr.ifBlank { ruleHint }
        else -> ruleHint
    }
}

/**
 * Phonetic guide for German pronunciation.
 */
@Immutable
data class PronunciationRule(
    val soundLetter: String,
    val ipa: String,
    val explanationDe: String,
    val explanationEn: String = "",
    val explanationAr: String = "",
    val exampleWords: List<String>
) {
    fun localizedExplanation(lang: String): String = when (lang.lowercase()) {
        "en" -> explanationEn.ifBlank { explanationDe }
        "ar" -> explanationAr.ifBlank { explanationDe }
        else -> explanationDe
    }
}

/**
 * Preposition item with translation in English and Arabic, and example sentences.
 */
@Immutable
data class PrepositionItem(
    val word: String,
    val meaningEn: String,
    val meaningAr: String,
    val exampleDe: String = "",
    val exampleEn: String = "",
    val exampleAr: String = ""
) {
    fun localizedMeaning(lang: String): String = when (lang.lowercase()) {
        "en" -> meaningEn
        "ar" -> meaningAr
        else -> meaningEn
    }

    fun localizedExample(lang: String): String = when (lang.lowercase()) {
        "ar" -> exampleAr.ifBlank { exampleEn }
        else -> exampleEn
    }
}

/**
 * Dedicated data model for Fixed Preposition Groups (Akkusativ, Dativ, Genitiv).
 */
@Immutable
data class PrepositionGroup(
    val kasusId: String,
    val kasusTitleDe: String,
    val kasusTitleEn: String,
    val kasusTitleAr: String,
    val questionDe: String,
    val questionEn: String,
    val questionAr: String,
    val prepositions: List<PrepositionItem>,
    val mnemonicDe: String,
    val mnemonicEn: String,
    val mnemonicAr: String,
    val ruleNoteDe: String,
    val ruleNoteEn: String,
    val ruleNoteAr: String,
    val examples: List<GrammarExample>,
    val speakText: String
) {
    fun localizedKasusTitle(lang: String): String = when (lang.lowercase()) {
        "en" -> kasusTitleEn
        "ar" -> kasusTitleAr
        else -> kasusTitleDe
    }

    fun localizedQuestion(lang: String): String = when (lang.lowercase()) {
        "en" -> questionEn
        "ar" -> questionAr
        else -> questionDe
    }

    fun localizedMnemonic(lang: String): String = when (lang.lowercase()) {
        "en" -> mnemonicEn.ifBlank { mnemonicDe }
        "ar" -> mnemonicAr.ifBlank { mnemonicDe }
        else -> mnemonicDe
    }

    fun localizedRuleNote(lang: String): String = when (lang.lowercase()) {
        "en" -> ruleNoteEn.ifBlank { ruleNoteDe }
        "ar" -> ruleNoteAr.ifBlank { ruleNoteDe }
        else -> ruleNoteDe
    }
}

/**
 * Dedicated data model for Two-Way Prepositions (Wechselpräpositionen).
 */
@Immutable
data class TwoWayPrepositionItem(
    val word: String,
    val meaningEn: String,
    val meaningAr: String,
    val akkusativGerman: String,
    val akkusativEn: String,
    val akkusativAr: String,
    val dativGerman: String,
    val dativEn: String,
    val dativAr: String,
    val speakTextAkk: String = akkusativGerman,
    val speakTextDat: String = dativGerman
) {
    fun localizedMeaning(lang: String): String = when (lang.lowercase()) {
        "en" -> meaningEn
        "ar" -> meaningAr
        else -> meaningEn
    }

    fun localizedAkkusativ(lang: String): String = when (lang.lowercase()) {
        "ar" -> akkusativAr.ifBlank { akkusativEn }
        else -> akkusativEn
    }

    fun localizedDativ(lang: String): String = when (lang.lowercase()) {
        "ar" -> dativAr.ifBlank { dativEn }
        else -> dativEn
    }
}

/**
 * Central repository of German Grammar Tables — streamlined for fast, distraction-free study.
 */
object GrammarTablesData {

    val PRONUNCIATION_GUIDE = listOf(
        PronunciationRule(
            soundLetter = "Ä / ä",
            ipa = "[ɛː] / [ɛ]",
            explanationDe = "Offenes 'e' (wie in Käse, Männer)",
            explanationEn = "Open 'e' (like 'e' in bed / fair)",
            explanationAr = "صوت 'e' مفتوح (مثل الألف الممالة)",
            exampleWords = listOf("Käse", "Männer", "Äpfel", "spät")
        ),
        PronunciationRule(
            soundLetter = "Ö / ö",
            ipa = "[øː] / [œ]",
            explanationDe = "Lippen runden wie 'o', 'e' sprechen",
            explanationEn = "Round lips for 'o', say 'e'",
            explanationAr = "ضم الشفتين كـ 'o' مع نطق 'e'",
            exampleWords = listOf("schön", "König", "öffnen", "Löffel")
        ),
        PronunciationRule(
            soundLetter = "Ü / ü",
            ipa = "[yː] / [ʏ]",
            explanationDe = "Lippen runden wie 'u', 'i' sprechen",
            explanationEn = "Round lips for 'u', say 'ee'",
            explanationAr = "ضم الشفتين كـ 'u' مع نطق 'i'",
            exampleWords = listOf("über", "grün", "fünf", "Mütter")
        ),
        PronunciationRule(
            soundLetter = "EI / AI",
            ipa = "[aɪ̯]",
            explanationDe = "Klingt wie 'ai' (Eis, Mai)",
            explanationEn = "Sounds like 'eye' (mine)",
            explanationAr = "ينطق مثل 'آي'",
            exampleWords = listOf("Eis", "Reise", "Mai", "arbeiten")
        ),
        PronunciationRule(
            soundLetter = "EU / ÄU",
            ipa = "[ɔɪ̯]",
            explanationDe = "Klingt wie 'oi' (Euro, Häuser)",
            explanationEn = "Sounds like 'oy' (boy, coin)",
            explanationAr = "ينطق مثل 'أوي'",
            exampleWords = listOf("Euro", "Freund", "Häuser", "heute")
        ),
        PronunciationRule(
            soundLetter = "AU",
            ipa = "[aʊ̯]",
            explanationDe = "Klingt wie 'au' (Haus, Baum)",
            explanationEn = "Sounds like 'ow' (how, cow)",
            explanationAr = "ينطق مثل 'آو'",
            exampleWords = listOf("Haus", "Baum", "Auto", "Frau")
        ),
        PronunciationRule(
            soundLetter = "CH (ich)",
            ipa = "[ç]",
            explanationDe = "Weich nach e, i, ä, ö, ü, ei, eu",
            explanationEn = "Soft after e, i, ä, ö, ü, ei, eu",
            explanationAr = "شين مخففة بعد e, i, ä, ö, ü",
            exampleWords = listOf("ich", "Milch", "Recht", "Bücher")
        ),
        PronunciationRule(
            soundLetter = "CH (ach)",
            ipa = "[x]",
            explanationDe = "Kehllaut nach a, o, u, au",
            explanationEn = "Throat sound after a, o, u, au",
            explanationAr = "ينطق كالخاء بعد a, o, u, au",
            exampleWords = listOf("Nacht", "Buch", "Kochen", "Sprache")
        ),
        PronunciationRule(
            soundLetter = "SCH",
            ipa = "[ʃ]",
            explanationDe = "Zischlaut wie in 'Schule'",
            explanationEn = "Sounds like 'sh' in 'shoe'",
            explanationAr = "ينطق كحرف الشين",
            exampleWords = listOf("Schule", "schön", "Tasche", "Schreiben")
        ),
        PronunciationRule(
            soundLetter = "ST / SP",
            ipa = "[ʃt] / [ʃp]",
            explanationDe = "Am Wortanfang wie 'scht' / 'schp'",
            explanationEn = "At word start: 'sht' / 'shp'",
            explanationAr = "في بداية الكلمة: 'شت' / 'شب'",
            exampleWords = listOf("Stadt", "sprechen", "Sport", "stehen")
        ),
        PronunciationRule(
            soundLetter = "IG",
            ipa = "[ɪç]",
            explanationDe = "Am Wortende wie weiches '-ich'",
            explanationEn = "At word end: soft '-ich'",
            explanationAr = "في نهاية الكلمة ينطق '-يش' خفيفة",
            exampleWords = listOf("richtig", "wichtig", "fertig", "König")
        ),
        PronunciationRule(
            soundLetter = "V",
            ipa = "[f] / [v]",
            explanationDe = "Meist wie 'F' (Vater), teils 'W' (Vase)",
            explanationEn = "Usually 'F' (Vater), or 'V' (Vase)",
            explanationAr = "ينطق كالفاء غالباً، أو V في الدخيل",
            exampleWords = listOf("Vater", "Vogel", "viel", "Vase")
        ),
        PronunciationRule(
            soundLetter = "W",
            ipa = "[v]",
            explanationDe = "Wie englisches 'V' (Wasser)",
            explanationEn = "Like English 'V' (Wasser)",
            explanationAr = "ينطق مثل 'V' الإنجليزية",
            exampleWords = listOf("Wasser", "Welt", "wohnen", "Wort")
        ),
        PronunciationRule(
            soundLetter = "Z / TZ",
            ipa = "[t͡s]",
            explanationDe = "Scharfes 'ts' (Zeit, Platz)",
            explanationEn = "Sharp 'ts' sound (cats)",
            explanationAr = "ينطق كصوت 'تس'",
            exampleWords = listOf("Zeit", "Zimmer", "Katze", "Platz")
        ),
        PronunciationRule(
            soundLetter = "S",
            ipa = "[z]",
            explanationDe = "Vor Vokal stimmhaft (Sonne, See)",
            explanationEn = "Voiced 'z' before vowels",
            explanationAr = "ينطق كالزاي (Z) قبل حروف العلة",
            exampleWords = listOf("Sonne", "See", "Suppe", "sagen")
        )
    )

    /**
     * Fixed preposition groups (Akkusativ, Dativ, Genitiv) — concise and clean.
     */
    val FIXED_PREPOSITION_GROUPS: List<PrepositionGroup> = listOf(
        PrepositionGroup(
            kasusId = "akkusativ",
            kasusTitleDe = "Akkusativ",
            kasusTitleEn = "Akkusativ",
            kasusTitleAr = "النصب (Akkusativ)",
            questionDe = "Wen? / Was?",
            questionEn = "Wen? / Was? (Whom? / What?)",
            questionAr = "Wen? / Was? (مَن؟ / ماذا؟)",
            prepositions = listOf(
                PrepositionItem("bis", "until", "حتى", "Der Zug fährt bis Hamburg.", "The train goes until Hamburg.", "القطار يسير حتى هامبورغ."),
                PrepositionItem("durch", "through", "عبر", "Ich gehe durch den Park.", "I walk through the park.", "أمشي عبر الحديقة."),
                PrepositionItem("für", "for", "لأجل", "Das Geschenk ist für dich.", "The gift is for you.", "الهدية من أجلك."),
                PrepositionItem("gegen", "against", "ضد / نحو", "Das Auto fuhr gegen die Wand.", "The car drove against the wall.", "اصطدمت السيارة بالجدار."),
                PrepositionItem("ohne", "without", "بدون", "Ohne dich gehe ich nicht.", "Without you I don't go.", "بدونك لن أذهب."),
                PrepositionItem("um", "around / at", "حول / عند", "Wir sitzen um den Tisch.", "We sit around the table.", "نجلس حول الطاولة."),
                PrepositionItem("entlang", "along", "بمحاذاة", "Geh den Fluss entlang.", "Walk along the river.", "امشِ بمحاذاة النهر.")
            ),
            mnemonicDe = "DOGFU: durch, ohne, gegen, für, um",
            mnemonicEn = "DOGFU: durch, ohne, gegen, für, um",
            mnemonicAr = "DOGFU: durch, ohne, gegen, für, um",
            ruleNoteDe = "Immer mit Akkusativ.",
            ruleNoteEn = "Always takes Accusative.",
            ruleNoteAr = "تأتي دائماً مع Akkusativ.",
            examples = emptyList(),
            speakText = "Akkusativ"
        ),
        PrepositionGroup(
            kasusId = "dativ",
            kasusTitleDe = "Dativ",
            kasusTitleEn = "Dativ",
            kasusTitleAr = "الجر (Dativ)",
            questionDe = "Wem? / Wo?",
            questionEn = "Wem? / Wo? (To whom? / Where?)",
            questionAr = "Wem? / Wo? (لمَن؟ / أين؟)",
            prepositions = listOf(
                PrepositionItem("aus", "from", "مِن", "Er kommt aus der Schweiz.", "He comes from Switzerland.", "هو قادم من سويسرا."),
                PrepositionItem("bei", "at / with", "عند / لدى", "Ich wohne bei meiner Familie.", "I live with my family.", "أعيش عند عائلتي."),
                PrepositionItem("mit", "with / by", "مع / بـ", "Ich fahre mit dem Bus.", "I go by bus.", "أسافر بالحافلة."),
                PrepositionItem("nach", "after / to", "بعد / إلى", "Nach dem Kurs gehe ich heim.", "After class I go home.", "بعد الدرس أذهب للمنزل."),
                PrepositionItem("seit", "since / for", "منذ", "Seit einem Jahr lerne ich Deutsch.", "I've learned German for a year.", "منذ سنة أتعلم الألمانية."),
                PrepositionItem("von", "from / of", "مِن / عن", "Ein Geschenk von meiner Mutter.", "A gift from my mother.", "هدية من أمي."),
                PrepositionItem("zu", "to", "إلى", "Ich gehe zum Bahnhof.", "I walk to the station.", "أذهب إلى المحطة."),
                PrepositionItem("gegenüber", "opposite", "مقابل", "Gegenüber dem Bahnhof.", "Opposite the station.", "مقابل المحطة."),
                PrepositionItem("außer", "except", "ما عدا", "Alle außer meinem Bruder.", "Everyone except my brother.", "الجميع ما عدا أخي.")
            ),
            mnemonicDe = "ABMNSVZ: aus, bei, mit, nach, seit, von, zu",
            mnemonicEn = "ABMNSVZ: aus, bei, mit, nach, seit, von, zu",
            mnemonicAr = "ABMNSVZ: aus, bei, mit, nach, seit, von, zu",
            ruleNoteDe = "Immer mit Dativ.",
            ruleNoteEn = "Always takes Dative.",
            ruleNoteAr = "تأتي دائماً مع Dativ.",
            examples = emptyList(),
            speakText = "Dativ"
        ),
        PrepositionGroup(
            kasusId = "genitiv",
            kasusTitleDe = "Genitiv",
            kasusTitleEn = "Genitiv",
            kasusTitleAr = "الإضافة (Genitiv)",
            questionDe = "Wessen?",
            questionEn = "Wessen? (Whose?)",
            questionAr = "Wessen? (لمَن؟)",
            prepositions = listOf(
                PrepositionItem("während", "during", "أثناء", "Während der Reise las ich viel.", "During the trip I read a lot.", "أثناء الرحلة قرأت كثيراً."),
                PrepositionItem("wegen", "because of", "بسبب", "Wegen des Regens bleiben wir hier.", "Because of the rain we stay here.", "بسبب المطر نبقى هنا."),
                PrepositionItem("trotz", "despite", "رغم", "Trotz des Regens gehen wir raus.", "Despite the rain we go out.", "رغم المطر نخرج."),
                PrepositionItem("anstatt", "instead of", "بدلاً من", "Statt eines Autos kauft er ein Rad.", "Instead of a car he buys a bike.", "بدلاً من سيارة يشتري دراجة."),
                PrepositionItem("außerhalb", "outside", "خارج", "Er wohnt außerhalb der Stadt.", "He lives outside the city.", "يسكن خارج المدينة."),
                PrepositionItem("innerhalb", "within", "داخل / خلال", "Innerhalb eines Tages.", "Within one day.", "خلال يوم واحد.")
            ),
            mnemonicDe = "während, wegen, trotz, (an)statt",
            mnemonicEn = "während, wegen, trotz, (an)statt",
            mnemonicAr = "während, wegen, trotz, (an)statt",
            ruleNoteDe = "Immer mit Genitiv (des / der).",
            ruleNoteEn = "Always takes Genitive (des / der).",
            ruleNoteAr = "تأتي دائماً مع Genitiv (des / der).",
            examples = emptyList(),
            speakText = "Genitiv"
        )
    )

    /**
     * Two-way prepositions (Wechselpräpositionen) — concise meanings and examples.
     */
    val TWO_WAY_PREPOSITIONS: List<TwoWayPrepositionItem> = listOf(
        TwoWayPrepositionItem(
            word = "an",
            meaningEn = "at / on",
            meaningAr = "على / عند",
            akkusativGerman = "Ich hänge das Bild an die Wand.",
            akkusativEn = "I hang the picture on the wall.",
            akkusativAr = "أعلق الصورة على الحائط.",
            dativGerman = "Das Bild hängt an der Wand.",
            dativEn = "The picture hangs on the wall.",
            dativAr = "الصورة معلقة على الحائط."
        ),
        TwoWayPrepositionItem(
            word = "auf",
            meaningEn = "on / onto",
            meaningAr = "على (أفقي)",
            akkusativGerman = "Ich lege das Buch auf den Tisch.",
            akkusativEn = "I put the book on the table.",
            akkusativAr = "أضع الكتاب على الطاولة.",
            dativGerman = "Das Buch liegt auf dem Tisch.",
            dativEn = "The book lies on the table.",
            dativAr = "الكتاب على الطاولة."
        ),
        TwoWayPrepositionItem(
            word = "hinter",
            meaningEn = "behind",
            meaningAr = "خلف",
            akkusativGerman = "Er geht hinter das Haus.",
            akkusativEn = "He walks behind the house.",
            akkusativAr = "يمشي إلى خلف المنزل.",
            dativGerman = "Er steht hinter dem Haus.",
            dativEn = "He stands behind the house.",
            dativAr = "يقف خلف المنزل."
        ),
        TwoWayPrepositionItem(
            word = "in",
            meaningEn = "in / into",
            meaningAr = "في / إلى",
            akkusativGerman = "Wir gehen in die Stadt.",
            akkusativEn = "We go into town.",
            akkusativAr = "نذهب إلى المدينة.",
            dativGerman = "Wir sind in der Stadt.",
            dativEn = "We are in town.",
            dativAr = "نحن في المدينة."
        ),
        TwoWayPrepositionItem(
            word = "neben",
            meaningEn = "next to",
            meaningAr = "بجانب",
            akkusativGerman = "Stell den Stuhl neben den Tisch.",
            akkusativEn = "Put the chair next to the table.",
            akkusativAr = "ضع الكرسي بجانب الطاولة.",
            dativGerman = "Der Stuhl steht neben dem Tisch.",
            dativEn = "The chair is next to the table.",
            dativAr = "الكرسي بجانب الطاولة."
        ),
        TwoWayPrepositionItem(
            word = "über",
            meaningEn = "over / above",
            meaningAr = "فوق",
            akkusativGerman = "Er hängt die Lampe über den Tisch.",
            akkusativEn = "He hangs the lamp over the table.",
            akkusativAr = "يعلق المصباح فوق الطاولة.",
            dativGerman = "Die Lampe hängt über dem Tisch.",
            dativEn = "The lamp hangs over the table.",
            dativAr = "المصباح معلق فوق الطاولة."
        ),
        TwoWayPrepositionItem(
            word = "unter",
            meaningEn = "under",
            meaningAr = "تحت",
            akkusativGerman = "Die Katze läuft unter das Bett.",
            akkusativEn = "The cat runs under the bed.",
            akkusativAr = "القطة تركض تحت السرير.",
            dativGerman = "Die Katze schläft unter dem Bett.",
            dativEn = "The cat sleeps under the bed.",
            dativAr = "القطة تنام تحت السرير."
        ),
        TwoWayPrepositionItem(
            word = "vor",
            meaningEn = "in front of",
            meaningAr = "أمام",
            akkusativGerman = "Fahr das Auto vor die Garage.",
            akkusativEn = "Drive the car in front of the garage.",
            akkusativAr = "قُد السيارة أمام المرآب.",
            dativGerman = "Das Auto steht vor der Garage.",
            dativEn = "The car stands in front of the garage.",
            dativAr = "السيارة أمام المرآب."
        ),
        TwoWayPrepositionItem(
            word = "zwischen",
            meaningEn = "between",
            meaningAr = "بين",
            akkusativGerman = "Setz dich zwischen die Freunde.",
            akkusativEn = "Sit between the friends.",
            akkusativAr = "اجلس بين الأصدقاء.",
            dativGerman = "Ich sitze zwischen den Freunden.",
            dativEn = "I sit between the friends.",
            dativAr = "أنا جالس بين الأصدقاء."
        )
    )

    val CATEGORIES: List<GrammarTableCategory> = listOf(
        GrammarTableCategory(
            id = "pronomen",
            titleDe = "Pronomen",
            titleEn = "Pronouns",
            titleAr = "الضمائر",
            iconName = "Psychology",
            descriptionDe = "Personal-, Reflexiv-, Demonstrativ-, Frage- und Indefinitpronomen.",
            descriptionEn = "Personal, reflexive, demonstrative, interrogative, and indefinite pronouns.",
            descriptionAr = "الضمائر الشخصية والانعكاسية والإشارية والاستفهامية.",
            tables = listOf(
                GrammarReferenceTable(
                    id = "personalpronomen",
                    titleDe = "Personalpronomen",
                    titleEn = "Personal Pronouns",
                    titleAr = "الضمائر الشخصية",
                    subtitleDe = "Alle 4 Kasus",
                    subtitleEn = "All 4 cases",
                    subtitleAr = "الحالات الإعرابية الأربع",
                    headers = listOf("Person", "Nominativ", "Akkusativ", "Dativ", "Genitiv"),
                    headersEn = listOf("Person", "Nominative", "Accusative", "Dative", "Genitive"),
                    headersAr = listOf("الضمير", "Nominativ", "Akkusativ", "Dativ", "Genitiv"),
                    rows = listOf(
                        GrammarTableRow("ich", listOf("ich", "mich", "mir", "meiner"), "ich", "ich (I)", "ich (أنا)"),
                        GrammarTableRow("du", listOf("du", "dich", "dir", "deiner"), "du", "du (you)", "du (أنت)"),
                        GrammarTableRow("er", listOf("er", "ihn", "ihm", "seiner"), "er", "er (he)", "er (هو)"),
                        GrammarTableRow("sie", listOf("sie", "sie", "ihr", "ihrer"), "sie", "sie (she)", "sie (هي)"),
                        GrammarTableRow("es", listOf("es", "es", "ihm", "seiner"), "es", "es (it)", "es (محايد)"),
                        GrammarTableRow("wir", listOf("wir", "uns", "uns", "unser"), "wir", "wir (we)", "wir (نحن)"),
                        GrammarTableRow("ihr", listOf("ihr", "euch", "euch", "euer"), "ihr", "ihr (you pl.)", "ihr (أنتم)"),
                        GrammarTableRow("sie (Pl.)", listOf("sie", "sie", "ihnen", "ihrer"), "sie", "sie (they)", "sie (هم)"),
                        GrammarTableRow("Sie", listOf("Sie", "Sie", "Ihnen", "Ihrer"), "Sie", "Sie (formal)", "Sie (حضرتك)")
                    ),
                    notesDe = "Bei wir (uns) und ihr (euch) sind Akkusativ & Dativ gleich.",
                    notesEn = "For wir (uns) and ihr (euch), Accusative & Dative are identical.",
                    notesAr = "في wir (uns) و ihr (euch) تتطابق صيغتا النصب والجر.",
                    examples = listOf(
                        GrammarExample("Er hilft mir (Dat.) und sieht mich (Akk.).", "He helps me and sees me.", "هو يساعدني (جر) ويراني (نصب)."),
                        GrammarExample("Wir danken Ihnen.", "We thank you.", "نشكر حضرتكم.")
                    )
                ),
                GrammarReferenceTable(
                    id = "reflexivpronomen",
                    titleDe = "Reflexivpronomen",
                    titleEn = "Reflexive Pronouns",
                    titleAr = "ضمائر الانعكاس",
                    subtitleDe = "Akkusativ & Dativ",
                    subtitleEn = "Accusative & Dative",
                    subtitleAr = "النصب والجر",
                    headers = listOf("Person", "Akkusativ", "Dativ"),
                    headersEn = listOf("Person", "Accusative", "Dative"),
                    headersAr = listOf("الضمير", "Akkusativ", "Dativ"),
                    rows = listOf(
                        GrammarTableRow("ich", listOf("mich", "mir"), "ich"),
                        GrammarTableRow("du", listOf("dich", "dir"), "du"),
                        GrammarTableRow("er / sie / es", listOf("sich", "sich"), "sich"),
                        GrammarTableRow("wir", listOf("uns", "uns"), "uns"),
                        GrammarTableRow("ihr", listOf("euch", "euch"), "euch"),
                        GrammarTableRow("sie / Sie", listOf("sich", "sich"), "sich")
                    ),
                    notesDe = "Unterschied nur bei ich (mich/mir) & du (dich/dir); 3. Person immer 'sich'.",
                    notesEn = "Differs only for ich (mich/mir) & du (dich/dir); 3rd person is always 'sich'.",
                    notesAr = "تختلف فقط مع ich (mich/mir) و du (dich/dir)؛ والغائب دائماً sich.",
                    examples = listOf(
                        GrammarExample("Ich wasche mich. (Akkusativ)", "I wash myself.", "أنا أغتسل."),
                        GrammarExample("Ich putze mir die Zähne. (Dativ + Objekt)", "I brush my teeth.", "أنظف أسناني.")
                    )
                ),
                GrammarReferenceTable(
                    id = "demonstrativpronomen",
                    titleDe = "Demonstrativpronomen",
                    titleEn = "Demonstrative Pronouns",
                    titleAr = "الضمائر الإشارية",
                    subtitleDe = "dieser, diese, dieses",
                    subtitleEn = "this / these",
                    subtitleAr = "هذا، هذه، هؤلاء",
                    headers = listOf("Kasus", "Maskulin", "Feminin", "Neutrum", "Plural"),
                    headersEn = listOf("Case", "Masculine", "Feminine", "Neuter", "Plural"),
                    headersAr = listOf("الحالة", "مذكر", "مؤنث", "محايد", "جمع"),
                    rows = listOf(
                        GrammarTableRow("Nominativ", listOf("dieser", "diese", "dieses", "diese"), "Nominativ", "Nominative", "الرفع"),
                        GrammarTableRow("Akkusativ", listOf("diesen", "diese", "dieses", "diese"), "Akkusativ", "Accusative", "النصب"),
                        GrammarTableRow("Dativ", listOf("diesem", "dieser", "diesem", "diesen"), "Dativ", "Dative", "الجر"),
                        GrammarTableRow("Genitiv", listOf("dieses", "dieser", "dieses", "dieser"), "Genitiv", "Genitive", "الإضافة")
                    ),
                    notesDe = "Endungen genau wie beim bestimmten Artikel (der, die, das).",
                    notesEn = "Endings match the definite article (der, die, das).",
                    notesAr = "نفس نهايات أداة التعريف (der, die, das)."
                ),
                GrammarReferenceTable(
                    id = "interrogativpronomen",
                    titleDe = "Fragepronomen",
                    titleEn = "Interrogative Pronouns",
                    titleAr = "ضمائر الاستفهام",
                    subtitleDe = "wer, was, welcher",
                    subtitleEn = "who, what, which",
                    subtitleAr = "مَن، ماذا، أيّ",
                    headers = listOf("Kasus", "Person (Wer?)", "Sache (Was?)", "Auswahl (Welch-?)"),
                    headersEn = listOf("Case", "Person (Who?)", "Thing (What?)", "Which (Welch-?)"),
                    headersAr = listOf("الحالة", "للعاقل (Wer?)", "لغير العاقل (Was?)", "للاختيار (Welch-?)"),
                    rows = listOf(
                        GrammarTableRow("Nominativ", listOf("wer", "was", "welcher / -e / -es"), "wer", "Nominative", "الرفع"),
                        GrammarTableRow("Akkusativ", listOf("wen", "was", "welchen / -e / -es"), "wen", "Accusative", "النصب"),
                        GrammarTableRow("Dativ", listOf("wem", "–", "welchem / -er / -em"), "wem", "Dative", "الجر"),
                        GrammarTableRow("Genitiv", listOf("wessen", "wessen", "welches / -er / -es"), "wessen", "Genitive", "الإضافة")
                    ),
                    notesDe = "wer = Personen (4 Kasus)  |  was = Sachen (Nom. & Akk. gleich).",
                    notesEn = "wer = people (4 cases)  |  was = things (Nom. & Acc. identical).",
                    notesAr = "wer = للعاقل (4 حالات)  |  was = للأشياء (ثابت في الرفع والنصب)."
                ),
                GrammarReferenceTable(
                    id = "indefinitpronomen",
                    titleDe = "Indefinitpronomen",
                    titleEn = "Indefinite Pronouns",
                    titleAr = "ضمائر النكرة",
                    subtitleDe = "man, jemand, niemand, etwas, nichts",
                    subtitleEn = "one, someone, no one, something, nothing",
                    subtitleAr = "المرء، أحدهم، لا أحد، شيء، لا شيء",
                    headers = listOf("Typ", "Pronomen", "Beispiel"),
                    headersEn = listOf("Type", "Pronoun", "Example"),
                    headersAr = listOf("النوع", "الضمير", "مثال"),
                    rows = listOf(
                        GrammarTableRow("Allgemein", listOf("man", "Hier darf man nicht rauchen."), "man", "General", "عام"),
                        GrammarTableRow("Person", listOf("jemand / niemand", "Ist jemand da?"), "jemand", "Person", "شخص"),
                        GrammarTableRow("Sache", listOf("etwas / nichts", "Ich höre nichts."), "etwas", "Thing", "شيء"),
                        GrammarTableRow("Menge", listOf("alle / viele / wenige", "Alle sind bereit."), "alle", "Quantity", "كمية"),
                        GrammarTableRow("Nomen-Ersatz", listOf("einer / keiner / eins", "Ich habe keins."), "einer", "Substitute", "بديل اسم")
                    ),
                    notesDe = "'man' steht im Nominativ (3. Sg.); Akkusativ = einen, Dativ = einem.",
                    notesEn = "'man' is Nominative (3rd Sg.); Accusative = einen, Dative = einem.",
                    notesAr = "'man' للرفع المفرد؛ وفي النصب einen وفي الجر einem."
                )
            )
        ),
        GrammarTableCategory(
            id = "possessiv",
            titleDe = "Possessiv",
            titleEn = "Possessive",
            titleAr = "الملكية",
            iconName = "Star",
            descriptionDe = "Possessivartikel für alle Personen in allen 4 Kasus.",
            descriptionEn = "Possessive determiners for all persons across all 4 cases.",
            descriptionAr = "أدوات الملكية لجميع الضمائر في الحالات الأربع.",
            tables = listOf(
                GrammarReferenceTable(
                    id = "possessiv_uebersicht",
                    titleDe = "Übersicht (Nominativ)",
                    titleEn = "Overview (Nominative)",
                    titleAr = "نظرة عامة (حالة الرفع)",
                    subtitleDe = "Grundformen nach Besitzer",
                    subtitleEn = "Base forms by owner",
                    subtitleAr = "الصيغ الأساسية حسب المالك",
                    headers = listOf("Person", "Maskulin", "Feminin", "Neutrum", "Plural"),
                    headersEn = listOf("Person", "Masculine", "Feminine", "Neuter", "Plural"),
                    headersAr = listOf("الضمير", "مذكر", "مؤنث", "محايد", "جمع"),
                    rows = listOf(
                        GrammarTableRow("ich", listOf("mein", "meine", "mein", "meine"), "mein", "ich (my)", "ich (لي)"),
                        GrammarTableRow("du", listOf("dein", "deine", "dein", "deine"), "dein", "du (your)", "du (لك)"),
                        GrammarTableRow("er / es", listOf("sein", "seine", "sein", "seine"), "sein", "er/es (his/its)", "er/es (له)"),
                        GrammarTableRow("sie", listOf("ihr", "ihre", "ihr", "ihre"), "ihr", "sie (her)", "sie (لها)"),
                        GrammarTableRow("wir", listOf("unser", "unsere", "unser", "unsere"), "unser", "wir (our)", "wir (لنا)"),
                        GrammarTableRow("ihr", listOf("euer", "eure", "euer", "eure"), "euer", "ihr (your pl.)", "ihr (لكم)"),
                        GrammarTableRow("sie (Pl.)", listOf("ihr", "ihre", "ihr", "ihre"), "ihr", "sie (their)", "sie (لهم)"),
                        GrammarTableRow("Sie", listOf("Ihr", "Ihre", "Ihr", "Ihre"), "Ihr", "Sie (Your)", "Sie (لحضرتك)")
                    ),
                    notesDe = "Maskulin & Neutrum haben im Nominativ keine Endung.",
                    notesEn = "Masculine & Neuter take no ending in Nominative.",
                    notesAr = "المذكر والمحايد بدون لاحقة في حالة الرفع."
                ),
                GrammarReferenceTable(
                    id = "possessiv_endungen_schema",
                    titleDe = "Endungsschema",
                    titleEn = "Endings Pattern",
                    titleAr = "مخطط النهايات",
                    subtitleDe = "Wie unbestimmter Artikel (ein / kein)",
                    subtitleEn = "Matches indefinite article (ein / kein)",
                    subtitleAr = "نفس نهايات أداة النكرة (ein / kein)",
                    headers = listOf("Kasus", "Maskulin", "Feminin", "Neutrum", "Plural"),
                    headersEn = listOf("Case", "Masculine", "Feminine", "Neuter", "Plural"),
                    headersAr = listOf("الحالة", "مذكر", "مؤنث", "محايد", "جمع"),
                    rows = listOf(
                        GrammarTableRow("Nominativ", listOf("–", "-e", "–", "-e"), "Nominativ", "Nominative", "الرفع"),
                        GrammarTableRow("Akkusativ", listOf("-en", "-e", "–", "-e"), "Akkusativ", "Accusative", "النصب"),
                        GrammarTableRow("Dativ", listOf("-em", "-er", "-em", "-en (+n)"), "Dativ", "Dative", "الجر"),
                        GrammarTableRow("Genitiv", listOf("-es (+s)", "-er", "-es (+s)", "-er"), "Genitiv", "Genitive", "الإضافة")
                    ),
                    notesDe = "Im Akkusativ ändert sich nur Maskulin (-en). Dativ Plural + '-n' am Nomen.",
                    notesEn = "In Accusative only Masculine changes (-en). Dative Plural adds '-n' to noun.",
                    notesAr = "في النصب يتغير المذكر فقط (-en). وفي جر الجمع يضاف n للاسم."
                ),
                GrammarReferenceTable(
                    id = "possessiv_mein",
                    titleDe = "mein (ich)",
                    titleEn = "mein (my)",
                    titleAr = "mein (ملكي - ich)",
                    headers = listOf("Kasus", "Maskulin", "Feminin", "Neutrum", "Plural"),
                    headersEn = listOf("Case", "Masculine", "Feminine", "Neuter", "Plural"),
                    headersAr = listOf("الحالة", "مذكر", "مؤنث", "محايد", "جمع"),
                    rows = listOf(
                        GrammarTableRow("Nominativ", listOf("mein", "meine", "mein", "meine"), "mein", "Nominative", "الرفع"),
                        GrammarTableRow("Akkusativ", listOf("meinen", "meine", "mein", "meine"), "meinen", "Accusative", "النصب"),
                        GrammarTableRow("Dativ", listOf("meinem", "meiner", "meinem", "meinen"), "meinem", "Dative", "الجر"),
                        GrammarTableRow("Genitiv", listOf("meines", "meiner", "meines", "meiner"), "meines", "Genitive", "الإضافة")
                    )
                ),
                GrammarReferenceTable(
                    id = "possessiv_dein",
                    titleDe = "dein (du)",
                    titleEn = "dein (your)",
                    titleAr = "dein (ملكك - du)",
                    headers = listOf("Kasus", "Maskulin", "Feminin", "Neutrum", "Plural"),
                    headersEn = listOf("Case", "Masculine", "Feminine", "Neuter", "Plural"),
                    headersAr = listOf("الحالة", "مذكر", "مؤنث", "محايد", "جمع"),
                    rows = listOf(
                        GrammarTableRow("Nominativ", listOf("dein", "deine", "dein", "deine"), "dein", "Nominative", "الرفع"),
                        GrammarTableRow("Akkusativ", listOf("deinen", "deine", "dein", "deine"), "deinen", "Accusative", "النصب"),
                        GrammarTableRow("Dativ", listOf("deinem", "deiner", "deinem", "deinen"), "deinem", "Dative", "الجر"),
                        GrammarTableRow("Genitiv", listOf("deines", "deiner", "deines", "deiner"), "deines", "Genitive", "الإضافة")
                    )
                ),
                GrammarReferenceTable(
                    id = "possessiv_sein",
                    titleDe = "sein (er / es)",
                    titleEn = "sein (his / its)",
                    titleAr = "sein (ملكه - er / es)",
                    headers = listOf("Kasus", "Maskulin", "Feminin", "Neutrum", "Plural"),
                    headersEn = listOf("Case", "Masculine", "Feminine", "Neuter", "Plural"),
                    headersAr = listOf("الحالة", "مذكر", "مؤنث", "محايد", "جمع"),
                    rows = listOf(
                        GrammarTableRow("Nominativ", listOf("sein", "seine", "sein", "seine"), "sein", "Nominative", "الرفع"),
                        GrammarTableRow("Akkusativ", listOf("seinen", "seine", "sein", "seine"), "seinen", "Accusative", "النصب"),
                        GrammarTableRow("Dativ", listOf("seinem", "seiner", "seinem", "seinen"), "seinem", "Dative", "الجر"),
                        GrammarTableRow("Genitiv", listOf("seines", "seiner", "seines", "seiner"), "seines", "Genitive", "الإضافة")
                    )
                ),
                GrammarReferenceTable(
                    id = "possessiv_ihr_sg",
                    titleDe = "ihr (sie – Sg.)",
                    titleEn = "ihr (her)",
                    titleAr = "ihr (ملكها - sie)",
                    headers = listOf("Kasus", "Maskulin", "Feminin", "Neutrum", "Plural"),
                    headersEn = listOf("Case", "Masculine", "Feminine", "Neuter", "Plural"),
                    headersAr = listOf("الحالة", "مذكر", "مؤنث", "محايد", "جمع"),
                    rows = listOf(
                        GrammarTableRow("Nominativ", listOf("ihr", "ihre", "ihr", "ihre"), "ihr", "Nominative", "الرفع"),
                        GrammarTableRow("Akkusativ", listOf("ihren", "ihre", "ihr", "ihre"), "ihren", "Accusative", "النصب"),
                        GrammarTableRow("Dativ", listOf("ihrem", "ihrer", "ihrem", "ihren"), "ihrem", "Dative", "الجر"),
                        GrammarTableRow("Genitiv", listOf("ihres", "ihrer", "ihres", "ihrer"), "ihres", "Genitive", "الإضافة")
                    )
                ),
                GrammarReferenceTable(
                    id = "possessiv_unser",
                    titleDe = "unser (wir)",
                    titleEn = "unser (our)",
                    titleAr = "unser (ملكنا - wir)",
                    headers = listOf("Kasus", "Maskulin", "Feminin", "Neutrum", "Plural"),
                    headersEn = listOf("Case", "Masculine", "Feminine", "Neuter", "Plural"),
                    headersAr = listOf("الحالة", "مذكر", "مؤنث", "محايد", "جمع"),
                    rows = listOf(
                        GrammarTableRow("Nominativ", listOf("unser", "unsere", "unser", "unsere"), "unser", "Nominative", "الرفع"),
                        GrammarTableRow("Akkusativ", listOf("unseren", "unsere", "unser", "unsere"), "unseren", "Accusative", "النصب"),
                        GrammarTableRow("Dativ", listOf("unserem", "unserer", "unserem", "unseren"), "unserem", "Dative", "الجر"),
                        GrammarTableRow("Genitiv", listOf("unseres", "unserer", "unseres", "unserer"), "unseres", "Genitive", "الإضافة")
                    )
                ),
                GrammarReferenceTable(
                    id = "possessiv_euer",
                    titleDe = "euer (ihr)",
                    titleEn = "euer (your pl.)",
                    titleAr = "euer (ملككم - ihr)",
                    headers = listOf("Kasus", "Maskulin", "Feminin", "Neutrum", "Plural"),
                    headersEn = listOf("Case", "Masculine", "Feminine", "Neuter", "Plural"),
                    headersAr = listOf("الحالة", "مذكر", "مؤنث", "محايد", "جمع"),
                    rows = listOf(
                        GrammarTableRow("Nominativ", listOf("euer", "eure", "euer", "eure"), "euer", "Nominative", "الرفع"),
                        GrammarTableRow("Akkusativ", listOf("euren", "eure", "euer", "eure"), "euren", "Accusative", "النصب"),
                        GrammarTableRow("Dativ", listOf("eurem", "eurer", "eurem", "euren"), "eurem", "Dative", "الجر"),
                        GrammarTableRow("Genitiv", listOf("eures", "eurer", "eures", "eurer"), "eures", "Genitive", "الإضافة")
                    ),
                    notesDe = "Mit Endung fällt das mittlere 'e' weg: eure, euren, eurem, eurer, eures.",
                    notesEn = "When adding an ending, the middle 'e' drops: eure, euren, eurem.",
                    notesAr = "تحذف الـ e الوسطى عند إضافة أي لاحقة: eure, euren, eurem."
                ),
                GrammarReferenceTable(
                    id = "possessiv_ihr_pl_sie",
                    titleDe = "ihr (Pl.) / Ihr (Höflich)",
                    titleEn = "ihr (their) / Ihr (Your)",
                    titleAr = "ihr (ملكهم) / Ihr (لحضرتك)",
                    headers = listOf("Kasus", "Maskulin", "Feminin", "Neutrum", "Plural"),
                    headersEn = listOf("Case", "Masculine", "Feminine", "Neuter", "Plural"),
                    headersAr = listOf("الحالة", "مذكر", "مؤنث", "محايد", "جمع"),
                    rows = listOf(
                        GrammarTableRow("Nominativ", listOf("ihr / Ihr", "ihre / Ihre", "ihr / Ihr", "ihre / Ihre"), "ihr", "Nominative", "الرفع"),
                        GrammarTableRow("Akkusativ", listOf("ihren / Ihren", "ihre / Ihre", "ihr / Ihr", "ihre / Ihre"), "ihren", "Accusative", "النصب"),
                        GrammarTableRow("Dativ", listOf("ihrem / Ihrem", "ihrer / Ihrer", "ihrem / Ihrem", "ihren / Ihren"), "ihrem", "Dative", "الجر"),
                        GrammarTableRow("Genitiv", listOf("ihres / Ihres", "ihrer / Ihrer", "ihres / Ihres", "ihrer / Ihrer"), "ihres", "Genitive", "الإضافة")
                    ),
                    notesDe = "Höflichkeitsform 'Ihr' wird immer großgeschrieben.",
                    notesEn = "Formal 'Ihr' is always capitalized.",
                    notesAr = "صيغة الاحترام Ihr تُكتب دائماً بحرف كبير."
                )
            )
        ),
        GrammarTableCategory(
            id = "praepositionen",
            titleDe = "Präpositionen",
            titleEn = "Prepositions",
            titleAr = "حروف الجر",
            iconName = "Translate",
            descriptionDe = "Feste Präpositionen & Wechselpräpositionen.",
            descriptionEn = "Fixed & two-way prepositions.",
            descriptionAr = "حروف الجر الثابتة والمشتركة.",
            tables = listOf(
                GrammarReferenceTable(
                    id = "praep_kasus",
                    titleDe = "Präpositionen nach Kasus",
                    titleEn = "Prepositions by Case",
                    titleAr = "حروف الجر حسب الحالة",
                    headers = listOf("Kasus", "Präpositionen", "Merkhilfe", "Beispiel"),
                    headersEn = listOf("Case", "Prepositions", "Mnemonic", "Example"),
                    headersAr = listOf("الحالة", "حروف الجر", "الاختصار", "مثال"),
                    rows = listOf(
                        GrammarTableRow(
                            label = "Akkusativ",
                            labelEn = "Accusative",
                            labelAr = "النصب",
                            cells = listOf("bis, durch, für, gegen, ohne, um, entlang", "DOGFU", "Ich gehe durch den Park."),
                            speakText = "Akkusativ"
                        ),
                        GrammarTableRow(
                            label = "Dativ",
                            labelEn = "Dative",
                            labelAr = "الجر",
                            cells = listOf("aus, bei, mit, nach, seit, von, zu, gegenüber, außer", "ABMNSVZ", "Ich fahre mit dem Bus."),
                            speakText = "Dativ"
                        ),
                        GrammarTableRow(
                            label = "Genitiv",
                            labelEn = "Genitive",
                            labelAr = "الإضافة",
                            cells = listOf("während, wegen, trotz, (an)statt, außerhalb, innerhalb", "während / wegen / trotz", "Trotz des Regens."),
                            speakText = "Genitiv"
                        )
                    )
                ),
                GrammarReferenceTable(
                    id = "wechselpraepositionen",
                    titleDe = "Wechselpräpositionen",
                    titleEn = "Two-Way Prepositions",
                    titleAr = "حروف الجر المشتركة",
                    subtitleDe = "Wohin? → Akkusativ  |  Wo? → Dativ",
                    subtitleEn = "Where to? → Accusative  |  Where? → Dative",
                    subtitleAr = "Wohin? (حركة) → Akkusativ  |  Wo? (سكون) → Dativ",
                    headers = listOf("Präposition", "Akkusativ (Wohin?)", "Dativ (Wo?)"),
                    headersEn = listOf("Preposition", "Accusative (Where to?)", "Dative (Where?)"),
                    headersAr = listOf("حرف الجر", "Akkusativ (إلى أين؟)", "Dativ (أين؟)"),
                    rows = listOf(
                        GrammarTableRow("an", listOf("Ich hänge das Bild an die Wand.", "Das Bild hängt an der Wand."), "an"),
                        GrammarTableRow("auf", listOf("Ich lege das Buch auf den Tisch.", "Das Buch liegt auf dem Tisch."), "auf"),
                        GrammarTableRow("hinter", listOf("Er geht hinter das Haus.", "Er steht hinter dem Haus."), "hinter"),
                        GrammarTableRow("in", listOf("Wir gehen in die Stadt.", "Wir sind in der Stadt."), "in"),
                        GrammarTableRow("neben", listOf("Stell den Stuhl neben den Tisch.", "Der Stuhl steht neben dem Tisch."), "neben"),
                        GrammarTableRow("über", listOf("Er hängt die Lampe über den Tisch.", "Die Lampe hängt über dem Tisch."), "über"),
                        GrammarTableRow("unter", listOf("Die Katze läuft unter das Bett.", "Die Katze schläft unter dem Bett."), "unter"),
                        GrammarTableRow("vor", listOf("Fahr das Auto vor die Garage.", "Das Auto steht vor der Garage."), "vor"),
                        GrammarTableRow("zwischen", listOf("Setz dich zwischen die Freunde.", "Ich sitze zwischen den Freunden."), "zwischen")
                    )
                )
            )
        )
    )
}
