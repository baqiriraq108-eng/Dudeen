package com.example.util

import java.util.Locale

/**
 * German IPA (International Phonetic Alphabet) Transcriber.
 * Produces standard dictionary-style IPA transcriptions (as in Duden / Langenscheidt / PONS)
 * with stress mark (ˈ) and vowel length (ː), enclosed in square brackets [ ].
 */
object GermanIpaTranscriber {

    // Common standard German word dictionary for exact dictionary-grade transcriptions
    private val exactDictionary: Map<String, String> = mapOf(
        // Common nouns & examples from prompt
        "kärtchen" to "ˈkɛʁtçən",
        "kartchen" to "ˈkɛʁtçən",
        "das kärtchen" to "das ˈkɛʁtçən",
        "die kärtchen" to "diː ˈkɛʁtçən",
        "des kärtchens" to "dɛs ˈkɛʁtçəns",
        "tisch" to "tɪʃ",
        "der tisch" to "deːɐ̯ tɪʃ",
        "die tische" to "diː ˈtɪʃə",
        "des tisches" to "dɛs ˈtɪʃəs",
        "mann" to "man",
        "der mann" to "deːɐ̯ man",
        "frau" to "fʁaʊ̯",
        "die frau" to "diː fʁaʊ̯",
        "kind" to "kɪnt",
        "das kind" to "das kɪnt",
        "haus" to "haʊ̯s",
        "das haus" to "das haʊ̯s",
        "wohnung" to "ˈvoːnʊŋ",
        "die wohnung" to "diː ˈvoːnʊŋ",
        "auto" to "ˈaʊ̯to",
        "das auto" to "das ˈaʊ̯to",
        "buch" to "buːx",
        "das buch" to "das buːx",
        "apfel" to "ˈapfl̩",
        "der apfel" to "deːɐ̯ ˈapfl̩",
        "mutter" to "ˈmʊtɐ",
        "vater" to "ˈfaːtɐ",
        "bruder" to "ˈbʁuːdɐ",
        "schwester" to "ˈʃvɛstɐ",
        "mädchen" to "ˈmɛːtçən",
        "junge" to "ˈjʊŋə",
        "tag" to "taːk",
        "guten tag" to "ˈɡuːtn̩ taːk",
        "guten morgen" to "ˈɡuːtn̩ ˈmɔʁɡn̩",
        "guten abend" to "ˈɡuːtn̩ ˈaːbn̩t",
        "gute nacht" to "ˈɡuːtə naxt",
        "danke" to "ˈdaŋkə",
        "bitte" to "ˈbɪtə",
        "auf wiedersehen" to "aʊ̯f ˈviːdɐˌzeːən",
        "hallo" to "haˈloː",
        "tschüss" to "tʃʏs",
        "ja" to "jaː",
        "nein" to "naɪ̯n",
        "wetter" to "ˈvɛtɐ",
        "sonne" to "ˈzɔnə",
        "mond" to "moːnt",
        "stern" to "ʃtɛʁn",
        "wasser" to "ˈvasɐ",
        "brot" to "bʁoːt",
        "kaffee" to "ˈkafe",
        "tee" to "teː",
        "schule" to "ˈʃuːlə",
        "lehrer" to "ˈleːʁɐ",
        "schüler" to "ˈʃyːlɐ",
        "freund" to "fʁɔɪ̯nt",
        "zeit" to "tsaɪ̯t",
        "stadt" to "ʃtat",
        "land" to "lant",
        "straße" to "ˈʃtʁaːsə",
        "zug" to "tsuːk",
        "bahn" to "baːn",
        "geld" to "ɡɛlt",
        "arbeit" to "ˈaʁbaɪ̯t",
        "liebe" to "ˈliːbə",
        "leben" to "ˈleːbn̩",
        "welt" to "vɛlt",
        "herz" to "hɛʁts",

        // Common verbs
        "arbeiten" to "ˈaʁbaɪ̯tn̩",
        "arbeitete" to "ˈaʁbaɪ̯tətə",
        "gearbeitet" to "ɡəˈʔaʁbaɪ̯tət",
        "arbeitend" to "ˈaʁbaɪ̯tn̩t",
        "gehen" to "ˈɡeːən",
        "ging" to "ɡɪŋ",
        "gegangen" to "ɡəˈɡaŋən",
        "gehend" to "ˈɡeːənt",
        "sehen" to "ˈzeːən",
        "sah" to "zaː",
        "gesehen" to "ɡəˈzeːən",
        "sehend" to "ˈzeːənt",
        "sprechen" to "ˈʃpʁɛçn̩",
        "sprach" to "ʃpʁaːx",
        "gesprochen" to "ɡəˈʃpʁɔxn̩",
        "sprechend" to "ˈʃpʁɛçn̩t",
        "fahren" to "ˈfaːʁən",
        "fuhr" to "fuːɐ̯",
        "gefahren" to "ɡəˈfaːʁən",
        "fahrend" to "ˈfaːʁənt",
        "kommen" to "ˈkɔmən",
        "kam" to "kaːm",
        "gekommen" to "ɡəˈkɔmən",
        "kommend" to "ˈkɔmənt",
        "lernen" to "ˈlɛʁnən",
        "lernte" to "ˈlɛʁntə",
        "gelernt" to "ɡəˈlɛʁnt",
        "lernend" to "ˈlɛʁnənt",
        "essen" to "ˈʔɛsn̩",
        "aß" to "ʔaːs",
        "gegessen" to "ɡəˈɡɛsn̩",
        "essend" to "ˈʔɛsn̩t",
        "trinken" to "ˈtʁɪŋkn̩",
        "trank" to "tʁaŋk",
        "getrunken" to "ɡəˈtʁʊŋkn̩",
        "trinkend" to "ˈtʁɪŋkn̩t",
        "haben" to "ˈhaːbn̩",
        "hatte" to "ˈhatə",
        "gehabt" to "ɡəˈhaːpt",
        "habend" to "ˈhaːbnt",
        "sein" to "zaɪ̯n",
        "war" to "vaːɐ̯",
        "gewesen" to "ɡəˈveːzn̩",
        "seiend" to "ˈzaɪ̯ənt",
        "machen" to "ˈmaxn̩",
        "machte" to "ˈmaxtə",
        "gemacht" to "ɡəˈmaxt",
        "machend" to "ˈmaxn̩t",
        "schreiben" to "ˈʃʁaɪ̯bn̩",
        "schrieb" to "ʃʁiːp",
        "geschrieben" to "ɡəˈʃʁiːbn̩",
        "schreibend" to "ˈʃʁaɪ̯bnt",
        "lesen" to "ˈleːzn̩",
        "las" to "laːs",
        "gelesen" to "ɡəˈleːzn̩",
        "lesend" to "ˈleːznt",
        "hören" to "ˈhøːʁən",
        "hörte" to "ˈhøːɐ̯tə",
        "gehört" to "ɡəˈhøːɐ̯t",
        "hörend" to "ˈhøːʁənt",
        "sagen" to "ˈzaːɡn̩",
        "sagte" to "ˈzaːktə",
        "gesagt" to "ɡəˈzaːkt",
        "sagend" to "ˈzaːɡn̩t",
        "wissen" to "ˈvɪsn̩",
        "wusste" to "ˈvʊstə",
        "gewusst" to "ɡəˈvʊst",
        "können" to "ˈkœnən",
        "konnte" to "ˈkɔntə",
        "gekonnt" to "ɡəˈkɔnt",
        "müssen" to "ˈmʏsn̩",
        "musste" to "ˈmʊstə",
        "gemusst" to "ɡəˈmʊst",
        "wollen" to "ˈvɔlən",
        "wollte" to "ˈvɔltə",
        "gewollt" to "ɡəˈvɔlt",
        "sollen" to "ˈzɔlən",
        "sollte" to "ˈzɔltə",
        "gesollt" to "ɡəˈzɔlt",
        "dürfen" to "ˈdʏʁfn̩",
        "durfte" to "ˈdʊʁftə",
        "gedurft" to "ɡəˈdʊʁft",
        "mögen" to "ˈmøːɡn̩",
        "mochte" to "ˈmɔxtə",
        "gemocht" to "ɡəˈmɔxt",

        // Common adjectives
        "schön" to "ʃøːn",
        "schöner" to "ˈʃøːnɐ",
        "am schönsten" to "am ˈʃøːnstn̩",
        "schnell" to "ʃnɛl",
        "schneller" to "ˈʃnɛlɐ",
        "am schnellsten" to "am ˈʃnɛlstn̩",
        "gut" to "ɡuːt",
        "besser" to "ˈbɛsɐ",
        "am besten" to "am ˈbɛstn̩",
        "groß" to "ɡʁoːs",
        "größer" to "ˈɡʁøːsɐ",
        "am größten" to "am ˈɡʁøːstn̩",
        "klein" to "klaɪ̯n",
        "kleiner" to "ˈklaɪ̯nɐ",
        "am kleinsten" to "am ˈklaɪ̯nstn̩",
        "alt" to "ʔalt",
        "älter" to "ˈʔɛltɐ",
        "am ältesten" to "am ˈʔɛltəstn̩",
        "jung" to "jʊŋ",
        "jünger" to "ˈjʏŋɐ",
        "am jüngsten" to "am ˈjʏŋstn̩",
        "neu" to "nɔɪ̯",
        "neuer" to "ˈnɔɪ̯ɐ",
        "am neuesten" to "am ˈnɔɪ̯əstn̩",
        "hoch" to "hoːx",
        "höher" to "ˈhøːɐ",
        "am höchsten" to "am ˈhøːçstn̩",
        "lang" to "laŋ",
        "länger" to "ˈlɛŋɐ",
        "am längsten" to "am ˈlɛŋstn̩",
        "kurz" to "kʊʁts",
        "kürzer" to "ˈkʏʁtsɐ",
        "am kürzesten" to "am ˈkʏʁtsəstn̩",
        "schwer" to "ʃveːɐ̯",
        "schwerer" to "ˈʃveːʁɐ",
        "am schwersten" to "am ˈʃveːɐ̯stn̩",
        "leicht" to "laɪ̯çt",
        "leichter" to "ˈlaɪ̯çtɐ",
        "am leichtesten" to "am ˈlaɪ̯çtəstn̩",
        "teuer" to "ˈtɔɪ̯ɐ",
        "billig" to "ˈbɪlɪç",
        "warm" to "vaʁm",
        "kalt" to "kalt",
        "frisch" to "fʁɪʃ",
        "stark" to "ʃtaʁk",
        "schwach" to "ʃvax",
        "hell" to "hɛl",
        "dunkel" to "ˈdʊŋkl̩",
        "wichtig" to "ˈvɪçtɪç",
        "einfach" to "ˈʔaɪ̯nfax",
        "richtig" to "ˈʁɪçtɪç",
        "falsch" to "falʃ",
        "klar" to "klaːɐ̯",

        // Pronouns, prepositions, particles, sentence words
        "ich" to "ʔɪç",
        "du" to "duː",
        "er" to "ʔeːɐ̯",
        "sie" to "ziː",
        "es" to "ʔɛs",
        "wir" to "viːɐ̯",
        "ihr" to "ʔiːɐ̯",
        "mir" to "miːɐ̯",
        "dir" to "diːɐ̯",
        "ihm" to "ʔiːm",
        "ihr" to "ʔiːɐ̯",
        "uns" to "ʔʊns",
        "euch" to "ʔɔɪ̯ç",
        "ihnen" to "ˈʔiːnən",
        "mich" to "mɪç",
        "dich" to "dɪç",
        "ihn" to "ʔiːn",
        "mein" to "maɪ̯n",
        "meine" to "ˈmaɪ̯nə",
        "dein" to "daɪ̯n",
        "deine" to "ˈdaɪ̯nə",
        "sein" to "zaɪ̯n",
        "seine" to "ˈzaɪ̯nə",
        "unser" to "ˈʔʊnzɐ",
        "euer" to "ˈʔɔɪ̯ɐ",
        "der" to "deːɐ̯",
        "die" to "diː",
        "das" to "das",
        "den" to "deːn",
        "dem" to "deːm",
        "des" to "dɛs",
        "ein" to "ʔaɪ̯n",
        "eine" to "ˈʔaɪ̯nə",
        "einen" to "ˈʔaɪ̯nən",
        "einem" to "ˈʔaɪ̯nəm",
        "eines" to "ˈʔaɪ̯nəs",
        "einer" to "ˈʔaɪ̯nɐ",
        "kein" to "kaɪ̯n",
        "keine" to "ˈkaɪ̯nə",
        "wie" to "viː",
        "geht" to "ɡeːt",
        "ist" to "ʔɪst",
        "sind" to "zɪnt",
        "bist" to "bɪst",
        "was" to "vas",
        "wer" to "veːɐ̯",
        "wo" to "voː",
        "woher" to "voˈheːɐ̯",
        "wohin" to "voˈhɪn",
        "wann" to "van",
        "warum" to "vaˈʁʊm",
        "in" to "ʔɪn",
        "an" to "ʔan",
        "auf" to "ʔaʊ̯f",
        "aus" to "ʔaʊ̯s",
        "bei" to "baɪ̯",
        "mit" to "mɪt",
        "nach" to "naːx",
        "von" to "fɔn",
        "zu" to "tsuː",
        "für" to "fyːɐ̯",
        "durch" to "dʊʁç",
        "gegen" to "ˈɡeːɡn̩",
        "ohne" to "ˈʔoːnə",
        "um" to "ʔʊm",
        "über" to "ˈʔyːbɐ",
        "unter" to "ˈʔʊntɐ",
        "vor" to "foːɐ̯",
        "hinter" to "ˈhɪntɐ",
        "neben" to "ˈneːbn̩",
        "zwischen" to "ˈtsvɪʃn̩",
        "sehr" to "zeːɐ̯",
        "viel" to "fiːl",
        "viele" to "ˈfiːlə",
        "nicht" to "nɪçt",
        "auch" to "ʔaʊ̯x",
        "nur" to "nuːɐ̯",
        "schon" to "ʃoːn",
        "noch" to "nɔx",
        "jetzt" to "jɛtst",
        "hier" to "hiːɐ̯",
        "da" to "daː",
        "dort" to "dɔʁt",
        "heute" to "ˈhɔɪ̯tə",
        "morgen" to "ˈmɔʁɡn̩",
        "gestern" to "ˈɡɛstɐn",
        "immer" to "ˈʔɪmɐ",
        "oft" to "ʔɔft",
        "manchmal" to "ˈmançmaːl",
        "nie" to "niː",
        "nämlich" to "ˈnɛːmlɪç",
        "kochen" to "ˈkɔxn̩",
        "hobby" to "ˈhɔbi"
    )

