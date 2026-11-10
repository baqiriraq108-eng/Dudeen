package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GeoOnSurface
import com.example.ui.theme.GeoOnSurfaceVariant
import com.example.ui.theme.GeoPrimary
import com.example.util.GermanIpaTranscriber

/**
 * Pronunciation (Aussprache) section in European/German dictionary style (IPA).
 * Displays full word pronunciation in square brackets [ ... ] with stress mark ˈ and length ː.
 * Supports horizontal scrolling, no text wrapping, highlighted phonetic symbols, and audio speaker button.
 */
@Composable
fun IpaPronunciationSection(
    rawIpa: String?,
    fallbackText: String,
    textToSpeak: String,
    speakerId: String,
    onSpeak: (String, String) -> Unit,
    currentlySpokenId: String?,
    modifier: Modifier = Modifier,
    testTag: String = "ipa_pronunciation_section"
) {
    val formattedIpa = remember(rawIpa, fallbackText) {
        GermanIpaTranscriber.formatIpa(rawIpa, fallbackText)
    }

    if (formattedIpa.isBlank()) return

    val isPlaying = currentlySpokenId != null && currentlySpokenId == speakerId

    val onSurfaceVariant = GeoOnSurfaceVariant
    val primaryColor = GeoPrimary
    val onSurface = GeoOnSurface

    val annotatedIpa = remember(formattedIpa, onSurfaceVariant, primaryColor, onSurface) {
        buildAnnotatedString {
            // Label "Aussprache: "
            withStyle(
                SpanStyle(
                    color = onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            ) {
                append("Aussprache: ")
            }

            // Split into parts to format brackets and phonetic symbols nicely
            // We highlight phonetic symbols in Primary color while brackets are in normal text color
            var inBrackets = false
            for (ch in formattedIpa) {
                when (ch) {
                    '[' -> {
                        withStyle(
                            SpanStyle(
                                color = onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.5.sp
                            )
                        ) {
                            append("[")
                        }
                        inBrackets = true
                    }
                    ']' -> {
                        withStyle(
                            SpanStyle(
                                color = onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.5.sp
                            )
                        ) {
                            append("]")
                        }
                        inBrackets = false
                    }
                    else -> {
                        if (inBrackets) {
                            withStyle(
                                SpanStyle(
                                    color = primaryColor,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.5.sp
                                )
                            ) {
                                append(ch.toString())
                            }
                        } else {
                            withStyle(
                                SpanStyle(
                                    color = onSurface,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            ) {
                                append(ch.toString())
                            }
                        }
                    }
                }
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Horizontally scrolling text with strictly no wrapping
        Box(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = annotatedIpa,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                ),
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.testTag("${testTag}_text")
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Speaker icon (🔊) next to pronunciation section to pronounce the full word
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (isPlaying) Color(0xFFFFCDD2) else Color(0xFFEEEEEE))
                .clickable { onSpeak(textToSpeak, speakerId) }
                .testTag("${testTag}_speaker_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = if (isPlaying) "Stoppen" else "Aussprache anhören",
                tint = if (isPlaying) Color(0xFFD32F2F) else Color(0xFF757575),
                modifier = Modifier.size(15.dp)
            )
        }
    }
}
