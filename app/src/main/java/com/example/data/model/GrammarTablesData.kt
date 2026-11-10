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
        else -> ""
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
        else -> ""
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
 * Central repository of German Grammar Tables.
 */
object GrammarTablesData {

    val PRONUNCIATION_GUIDE = listOf(
        PronunciationRule(
            soundLetter = "Ä / ä",
            ipa = "[ɛː] / [ɛ]",
            explanationDe = "Offenes oder langes 'e' (wie in 'Käse' oder 'Männer')",
            explanationEn = "Open or long 'e' sound (like 'e' in 'bed' or 'fair')",
            explanationAr = "صوت 'e' مفتوح أو ممدود (مثل صوت الألف الممالة الخفيفة)",
            exampleWords = listOf("Käse", "Männer", "Äpfel", "spät")
        ),
        PronunciationRule(
            soundLetter = "Ö / ö",
            ipa = "[øː] / [œ]",
            explanationDe = "Lippen runden wie für 'o', aber 'e' sprechen (wie in 'schön', 'öffnen')",
            explanationEn = "Round lips as for 'o', but pronounce 'e' (like 'eu' in French 'peu')",
            explanationAr = "ضم الشفتين كأنك تنطق 'o' مع نطق حرف 'e'",
            exampleWords = listOf("schön", "König", "öffnen", "Löffel")
        ),
        PronunciationRule(
            soundLetter = "Ü / ü",
            ipa = "[yː] / [ʏ]",
            explanationDe = "Lippen runden wie für 'u', aber 'i' sprechen (wie in 'über', 'Mütter')",
            explanationEn = "Round lips as for 'oo', but pronounce 'ee' (like 'u' in French 'tu')",
            explanationAr = "ضم الشفتين كأنك تنطق 'u' مع نطق حرف 'i'",
            exampleWords = listOf("über", "grün", "fünf", "Mütter")
        ),
        PronunciationRule(
            soundLetter = "EI / AI",
            ipa = "[aɪ̯]",
            explanationDe = "Klingt wie 'ai' in 'Mai' oder 'ei' in 'Ei'",
            explanationEn = "Sounds like 'eye' or 'i' in 'mine'",
            explanationAr = "ينطق كحرف الياء المفتوح ما قبلها (مثل 'آي')",
            exampleWords = listOf("Eis", "Reise", "Mai", "arbeiten")
        ),
        PronunciationRule(
            soundLetter = "EU / ÄU",
            ipa = "[ɔɪ̯]",
            explanationDe = "Klingt wie 'oi' in 'Euro' oder 'Häuser'",
            explanationEn = "Sounds like 'oy' in 'boy' or 'coin'",
            explanationAr = "ينطق مثل 'أوي' (كما في كلمة Euro)",
            exampleWords = listOf("Euro", "Freund", "Häuser", "heute")
        ),
        PronunciationRule(
            soundLetter = "AU",
            ipa = "[aʊ̯]",
            explanationDe = "Klingt wie 'au' in 'Haus' oder 'Baum'",
            explanationEn = "Sounds like 'ow' in 'how' or 'cow'",
            explanationAr = "ينطق مثل 'آو' (كما في كلمة Haus)",
            exampleWords = listOf("Haus", "Baum", "Auto", "Frau")
        ),
        PronunciationRule(
            soundLetter = "CH (ich-Laut)",
            ipa = "[ç]",
            explanationDe = "Nach e, i, ä, ö, ü, ei, eu (weich gehaucht: 'ich', 'Milch')",
            explanationEn = "Soft 'ch' after e, i, ä, ö, ü, ei, eu (hissing sound: 'ich', 'Milch')",
            explanationAr = "صوت الشين المخففة بعد الحروف الكسرية (e, i, ä, ö, ü)",
            exampleWords = listOf("ich", "Milch", "Recht", "Bücher")
        ),
        PronunciationRule(
            soundLetter = "CH (ach-Laut)",
            ipa = "[x]",
            explanationDe = "Nach a, o, u, au (tief in der Kehle: 'Buch', 'Nacht')",
            explanationEn = "Guttural 'ch' after a, o, u, au (like Scottish 'loch': 'Buch')",
            explanationAr = "ينطق مثل حرف الخاء بعد الحروف الحلقية (a, o, u, au)",
            exampleWords = listOf("Nacht", "Buch", "Kochen", "Sprache")
        ),
        PronunciationRule(
            soundLetter = "SCH",
            ipa = "[ʃ]",
            explanationDe = "Zischlaut wie in 'Schule', 'schön'",
            explanationEn = "Sounds like 'sh' in 'shoe' or 'share'",
            explanationAr = "ينطق تماماً مثل حرف الشين في العربية",
            exampleWords = listOf("Schule", "schön", "Tasche", "Schreiben")
        ),
        PronunciationRule(
            soundLetter = "ST / SP",
            ipa = "[ʃt] / [ʃp]",
            explanationDe = "Am Wortanfang wie 'scht' und 'schp' gesprochen",
            explanationEn = "At word start, pronounced as 'sht' and 'shp' ('Stadt', 'Sport')",
            explanationAr = "في بداية الكلمة ينطقان مثل 'شت' و 'شب'",
            exampleWords = listOf("Stadt", "sprechen", "Sport", "stehen")
        ),
        PronunciationRule(
            soundLetter = "IG (am Ende)",
            ipa = "[ɪç]",
            explanationDe = "Am Wortende wie '-ich' gesprochen (z. B. 'richtig')",
            explanationEn = "At end of word, pronounced as soft '-ich' (e.g. 'richtig')",
            explanationAr = "في نهاية الكلمة ينطق مثل '-يش' خفيفة",
            exampleWords = listOf("richtig", "wichtig", "fertig", "König")
        ),
        PronunciationRule(
            soundLetter = "V",
            ipa = "[f] / [v]",
            explanationDe = "Meist wie deutsches 'F' (Vater, Vogel), bei Fremdwörtern wie 'W' (Vase)",
            explanationEn = "Usually pronounced as 'F' (Vater, Vogel), in loanwords as 'V' (Vase)",
            explanationAr = "غالباً ينطق كالفاء (Vater)، وفي الكلمات الدخيلة كـ 'V'",
            exampleWords = listOf("Vater", "Vogel", "viel", "Vase")
        ),
        PronunciationRule(
            soundLetter = "W",
            ipa = "[v]",
            explanationDe = "Immer wie weiches englisches 'V' (Wasser, Welt)",
            explanationEn = "Always pronounced as English 'V' (Wasser, Welt)",
            explanationAr = "ينطق دائماً مثل حرف 'V' بالإنجليزية",
            exampleWords = listOf("Wasser", "Welt", "wohnen", "Wort")
        ),
        PronunciationRule(
            soundLetter = "Z / TZ",
            ipa = "[t͡s]",
            explanationDe = "Scharfes 'ts' wie in 'Zeit', 'Zimmer', 'Platz'",
            explanationEn = "Sharp 'ts' sound as in 'cats' or 'pizza' ('Zeit', 'Platz')",
            explanationAr = "ينطق دائماً كصوت 'تس' حاد ومشدد",
            exampleWords = listOf("Zeit", "Zimmer", "Katze", "Platz")
        ),
        PronunciationRule(
            soundLetter = "S (am Wortanfang)",
            ipa = "[z]",
            explanationDe = "Vor Vokal weich und stimmhaft gesprochen (Sonne, See)",
            explanationEn = "Voiced 'z' sound before vowels (like 'z' in 'zebra': Sonne)",
            explanationAr = "ينطق كصوت الزاي (Z) إذا تبعه حرف علة في بداية الكلمة",
            exampleWords = listOf("Sonne", "See", "Suppe", "sagen")
        )
    )

