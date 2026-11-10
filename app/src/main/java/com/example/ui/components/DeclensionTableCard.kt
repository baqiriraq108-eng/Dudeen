package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeclensionCaseRow
import com.example.data.model.DeclensionTableGroup
import com.example.data.model.WordDeclensionResult
import com.example.data.model.WordExample
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.LocalTranslationLanguage
import com.example.util.isArabicText
import com.example.util.cleanTranslationText
import com.example.data.model.resolveSentenceExamples
import androidx.compose.ui.graphics.Brush
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.GeoBannerGradient
import com.example.ui.theme.GeoBorder
import com.example.ui.theme.GeoCardRibbonColor
import com.example.ui.theme.GeoHeaderKasus
import com.example.ui.theme.GeoIsBoldTheme
import com.example.ui.theme.GeoOnPrimaryContainer
import com.example.ui.theme.GeoOnSecondaryContainer
import com.example.ui.theme.GeoOnSurface
import com.example.ui.theme.GeoOnSurfaceVariant
import com.example.ui.theme.GeoOutline
import com.example.ui.theme.GeoPrimary
import com.example.ui.theme.GeoPrimaryContainer
import com.example.ui.theme.GeoSecondaryContainer
import com.example.ui.theme.GeoSurface
import com.example.ui.theme.GeoSurfaceVariant

@Composable
fun SentenceResultView(
    result: WordDeclensionResult,
    onSpeak: (String, String) -> Unit = { _, _ -> },
    currentlySpokenText: String? = null,
    currentlySpokenId: String? = null,
    currentManualTranslation: String = "",
    onSaveManualTranslation: (String) -> Unit = {},
    inputQuery: String = "",
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val accentColor = if (isDark) Color(0xFF80CBC4) else Color(0xFF00695C)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("sentence_result_card"),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            if (GeoIsBoldTheme) 1.5.dp else 1.dp,
            if (GeoIsBoldTheme) GeoCardRibbonColor.copy(alpha = 0.55f) else GeoBorder
        ),
        colors = CardDefaults.cardColors(containerColor = GeoSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (GeoIsBoldTheme) 2.dp else 0.dp)
    ) {
        if (GeoIsBoldTheme) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.5.dp)
                    .background(Brush.horizontalGradient(GeoBannerGradient))
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // Top Row: German sentence (with text wrapping) + Copy Button & Speaker Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = result.word,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = GeoOnSurface,
                        lineHeight = 26.sp
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sentence_text"),
                    softWrap = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                val textToCopy = inputQuery.trim().ifBlank { result.word.trim() }
                CopyResultButton(
                    textToCopy = textToCopy,
                    size = 34.dp,
                    iconSize = 18.dp,
                    testTag = "copy_sentence_button"
                )

                Spacer(modifier = Modifier.width(6.dp))

                val sentenceSpeakerId = "decl_sentence_header_${result.word.hashCode()}"
                val isPlayingSentence = currentlySpokenId != null && currentlySpokenId == sentenceSpeakerId
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (isPlayingSentence) Color(0xFFFFCDD2) else Color(0xFFEEEEEE))
                        .clickable { onSpeak(result.word, sentenceSpeakerId) }
                        .testTag("pronounce_sentence_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlayingSentence) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = if (isPlayingSentence) "Stoppen" else "Satz anhören",
                        tint = if (isPlayingSentence) Color(0xFFD32F2F) else Color(0xFF757575),
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            // Second Row: Pronunciation (IPA) directly below the sentence and above translation
            val sentenceIpaSpeakerId = "decl_sentence_ipa_${result.word.hashCode()}"
            Spacer(modifier = Modifier.height(8.dp))
            IpaPronunciationSection(
                rawIpa = result.ipa,
                fallbackText = result.word,
                textToSpeak = result.word,
                speakerId = sentenceIpaSpeakerId,
                onSpeak = onSpeak,
                currentlySpokenId = currentlySpokenId,
                testTag = "sentence_ipa_section"
            )

            // Third Row: Translation directly below the pronunciation (Arabic or English)
            if (result.meaningEnglish.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                val isArabic = isArabicText(result.meaningEnglish)
                val prefix = if (isArabic) "الترجمة العربية : " else "English: "
                val prefixColor = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
                val cleanMeaning = remember(result.meaningEnglish, isArabic) {
                    cleanTranslationText(result.meaningEnglish, isArabic)
                }
                val annotatedText = remember(cleanMeaning, prefix, prefixColor, isDark) {
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = prefixColor, fontWeight = FontWeight.Bold)) {
                            append(prefix)
                        }
                        append(cleanMeaning)
                    }
                }
                Text(
                    text = annotatedText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.5.sp,
                        color = GeoOnSurfaceVariant,
                        lineHeight = 21.sp,
                        textAlign = if (isArabic) TextAlign.End else TextAlign.Start
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sentence_translation"),
                    softWrap = true
                )
            }

            if (currentManualTranslation.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                ManualTranslationDisplay(
                    manualTranslation = currentManualTranslation,
                    isDark = isDark,
                    fontSize = 14.5.sp,
                    testTag = "sentence_manual_translation"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            ManualTranslationInputRow(
                currentManualTranslation = currentManualTranslation,
                onSave = onSaveManualTranslation
            )

            val examples = remember(result) {
                resolveSentenceExamples(result)
            }

            val textColor = GeoOnSurface
            val layoutData = remember(examples, accentColor, textColor, isDark) {
                calculateSentenceListLayout(
                    examples = examples,
                    accentColor = accentColor,
                    textColor = textColor,
                    isDark = isDark
                )
            }

            // Subtle Horizontal Divider before Examples (replaces date)
            if (examples.isNotEmpty()) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 7.dp),
                    thickness = 0.8.dp,
                    color = if (isDark) Color(0xFF383838) else Color(0xFFE8E8E8)
                )

                // Upper examples: wrap naturally onto multiple lines
                layoutData.upperExamples.forEachIndexed { index, annotated ->
                    if (index > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Text(
                        text = annotated,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        softWrap = true
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Bottom Row: Adjacent Horizontally Scrolling Text (if active) + Satz Badge (NO date)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (layoutData.adjacentContent != null) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = layoutData.adjacentContent,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                color = GeoOnSurface
                            ),
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.testTag("adjacent_sentence_example_text")
                        )
                    }
                    // Horizontal safe margin of at least 12dp between text and badge
                    Spacer(modifier = Modifier.width(12.dp))
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Satz badge (Pill-style, fixed at bottom-right)
                NounGenderBadge(
                    gender = "Satz",
                    modifier = Modifier.testTag("sentence_type_badge")
                )
            }
        }
    }
}

