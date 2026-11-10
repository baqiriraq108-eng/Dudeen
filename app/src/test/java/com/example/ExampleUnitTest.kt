package com.example

import com.example.data.model.GrammarResult
import com.example.data.model.GrammarType
import com.example.data.model.WordDeclensionResult
import com.example.data.model.WordExample
import com.example.data.model.resolveWordExamples
import com.example.data.model.withResolvedSentenceExamples
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testResolveWordExamples_apfelBananeKirsche() {
    val json = """
      {
        "type": "Wörter",
        "word": "Apfel, Banane, Kirsche",
        "gender": "Wörter",
        "meaningEnglish": "Apple / Banana / Cherry"
      }
    """.trimIndent()
    val result = WordDeclensionResult.fromJson(json)
    val examples = resolveWordExamples(result)

    assertEquals(3, examples.size)
    assertEquals("Apfel", examples[0].word)
    assertEquals("Der Apfel schmeckt sehr süß und frisch.", examples[0].sentence)
    assertEquals("Banane", examples[1].word)
    assertEquals("Die Banane ist gelb und reich an Kalium.", examples[1].sentence)
    assertEquals("Kirsche", examples[2].word)
    assertEquals("Die Kirsche blüht im Frühling und reift im Sommer.", examples[2].sentence)
  }

  @Test
  fun testWordDeclensionResult_withExplicitExamples() {
    val json = """
      {
        "type": "Wörter",
        "word": "Hund, Katze",
        "gender": "Wörter",
        "meaningEnglish": "Dog / Cat",
        "examples": [
          { "word": "Hund", "sentence": "Der Hund bellt laut." },
          { "word": "Katze", "sentence": "Die Katze schläft." }
        ]
      }
    """.trimIndent()
    val result = WordDeclensionResult.fromJson(json)
    assertEquals(2, result.examples.size)
    assertEquals("Hund", result.examples[0].word)
    assertEquals("Der Hund bellt laut.", result.examples[0].sentence)
    assertEquals("Katze", result.examples[1].word)
    assertEquals("Die Katze schläft.", result.examples[1].sentence)

    val resolved = resolveWordExamples(result)
    assertEquals(2, resolved.size)
    assertEquals("Der Hund bellt laut.", resolved[0].sentence)
  }

  @Test
  fun testResolveSentenceExamples_hobbyKochen() {
    val json = """
      {
        "type": "Satz",
        "word": "Mein Hobby ist nämlich Kochen.",
        "gender": "–",
        "meaningEnglish": "My hobby is cooking, you see."
      }
    """.trimIndent()
    val result = WordDeclensionResult.fromJson(json)
    val examples = com.example.data.model.resolveSentenceExamples(result)

    assertEquals(3, examples.size)
    assertEquals("Lesen", examples[0].word)
    assertEquals("Mein Hobby ist Lesen.", examples[0].sentence)
    assertEquals("Schwimmen", examples[1].word)
    assertEquals("Mein Hobby ist Schwimmen.", examples[1].sentence)
    assertEquals("Musik hören", examples[2].word)
    assertEquals("Mein Hobby ist Musik hören.", examples[2].sentence)
  }

  @Test
  fun testResolveSentenceExamples_withExplicitExamples() {
    val json = """
      {
        "type": "Satz",
        "word": "Mein Hobby ist nämlich Kochen.",
        "gender": "–",
        "meaningEnglish": "My hobby is cooking, you see.",
        "examples": [
          { "word": "Lesen", "sentence": "Mein Hobby ist Lesen und ich lese jeden Abend ein Buch." },
          { "word": "Schwimmen", "sentence": "Mein Hobby ist Schwimmen und ich gehe jeden Morgen ins Schwimmbad." },
          { "word": "Musik hören", "sentence": "Mein Hobby ist Musik hören und ich höre viele verschiedene Lieder." }
        ]
      }
    """.trimIndent()
    val result = WordDeclensionResult.fromJson(json)
    assertEquals(3, result.examples.size)
    assertEquals("Lesen", result.examples[0].word)
    assertTrue(result.examples[0].sentence.contains("jeden Abend ein Buch"))

    val resolved = com.example.data.model.resolveSentenceExamples(result)
    assertEquals(3, resolved.size)
  }

  @Test
  fun testCalculateSentenceListLayout_shortText() {
    val examples = listOf(
      WordExample("Lesen", "Mein Hobby ist Lesen."),
      WordExample("Schwimmen", "Mein Hobby ist Schwimmen.")
    )
    val layout = com.example.ui.components.calculateSentenceListLayout(
      examples = examples,
      accentColor = androidx.compose.ui.graphics.Color(0xFF00695C),
      textColor = androidx.compose.ui.graphics.Color.Black,
      isDark = false
    )
    // Short text: wraps in upperExamples, no horizontal scrolling
    assertEquals(2, layout.upperExamples.size)
    assertNull(layout.adjacentContent)
    assertFalse(layout.isScrollingActive)
  }

  @Test
  fun testCalculateSentenceListLayout_longText_activatesScrolling() {
    val examples = listOf(
      WordExample("Lesen", "Mein Hobby ist Lesen und ich lese jeden Abend ein langes deutsches Buch."),
      WordExample("Schwimmen", "Mein Hobby ist Schwimmen und ich gehe jeden Samstag morgen ins städtische Schwimmbad."),
      WordExample("Musik", "Mein Hobby ist Musik hören und klassische Konzerte in Berlin besuchen.")
    )
    val layout = com.example.ui.components.calculateSentenceListLayout(
      examples = examples,
      accentColor = androidx.compose.ui.graphics.Color(0xFF00695C),
      textColor = androidx.compose.ui.graphics.Color.Black,
      isDark = false
    )
    // Long text: first 2 in upper area, 3rd adjacent to Satz label with horizontal scroll
    assertEquals(2, layout.upperExamples.size)
    assertNotNull(layout.adjacentContent)
    assertTrue(layout.isScrollingActive)
  }

  @Test
  fun testWithResolvedSentenceExamples_updatesRawJson() {
    val result = WordDeclensionResult(
      word = "Mein Hobby ist nämlich Kochen.",
      gender = "–",
      genderArticle = "",
      pluralNoun = "–",
      meaningEnglish = "My hobby is cooking, you see.",
      singular = com.example.data.model.DeclensionTableGroup("Singular", emptyList()),
      plural = com.example.data.model.DeclensionTableGroup("Plural", emptyList()),
      rawJson = "{\"word\":\"Mein Hobby ist nämlich Kochen.\"}",
      examples = emptyList()
    )
    val updated = result.withResolvedSentenceExamples()
    assertTrue(updated.examples.isNotEmpty())
    assertTrue(updated.rawJson.contains("\"examples\""))
  }

  @Test
  fun testNomenAndSaetze_sentenceWithComma_parsedAsSentence() {
    val sentenceJson = """
      {
        "type": "Satz",
        "word": "Ich weiß, dass er heute kommt.",
        "meaningEnglish": "I know that he is coming today.",
        "gender": "–",
        "genderArticle": "",
        "pluralNoun": "–",
        "examples": [
          { "word": "wissen", "example": "Ich weiß nicht, wie spät es ist." },
          { "word": "kommen", "example": "Er kommt morgen nach Berlin." }
        ]
      }
    """.trimIndent()

    val grammarResult = GrammarResult.fromJson(sentenceJson, fallbackType = GrammarType.SENTENCE)
    assertTrue("Should be parsed as GrammarResult.Sentence", grammarResult is GrammarResult.Sentence)
    val declension = (grammarResult as GrammarResult.Sentence).declension
    assertEquals("–", declension.gender)
    assertEquals("Ich weiß, dass er heute kommt.", declension.word)
    assertEquals(2, declension.examples.size)
  }

  @Test
  fun testWoerterTab_wordsWithComma_parsedAsNomenList() {
    val listJson = """
      {
        "type": "Wörter",
        "word": "Apfel, Banane, Kirsche",
        "meaningEnglish": "apple, banana, cherry",
        "gender": "Wörter",
        "genderArticle": "",
        "pluralNoun": "–",
        "examples": [
          { "word": "Apfel", "example": "Der Apfel schmeckt süß." },
          { "word": "Banane", "example": "Die Banane ist gelb." }
        ]
      }
    """.trimIndent()

    val grammarResult = GrammarResult.fromJson(listJson, fallbackType = GrammarType.NOMEN_LIST)
    assertTrue("Should be parsed as GrammarResult.NomenList", grammarResult is GrammarResult.NomenList)
    assertFalse("Must NOT be parsed as GrammarResult.Sentence", grammarResult is GrammarResult.Sentence)
    val declension = (grammarResult as GrammarResult.NomenList).declension
    assertEquals("Wörter", declension.gender)
    assertEquals("Apfel, Banane, Kirsche", declension.word)
  }

  @Test
  fun testInputClassification_noOverlapBetweenFields() {
    val sentenceWithComma = "Ich glaube, es regnet bald."
    val wordsSentence = sentenceWithComma.split(Regex("[\\s,;]+")).filter { it.isNotBlank() }
    assertTrue("Sentence with comma has >= 2 words", wordsSentence.size >= 2)

    val singleNoun = "Tisch"
    val wordsNoun = singleNoun.split(Regex("[\\s,;]+")).filter { it.isNotBlank() }
    assertEquals(1, wordsNoun.size)

    val wordListWithCommas = "Hund, Katze, Maus"
    val wordsList = wordListWithCommas.split(Regex("[,;،/\\n]+")).map { it.trim() }.filter { it.isNotBlank() }
    assertEquals(3, wordsList.size)
  }
}

