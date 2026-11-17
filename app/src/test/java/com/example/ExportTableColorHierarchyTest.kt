package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.core.graphics.ColorUtils
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AdjektivResult
import com.example.data.model.DeclensionCaseRow
import com.example.data.model.DeclensionTableGroup
import com.example.data.model.VerbConjugationResult
import com.example.data.model.VerbImperativ
import com.example.data.model.VerbTenseConjugation
import com.example.data.model.WordDeclensionResult
import com.example.ui.components.PrintExportDialog
import com.example.ui.theme.MyApplicationTheme
import com.example.util.CustomColorManager
import com.example.util.DeclensionHistoryExportHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.zip.ZipFile

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExportTableColorHierarchyTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun sampleNoun(word: String, article: String = "der"): WordDeclensionResult {
        val plural = "die ${word}e"
        val singularRows = listOf(
            DeclensionCaseRow("nominativ", "Nominativ", "Wer?", "$article $word", "ein $word", word),
            DeclensionCaseRow("akkusativ", "Akkusativ", "Wen?", "$article $word", "einen $word", word),
            DeclensionCaseRow("dativ", "Dativ", "Wem?", "dem $word", "einem $word", word),
            DeclensionCaseRow("genitiv", "Genitiv", "Wessen?", "des ${word}s", "eines ${word}s", "${word}s")
        )
        val pluralRows = listOf(
            DeclensionCaseRow("nominativ", "Nominativ", "Wer?", plural, "keine", word),
            DeclensionCaseRow("akkusativ", "Akkusativ", "Wen?", plural, "keine", word),
            DeclensionCaseRow("dativ", "Dativ", "Wem?", "den ${word}n", "keinen", "${word}n"),
            DeclensionCaseRow("genitiv", "Genitiv", "Wessen?", "der $plural", "keiner", word)
        )
        return WordDeclensionResult(
            word = word,
            gender = if (article == "die") "Feminin" else "Maskulin",
            genderArticle = article,
            pluralNoun = plural,
            meaningEnglish = "meaning-$word",
            singular = DeclensionTableGroup("Singular", singularRows),
            plural = DeclensionTableGroup("Plural", pluralRows)
        )
    }

    private fun sampleVerb(verb: String): VerbConjugationResult {
        val tenses = listOf(
            VerbTenseConjugation("praesens", "Präsens", "${verb}-ich", "${verb}-du", "${verb}-er", "${verb}-wir", "${verb}-ihr", "${verb}-sie"),
            VerbTenseConjugation("praeteritum", "Präteritum", "${verb}-ich", "${verb}-du", "${verb}-er", "${verb}-wir", "${verb}-ihr", "${verb}-sie"),
            VerbTenseConjugation("perfekt", "Perfekt", "${verb}-ich", "${verb}-du", "${verb}-er", "${verb}-wir", "${verb}-ihr", "${verb}-sie"),
            VerbTenseConjugation("plusquamperfekt", "Plusquamperfekt", "${verb}-ich", "${verb}-du", "${verb}-er", "${verb}-wir", "${verb}-ihr", "${verb}-sie"),
            VerbTenseConjugation("futur1", "Futur I", "${verb}-ich", "${verb}-du", "${verb}-er", "${verb}-wir", "${verb}-ihr", "${verb}-sie"),
            VerbTenseConjugation("futur2", "Futur II", "${verb}-ich", "${verb}-du", "${verb}-er", "${verb}-wir", "${verb}-ihr", "${verb}-sie")
        )
        return VerbConjugationResult(
            word = verb,
            infinitiv = verb,
            hilfsverb = "haben",
            partizip1 = "${verb}end",
            partizip2 = "ge${verb}t",
            meaningEnglish = "to $verb",
            imperativ = VerbImperativ(du = "$verb!", ihr = "${verb}t!", sie = "$verb Sie!"),
            tenses = tenses
        )
    }

    private fun sampleAdjective(positiv: String): AdjektivResult {
        return AdjektivResult(
            word = positiv,
            komparativ = "${positiv}er",
            superlativ = "am ${positiv}sten",
            meaningEnglish = "meaning-$positiv"
        )
    }

    @Test
    fun testFourTierPaletteBounds_CalmAndVividModes() {
        val seedColors = listOf("#2979FF", "#10B981", "#E11D48", "#8B5CF6", "#F59E0B")
        val calmHeaderHexes = mutableSetOf<String>()
        val calmBlockHexes = mutableSetOf<String>()

        for (seed in seedColors) {
            // 1. Mode 1: Enhanced Calm Tonal Hierarchy (isVividMode = false)
            val calmPalette = DeclensionHistoryExportHelper.resolveExportTableColorPalette(seed, isVividMode = false)
            assertFalse("Seed $seed should not be default neutral", calmPalette.isDefaultNeutral)
            assertFalse("Calm mode flag should be false", calmPalette.isVividMode)

            calmHeaderHexes.add(calmPalette.tier1HeaderFillHex)
            calmBlockHexes.add(calmPalette.tier2SecondaryBlockFillHex)

            // Verify Tier 1 Header Row Fill (Calm): Saturation 48%–62%, Lightness 74%–80%
            val hsl1 = FloatArray(3)
            ColorUtils.colorToHSL(calmPalette.tier1HeaderFillColorInt, hsl1)
            assertTrue("Calm Tier 1 S (${hsl1[1]}) out of range for $seed", hsl1[1] in 0.45f..0.65f)
            assertTrue("Calm Tier 1 L (${hsl1[2]}) out of range for $seed", hsl1[2] in 0.72f..0.82f)

            // Verify Tier 2A Primary Block (Calm): Saturation 24%–34%, Lightness 94%–95.5%
            val hsl2A = FloatArray(3)
            ColorUtils.colorToHSL(calmPalette.tier2PrimaryBlockFillColorInt, hsl2A)
            assertTrue("Calm Tier 2A S (${hsl2A[1]}) out of range for $seed", hsl2A[1] in 0.20f..0.38f)
            assertTrue("Calm Tier 2A L (${hsl2A[2]}) out of range for $seed", hsl2A[2] in 0.92f..0.97f)

            // Verify Tier 2B Secondary Block (Calm): Saturation 35%–48%, Lightness 86%–89%
            val hsl2B = FloatArray(3)
            ColorUtils.colorToHSL(calmPalette.tier2SecondaryBlockFillColorInt, hsl2B)
            assertTrue("Calm Tier 2B S (${hsl2B[1]}) out of range for $seed", hsl2B[1] in 0.32f..0.52f)
            assertTrue("Calm Tier 2B L (${hsl2B[2]}) out of range for $seed", hsl2B[2] in 0.84f..0.91f)

            // Verify Tier 3 Major Structural Border (Calm): Saturation 50%–65%, Lightness 52%–60%
            val hsl3 = FloatArray(3)
            ColorUtils.colorToHSL(calmPalette.tier3MajorBorderColorInt, hsl3)
            assertTrue("Calm Tier 3 S (${hsl3[1]}) out of range for $seed", hsl3[1] in 0.47f..0.68f)
            assertTrue("Calm Tier 3 L (${hsl3[2]}) out of range for $seed", hsl3[2] in 0.50f..0.62f)

            // Verify Tier 4 Inner Cell Gridlines (Calm): Saturation 32%–45%, Lightness 76%–81%
            val hsl4 = FloatArray(3)
            ColorUtils.colorToHSL(calmPalette.tier4InnerGridBorderColorInt, hsl4)
            assertTrue("Calm Tier 4 S (${hsl4[1]}) out of range for $seed", hsl4[1] in 0.29f..0.48f)
            assertTrue("Calm Tier 4 L (${hsl4[2]}) out of range for $seed", hsl4[2] in 0.74f..0.83f)

            // 2. Mode 2: Actual Wheel Color in 4-Tier Hierarchy (isVividMode = true)
            val vividPalette = DeclensionHistoryExportHelper.resolveExportTableColorPalette(seed, isVividMode = true)
            assertFalse(vividPalette.isDefaultNeutral)
            assertTrue("Vivid mode flag should be true", vividPalette.isVividMode)
            // Tier 1 Header Fill uses the exact chosen color from the Color Wheel
            assertEquals("Vivid Tier 1 Header must match exact seed hex", seed, vividPalette.tier1HeaderFillHex)

            val vHsl2A = FloatArray(3)
            ColorUtils.colorToHSL(vividPalette.tier2PrimaryBlockFillColorInt, vHsl2A)
            assertTrue("Vivid Tier 2A L (${vHsl2A[2]}) out of range for $seed", vHsl2A[2] in 0.89f..0.95f)

            val vHsl2B = FloatArray(3)
            ColorUtils.colorToHSL(vividPalette.tier2SecondaryBlockFillColorInt, vHsl2B)
            assertTrue("Vivid Tier 2B L (${vHsl2B[2]}) out of range for $seed", vHsl2B[2] in 0.79f..0.87f)
        }

        // All 5 seed colors must produce unmistakably distinct Calm Mode palettes
        assertEquals(5, calmHeaderHexes.size)
        assertEquals(5, calmBlockHexes.size)

        // Default Neutral Reset
        val neutralPalette = DeclensionHistoryExportHelper.resolveExportTableColorPalette("")
        assertTrue(neutralPalette.isDefaultNeutral)
        assertEquals("FFD9D9D9", neutralPalette.tier1HeaderFillArgbHex)
        assertEquals("FFFFFFFF", neutralPalette.tier2PrimaryBlockFillArgbHex)
        assertEquals("FFF2F2F2", neutralPalette.tier2SecondaryBlockFillArgbHex)
    }

    @Test
    fun testXlsxAndPdfExport_AppliesFourTierPaletteAndCustomFontColors() {
        val nouns = listOf(sampleNoun("Tisch", "der"), sampleNoun("Lampe", "die"))
        val verbs = listOf(sampleVerb("machen"), sampleVerb("gehen"))
        val adjectives = listOf(sampleAdjective("schnell"), sampleAdjective("klein"))

        val seedHex = "#2979FF"
        for (vividMode in listOf(false, true)) {
            val palette = DeclensionHistoryExportHelper.resolveExportTableColorPalette(seedHex, isVividMode = vividMode)
            val typography = DeclensionHistoryExportHelper.ExportTypographyOptions(
                bodyFontSize = 12f,
                bodyFontFamily = "Arial",
                bodyFontColorHex = "#1B5E20",
                headerFontSize = 14f,
                headerFontFamily = "Arial",
                headerFontColorHex = "#4A148C",
                tableSeedColorHex = seedHex,
                isTableColorVivid = vividMode
            )

            val xlsxFile = tempFolder.newFile("test_hierarchy_${vividMode}.xlsx")
            DeclensionHistoryExportHelper.createUnifiedXlsxFile(
                file = xlsxFile,
                nouns = nouns,
                verbs = verbs,
                adjectives = adjectives,
                scope = DeclensionHistoryExportHelper.ExportScope.ALLE,
                documentTitle = "test_hierarchy",
                typography = typography
            )
            assertTrue(xlsxFile.exists() && xlsxFile.length() > 0)

            ZipFile(xlsxFile).use { zip ->
                val stylesXml = zip.getInputStream(zip.getEntry("xl/styles.xml")).bufferedReader().readText()

                // Verify all 4 tiers appear in styles.xml fills and borders
                assertTrue(stylesXml.contains("rgb=\"${palette.tier1HeaderFillArgbHex}\""))
                assertTrue(stylesXml.contains("rgb=\"${palette.tier2PrimaryBlockFillArgbHex}\""))
                assertTrue(stylesXml.contains("rgb=\"${palette.tier2SecondaryBlockFillArgbHex}\""))
                assertTrue(stylesXml.contains("rgb=\"${palette.tier3MajorBorderArgbHex}\""))
                assertTrue(stylesXml.contains("rgb=\"${palette.tier4InnerGridBorderArgbHex}\""))

                // Verify independent custom font colors are applied alongside the 4-tier table colors
                assertTrue("Body font color must be applied", stylesXml.contains("rgb=\"FF1B5E20\""))
                assertTrue("Header font color must be applied", stylesXml.contains("rgb=\"FF4A148C\""))

                // Verify Verb Master Sheet (sheet2.xml) alternates by 7-row verb block and uses Tier 3 separator on 7th row
                val verbSheetXml = zip.getInputStream(zip.getEntry("xl/worksheets/sheet2.xml")).bufferedReader().readText()
                assertTrue(verbSheetXml.contains("<c r=\"B3\" s=\"20\""))
                assertTrue(verbSheetXml.contains("<c r=\"B8\" s=\"20\""))
                assertTrue(verbSheetXml.contains("<c r=\"C3\" s=\"2\""))
                assertTrue(verbSheetXml.contains("<c r=\"C8\" s=\"2\""))
                assertTrue(verbSheetXml.contains("<c r=\"B9\" s=\"24\""))
                assertTrue(verbSheetXml.contains("<c r=\"C9\" s=\"22\""))
                assertTrue(verbSheetXml.contains("<c r=\"B10\" s=\"21\""))
                assertTrue(verbSheetXml.contains("<c r=\"B15\" s=\"21\""))
                assertTrue(verbSheetXml.contains("<c r=\"C10\" s=\"3\""))
                assertTrue(verbSheetXml.contains("<c r=\"C15\" s=\"3\""))
                assertTrue(verbSheetXml.contains("<c r=\"B16\" s=\"25\""))
                assertTrue(verbSheetXml.contains("<c r=\"C16\" s=\"23\""))
            }
        }

        // Also verify PDF table layout & 4-tier color resolution across Nouns, Verbs (7-row blocks), and Adjectives
        val verbRows = DeclensionHistoryExportHelper.buildMasterVerbRows(verbs)
        assertEquals(14, verbRows.size)
        for (idx in verbRows.indices) {
            val blockIdx = idx / 7
            val expectedZebra = (blockIdx % 2 == 1)
            assertEquals(idx < 7, !expectedZebra)
            assertEquals(idx % 7 == 6, verbRows[idx].isLastRowOfVerb)
        }
    }

    @Test
    fun testPrintExportDialog_UnifiedFontAndTableColorSelectorsAndModes() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        CustomColorManager.setExportTableSeedColor(context, "#2979FF")
        CustomColorManager.setExportTableColorVividMode(context, false)
        CustomColorManager.setExportBodyFontColor(context, "#1E293B")
        CustomColorManager.setExportHeaderFontColor(context, "#0F172A")

        composeTestRule.setContent {
            MyApplicationTheme {
                PrintExportDialog(onDismiss = {})
            }
        }

        // Header
        composeTestRule.onNodeWithText("Exportieren").assertIsDisplayed()

        // Both font subsections show exact clean German titles and identical "Aa Bb Ää العربية" sample text
        composeTestRule.onNodeWithText("Schriftart & -größe (Text)").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Schriftart & -größe (Überschriften)").performScrollTo().assertIsDisplayed()
        assertEquals(2, composeTestRule.onAllNodesWithText("Aa Bb Ää العربية").fetchSemanticsNodes().size)

        // Unified font color selectors (body + header) with unnecessary Standard reset buttons removed
        composeTestRule.onNodeWithTag("body_color_selector").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("header_color_selector").performScrollTo().assertIsDisplayed()
        assertEquals(0, composeTestRule.onAllNodesWithTag("body_color_reset").fetchSemanticsNodes().size)
        assertEquals(0, composeTestRule.onAllNodesWithTag("header_color_reset").fetchSemanticsNodes().size)

        // Scroll to Dedicated Export Table Color card & Mode Switcher (Calm vs Vivid)
        composeTestRule.onNodeWithTag("export_table_color_card", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("Tabellenfarbe (Hintergrund)").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("4-Stufen-Hierarchie").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("export_table_mode_calm").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("export_table_mode_vivid").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("export_table_color_selector").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("export_table_color_strip", useUnmergedTree = true).assertExists()
        assertEquals(0, composeTestRule.onAllNodesWithText("Kopfzeile", useUnmergedTree = true).fetchSemanticsNodes().size)
        assertEquals(0, composeTestRule.onAllNodesWithTag("export_table_color_reset_neutral").fetchSemanticsNodes().size)

        // Filename field with attached .xlsx suffix
        composeTestRule.onNodeWithTag("file_name_input").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText(".xlsx").assertIsDisplayed()

        // Bottom action buttons are scrollable and fully reachable without clipping
        composeTestRule.onNodeWithTag("export_cancel_button").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("export_share_button").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("export_save_button").performScrollTo().assertIsDisplayed()

        // Switch to Vivid (Actual Wheel Color) mode and verify HEX matches #2979FF
        composeTestRule.onNodeWithTag("export_table_mode_vivid").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertTrue(CustomColorManager.getExportTableColorVividMode(context))
        composeTestRule.onNodeWithText("#2979FF", useUnmergedTree = true).assertExists()

        // Switch back to Calm mode
        composeTestRule.onNodeWithTag("export_table_mode_calm").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertFalse(CustomColorManager.getExportTableColorVividMode(context))

        // Open Color Wheel in Export Table Color mode
        composeTestRule.onNodeWithTag("export_table_color_selector").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("export_color_wheel_tier_preview", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithTag("color_wheel_default_neutral_button").assertExists()
    }
}