@Composable
fun AdjektivResultView(
    result: com.example.data.model.AdjektivResult,
    onSpeak: (String, String) -> Unit = { _, _ -> },
    ttsSpeed: Float = 1.0f,
    onTtsSpeedChange: (Float) -> Unit = {},
    currentlySpokenText: String? = null,
    currentlySpokenId: String? = null,
    currentManualTranslation: String = "",
    onSaveManualTranslation: (String) -> Unit = {},
    inputQuery: String = "",
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val accentColor = if (isDark) Color(0xFF80CBC4) else Color(0xFF00695C)
    // Fixed turquoise label color (does not change with app theme)
    val turquoiseLabelColor = Color(0xFF00897B)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("adjektiv_result_card"),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            if (GeoIsBoldTheme) 1.5.dp else 1.dp,
            if (GeoIsBoldTheme) GeoCardRibbonColor.copy(alpha = 0.55f) else GeoBorder
        ),
        colors = CardDefaults.cardColors(containerColor = GeoSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (GeoIsBoldTheme) 2.dp else 0.dp)
    ) {
        if (GeoIsBoldTheme) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.5.dp)
                    .background(Brush.horizontalGradient(GeoBannerGradient))
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // 1. Top Row: Adjective in base form + Copy Button & Speaker Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = result.word,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = GeoOnSurface
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("adjektiv_base_text"),
                    softWrap = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                val textToCopy = inputQuery.trim().ifBlank { result.word.trim() }
                CopyResultButton(
                    textToCopy = textToCopy,
                    size = 36.dp,
                    iconSize = 19.dp,
                    testTag = "copy_adjektiv_button"
                )

                Spacer(modifier = Modifier.width(6.dp))

                val adjektivSpeakerId = "decl_adjektiv_header_${result.word.hashCode()}"
                val isPlaying = currentlySpokenId != null && currentlySpokenId == adjektivSpeakerId
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) Color(0xFFFFCDD2) else Color(0xFFEEEEEE))
                        .clickable { onSpeak(result.word, adjektivSpeakerId) }
                        .testTag("pronounce_adjektiv_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = if (isPlaying) "Stoppen" else "Adjektiv anhören",
                        tint = if (isPlaying) Color(0xFFD32F2F) else Color(0xFF757575),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 2. Below Adjective: Komp.: ... · Sup.: ... (Inflection: horizontally scrollable, no wrapping)
            Spacer(modifier = Modifier.height(8.dp))
            val valueColor = GeoOnSurface
            val compSupAnnotated = remember(result.komparativ, result.superlativ, isDark, valueColor) {
                val labelColor = if (isDark) Color(0xFF80CBC4) else Color(0xFF00695C)
                buildAnnotatedString {
                    withStyle(SpanStyle(color = labelColor, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)) {
                        append("Komp.: ")
                    }
                    withStyle(SpanStyle(color = valueColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)) {
                        append(result.komparativ.ifBlank { "–" })
                    }
                    append("   ·   ")
                    withStyle(SpanStyle(color = labelColor, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)) {
                        append("Sup.: ")
                    }
                    withStyle(SpanStyle(color = valueColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)) {
                        append(result.superlativ.ifBlank { "–" })
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = compSupAnnotated,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.testTag("adjektiv_komp_sup_text")
                )
            }

            // 3. Below Komp./Sup.: Pronunciation (IPA) - horizontally scrolling, no wrapping, speaker icon
            val adjektivIpaSpeakerId = "decl_adjektiv_ipa_${result.word.hashCode()}"
            Spacer(modifier = Modifier.height(6.dp))
            IpaPronunciationSection(
                rawIpa = result.ipa,
                fallbackText = result.word,
                textToSpeak = result.word,
                speakerId = adjektivIpaSpeakerId,
                onSpeak = onSpeak,
                currentlySpokenId = currentlySpokenId,
                testTag = "adjektiv_ipa_section"
            )

            // 4. Below Pronunciation: Translation (Arabic or English)
            if (result.meaningEnglish.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                val isArabic = isArabicText(result.meaningEnglish)
                val prefix = if (isArabic) "الترجمة العربية : " else "Eng.: "
                val prefixColor = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
                val cleanMeaning = remember(result.meaningEnglish, isArabic) {
                    cleanTranslationText(result.meaningEnglish, isArabic)
                }
                val translationAnnotated = remember(cleanMeaning, prefix, prefixColor, isDark) {
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = prefixColor, fontWeight = FontWeight.Bold)) {
                            append(prefix)
                        }
                        append(cleanMeaning)
                    }
                }
                Text(
                    text = translationAnnotated,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.5.sp,
                        color = GeoOnSurfaceVariant,
                        lineHeight = 21.sp,
                        textAlign = if (isArabic) TextAlign.End else TextAlign.Start
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("adjektiv_translation"),
                    softWrap = true
                )
            }

            if (currentManualTranslation.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                ManualTranslationDisplay(
                    manualTranslation = currentManualTranslation,
                    isDark = isDark,
                    fontSize = 14.5.sp,
                    testTag = "adjektiv_manual_translation"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            ManualTranslationInputRow(
                currentManualTranslation = currentManualTranslation,
                onSave = onSaveManualTranslation
            )

            // 4. Examples (with wrapping until the level of the turquoise label, then horizontal scrolling)
            val examples = result.examples
            val textColor = GeoOnSurface
            val layoutData = remember(examples, accentColor, textColor, isDark) {
                calculateSentenceListLayout(
                    examples = examples,
                    accentColor = accentColor,
                    textColor = textColor,
                    isDark = isDark
                )
            }

            if (examples.isNotEmpty()) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 7.dp),
                    thickness = 0.8.dp,
                    color = if (isDark) Color(0xFF383838) else Color(0xFFE8E8E8)
                )

                // Upper examples: wrap naturally onto multiple lines
                layoutData.upperExamples.forEachIndexed { index, annotated ->
                    if (index > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Text(
                        text = annotated,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        softWrap = true
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Bottom Row: Adjacent Horizontally Scrolling Text (if active) + Turquoise Adjektiv Label
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (layoutData.adjacentContent != null) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = layoutData.adjacentContent,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                color = GeoOnSurface
                            ),
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.testTag("adjacent_adjektiv_example_text")
                        )
                    }
                    // Horizontal safe margin of at least 12dp between text and label
                    Spacer(modifier = Modifier.width(12.dp))
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Adjektiv pill-style label (Fixed turquoise, white text, 12dp safe margin)
                NounGenderBadge(
                    gender = "Adjektiv",
                    modifier = Modifier.testTag("adjektiv_type_badge")
                )
            }
        }
    }
}

