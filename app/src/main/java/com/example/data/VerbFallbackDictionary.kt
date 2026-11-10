package com.example.data

import com.example.data.model.VerbConjugationResult
import com.example.data.model.VerbImperativ
import com.example.data.model.VerbTenseConjugation
import com.example.data.model.WordExample

/**
 * Built-in dictionary and helper for German verbs in the Grammar Tables sheet.
 * Provides instant conjugations, prepositions, case objects, and examples,
 * serving as an offline and instant fallback for AI search.
 */
object VerbFallbackDictionary {

    /**
     * Common German verb prepositions mapping
     */
    val VERB_PREPOSITIONS: Map<String, List<String>> = mapOf(
        "gehen" to listOf(
            "zu + Dativ (z. B. Ich gehe zum Arzt)",
            "nach + Dativ (z. B. Wir gehen nach Hause)",
            "in + Akkusativ (z. B. Sie geht in die Schule)",
            "auf + Akkusativ (z. B. Er geht auf den Markt)"
        ),
        "fahren" to listOf(
            "mit + Dativ (z. B. mit dem Zug fahren)",
            "nach + Dativ (z. B. nach Berlin fahren)",
            "in + Akkusativ (z. B. in die Stadt fahren)"
        ),
        "kommen" to listOf(
            "aus + Dativ (z. B. aus Deutschland kommen)",
            "zu + Dativ (z. B. zur Party kommen)",
            "von + Dativ (z. B. von der Arbeit kommen)"
        ),
        "denken" to listOf(
            "an + Akkusativ (z. B. an die Zukunft denken)",
            "über + Akkusativ (z. B. über ein Problem nachdenken)"
        ),
        "warten" to listOf(
            "auf + Akkusativ (z. B. auf den Bus warten)"
        ),
        "sprechen" to listOf(
            "mit + Dativ (z. B. mit dem Lehrer sprechen)",
            "über + Akkusativ (z. B. über das Thema sprechen)",
            "von + Dativ (z. B. von Erfahrungen sprechen)"
        ),
        "gehören" to listOf(
            "zu + Dativ (z. B. Das gehört zu meinen Aufgaben)"
        ),
        "bitten" to listOf(
            "um + Akkusativ (z. B. um Hilfe bitten)"
        ),
        "fragen" to listOf(
            "nach + Dativ (z. B. nach dem Weg fragen)"
        ),
        "teilnehmen" to listOf(
            "an + Dativ (z. B. am Kurs teilnehmen)"
        ),
        "freuen" to listOf(
            "auf + Akkusativ (z. B. sich auf die Ferien freuen - Zukunft)",
            "über + Akkusativ (z. B. sich über das Geschenk freuen - Gegenwart)"
        ),
        "arbeiten" to listOf(
            "bei + Dativ (z. B. bei einer Firma arbeiten)",
            "an + Dativ (z. B. an einem Projekt arbeiten)"
        ),
        "lernen" to listOf(
            "für + Akkusativ (z. B. für die Prüfung lernen)",
            "von + Dativ (z. B. von den Besten lernen)"
        )
    )

    fun getPrepositionsForVerb(verb: String): List<String> {
        val clean = verb.trim().lowercase()
        return VERB_PREPOSITIONS[clean] ?: emptyList()
    }

    fun lookup(verb: String): VerbConjugationResult? {
        val clean = verb.trim().lowercase()
        return when (clean) {
            "gehen" -> createGehen()
            "fahren" -> createFahren()
            "kommen" -> createKommen()
            "lernen" -> createLernen()
            "arbeiten" -> createArbeiten()
            "sprechen" -> createSprechen()
            "sehen" -> createSehen()
            "essen" -> createEssen()
            "haben" -> createHaben()
            "sein" -> createSein()
            else -> null
        }
    }

