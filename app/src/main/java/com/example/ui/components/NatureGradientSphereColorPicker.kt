package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.LocalAppLanguage
import com.example.util.CustomColorManager
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Real Color Engine for the Nature-Inspired Gradient Sphere System.
 * Implements precise bidirectional conversions between HEX, RGB, HSL, and CMYK.
 */
object NatureColorEngine {

    data class HslColor(
        val hue: Float, // 0..360
        val saturation: Float, // 0..1
        val lightness: Float // 0..1
    ) {
        val rgb: Triple<Int, Int, Int> get() = hslToRgb(hue, saturation, lightness)
        val hex: String get() = hslToHex(hue, saturation, lightness)
        val composeColor: Color get() {
            val (r, g, b) = rgb
            return Color(r, g, b)
        }
        val cmyk: List<Int> get() {
            val (r, g, b) = rgb
            return rgbToCmyk(r, g, b)
        }
    }

    fun hslToRgb(h: Float, s: Float, l: Float): Triple<Int, Int, Int> {
        val c = (1f - kotlin.math.abs(2f * l - 1f)) * s
        val hPrime = ((h % 360f + 360f) % 360f) / 60f
        val x = c * (1f - kotlin.math.abs((hPrime % 2f) - 1f))
        val m = l - c / 2f
        val (rPrime, gPrime, bPrime) = when {
            hPrime < 1f -> Triple(c, x, 0f)
            hPrime < 2f -> Triple(x, c, 0f)
            hPrime < 3f -> Triple(0f, c, x)
            hPrime < 4f -> Triple(0f, x, c)
            hPrime < 5f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        val r = ((rPrime + m) * 255f).roundToInt().coerceIn(0, 255)
        val g = ((gPrime + m) * 255f).roundToInt().coerceIn(0, 255)
        val b = ((bPrime + m) * 255f).roundToInt().coerceIn(0, 255)
        return Triple(r, g, b)
    }

    fun rgbToHsl(r: Int, g: Int, b: Int): HslColor {
        val rNorm = r / 255f
        val gNorm = g / 255f
        val bNorm = b / 255f
        val max = maxOf(rNorm, gNorm, bNorm)
        val min = minOf(rNorm, gNorm, bNorm)
        val delta = max - min
        val l = (max + min) / 2f
        val s = if (delta <= 0.0001f) 0f else (delta / (1f - kotlin.math.abs(2f * l - 1f))).coerceIn(0f, 1f)
        val h = when {
            delta <= 0.0001f -> 0f
            max == rNorm -> (((gNorm - bNorm) / delta) % 6f) * 60f
            max == gNorm -> (((bNorm - rNorm) / delta) + 2f) * 60f
            else -> (((rNorm - gNorm) / delta) + 4f) * 60f
        }.let { if (it < 0f) it + 360f else it }
        return HslColor(h.coerceIn(0f, 360f), s.coerceIn(0f, 1f), l.coerceIn(0f, 1f))
    }

    fun hslToHex(h: Float, s: Float, l: Float): String {
        val (r, g, b) = hslToRgb(h, s, l)
        return String.format(Locale.ROOT, "#%02X%02X%02X", r, g, b)
    }

    fun hexToHsl(hex: String): HslColor {
        val clean = hex.trim().removePrefix("#")
        return try {
            val num = clean.toLong(16)
            val (r, g, b) = when (clean.length) {
                6 -> Triple(((num shr 16) and 0xFF).toInt(), ((num shr 8) and 0xFF).toInt(), (num and 0xFF).toInt())
                3 -> {
                    val r4 = ((num shr 8) and 0xF).toInt()
                    val g4 = ((num shr 4) and 0xF).toInt()
                    val b4 = (num and 0xF).toInt()
                    Triple(r4 or (r4 shl 4), g4 or (g4 shl 4), b4 or (b4 shl 4))
                }
                8 -> Triple(((num shr 16) and 0xFF).toInt(), ((num shr 8) and 0xFF).toInt(), (num and 0xFF).toInt())
                else -> Triple(229, 57, 53) // Default bright vivid red
            }
            rgbToHsl(r, g, b)
        } catch (_: Exception) {
            HslColor(0f, 1f, 0.5f) // Default bright red
        }
    }

    fun rgbToCmyk(r: Int, g: Int, b: Int): List<Int> {
        val rNorm = r / 255f
        val gNorm = g / 255f
        val bNorm = b / 255f
        val k = 1f - maxOf(rNorm, gNorm, bNorm)
        if (k >= 0.999f) return listOf(0, 0, 0, 100)
        val c = ((1f - rNorm - k) / (1f - k) * 100f).roundToInt().coerceIn(0, 100)
        val m = ((1f - gNorm - k) / (1f - k) * 100f).roundToInt().coerceIn(0, 100)
        val y = ((1f - bNorm - k) / (1f - k) * 100f).roundToInt().coerceIn(0, 100)
        val kPct = (k * 100f).roundToInt().coerceIn(0, 100)
        return listOf(c, m, y, kPct)
    }

    fun cmykToRgb(c: Float, m: Float, y: Float, k: Float): Triple<Int, Int, Int> {
        val cNorm = (c / 100f).coerceIn(0f, 1f)
        val mNorm = (m / 100f).coerceIn(0f, 1f)
        val yNorm = (y / 100f).coerceIn(0f, 1f)
        val kNorm = (k / 100f).coerceIn(0f, 1f)
        val r = (255f * (1f - cNorm) * (1f - kNorm)).roundToInt().coerceIn(0, 255)
        val g = (255f * (1f - mNorm) * (1f - kNorm)).roundToInt().coerceIn(0, 255)
        val b = (255f * (1f - yNorm) * (1f - kNorm)).roundToInt().coerceIn(0, 255)
        return Triple(r, g, b)
    }

    /**
     * Generates 5 harmonious color groups based dynamically on the selected color.
     */
    fun generateHarmonyPalettes(current: HslColor): List<List<String>> {
        val h = current.hue
        val s = current.saturation.coerceIn(0.4f, 1.0f)
        val l = current.lightness.coerceIn(0.35f, 0.65f)

        // 1. Analogous Flora Harmony
        val g1 = listOf(
            hslToHex((h - 30f + 360f) % 360f, (s * 1.05f).coerceIn(0.4f, 1f), (l * 0.95f).coerceIn(0.3f, 0.65f)),
            hslToHex(h, s, l),
            hslToHex((h + 30f) % 360f, (s * 0.95f).coerceIn(0.4f, 1f), (l * 1.05f).coerceIn(0.35f, 0.7f)),
            hslToHex((h + 60f) % 360f, s, (l * 1.15f).coerceIn(0.4f, 0.75f))
        )

        // 2. Complementary / Vibrant Violet-Pink Accents
        val g2 = listOf(
            hslToHex((h + 135f) % 360f, 0.85f, 0.52f),
            hslToHex((h + 160f) % 360f, 0.90f, 0.58f),
            hslToHex((h + 180f) % 360f, 0.95f, 0.50f),
            hslToHex((h + 205f) % 360f, 0.80f, 0.62f)
        )

        // 3. Triadic Fresh Blue & Cyan Harmonies
        val g3 = listOf(
            hslToHex(h, s, l),
            hslToHex((h + 120f) % 360f, 0.88f, 0.52f),
            hslToHex((h + 240f) % 360f, 0.90f, 0.52f),
            hslToHex((h + 180f) % 360f, 0.85f, 0.56f)
        )

        // 4. Warm Sun & Terracotta Complementary Harmonies
        val g4 = listOf(
            hslToHex(35f, 0.95f, 0.52f),
            hslToHex(15f, 0.92f, 0.55f),
            hslToHex((h + 220f) % 360f, 0.85f, 0.58f),
            hslToHex((h + 260f) % 360f, 0.80f, 0.52f)
        )

        // 5. Earthy Fresh & Sage Accents
        val g5 = listOf(
            hslToHex(145f, 0.65f, 0.42f),
            hslToHex(165f, 0.70f, 0.48f),
            hslToHex(195f, 0.75f, 0.52f),
            hslToHex(215f, 0.80f, 0.55f)
        )

        return listOf(g1, g2, g3, g4, g5)
    }
}

/**
 * 3D Luminous Gradient Sphere Canvas Component.
 * Changes appearance dynamically according to the selected color system (RGB, HSL, HEX, CMYK):
 * - RGB: Additive color model with Red, Green, Blue key corner radiance.
 * - HSL: Vivid organic spectrum with saturation and lightness gradient mapping.
 * - HEX: Crisp pure hue chroma wheel without dark vignettes.
 * - CMYK: Subtractive model highlighting Cyan, Magenta, Yellow, Key interactions.
 */
@Composable
fun GradientSphereCanvas(
    currentHsl: NatureColorEngine.HslColor,
    modeIndex: Int, // 0=RGB, 1=HSL, 2=HEX, 3=CMYK
    onColorChanged: (hue: Float, saturation: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    fun updateFromOffset(offset: Offset, sizePx: Float) {
                        val cx = sizePx / 2f
                        val cy = sizePx / 2f
                        val radius = (sizePx / 2f) * 0.84f
                        val dx = offset.x - cx
                        val dy = offset.y - cy
                        val dist = hypot(dx, dy).coerceIn(0f, radius)
                        val angleRad = atan2(dy, dx)
                        val angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat().let {
                            if (it < 0f) it + 360f else it
                        }
                        val sat = (dist / radius).coerceIn(0.01f, 1f)
                        onColorChanged(angleDeg, sat)
                    }

                    detectTapGestures(
                        onTap = { offset ->
                            updateFromOffset(offset, size.width.toFloat())
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val sizePx = size.width.toFloat()
                            val cx = sizePx / 2f
                            val cy = sizePx / 2f
                            val radius = (sizePx / 2f) * 0.84f
                            val dx = offset.x - cx
                            val dy = offset.y - cy
                            val dist = hypot(dx, dy).coerceIn(0f, radius)
                            val angleRad = atan2(dy, dx)
                            val angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat().let {
                                if (it < 0f) it + 360f else it
                            }
                            val sat = (dist / radius).coerceIn(0.01f, 1f)
                            onColorChanged(angleDeg, sat)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val sizePx = size.width.toFloat()
                            val cx = sizePx / 2f
                            val cy = sizePx / 2f
                            val radius = (sizePx / 2f) * 0.84f
                            val dx = change.position.x - cx
                            val dy = change.position.y - cy
                            val dist = hypot(dx, dy).coerceIn(0f, radius)
                            val angleRad = atan2(dy, dx)
                            val angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat().let {
                                if (it < 0f) it + 360f else it
                            }
                            val sat = (dist / radius).coerceIn(0.01f, 1f)
                            onColorChanged(angleDeg, sat)
                        }
                    )
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val sphereRadius = (size.minDimension / 2f) * 0.84f
            val orbitalRadius = (size.minDimension / 2f) * 0.94f

            // 1. Soft Ambient Outer Glow
            val glowColor = when (modeIndex) {
                0 -> Color(0x30E53935) // RGB reddish/additive glow
                1 -> Color(0x304CAF50) // HSL fresh green glow
                2 -> Color(0x301E88E5) // HEX blue crisp glow
                3 -> Color(0x3000ACC1) // CMYK cyan/subtractive glow
                else -> Color(0x304CAF50)
            }
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor,
                        Color(0x108BC34A),
                        Color.Transparent
                    ),
                    center = center,
                    radius = orbitalRadius * 1.08f
                ),
                radius = orbitalRadius * 1.08f,
                center = center
            )

            // 2. Delicate Orbital Ring with Mode-Specific Nodes
            drawCircle(
                color = Color(0x38557A60),
                radius = orbitalRadius,
                center = center,
                style = Stroke(width = 1.2f)
            )

            // Orbital Accent Nodes depending on mode
            when (modeIndex) {
                0 -> {
                    // RGB nodes at 0° (Red), 120° (Green), 240° (Blue)
                    listOf(0.0 to Color(0xFFE53935), 120.0 to Color(0xFF43A047), 240.0 to Color(0xFF1E88E5)).forEach { (deg, col) ->
                        val rad = Math.toRadians(deg)
                        val pos = Offset(
                            center.x + orbitalRadius * cos(rad).toFloat(),
                            center.y + orbitalRadius * sin(rad).toFloat()
                        )
                        drawCircle(color = col, radius = 4f, center = pos)
                    }
                }
                3 -> {
                    // CMYK nodes at 180° (Cyan), 300° (Magenta), 60° (Yellow)
                    listOf(180.0 to Color(0xFF00ACC1), 300.0 to Color(0xFFD81B60), 60.0 to Color(0xFFFDD835)).forEach { (deg, col) ->
                        val rad = Math.toRadians(deg)
                        val pos = Offset(
                            center.x + orbitalRadius * cos(rad).toFloat(),
                            center.y + orbitalRadius * sin(rad).toFloat()
                        )
                        drawCircle(color = col, radius = 4f, center = pos)
                    }
                }
                else -> {
                    // Standard botanical nodes at 180° and 90°
                    listOf(180.0 to Color(0xFF2E633F), 90.0 to Color(0xFF2E633F)).forEach { (deg, col) ->
                        val rad = Math.toRadians(deg)
                        val pos = Offset(
                            center.x + orbitalRadius * cos(rad).toFloat(),
                            center.y + orbitalRadius * sin(rad).toFloat()
                        )
                        drawCircle(color = col, radius = 3.5f, center = pos)
                    }
                }
            }

            // 3. Mode-Specific Wheel Body Rendering (Bright, Vibrant, Vivid!)
            val spectrumColors = when (modeIndex) {
                0 -> listOf(
                    // RGB Additive Spectrum
                    Color(0xFFFF1744), // Pure Red (0°)
                    Color(0xFFFF9100), // Orange (30°)
                    Color(0xFFFFEA00), // Yellow (60°)
                    Color(0xFF00E676), // Pure Green (120°)
                    Color(0xFF00E5FF), // Cyan (180°)
                    Color(0xFF2979FF), // Pure Blue (240°)
                    Color(0xFFD500F9), // Magenta (300°)
                    Color(0xFFFF1744)  // Pure Red (360°)
                )
                3 -> listOf(
                    // CMYK Subtractive Spectrum
                    Color(0xFFFF5252), // Red
                    Color(0xFFFFD600), // Pure Process Yellow (60°)
                    Color(0xFF00E676), // Green
                    Color(0xFF00E5FF), // Pure Process Cyan (180°)
                    Color(0xFF2979FF), // Blue
                    Color(0xFFFF4081), // Pure Process Magenta (300°)
                    Color(0xFFFF5252)  // Red
                )
                2 -> listOf(
                    // HEX Crisp Clean Full Spectrum
                    Color(0xFFFF0000),
                    Color(0xFFFF8000),
                    Color(0xFFFFFF00),
                    Color(0xFF00FF00),
                    Color(0xFF00FFFF),
                    Color(0xFF0000FF),
                    Color(0xFF8000FF),
                    Color(0xFFFF0080),
                    Color(0xFFFF0000)
                )
                else -> listOf(
                    // HSL Nature Swept Spectrum (Bright & Luminous)
                    Color(0xFFE53935), // Red (0°)
                    Color(0xFFFB8C00), // Orange (30°)
                    Color(0xFFFDD835), // Yellow (60°)
                    Color(0xFF7CB342), // Lime (90°)
                    Color(0xFF43A047), // Green (120°)
                    Color(0xFF00ACC1), // Turquoise / Cyan (180°)
                    Color(0xFF1E88E5), // Blue (220°)
                    Color(0xFF3949AB), // Deep Indigo (240°)
                    Color(0xFF8E24AA), // Violet (280°)
                    Color(0xFFD81B60), // Magenta (320°)
                    Color(0xFFE53935)  // Red (360°)
                )
            }

            drawCircle(
                brush = Brush.sweepGradient(spectrumColors, center),
                radius = sphereRadius,
                center = center
            )

            // 4. Center Brightness / Saturation Core
            // White core at center fading out to full saturation at circumference
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xE6FFFFFF),
                        Color(0x73FFFFFF),
                        Color(0x1AFFFFFF),
                        Color.Transparent
                    ),
                    center = center,
                    radius = sphereRadius
                ),
                radius = sphereRadius,
                center = center
            )

            // 5. Subtle 3D Spherical Edge Shadow (Gentle curvature without dimming brightness)
            if (modeIndex != 2) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color(0x08000000),
                            Color(0x20000000),
                            Color(0x381A3826)
                        ),
                        center = center,
                        radius = sphereRadius
                    ),
                    radius = sphereRadius,
                    center = center
                )

                // 6. Gentle Specular Gloss Highlight (upper-left luminous reflection)
                val highlightCenter = Offset(
                    center.x - sphereRadius * 0.32f,
                    center.y - sphereRadius * 0.32f
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x80FFFFFF),
                            Color(0x30FFFFFF),
                            Color.Transparent
                        ),
                        center = highlightCenter,
                        radius = sphereRadius * 0.65f
                    ),
                    radius = sphereRadius * 0.65f,
                    center = highlightCenter
                )
            }

            // 7. Sphere Outer Border Ring
            drawCircle(
                color = Color(0x332E633F),
                radius = sphereRadius,
                center = center,
                style = Stroke(width = 1.5f)
            )

            // 8. Interactive Live Selection Indicator (Instantly in sync with current color)
            val indicatorAngleRad = Math.toRadians(currentHsl.hue.toDouble())
            val indicatorDist = currentHsl.saturation.coerceIn(0.01f, 1f) * sphereRadius
            val indicatorCenter = Offset(
                center.x + (indicatorDist * cos(indicatorAngleRad)).toFloat(),
                center.y + (indicatorDist * sin(indicatorAngleRad)).toFloat()
            )

            // Outer white ring
            drawCircle(
                color = Color.White,
                radius = 9.dp.toPx(),
                center = indicatorCenter,
                style = Stroke(width = 2.5.dp.toPx())
            )
            // Dark green subtle outline
            drawCircle(
                color = Color(0xFF1B3B2B),
                radius = 10.5.dp.toPx(),
                center = indicatorCenter,
                style = Stroke(width = 1.dp.toPx())
            )
            // Filled center with live selected color
            drawCircle(
                color = currentHsl.composeColor,
                radius = 5.dp.toPx(),
                center = indicatorCenter
            )
        }
    }
}