internal data class SentenceListLayoutData(
    val upperExamples: List<AnnotatedString>,
    val adjacentContent: AnnotatedString?,
    val isScrollingActive: Boolean
)

internal fun createSentenceAnnotated(
    ex: WordExample,
    bulletColor: Color,
    accentColor: Color,
    textColor: Color
): AnnotatedString {
    val bullet = "• "
    val cleanSentence = ex.sentence.trim().removePrefix("•").removePrefix("–").removePrefix("-").trim()
    val keyWord = ex.word.trim().removePrefix("•").removePrefix("–").removePrefix("-").trim()

    return buildAnnotatedString {
        withStyle(SpanStyle(color = bulletColor, fontWeight = FontWeight.Bold)) {
            append(bullet)
        }
        if (keyWord.isNotBlank()) {
            val idx = cleanSentence.indexOf(keyWord, ignoreCase = true)
            if (idx >= 0) {
                if (idx > 0) {
                    withStyle(SpanStyle(color = textColor, fontSize = 12.sp)) {
                        append(cleanSentence.substring(0, idx))
                    }
                }
                withStyle(SpanStyle(color = accentColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)) {
                    append(cleanSentence.substring(idx, idx + keyWord.length))
                }
                if (idx + keyWord.length < cleanSentence.length) {
                    withStyle(SpanStyle(color = textColor, fontSize = 12.sp)) {
                        append(cleanSentence.substring(idx + keyWord.length))
                    }
                }
            } else {
                withStyle(SpanStyle(color = accentColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)) {
                    append("$keyWord: ")
                }
                withStyle(SpanStyle(color = textColor, fontSize = 12.sp)) {
                    append(cleanSentence)
                }
            }
        } else {
            withStyle(SpanStyle(color = textColor, fontSize = 12.sp)) {
                append(cleanSentence)
            }
        }
    }
}

internal fun calculateSentenceListLayout(
    examples: List<WordExample>,
    accentColor: Color,
    textColor: Color,
    isDark: Boolean
): SentenceListLayoutData {
    if (examples.isEmpty()) {
        return SentenceListLayoutData(emptyList(), null, false)
    }

    val bulletColor = if (isDark) Color(0xFFB0B0B0) else Color(0xFF666666)

    fun createAnnotated(ex: WordExample): AnnotatedString {
        return createSentenceAnnotated(ex, bulletColor, accentColor, textColor)
    }

    when (examples.size) {
        1 -> {
            val ex = examples[0]
            val s = ex.sentence.trim().removePrefix("•").removePrefix("–").removePrefix("-").trim()
            if (s.length > 75) {
                val splitIdx = findWordBoundarySplit(s, 48)
                val part1 = s.substring(0, splitIdx).trim()
                val part2 = s.substring(splitIdx).trim()

                val upper = listOf(
                    createSentenceAnnotated(
                        WordExample(ex.word, part1),
                        bulletColor,
                        accentColor,
                        textColor
                    )
                )
                val adjacent = buildAnnotatedString {
                    withStyle(SpanStyle(color = textColor, fontSize = 12.sp)) {
                        append("  $part2")
                    }
                }
                return SentenceListLayoutData(upper, adjacent, isScrollingActive = true)
            } else {
                return SentenceListLayoutData(listOf(createAnnotated(ex)), null, isScrollingActive = false)
            }
        }
        2 -> {
            val totalChars = examples[0].sentence.length + examples[1].sentence.length
            if (totalChars <= 70) {
                return SentenceListLayoutData(examples.map { createAnnotated(it) }, null, isScrollingActive = false)
            } else {
                return SentenceListLayoutData(
                    upperExamples = listOf(createAnnotated(examples[0])),
                    adjacentContent = createAnnotated(examples[1]),
                    isScrollingActive = true
                )
            }
        }
        3 -> {
            val firstTwoChars = examples[0].sentence.length + examples[1].sentence.length
            val totalChars = firstTwoChars + examples[2].sentence.length
            if (firstTwoChars <= 80 && totalChars <= 110) {
                return SentenceListLayoutData(examples.map { createAnnotated(it) }, null, isScrollingActive = false)
            } else {
                return SentenceListLayoutData(
                    upperExamples = listOf(createAnnotated(examples[0]), createAnnotated(examples[1])),
                    adjacentContent = createAnnotated(examples[2]),
                    isScrollingActive = true
                )
            }
        }
        else -> {
            val upper = listOf(createAnnotated(examples[0]), createAnnotated(examples[1]))
            val remaining = examples.drop(2)
            val adjacent = buildAnnotatedString {
                remaining.forEachIndexed { index, ex ->
                    if (index > 0) append("    ")
                    append(createAnnotated(ex))
                }
            }
            return SentenceListLayoutData(upper, adjacent, isScrollingActive = true)
        }
    }
}

internal data class NomenListLayoutData(
    val upperExamples: List<AnnotatedString>,
    val adjacentContent: AnnotatedString?,
    val isScrollingActive: Boolean
)