    /**
     * Cleans and validates an IPA string.
     * Ensures standard format enclosed in brackets `[ ... ]`.
     */
    fun formatIpa(rawIpa: String?, fallbackText: String = ""): String {
        val trimmed = rawIpa?.trim().orEmpty()
        if (trimmed.isNotBlank() && !trimmed.equals("null", ignoreCase = true) && !trimmed.equals("–", ignoreCase = true) && !trimmed.equals("-", ignoreCase = true)) {
            val content = trimmed.removePrefix("[").removeSuffix("]").trim()
            if (content.isNotBlank() && content.any { it !in "a-zA-Z0-9., " || it.isLetter() }) {
                return "[$content]"
            }
        }
        val generated = transcribe(fallbackText)
        return generated
    }

    /**
     * Transcribes German input (single word, phrase, sentence, or list of words)
     * into authoritative IPA format in square brackets `[ ... ]`.
     */
    fun transcribe(input: String?): String {
        if (input.isNullOrBlank()) return ""
        val cleanInput = input.trim().removePrefix("•").removePrefix("-").trim()
        if (cleanInput.isBlank()) return ""

        // Check exact match for full phrase
        val lowerAll = cleanInput.lowercase(Locale.ROOT)
        exactDictionary[lowerAll]?.let {
            return "[$it]"
        }

        // Split by words/punctuation
        // If it's a comma-separated list of words (e.g. "Haus, Wohnung, Auto")
        if (cleanInput.contains(",")) {
            val items = cleanInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
            if (items.size > 1) {
                val ipaItems = items.map { transcribeSinglePhrase(it) }
                return ipaItems.joinToString(", ")
            }
        }

        return transcribeSinglePhrase(cleanInput)
    }

