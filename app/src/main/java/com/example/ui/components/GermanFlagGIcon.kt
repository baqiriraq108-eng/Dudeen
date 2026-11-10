package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalIsDarkTheme

/**
 * A completely flat, bold capital 'G' icon representing the Grammar (Grammatik) feature.
 * Features the German national flag tricolor split horizontally in thirds:
 * - Top third: Black
 * - Middle third: Red
 * - Bottom third: Yellow/Gold
 * Matches the exact 24.dp size and visual weight of the adjacent TopAppBar action icons.
 */
@Composable
fun GermanFlagGIcon(
    modifier: Modifier = Modifier,
    isDark: Boolean = LocalIsDarkTheme.current
) {
    // In dark mode, pure black is subtly lifted to a dark charcoal (#383838)
    // so the top third remains clearly distinct against dark app bar backgrounds.
    val blackColor = if (isDark) Color(0xFF383838) else Color(0xFF000000)
    val redColor = Color(0xFFDD0000)
    val yellowColor = Color(0xFFFFCC00)

    Canvas(
        modifier = modifier.size(24.dp)
    ) {
        val sizePx = size.minDimension
        val textPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            isDither = true
            typeface = android.graphics.Typeface.create(
                android.graphics.Typeface.SANS_SERIF,
                android.graphics.Typeface.BOLD
            )
            textSize = sizePx * 0.95f
            textAlign = android.graphics.Paint.Align.CENTER
        }

        val textBounds = android.graphics.Rect()
        textPaint.getTextBounds("G", 0, 1, textBounds)

        val glyphHeight = textBounds.height().toFloat()
        // Center the glyph vertically inside the 24.dp frame
        val glyphTop = (size.height - glyphHeight) / 2f
        val glyphBottom = glyphTop + glyphHeight

        // Exact horizontal tri-color gradient over the letter's height (top: black, mid: red, bot: yellow)
        val shader = android.graphics.LinearGradient(
            0f,
            glyphTop,
            0f,
            glyphBottom,
            intArrayOf(
                blackColor.toArgb(),
                blackColor.toArgb(),
                redColor.toArgb(),
                redColor.toArgb(),
                yellowColor.toArgb(),
                yellowColor.toArgb()
            ),
            floatArrayOf(0.0f, 0.333f, 0.334f, 0.666f, 0.667f, 1.0f),
            android.graphics.Shader.TileMode.CLAMP
        )
        textPaint.shader = shader

        val x = size.width / 2f
        val y = glyphTop - textBounds.top

        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawText("G", x, y, textPaint)
        }
    }
}