internal fun calculateNomenListLayout(
    examples: List<WordExample>,
    primaryColor: Color,
    textColor: Color,
    isDark: Boolean
): NomenListLayoutData {
    if (examples.isEmpty()) {
        return NomenListLayoutData(emptyList(), null, false)
    }

    val bulletColor = if (isDark) Color(0xFFB0B0B0) else Color(0xFF666666)

    fun createAnnotated(ex: WordExample): AnnotatedString {
        return buildAnnotatedString {
            withStyle(SpanStyle(color = bulletColor, fontWeight = FontWeight.Bold)) {
                append("• ")
            }
            withStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)) {
                append("${ex.word.trim()}: ")
            }
            withStyle(SpanStyle(color = textColor, fontSize = 12.sp)) {
                append(ex.sentence.trim())
            }
        }
    }

    when (examples.size) {
        1 -> {
            val ex = examples[0]
            val s = ex.sentence.trim()
            if (s.length > 75) {
                // Critical Case for single long sentence: text wraps and reaches adjacent level
                val splitIdx = findWordBoundarySplit(s, 48)
                val part1 = s.substring(0, splitIdx).trim()
                val part2 = s.substring(splitIdx).trim()

                val upper = listOf(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = bulletColor, fontWeight = FontWeight.Bold)) {
                            append("• ")
                        }
                        withStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)) {
                            append("${ex.word.trim()}: ")
                        }
                        withStyle(SpanStyle(color = textColor, fontSize = 12.sp)) {
                            append(part1)
                        }
                    }
                )
                val adjacent = buildAnnotatedString {
                    withStyle(SpanStyle(color = textColor, fontSize = 12.sp)) {
                        append("  $part2")
                    }
                }
                return NomenListLayoutData(upper, adjacent, isScrollingActive = true)
            } else {
                // Normal Case: Short text, wraps naturally in upper area, no scrolling
                return NomenListLayoutData(listOf(createAnnotated(ex)), null, isScrollingActive = false)
            }
        }
        2 -> {
            val totalChars = examples[0].sentence.length + examples[1].sentence.length
            if (totalChars <= 70) {
                // Normal Case: both fit in upper area
                return NomenListLayoutData(examples.map { createAnnotated(it) }, null, isScrollingActive = false)
            } else {
                // Critical Case: ex[0] in upper, ex[1] adjacent to label
                return NomenListLayoutData(
                    upperExamples = listOf(createAnnotated(examples[0])),
                    adjacentContent = createAnnotated(examples[1]),
                    isScrollingActive = true
                )
            }
        }
        3 -> {
            val firstTwoChars = examples[0].sentence.length + examples[1].sentence.length
            val totalChars = firstTwoChars + examples[2].sentence.length
            if (firstTwoChars <= 80 && totalChars <= 110) {
                // Normal Case (Short Text): all 3 fit in upper area without scrolling
                return NomenListLayoutData(examples.map { createAnnotated(it) }, null, isScrollingActive = false)
            } else {
                // Critical Case (Long Text): first 2 in upper area (wrapping naturally), 3rd adjacent to label
                return NomenListLayoutData(
                    upperExamples = listOf(createAnnotated(examples[0]), createAnnotated(examples[1])),
                    adjacentContent = createAnnotated(examples[2]),
                    isScrollingActive = true
                )
            }
        }
        else -> {
            // 4 or more examples: first 2 in upper, remaining in adjacent horizontal scroll
            val upper = listOf(createAnnotated(examples[0]), createAnnotated(examples[1]))
            val remaining = examples.drop(2)
            val adjacent = buildAnnotatedString {
                remaining.forEachIndexed { index, ex ->
                    if (index > 0) append("    ")
                    withStyle(SpanStyle(color = bulletColor, fontWeight = FontWeight.Bold)) {
                        append("• ")
                    }
                    withStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)) {
                        append("${ex.word.trim()}: ")
                    }
                    withStyle(SpanStyle(color = textColor, fontSize = 12.sp)) {
                        append(ex.sentence.trim())
                    }
                }
            }
            return NomenListLayoutData(upper, adjacent, isScrollingActive = true)
        }
    }
}

private fun findWordBoundarySplit(text: String, targetLen: Int): Int {
    if (text.length <= targetLen) return text.length
    val spaceAfter = text.indexOf(' ', targetLen)
    val spaceBefore = text.lastIndexOf(' ', targetLen)
    return when {
        spaceBefore != -1 && (targetLen - spaceBefore) <= 15 -> spaceBefore
        spaceAfter != -1 -> spaceAfter
        else -> targetLen
    }
}