/**
 * Botanical Leaf Silhouette Decorative Background.
 */
@Composable
fun BotanicalAtmosphereBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val leafTint = Color(0x182E633F)

        fun drawLeaf(cx: Float, cy: Float, radius: Float, angleDeg: Float) {
            val path = Path().apply {
                val rad = Math.toRadians(angleDeg.toDouble())
                val cosA = cos(rad).toFloat()
                val sinA = sin(rad).toFloat()

                val p0 = Offset(cx - radius * cosA, cy - radius * sinA)
                val p1 = Offset(cx + radius * cosA, cy + radius * sinA)
                val cp1 = Offset(cx + radius * 0.6f * -sinA, cy + radius * 0.6f * cosA)
                val cp2 = Offset(cx - radius * 0.6f * -sinA, cy - radius * 0.6f * cosA)

                moveTo(p0.x, p0.y)
                quadraticTo(cp1.x, cp1.y, p1.x, p1.y)
                quadraticTo(cp2.x, cp2.y, p0.x, p0.y)
                close()
            }
            drawPath(path, color = leafTint)
        }

        // Top-left foliage
        drawLeaf(16f, 16f, 28f, 35f)
        drawLeaf(36f, 12f, 20f, 65f)
        drawLeaf(14f, 42f, 22f, 15f)

        // Top-right foliage
        drawLeaf(w - 20f, 18f, 26f, -40f)
        drawLeaf(w - 42f, 14f, 18f, -70f)
        drawLeaf(w - 16f, 44f, 20f, -20f)

        // Bottom-left foliage
        drawLeaf(20f, h - 20f, 26f, -45f)
        drawLeaf(42f, h - 14f, 18f, -15f)

        // Bottom-right foliage
        drawLeaf(w - 24f, h - 22f, 28f, 40f)
        drawLeaf(w - 46f, h - 16f, 20f, 75f)
    }
}