    private fun createGehen() = VerbConjugationResult(
        word = "gehen",
        infinitiv = "gehen",
        hilfsverb = "sein",
        auxiliary = "(ist)",
        praeteritum = "ging",
        partizip1 = "gehend",
        partizip2 = "gegangen",
        verbType = "stark (unregelmäßig)",
        separable = "untrennbar",
        caseObject = "intransitiv / Dativ (mit zu)",
        meaningEnglish = "to go, to walk / يذهب، يمشي",
        imperativ = VerbImperativ(
            du = "geh(e)!",
            ihr = "geht!",
            sie = "gehen Sie!"
        ),
        tenses = listOf(
            VerbTenseConjugation(
                tenseKey = "praesens",
                tenseNameDe = "Präsens (Gegenwart)",
                ich = "gehe",
                du = "gehst",
                erSieEs = "geht",
                wir = "gehen",
                ihr = "geht",
                sieSie = "gehen"
            ),
            VerbTenseConjugation(
                tenseKey = "praeteritum",
                tenseNameDe = "Präteritum (Vergangenheit)",
                ich = "ging",
                du = "gingst",
                erSieEs = "ging",
                wir = "gingen",
                ihr = "gingt",
                sieSie = "gingen"
            ),
            VerbTenseConjugation(
                tenseKey = "perfekt",
                tenseNameDe = "Perfekt (Vollendete Gegenwart)",
                ich = "bin gegangen",
                du = "bist gegangen",
                erSieEs = "ist gegangen",
                wir = "sind gegangen",
                ihr = "seid gegangen",
                sieSie = "sind gegangen"
            ),
            VerbTenseConjugation(
                tenseKey = "plusquamperfekt",
                tenseNameDe = "Plusquamperfekt (Vorvergangenheit)",
                ich = "war gegangen",
                du = "warst gegangen",
                erSieEs = "war gegangen",
                wir = "waren gegangen",
                ihr = "wart gegangen",
                sieSie = "waren gegangen"
            ),
            VerbTenseConjugation(
                tenseKey = "futur1",
                tenseNameDe = "Futur I (Zukunft)",
                ich = "werde gehen",
                du = "wirst gehen",
                erSieEs = "wird gehen",
                wir = "werden gehen",
                ihr = "werdet gehen",
                sieSie = "werden gehen"
            )
        ),
        examples = listOf(
            WordExample("gehen", "Ich gehe jeden Tag zu Fuß zur Arbeit."),
            WordExample("gehen", "Er ist gestern sehr spät nach Hause gegangen."),
            WordExample("gehen", "Wohin gehst du am Wochenende?"),
            WordExample("gehen", "Wir gehen ins Kino, um einen neuen Film zu sehen.")
        )
    )

    private fun createFahren() = VerbConjugationResult(
        word = "fahren",
        infinitiv = "fahren",
        hilfsverb = "sein",
        auxiliary = "(ist)",
        praeteritum = "fuhr",
        partizip1 = "fahrend",
        partizip2 = "gefahren",
        verbType = "stark (a -> ä)",
        separable = "untrennbar",
        caseObject = "intransitiv / Akkusativ (Fahrzeug)",
        meaningEnglish = "to drive, to ride / يقود، يسافر بالمركبة",
        imperativ = VerbImperativ(
            du = "fahr(e)!",
            ihr = "fahrt!",
            sie = "fahren Sie!"
        ),
        tenses = listOf(
            VerbTenseConjugation(
                tenseKey = "praesens",
                tenseNameDe = "Präsens (Gegenwart)",
                ich = "fahre",
                du = "fährst",
                erSieEs = "fährt",
                wir = "fahren",
                ihr = "fahrt",
                sieSie = "fahren"
            ),
            VerbTenseConjugation(
                tenseKey = "praeteritum",
                tenseNameDe = "Präteritum (Vergangenheit)",
                ich = "fuhr",
                du = "fuhrst",
                erSieEs = "fuhr",
                wir = "fuhren",
                ihr = "fuhrt",
                sieSie = "fuhren"
            ),
            VerbTenseConjugation(
                tenseKey = "perfekt",
                tenseNameDe = "Perfekt (Vollendete Gegenwart)",
                ich = "bin gefahren",
                du = "bist gefahren",
                erSieEs = "ist gefahren",
                wir = "sind gefahren",
                ihr = "seid gefahren",
                sieSie = "sind gefahren"
            )
        ),
        examples = listOf(
            WordExample("fahren", "Wir fahren morgen früh nach Berlin."),
            WordExample("fahren", "Fährst du lieber mit dem Zug oder mit dem Auto?")
        )
    )