@Composable
fun NomenListResultView(
    result: WordDeclensionResult,
    onSpeak: (String, String) -> Unit = { _, _ -> },
    currentlySpokenText: String? = null,
    currentlySpokenId: String? = null,
    currentManualTranslation: String = "",
    onSaveManualTranslation: (String) -> Unit = {},
    inputQuery: String = "",
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val examples = remember(result) { com.example.data.model.resolveWordExamples(result) }

    val primaryColor = if (isDark) Color(0xFF90CAF9) else GeoPrimary
    val textColor = GeoOnSurface
    val layoutData = remember(examples, primaryColor, textColor, isDark) {
        calculateNomenListLayout(examples, primaryColor, textColor, isDark)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("nomen_list_result_card"),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            if (GeoIsBoldTheme) 1.5.dp else 1.dp,
            if (GeoIsBoldTheme) GeoCardRibbonColor.copy(alpha = 0.55f) else GeoBorder
        ),
        colors = CardDefaults.cardColors(containerColor = GeoSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (GeoIsBoldTheme) 2.dp else 0.dp)
    ) {
        if (GeoIsBoldTheme) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.5.dp)
                    .background(Brush.horizontalGradient(GeoBannerGradient))
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            // Top Row: German word(s) + Copy Button & Speaker Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = result.word,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = GeoOnSurface
                        ),
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.testTag("nomen_list_text")
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                val textToCopy = inputQuery.trim().ifBlank { result.word.trim() }
                CopyResultButton(
                    textToCopy = textToCopy,
                    size = 32.dp,
                    iconSize = 17.dp,
                    testTag = "copy_nomen_list_button"
                )

                Spacer(modifier = Modifier.width(6.dp))

                val nomenSpeakerId = "decl_nomen_header_${result.word.hashCode()}"
                val isPlayingNomen = currentlySpokenId != null && currentlySpokenId == nomenSpeakerId
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isPlayingNomen) Color(0xFFFFCDD2) else Color(0xFFEEEEEE))
                        .clickable { onSpeak(result.word, nomenSpeakerId) }
                        .testTag("pronounce_nomen_list_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlayingNomen) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = if (isPlayingNomen) "Stoppen" else "Wörter anhören",
                        tint = if (isPlayingNomen) Color(0xFFD32F2F) else Color(0xFF757575),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Second Row: Pronunciation (IPA) - horizontally scrolling, no wrapping, speaker icon
            val nomenIpaSpeakerId = "decl_nomen_ipa_${result.word.hashCode()}"
            Spacer(modifier = Modifier.height(6.dp))
            IpaPronunciationSection(
                rawIpa = result.ipa,
                fallbackText = result.word,
                textToSpeak = result.word,
                speakerId = nomenIpaSpeakerId,
                onSpeak = onSpeak,
                currentlySpokenId = currentlySpokenId,
                testTag = "nomen_list_ipa_section"
            )

            // Third Row: Translation with colored prefix ("Eng.: " or "الترجمة العربية : ") and right-justification for Arabic
            if (result.meaningEnglish.isNotBlank()) {
                val transLang = LocalTranslationLanguage.current
                val isArabic = isArabicText(result.meaningEnglish) || transLang == "ar"
                val prefix = if (isArabic) "الترجمة العربية : " else "Eng.: "
                Spacer(modifier = Modifier.height(8.dp))
                val rawMeaning = remember(result.meaningEnglish, isArabic) {
                    cleanTranslationText(result.meaningEnglish, isArabic)
                }
                val prefixColor = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
                val annotatedTranslation = remember(rawMeaning, prefix, prefixColor, isDark) {
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = prefixColor, fontWeight = FontWeight.Bold)) {
                            append(prefix)
                        }
                        append(rawMeaning)
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = annotatedTranslation,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.5.sp,
                            color = GeoOnSurfaceVariant
                        ),
                        softWrap = false,
                        modifier = Modifier.testTag("nomen_list_translation")
                    )
                }
            }

            if (currentManualTranslation.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                ManualTranslationDisplay(
                    manualTranslation = currentManualTranslation,
                    isDark = isDark,
                    fontSize = 14.5.sp,
                    testTag = "nomen_list_manual_translation"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            ManualTranslationInputRow(
                currentManualTranslation = currentManualTranslation,
                onSave = onSaveManualTranslation
            )

            // Subtle Horizontal Divider before Examples
            if (examples.isNotEmpty()) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 7.dp),
                    thickness = 0.8.dp,
                    color = if (isDark) Color(0xFF383838) else Color(0xFFE8E8E8)
                )

                // Upper examples: wrap naturally onto multiple lines
                layoutData.upperExamples.forEachIndexed { index, annotated ->
                    if (index > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Text(
                        text = annotated,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        softWrap = true
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Bottom Row: Adjacent Horizontally Scrolling Text (if active) + Wörter / Wort Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (layoutData.adjacentContent != null) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = layoutData.adjacentContent,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                color = GeoOnSurface
                            ),
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.testTag("adjacent_example_text")
                        )
                    }
                    // Horizontal safe margin of at least 12dp between text and badge
                    Spacer(modifier = Modifier.width(12.dp))
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Wörter / Wort badge (Yellow pill-style theme, fixed at bottom-right)
                val isMultipleWords = remember(result.word) { com.example.data.model.isMultipleWordsInput(result.word) }
                val badgeText = if (isMultipleWords) "Wörter" else "Wort"

                NounGenderBadge(
                    gender = badgeText,
                    modifier = Modifier.testTag("nomen_type_badge")
                )
            }
        }
    }
}