/**
 * Nature-Inspired Gradient Sphere System - Complete Color Picker Component.
 * Clean, organic, calm, modern, and production-ready with full bidirectional synchronization.
 */
@Composable
fun NatureGradientSphereColorPicker(
    initialColorHex: String,
    onColorChosen: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    appLanguage: String = LocalAppLanguage.current
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val configuration = LocalConfiguration.current
    val strings = remember(appLanguage) { AppStrings.get(appLanguage) }

    // Initialize with vibrant, bright default if not provided
    val initialHsl = remember(initialColorHex) {
        if (initialColorHex.isNotBlank() && CustomColorManager.isValidHex(initialColorHex)) {
            val parsed = NatureColorEngine.hexToHsl(initialColorHex)
            // Ensure bright and vibrant default
            parsed
        } else {
            NatureColorEngine.HslColor(0f, 1.0f, 0.5f) // Default bright vivid Red
        }
    }

    // Single source of truth for color state
    var currentHue by remember { mutableFloatStateOf(initialHsl.hue) }
    var currentSaturation by remember { mutableFloatStateOf(initialHsl.saturation.coerceIn(0.01f, 1.0f)) }
    var currentLightness by remember { mutableFloatStateOf(initialHsl.lightness.coerceIn(0.05f, 0.95f)) }

    val currentHslColor = NatureColorEngine.HslColor(currentHue, currentSaturation, currentLightness)
    val currentHex = currentHslColor.hex
    val currentRgb = currentHslColor.rgb
    val currentCmyk = currentHslColor.cmyk

    var selectedModeIndex by remember { mutableIntStateOf(1) } // 0=RGB, 1=HSL, 2=HEX, 3=CMYK
    var hexInputText by remember { mutableStateOf(currentHex) }
    var hexInputError by remember { mutableStateOf(false) }

    // Synchronize hex text field with live color changes
    LaunchedEffect(currentHex) {
        if (!hexInputText.equals(currentHex, ignoreCase = true)) {
            hexInputText = currentHex
            hexInputError = false
        }
    }

    var customColors by remember {
        mutableStateOf(CustomColorManager.getCustomColors(context))
    }
    var showSavedColorsDialog by remember { mutableStateOf(false) }
    var colorToDelete by remember { mutableStateOf<String?>(null) }

    val harmonyPalettes = remember(currentHue, currentSaturation, currentLightness) {
        NatureColorEngine.generateHarmonyPalettes(currentHslColor)
    }

    val maxDialogHeight = remember(configuration.screenHeightDp) {
        (configuration.screenHeightDp * 0.92f).dp
    }

    // Palette Colors (Nature Design Theme)
    val natureBackground = Color(0xFFF9FBF6)
    val natureCardBg = Color(0xFFFCFDF9)
    val natureBorder = Color(0xFFDCEAD4)
    val natureTextDark = Color(0xFF1B3B2B)
    val natureTextMuted = Color(0xFF557A60)
    val naturePrimaryGreen = Color(0xFF1B3B2B)
    val natureSurfaceLight = Color(0xFFEDF4E8)
    val natureSurfaceActive = Color(0xFF1B3B2B)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = natureCardBg),
            border = BorderStroke(1.2.dp, natureBorder),
            modifier = modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 520.dp)
                .heightIn(max = maxDialogHeight)
                .shadow(12.dp, RoundedCornerShape(24.dp), ambientColor = Color(0x202E633F), spotColor = Color(0x302E633F))
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Subtle botanical foliage background
                BotanicalAtmosphereBackground(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(24.dp))
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // ==========================================
                    // 1. HEADER: Leaf Icon + Title + Subtitle
                    // ==========================================
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(natureSurfaceLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Spa,
                                contentDescription = null,
                                tint = Color(0xFF2E633F),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.naturePickerTitle,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = natureTextDark,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = strings.naturePickerSubtitle,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = natureTextMuted,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = strings.close,
                                tint = natureTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // =========================================================================
                    // 2. MAIN CONTAINER: Gradient Sphere + Color Preview Panel + Modes
                    // =========================================================================
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = natureBackground,
                        border = BorderStroke(1.dp, natureBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Responsive top row: Sphere + Preview
                            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                val isNarrow = maxWidth < 340.dp
                                if (isNarrow) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        GradientSphereCanvas(
                                            currentHsl = currentHslColor,
                                            modeIndex = selectedModeIndex,
                                            onColorChanged = { h, s ->
                                                currentHue = h
                                                currentSaturation = s
                                            },
                                            modifier = Modifier
                                                .size(160.dp)
                                                .testTag("gradient_sphere_canvas")
                                        )

                                        ColorPreviewAndModesPanel(
                                            currentHex = currentHex,
                                            currentRgb = currentRgb,
                                            currentHue = currentHue,
                                            currentSaturation = currentSaturation,
                                            currentLightness = currentLightness,
                                            currentCmyk = currentCmyk,
                                            currentHslColor = currentHslColor,
                                            selectedModeIndex = selectedModeIndex,
                                            onModeSelected = { selectedModeIndex = it },
                                            onCopyHex = {
                                                clipboardManager.setText(AnnotatedString(currentHex))
                                                Toast.makeText(context, "$currentHex kopiert", Toast.LENGTH_SHORT).show()
                                            },
                                            natureTextDark = natureTextDark,
                                            natureTextMuted = natureTextMuted,
                                            natureSurfaceActive = natureSurfaceActive,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        GradientSphereCanvas(
                                            currentHsl = currentHslColor,
                                            modeIndex = selectedModeIndex,
                                            onColorChanged = { h, s ->
                                                currentHue = h
                                                currentSaturation = s
                                            },
                                            modifier = Modifier
                                                .size(160.dp)
                                                .testTag("gradient_sphere_canvas")
                                        )

                                        ColorPreviewAndModesPanel(
                                            currentHex = currentHex,
                                            currentRgb = currentRgb,
                                            currentHue = currentHue,
                                            currentSaturation = currentSaturation,
                                            currentLightness = currentLightness,
                                            currentCmyk = currentCmyk,
                                            currentHslColor = currentHslColor,
                                            selectedModeIndex = selectedModeIndex,
                                            onModeSelected = { selectedModeIndex = it },
                                            onCopyHex = {
                                                clipboardManager.setText(AnnotatedString(currentHex))
                                                Toast.makeText(context, "$currentHex kopiert", Toast.LENGTH_SHORT).show()
                                            },
                                            natureTextDark = natureTextDark,
                                            natureTextMuted = natureTextMuted,
                                            natureSurfaceActive = natureSurfaceActive,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            // =========================================================================
                            // 3. SYSTEM-SPECIFIC FUNCTIONAL CONTROLS (RGB / HSL / HEX / CMYK)
                            // =========================================================================
                            when (selectedModeIndex) {
                                0 -> {
                                    // --------------------------------------------------
                                    // RGB SYSTEM: 3 Sliders (R, G, B: 0..255)
                                    // --------------------------------------------------
                                    RgbControlPanel(
                                        r = currentRgb.first,
                                        g = currentRgb.second,
                                        b = currentRgb.third,
                                        onRgbChanged = { newR, newG, newB ->
                                            val newHsl = NatureColorEngine.rgbToHsl(newR, newG, newB)
                                            currentHue = newHsl.hue
                                            currentSaturation = newHsl.saturation
                                            currentLightness = newHsl.lightness
                                        },
                                        natureTextDark = natureTextDark
                                    )
                                }
                                1 -> {
                                    // --------------------------------------------------
                                    // HSL SYSTEM: 3 Sliders (Hue, Saturation, Lightness)
                                    // --------------------------------------------------
                                    HslControlPanel(
                                        hue = currentHue,
                                        saturation = currentSaturation,
                                        lightness = currentLightness,
                                        currentHslColor = currentHslColor,
                                        onHueChanged = { currentHue = it },
                                        onSaturationChanged = { currentSaturation = it },
                                        onLightnessChanged = { currentLightness = it },
                                        natureTextDark = natureTextDark
                                    )
                                }
                                2 -> {
                                    // --------------------------------------------------
                                    // HEX SYSTEM: Direct Hex Input + Value Breakdown
                                    // --------------------------------------------------
                                    HexControlPanel(
                                        hexText = hexInputText,
                                        onHexChange = { input ->
                                            hexInputError = false
                                            val sanitized = if (input.startsWith("#")) input else "#$input"
                                            hexInputText = sanitized.take(7)
                                            if (CustomColorManager.isValidHex(sanitized)) {
                                                val hsl = NatureColorEngine.hexToHsl(sanitized)
                                                currentHue = hsl.hue
                                                currentSaturation = hsl.saturation
                                                currentLightness = hsl.lightness
                                            }
                                        },
                                        hasError = hexInputError,
                                        currentHslColor = currentHslColor,
                                        natureTextDark = natureTextDark,
                                        natureTextMuted = natureTextMuted,
                                        naturePrimaryGreen = naturePrimaryGreen
                                    )
                                }
                                3 -> {
                                    // --------------------------------------------------
                                    // CMYK SYSTEM: 4 Sliders (C, M, Y, K: 0%..100%)
                                    // --------------------------------------------------
                                    CmykControlPanel(
                                        c = currentCmyk[0],
                                        m = currentCmyk[1],
                                        y = currentCmyk[2],
                                        k = currentCmyk[3],
                                        onCmykChanged = { newC, newM, newY, newK ->
                                            val (newR, newG, newB) = NatureColorEngine.cmykToRgb(
                                                newC.toFloat(),
                                                newM.toFloat(),
                                                newY.toFloat(),
                                                newK.toFloat()
                                            )
                                            val newHsl = NatureColorEngine.rgbToHsl(newR, newG, newB)
                                            currentHue = newHsl.hue
                                            currentSaturation = newHsl.saturation
                                            currentLightness = newHsl.lightness
                                        },
                                        natureTextDark = natureTextDark
                                    )
                                }
                            }
                        }
                    }

                    // ==========================================
                    // 4. HARMONY SUGGESTIONS
                    // ==========================================
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Eco,
                                    contentDescription = null,
                                    tint = Color(0xFF2E633F),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = strings.harmonySuggestionsTitle,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = natureTextDark
                                )
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = natureTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Palettes horizontal scroll row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            harmonyPalettes.forEach { paletteGroup ->
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFFEDF4E8),
                                    border = BorderStroke(1.dp, Color(0xFFDCEAD4)),
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        paletteGroup.forEach { hex ->
                                            val swatchColor = NatureColorEngine.hexToHsl(hex).composeColor
                                            val isSelected = hex.equals(currentHex, ignoreCase = true)
                                            Box(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .clip(CircleShape)
                                                    .background(swatchColor)
                                                    .border(
                                                        width = if (isSelected) 2.5.dp else 1.dp,
                                                        color = if (isSelected) natureTextDark else Color(0x33000000),
                                                        shape = CircleShape
                                                    )
                                                    .clickable {
                                                        val hsl = NatureColorEngine.hexToHsl(hex)
                                                        currentHue = hsl.hue
                                                        currentSaturation = hsl.saturation
                                                        currentLightness = hsl.lightness
                                                    }
                                                    .testTag("harmony_swatch_$hex")
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ==========================================
                    // 5. SAVED COLORS (Clickable -> Opens Full Modal)
                    // ==========================================
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFEDF4E8),
                        border = BorderStroke(1.dp, Color(0xFFDCEAD4)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSavedColorsDialog = true }
                            .testTag("saved_colors_section_button")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFF2E633F),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = strings.savedColorsTitle,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = natureTextDark
                                )
                                Text(
                                    text = "(${customColors.size})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = natureTextMuted
                                )
                            }

                            // Saved swatches pill on the right
                            val displaySaved = if (customColors.isNotEmpty()) {
                                customColors.take(6)
                            } else {
                                listOf("#E53935", "#4CAF50", "#2196F3", "#FF9800", "#9C27B0")
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                displaySaved.forEach { hex ->
                                    val color = NatureColorEngine.hexToHsl(hex).composeColor
                                    val isSelected = hex.equals(currentHex, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) natureTextDark else Color(0x33000000),
                                                shape = CircleShape
                                            )
                                    )
                                }

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Open Saved Colors",
                                    tint = natureTextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // ==========================================
                    // 6. ADD CUSTOM COLOR (Input + Add Button)
                    // ==========================================
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircleOutline,
                                contentDescription = null,
                                tint = Color(0xFF2E633F),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = strings.colorPickerAddCustomColor,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = natureTextDark
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Rounded input field
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                border = BorderStroke(
                                    1.2.dp,
                                    if (hexInputError) MaterialTheme.colorScheme.error else Color(0xFFDCEAD4)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    BasicTextField(
                                        value = hexInputText,
                                        onValueChange = { input ->
                                            hexInputError = false
                                            val sanitized = if (input.startsWith("#")) input else "#$input"
                                            hexInputText = sanitized.take(7)
                                            if (CustomColorManager.isValidHex(sanitized)) {
                                                val hsl = NatureColorEngine.hexToHsl(sanitized)
                                                currentHue = hsl.hue
                                                currentSaturation = hsl.saturation
                                                currentLightness = hsl.lightness
                                            }
                                        },
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            color = natureTextDark,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        cursorBrush = SolidColor(naturePrimaryGreen),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("add_custom_hex_input")
                                    )

                                    if (hexInputText.isNotEmpty()) {
                                        IconButton(
                                            onClick = { hexInputText = "#" },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear",
                                                tint = natureTextMuted,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }

                                    // Preview circle swatch inside input
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(currentHslColor.composeColor)
                                            .border(1.dp, Color(0x33000000), CircleShape)
                                    )
                                }
                            }

                            // Add Button
                            Button(
                                onClick = {
                                    if (CustomColorManager.isValidHex(hexInputText)) {
                                        val formatted = CustomColorManager.normalizeHex(hexInputText)
                                        CustomColorManager.addCustomColor(context, formatted)
                                        customColors = CustomColorManager.getCustomColors(context)
                                        val hsl = NatureColorEngine.hexToHsl(formatted)
                                        currentHue = hsl.hue
                                        currentSaturation = hsl.saturation
                                        currentLightness = hsl.lightness
                                        hexInputError = false
                                        Toast.makeText(context, "$formatted", Toast.LENGTH_SHORT).show()
                                    } else {
                                        hexInputError = true
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = naturePrimaryGreen,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(44.dp)
                                    .testTag("add_custom_color_button")
                            ) {
                                Text(
                                    text = strings.colorPickerAddButton,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // ==========================================
                    // 7. BOTTOM ACTIONS: Cancel / Apply
                    // ==========================================
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Cancel Button
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.2.dp, Color(0xFFCCDDC4)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFFF7F9F3),
                                contentColor = natureTextDark
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("color_picker_cancel_button")
                        ) {
                            Text(
                                text = strings.colorPickerCancel,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = natureTextDark
                            )
                        }

                        // Apply Button
                        Button(
                            onClick = {
                                onColorChosen(currentHex)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = naturePrimaryGreen,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("color_picker_apply_button")
                        ) {
                            Text(
                                text = strings.colorPickerApply,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // SAVED COLORS FULL MANAGEMENT MODAL
    // ==========================================
    if (showSavedColorsDialog) {
        SavedColorsManagementDialog(
            customColors = customColors,
            selectedHex = currentHex,
            onSelectColor = { chosenHex ->
                val hsl = NatureColorEngine.hexToHsl(chosenHex)
                currentHue = hsl.hue
                currentSaturation = hsl.saturation
                currentLightness = hsl.lightness
                showSavedColorsDialog = false
            },
            onDeleteColor = { hexToDelete ->
                colorToDelete = hexToDelete
            },
            onAddNewColor = { newHex ->
                if (CustomColorManager.isValidHex(newHex)) {
                    val formatted = CustomColorManager.normalizeHex(newHex)
                    CustomColorManager.addCustomColor(context, formatted)
                    customColors = CustomColorManager.getCustomColors(context)
                }
            },
            onDismiss = { showSavedColorsDialog = false },
            natureBackground = natureBackground,
            natureBorder = natureBorder,
            natureTextDark = natureTextDark,
            natureTextMuted = natureTextMuted,
            naturePrimaryGreen = naturePrimaryGreen
        )
    }

    // Delete custom color dialog
    if (colorToDelete != null) {
        val targetHex = colorToDelete!!
        AlertDialog(
            onDismissRequest = { colorToDelete = null },
            title = {
                Text(
                    text = strings.colorPickerDeleteTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = natureTextDark
                )
            },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(NatureColorEngine.hexToHsl(targetHex).composeColor)
                            .border(1.dp, natureBorder, CircleShape)
                    )
                    Text(
                        text = strings.deleteColorPrompt(targetHex),
                        fontSize = 13.sp,
                        color = natureTextMuted
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        CustomColorManager.removeCustomColor(context, targetHex)
                        customColors = CustomColorManager.getCustomColors(context)
                        colorToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(strings.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { colorToDelete = null }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}

/**
 * Functional RGB Slider Control Panel (Red, Green, Blue: 0..255).
 */
@Composable
private fun RgbControlPanel(
    r: Int,
    g: Int,
    b: Int,
    onRgbChanged: (Int, Int, Int) -> Unit,
    natureTextDark: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Red Slider
        RgbSingleSlider(
            label = "R",
            value = r,
            onValueChanged = { onRgbChanged(it, g, b) },
            startColor = Color(0, g, b),
            endColor = Color(255, g, b),
            activeColor = Color(r, g, b),
            natureTextDark = natureTextDark
        )

        // Green Slider
        RgbSingleSlider(
            label = "G",
            value = g,
            onValueChanged = { onRgbChanged(r, it, b) },
            startColor = Color(r, 0, b),
            endColor = Color(r, 255, b),
            activeColor = Color(r, g, b),
            natureTextDark = natureTextDark
        )

        // Blue Slider
        RgbSingleSlider(
            label = "B",
            value = b,
            onValueChanged = { onRgbChanged(r, g, it) },
            startColor = Color(r, g, 0),
            endColor = Color(r, g, 255),
            activeColor = Color(r, g, b),
            natureTextDark = natureTextDark
        )
    }
}

@Composable
private fun RgbSingleSlider(
    label: String,
    value: Int,
    onValueChanged: (Int) -> Unit,
    startColor: Color,
    endColor: Color,
    activeColor: Color,
    natureTextDark: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label ($label-Wert)",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = natureTextDark
            )
            Text(
                text = "$value",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = natureTextDark
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { offset ->
                            val pct = (offset.x / size.width).coerceIn(0f, 1f)
                            onValueChanged((pct * 255f).roundToInt().coerceIn(0, 255))
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, _ ->
                            change.consume()
                            val pct = (change.position.x / size.width).coerceIn(0f, 1f)
                            onValueChanged((pct * 255f).roundToInt().coerceIn(0, 255))
                        }
                    )
                }
        ) {
            val trackHeight = 10.dp.toPx()
            val cy = size.height / 2f
            val trackY = cy - trackHeight / 2f

            drawRoundRect(
                brush = Brush.horizontalGradient(listOf(startColor, endColor)),
                topLeft = Offset(0f, trackY),
                size = Size(size.width, trackHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight / 2f)
            )

            val thumbX = (value / 255f) * size.width
            drawCircle(
                color = Color.White,
                radius = 8.dp.toPx(),
                center = Offset(thumbX, cy)
            )
            drawCircle(
                color = Color(0xFF1B3B2B),
                radius = 8.dp.toPx(),
                center = Offset(thumbX, cy),
                style = Stroke(width = 1.5.dp.toPx())
            )
            drawCircle(
                color = activeColor,
                radius = 4.5.dp.toPx(),
                center = Offset(thumbX, cy)
            )
        }
    }
}

/**
 * Functional HSL Slider Control Panel (Hue, Saturation, Lightness).
 */
@Composable
private fun HslControlPanel(
    hue: Float,
    saturation: Float,
    lightness: Float,
    currentHslColor: NatureColorEngine.HslColor,
    onHueChanged: (Float) -> Unit,
    onSaturationChanged: (Float) -> Unit,
    onLightnessChanged: (Float) -> Unit,
    natureTextDark: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Hue Slider
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hue (Farbton)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = natureTextDark
                )
                Text(
                    text = "${hue.roundToInt()}°",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = natureTextDark
                )
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { offset ->
                                val pct = (offset.x / size.width).coerceIn(0f, 1f)
                                onHueChanged(pct * 360f)
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, _ ->
                                change.consume()
                                val pct = (change.position.x / size.width).coerceIn(0f, 1f)
                                onHueChanged(pct * 360f)
                            }
                        )
                    }
            ) {
                val trackHeight = 10.dp.toPx()
                val cy = size.height / 2f
                val trackY = cy - trackHeight / 2f

                val rainbow = Brush.horizontalGradient(
                    listOf(
                        Color(0xFFE53935),
                        Color(0xFFFDD835),
                        Color(0xFF43A047),
                        Color(0xFF00ACC1),
                        Color(0xFF1E88E5),
                        Color(0xFF8E24AA),
                        Color(0xFFD81B60),
                        Color(0xFFE53935)
                    )
                )
                drawRoundRect(
                    brush = rainbow,
                    topLeft = Offset(0f, trackY),
                    size = Size(size.width, trackHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight / 2f)
                )

                val thumbX = (hue / 360f) * size.width
                drawCircle(
                    color = Color.White,
                    radius = 8.dp.toPx(),
                    center = Offset(thumbX, cy)
                )
                drawCircle(
                    color = Color(0xFF1B3B2B),
                    radius = 8.dp.toPx(),
                    center = Offset(thumbX, cy),
                    style = Stroke(width = 1.5.dp.toPx())
                )
                drawCircle(
                    color = currentHslColor.composeColor,
                    radius = 4.5.dp.toPx(),
                    center = Offset(thumbX, cy)
                )
            }
        }

        // 2. Saturation Slider
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Saturation (Sättigung)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = natureTextDark
                )
                Text(
                    text = "${(saturation * 100).roundToInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = natureTextDark
                )
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { offset ->
                                onSaturationChanged((offset.x / size.width).coerceIn(0.01f, 1f))
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, _ ->
                                change.consume()
                                onSaturationChanged((change.position.x / size.width).coerceIn(0.01f, 1f))
                            }
                        )
                    }
            ) {
                val trackHeight = 10.dp.toPx()
                val cy = size.height / 2f
                val trackY = cy - trackHeight / 2f

                val minColor = NatureColorEngine.HslColor(hue, 0f, lightness).composeColor
                val maxColor = NatureColorEngine.HslColor(hue, 1f, lightness).composeColor

                drawRoundRect(
                    brush = Brush.horizontalGradient(listOf(minColor, maxColor)),
                    topLeft = Offset(0f, trackY),
                    size = Size(size.width, trackHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight / 2f)
                )

                val thumbX = saturation * size.width
                drawCircle(
                    color = Color.White,
                    radius = 8.dp.toPx(),
                    center = Offset(thumbX, cy)
                )
                drawCircle(
                    color = Color(0xFF1B3B2B),
                    radius = 8.dp.toPx(),
                    center = Offset(thumbX, cy),
                    style = Stroke(width = 1.5.dp.toPx())
                )
                drawCircle(
                    color = currentHslColor.composeColor,
                    radius = 4.5.dp.toPx(),
                    center = Offset(thumbX, cy)
                )
            }
        }

        // 3. Lightness Slider
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lightness (Helligkeit)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = natureTextDark
                )
                Text(
                    text = "${(lightness * 100).roundToInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = natureTextDark
                )
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { offset ->
                                onLightnessChanged((offset.x / size.width).coerceIn(0.05f, 0.95f))
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, _ ->
                                change.consume()
                                onLightnessChanged((change.position.x / size.width).coerceIn(0.05f, 0.95f))
                            }
                        )
                    }
            ) {
                val trackHeight = 10.dp.toPx()
                val cy = size.height / 2f
                val trackY = cy - trackHeight / 2f

                val blackColor = Color.Black
                val midColor = NatureColorEngine.HslColor(hue, saturation, 0.5f).composeColor
                val whiteColor = Color.White

                drawRoundRect(
                    brush = Brush.horizontalGradient(listOf(blackColor, midColor, whiteColor)),
                    topLeft = Offset(0f, trackY),
                    size = Size(size.width, trackHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight / 2f)
                )

                val thumbX = lightness * size.width
                drawCircle(
                    color = Color.White,
                    radius = 8.dp.toPx(),
                    center = Offset(thumbX, cy)
                )
                drawCircle(
                    color = Color(0xFF1B3B2B),
                    radius = 8.dp.toPx(),
                    center = Offset(thumbX, cy),
                    style = Stroke(width = 1.5.dp.toPx())
                )
                drawCircle(
                    color = currentHslColor.composeColor,
                    radius = 4.5.dp.toPx(),
                    center = Offset(thumbX, cy)
                )
            }
        }
    }
}

