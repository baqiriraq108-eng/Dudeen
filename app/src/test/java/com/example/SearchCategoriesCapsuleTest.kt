package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.ui.components.HistoryFilterPills
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
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
}