@Composable
fun DeclensionResultView(
    result: WordDeclensionResult,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onSpeak: (String, String) -> Unit = { _, _ -> },
    onExport: () -> Unit = {},
    ttsSpeed: Float = 1.0f,
    onTtsSpeedChange: (Float) -> Unit = {},
    currentlySpokenText: String? = null,
    currentlySpokenId: String? = null,
    currentManualTranslation: String = "",
    onSaveManualTranslation: (String) -> Unit = {},
    inputQuery: String = "",
    modifier: Modifier = Modifier
) {
    val isNomenList = remember(result.gender) {
        result.gender.equals("Wort", ignoreCase = true) ||
        result.gender.equals("Wörter", ignoreCase = true) ||
        result.gender.equals("Woerter", ignoreCase = true)
    }
    val isSentence = remember(result.gender, result.word, isNomenList) {
        !isNomenList && (
            result.gender.equals("Satz", ignoreCase = true) ||
            result.gender == "–" ||
            result.word.trim().split(Regex("[\\s,;]+")).filter { it.isNotBlank() }.size >= 2
        )
    }

    if (isNomenList) {
        NomenListResultView(
            result = result,
            onSpeak = onSpeak,
            currentlySpokenText = currentlySpokenText,
            currentlySpokenId = currentlySpokenId,
            currentManualTranslation = currentManualTranslation,
            onSaveManualTranslation = onSaveManualTranslation,
            inputQuery = inputQuery,
            modifier = modifier
        )
        return
    }

    if (isSentence) {
        SentenceResultView(
            result = result,
            onSpeak = onSpeak,
            currentlySpokenText = currentlySpokenText,
            currentlySpokenId = currentlySpokenId,
            currentManualTranslation = currentManualTranslation,
            onSaveManualTranslation = onSaveManualTranslation,
            inputQuery = inputQuery,
            modifier = modifier
        )
        return
    }

    val primaryColor = GeoPrimary
    val onSurfaceColor = GeoOnSurface
    val separatorColor = GeoOnSurfaceVariant

    val plText = remember(result.pluralNoun, result.plural) {
        val direct = result.pluralNoun.trim()
        if (direct.isNotBlank() && direct != "–" && direct != "-") {
            direct
        } else {
            result.plural.rows.firstOrNull { it.caseKey == "nominativ" }?.definite?.trim().orEmpty()
        }
    }
    val genText = remember(result.singular) {
        result.singular.rows.firstOrNull { it.caseKey == "genitiv" }?.definite?.trim().orEmpty()
    }

    val grammaticalDetailsAnnotated = remember(plText, genText, primaryColor, onSurfaceColor, separatorColor) {
        buildAnnotatedString {
            var hasPrev = false
            if (plText.isNotBlank()) {
                withStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.Bold)) {
                    append("Pl.: ")
                }
                withStyle(SpanStyle(color = onSurfaceColor, fontWeight = FontWeight.Medium)) {
                    append(plText)
                }
                hasPrev = true
            }
            if (genText.isNotBlank()) {
                if (hasPrev) {
                    withStyle(SpanStyle(color = separatorColor)) {
                        append("   ·   ")
                    }
                }
                withStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.Bold)) {
                    append("Gen.: ")
                }
                withStyle(SpanStyle(color = onSurfaceColor, fontWeight = FontWeight.Medium)) {
                    append(genText)
                }
            }
        }
    }

    val fullPhraseString = remember(result.genderArticle, result.word) {
        if (result.genderArticle.isNotBlank()) {
            "${result.genderArticle} ${result.word}"
        } else {
            result.word
        }
    }

    val fullWordAnnotated = remember(result.genderArticle, result.word, primaryColor, onSurfaceColor) {
        buildAnnotatedString {
            if (result.genderArticle.isNotBlank()) {
                withStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.Bold)) {
                    append(result.genderArticle)
                    append(" ")
                }
            }
            withStyle(SpanStyle(color = onSurfaceColor, fontWeight = FontWeight.Bold)) {
                append(result.word)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("declension_result_container")
    ) {
        // Redesigned Top Word Display Card (Flexible & Responsive)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
                if (GeoIsBoldTheme) 1.5.dp else 1.dp,
                if (GeoIsBoldTheme) GeoCardRibbonColor.copy(alpha = 0.55f) else GeoBorder
            ),
            colors = CardDefaults.cardColors(containerColor = GeoSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = if (GeoIsBoldTheme) 2.dp else 0.dp)
        ) {
            if (GeoIsBoldTheme) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.5.dp)
                        .background(Brush.horizontalGradient(GeoBannerGradient))
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // Row 1: Main Word + Article (left) and Audio Icon (right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        AutoSizingWordText(
                            text = fullWordAnnotated,
                            rawLength = fullPhraseString.length,
                            modifier = Modifier.fillMaxWidth(),
                            minTextSize = 14f,
                            maxTextSize = 24f
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    val textToCopy = inputQuery.trim().ifBlank { result.word.trim() }
                    CopyResultButton(
                        textToCopy = textToCopy,
                        size = 32.dp,
                        iconSize = 17.dp,
                        testTag = "copy_noun_button"
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Audio Pronunciation Button (consistent 32x32 grey circular style, fixed right)
                    val wordSpeakerId = "decl_main_word_${result.word.hashCode()}"
                    val isPlayingWord = currentlySpokenId != null && currentlySpokenId == wordSpeakerId
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isPlayingWord) Color(0xFFFFCDD2) else Color(0xFFEEEEEE))
                            .clickable { onSpeak(fullPhraseString, wordSpeakerId) }
                            .testTag("pronounce_word_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlayingWord) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isPlayingWord) "Stoppen" else "Aussprache anhören",
                            tint = if (isPlayingWord) Color(0xFFD32F2F) else Color(0xFF757575),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Row 2: Grammatical Details - Plural & Genitiv (Inflection: horizontally scrollable, no wrapping)
                if (plText.isNotBlank() || genText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = grammaticalDetailsAnnotated,
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 14.sp,
                            color = GeoOnSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 19.sp,
                            maxLines = 1,
                            softWrap = false
                        )

                        if (plText.isNotBlank() && plText != "–" && plText != "-") {
                            Spacer(modifier = Modifier.width(6.dp))
                            val pluralSpeakerId = "decl_plural_word_${result.word.hashCode()}"
                            val isPlayingPlural = currentlySpokenId != null && currentlySpokenId == pluralSpeakerId
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (isPlayingPlural) Color(0xFFFFCDD2) else Color(0xFFEEEEEE))
                                    .clickable { onSpeak(plText, pluralSpeakerId) }
                                    .testTag("pronounce_plural_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPlayingPlural) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = if (isPlayingPlural) "Stoppen" else "Plural anhören",
                                    tint = if (isPlayingPlural) Color(0xFFD32F2F) else Color(0xFF757575),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                // Row 3: Pronunciation (IPA) - horizontally scrolling, no wrapping, speaker icon
                val nounIpaSpeakerId = "decl_noun_ipa_${result.word.hashCode()}"
                val fallbackNounText = remember(result.genderArticle, result.word) {
                    if (result.genderArticle.isNotBlank()) "${result.genderArticle} ${result.word}" else result.word
                }
                Spacer(modifier = Modifier.height(6.dp))
                IpaPronunciationSection(
                    rawIpa = result.ipa,
                    fallbackText = fallbackNounText,
                    textToSpeak = fullPhraseString,
                    speakerId = nounIpaSpeakerId,
                    onSpeak = onSpeak,
                    currentlySpokenId = currentlySpokenId,
                    testTag = "noun_ipa_section"
                )

                // Row 4: Translation (English or Arabic with right-justification and distinct prefix color)
                if (result.meaningEnglish.isNotBlank()) {
                    val transLang = LocalTranslationLanguage.current
                    val isDark = LocalIsDarkTheme.current
                    val isArabic = isArabicText(result.meaningEnglish) || transLang == "ar"
                    val prefix = if (isArabic) "الترجمة العربية : " else "Eng.: "
                    val prefixColor = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
                    val meaningText = remember(result.meaningEnglish, isArabic) {
                        cleanTranslationText(result.meaningEnglish, isArabic)
                    }
                    val annotatedMeaning = remember(meaningText, prefix, prefixColor, isDark) {
                        buildAnnotatedString {
                            withStyle(SpanStyle(color = prefixColor, fontWeight = FontWeight.Bold)) {
                                append(prefix)
                            }
                            append(meaningText)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = if (isArabic) Arrangement.End else Arrangement.spacedBy(8.dp)
                    ) {
                        if (!isArabic) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(GeoPrimaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = GeoOnPrimaryContainer
                                )
                            }
                        }
                        Text(
                            text = annotatedMeaning,
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = GeoOnSurface,
                            textAlign = if (isArabic) TextAlign.End else TextAlign.Start,
                            maxLines = 2,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (isArabic) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(GeoPrimaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = GeoOnPrimaryContainer
                                )
                            }
                        }
                    }
                }

                if (currentManualTranslation.isNotBlank()) {
                    val isDark = LocalIsDarkTheme.current
                    Spacer(modifier = Modifier.height(4.dp))
                    ManualTranslationDisplay(
                        manualTranslation = currentManualTranslation,
                        isDark = isDark,
                        fontSize = 14.sp,
                        textColor = GeoOnSurface,
                        testTag = "noun_manual_translation"
                    )
                }

                // Row 4: Manual translation input row (appears only in search result card)
                Spacer(modifier = Modifier.height(8.dp))
                ManualTranslationInputRow(
                    currentManualTranslation = currentManualTranslation,
                    onSave = onSaveManualTranslation
                )

                // Row 5: Example sentence (left) and colored Gender label (bottom-right)
                val firstExample = remember(result.examples) {
                    result.examples.firstOrNull()?.sentence?.trim().orEmpty()
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (firstExample.isNotBlank()) {
                        Box(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = "• $firstExample",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.5.sp,
                                    color = GeoOnSurfaceVariant
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    NounGenderBadge(gender = result.gender)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Geometric Segmented Tabs: Singular / Plural / Both (single-line no-wrap)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(GeoSurfaceVariant)
                .border(1.dp, GeoBorder, RoundedCornerShape(20.dp))
                .padding(4.dp)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier
                            .tabIndicatorOffset(tabPositions[selectedTab])
                            .clip(RoundedCornerShape(16.dp)),
                        color = GeoPrimary,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { onTabSelected(0) },
                    selectedContentColor = GeoOnPrimaryContainer,
                    unselectedContentColor = GeoOnSurfaceVariant,
                    text = {
                        Text(
                            text = "Singular",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    },
                    modifier = Modifier.testTag("tab_singular")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { onTabSelected(1) },
                    selectedContentColor = GeoOnPrimaryContainer,
                    unselectedContentColor = GeoOnSurfaceVariant,
                    text = {
                        Text(
                            text = "Plural",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    },
                    modifier = Modifier.testTag("tab_plural")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { onTabSelected(2) },
                    selectedContentColor = GeoOnPrimaryContainer,
                    unselectedContentColor = GeoOnSurfaceVariant,
                    text = {
                        Text(
                            text = "Beiden",
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    },
                    modifier = Modifier.testTag("tab_both")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tables display with Geometric Grid
        when (selectedTab) {
            0 -> DeclensionTableCard(group = result.singular, isPlural = false, onSpeak = onSpeak, currentlySpokenText = currentlySpokenText, currentlySpokenId = currentlySpokenId)
            1 -> DeclensionTableCard(group = result.plural, isPlural = true, onSpeak = onSpeak, currentlySpokenText = currentlySpokenText, currentlySpokenId = currentlySpokenId)
            2 -> {
                DeclensionTableCard(group = result.singular, isPlural = false, onSpeak = onSpeak, currentlySpokenText = currentlySpokenText, currentlySpokenId = currentlySpokenId)
                Spacer(modifier = Modifier.height(14.dp))
                DeclensionTableCard(group = result.plural, isPlural = true, onSpeak = onSpeak, currentlySpokenText = currentlySpokenText, currentlySpokenId = currentlySpokenId)
            }
        }
    }
}

@Composable
fun DeclensionTableCard(
    group: DeclensionTableGroup,
    isPlural: Boolean = false,
    onSpeak: (String, String) -> Unit = { _, _ -> },
    currentlySpokenText: String? = null,
    currentlySpokenId: String? = null,
    modifier: Modifier = Modifier
) {
    val headerBg = if (isPlural) GeoSecondaryContainer else GeoPrimaryContainer
    val headerTextColor = if (isPlural) GeoOnSecondaryContainer else GeoOnPrimaryContainer

    // Outer Container with Geometric Balance: white card, crisp border, rounded-2xl
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            if (GeoIsBoldTheme) 1.5.dp else 1.dp,
            if (GeoIsBoldTheme) GeoCardRibbonColor.copy(alpha = 0.55f) else GeoBorder
        ),
        colors = CardDefaults.cardColors(containerColor = GeoSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (GeoIsBoldTheme) 2.dp else 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Section Header Banner: uppercase, tracking-wider, e.g. "SINGULAR" or "PLURAL"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (GeoIsBoldTheme) Modifier.background(Brush.horizontalGradient(GeoBannerGradient))
                        else Modifier.background(headerBg)
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = group.numberDe.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = if (GeoIsBoldTheme) Color.White else headerTextColor,
                    maxLines = 1,
                    softWrap = false
                )
            }

            // Declension Geometric Grid with horizontal scroll for long German nouns
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val cardWidth = maxWidth
                val maxKasusTextLen = group.rows.maxOfOrNull { maxOf(it.caseNameDe.length, it.caseQuestionDe.length) } ?: 0
                val kasusWidth = maxOf(120.dp, (maxKasusTextLen * 10f + 36f).dp)
                val maxDefiniteLen = group.rows.maxOfOrNull { it.definite.length } ?: 0
                val maxIndefiniteLen = group.rows.maxOfOrNull { it.indefinite.length } ?: 0
                val maxNoArticleLen = group.rows.maxOfOrNull { it.noArticle.length } ?: 0

                val remainingWidth = (cardWidth - kasusWidth - 3.dp).coerceAtLeast(300.dp)
                val baseColWidth = remainingWidth / 3f

                val colDefWidth = maxOf(baseColWidth, (maxDefiniteLen * 10f + 56f).dp, 120.dp)
                val colIndefWidth = maxOf(baseColWidth, (maxIndefiniteLen * 10f + 56f).dp, 120.dp)
                val colNoArtWidth = maxOf(baseColWidth, (maxNoArticleLen * 10f + 56f).dp, 108.dp)
                val totalDeclensionTableWidth = kasusWidth + 3.dp + colDefWidth + colIndefWidth + colNoArtWidth

                val scrollState = rememberScrollState()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                ) {
                    Column(modifier = Modifier.width(totalDeclensionTableWidth)) {
                        // Table Column Titles Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                                .background(GeoHeaderKasus)
                                .border(BorderStroke(1.dp, GeoBorder)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TableCellHeader(
                                title = "Kasus\n(Fall)",
                                modifier = Modifier.width(kasusWidth)
                            )
                            TableGridDivider()
                            TableCellHeader(
                                title = "Bestimmt\n(mit Artikel)",
                                modifier = Modifier.width(colDefWidth)
                            )
                            TableGridDivider()
                            TableCellHeader(
                                title = "Unbestimmt\n(ein / kein)",
                                modifier = Modifier.width(colIndefWidth)
                            )
                            TableGridDivider()
                            TableCellHeader(
                                title = "Ohne\n(Nullartikel)",
                                modifier = Modifier.width(colNoArtWidth)
                            )
                        }

                        // Table Rows with grid dividers
                        group.rows.forEachIndexed { index, row ->
                            val isLast = index == group.rows.size - 1
                            DeclensionGeometricRow(
                                row = row,
                                kasusWidth = kasusWidth,
                                colDefWidth = colDefWidth,
                                colIndefWidth = colIndefWidth,
                                colNoArtWidth = colNoArtWidth,
                                showBottomBorder = !isLast,
                                isPlural = isPlural,
                                onSpeak = onSpeak,
                                currentlySpokenText = currentlySpokenText,
                                currentlySpokenId = currentlySpokenId
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TableCellHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = GeoPrimary,
            textAlign = TextAlign.Center,
            lineHeight = 13.sp,
            letterSpacing = 0.3.sp
        )
    }
}

@Composable
fun DeclensionGeometricRow(
    row: DeclensionCaseRow,
    kasusWidth: Dp = 104.dp,
    colDefWidth: Dp = 95.dp,
    colIndefWidth: Dp = 95.dp,
    colNoArtWidth: Dp = 85.dp,
    showBottomBorder: Boolean = true,
    isPlural: Boolean = false,
    onSpeak: (String, String) -> Unit = { _, _ -> },
    currentlySpokenText: String? = null,
    currentlySpokenId: String? = null
) {
    val prefix = if (isPlural) "pl" else "sg"
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .background(GeoSurface),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Case Name (Kasus cell) - Dynamic & Safe: wrap_content, paddingEnd 8dp, min 10sp, max 16sp
            Box(
                modifier = Modifier
                    .width(kasusWidth)
                    .fillMaxHeight()
                    .padding(vertical = 6.dp, horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(horizontal = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    DynamicTextView(
                        text = row.caseNameDe,
                        minTextSize = 10f,
                        maxTextSize = 14f,
                        paddingStart = 4.dp,
                        paddingEnd = 8.dp,
                        fontWeight = FontWeight.Bold,
                        color = GeoOnSurface,
                        textAlign = TextAlign.Center,
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    DynamicTextView(
                        text = row.caseQuestionDe,
                        minTextSize = 10f, // Exact user requirement for question mark case (e.g. Wem?)
                        maxTextSize = 16f, // Exact user requirement: 10sp to 16sp
                        paddingStart = 4.dp,
                        paddingEnd = 8.dp, // Ensures the '?' has dedicated space and is never cut off
                        color = GeoOutline,
                        textAlign = TextAlign.Center,
                        textStyle = MaterialTheme.typography.labelSmall
                    )
                }
            }

            TableGridDivider()

            // Definite (with Audio Icon)
            DeclensionCell(
                text = row.definite,
                width = colDefWidth,
                speakerId = "cell_${prefix}_${row.caseNameDe}_def",
                onSpeak = onSpeak,
                isBold = true,
                textColor = GeoOnSurface,
                currentlySpokenText = currentlySpokenText,
                currentlySpokenId = currentlySpokenId
            )

            TableGridDivider()

            // Indefinite (with Audio Icon)
            DeclensionCell(
                text = row.indefinite,
                width = colIndefWidth,
                speakerId = "cell_${prefix}_${row.caseNameDe}_indef",
                onSpeak = onSpeak,
                isBold = false,
                textColor = GeoOnSurface,
                currentlySpokenText = currentlySpokenText,
                currentlySpokenId = currentlySpokenId
            )

            TableGridDivider()

            // Without Article (with Audio Icon)
            DeclensionCell(
                text = row.noArticle,
                width = colNoArtWidth,
                speakerId = "cell_${prefix}_${row.caseNameDe}_noart",
                onSpeak = onSpeak,
                isBold = false,
                textColor = GeoOnSurfaceVariant,
                currentlySpokenText = currentlySpokenText,
                currentlySpokenId = currentlySpokenId
            )
        }

        if (showBottomBorder) {
            HorizontalDivider(thickness = 1.dp, color = GeoBorder)
        }
    }
}

@Composable
private fun DeclensionCell(
    text: String,
    width: Dp,
    speakerId: String = "",
    onSpeak: (String, String) -> Unit,
    isBold: Boolean = false,
    textColor: Color = GeoOnSurface,
    currentlySpokenText: String? = null,
    currentlySpokenId: String? = null
) {
    val hasContent = text.isNotBlank() && text != "-" && text != "–" && text != "—"
    val isPlayingCell = hasContent && currentlySpokenId != null && currentlySpokenId == speakerId

    Box(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .padding(vertical = 7.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (hasContent) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.weight(1f, fill = false),
                    contentAlignment = Alignment.Center
                ) {
                    DynamicTextView(
                        text = text,
                        minTextSize = 8f,
                        maxTextSize = 15f,
                        paddingStart = 4.dp,
                        paddingEnd = 8.dp,
                        fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal,
                        color = textColor,
                        textAlign = TextAlign.Center,
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(if (isPlayingCell) Color(0xFFFFCDD2) else Color(0xFFEEEEEE))
                        .clickable { onSpeak(text, speakerId) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlayingCell) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = if (isPlayingCell) "Stop $text" else "Speak $text",
                        tint = if (isPlayingCell) Color(0xFFD32F2F) else Color(0xFF9E9E9E),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        } else {
            DynamicTextView(
                text = text,
                minTextSize = 8f,
                maxTextSize = 15f,
                paddingStart = 4.dp,
                paddingEnd = 8.dp,
                fontWeight = FontWeight.Normal,
                color = GeoOnSurfaceVariant,
                textAlign = TextAlign.Center,
                textStyle = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun TableGridDivider() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(1.dp)
            .background(GeoBorder)
    )
}