    private fun transcribeSinglePhrase(phrase: String): String {
        val trimmed = phrase.trim()
        if (trimmed.isBlank()) return ""

        val lowerPhrase = trimmed.lowercase(Locale.ROOT)
        exactDictionary[lowerPhrase]?.let {
            return "[$it]"
        }

        // Split sentence or multi-word phrase into individual tokens while preserving word sequence
        val tokens = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }
        val transcribedWords = tokens.map { token ->
            val cleanToken = token.replace(Regex("[.,!?:;\"'»«()]+"), "").trim()
            if (cleanToken.isBlank()) ""
            else transcribeWord(cleanToken)
        }.filter { it.isNotBlank() }

        if (transcribedWords.isEmpty()) return ""
        val combined = transcribedWords.joinToString(" ")
        return "[$combined]"
    }

    /**
     * Transcribes a single German word using dictionary lookup and algorithmic phonetic rules.
     */
    private fun transcribeWord(word: String): String {
        val lower = word.lowercase(Locale.ROOT).trim()
        if (lower.isBlank()) return ""

        // Check dictionary
        exactDictionary[lower]?.let { return it }

        // Remove prefixes or handle compound words
        val prefixStress = findPrefix(lower)

        var text = lower
        // Phonetic rule transformations for Standard High German (Standarddeutsch / Duden)
        val sb = StringBuilder()

        var i = 0
        val n = text.length
        val isFirst = true

        while (i < n) {
            val c = text[i]
            val rem = text.substring(i)

            // 1. Trigraphs & Digraphs
            when {
                rem.startsWith("tsch") -> {
                    sb.append("tʃ")
                    i += 4
                }
                rem.startsWith("dsch") -> {
                    sb.append("dʒ")
                    i += 4
                }
                rem.startsWith("sch") -> {
                    sb.append("ʃ")
                    i += 3
                }
                rem.startsWith("ph") -> {
                    sb.append("f")
                    i += 2
                }
                rem.startsWith("th") -> {
                    sb.append("t")
                    i += 2
                }
                rem.startsWith("dt") -> {
                    sb.append("t")
                    i += 2
                }
                rem.startsWith("tz") -> {
                    sb.append("t͡s")
                    i += 2
                }
                rem.startsWith("ck") -> {
                    sb.append("k")
                    i += 2
                }
                rem.startsWith("qu") -> {
                    sb.append("kv")
                    i += 2
                }
                rem.startsWith("st") && (i == 0 || (i > 0 && text[i - 1] in " -_")) -> {
                    sb.append("ʃt")
                    i += 2
                }
                rem.startsWith("sp") && (i == 0 || (i > 0 && text[i - 1] in " -_")) -> {
                    sb.append("ʃp")
                    i += 2
                }
                rem.startsWith("ng") -> {
                    sb.append("ŋ")
                    i += 2
                }
                rem.startsWith("nk") -> {
                    sb.append("ŋk")
                    i += 2
                }
                // Diphthongs
                rem.startsWith("ei") || rem.startsWith("ai") || rem.startsWith("ey") || rem.startsWith("ay") -> {
                    sb.append("aɪ̯")
                    i += 2
                }
                rem.startsWith("eu") || rem.startsWith("äu") || rem.startsWith("aeu") -> {
                    sb.append("ɔɪ̯")
                    i += if (rem.startsWith("aeu")) 3 else 2
                }
                rem.startsWith("au") -> {
                    sb.append("aʊ̯")
                    i += 2
                }
                rem.startsWith("ie") -> {
                    sb.append("iː")
                    i += 2
                }
                // ch rules: [x] after a, o, u, au; [ç] after e, i, ä, ö, ü, l, r, n
                rem.startsWith("ch") -> {
                    val prev = if (i > 0) text[i - 1] else ' '
                    if (prev in "aou") {
                        sb.append("x")
                    } else {
                        sb.append("ç")
                    }
                    i += 2
                }
                // Word-ending -ig -> [ɪç]
                rem.startsWith("ig") && (i + 2 == n || (i + 2 < n && text[i + 2] in " .!?,;:")) -> {
                    sb.append("ɪç")
                    i += 2
                }
                // Word-ending -er -> [ɐ]
                rem.startsWith("er") && (i + 2 == n) -> {
                    sb.append("ɐ")
                    i += 2
                }
                // Word-ending -en -> [n̩] or [ən]
                rem.startsWith("en") && (i + 2 == n) -> {
                    sb.append("n̩")
                    i += 2
                }
                // Vowels with Dehnungs-h (e.g. ah, eh, ih, oh, uh, äh, öh, üh)
                rem.length >= 2 && rem[1] == 'h' && rem[0] in "aeiouäöü" -> {
                    when (rem[0]) {
                        'a' -> sb.append("aː")
                        'e' -> sb.append("eː")
                        'i' -> sb.append("iː")
                        'o' -> sb.append("oː")
                        'u' -> sb.append("uː")
                        'ä' -> sb.append("ɛː")
                        'ö' -> sb.append("øː")
                        'ü' -> sb.append("yː")
                    }
                    i += 2
                }
                // Double vowels (aa, ee, oo) -> long vowel
                rem.length >= 2 && rem[0] == rem[1] && rem[0] in "aeo" -> {
                    when (rem[0]) {
                        'a' -> sb.append("aː")
                        'e' -> sb.append("eː")
                        'o' -> sb.append("oː")
                    }
                    i += 2
                }
                // Individual characters
                c == 'z' -> {
                    sb.append("t͡s")
                    i += 1
                }
                c == 'v' -> {
                    // Usually [f] in native German (Vater, Vogel, viel, vier, vor), [v] in loanwords
                    if (lower.startsWith("vater") || lower.startsWith("vogel") || lower.startsWith("viel") || lower.startsWith("vier") || lower.startsWith("vor") || lower.startsWith("von") || lower.startsWith("voll")) {
                        sb.append("f")
                    } else {
                        sb.append("v")
                    }
                    i += 1
                }
                c == 'w' -> {
                    sb.append("v")
                    i += 1
                }
                c == 'j' -> {
                    sb.append("j")
                    i += 1
                }
                c == 's' -> {
                    // [z] before vowel, [s] otherwise
                    if (i + 1 < n && text[i + 1] in "aeiouäöü" && (i == 0 || text[i - 1] in " -_")) {
                        sb.append("z")
                    } else {
                        sb.append("s")
                    }
                    i += 1
                }
                c == 'ß' -> {
                    sb.append("s")
                    i += 1
                }
                c == 'r' -> {
                    // [ʁ] before vowel, [ɐ̯] after vowel
                    if (i + 1 < n && text[i + 1] in "aeiouäöü") {
                        sb.append("ʁ")
                    } else {
                        sb.append("ʁ")
                    }
                    i += 1
                }
                c == 'ä' -> {
                    val isShort = isNextDoubleConsonant(text, i + 1)
                    sb.append(if (isShort) "ɛ" else "ɛː")
                    i += 1
                }
                c == 'ö' -> {
                    val isShort = isNextDoubleConsonant(text, i + 1)
                    sb.append(if (isShort) "œ" else "øː")
                    i += 1
                }
                c == 'ü' -> {
                    val isShort = isNextDoubleConsonant(text, i + 1)
                    sb.append(if (isShort) "ʏ" else "yː")
                    i += 1
                }
                c == 'a' -> {
                    val isShort = isNextDoubleConsonant(text, i + 1)
                    sb.append(if (isShort) "a" else "aː")
                    i += 1
                }
                c == 'e' -> {
                    val isShort = isNextDoubleConsonant(text, i + 1)
                    if (i == n - 1 && n > 2) {
                        sb.append("ə")
                    } else {
                        sb.append(if (isShort) "ɛ" else "eː")
                    }
                    i += 1
                }
                c == 'i' -> {
                    val isShort = isNextDoubleConsonant(text, i + 1)
                    sb.append(if (isShort) "ɪ" else "iː")
                    i += 1
                }
                c == 'o' -> {
                    val isShort = isNextDoubleConsonant(text, i + 1)
                    sb.append(if (isShort) "ɔ" else "oː")
                    i += 1
                }
                c == 'u' -> {
                    val isShort = isNextDoubleConsonant(text, i + 1)
                    sb.append(if (isShort) "ʊ" else "uː")
                    i += 1
                }
                c == 'c' -> {
                    if (i + 1 < n && text[i + 1] in "eiyäö") {
                        sb.append("t͡s")
                    } else {
                        sb.append("k")
                    }
                    i += 1
                }
                c == 'x' -> {
                    sb.append("ks")
                    i += 1
                }
                // Auslautverhärtung for final consonants: b->p, d->t, g->k
                c == 'b' && (i == n - 1 || (i + 1 < n && text[i + 1] !in "aeiouäöüjlrw")) -> {
                    sb.append("p")
                    i += 1
                }
                c == 'd' && (i == n - 1 || (i + 1 < n && text[i + 1] !in "aeiouäöüjlrw")) -> {
                    sb.append("t")
                    i += 1
                }
                c == 'g' && (i == n - 1 || (i + 1 < n && text[i + 1] !in "aeiouäöüjlrw")) -> {
                    sb.append("k")
                    i += 1
                }
                else -> {
                    sb.append(c)
                    i += 1
                }
            }
        }

        var res = sb.toString()
        // Add primary stress mark ˈ before the word if multisyllabic or if root stem
        if (!res.startsWith("ˈ") && !res.startsWith("ˌ") && countVowels(lower) >= 1) {
            // If word has unstressed prefix (be-, ge-, er-, ver-, zer-, ent-, emp-), stress goes after prefix
            val unstressedPrefixes = listOf("bə", "ɡə", "ʔɛɐ̯", "fɛɐ̯", "tsɛɐ̯", "ʔɛnt", "ʔɛmp")
            var placed = false
            for (p in unstressedPrefixes) {
                if (res.startsWith(p) && res.length > p.length) {
                    res = p + "ˈ" + res.substring(p.length)
                    placed = true
                    break
                }
            }
            if (!placed && (countVowels(lower) >= 2 || lower.length >= 4)) {
                res = "ˈ$res"
            }
        }

        return res
    }

    private fun isNextDoubleConsonant(text: String, startIdx: Int): Boolean {
        if (startIdx >= text.length) return false
        if (startIdx + 1 < text.length && text[startIdx] == text[startIdx + 1] && text[startIdx] !in "aeiouäöü") return true
        if (startIdx + 1 < text.length && (text.substring(startIdx).startsWith("ck") || text.substring(startIdx).startsWith("tz") || text.substring(startIdx).startsWith("sch"))) return true
        // If followed by 2 or more consonants
        var consCount = 0
        for (k in startIdx until text.length) {
            if (text[k] in "aeiouäöü") break
            consCount++
        }
        return consCount >= 2
    }

    private fun countVowels(text: String): Int {
        val vowels = "aeiouäöü"
        return text.count { it in vowels }
    }

    private fun findPrefix(text: String): String? {
        val unstressed = listOf("be", "ge", "er", "ver", "zer", "ent", "emp")
        return unstressed.firstOrNull { text.startsWith(it) && text.length > it.length + 2 }
    }
}