/**
 * Functional HEX Panel (Hex Input + Real-Time Preview).
 */
@Composable
private fun HexControlPanel(
    hexText: String,
    onHexChange: (String) -> Unit,
    hasError: Boolean,
    currentHslColor: NatureColorEngine.HslColor,
    natureTextDark: Color,
    natureTextMuted: Color,
    naturePrimaryGreen: Color
) {
    val appLanguage = LocalAppLanguage.current
    val strings = remember(appLanguage) { AppStrings.get(appLanguage) }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = strings.enterHexCodeLabel,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = natureTextDark
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(
                1.2.dp,
                if (hasError) MaterialTheme.colorScheme.error else Color(0xFFDCEAD4)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "#",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = natureTextDark
                )

                BasicTextField(
                    value = hexText.removePrefix("#"),
                    onValueChange = { onHexChange(it) },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = natureTextDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    cursorBrush = SolidColor(naturePrimaryGreen),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("hex_mode_input")
                )

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(currentHslColor.composeColor)
                        .border(1.dp, Color(0x33000000), CircleShape)
                )
            }
        }

        Text(
            text = strings.hexInputHelperText,
            fontSize = 11.5.sp,
            color = natureTextMuted
        )
    }
}

/**
 * Functional CMYK Slider Control Panel (Cyan, Magenta, Yellow, Black/Key: 0%..100%).
 */