    private fun createKommen() = VerbConjugationResult(
        word = "kommen",
        infinitiv = "kommen",
        hilfsverb = "sein",
        auxiliary = "(ist)",
        praeteritum = "kam",
        partizip1 = "kommend",
        partizip2 = "gekommen",
        verbType = "stark (unregelmäßig)",
        separable = "untrennbar",
        caseObject = "intransitiv",
        meaningEnglish = "to come / يأتي",
        imperativ = VerbImperativ(
            du = "komm!",
            ihr = "kommt!",
            sie = "kommen Sie!"
        ),
        tenses = listOf(
            VerbTenseConjugation(
                tenseKey = "praesens",
                tenseNameDe = "Präsens (Gegenwart)",
                ich = "komme",
                du = "kommst",
                erSieEs = "kommt",
                wir = "kommen",
                ihr = "kommt",
                sieSie = "kommen"
            ),
            VerbTenseConjugation(
                tenseKey = "praeteritum",
                tenseNameDe = "Präteritum (Vergangenheit)",
                ich = "kam",
                du = "kamst",
                erSieEs = "kam",
                wir = "kamen",
                ihr = "kamt",
                sieSie = "kamen"
            ),
            VerbTenseConjugation(
                tenseKey = "perfekt",
                tenseNameDe = "Perfekt (Vollendete Gegenwart)",
                ich = "bin gekommen",
                du = "bist gekommen",
                erSieEs = "ist gekommen",
                wir = "sind gekommen",
                ihr = "seid gekommen",
                sieSie = "sind gekommen"
            )
        ),
        examples = listOf(
            WordExample("kommen", "Sie kommt aus der Schweiz."),
            WordExample("kommen", "Wann kommst du heute nach Hause?")
        )
    )

    private fun createLernen() = VerbConjugationResult(
        word = "lernen",
        infinitiv = "lernen",
        hilfsverb = "haben",
        auxiliary = "(hat)",
        praeteritum = "lernte",
        partizip1 = "lernend",
        partizip2 = "gelernt",
        verbType = "schwach (regelmäßig)",
        separable = "untrennbar",
        caseObject = "Akkusativ",
        meaningEnglish = "to learn, to study / يتعلم",
        imperativ = VerbImperativ(
            du = "lerne!",
            ihr = "lernt!",
            sie = "lernen Sie!"
        ),
        tenses = listOf(
            VerbTenseConjugation(
                tenseKey = "praesens",
                tenseNameDe = "Präsens (Gegenwart)",
                ich = "lerne",
                du = "lernst",
                erSieEs = "lernt",
                wir = "lernen",
                ihr = "lernt",
                sieSie = "lernen"
            ),
            VerbTenseConjugation(
                tenseKey = "praeteritum",
                tenseNameDe = "Präteritum (Vergangenheit)",
                ich = "lernte",
                du = "lerntest",
                erSieEs = "lernte",
                wir = "lernten",
                ihr = "lerntet",
                sieSie = "lernten"
            ),
            VerbTenseConjugation(
                tenseKey = "perfekt",
                tenseNameDe = "Perfekt (Vollendete Gegenwart)",
                ich = "habe gelernt",
                du = "hast gelernt",
                erSieEs = "hat gelernt",
                wir = "haben gelernt",
                ihr = "habt gelernt",
                sieSie = "haben gelernt"
            )
        ),
        examples = listOf(
            WordExample("lernen", "Ich lerne seit sechs Monaten intensiv Deutsch."),
            WordExample("lernen", "Sie hat fleißig für die Prüfung gelernt.")
        )
    )

    private fun createArbeiten() = VerbConjugationResult(
        word = "arbeiten",
        infinitiv = "arbeiten",
        hilfsverb = "haben",
        auxiliary = "(hat)",
        praeteritum = "arbeitete",
        partizip1 = "arbeitend",
        partizip2 = "gearbeitet",
        verbType = "schwach (regelmäßig)",
        separable = "untrennbar",
        caseObject = "intransitiv / an + Dativ",
        meaningEnglish = "to work / يعمل",
        imperativ = VerbImperativ(
            du = "arbeite!",
            ihr = "arbeitet!",
            sie = "arbeiten Sie!"
        ),
        tenses = listOf(
            VerbTenseConjugation(
                tenseKey = "praesens",
                tenseNameDe = "Präsens (Gegenwart)",
                ich = "arbeite",
                du = "arbeitest",
                erSieEs = "arbeitet",
                wir = "arbeiten",
                ihr = "arbeitet",
                sieSie = "arbeiten"
            ),
            VerbTenseConjugation(
                tenseKey = "praeteritum",
                tenseNameDe = "Präteritum (Vergangenheit)",
                ich = "arbeitete",
                du = "arbeitetest",
                erSieEs = "arbeitete",
                wir = "arbeiteten",
                ihr = "arbeitetet",
                sieSie = "arbeiteten"
            ),
            VerbTenseConjugation(
                tenseKey = "perfekt",
                tenseNameDe = "Perfekt (Vollendete Gegenwart)",
                ich = "habe gearbeitet",
                du = "hast gearbeitet",
                erSieEs = "hat gearbeitet",
                wir = "haben gearbeitet",
                ihr = "habt gearbeitet",
                sieSie = "haben gearbeitet"
            )
        ),
        examples = listOf(
            WordExample("arbeiten", "Er arbeitet als Softwareentwickler in München."),
            WordExample("arbeiten", "Wir haben die ganze Nacht an dem Projekt gearbeitet.")
        )
    )

