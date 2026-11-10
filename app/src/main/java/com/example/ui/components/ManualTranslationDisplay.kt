package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GeoOnSurfaceVariant
import com.example.util.isArabicText
import com.example.util.parseManualTranslations

/**
 * Displays manual translations with the prefix abbreviation ("Alt.: ") expressed ONLY ONCE,
 * with each manual translation arranged neatly one on top of the other.
 */
@Composable
fun ManualTranslationDisplay(
    manualTranslation: String,
    modifier: Modifier = Modifier,
    isDark: Boolean = false,
    fontSize: TextUnit = 14.5.sp,
    textColor: Color = GeoOnSurfaceVariant,
    testTag: String? = null
) {
    val items = remember(manualTranslation) {
        parseManualTranslations(manualTranslation)
    }
    if (items.isEmpty()) return

    val altPrefixColor = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)
    val calculatedLineHeight = (fontSize.value * 1.4f).sp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.Start
    ) {
        // Abbreviation expressed only once
        Text(
            text = "Alt.: ",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = altPrefixColor,
                lineHeight = calculatedLineHeight
            )
        )

        // Multiple translations arranged one above the other
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items.forEach { item ->
                val isAr = isArabicText(item)
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = fontSize,
                        fontWeight = FontWeight.Normal,
                        color = textColor,
                        lineHeight = calculatedLineHeight,
                        textAlign = if (isAr) TextAlign.End else TextAlign.Start,
                        textDirection = TextDirection.Content
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    softWrap = true
                )
            }
        }
    }
}