    /**
     * Dedicated fixed preposition groups for stacked cards layout.
     */
    val FIXED_PREPOSITION_GROUPS: List<PrepositionGroup> = listOf(
        PrepositionGroup(
            kasusId = "akkusativ",
            kasusTitleDe = "Akkusativ",
            kasusTitleEn = "Akkusativ (Direct Object)",
            kasusTitleAr = "حالة النصب (Akkusativ)",
            questionDe = "Frage: Wen? / Was?",
            questionEn = "Frage: Wen? / Was? (Whom? / What?)",
            questionAr = "السؤال: مَن؟ / ماذا؟ (Wen? / Was?)",
            prepositions = listOf(
                PrepositionItem("bis", "until / to", "حتى / إلى", "Der Zug fährt bis Hamburg.", "The train goes until Hamburg.", "القطار يسير حتى هامبورغ."),
                PrepositionItem("durch", "through / by", "عبر / من خلال", "Ich gehe durch den Park.", "I walk through the park.", "أمشي عبر الحديقة."),
                PrepositionItem("für", "for", "لـ / من أجل", "Das Geschenk ist für dich.", "The gift is for you.", "الهدية من أجلك."),
                PrepositionItem("gegen", "against / around", "ضد / نحو", "Das Auto fuhr gegen die Wand.", "The car drove against the wall.", "اصطدمت السيارة بالجدار."),
                PrepositionItem("ohne", "without", "بدون / من غير", "Ohne meinen Kaffee kann ich nicht arbeiten.", "Without my coffee I cannot work.", "بدون قهوتي لا أستطيع العمل."),
                PrepositionItem("um", "around / at (time)", "حول / في تمام", "Wir sitzen um den Tisch.", "We sit around the table.", "نجلس حول الطاولة."),
                PrepositionItem("entlang", "along", "على طول / بمحاذاة", "Gehen Sie den Fluss entlang.", "Walk along the river.", "امشِ بمحاذاة النهر.")
            ),
            mnemonicDe = "DOGFU (durch, ohne, gegen, für, um, entlang)",
            mnemonicEn = "DOGFU (durch, ohne, gegen, für, um, entlang)",
            mnemonicAr = "اختصار الحفظ: DOGFU (durch, ohne, gegen, für, um, entlang)",
            ruleNoteDe = "Nach diesen Präpositionen steht das nachfolgende Nomen/Pronomen IMMER im Akkusativ.",
            ruleNoteEn = "Nouns or pronouns following these prepositions are ALWAYS in the Accusative case.",
            ruleNoteAr = "الاسم أو الضمير الواقع بعد هذه الحروف يأتي دائماً في حالة النصب (Akkusativ).",
            examples = listOf(
                GrammarExample("Ich gehe durch den Park.", "I walk through the park.", "أمشي عبر الحديقة.", "durch + Akkusativ: den Park"),
                GrammarExample("Ohne meinen Kaffee kann ich nicht arbeiten.", "Without my coffee I cannot work.", "بدون قهوتي لا أستطيع العمل.", "ohne + Akkusativ: meinen Kaffee"),
                GrammarExample("Das Geschenk ist für dich.", "The gift is for you.", "الهدية من أجلك.", "für + Akkusativ: dich")
            ),
            speakText = "Akkusativ: bis, durch, für, gegen, ohne, um, entlang"
        ),
        PrepositionGroup(
            kasusId = "dativ",
            kasusTitleDe = "Dativ",
            kasusTitleEn = "Dativ (Indirect Object)",
            kasusTitleAr = "حالة الجر (Dativ)",
            questionDe = "Frage: Wem? / Wo?",
            questionEn = "Frage: Wem? / Wo? (To whom? / Where?)",
            questionAr = "السؤال: لمَن؟ / أين؟ (Wem? / Wo?)",
            prepositions = listOf(
                PrepositionItem("aus", "out of / from", "من (مكان/مادة)", "Er kommt aus der Schweiz.", "He comes from Switzerland.", "هو قادم من سويسرا."),
                PrepositionItem("bei", "at / with / near", "عند / لدى", "Ich wohne bei meiner Familie.", "I live with my family.", "أعيش عند عائلتي."),
                PrepositionItem("mit", "with / by", "مع / بواسطة", "Ich fahre mit dem Bus.", "I go by bus.", "أسافر بالحافلة."),
                PrepositionItem("nach", "after / to", "بعد / إلى", "Nach dem Unterricht gehe ich nach Hause.", "After class I go home.", "بعد الدرس أذهب إلى المنزل."),
                PrepositionItem("seit", "since / for", "منذ", "Seit einem Monat lerne ich Deutsch.", "For a month I have been learning German.", "منذ شهر وأنا أتعلم الألمانية."),
                PrepositionItem("von", "from / of", "من / تابع لـ", "Das ist ein Geschenk von meiner Mutter.", "That is a gift from my mother.", "هذه هدية من أمي."),
                PrepositionItem("zu", "to / at", "إلى / عند", "Wir gehen zu Fuß zur Schule.", "We walk to school on foot.", "نذهب مشياً إلى المدرسة."),
                PrepositionItem("gegenüber", "opposite / across", "مقابل / قبالة", "Die Bank liegt gegenüber dem Bahnhof.", "The bank is opposite the station.", "البنك يقع مقابل محطة القطار."),
                PrepositionItem("außer", "except / besides", "ما عدا / باستثناء", "Alle sind da außer meinem Bruder.", "Everyone is here except my brother.", "الجميع حاضرون ما عدا أخي.")
            ),
            mnemonicDe = "ABMNSVZ (aus, bei, mit, nach, seit, von, zu)",
            mnemonicEn = "ABMNSVZ (aus, bei, mit, nach, seit, von, zu)",
            mnemonicAr = "اختصار الحفظ: ABMNSVZ (aus, bei, mit, nach, seit, von, zu)",
            ruleNoteDe = "Nach diesen Präpositionen steht das nachfolgende Nomen/Pronomen IMMER im Dativ.",
            ruleNoteEn = "Nouns or pronouns following these prepositions are ALWAYS in the Dative case.",
            ruleNoteAr = "الاسم أو الضمير الواقع بعد هذه الحروف يأتي دائماً في حالة الجر (Dativ).",
            examples = listOf(
                GrammarExample("Er kommt aus der Schweiz.", "He comes from Switzerland.", "هو قادم من سويسرا.", "aus + Dativ: der Schweiz"),
                GrammarExample("Ich wohne bei meiner Familie.", "I live with my family.", "أعيش عند عائلتي.", "bei + Dativ: meiner Familie"),
                GrammarExample("Seit einem Monat lerne ich Deutsch.", "For a month I have been learning German.", "منذ شهر وأنا أتعلم الألمانية.", "seit + Dativ: einem Monat")
            ),
            speakText = "Dativ: aus, bei, mit, nach, seit, von, zu, gegenüber, außer"
        ),
        PrepositionGroup(
            kasusId = "genitiv",
            kasusTitleDe = "Genitiv",
            kasusTitleEn = "Genitiv (Possession & Reason)",
            kasusTitleAr = "حالة الإضافة (Genitiv)",
            questionDe = "Frage: Wessen?",
            questionEn = "Frage: Wessen? (Whose?)",
            questionAr = "السؤال: مَن صاحب؟ (Wessen?)",
            prepositions = listOf(
                PrepositionItem("während", "during", "أثناء / خلال", "Während der Reise habe ich viel gelesen.", "During the trip I read a lot.", "خلال الرحلة قرأتُ كثيراً."),
                PrepositionItem("wegen", "because of", "بسبب", "Wegen des Regens bleiben wir zu Hause.", "Because of the rain we stay home.", "بسبب المطر نبقى في المنزل."),
                PrepositionItem("trotz", "despite", "بالرغم من", "Trotz des Regens gehen wir spazieren.", "Despite the rain we go for a walk.", "على الرغم من المطر نذهب في نزهة."),
                PrepositionItem("anstatt", "instead of", "بدلاً من", "Statt eines Autos kauft er ein Fahrrad.", "Instead of a car he buys a bike.", "بدلاً من سيارة يشتري دراجة."),
                PrepositionItem("außerhalb", "outside of", "خارج", "Er wohnt außerhalb der Stadt.", "He lives outside the city.", "يسكن خارج المدينة."),
                PrepositionItem("innerhalb", "inside of / within", "داخل / في غضون", "Bitte antworten Sie innerhalb eines Tages.", "Please reply within a day.", "يرجى الرد خلال يوم واحد.")
            ),
            mnemonicDe = "Wegen / Trotz / Während (Die 3 wichtigsten Genitiv-Präpositionen)",
            mnemonicEn = "Wegen / Trotz / Während (The 3 primary genitive prepositions)",
            mnemonicAr = "الحروف الثلاثة الأكثر شيوعاً: Wegen / Trotz / Während",
            ruleNoteDe = "Nach diesen Präpositionen folgt das Nomen im Genitiv (des/der + -(e)s).",
            ruleNoteEn = "Nouns following these prepositions take the Genitive case (des/der with -(e)s endings).",
            ruleNoteAr = "الاسم بعد هذه الحروف يأتي في حالة الإضافة (Genitiv) مع أداة des/der.",
            examples = listOf(
                GrammarExample("Trotz des Regens gehen wir spazieren.", "Despite the rain we go for a walk.", "على الرغم من المطر نذهب في نزهة.", "trotz + Genitiv: des Regens"),
                GrammarExample("Während der Reise habe ich viel gelesen.", "During the trip I read a lot.", "خلال الرحلة قرأتُ كثيراً.", "während + Genitiv: der Reise"),
                GrammarExample("Wegen des schlechten Wetters bleiben wir hier.", "Because of bad weather we stay here.", "بسبب سوء الأحوال الجوية نبقى هنا.", "wegen + Genitiv: des schlechten Wetters")
            ),
            speakText = "Genitiv: während, wegen, trotz, statt, außerhalb, innerhalb"
        )
    )