@Composable
private fun CmykControlPanel(
    c: Int,
    m: Int,
    y: Int,
    k: Int,
    onCmykChanged: (Int, Int, Int, Int) -> Unit,
    natureTextDark: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Cyan Slider
        CmykSingleSlider(
            label = "Cyan (C)",
            value = c,
            onValueChanged = { onCmykChanged(it, m, y, k) },
            startColor = Color(0xFFFFFFFF),
            endColor = Color(0xFF00ACC1),
            activeColor = Color(0xFF00ACC1),
            natureTextDark = natureTextDark
        )

        // Magenta Slider
        CmykSingleSlider(
            label = "Magenta (M)",
            value = m,
            onValueChanged = { onCmykChanged(c, it, y, k) },
            startColor = Color(0xFFFFFFFF),
            endColor = Color(0xFFD81B60),
            activeColor = Color(0xFFD81B60),
            natureTextDark = natureTextDark
        )

        // Yellow Slider
        CmykSingleSlider(
            label = "Yellow (Y)",
            value = y,
            onValueChanged = { onCmykChanged(c, m, it, k) },
            startColor = Color(0xFFFFFFFF),
            endColor = Color(0xFFFDD835),
            activeColor = Color(0xFFFDD835),
            natureTextDark = natureTextDark
        )

        // Key / Black Slider
        CmykSingleSlider(
            label = "Key / Black (K)",
            value = k,
            onValueChanged = { onCmykChanged(c, m, y, it) },
            startColor = Color(0xFFFFFFFF),
            endColor = Color(0xFF212121),
            activeColor = Color(0xFF212121),
            natureTextDark = natureTextDark
        )
    }
}

