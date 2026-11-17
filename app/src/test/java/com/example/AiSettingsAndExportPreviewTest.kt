package com.example

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.data.AiModelInfo
import com.example.data.ModelProvider
import com.example.ui.components.ApiKeyDialog
import com.example.ui.components.PrintExportDialog
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.MyApplicationTheme
import com.example.util.CustomColorManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AiSettingsAndExportPreviewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun exportDialog_rendersBodyAndHeaderTypographyPreviewsInDarkAndLightMode() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("custom_colors_preferences", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        CustomColorManager.setExportBodyFontColor(context, "#1E293B")
        CustomColorManager.setExportHeaderFontColor(context, "#2979FF")

        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = true) {
                PrintExportDialog(onDismiss = {})
            }
        }

        composeTestRule.waitForIdle()
        val bodyPreviewNode = composeTestRule.onNodeWithTag("body_typography_preview").performScrollTo().assertIsDisplayed()
        val headerPreviewNode = composeTestRule.onNodeWithTag("header_typography_preview").performScrollTo().assertIsDisplayed()
        assertEquals(2, composeTestRule.onAllNodesWithText("Aa Bb Ää العربية").fetchSemanticsNodes().size)

        // Verify white preview containers wrap their content rather than stretching full width of the card
        val colorSelectorWidth = composeTestRule.onNodeWithTag("body_color_selector").fetchSemanticsNode().size.width
        val bodyPreviewWidth = bodyPreviewNode.fetchSemanticsNode().size.width
        val headerPreviewWidth = headerPreviewNode.fetchSemanticsNode().size.width
        assertTrue("Body preview should wrap content width", bodyPreviewWidth < colorSelectorWidth)
        assertTrue("Header preview should wrap content width", headerPreviewWidth < colorSelectorWidth)

        // Verify Kopfzeile text is removed above the 4-tier table color strip
        composeTestRule.onNodeWithTag("export_table_color_strip", useUnmergedTree = true).assertExists()
        assertEquals(0, composeTestRule.onAllNodesWithText("Kopfzeile", useUnmergedTree = true).fetchSemanticsNodes().size)

        // Open Color Wheel and verify organized Harmonie-Vorschläge and Gespeicherte Farben (no "(0)")
        composeTestRule.onNodeWithTag("body_color_selector").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Harmonie-Vorschläge").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("saved_colors_section_button").performScrollTo().assertIsDisplayed()
        assertEquals(0, composeTestRule.onAllNodesWithText("(0)").fetchSemanticsNodes().size)
    }

    @Test
    fun apiKeyDialog_bottomSheetDropdowns_searchFilterAndActionsWork() {
        var provider by mutableStateOf(ModelProvider.GEMINI)
        var selectedModel by mutableStateOf("gemini-2.5-flash")
        var geminiKey by mutableStateOf("")
        var openRouterKey by mutableStateOf("")
        var appLang by mutableStateOf("de")
        var transLang by mutableStateOf("en")
        var testedCount = 0
        var savedCount = 0
        var dismissedCount = 0

        val sampleModels = listOf(
            AiModelInfo(
                id = "gemini-2.5-flash",
                name = "Gemini 2.5 Flash",
                provider = ModelProvider.GEMINI,
                isFreeTier = true,
                tier = "Free",
                functionalDescription = "Schnelles Standardmodell"
            ),
            AiModelInfo(
                id = "gemini-2.5-pro",
                name = "Gemini 2.5 Pro",
                provider = ModelProvider.GEMINI,
                isFreeTier = false,
                tier = "Pro",
                functionalDescription = "Tiefgehende Analyse"
            ),
            AiModelInfo(
                id = "openrouter/free",
                name = "OpenRouter Free Auto",
                provider = ModelProvider.OPENROUTER,
                isFreeTier = true,
                tier = "Free",
                functionalDescription = "Automatische kostenlose Route"
            )
        )

        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = true) {
                ApiKeyDialog(
                    selectedProvider = provider,
                    geminiKey = geminiKey,
                    openRouterKey = openRouterKey,
                    selectedModel = selectedModel,
                    availableModels = sampleModels.filter { it.provider == provider },
                    isTestingKey = false,
                    testKeyResult = null,
                    remainingDailyRequests = 18,
                    totalDailyQuota = 20,
                    onDismiss = { dismissedCount++ },
                    onSelectProvider = { newProvider ->
                        provider = newProvider
                        selectedModel = if (newProvider == ModelProvider.OPENROUTER) "openrouter/free" else "gemini-2.5-flash"
                    },
                    onGeminiKeyChange = { geminiKey = it },
                    onOpenRouterKeyChange = { openRouterKey = it },
                    onSelectModel = { selectedModel = it },
                    onTestKey = { testedCount++ },
                    onSaveAll = { savedCount++ },
                    onClearKey = {
                        if (provider == ModelProvider.GEMINI) geminiKey = "" else openRouterKey = ""
                    },
                    appLanguage = appLang,
                    onAppLanguageSelected = { appLang = it },
                    translationLanguage = transLang,
                    onTranslationLanguageSelected = { transLang = it }
                )
            }
        }

        val stringsDe = AppStrings.get("de")
        val stringsAr = AppStrings.get("ar")
        composeTestRule.waitForIdle()

        // 1. Sheet container & concise 2-word header
        composeTestRule.onNodeWithTag("api_key_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithText(stringsDe.settingsTitle).assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_back_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("active_model_details_box").assertIsDisplayed()
        composeTestRule.onNodeWithTag("api_key_hint_text").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("daily_quota_badge").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("18 / 20").assertIsDisplayed()
        assertEquals(0, composeTestRule.onAllNodesWithText("Free").fetchSemanticsNodes().size)

        // 2. Model dropdown with search & filter chip (Flash vs Pro)
        composeTestRule.onNodeWithTag("model_dropdown_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("model_search_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("model_filter_chip").assertIsDisplayed()

        // Toggle filter off to expose Gemini 2.5 Pro and search for "Pro"
        composeTestRule.onNodeWithTag("model_filter_chip").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("model_search_input").performTextInput("Pro")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Gemini 2.5 Pro").performClick()
        composeTestRule.waitForIdle()
        assertEquals("gemini-2.5-pro", selectedModel)

        // 3. Provider dropdown selects OpenRouter cleanly
        composeTestRule.onNodeWithTag("provider_dropdown_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("provider_openrouter_button").performClick()
        composeTestRule.waitForIdle()
        assertEquals(ModelProvider.OPENROUTER, provider)

        // 4. OpenRouter key input & test button
        composeTestRule.onNodeWithTag("openrouter_key_input").performScrollTo().performTextInput("sk-or-test-123")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("test_openrouter_key_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals(1, testedCount)

        // 5. Language dropdowns (App Language -> Arabic, verifies concise 2-word Arabic title)
        composeTestRule.onNodeWithTag("app_lang_dropdown_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("app_lang_ar_button").performClick()
        composeTestRule.waitForIdle()
        assertEquals("ar", appLang)
        composeTestRule.onNodeWithText(stringsAr.settingsTitle).assertIsDisplayed()

        composeTestRule.onNodeWithTag("trans_lang_dropdown_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("trans_lang_ar_button").performClick()
        composeTestRule.waitForIdle()
        assertEquals("ar", transLang)

        // 6. Confirm & Cancel footer buttons
        composeTestRule.onNodeWithTag("save_api_key_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals(1, savedCount)

        composeTestRule.onNodeWithTag("close_api_key_dialog").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertTrue(dismissedCount >= 1)
    }
}
