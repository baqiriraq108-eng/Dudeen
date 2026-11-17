package com.example

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.GrammarType
import com.example.ui.MainScreen
import com.example.ui.MainViewModel
import com.example.ui.SearchCategoryCapsuleBar
import com.example.ui.components.HistoryFilterPills
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.MyApplicationTheme
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class SearchCategoriesCapsuleTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testHistoryFilterCategoriesRenderInSingleCapsuleTape() {
        var selectedIndex = 0
        composeTestRule.setContent {
            MyApplicationTheme {
                HistoryFilterPills(
                    selectedFilterIndex = selectedIndex,
                    onFilterChanged = { selectedIndex = it },
                    appLanguage = "de"
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("history_filter_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("history_filter_0").assertIsDisplayed()
        composeTestRule.onNodeWithTag("history_filter_1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("history_filter_2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nomen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Verben").assertIsDisplayed()

        composeTestRule.onNodeWithTag("history_filter_2").performClick()
        composeTestRule.waitForIdle()
        assertEquals(2, selectedIndex)
    }

    @Test
    fun testAllSearchCategoryCapsulesHaveSymmetricalIconAndLabelSideMarginsInGerman() {
        val deStrings = AppStrings.get("de")
        composeTestRule.setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides deStrings.language.layoutDirection) {
                    SearchCategoryCapsuleBar(
                        selectedGrammarType = GrammarType.VERB,
                        strings = deStrings,
                        onSelectType = {}
                    )
                }
            }
        }

        composeTestRule.waitForIdle()

        val checks = listOf(
            Triple(GrammarType.NOMEN, deStrings.tabExpressions, "nomen"),
            Triple(GrammarType.VERB, deStrings.tabVerb, "verb"),
            Triple(GrammarType.NOMEN_LIST, deStrings.tabWords, "nomen_list"),
            Triple(GrammarType.ADJEKTIV, deStrings.tabAdjectives, "adjektiv")
        )
        checks.forEach { (_, label, tagSuffix) ->
            val capsuleBounds = composeTestRule.onNodeWithTag("grammar_type_tab_$tagSuffix").getBoundsInRoot()
            val iconBounds = composeTestRule.onNodeWithTag("grammar_type_icon_$tagSuffix", useUnmergedTree = true).getBoundsInRoot()
            val textBounds = composeTestRule.onNodeWithText(label, useUnmergedTree = true).getBoundsInRoot()

            val leftMargin = iconBounds.left.value - capsuleBounds.left.value
            val rightMargin = capsuleBounds.right.value - textBounds.right.value
            assertTrue(
                "Capsule $tagSuffix leftMargin ($leftMargin dp) and rightMargin ($rightMargin dp) must be symmetrical",
                abs(leftMargin - rightMargin) <= 1.0f
            )
            assertTrue("Capsule $tagSuffix margins must be positive", leftMargin >= 4f && rightMargin >= 4f)
        }
    }

    @Test
    fun testVerbCapsuleIconAndLabelAreSymmetricalInArabicRtl() {
        val arStrings = AppStrings.get("ar")

        composeTestRule.setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides arStrings.language.layoutDirection) {
                    SearchCategoryCapsuleBar(
                        selectedGrammarType = GrammarType.VERB,
                        strings = arStrings,
                        onSelectType = {}
                    )
                }
            }
        }

        composeTestRule.waitForIdle()
        val verbCapsuleBounds = composeTestRule.onNodeWithTag("grammar_type_tab_verb").getBoundsInRoot()
        val verbIconBounds = composeTestRule.onNodeWithTag("grammar_type_icon_verb", useUnmergedTree = true).getBoundsInRoot()
        val verbTextBounds = composeTestRule.onNodeWithText("فعل", useUnmergedTree = true).getBoundsInRoot()

        val marginLeft = verbTextBounds.left.value - verbCapsuleBounds.left.value
        val marginRight = verbCapsuleBounds.right.value - verbIconBounds.right.value
        assertTrue(
            "RTL Verb capsule leftMargin ($marginLeft dp) and rightMargin ($marginRight dp) must be symmetrical",
            abs(marginLeft - marginRight) <= 1.0f
        )
    }

    @Test
    fun testToolbarBackButtonDoesNotAppearWhenSelectingHistoryCategoryFilter() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MainViewModel(app)

        composeTestRule.setContent {
            MyApplicationTheme {
                MainScreen(viewModel = viewModel)
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("toolbar_back_button").assertDoesNotExist()

        composeTestRule.runOnUiThread {
            viewModel.onSelectHistoryFilter(1) // Nomen
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("toolbar_back_button").assertDoesNotExist()

        composeTestRule.runOnUiThread {
            viewModel.onSelectHistoryFilter(2) // Verben
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("toolbar_back_button").assertDoesNotExist()
    }
}