@Composable
private fun CmykSingleSlider(
    label: String,
    value: Int,
    onValueChanged: (Int) -> Unit,
    startColor: Color,
    endColor: Color,
    activeColor: Color,
    natureTextDark: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = natureTextDark
            )
            Text(
                text = "$value%",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = natureTextDark
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(22.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { offset ->
                            val pct = (offset.x / size.width).coerceIn(0f, 1f)
                            onValueChanged((pct * 100f).roundToInt().coerceIn(0, 100))
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, _ ->
                            change.consume()
                            val pct = (change.position.x / size.width).coerceIn(0f, 1f)
                            onValueChanged((pct * 100f).roundToInt().coerceIn(0, 100))
                        }
                    )
                }
        ) {
            val trackHeight = 9.dp.toPx()
            val cy = size.height / 2f
            val trackY = cy - trackHeight / 2f

            drawRoundRect(
                brush = Brush.horizontalGradient(listOf(startColor, endColor)),
                topLeft = Offset(0f, trackY),
                size = Size(size.width, trackHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight / 2f)
            )

            val thumbX = (value / 100f) * size.width
            drawCircle(
                color = Color.White,
                radius = 7.5.dp.toPx(),
                center = Offset(thumbX, cy)
            )
            drawCircle(
                color = Color(0xFF1B3B2B),
                radius = 7.5.dp.toPx(),
                center = Offset(thumbX, cy),
                style = Stroke(width = 1.5.dp.toPx())
            )
            drawCircle(
                color = activeColor,
                radius = 4.dp.toPx(),
                center = Offset(thumbX, cy)
            )
        }
    }
}

