package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.example.ui.components.GrammarTablesSheet
import com.example.ui.theme.MyApplicationTheme
import kotlin.math.abs
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class GrammarTablesHeaderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testGrammarHeaderHasNoIconOrSubtitleAndAlignsMidpointWithSpeedPill() {
        composeTestRule.setContent {
            MyApplicationTheme {
                GrammarTablesSheet(
                    onDismiss = {},
                    onSpeak = { _, _ -> },
                    currentlySpokenId = null,
                    ttsSpeed = 1.0f,
                    onTtsSpeedChange = {},
                    appLanguage = "de"
                )
            }
        }

        composeTestRule.waitForIdle()

        // 1. Book icon and subtitle are completely removed
        composeTestRule.onNodeWithTag("grammar_header_book_icon").assertDoesNotExist()
        composeTestRule.onNodeWithText("Übersichten, Regeln & Aussprache", substring = true)
            .assertDoesNotExist()

        // 2. Title and speed pill are displayed cleanly
        val titleNode = composeTestRule.onNodeWithTag("grammar_header_title")
        val speedNode = composeTestRule.onNodeWithTag("speech_rate_button")
        titleNode.assertIsDisplayed()
        speedNode.assertIsDisplayed()
        composeTestRule.onNodeWithText("Grammatik-Tabellen").assertIsDisplayed()

        // 3. Left-to-right non-overlapping placement and exact vertical midpoint alignment
        val titleBounds = titleNode.getBoundsInRoot()
        val speedBounds = speedNode.getBoundsInRoot()

        assertTrue(
            "Title should stay on the left with breathing room before speed pill",
            titleBounds.right < speedBounds.left
        )

        val titleCenterY = (titleBounds.top.value + titleBounds.bottom.value) / 2f
        val speedCenterY = (speedBounds.top.value + speedBounds.bottom.value) / 2f
        assertTrue(
            "Title centerY ($titleCenterY dp) must match speed pill centerY ($speedCenterY dp)",
            abs(titleCenterY - speedCenterY) <= 0.5f
        )
    }
}
