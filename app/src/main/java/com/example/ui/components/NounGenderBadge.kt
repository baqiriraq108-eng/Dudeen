package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalIsDarkTheme
import java.util.Locale

@Composable
fun NounGenderBadge(
    gender: String,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val normalized = gender.lowercase(Locale.ROOT).trim()

    val label = when {
        normalized.contains("mask") || normalized == "der" || normalized == "m" -> "Maskulin"
        normalized.contains("fem") || normalized == "die" || normalized == "f" -> "Feminin"
        normalized.contains("neu") || normalized == "das" || normalized == "n" -> "Neutrum"
        normalized.contains("plur") -> "Plural"
        normalized == "verb" || normalized.contains("verb") -> "Verb"
        normalized.contains("adj") -> "Adjektiv"
        normalized == "wort" -> "Wort"
        normalized == "wörter" || normalized == "woerter" -> "Wörter"
        normalized == "satz" || normalized == "sentence" || normalized == "–" || normalized == "-" -> "Satz"
        else -> gender.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
    }

    // Standardized Pill Colors:
    // Maskulin -> Blue (#1565C0 -> #0D47A1)
    // Feminin -> Pink/Red (#C2185B -> #880E4F)
    // Neutrum -> Lighter Beige (#D4BF9B -> #C4AD87)
    // Verb -> Slate Gray (#546E7A -> #455A64)
    // Adjektiv -> Turquoise (#00897B -> #00695C)
    // Satz -> Royal Purple (#5E35B1 -> #4527A0)
    // Wort / Wörter -> Rich Orange (#EA580C -> #C2410C)
    val (bgBrush, borderColor) = when (label) {
        "Maskulin" -> {
            val brush = if (isDark) {
                Brush.horizontalGradient(listOf(Color(0xFF1565C0), Color(0xFF0D47A1)))
            } else {
                Brush.horizontalGradient(listOf(Color(0xFF1E88E5), Color(0xFF1565C0)))
            }
            val border = if (isDark) Color(0xFF42A5F5).copy(alpha = 0.5f) else Color(0xFF1976D2).copy(alpha = 0.5f)
            brush to border
        }
        "Feminin" -> {
            val brush = if (isDark) {
                Brush.horizontalGradient(listOf(Color(0xFFC2185B), Color(0xFF880E4F)))
            } else {
                Brush.horizontalGradient(listOf(Color(0xFFE91E63), Color(0xFFC2185B)))
            }
            val border = if (isDark) Color(0xFFF48FB1).copy(alpha = 0.5f) else Color(0xFFD81B60).copy(alpha = 0.5f)
            brush to border
        }
        "Neutrum" -> {
            // Lighter Beige (#D4BF9B -> #C4AD87)
            val brush = if (isDark) {
                Brush.horizontalGradient(listOf(Color(0xFF6B583E), Color(0xFF52432E)))
            } else {
                Brush.horizontalGradient(listOf(Color(0xFFD4BF9B), Color(0xFFC4AD87)))
            }
            val border = if (isDark) Color(0xFF9E8460).copy(alpha = 0.6f) else Color(0xFFB89E78).copy(alpha = 0.7f)
            brush to border
        }
        "Verb" -> {
            val brush = if (isDark) {
                Brush.horizontalGradient(listOf(Color(0xFF37474F), Color(0xFF263238)))
            } else {
                Brush.horizontalGradient(listOf(Color(0xFF546E7A), Color(0xFF455A64)))
            }
            val border = if (isDark) Color(0xFF78909C).copy(alpha = 0.5f) else Color(0xFF546E7A).copy(alpha = 0.5f)
            brush to border
        }
        "Adjektiv" -> {
            val brush = if (isDark) {
                Brush.horizontalGradient(listOf(Color(0xFF00695C), Color(0xFF004D40)))
            } else {
                Brush.horizontalGradient(listOf(Color(0xFF00897B), Color(0xFF00695C)))
            }
            val border = if (isDark) Color(0xFF4DB6AC).copy(alpha = 0.5f) else Color(0xFF00796B).copy(alpha = 0.5f)
            brush to border
        }
        "Satz" -> {
            // Royal Purple (#5E35B1 -> #4527A0) - Harmonized with Nouns & Adjectives
            val brush = if (isDark) {
                Brush.horizontalGradient(listOf(Color(0xFF4527A0), Color(0xFF311B92)))
            } else {
                Brush.horizontalGradient(listOf(Color(0xFF5E35B1), Color(0xFF4527A0)))
            }
            val border = if (isDark) Color(0xFF7E57C2).copy(alpha = 0.6f) else Color(0xFF5E35B1).copy(alpha = 0.7f)
            brush to border
        }
        "Plural" -> {
            val brush = if (isDark) {
                Brush.horizontalGradient(listOf(Color(0xFF7B1FA2), Color(0xFF4A148C)))
            } else {
                Brush.horizontalGradient(listOf(Color(0xFF9C27B0), Color(0xFF7B1FA2)))
            }
            val border = if (isDark) Color(0xFFCE93D8).copy(alpha = 0.5f) else Color(0xFFBA68C8).copy(alpha = 0.6f)
            brush to border
        }
        "Wort", "Wörter" -> {
            // Rich Orange (#EA580C -> #C2410C) - Harmonized saturation
            val brush = if (isDark) {
                Brush.horizontalGradient(listOf(Color(0xFFC2410C), Color(0xFF9A3412)))
            } else {
                Brush.horizontalGradient(listOf(Color(0xFFEA580C), Color(0xFFC2410C)))
            }
            val border = if (isDark) Color(0xFFEA580C).copy(alpha = 0.6f) else Color(0xFFF97316).copy(alpha = 0.7f)
            brush to border
        }
        else -> {
            val brush = if (isDark) {
                Brush.horizontalGradient(listOf(Color(0xFFC2410C), Color(0xFF9A3412)))
            } else {
                Brush.horizontalGradient(listOf(Color(0xFFEA580C), Color(0xFFC2410C)))
            }
            val border = if (isDark) Color(0xFFEA580C).copy(alpha = 0.6f) else Color(0xFFF97316).copy(alpha = 0.7f)
            brush to border
        }
    }

    val badgeTextColor = Color.White

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgBrush)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .testTag("noun_gender_badge"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.4.sp,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.35f),
                    offset = Offset(0f, 1f),
                    blurRadius = 1.5f
                )
            ),
            color = badgeTextColor,
            maxLines = 1,
            softWrap = false
        )
    }
}