/**
 * Preview Panel displaying large live swatch, values (HEX, RGB, HSL, CMYK), and mode selector buttons.
 */
@Composable
private fun ColorPreviewAndModesPanel(
    currentHex: String,
    currentRgb: Triple<Int, Int, Int>,
    currentHue: Float,
    currentSaturation: Float,
    currentLightness: Float,
    currentCmyk: List<Int>,
    currentHslColor: NatureColorEngine.HslColor,
    selectedModeIndex: Int,
    onModeSelected: (Int) -> Unit,
    onCopyHex: () -> Unit,
    natureTextDark: Color,
    natureTextMuted: Color,
    natureSurfaceActive: Color,
    modifier: Modifier = Modifier
) {
    val appLanguage = LocalAppLanguage.current
    val strings = remember(appLanguage) { AppStrings.get(appLanguage) }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Preview Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF3F7EE),
            border = BorderStroke(1.dp, Color(0xFFDCEAD4)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = strings.colorPreviewLabel,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = natureTextDark
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Large color square
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(currentHslColor.composeColor)
                            .border(1.5.dp, Color(0x22000000), RoundedCornerShape(12.dp))
                            .testTag("color_preview_swatch")
                    )

                    // Color values
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // HEX with copy icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = currentHex,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = natureTextDark
                            )
                            IconButton(
                                onClick = onCopyHex,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy HEX",
                                    tint = natureTextMuted,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }

                        // RGB
                        Text(
                            text = "RGB   ${currentRgb.first}  ${currentRgb.second}  ${currentRgb.third}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = natureTextMuted
                        )

                        // HSL
                        Text(
                            text = "HSL   ${currentHue.roundToInt()}°  ${(currentSaturation * 100).roundToInt()}%  ${(currentLightness * 100).roundToInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = natureTextMuted
                        )
                    }
                }
            }
        }

        // Color Modes Selector
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = strings.colorModesLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = natureTextDark
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("RGB", "HSL", "HEX", "CMYK").forEachIndexed { index, modeName ->
                    val isSelected = selectedModeIndex == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) natureSurfaceActive else Color(0xFFE4EEE0))
                            .clickable { onModeSelected(index) }
                            .testTag("color_mode_$modeName"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = modeName,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isSelected) Color.White else natureTextDark
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dedicated Saved Colors Management Dialog allowing users to view, select, delete, or add saved colors freely.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SavedColorsManagementDialog(
    customColors: List<String>,
    selectedHex: String,
    onSelectColor: (String) -> Unit,
    onDeleteColor: (String) -> Unit,
    onAddNewColor: (String) -> Unit,
    onDismiss: () -> Unit,
    natureBackground: Color,
    natureBorder: Color,
    natureTextDark: Color,
    natureTextMuted: Color,
    naturePrimaryGreen: Color
) {
    val appLanguage = LocalAppLanguage.current
    val strings = remember(appLanguage) { AppStrings.get(appLanguage) }
    var newHexInput by remember { mutableStateOf("#") }
    var inputError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFCFDF9)),
            border = BorderStroke(1.2.dp, natureBorder),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 460.dp)
                .padding(12.dp)
                .shadow(12.dp, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFF2E633F),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = strings.savedColorsTitle,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = natureTextDark
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = strings.close,
                            tint = natureTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = strings.savedColorsSubtitle,
                    fontSize = 12.sp,
                    color = natureTextMuted
                )

                // Saved Colors Grid / List
                if (customColors.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = strings.noSavedColorsText,
                            fontSize = 13.sp,
                            color = natureTextMuted
                        )
                    }
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        customColors.forEach { hex ->
                            val color = NatureColorEngine.hexToHsl(hex).composeColor
                            val isSelected = hex.equals(selectedHex, ignoreCase = true)

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFFEDF4E8) else Color(0xFFF5F8F2),
                                border = BorderStroke(
                                    if (isSelected) 1.8.dp else 1.dp,
                                    if (isSelected) natureTextDark else Color(0xFFDCEAD4)
                                ),
                                modifier = Modifier
                                    .clickable { onSelectColor(hex) }
                                    .testTag("saved_color_item_$hex")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .border(1.dp, Color(0x33000000), CircleShape)
                                    )

                                    Text(
                                        text = hex,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = natureTextDark
                                    )

                                    IconButton(
                                        onClick = { onDeleteColor(hex) },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = strings.delete,
                                            tint = Color(0xFFE53935),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFDCEAD4), thickness = 1.dp)

                // Add new color row inside modal
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = strings.addNewColorLabel,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = natureTextDark
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            border = BorderStroke(
                                1.dp,
                                if (inputError) MaterialTheme.colorScheme.error else Color(0xFFDCEAD4)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            BasicTextField(
                                value = newHexInput,
                                onValueChange = {
                                    inputError = false
                                    val formatted = if (it.startsWith("#")) it else "#$it"
                                    newHexInput = formatted.take(7)
                                },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = natureTextDark,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                cursorBrush = SolidColor(naturePrimaryGreen),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 10.dp)
                            )
                        }

                        Button(
                            onClick = {
                                if (CustomColorManager.isValidHex(newHexInput)) {
                                    onAddNewColor(newHexInput)
                                    newHexInput = "#"
                                    inputError = false
                                } else {
                                    inputError = true
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = naturePrimaryGreen),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text(strings.colorPickerAddButton, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Close Button
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEDF4E8)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(strings.doneButton, color = natureTextDark, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