    private fun createSprechen() = VerbConjugationResult(
        word = "sprechen",
        infinitiv = "sprechen",
        hilfsverb = "haben",
        auxiliary = "(hat)",
        praeteritum = "sprach",
        partizip1 = "sprechend",
        partizip2 = "gesprochen",
        verbType = "stark (e -> i)",
        separable = "untrennbar",
        caseObject = "Akkusativ (Sprache) / mit + Dativ",
        meaningEnglish = "to speak / يتحدث",
        imperativ = VerbImperativ(
            du = "sprich!",
            ihr = "sprecht!",
            sie = "sprechen Sie!"
        ),
        tenses = listOf(
            VerbTenseConjugation(
                tenseKey = "praesens",
                tenseNameDe = "Präsens (Gegenwart)",
                ich = "spreche",
                du = "sprichst",
                erSieEs = "spricht",
                wir = "sprechen",
                ihr = "sprecht",
                sieSie = "sprechen"
            ),
            VerbTenseConjugation(
                tenseKey = "praeteritum",
                tenseNameDe = "Präteritum (Vergangenheit)",
                ich = "sprach",
                du = "sprachst",
                erSieEs = "sprach",
                wir = "sprachen",
                ihr = "spracht",
                sieSie = "sprachen"
            ),
            VerbTenseConjugation(
                tenseKey = "perfekt",
                tenseNameDe = "Perfekt (Vollendete Gegenwart)",
                ich = "habe gesprochen",
                du = "hast gesprochen",
                erSieEs = "hat gesprochen",
                wir = "haben gesprochen",
                ihr = "habt gesprochen",
                sieSie = "haben gesprochen"
            )
        ),
        examples = listOf(
            WordExample("sprechen", "Sie spricht fließend Deutsch und Englisch."),
            WordExample("sprechen", "Ich habe gestern lange mit meinem Chef gesprochen.")
        )
    )

    private fun createSehen() = VerbConjugationResult(
        word = "sehen",
        infinitiv = "sehen",
        hilfsverb = "haben",
        auxiliary = "(hat)",
        praeteritum = "sah",
        partizip1 = "sehend",
        partizip2 = "gesehen",
        verbType = "stark (e -> ie)",
        separable = "untrennbar",
        caseObject = "Akkusativ",
        meaningEnglish = "to see / يرى",
        imperativ = VerbImperativ(
            du = "sieh(e)!",
            ihr = "seht!",
            sie = "sehen Sie!"
        ),
        tenses = listOf(
            VerbTenseConjugation(
                tenseKey = "praesens",
                tenseNameDe = "Präsens (Gegenwart)",
                ich = "sehe",
                du = "siehst",
                erSieEs = "sieht",
                wir = "sehen",
                ihr = "seht",
                sieSie = "sehen"
            ),
            VerbTenseConjugation(
                tenseKey = "perfekt",
                tenseNameDe = "Perfekt (Vollendete Gegenwart)",
                ich = "habe gesehen",
                du = "hast gesehen",
                erSieEs = "hat gesehen",
                wir = "haben gesehen",
                ihr = "habt gesehen",
                sieSie = "haben gesehen"
            )
        ),
        examples = listOf(
            WordExample("sehen", "Ich sehe den Vogel im Garten."),
            WordExample("sehen", "Hast du diesen Film schon gesehen?")
        )
    )