    /**
     * Dedicated two-way prepositions for stacked cards layout.
     */
    val TWO_WAY_PREPOSITIONS: List<TwoWayPrepositionItem> = listOf(
        TwoWayPrepositionItem(
            word = "an",
            meaningEn = "at / on (vertical surface)",
            meaningAr = "على (سطح عمودي) / عند",
            akkusativGerman = "Ich hänge das Bild an die Wand.",
            akkusativEn = "I hang the picture on the wall (movement).",
            akkusativAr = "أعلق الصورة على الحائط (حركة واتجاه).",
            dativGerman = "Das Bild hängt an der Wand.",
            dativEn = "The picture is hanging on the wall (static position).",
            dativAr = "الصورة معلقة على الحائط (موضع وثبات)."
        ),
        TwoWayPrepositionItem(
            word = "auf",
            meaningEn = "on / onto (horizontal surface)",
            meaningAr = "على (سطح أفقي)",
            akkusativGerman = "Ich lege das Buch auf den Tisch.",
            akkusativEn = "I put the book onto the table (motion).",
            akkusativAr = "أضع الكتاب على الطاولة (حركة).",
            dativGerman = "Das Buch liegt auf dem Tisch.",
            dativEn = "The book is lying on the table (location).",
            dativAr = "الكتاب موضوع على الطاولة (سكون وموقع)."
        ),
        TwoWayPrepositionItem(
            word = "hinter",
            meaningEn = "behind",
            meaningAr = "خلف / وراء",
            akkusativGerman = "Er geht hinter das Haus.",
            akkusativEn = "He walks behind the house.",
            akkusativAr = "هو يمشي إلى خلف المنزل.",
            dativGerman = "Er steht hinter dem Haus.",
            dativEn = "He stands behind the house.",
            dativAr = "هو يقف خلف المنزل."
        ),
        TwoWayPrepositionItem(
            word = "in",
            meaningEn = "in / into",
            meaningAr = "في / إلى داخل",
            akkusativGerman = "Wir gehen ins Kino / in die Stadt.",
            akkusativEn = "We go into the cinema / to town.",
            akkusativAr = "نذهب إلى السينما / إلى المدينة.",
            dativGerman = "Wir sind im Kino / in der Stadt.",
            dativEn = "We are in the cinema / in town.",
            dativAr = "نحن في السينما / في المدينة."
        ),
        TwoWayPrepositionItem(
            word = "neben",
            meaningEn = "next to / beside",
            meaningAr = "بجانب / إلى جوار",
            akkusativGerman = "Stell den Stuhl neben den Tisch.",
            akkusativEn = "Put the chair next to the table.",
            akkusativAr = "ضع الكرسي بجانب الطاولة.",
            dativGerman = "Der Stuhl steht neben dem Tisch.",
            dativEn = "The chair is next to the table.",
            dativAr = "الكرسي موجود بجانب الطاولة."
        ),
        TwoWayPrepositionItem(
            word = "über",
            meaningEn = "above / over",
            meaningAr = "فوق / أعلى",
            akkusativGerman = "Er hängt die Lampe über den Tisch.",
            akkusativEn = "He hangs the lamp over the table.",
            akkusativAr = "يعلق المصباح فوق الطاولة.",
            dativGerman = "Die Lampe hängt über dem Tisch.",
            dativEn = "The lamp hangs above the table.",
            dativAr = "المصباح معلق فوق الطاولة."
        ),
        TwoWayPrepositionItem(
            word = "unter",
            meaningEn = "under / below",
            meaningAr = "تحت / أسفل",
            akkusativGerman = "Die Katze läuft unter das Bett.",
            akkusativEn = "The cat runs under the bed.",
            akkusativAr = "القطة تركض تحت السرير.",
            dativGerman = "Die Katze schläft unter dem Bett.",
            dativEn = "The cat is sleeping under the bed.",
            dativAr = "القطة تنام تحت السرير."
        ),
        TwoWayPrepositionItem(
            word = "vor",
            meaningEn = "in front of / before",
            meaningAr = "أمام",
            akkusativGerman = "Fahr das Auto vor die Garage.",
            akkusativEn = "Drive the car in front of the garage.",
            akkusativAr = "قد السيارة إلى أمام المرآب.",
            dativGerman = "Das Auto steht vor der Garage.",
            dativEn = "The car is parked in front of the garage.",
            dativAr = "السيارة متوقفة أمام المرآب."
        ),
        TwoWayPrepositionItem(
            word = "zwischen",
            meaningEn = "between",
            meaningAr = "بين",
            akkusativGerman = "Ich setze mich zwischen die Freunde.",
            akkusativEn = "I sit down between the friends.",
            akkusativAr = "أجلس بين الأصدقاء.",
            dativGerman = "Ich sitze zwischen den Freunden.",
            dativEn = "I am sitting between the friends.",
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
            descriptionDe = "Personalpronomen, Reflexivpronomen, Demonstrativ- und Indefinitpronomen.",
            descriptionEn = "Personal pronouns, reflexive, demonstrative, and indefinite pronouns.",
            descriptionAr = "الضمائر الشخصية والانعكاسية والإشارية وأدوات الاستفهام والنكرة.",
            tables = listOf(
                GrammarReferenceTable(
                    id = "personalpronomen",
                    titleDe = "Personalpronomen",
                    titleEn = "Personal Pronouns",
                    titleAr = "الضمائر الشخصية (Personalpronomen)",
                    subtitleDe = "Deklination in allen 4 Kasus",
                    subtitleEn = "Declension across all 4 cases",
                    subtitleAr = "تصريف الضمائر حسب الحالات الإعرابية الأربع",
                    headers = listOf("Person", "Nominativ", "Akkusativ", "Dativ", "Genitiv (selten)"),
                    headersEn = listOf("Person", "Nominative", "Accusative", "Dative", "Genitive (rare)"),
                    headersAr = listOf("الضمير", "الرفع (Nominativ)", "النصب (Akkusativ)", "الجر (Dativ)", "الإضافة (Genitiv)"),
                    rows = listOf(
                        GrammarTableRow("1. Sg. (ich)", listOf("ich", "mich", "mir", "meiner"), "ich", "1. Sg. (ich - I)", "1. Sg. (ich - أنا)"),
                        GrammarTableRow("2. Sg. (du)", listOf("du", "dich", "dir", "deiner"), "du", "2. Sg. (du - you)", "2. Sg. (du - أنت)"),
                        GrammarTableRow("3. Sg. m. (er)", listOf("er", "ihn", "ihm", "seiner"), "er", "3. Sg. m. (er - he)", "3. Sg. m. (er - هو)"),
                        GrammarTableRow("3. Sg. f. (sie)", listOf("sie", "sie", "ihr", "ihrer"), "sie", "3. Sg. f. (sie - she)", "3. Sg. f. (sie - هي)"),
                        GrammarTableRow("3. Sg. n. (es)", listOf("es", "es", "ihm", "seiner"), "es", "3. Sg. n. (es - it)", "3. Sg. n. (es - محايد)"),
                        GrammarTableRow("1. Pl. (wir)", listOf("wir", "uns", "uns", "unser"), "wir", "1. Pl. (wir - we)", "1. Pl. (wir - نحن)"),
                        GrammarTableRow("2. Pl. (ihr)", listOf("ihr", "euch", "euch", "euer"), "ihr", "2. Pl. (ihr - you pl.)", "2. Pl. (ihr - أنتم)"),
                        GrammarTableRow("3. Pl. (sie)", listOf("sie", "sie", "ihnen", "ihrer"), "sie", "3. Pl. (sie - they)", "3. Pl. (sie - هم/هن)"),
                        GrammarTableRow("Höflich (Sie)", listOf("Sie", "Sie", "Ihnen", "Ihrer"), "Sie", "Höflich (Sie - formal)", "Höflich (Sie - حضرتك)")
                    ),
                    notesDe = "Dativ- und Akkusativformen von 'wir' (uns) und 'ihr' (euch) sind identisch. Genitiv bei Personalpronomen ist selten (z.B. 'Er erinnerte sich meiner').",
                    notesEn = "Dative and accusative forms of 'wir' (uns) and 'ihr' (euch) are identical. Genitive personal pronouns are rare in modern German.",
                    notesAr = "صيغتا الجر والنصب للضميرين 'wir' (uns) و 'ihr' (euch) متطابقتان تماماً. ضمائر الشخصية في حالة الإضافة نادرة.",
                    examples = listOf(
                        GrammarExample("Er hilft mir (Dativ) und liebt mich (Akkusativ).", "He helps me and loves me.", "هو يساعدني (جر) ويحبني (نصب).", "mir = Dativ, mich = Akkusativ"),
                        GrammarExample("Wir danken Ihnen für Ihre freundliche Hilfe.", "We thank you for your kind help.", "نشكر حضرتكم على مساعدتكم الكريمة.", "Ihnen = Dativ Höflichkeitsform"),
                        GrammarExample("Ich habe dich gestern im Café gesehen.", "I saw you yesterday in the cafe.", "رأيتك البارحة في المقهى.", "dich = Akkusativ")
                    )
                ),
                GrammarReferenceTable(
                    id = "reflexivpronomen",
                    titleDe = "Reflexivpronomen",
                    titleEn = "Reflexive Pronouns",
                    titleAr = "ضمائر الانعكاس (Reflexivpronomen)",
                    subtitleDe = "Akkusativ und Dativ (echte & unechte Reflexivverben)",
                    subtitleEn = "Accusative and Dative forms",
                    subtitleAr = "ضمائر الانعكاس في حالتي النصب والجر",
                    headers = listOf("Person", "Akkusativ (Standard)", "Dativ (mit Zweitobjekt)"),
                    headersEn = listOf("Person", "Accusative (Standard)", "Dative (with direct object)"),
                    headersAr = listOf("الضمير", "النصب (Akkusativ)", "الجر (Dativ مع مفعول آخر)"),
                    rows = listOf(
                        GrammarTableRow("1. Sg. (ich)", listOf("mich", "mir"), "ich", "1. Sg. (ich)", "1. Sg. (ich)"),
                        GrammarTableRow("2. Sg. (du)", listOf("dich", "dir"), "du", "2. Sg. (du)", "2. Sg. (du)"),
                        GrammarTableRow("3. Sg. (er/sie/es)", listOf("sich", "sich"), "er", "3. Sg. (er/sie/es)", "3. Sg. (er/sie/es)"),
                        GrammarTableRow("1. Pl. (wir)", listOf("uns", "uns"), "wir", "1. Pl. (wir)", "1. Pl. (wir)"),
                        GrammarTableRow("2. Pl. (ihr)", listOf("euch", "euch"), "ihr", "2. Pl. (ihr)", "2. Pl. (ihr)"),
                        GrammarTableRow("3. Pl. / Sie", listOf("sich", "sich"), "Sie", "3. Pl. / Sie", "3. Pl. / Sie")
                    ),
                    notesDe = "Reflexivpronomen unterscheiden sich nur in der 1. und 2. Person Singular zwischen Akkusativ (mich/dich) und Dativ (mir/dir). In der 3. Person und Höflichkeitsform heißt es immer 'sich'.",
                    notesEn = "Reflexive pronouns only differ in 1st & 2nd person singular between Accusative (mich/dich) and Dative (mir/dir). In 3rd person and formal it is always 'sich'.",
                    notesAr = "تختلف ضمائر الانعكاس فقط مع المتكلم والمخاطب المفرد (mich/dich للنصب و mir/dir للجر)، بينما تكون دائماً 'sich' للغائب وصيغة الاحترام.",
                    examples = listOf(
                        GrammarExample("Ich wasche mich jeden Morgen.", "I wash myself every morning.", "أنا أغتسل كل صباح.", "mich = Akkusativ (kein weiteres Objekt)"),
                        GrammarExample("Ich putze mir die Zähne.", "I brush my teeth.", "أنظف أسناني.", "mir = Dativ (weil 'die Zähne' Akkusativ-Objekt ist)"),
                        GrammarExample("Er beeilt sich sehr.", "He is in a great hurry.", "هو يستعجل كثيراً.", "sich = Reflexivpronomen")
                    )
                ),
                GrammarReferenceTable(
                    id = "demonstrativpronomen",
                    titleDe = "Demonstrativpronomen",
                    titleEn = "Demonstrative Pronouns",
                    titleAr = "الضمائر الإشارية (Demonstrativpronomen)",
                    subtitleDe = "dieser, diese, dieses / jener / der, die, das",
                    subtitleEn = "this, that, these, those",
                    subtitleAr = "أسماء الإشارة (هذا، هذه، هؤلاء)",
                    headers = listOf("Kasus", "Maskulin (der)", "Feminin (die)", "Neutrum (das)", "Plural (die)"),
                    headersEn = listOf("Case", "Masculine", "Feminine", "Neuter", "Plural"),
                    headersAr = listOf("الحالة", "مذكر", "مؤنث", "محايد", "جمع"),
                    rows = listOf(
                        GrammarTableRow("Nominativ", listOf("dieser (der)", "diese (die)", "dieses (das)", "diese (die)"), "Nominativ", "Nominative", "الرفع"),
                        GrammarTableRow("Akkusativ", listOf("diesen (den)", "diese (die)", "dieses (das)", "diese (die)"), "Akkusativ", "Accusative", "النصب"),
                        GrammarTableRow("Dativ", listOf("diesem (dem)", "dieser (der)", "diesem (dem)", "diesen (den)"), "Dativ", "Dative", "الجر"),
                        GrammarTableRow("Genitiv", listOf("dieses (dessen)", "dieser (deren)", "dieses (dessen)", "dieser (deren)"), "Genitiv", "Genitive", "الإضافة")
                    ),
                    notesDe = "'Dieser/diese/dieses' betont das Naheliegende (dieses hier). Die betonten Artikel (der, die, das) haben im Genitiv besondere Formen: dessen / deren.",
                    notesEn = "'Dieser/diese/dieses' emphasizes something close (this one). Demonstrative articles have special genitive forms: dessen / deren.",
                    notesAr = "'Dieser' تشير إلى القريب (هذا هنا). أدوات الإشارة der/die/das لها صيغ خاصة في حالة الإضافة: dessen / deren.",
                    examples = listOf(
                        GrammarExample("Dieses Buch gefällt mir sehr gut.", "I really like this book.", "يعجبني هذا الكتاب كثيراً.", "dieses = Nominativ Neutrum"),
                        GrammarExample("Mit diesen Freunden verbringe ich gern Zeit.", "I like spending time with these friends.", "أحب قضاء الوقت مع هؤلاء الأصدقاء.", "diesen = Dativ Plural"),
                        GrammarExample("Diesen Film habe ich schon gesehen.", "I have already seen this movie.", "لقد شاهدت هذا الفيلم من قبل.", "diesen = Akkusativ Maskulin")
                    )
                ),
                GrammarReferenceTable(
                    id = "interrogativpronomen",
                    titleDe = "Interrogativpronomen",
                    titleEn = "Interrogative Pronouns",
                    titleAr = "ضمائر الاستفهام (Interrogativpronomen)",
                    subtitleDe = "wer, wen, wem, wessen & was / welcher",
                    subtitleEn = "who, whom, whose, what, which",
                    subtitleAr = "ضمائر وأدوات السؤال عن الأشخاص والأشياء",
                    headers = listOf("Kasus", "Person (Wer?)", "Sache (Was?)", "Auswahl (Welch-?)"),
                    headersEn = listOf("Case", "Person (Who?)", "Thing (What?)", "Selection (Which?)"),
                    headersAr = listOf("الحالة", "الأشخاص (مَن؟)", "الأشياء (ماذا؟)", "التحديد (أي؟)"),
                    rows = listOf(
                        GrammarTableRow("Nominativ", listOf("wer (Who?)", "was (What?)", "welcher / welche / welches"), "Nominativ", "Nominative", "الرفع"),
                        GrammarTableRow("Akkusativ", listOf("wen (Whom?)", "was (What?)", "welchen / welche / welches"), "Akkusativ", "Accusative", "النصب"),
                        GrammarTableRow("Dativ", listOf("wem (To whom?)", "– (Dativ bei was selten)", "welchem / welcher / welchem"), "Dativ", "Dative", "الجر"),
                        GrammarTableRow("Genitiv", listOf("wessen (Whose?)", "wessen (Whose?)", "welches / welcher / welches"), "Genitiv", "Genitive", "الإضافة")
                    ),
                    notesDe = "'Wer' fragt nach Personen und ändert sich nach Kasus (wer, wen, wem, wessen). 'Was' fragt nach Sachen und bleibt im Nominativ und Akkusativ gleich.",
                    notesEn = "'Wer' asks for persons and inflects across cases (wer, wen, wem, wessen). 'Was' asks for things and has the same form for Nom and Akk.",
                    notesAr = "'Wer' يسأل عن الأشخاص ويتصرف إعرابياً (wer, wen, wem, wessen). 'Was' يسأل عن الأشياء ولا يتغير في الرفع والنصب.",
                    examples = listOf(
                        GrammarExample("Wer hat das Fenster geöffnet?", "Who opened the window?", "مَن فتح النافذة؟", "Wer = Nominativ"),
                        GrammarExample("Wen hast du gestern angerufen?", "Whom did you call yesterday?", "بمَن اتصلت البارحة؟", "Wen = Akkusativ"),
                        GrammarExample("Wem gehört dieses rote Fahrrad?", "To whom does this red bike belong?", "لمَن تنتمي هذه الدراجة الحمراء؟", "Wem = Dativ"),
                        GrammarExample("Wessen Schlüssel liegt auf dem Tisch?", "Whose key is lying on the table?", "مفتاح مَن هذا الموضوع على الطاولة؟", "Wessen = Genitiv")
                    )
                ),
                GrammarReferenceTable(
                    id = "indefinitpronomen",
                    titleDe = "Indefinitpronomen",
                    titleEn = "Indefinite Pronouns",
                    titleAr = "ضمائر غير محددة (Indefinitpronomen)",
                    subtitleDe = "man, jemand, niemand, etwas, nichts, alle",
                    subtitleEn = "someone, no one, something, nothing, all",
                    subtitleAr = "ضمائر النكرة والعموم والكميات غير المحددة",
                    headers = listOf("Kategorie", "Pronomen", "Bedeutung / Funktion", "Beispiel"),
                    headersEn = listOf("Category", "Pronouns", "Meaning / Function", "Example"),
                    headersAr = listOf("الفئة", "الضمير", "الوظيفة والمعنى", "مثال"),
                    rows = listOf(
                        GrammarTableRow("Person (allgemein)", listOf("man", "jede beliebige Person (3. Sg.)", "Hier darf man nicht rauchen."), "-", "General Person", "شخص عام"),
                        GrammarTableRow("Person (unbestimmt)", listOf("jemand / niemand", "eine / keine Person", "Hat jemand meinen Stift gesehen?"), "-", "Indefinite Person", "شخص غير محدد"),
                        GrammarTableRow("Sache (unbestimmt)", listOf("etwas / nichts", "ein Ding / kein Ding", "Ich habe etwas Interessantes gehört."), "-", "Indefinite Thing", "شيء غير محدد"),
                        GrammarTableRow("Menge (Plural)", listOf("alle / viele / wenige", "Gesamtheit oder Teilmenge", "Viele Menschen lernen Deutsch."), "-", "Quantity (Pl.)", "كمية (جمع)"),
                        GrammarTableRow("Ersatz für Nomen", listOf("einer / keiner / welche", "Stellvertreter für Nomen", "Hast du ein Auto? – Nein, ich habe keins."), "-", "Noun Substitute", "بديل للاسم")
                    ),
                    notesDe = "'Man' steht immer nur im Nominativ Singular mit dem Verb in der 3. Person Sg. ('man kann', 'man muss'). Im Akkusativ/Dativ wird es durch 'einen / einem' ersetzt.",
                    notesEn = "'Man' (one/people) is used only in Nominative Singular with 3rd person singular verbs. In Accusative/Dative it is replaced by 'einen / einem'.",
                    notesAr = "الضمير 'man' (المرء) يأتي فقط في حالة الرفع المفرد مع فعل في صيغة الغائب المفرد. في النصب والجر يُستبدل بـ einen / einem.",
                    examples = listOf(
                        GrammarExample("Kann mir jemand bei der Aufgabe helfen?", "Can someone help me with the task?", "هل يمكن لأحد أن يساعدني في هذه المهمة؟", "jemand = unbestimmte Person"),
                        GrammarExample("Hier kann man sehr gut und entspannt lernen.", "One can learn very well and relaxed here.", "هنا يمكن للمرء أن يتعلم بشكل ممتاز وهادئ.", "man = 3. Person Singular"),
                        GrammarExample("Niemand weiß die richtige Antwort.", "Nobody knows the correct answer.", "لا أحد يعرف الجواب الصحيح.", "niemand = Negation")
                    )
                )
            )
        ),
        GrammarTableCategory(
            id = "possessiv",
            titleDe = "Possessiv",
            titleEn = "Possessive",
            titleAr = "ضمائر الملكية",
            iconName = "Star",
            descriptionDe = "Possessivartikel & Possessivpronomen für alle Personen in allen 4 Fällen (Nominativ, Akkusativ, Dativ, Genitiv).",
            descriptionEn = "Possessive determiners & pronouns for all persons across all 4 German cases.",
            descriptionAr = "جداول شاملة لضمائر وأدوات الملكية لجميع الضمائر في الحالات الإعرابية الأربع.",
            tables = listOf(
                GrammarReferenceTable(
                    id = "possessiv_uebersicht",
                    titleDe = "Possessivartikel Übersicht (Nominativ)",
                    titleEn = "Possessive Overview (Nominative)",
                    titleAr = "نظرة عامة على أدوات الملكية (حالة الرفع)",
                    subtitleDe = "Grundformen nach Besitzer und Genus",
                    subtitleEn = "Basic forms by owner and gender",
                    subtitleAr = "الصيغ الأساسية حسب المالك وجنس الاسم",
                    headers = listOf("Besitzer", "Maskulin (der)", "Feminin (die)", "Neutrum (das)", "Plural (die)"),
                    headersEn = listOf("Owner", "Masculine", "Feminine", "Neuter", "Plural"),
                    headersAr = listOf("المالك", "مذكر", "مؤنث", "محايد", "جمع"),
                    rows = listOf(
                        GrammarTableRow("1. Sg. (ich)", listOf("mein", "meine", "mein", "meine"), "ich", "ich (my)", "ich (لي/أنا)"),
                        GrammarTableRow("2. Sg. (du)", listOf("dein", "deine", "dein", "deine"), "du", "du (your)", "du (لك/أنت)"),
                        GrammarTableRow("3. Sg. m./n. (er/es)", listOf("sein", "seine", "sein", "seine"), "er", "er/es (his/its)", "er/es (له)"),
                        GrammarTableRow("3. Sg. f. (sie)", listOf("ihr", "ihre", "ihr", "ihre"), "sie", "sie (her)", "sie (لها)"),
                        GrammarTableRow("1. Pl. (wir)", listOf("unser", "unsere", "unser", "unsere"), "wir", "wir (our)", "wir (لنا)"),
                        GrammarTableRow("2. Pl. (ihr)", listOf("euer", "eure", "euer", "eure"), "ihr", "ihr (your pl.)", "ihr (لكم)"),
                        GrammarTableRow("3. Pl. (sie)", listOf("ihr", "ihre", "ihr", "ihre"), "sie", "sie (their)", "sie (لهم)"),
                        GrammarTableRow("Höflich (Sie)", listOf("Ihr", "Ihre", "Ihr", "Ihre"), "Sie", "Sie (Your formal)", "Sie (لحضرتك)")
                    ),
                    notesDe = "Possessivartikel begleiten das Nomen wie der unbestimmte Artikel 'ein'. Im Maskulinum und Neutrum Nominativ erhalten sie keine Endung (-).",
                    notesEn = "Possessive determiners accompany nouns like 'ein'. In Masculine and Neuter Nominative they take no ending (-).",
                    notesAr = "أدوات الملكية تتبع الاسم مثل أداة النكرة 'ein'. في حالتي المذكر والمحايد رفعاً لا تأخذ نهاية (-).",
                    examples = listOf(
                        GrammarExample("Das ist mein Vater, meine Mutter und mein Kind.", "That is my father, my mother, and my child.", "هذا والدي، والدتي وطفلي.", "mein (Mask./Neut.), meine (Fem./Pl.)"),
                        GrammarExample("Wo sind eure Koffer?", "Where are your suitcases?", "أين حقائبكم؟", "eure = Plural")
                    )
                ),
                GrammarReferenceTable(
                    id = "possessiv_mein",
                    titleDe = "1. Person Sg.: mein (ich)",
                    titleEn = "1st Person Sg.: mein (I / my)",
                    titleAr = "المتكلم المفرد: mein (أنا / ملكي)",
                    subtitleDe = "Vollständige Deklination in allen 4 Kasus",
                    subtitleEn = "Full declension across all 4 cases",
                    subtitleAr = "التصريف الكامل لـ mein في الحالات الإعرابية الأربع",
                    headers = listOf("Kasus", "Maskulin", "Feminin", "Neutrum", "Plural"),
                    headersEn = listOf("Case", "Masculine", "Feminine", "Neuter", "Plural"),
                    headersAr = listOf("الحالة", "مذكر", "مؤنث", "محايد", "جمع"),
                    rows = listOf(
                        GrammarTableRow("Nominativ", listOf("mein", "meine", "mein", "meine"), "Nominativ", "Nominative", "الرفع"),
                        GrammarTableRow("Akkusativ", listOf("meinen", "meine", "mein", "meine"), "Akkusativ", "Accusative", "النصب"),
                        GrammarTableRow("Dativ", listOf("meinem", "meiner", "meinem", "meinen"), "Dativ", "Dative", "الجر"),
                        GrammarTableRow("Genitiv", listOf("meines", "meiner", "meines", "meiner"), "Genitiv", "Genitive", "الإضافة")
                    ),
                    notesDe = "Im Dativ Plural erhält das nachfolgende Nomen zusätzlich ein '-n' (z.B. 'mit meinen Freunden').",
                    notesEn = "In Dative Plural, the following noun also receives an '-n' suffix (e.g. 'mit meinen Freunden').",
                    notesAr = "في حالة الجر للجمع (Dativ Plural) يُضاف حرف 'n' للاسم التابع أيضاً (مثل mit meinen Freunden).",
                    examples = listOf(
                        GrammarExample("Mein Bruder besucht mich heute.", "My brother is visiting me today.", "أخي يزورني اليوم.", "mein = Nominativ Maskulin"),
                        GrammarExample("Ich rufe meinen Vater an.", "I am calling my father.", "أنا اتصل بوالدي.", "meinen = Akkusativ Maskulin (-en)"),
                        GrammarExample("Ich helfe meiner Mutter und meinem Bruder.", "I help my mother and my brother.", "أساعد أمي وأخي.", "meiner = Dativ Feminin, meinem = Dativ Maskulin"),
                        GrammarExample("Das ist das Auto meines Vaters.", "That is my father's car.", "هذه سيارة والدي.", "meines = Genitiv Maskulin (-es)")
                    )
                ),
                GrammarReferenceTable(
                    id = "possessiv_dein",
                    titleDe = "2. Person Sg.: dein (du)",
                    titleEn = "2nd Person Sg.: dein (you / your)",
                    titleAr = "المخاطب المفرد: dein (أنت / ملكك)",
                    subtitleDe = "Vollständige Deklination in allen 4 Kasus",
                    subtitleEn = "Full declension across all 4 cases",
                    subtitleAr = "التصريف الكامل لـ dein في الحالات الإعرابية الأربع",
                    headers = listOf("Kasus", "Maskulin", "Feminin", "Neutrum", "Plural"),
                    headersEn = listOf("Case", "Masculine", "Feminine", "Neuter", "Plural"),
                    headersAr = listOf("الحالة", "مذكر", "مؤنث", "محايد", "جمع"),
                    rows = listOf(
                        GrammarTableRow("Nominativ", listOf("dein", "deine", "dein", "deine"), "Nominativ", "Nominative", "الرفع"),
                        GrammarTableRow("Akkusativ", listOf("deinen", "deine", "dein", "deine"), "Akkusativ", "Accusative", "النصب"),
                        GrammarTableRow("Dativ", listOf("deinem", "deiner", "deinem", "deinen"), "Dativ", "Dative", "الجر"),
                        GrammarTableRow("Genitiv", listOf("deines", "deiner", "deines", "deiner"), "Genitiv", "Genitive", "الإضافة")
                    ),
                    notesDe = "Folgt exakt den gleichen Endungen wie 'mein' (-en im Akkusativ Maskulin, -em/-er/-em/-en im Dativ, -es/-er/-es/-er im Genitiv).",
                    notesEn = "Follows the exact same ending pattern as 'mein'.",
                    notesAr = "يتبع نفس نهايات 'mein' تماماً في كل الحالات.",
                    examples = listOf(
                        GrammarExample("Ist das dein neuer Computer?", "Is that your new computer?", "هل هذا حاسوبك الجديد؟", "dein = Nominativ Maskulin"),
                        GrammarExample("Hast du deinen Schlüssel gefunden?", "Did you find your key?", "هل وجدت مفتاحك؟", "deinen = Akkusativ Maskulin"),
                        GrammarExample("Ich danke deiner Schwester für die Einladung.", "I thank your sister for the invitation.", "أشكر أختك على الدعوة.", "deiner = Dativ Feminin")
                    )
                ),
                GrammarReferenceTable(
                    id = "possessiv_sein",
                    titleDe = "3. Person Sg. Maskulin/Neutrum: sein (er / es)",
                    titleEn = "3rd Person Sg. Masc/Neut: sein (he/it / his/its)",
                    titleAr = "الغائب المذكر والمحايد: sein (هو/محايد / ملكه)",
                    subtitleDe = "Vollständige Deklination in allen 4 Kasus",
                    subtitleEn = "Full declension across all 4 cases",
                    subtitleAr = "التصريف الكامل لـ sein في الحالات الإعرابية الأربع",
                    headers = listOf("Kasus", "Maskulin", "Feminin", "Neutrum", "Plural"),
                    headersEn = listOf("Case", "Masculine", "Feminine", "Neuter", "Plural"),
                    headersAr = listOf("الحالة", "مذكر", "مؤنث", "محايد", "جمع"),
                    rows = listOf(
                        GrammarTableRow("Nominativ", listOf("sein", "seine", "sein", "seine"), "Nominativ", "Nominative", "الرفع"),
                        GrammarTableRow("Akkusativ", listOf("seinen", "seine", "sein", "seine"), "Akkusativ", "Accusative", "النصب"),
                        GrammarTableRow("Dativ", listOf("seinem", "seiner", "seinem", "seinen"), "Dativ", "Dative", "الجر"),
                        GrammarTableRow("Genitiv", listOf("seines", "seiner", "seines", "seiner"), "Genitiv", "Genitive", "الإضافة")
                    ),
                    notesDe = "'sein' wird verwendet, wenn der Besitzer männlich (er/der Mann) oder sächlich (es/das Kind) ist.",
                    notesEn = "'sein' is used when the owner is masculine (er) or neuter (es).",
                    notesAr = "تُستخدم 'sein' عندما يكون المالك مذكراً (er) أو محايداً (es).",
                    examples = listOf(
                        GrammarExample("Thomas sucht seinen Hund.", "Thomas is looking for his dog.", "توماس يبحث عن كلبه.", "seinen = Akkusativ Maskulin"),
                        GrammarExample("Das Kind spielt mit seinem Spielzeug.", "The child plays with its toy.", "الطفل يلعب بلعبته.", "seinem = Dativ Neutrum"),
                        GrammarExample("Wegen seines Erfolgs feiert er ein großes Fest.", "Because of his success he celebrates a big party.", "بسبب نجاحه يقيم احتفالاً كبيراً.", "seines = Genitiv Maskulin")
                    )
                ),
                GrammarReferenceTable(
                    id = "possessiv_ihr_sg",
                    titleDe = "3. Person Sg. Feminin: ihr (sie)",
                    titleEn = "3rd Person Sg. Fem: ihr (she / her)",
                    titleAr = "الغائب المؤنث المفرد: ihr (هي / ملكها)",
                    subtitleDe = "Vollständige Deklination in allen 4 Kasus",
                    subtitleEn = "Full declension across all 4 cases",
                    subtitleAr = "التصريف الكامل لـ ihr في الحالات الإعرابية الأربع",
                    headers = listOf("Kasus", "Maskulin", "Feminin", "Neutrum", "Plural"),
                    headersEn = listOf("Case", "Masculine", "Feminine", "Neuter", "Plural"),
                    headersAr = listOf("الحالة", "مذكر", "مؤنث", "محايد", "جمع"),
                    rows = listOf(
                        GrammarTableRow("Nominativ", listOf("ihr", "ihre", "ihr", "ihre"), "Nominativ", "Nominative", "الرفع"),
                        GrammarTableRow("Akkusativ", listOf("ihren", "ihre", "ihr", "ihre"), "Akkusativ", "Accusative", "النصب"),
                        GrammarTableRow("Dativ", listOf("ihrem", "ihrer", "ihrem", "ihren"), "Dativ", "Dative", "الجر"),
                        GrammarTableRow("Genitiv", listOf("ihres", "ihrer", "ihres", "ihrer"), "Genitiv", "Genitive", "الإضافة")
                    ),
                    notesDe = "'ihr' (kleingeschrieben) gehört zur weiblichen Person (sie / die Frau). Nicht verwechseln mit 'ihr' (Personalpronomen: ihr lernt).",
                    notesEn = "'ihr' (lowercase) refers to a feminine owner (she / her).",
                    notesAr = "'ihr' (بحرف صغير) تشير إلى مالكة مؤنثة (هي / لها).",
                    examples = listOf(
                        GrammarExample("Maria liebt ihren Beruf.", "Maria loves her job.", "ماريا تحب عملها.", "ihren = Akkusativ Maskulin"),
                        GrammarExample("Sie fährt mit ihrem Fahrrad zur Arbeit.", "She rides to work on her bicycle.", "هي تذهب إلى العمل بدراجتها.", "ihrem = Dativ Neutrum"),
                        GrammarExample("Die Wohnung ihrer Eltern ist sehr gemütlich.", "Her parents' apartment is very cozy.", "شقة والديها مريحة جداً.", "ihrer = Genitiv Plural")
                    )
                ),
                GrammarReferenceTable(
                    id = "possessiv_unser",
                    titleDe = "1. Person Plural: unser (wir)",
                    titleEn = "1st Person Plural: unser (we / our)",
                    titleAr = "المتكلم الجمع: unser (نحن / ملكنا)",
                    subtitleDe = "Vollständige Deklination in allen 4 Kasus",
                    subtitleEn = "Full declension across all 4 cases",
                    subtitleAr = "التصريف الكامل لـ unser في الحالات الإعرابية الأربع",
                    headers = listOf("Kasus", "Maskulin", "Feminin", "Neutrum", "Plural"),
                    headersEn = listOf("Case", "Masculine", "Feminine", "Neuter", "Plural"),
                    headersAr = listOf("الحالة", "مذكر", "مؤنث", "محايد", "جمع"),
                    rows = listOf(
                        GrammarTableRow("Nominativ", listOf("unser", "unsere", "unser", "unsere"), "Nominativ", "Nominative", "الرفع"),
                        GrammarTableRow("Akkusativ", listOf("unseren", "unsere", "unser", "unsere"), "Akkusativ", "Accusative", "النصب"),
                        GrammarTableRow("Dativ", listOf("unserem", "unserer", "unserem", "unseren"), "Dativ", "Dative", "الجر"),
                        GrammarTableRow("Genitiv", listOf("unseres", "unserer", "unseres", "unserer"), "Genitiv", "Genitive", "الإضافة")
                    ),
                    notesDe = "Stammform ist 'unser'. Endungen werden direkt angehängt: unsere, unseren, unserem, unserer, unseres.",
                    notesEn = "Root is 'unser'. Standard endings are attached directly: unsere, unseren, unserem, unserer, unseres.",
                    notesAr = "الجذر هو 'unser' وتضاف النهايات القياسية مباشرة: unsere, unseren, unserem, unserer, unseres.",
                    examples = listOf(
                        GrammarExample("Unser Haus hat einen großen Garten.", "Our house has a big garden.", "منزلنا يحتوي على حديقة كبيرة.", "unser = Nominativ Neutrum"),
                        GrammarExample("Wir besuchen unseren Großvater am Wochenende.", "We visit our grandfather on the weekend.", "نزور جدنا في عطلة نهاية الأسبوع.", "unseren = Akkusativ Maskulin"),
                        GrammarExample("In unserer Stadt gibt es viele Sehenswürdigkeiten.", "In our city there are many sights.", "في مدينتنا توجد معالم سياحية كثيرة.", "unserer = Dativ Feminin")
                    )
                ),
                GrammarReferenceTable(
                    id = "possessiv_euer",
                    titleDe = "2. Person Plural: euer (ihr)",
                    titleEn = "2nd Person Plural: euer (you pl. / your)",
                    titleAr = "المخاطب الجمع: euer (أنتم / ملككم)",
                    subtitleDe = "Vollständige Deklination mit e-Ausfall (eure, eurem...)",
                    subtitleEn = "Full declension with 'e'-drop (eure, eurem...)",
                    subtitleAr = "التصريف الكامل مع سقوط حرف e الأوسط (eure, eurem...)",
                    headers = listOf("Kasus", "Maskulin", "Feminin", "Neutrum", "Plural"),
                    headersEn = listOf("Case", "Masculine", "Feminine", "Neuter", "Plural"),
                    headersAr = listOf("الحالة", "مذكر", "مؤنث", "محايد", "جمع"),
                    rows = listOf(
                        GrammarTableRow("Nominativ", listOf("euer", "eure", "euer", "eure"), "Nominativ", "Nominative", "الرفع"),
                        GrammarTableRow("Akkusativ", listOf("euren", "eure", "euer", "eure"), "Akkusativ", "Accusative", "النصب"),
                        GrammarTableRow("Dativ", listOf("eurem", "eurer", "eurem", "euren"), "Dativ", "Dative", "الجر"),
                        GrammarTableRow("Genitiv", listOf("eures", "eurer", "eures", "eurer"), "Genitiv", "Genitive", "الإضافة")
                    ),
                    notesDe = "WICHTIGER MERKSATZ: Bei 'euer' entfällt das mittlere '-e-' bei allen Endungen! Es heißt 'eure', 'euren', 'eurem', 'eurer', 'eures' (NIEMALS 'euere' oder 'euerem').",
                    notesEn = "CRITICAL RULE: With 'euer', the middle '-e-' drops whenever an ending is added: 'eure', 'euren', 'eurem', 'eurer', 'eures' (NEVER 'euere').",
                    notesAr = "ملاحظة تعليمية بالغة الأهمية (Merke): تسقط الـ '-e-' الوسطى من 'euer' عند إضافة أي لاحقة: eure, euren, eurem, eurer, eures (ولا يُقال أبداً euere).",
                    examples = listOf(
                        GrammarExample("Ist das euer Lehrer?", "Is that your teacher?", "هل هذا معلمكم؟", "euer = Nominativ Maskulin"),
                        GrammarExample("Habt ihr eure Hausaufgaben schon gemacht?", "Have you already done your homework?", "هل أنجزتم واجباتكم المدرسية؟", "eure = Akkusativ Plural (e-Ausfall)"),
                        GrammarExample("Wir gratulieren herzlich zu eurem Jubiläum!", "We warmly congratulate you on your anniversary!", "نهنئكم من القلب بمناسبتكم السعيدة!", "eurem = Dativ Neutrum")
                    )
                ),
                GrammarReferenceTable(
                    id = "possessiv_ihr_pl_sie",
                    titleDe = "3. Plural & Höflichkeitsform: ihr / Ihr",
                    titleEn = "3rd Plural & Formal: ihr / Ihr (their / Your)",
                    titleAr = "الغائب الجمع وصيغة الاحترام: ihr / Ihr (لهم / لحضرتك)",
                    subtitleDe = "ihr (sie Pl.) vs. Ihr (Sie Höflichkeitsform - Großschreibung)",
                    subtitleEn = "ihr (they/their) vs. Ihr (You/Your formal - capitalized)",
                    subtitleAr = "ihr (للغائبين) مقابل Ihr (لحضرتك بصيغة الاحترام وتكتب بحرف كبير دائماً)",
                    headers = listOf("Kasus", "Maskulin", "Feminin", "Neutrum", "Plural"),
                    headersEn = listOf("Case", "Masculine", "Feminine", "Neuter", "Plural"),
                    headersAr = listOf("الحالة", "مذكر", "مؤنث", "محايد", "جمع"),
                    rows = listOf(
                        GrammarTableRow("Nominativ", listOf("ihr / Ihr", "ihre / Ihre", "ihr / Ihr", "ihre / Ihre"), "Nominativ", "Nominative", "الرفع"),
                        GrammarTableRow("Akkusativ", listOf("ihren / Ihren", "ihre / Ihre", "ihr / Ihr", "ihre / Ihre"), "Akkusativ", "Accusative", "النصب"),
                        GrammarTableRow("Dativ", listOf("ihrem / Ihrem", "ihrer / Ihrer", "ihrem / Ihrem", "ihren / Ihren"), "Dativ", "Dative", "الجر"),
                        GrammarTableRow("Genitiv", listOf("ihres / Ihres", "ihrer / Ihrer", "ihres / Ihres", "ihrer / Ihrer"), "Genitiv", "Genitive", "الإضافة")
                    ),
                    notesDe = "Die Höflichkeitsform 'Ihr, Ihre, Ihren, Ihrem, Ihres' wird im Deutschen in Briefen und Anreden IMMER großgeschrieben.",
                    notesEn = "The formal possessive 'Ihr, Ihre, Ihren, Ihrem, Ihres' is ALWAYS capitalized in German.",
                    notesAr = "صيغة الاحترام 'Ihr / Ihre / Ihren / Ihrem / Ihres' تُكتب دائماً بحرف كبير (Großschreibung) في الألمانية للتعبير عن التقدير.",
                    examples = listOf(
                        GrammarExample("Die Kinder packen ihre Schulsachen ein.", "The children pack their school things.", "الأطفال يوضبون أغراضهم المدرسية.", "ihre = 3. Person Plural (klein)"),
                        GrammarExample("Herr Müller, ist das Ihr Koffer?", "Mr. Müller, is that your suitcase?", "سيد مولر، هل هذه حقيبة حضرتك؟", "Ihr = Höflichkeitsform (groß)"),
                        GrammarExample("Wir danken Ihnen für Ihre freundliche Unterstützung.", "We thank you for your kind support.", "نشكر حضرتكم على دعمكم اللطيف.", "Ihre = Höflichkeitsform Akkusativ")
                    )
                ),
                GrammarReferenceTable(
                    id = "possessiv_endungen_schema",
                    titleDe = "Systematisches Endungsschema aller Possessivartikel",
                    titleEn = "Possessive Determiner Endings Rule",
                    titleAr = "المخطط الشامل لنهايات ولواحق أدوات الملكية",
                    subtitleDe = "Die Endungen entsprechen dem unbestimmten Artikel 'ein / kein'",
                    subtitleEn = "Endings follow the indefinite article pattern (ein / kein)",
                    subtitleAr = "تتبع نفس نهايات أداة النكرة (ein / kein) في كل الحالات",
                    headers = listOf("Kasus", "Maskulin (der)", "Feminin (die)", "Neutrum (das)", "Plural (die)"),
                    headersEn = listOf("Case", "Masculine", "Feminine", "Neuter", "Plural"),
                    headersAr = listOf("الحالة", "مذكر (der)", "مؤنث (die)", "محايد (das)", "جمع (die)"),
                    rows = listOf(
                        GrammarTableRow("Nominativ", listOf("- [keine Endung]", "-e", "- [keine Endung]", "-e"), "Nominativ", "Nominative", "الرفع"),
                        GrammarTableRow("Akkusativ", listOf("-en", "-e", "- [keine Endung]", "-e"), "Akkusativ", "Accusative", "النصب"),
                        GrammarTableRow("Dativ", listOf("-em", "-er", "-em", "-en (+n)"), "Dativ", "Dative", "الجر"),
                        GrammarTableRow("Genitiv", listOf("-es (+s)", "-er", "-es (+s)", "-er"), "Genitiv", "Genitive", "الإضافة")
                    ),
                    notesDe = "Merke: Maskulin und Neutrum haben im Nominativ KEINE Endung. Im Akkusativ ändert sich NUR Maskulin (-en). Im Dativ Plural bekommt auch das Nomen ein '-n'.",
                    notesEn = "Rule: Masculine and Neuter have NO ending in Nominative. In Accusative ONLY Masculine changes (-en). In Dative Plural the noun also takes '-n'.",
                    notesAr = "قاعدة ذهبية: المذكر والمحايد ليس لهما نهاية في الرفع. في النصب يتغير المذكر فقط (-en). في جر الجمع يضاف 'n' للاسم أيضاً.",
                    examples = listOf(
                        GrammarExample("Maskulin: mein -> meinen (Akk) -> meinem (Dat) -> meines (Gen)", "Masculine ending progression", "تدرج نهايات المذكر", "Endungen: -, -en, -em, -es"),
                        GrammarExample("Feminin: meine -> meine (Akk) -> meiner (Dat) -> meiner (Gen)", "Feminine ending progression", "تدرج نهايات المؤنث", "Endungen: -e, -e, -er, -er"),
                        GrammarExample("Neutrum: mein -> mein (Akk) -> meinem (Dat) -> meines (Gen)", "Neuter ending progression", "تدرج نهايات المحايد", "Endungen: -, -, -em, -es"),
                        GrammarExample("Plural: meine -> meine (Akk) -> meinen+n (Dat) -> meiner (Gen)", "Plural ending progression", "تدرج نهايات الجمع", "Endungen: -e, -e, -en, -er")
                    )
                )
            )
        ),
        GrammarTableCategory(
            id = "praepositionen",
            titleDe = "Präpositionen",
            titleEn = "Prepositions",
            titleAr = "حروف الجر",
            iconName = "Translate",
            descriptionDe = "Präpositionen mit festem Kasus sowie Wechselpräpositionen (Akkusativ vs. Dativ).",
            descriptionEn = "Fixed-case prepositions and two-way prepositions (Accusative vs. Dative).",
            descriptionAr = "حروف الجر ذات الحالات الثابتة والحروف المشتركة (النصب مقابل الجر).",
            tables = listOf(
                GrammarReferenceTable(
                    id = "praep_kasus",
                    titleDe = "Feste Präpositionen nach Kasus",
                    titleEn = "Fixed Prepositions by Case",
                    titleAr = "حروف الجر ذات الحالات الثابتة",
                    subtitleDe = "Akkusativ, Dativ und Genitiv",
                    subtitleEn = "Accusative, Dative, and Genitive",
                    subtitleAr = "النصب والجر والإضافة",
                    headers = listOf("Kasus", "Präpositionen", "Merkhilfe / Signal", "Beispiel"),
                    headersEn = listOf("Case", "Prepositions", "Mnemonic / Signal", "Example"),
                    headersAr = listOf("الحالة", "حروف الجر", "وسيلة الحفظ", "مثال"),
                    rows = listOf(
                        GrammarTableRow(
                            label = "Akkusativ",
                            labelEn = "Accusative",
                            labelAr = "النصب",
                            cells = listOf("bis, durch, für, gegen, ohne, um, entlang", "DOGFU (durch, ohne, gegen, für, um, entlang)", "Ich gehe durch den Park."),
                            speakText = "Akkusativ"
                        ),
                        GrammarTableRow(
                            label = "Dativ",
                            labelEn = "Dative",
                            labelAr = "الجر",
                            cells = listOf("aus, bei, mit, nach, seit, von, zu, gegenüber, außer", "ABMNSVZ (aus, bei, mit, nach, seit, von, zu)", "Ich fahre mit dem Bus zur Arbeit."),
                            speakText = "Dativ"
                        ),
                        GrammarTableRow(
                            label = "Genitiv",
                            labelEn = "Genitive",
                            labelAr = "الإضافة",
                            cells = listOf("während, wegen, trotz, anstatt, außerhalb, innerhalb", "Wegen / Trotz / Während", "Trotz des Regens gehen wir spazieren."),
                            speakText = "Genitiv"
                        )
                    ),
                    notesDe = "Nach diesen Präpositionen steht das nachfolgende Nomen/Pronomen IMMER im genannten Kasus.",
                    notesEn = "Nouns or pronouns following these prepositions are ALWAYS strictly in the specified case.",
                    notesAr = "الاسم أو الضمير الواقع بعد هذه الحروف يأتي دائماً وبشكل قاطع في الحالة المذكورة.",
                    examples = listOf(
                        GrammarExample("Ohne meinen Kaffee kann ich nicht arbeiten.", "Without my coffee I cannot work.", "بدون قهوتي لا أستطيع العمل.", "ohne + Akkusativ: meinen Kaffee"),
                        GrammarExample("Seit einem Monat lerne ich Deutsch mit meinen Freunden.", "For a month I have been learning German with my friends.", "منذ شهر وأنا أتعلم الألمانية مع أصدقائي.", "seit + Dativ: einem Monat; mit + Dativ: meinen Freunden")
                    )
                ),
                GrammarReferenceTable(
                    id = "wechselpraepositionen",
                    titleDe = "Wechselpräpositionen",
                    titleEn = "Two-Way Prepositions",
                    titleAr = "حروف الجر المشتركة (Wechselpräpositionen)",
                    subtitleDe = "Wohin? (Richtung = Akkusativ) vs. Wo? (Ort/Lage = Dativ)",
                    subtitleEn = "Where to? (Movement = Accusative) vs. Where? (Location = Dative)",
                    subtitleAr = "إلى أين؟ (حركة = Akkusativ) مقابل أين؟ (موضع = Dativ)",
                    headers = listOf("Präposition", "Akkusativ (Wohin? / Bewegung)", "Dativ (Wo? / Zustand)"),
                    headersEn = listOf("Preposition", "Accusative (Where to? / Movement)", "Dative (Where? / State)"),
                    headersAr = listOf("حرف الجر", "النصب (إلى أين؟ / حركة)", "الجر (أين؟ / سكون)"),
                    rows = listOf(
                        GrammarTableRow("an", listOf("Ich hänge das Bild an die Wand.", "Das Bild hängt an der Wand."), "an"),
                        GrammarTableRow("auf", listOf("Ich lege das Buch auf den Tisch.", "Das Buch liegt auf dem Tisch."), "auf"),
                        GrammarTableRow("hinter", listOf("Er geht hinter das Haus.", "Er steht hinter dem Haus."), "hinter"),
                        GrammarTableRow("in", listOf("Wir gehen in die Schule / ins Kino.", "Wir sind in der Schule / im Kino."), "in"),
                        GrammarTableRow("neben", listOf("Stell den Stuhl neben den Tisch.", "Der Stuhl steht neben dem Tisch."), "neben"),
                        GrammarTableRow("über", listOf("Er hängt die Lampe über den Tisch.", "Die Lampe hängt über dem Tisch."), "über"),
                        GrammarTableRow("unter", listOf("Die Katze läuft unter das Bett.", "Die Katze schläft unter dem Bett."), "unter"),
                        GrammarTableRow("vor", listOf("Fahr das Auto vor die Garage.", "Das Auto steht vor der Garage."), "vor"),
                        GrammarTableRow("zwischen", listOf("Ich setze mich zwischen die Freunde.", "Ich sitze zwischen den Freunden."), "zwischen")
                    ),
                    notesDe = "Merke: Verben der Bewegung mit Ziel (stellen, legen, setzen, hängen) verlangen Akkusativ (Wohin?). Verben der Position (stehen, liegen, sitzen, hängen) verlangen Dativ (Wo?).",
                    notesEn = "Rule: Directional verbs with a destination (stellen, legen, setzen) take Accusative (Where to?). Positional verbs (stehen, liegen, sitzen) take Dative (Where?).",
                    notesAr = "قاعدة: أفعال الحركة نحو هدف (stellen, legen, setzen) تأخذ Akkusativ. أفعال السكون والموضع (stehen, liegen, sitzen) تأخذ Dativ.",
                    examples = listOf(
                        GrammarExample("Wohin legst du den Stift? – Auf den Schreibtisch (Akkusativ).", "Where do you put the pen? – On the desk.", "إلى أين تضع القلم؟ – على المكتب (نصب).", "Richtung / Ziel = Akkusativ"),
                        GrammarExample("Wo liegt der Stift? – Auf dem Schreibtisch (Dativ).", "Where is the pen lying? – On the desk.", "أين يقع القلم؟ – على المكتب (جر).", "Ort / Position = Dativ")
                    )
                )
            )
        )
    )
}
