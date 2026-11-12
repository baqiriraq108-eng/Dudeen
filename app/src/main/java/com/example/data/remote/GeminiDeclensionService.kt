package com.example.data.remote

import android.content.Context
import com.example.data.ModelProvider
import com.example.data.model.WordDeclensionResult
import com.example.util.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class GeminiDeclensionService(private val context: Context? = null) {

    // Fast per-attempt timeouts so overloaded endpoints fail over quickly instead of hanging for 60s
    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(12, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    companion object {
        const val DEFAULT_GEMINI_MODEL = "gemini-2.5-flash"
        const val DEFAULT_OPENROUTER_MODEL = "openrouter/auto"

        private const val MODEL_COOLDOWN_MS = 60_000L
        private val modelCooldownUntilMs = ConcurrentHashMap<String, Long>()

        /**
         * Records a temporary cooldown for a model that returned 503 (overloaded), 429 (quota/rate limit),
         * 404 (unavailable), or timed out, so subsequent requests immediately route to a healthy model.
         */
        fun markModelUnhealthy(modelId: String, durationMs: Long = MODEL_COOLDOWN_MS) {
            modelCooldownUntilMs[modelId.lowercase()] = System.currentTimeMillis() + durationMs
        }

        fun isModelHealthy(modelId: String): Boolean {
            val until = modelCooldownUntilMs[modelId.lowercase()] ?: return true
            if (System.currentTimeMillis() >= until) {
                modelCooldownUntilMs.remove(modelId.lowercase())
                return true
            }
            return false
        }

        /**
         * Maps outdated, deprecated, non-existent, or paid-only model names to verified active Free-Tier models.
         */
        fun sanitizeModelId(provider: ModelProvider, rawModelId: String): String {
            val trimmed = rawModelId.trim().removePrefix("models/")
            if (provider == ModelProvider.GEMINI) {
                val mapped = when {
                    trimmed.isBlank() -> DEFAULT_GEMINI_MODEL
                    trimmed.equals("gemini-3.5-flash", ignoreCase = true) -> "gemini-2.5-flash"
                    trimmed.equals("gemini-3.8-flash", ignoreCase = true) -> "gemini-2.5-flash"
                    trimmed.equals("gemini-3.1-pro-preview", ignoreCase = true) -> "gemini-2.5-pro"
                    trimmed.equals("gemini-3-pro-preview", ignoreCase = true) -> "gemini-2.5-pro"
                    trimmed.equals("gemini-1.5-flash", ignoreCase = true) -> "gemini-2.5-flash"
                    trimmed.equals("gemini-1.5-flash-8b", ignoreCase = true) -> "gemini-2.5-flash-lite"
                    trimmed.equals("gemini-1.5-pro", ignoreCase = true) -> "gemini-2.5-pro"
                    trimmed.equals("gemini-2.0-flash-exp", ignoreCase = true) -> "gemini-2.0-flash"
                    trimmed.equals("gemini-2.0-flash-lite", ignoreCase = true) -> "gemini-2.5-flash-lite"
                    trimmed.equals("gemini-2.0-pro", ignoreCase = true) -> "gemini-2.5-pro"
                    trimmed.equals("gemini-pro", ignoreCase = true) -> "gemini-2.5-pro"
                    trimmed.equals("gemini-flash-latest", ignoreCase = true) -> "gemini-2.5-flash"
                    else -> trimmed
                }
                return if (com.example.data.ApiKeyManager.VERIFIED_FREE_GEMINI_MODEL_IDS.contains(mapped.lowercase())) {
                    mapped.lowercase()
                } else {
                    DEFAULT_GEMINI_MODEL
                }
            } else {
                return when {
                    trimmed.isBlank() -> DEFAULT_OPENROUTER_MODEL
                    trimmed.equals("google/gemini-2.0-flash-exp:free", ignoreCase = true) -> DEFAULT_OPENROUTER_MODEL
                    trimmed.equals("google/gemini-2.5-flash", ignoreCase = true) -> DEFAULT_OPENROUTER_MODEL
                    else -> trimmed
                }
            }
        }

        /**
         * Builds the ordered candidate model list starting with the user's selected model
         * followed by the active Free-Tier Flash/Lite failover chain.
         * Healthy models are prioritized first; cooled-down models are placed at the end as last resort.
         */
        fun buildFailoverChain(primaryModelId: String): List<String> {
            val sanitizedPrimary = sanitizeModelId(ModelProvider.GEMINI, primaryModelId)
            val allCandidates = LinkedHashSet<String>().apply {
                add(sanitizedPrimary)
                addAll(com.example.data.ApiKeyManager.FAST_GEMINI_FAILOVER_CHAIN)
            }.toList()

            val healthy = allCandidates.filter { isModelHealthy(it) }
            val cooledDown = allCandidates.filterNot { isModelHealthy(it) }
            return healthy + cooledDown
        }

        /**
         * Returns true if the model supports `thinkingConfig.thinkingBudget = 0` to eliminate
         * internal thinking latency for deterministic structured JSON grammar tables.
         */
        private fun supportsZeroThinkingBudget(modelId: String): Boolean {
            val lower = modelId.lowercase()
            return lower == "gemini-2.5-flash" ||
                lower == "gemini-2.5-flash-lite" ||
                lower == "gemini-3-flash-preview" ||
                lower == "gemini-3.1-flash-lite-preview"
        }
    }

    suspend fun fetchDeclension(
        word: String,
        apiKey: String,
        provider: ModelProvider = ModelProvider.GEMINI,
        selectedModelId: String = DEFAULT_GEMINI_MODEL,
        translationLang: String = "en"
    ): WordDeclensionResult = withContext(Dispatchers.IO) {
        val result = fetchGrammar(word, com.example.data.model.GrammarType.NOMEN, apiKey, provider, selectedModelId, translationLang)
        when (result) {
            is com.example.data.model.GrammarResult.Noun -> result.declension
            is com.example.data.model.GrammarResult.Sentence -> result.declension
            is com.example.data.model.GrammarResult.NomenList -> result.declension
            is com.example.data.model.GrammarResult.Verb -> throw IllegalStateException("Erwartetes Nomen, aber Verb erhalten.")
            is com.example.data.model.GrammarResult.Adjektiv -> throw IllegalStateException("Erwartetes Nomen, aber Adjektiv erhalten.")
        }
    }

    suspend fun fetchGrammar(
        word: String,
        type: com.example.data.model.GrammarType,
        apiKey: String,
        provider: ModelProvider = ModelProvider.GEMINI,
        selectedModelId: String = DEFAULT_GEMINI_MODEL,
        translationLang: String = "en"
    ): com.example.data.model.GrammarResult = withContext(Dispatchers.IO) {
        val trimmedWord = word.trim()
        if (trimmedWord.isEmpty()) {
            val typeLabel = if (type == com.example.data.model.GrammarType.NOMEN) "deutsches Nomen" else "deutsches Verb"
            throw IllegalArgumentException("Bitte geben Sie ein $typeLabel ein.")
        }

        if (apiKey.isBlank()) {
            throw IllegalStateException("Bitte konfigurieren Sie Ihren API-Schlüssel für ${provider.displayName} in den Einstellungen.")
        }

        // Offline / No Internet verification
        if (context != null && !NetworkUtils.isNetworkAvailable(context)) {
            throw IOException("Keine Internetverbindung. Die linguistische Analyse erfordert eine aktive Internetverbindung.")
        }

        val prompt = when (type) {
            com.example.data.model.GrammarType.NOMEN -> buildNounPrompt(trimmedWord, translationLang)
            com.example.data.model.GrammarType.VERB -> buildVerbPrompt(trimmedWord, translationLang)
            com.example.data.model.GrammarType.SENTENCE -> buildSentencePrompt(trimmedWord, translationLang)
            com.example.data.model.GrammarType.NOMEN_LIST -> buildNomenListPrompt(trimmedWord, translationLang)
            com.example.data.model.GrammarType.ADJEKTIV -> buildAdjektivPrompt(trimmedWord, translationLang)
        }

        val effectiveModelId = sanitizeModelId(provider, selectedModelId)

        val jsonContent = if (provider == ModelProvider.GEMINI) {
            fetchRawFromGemini(prompt, apiKey, effectiveModelId)
        } else {
            fetchRawFromOpenAiCompatible(prompt, apiKey, provider, effectiveModelId)
        }

        return@withContext com.example.data.model.GrammarResult.fromJson(
            jsonStr = jsonContent,
            fallbackType = type,
            fallbackWord = trimmedWord
        )
    }

    /**
     * Executes query via Google Gemini Generative Language API with:
     * 1. Zero-thinking fast JSON config (`thinkingBudget: 0`) for Flash/Lite models.
     * 2. Automatic instant failover cascade across verified Free-Tier Flash/Lite models
     *    when a model is overloaded (503), rate-limited (429), unavailable (404), or times out.
     */
    private fun fetchRawFromGemini(
        prompt: String,
        apiKey: String,
        selectedModelId: String
    ): String {
        val candidateModels = buildFailoverChain(selectedModelId)
        var lastException: IOException? = null

        for (targetModel in candidateModels) {
            try {
                return executeSingleGeminiCall(
                    prompt = prompt,
                    apiKey = apiKey,
                    targetModel = targetModel,
                    includeZeroThinking = supportsZeroThinkingBudget(targetModel)
                )
            } catch (e: GeminiInvalidKeyException) {
                // Do not fail over on an invalid API key; surface immediately
                throw IOException(e.message ?: "Invalid Gemini API Key. Please check your key at ai.google.dev.")
            } catch (e: GeminiRetryableException) {
                markModelUnhealthy(targetModel)
                lastException = IOException(e.message ?: "Modell $targetModel ausgelastet.")
                // Continue immediately to next healthy free-tier Flash/Lite model in cascade
            } catch (e: IOException) {
                // SocketTimeoutException / network glitch on this model endpoint
                markModelUnhealthy(targetModel, 30_000L)
                lastException = e
            }
        }

        throw lastException ?: IOException("Alle kostenlosen Gemini-Modelle sind derzeit ausgelastet. Bitte versuchen Sie es gleich erneut.")
    }

    private class GeminiInvalidKeyException(message: String) : IOException(message)
    private class GeminiRetryableException(val code: Int, message: String) : IOException(message)

    private fun executeSingleGeminiCall(
        prompt: String,
        apiKey: String,
        targetModel: String,
        includeZeroThinking: Boolean
    ): String {
        val requestJson = JSONObject().apply {
            val partsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("text", prompt)
                })
            }
            val contentsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", partsArray)
                })
            }
            put("contents", contentsArray)
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.1)
                if (includeZeroThinking) {
                    put("thinkingConfig", JSONObject().apply {
                        put("thinkingBudget", 0)
                    })
                }
            })
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$targetModel:generateContent?key=$apiKey"
        val request = Request.Builder()
            .url(url)
            .post(requestJson.toString().toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val code = response.code
                // If an endpoint rejects thinkingConfig with HTTP 400, retry once without thinkingConfig on the same model
                if (code == 400 && includeZeroThinking && responseBody.contains("thinking", ignoreCase = true)) {
                    return executeSingleGeminiCall(
                        prompt = prompt,
                        apiKey = apiKey,
                        targetModel = targetModel,
                        includeZeroThinking = false
                    )
                }

                val errorMsg = parseGeminiErrorMessage(code, responseBody)
                if (code == 401 || (code in listOf(400, 403) && errorMsg.contains("Invalid Gemini API Key", ignoreCase = true))) {
                    throw GeminiInvalidKeyException(errorMsg)
                }
                if (code in listOf(404, 408, 429, 500, 502, 503, 504)) {
                    throw GeminiRetryableException(code, errorMsg)
                }
                throw IOException(errorMsg)
            }

            return extractGeminiContentText(responseBody)
        }
    }

    /**
     * Executes query via OpenAI-compatible endpoints (Groq, Cerebras, OpenRouter, Mistral)
     */
    private fun fetchRawFromOpenAiCompatible(
        prompt: String,
        apiKey: String,
        provider: ModelProvider,
        selectedModelId: String
    ): String {
        val endpointUrl = "${provider.baseUrl}/chat/completions"
        val targetModel = sanitizeModelId(provider, selectedModelId)

        val requestJson = JSONObject().apply {
            put("model", targetModel)
            val messages = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", "You are an authoritative German linguistic dictionary. If the user's input word contains spelling errors, typos, or incorrect casing, silently correct it to the intended valid German word without asking or commenting. Return strictly raw valid JSON matching the exact specified schema. No markdown backticks, no explanations, no thinking text.")
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            }
            put("messages", messages)
            put("temperature", 0.1)
        }

        val requestBuilder = Request.Builder()
            .url(endpointUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")

        if (provider == ModelProvider.OPENROUTER) {
            requestBuilder.addHeader("HTTP-Referer", "https://deklination.app")
            requestBuilder.addHeader("X-Title", "Deklo")
        }

        val request = requestBuilder
            .post(requestJson.toString().toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val errorMsg = parseOpenAiErrorMessage(provider, response.code, responseBody)
                throw IOException(errorMsg)
            }

            return extractOpenAiContentText(responseBody)
        }
    }

    /**
     * Verifies API Key with a lightweight request
     */
    suspend fun testApiKey(
        provider: ModelProvider,
        apiKey: String,
        modelId: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isEmpty()) {
            return@withContext Result.failure(Exception("Bitte API-Schlüssel eingeben (Key required)"))
        }

        if (context != null && !NetworkUtils.isNetworkAvailable(context)) {
            return@withContext Result.failure(Exception("Keine Internetverbindung (Offline)"))
        }

        try {
            if (provider == ModelProvider.GEMINI) {
                val url = "https://generativelanguage.googleapis.com/v1beta/models?key=$trimmedKey"
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    val body = response.body?.string().orEmpty()
                    if (response.isSuccessful && body.isNotBlank()) {
                        Result.success("✓ Schlüssel gültig (Google Gemini)")
                    } else {
                        if (response.code in listOf(400, 401, 403) && (body.contains("API_KEY_INVALID", ignoreCase = true) || body.contains("API key not valid", ignoreCase = true))) {
                            Result.failure(Exception("Invalid Gemini API Key. Please check your key at ai.google.dev."))
                        } else {
                            Result.failure(Exception(parseGeminiErrorMessage(response.code, body)))
                        }
                    }
                }
            } else {
                // OpenRouter: First check the official /auth/key endpoint for key details and usage
                val authKeyUrl = "https://openrouter.ai/api/v1/auth/key"
                val authRequest = Request.Builder()
                    .url(authKeyUrl)
                    .addHeader("Authorization", "Bearer $trimmedKey")
                    .addHeader("HTTP-Referer", "https://deklination.app")
                    .addHeader("X-Title", "Deklo")
                    .get()
                    .build()

                client.newCall(authRequest).execute().use { response ->
                    val body = response.body?.string().orEmpty()
                    if (response.isSuccessful) {
                        val successMessage = try {
                            val root = JSONObject(body)
                            val data = root.optJSONObject("data")
                            val usage = data?.optDouble("usage", -1.0) ?: -1.0
                            val limit = if (data != null && !data.isNull("limit")) data.optDouble("limit", -1.0) else -1.0
                            val isFreeTier = data?.optBoolean("is_free_tier", false) ?: false

                            buildString {
                                append("✓ Schlüssel gültig (OpenRouter)")
                                if (usage >= 0.0) {
                                    append(" • $${String.format(java.util.Locale.US, "%.3f", usage)}")
                                }
                                if (limit > 0.0) {
                                    append(" / $${String.format(java.util.Locale.US, "%.2f", limit)}")
                                }
                                if (isFreeTier) {
                                    append(" (Free Tier)")
                                }
                            }
                        } catch (e: Exception) {
                            "✓ Schlüssel gültig (OpenRouter)"
                        }
                        Result.success(successMessage)
                    } else if (response.code == 401) {
                        Result.failure(Exception("Schlüssel ungültig (401 Unauthorized)"))
                    } else if (response.code == 403) {
                        Result.failure(Exception("Zugriff verweigert (403 Forbidden)"))
                    } else if (response.code == 429) {
                        Result.failure(Exception("Ratenlimit erreicht (429 Rate Limit)"))
                    } else {
                        // Fallback ping via chat/completions if auth/key is unavailable
                        testOpenRouterViaChat(trimmedKey, modelId)
                    }
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Verbindungstest fehlgeschlagen."))
        }
    }

    private fun testOpenRouterViaChat(apiKey: String, modelId: String): Result<String> {
        return try {
            val url = "https://openrouter.ai/api/v1/chat/completions"
            val targetModel = sanitizeModelId(ModelProvider.OPENROUTER, modelId)
            val pingJson = JSONObject().apply {
                put("model", targetModel)
                val messages = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", "ping")
                    })
                }
                put("messages", messages)
                put("max_tokens", 5)
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .addHeader("HTTP-Referer", "https://deklination.app")
                .addHeader("X-Title", "Deklo")
                .post(pingJson.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    Result.success("✓ Schlüssel gültig (OpenRouter)")
                } else {
                    Result.failure(Exception(parseOpenAiErrorMessage(ModelProvider.OPENROUTER, response.code, body)))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Verbindungstest fehlgeschlagen."))
        }
    }

    /**
     * Dynamically fetches the list of available models from Google Generative Language API
     */
    suspend fun fetchGeminiModelsFromApi(apiKey: String): Result<String> = withContext(Dispatchers.IO) {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isEmpty()) {
            return@withContext Result.failure(Exception("Invalid Gemini API Key. Please check your key at ai.google.dev."))
        }

        if (context != null && !NetworkUtils.isNetworkAvailable(context)) {
            return@withContext Result.failure(IOException("Keine Internetverbindung"))
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models?key=$trimmedKey"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (response.isSuccessful && body.isNotBlank()) {
                    Result.success(body)
                } else {
                    if (response.code in listOf(400, 401, 403) && (body.contains("API_KEY_INVALID", ignoreCase = true) || body.contains("API key not valid", ignoreCase = true))) {
                        Result.failure(Exception("Invalid Gemini API Key. Please check your key at ai.google.dev."))
                    } else {
                        val errorMsg = parseGeminiErrorMessage(response.code, body)
                        Result.failure(Exception(errorMsg))
                    }
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Dynamically fetches the list of available models from OpenRouter API
     */
    suspend fun fetchOpenRouterModelsFromApi(apiKey: String = ""): Result<String> = withContext(Dispatchers.IO) {
        if (context != null && !NetworkUtils.isNetworkAvailable(context)) {
            return@withContext Result.failure(IOException("Keine Internetverbindung"))
        }

        try {
            val url = "https://openrouter.ai/api/v1/models"
            val requestBuilder = Request.Builder()
                .url(url)
                .addHeader("HTTP-Referer", "https://deklination.app")
                .addHeader("X-Title", "Deklo")
                .get()

            val trimmedKey = apiKey.trim()
            if (trimmedKey.isNotEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $trimmedKey")
            }

            client.newCall(requestBuilder.build()).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (response.isSuccessful && body.isNotBlank()) {
                    Result.success(body)
                } else {
                    Result.failure(IOException("Modelle konnten nicht geladen werden (Code: ${response.code})"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildNomenListPrompt(words: String, translationLang: String = "en"): String {
        val isArabic = translationLang.equals("ar", ignoreCase = true)
        val transInstruction = if (isArabic) {
            "Translate each German word directly into ARABIC SCRIPT in \"meaningEnglish\" separated by slashes \"/\" or commas (e.g. \"بيت / شقة / سيارة\"). Do NOT output English words."
        } else {
            "Translate each German word directly to English in \"meaningEnglish\" separated by slashes \"/\" or commas (e.g. \"House / Apartment / Car\")."
        }
        val exampleMeaning = if (isArabic) "بيت / شقة / سيارة" else "House / Apartment / Car"

        return """
            You are an authoritative German linguistic dictionary and translator.
            Translate the following German word or list of German words: "$words".

            IMPORTANT INSTRUCTIONS:
            1. Treat the input strictly as word(s) or a list of words, NEVER as a sentence, regardless of whether the words are separated by commas (, or ،), semicolons (;), slashes (/), dashes (-), or spaces.
            2. SILENTLY correct any spelling or typo errors for each German word.
            3. Clean up the German words into a clean comma-separated list in "word" (e.g. "Haus, Wohnung, Auto").
            4. Provide standard International Phonetic Alphabet (IPA) pronunciation enclosed in square brackets [ ] with stress marks (ˈ) and length (ː) in "ipa" (e.g. "[haʊ̯s], [ˈvoːnʊŋ], [ˈaʊ̯to]").
            5. $transInstruction
            6. For EACH German word, generate a natural, clear German example sentence demonstrating its typical usage in the "examples" array.
            7. Do NOT provide any declension tables, sentences, or grammatical case inflections.
            8. Set "type" to "Wörter" and "gender" to "Wörter".
            9. Provide "–" for genderArticle, pluralNoun, and all singular/plural case forms.

            Return strictly valid JSON matching this schema:
            {
              "type": "Wörter",
              "word": "Haus, Wohnung, Auto",
              "ipa": "[haʊ̯s], [ˈvoːnʊŋ], [ˈaʊ̯to]",
              "gender": "Wörter",
              "genderArticle": "",
              "pluralNoun": "–",
              "meaningEnglish": "$exampleMeaning",
              "examples": [
                { "word": "Haus", "sentence": "Das schöne Haus hat einen großen Garten." },
                { "word": "Wohnung", "sentence": "Die moderne Wohnung liegt im Zentrum der Stadt." },
                { "word": "Auto", "sentence": "Das neue Auto fährt sehr leise und sparsam." }
              ],
              "singular": {
                "nominativ": { "definite": "–", "indefinite": "–", "noArticle": "–" },
                "akkusativ": { "definite": "–", "indefinite": "–", "noArticle": "–" },
                "dativ": { "definite": "–", "indefinite": "–", "noArticle": "–" },
                "genitiv": { "definite": "–", "indefinite": "–", "noArticle": "–" }
              },
              "plural": {
                "nominativ": { "definite": "–", "indefinite": "–", "noArticle": "–" },
                "akkusativ": { "definite": "–", "indefinite": "–", "noArticle": "–" },
                "dativ": { "definite": "–", "indefinite": "–", "noArticle": "–" },
                "genitiv": { "definite": "–", "indefinite": "–", "noArticle": "–" }
              }
            }
        """.trimIndent()
    }

    private fun buildAdjektivPrompt(word: String, translationLang: String = "en"): String {
        val isArabic = translationLang.equals("ar", ignoreCase = true)
        val transInstruction = if (isArabic) {
            "Translate the adjective directly into clear ARABIC SCRIPT in \"meaningEnglish\" (e.g. \"جميل / أنيق\" or \"سريع\"). Do NOT output English words."
        } else {
            "Translate the adjective directly into clear English in \"meaningEnglish\" (e.g. \"beautiful / pretty\" or \"fast / quick\")."
        }
        val exampleMeaning = if (isArabic) "جميل / أنيق" else "beautiful / pretty"

        return """
            You are an authoritative German linguistic dictionary.
            Analyze the German adjective: "$word".

            IMPORTANT INSTRUCTIONS:
            1. SILENTLY correct any typos, misspellings, accent errors, or casing issues in the German adjective (e.g. "schoen" -> "schön", "schnel" -> "schnell").
            2. Base form in "word" (standard lowercase German adjective spelling, e.g. "schön", "schnell", "gut", "groß").
            3. Standard IPA pronunciation enclosed in square brackets [ ] with stress marks and vowel lengths in "ipa" (e.g. "[ʃøːn]", "[ˈʃnɛl]").
            4. Comparative form in "komparativ" (e.g. "schöner", "schneller", "besser", "größer", "höher").
            5. Superlative form with "am" in "superlativ" (e.g. "am schönsten", "am schnellsten", "am besten", "am größten", "am höchsten").
            6. $transInstruction
            7. Provide 2-3 clear, natural German example sentences showing the adjective in use in the "examples" array.
            8. Set "type" to "Adjektiv" and "gender" to "Adjektiv".

            Return strictly valid JSON matching this schema:
            {
              "type": "Adjektiv",
              "word": "schön",
              "ipa": "[ʃøːn]",
              "gender": "Adjektiv",
              "komparativ": "schöner",
              "superlativ": "am schönsten",
              "meaningEnglish": "$exampleMeaning",
              "examples": [
                {
                  "word": "schön",
                  "sentence": "Das Wetter ist heute besonders schön."
                },
                {
                  "word": "schöner",
                  "sentence": "Dieses Bild ist noch schöner als das andere."
                }
              ]
            }
        """.trimIndent()
    }

    private fun buildSentencePrompt(sentence: String, translationLang: String = "en"): String {
        val isArabic = translationLang.equals("ar", ignoreCase = true)
        val transInstruction = if (isArabic) {
            "3. Natural, accurate Arabic translation in ARABIC SCRIPT in \"meaningEnglish\" (e.g. \"هوايتي هي الطبخ في الواقع\"). Do NOT output English words."
        } else {
            "3. Natural, accurate English translation in \"meaningEnglish\"."
        }
        val exampleMeaning = if (isArabic) "هوايتي هي الطبخ في الواقع" else "My hobby is cooking, you see."

        return """
            You are an authoritative German linguistic dictionary and translator.
            Analyze and translate the German sentence or expression: "$sentence".

            IMPORTANT SPELLING & TYPO RULE: If the input sentence contains typos, misspellings, accent/umlaut errors, punctuation issues, or incorrect casing (e.g. "gutn morgn" instead of "Guten Morgen", "wie get es dir" instead of "Wie geht es dir"), SILENTLY correct it to the correct intended German sentence and return the translation for the corrected sentence. Do not ask questions, do not add comments, and do not say "Did you mean?". Return the result as if the user had typed the sentence correctly from the beginning, without showing any indication that a correction occurred.

            Include:
            1. Corrected German sentence in "word" (capitalized correctly, natural German punctuation).
            2. Standard IPA pronunciation of the sentence enclosed in square brackets [ ] with stress marks and vowel lengths in "ipa" (e.g. "[ˈmaɪ̯n ˈhɔbi ɪst ˈnɛːmlɪç ˈkɔxn̩]" or "[viː ɡeːt ɛs diːɐ̯]").
            3. Type: "Satz".
            $transInstruction
            4. Generate 2 to 3 short, natural German example sentences related to the original sentence in the "examples" array. For each example, provide the key differing/highlighted word or topic in "word" and the complete German example sentence in "sentence".
               For example, if the input is "Mein Hobby ist nämlich Kochen.", examples could be:
               [
                 { "word": "Lesen", "sentence": "Mein Hobby ist Lesen." },
                 { "word": "Schwimmen", "sentence": "Mein Hobby ist Schwimmen." },
                 { "word": "Musik hören", "sentence": "Mein Hobby ist Musik hören." }
               ]
            5. For gender and pluralNoun, provide "–".
            6. For all grammatical cases (nominativ, akkusativ, dativ, genitiv, ablativ), provide "–".

            Return strictly valid JSON only matching this schema:
            {
              "type": "Satz",
              "word": "Mein Hobby ist nämlich Kochen.",
              "ipa": "[ˈmaɪ̯n ˈhɔbi ɪst ˈnɛːmlɪç ˈkɔxn̩]",
              "gender": "–",
              "genderArticle": "",
              "pluralNoun": "–",
              "meaningEnglish": "$exampleMeaning",
              "examples": [
                { "word": "Lesen", "sentence": "Mein Hobby ist Lesen." },
                { "word": "Schwimmen", "sentence": "Mein Hobby ist Schwimmen." },
                { "word": "Musik hören", "sentence": "Mein Hobby ist Musik hören." }
              ],
              "singular": {
                "nominativ": { "definite": "–", "indefinite": "–", "noArticle": "–" },
                "akkusativ": { "definite": "–", "indefinite": "–", "noArticle": "–" },
                "dativ": { "definite": "–", "indefinite": "–", "noArticle": "–" },
                "genitiv": { "definite": "–", "indefinite": "–", "noArticle": "–" },
                "ablativ": { "definite": "–", "indefinite": "–", "noArticle": "–" }
              },
              "plural": {
                "nominativ": { "definite": "–", "indefinite": "–", "noArticle": "–" },
                "akkusativ": { "definite": "–", "indefinite": "–", "noArticle": "–" },
                "dativ": { "definite": "–", "indefinite": "–", "noArticle": "–" },
                "genitiv": { "definite": "–", "indefinite": "–", "noArticle": "–" },
                "ablativ": { "definite": "–", "indefinite": "–", "noArticle": "–" }
              }
            }
        """.trimIndent()
    }

    private fun buildNounPrompt(word: String, translationLang: String = "en"): String {
        val isArabic = translationLang.equals("ar", ignoreCase = true)
        val transInstruction = if (isArabic) {
            "5. Clear Arabic translation / meaning in ARABIC SCRIPT in \"meaningEnglish\" (e.g. \"طاولة / مكتب\", \"كتاب\", \"سيارة\", \"امرأة\"). Do NOT output English words."
        } else {
            "5. Clear English translation / meaning (e.g. \"table / desk\", \"book\", \"car\", \"woman\")."
        }
        val exampleMeaning = if (isArabic) "طاولة / مكتب" else "table / desk"

        return """
            You are an authoritative German linguistic dictionary.
            Analyze the German noun: "$word".
            IMPORTANT SPELLING & TYPO RULE: If the input word contains typos, misspellings, accent errors, or incorrect casing (e.g. "tifcht" instead of "Tisch", "abfel" instead of "Apfel", "frau" instead of "Frau"), SILENTLY correct it to the correct intended German noun and return the full declension table for the corrected word. Do not ask questions, do not add comments, and do not say "Did you mean?". Return the JSON directly for the corrected word.
            Provide the complete declension table for SINGULAR and PLURAL across all 4 German grammatical cases: Nominativ, Akkusativ, Dativ, and Genitiv.
            Include:
            1. Base noun (capitalized correctly in German).
            2. Gender ("Maskulin", "Feminin", "Neutrum", or "Pluralwort").
            3. Base definite article ("der", "die", or "das").
            4. Plural noun form with article (e.g. "die Tische", "die Bücher", "die Frauen").
            5. Standard IPA (International Phonetic Alphabet) pronunciation enclosed in square brackets [ ] with stress marks (ˈ) and length (ː) in "ipa" (e.g. "[ˈkɛʁtçən]" or "[tɪʃ]").
            $transInstruction
            6. For Singular and Plural, provide the 4 cases (nominativ, akkusativ, dativ, genitiv) with:
               - definite: with definite article (der/die/das/des/dem/den etc. and inflected noun, e.g. "des Mannes", "dem Mann", "den Männern")
               - indefinite: with indefinite article (ein/eine/einen/einem/eines for singular; keine/keinen/keiner for plural)
               - noArticle: with zero article (e.g. "Mann", "Mannes", "Männer", "Männern")
            Ensure grammatical precision (weak masculine n-deklination if applicable like "des Studenten", genitive -(e)s, dativ plural -(e)n).

            Return strictly valid JSON only matching this schema:
            {
              "type": "Nomen",
              "word": "Tisch",
              "ipa": "[tɪʃ]",
              "gender": "Maskulin",
              "genderArticle": "der",
              "pluralNoun": "die Tische",
              "meaningEnglish": "$exampleMeaning",
              "singular": {
                "nominativ": { "definite": "der Tisch", "indefinite": "ein Tisch", "noArticle": "Tisch" },
                "akkusativ": { "definite": "den Tisch", "indefinite": "einen Tisch", "noArticle": "Tisch" },
                "dativ": { "definite": "dem Tisch", "indefinite": "einem Tisch", "noArticle": "Tisch" },
                "genitiv": { "definite": "des Tisches", "indefinite": "eines Tisches", "noArticle": "Tisches" }
              },
              "plural": {
                "nominativ": { "definite": "die Tische", "indefinite": "keine Tische", "noArticle": "Tische" },
                "akkusativ": { "definite": "die Tische", "indefinite": "keine Tische", "noArticle": "Tische" },
                "dativ": { "definite": "den Tischen", "indefinite": "keinen Tischen", "noArticle": "Tischen" },
                "genitiv": { "definite": "der Tische", "indefinite": "keiner Tische", "noArticle": "Tische" }
              }
            }
        """.trimIndent()
    }

    private fun buildVerbPrompt(word: String, translationLang: String = "en"): String {
        val isArabic = translationLang.equals("ar", ignoreCase = true)
        val transInstruction = if (isArabic) {
            "10. Clear Arabic translation / meaning in ARABIC SCRIPT in \"meaningEnglish\" and \"english\" (e.g. \"يعمل\", \"يذهب\", \"يشكّل، يعلّم، يبني\"). Do NOT output English words."
        } else {
            "10. Clear English translation / meaning (e.g. \"to work\", \"to go\", \"to form, to educate, to build\")."
        }
        val exampleMeaning = if (isArabic) "يعمل" else "to work"

        return """
            You are an authoritative German linguistic dictionary.
            Analyze the German verb: "$word".
            IMPORTANT SPELLING & TYPO RULE: If the input word contains typos, misspellings, accent errors, or incorrect casing (e.g. "arbeiteb" instead of "arbeiten", "gehn" instead of "gehen", "schlafn" instead of "schlafen"), SILENTLY correct it to the correct intended German verb and return the full conjugation table for the corrected verb. Do not ask questions, do not add comments, and do not say "Did you mean?". Return the JSON directly for the corrected word.
            Provide the complete conjugation across all 6 German tenses (Präsens, Präteritum, Perfekt, Plusquamperfekt, Futur I, Futur II) for the active indicative (Indikativ Aktiv), the Imperativ (du, ihr, Sie), and the fundamental forms (Infinitiv, Hilfsverb haben/sein, Partizip I, Partizip II).
            Include:
            1. Infinitiv (base verb in lowercase/correct German spelling, e.g. "arbeiten", "gehen", "sehen").
            2. Standard IPA (International Phonetic Alphabet) pronunciation enclosed in square brackets [ ] with stress marks (ˈ) and length (ː) in "ipa" (e.g. "[ˈaʁbaɪ̯tn̩]").
            3. Hilfsverb ("haben" or "sein").
            4. Auxiliary ("hat" or "ist").
            5. Präteritum 3rd person singular form (e.g. "arbeitete", "nahm", "ging").
            6. Partizip I (e.g. "arbeitend", "gehend").
            7. Partizip II (e.g. "gearbeitet", "gegangen").
            8. Verb Type ("stark", "schwach", or "gemischt").
            9. Separability ("trennbar" or "untrennbar", or empty string "" if not a compound prefix verb).
            10. Case object ("Akk.", "Dat.", "Gen.", "Akk. / Dat." or empty string "" if intransitive).
            $transInstruction
            11. Imperativ:
               - du (e.g. "arbeite", "geh")
               - ihr (e.g. "arbeitet", "geht")
               - sie (e.g. "arbeiten Sie", "gehen Sie")
            12. All 6 tenses with all 6 grammatical persons:
               - ich
               - du
               - erSieEs (er/sie/es)
               - wir
               - ihr
               - sieSie (sie/Sie)
            Tenses required:
            - praesens (Präsens)
            - praeteritum (Präteritum)
            - perfekt (Perfekt)
            - plusquamperfekt (Plusquamperfekt)
            - futur1 (Futur I)
            - futur2 (Futur II)

            Return strictly valid JSON only matching this exact schema:
            {
              "type": "Verb",
              "word": "arbeiten",
              "infinitiv": "arbeiten",
              "ipa": "[ˈaʁbaɪ̯tn̩]",
              "hilfsverb": "haben",
              "auxiliary": "hat",
              "praeteritum": "arbeitete",
              "partizip1": "arbeitend",
              "partizip2": "gearbeitet",
              "verb_type": "schwach",
              "separable": "untrennbar",
              "case_object": "Akk.",
              "meaningEnglish": "$exampleMeaning",
              "english": "$exampleMeaning",
              "imperativ": {
                "du": "arbeite",
                "ihr": "arbeitet",
                "sie": "arbeiten Sie"
              },
              "tenses": {
                "praesens": { "ich": "arbeite", "du": "arbeitest", "erSieEs": "arbeitet", "wir": "arbeiten", "ihr": "arbeitet", "sieSie": "arbeiten" },
                "praeteritum": { "ich": "arbeitete", "du": "arbeitetest", "erSieEs": "arbeitete", "wir": "arbeiteten", "ihr": "arbeitetet", "sieSie": "arbeiteten" },
                "perfekt": { "ich": "habe gearbeitet", "du": "hast gearbeitet", "erSieEs": "hat gearbeitet", "wir": "haben gearbeitet", "ihr": "habt gearbeitet", "sieSie": "haben gearbeitet" },
                "plusquamperfekt": { "ich": "hatte gearbeitet", "du": "hattest gearbeitet", "erSieEs": "hatte gearbeitet", "wir": "hatten gearbeitet", "ihr": "hattet gearbeitet", "sieSie": "hatten gearbeitet" },
                "futur1": { "ich": "werde arbeiten", "du": "wirst arbeiten", "erSieEs": "wird arbeiten", "wir": "werden arbeiten", "ihr": "werdet arbeiten", "sieSie": "werden arbeiten" },
                "futur2": { "ich": "werde gearbeitet haben", "du": "wirst gearbeitet haben", "erSieEs": "wird gearbeitet haben", "wir": "werden gearbeitet haben", "ihr": "werdet gearbeitet haben", "sieSie": "werden gearbeitet haben" }
              }
            }
        """.trimIndent()
    }

    private fun extractGeminiContentText(responseJson: String): String {
        val root = JSONObject(responseJson)
        val candidates = root.optJSONArray("candidates")
            ?: throw IOException("Keine Antwort von Gemini API erhalten.")
        if (candidates.length() == 0) {
            throw IOException("Keine Antwort von Gemini API erhalten.")
        }
        val firstCandidate = candidates.getJSONObject(0)
        val content = firstCandidate.optJSONObject("content")
            ?: throw IOException("Antwortinhalt fehlt.")
        val parts = content.optJSONArray("parts")
            ?: throw IOException("Antwortteile fehlen.")
        val firstPart = parts.getJSONObject(0)
        return cleanJsonText(firstPart.optString("text", ""))
    }

    private fun extractOpenAiContentText(responseJson: String): String {
        val root = JSONObject(responseJson)
        val choices = root.optJSONArray("choices")
            ?: throw IOException("Keine Antwortauswahl von ${root.optString("model", "Modell")} erhalten.")
        if (choices.length() == 0) {
            throw IOException("Leere Antwort vom Modellanbieter erhalten.")
        }
        val firstChoice = choices.getJSONObject(0)
        val message = firstChoice.optJSONObject("message")
            ?: throw IOException("Nachrichtenobjekt fehlt in der Antwort.")
        val content = message.optString("content", "")
            .ifBlank { message.optString("reasoning_content", "") }
            .ifBlank { message.optString("reasoning", "") }
            .ifBlank { firstChoice.optString("text", "") }
        return cleanJsonText(content)
    }

    private fun cleanJsonText(raw: String): String {
        var text = raw.trim()
        // Strip out reasoning / thinking tags from reasoning models
        text = text.replace(Regex("<think>[\\s\\S]*?</think>", RegexOption.IGNORE_CASE), "").trim()
        text = text.replace(Regex("```json\\s*", RegexOption.IGNORE_CASE), "")
        text = text.replace(Regex("```\\s*"), "")

        val firstBrace = text.indexOf('{')
        val lastBrace = text.lastIndexOf('}')
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            text = text.substring(firstBrace, lastBrace + 1)
        }
        return text.trim()
    }

    private fun parseGeminiErrorMessage(code: Int, responseBody: String): String {
        return try {
            val root = JSONObject(responseBody)
            val errorObj = root.optJSONObject("error")
            val message = errorObj?.optString("message", "")
            val status = errorObj?.optString("status", "")
            when {
                status.equals("API_KEY_INVALID", ignoreCase = true) ||
                code == 401 ||
                (code in listOf(400, 403) && (message?.contains("API key", ignoreCase = true) == true || message?.contains("API_KEY", ignoreCase = true) == true)) ->
                    "Invalid Gemini API Key. Please check your key at ai.google.dev."
                code == 404 ->
                    "Ausgewähltes Gemini-Modell wurde nicht gefunden. Bitte wählen Sie ein anderes Modell (z. B. gemini-2.5-flash)."
                code == 429 || status.equals("RESOURCE_EXHAUSTED", ignoreCase = true) ->
                    "Gemini API Kontingent erreicht (429). Bitte warten Sie einen Moment oder wählen Sie ein anderes Modell."
                !message.isNullOrBlank() ->
                    "Gemini Fehler: $message"
                else ->
                    "Verbindungsfehler zu Google Gemini (Code $code)"
            }
        } catch (e: Exception) {
            "Verbindungsfehler zu Google Gemini (Code $code)"
        }
    }

    private fun parseOpenAiErrorMessage(provider: ModelProvider, code: Int, responseBody: String): String {
        return try {
            val root = JSONObject(responseBody)
            val errorObj = root.optJSONObject("error")
            val message = errorObj?.optString("message", "")
            when {
                code == 401 ->
                    "Ungültiger API-Schlüssel für ${provider.displayName}. Bitte überprüfen Sie Ihren Schlüssel."
                code == 403 ->
                    "Zugriff verweigert für ${provider.displayName} (403). Überprüfen Sie Berechtigungen oder Kontoguthaben."
                code == 429 ->
                    "Ratenlimit für ${provider.displayName} erreicht (429). Bitte warten Sie kurz."
                !message.isNullOrBlank() ->
                    "${provider.displayName} Fehler: $message"
                else ->
                    "Fehler bei ${provider.displayName} (Code $code)"
            }
        } catch (e: Exception) {
            "Fehler bei ${provider.displayName} (Code $code)"
        }
    }
}