    private fun createEssen() = VerbConjugationResult(
        word = "essen",
        infinitiv = "essen",
        hilfsverb = "haben",
        auxiliary = "(hat)",
        praeteritum = "aß",
        partizip1 = "essend",
        partizip2 = "gegessen",
        verbType = "stark (e -> i)",
        separable = "untrennbar",
        caseObject = "Akkusativ",
        meaningEnglish = "to eat / يأكل",
        imperativ = VerbImperativ(
            du = "iss!",
            ihr = "esst!",
            sie = "essen Sie!"
        ),
        tenses = listOf(
            VerbTenseConjugation(
                tenseKey = "praesens",
                tenseNameDe = "Präsens (Gegenwart)",
                ich = "esse",
                du = "isst",
                erSieEs = "isst",
                wir = "essen",
                ihr = "esst",
                sieSie = "essen"
            ),
            VerbTenseConjugation(
                tenseKey = "perfekt",
                tenseNameDe = "Perfekt (Vollendete Gegenwart)",
                ich = "habe gegessen",
                du = "hast gegessen",
                erSieEs = "hat gegessen",
                wir = "haben gegessen",
                ihr = "habt gegessen",
                sieSie = "haben gegessen"
            )
        ),
        examples = listOf(
            WordExample("essen", "Wir essen heute Abend Pizza zusammen."),
            WordExample("essen", "Was hast du zum Mittagessen gegessen?")
        )
    )

    private fun createHaben() = VerbConjugationResult(
        word = "haben",
        infinitiv = "haben",
        hilfsverb = "haben",
        auxiliary = "(hat)",
        praeteritum = "hatte",
        partizip1 = "habend",
        partizip2 = "gehabt",
        verbType = "unregelmäßig",
        separable = "untrennbar",
        caseObject = "Akkusativ",
        meaningEnglish = "to have / يملك، عنده",
        imperativ = VerbImperativ(
            du = "hab(e)!",
            ihr = "habt!",
            sie = "haben Sie!"
        ),
        tenses = listOf(
            VerbTenseConjugation(
                tenseKey = "praesens",
                tenseNameDe = "Präsens (Gegenwart)",
                ich = "habe",
                du = "hast",
                erSieEs = "hat",
                wir = "haben",
                ihr = "habt",
                sieSie = "haben"
            ),
            VerbTenseConjugation(
                tenseKey = "praeteritum",
                tenseNameDe = "Präteritum (Vergangenheit)",
                ich = "hatte",
                du = "hattest",
                erSieEs = "hatte",
                wir = "hatten",
                ihr = "hattet",
                sieSie = "hatten"
            ),
            VerbTenseConjugation(
                tenseKey = "perfekt",
                tenseNameDe = "Perfekt (Vollendete Gegenwart)",
                ich = "habe gehabt",
                du = "hast gehabt",
                erSieEs = "hat gehabt",
                wir = "haben gehabt",
                ihr = "habt gehabt",
                sieSie = "haben gehabt"
            )
        ),
        examples = listOf(
            WordExample("haben", "Ich habe heute viel Zeit zum Lernen."),
            WordExample("haben", "Wir haben viel Glück gehabt.")
        )
    )

    private fun createSein() = VerbConjugationResult(
        word = "sein",
        infinitiv = "sein",
        hilfsverb = "sein",
        auxiliary = "(ist)",
        praeteritum = "war",
        partizip1 = "seiend",
        partizip2 = "gewesen",
        verbType = "unregelmäßig",
        separable = "untrennbar",
        caseObject = "Nominativ (Kopulaverb)",
        meaningEnglish = "to be / يكون",
        imperativ = VerbImperativ(
            du = "sei!",
            ihr = "seid!",
            sie = "seien Sie!"
        ),
        tenses = listOf(
            VerbTenseConjugation(
                tenseKey = "praesens",
                tenseNameDe = "Präsens (Gegenwart)",
                ich = "bin",
                du = "bist",
                erSieEs = "ist",
                wir = "sind",
                ihr = "seid",
                sieSie = "sind"
            ),
            VerbTenseConjugation(
                tenseKey = "praeteritum",
                tenseNameDe = "Präteritum (Vergangenheit)",
                ich = "war",
                du = "warst",
                erSieEs = "war",
                wir = "waren",
                ihr = "wart",
                sieSie = "waren"
            ),
            VerbTenseConjugation(
                tenseKey = "perfekt",
                tenseNameDe = "Perfekt (Vollendete Gegenwart)",
                ich = "bin gewesen",
                du = "bist gewesen",
                erSieEs = "ist gewesen",
                wir = "sind gewesen",
                ihr = "seid gewesen",
                sieSie = "sind gewesen"
            )
        ),
        examples = listOf(
            WordExample("sein", "Das Wetter ist heute wunderschön."),
            WordExample("sein", "Ich bin gestern den ganzen Tag zu Hause gewesen.")
        )
    )
}
