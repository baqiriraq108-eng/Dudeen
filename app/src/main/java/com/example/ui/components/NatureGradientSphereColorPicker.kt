package com.example.ui.components

import android.widget.Toast
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.LocalAppLanguage
import com.example.ui.theme.GeoBorder
import com.example.ui.theme.GeoOnSurface
import com.example.ui.theme.GeoOnSurfaceVariant
import com.example.ui.theme.GeoPrimary
import com.example.ui.theme.GeoSurface
import com.example.ui.theme.GeoSurfaceVariant
import com.example.util.CustomColorManager
import com.example.util.DeclensionHistoryExportHelper
import java.util.Locale
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Bidirectional Color Engine supporting HEX, RGB, HSL, and CMYK with full 0..1 lightness & saturation range
 * so pure black (#000000), pure white (#FFFFFF), and neutral grays are preserved accurately.
 */
object NatureColorEngine {

    data class HslColor(
        val hue: Float,        // 0..360
        val saturation: Float, // 0..1
        val lightness: Float   // 0..1
    ) {
        val rgb: Triple<Int, Int, Int> get() = hslToRgb(hue, saturation, lightness)
        val hex: String get() = hslToHex(hue, saturation, lightness)
        val composeColor: Color
            get() {
                val (r, g, b) = rgb
                return Color(r, g, b)
            }
        val cmyk: List<Int>
            get() {
                val (r, g, b) = rgb
                return rgbToCmyk(r, g, b)
            }
    }

    fun hslToRgb(h: Float, s: Float, l: Float): Triple<Int, Int, Int> {
        val cleanS = s.coerceIn(0f, 1f)
        val cleanL = l.coerceIn(0f, 1f)
        val c = (1f - abs(2f * cleanL - 1f)) * cleanS
        val hPrime = ((h % 360f + 360f) % 360f) / 60f
        val x = c * (1f - abs((hPrime % 2f) - 1f))
        val m = cleanL - c / 2f
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
        val rNorm = (r.coerceIn(0, 255)) / 255f
        val gNorm = (g.coerceIn(0, 255)) / 255f
        val bNorm = (b.coerceIn(0, 255)) / 255f
        val max = maxOf(rNorm, gNorm, bNorm)
        val min = minOf(rNorm, gNorm, bNorm)
        val delta = max - min
        val l = (max + min) / 2f
        val s = if (delta <= 0.0001f) 0f else (delta / (1f - abs(2f * l - 1f))).coerceIn(0f, 1f)
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
        return rgbToHex(r, g, b)
    }

    fun rgbToHex(r: Int, g: Int, b: Int): String {
        return String.format(Locale.ROOT, "#%02X%02X%02X", r.coerceIn(0, 255), g.coerceIn(0, 255), b.coerceIn(0, 255))
    }

    fun normalizeHexCode(hex: String): String {
        val clean = hex.trim().removePrefix("#").uppercase(Locale.ROOT)
        return when (clean.length) {
            3 -> "#${clean[0]}${clean[0]}${clean[1]}${clean[1]}${clean[2]}${clean[2]}"
            6 -> "#$clean"
            8 -> "#${clean.takeLast(6)}"
            else -> hexToHsl(hex).hex
        }
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
                else -> Triple(0, 0, 0)
            }
            rgbToHsl(r, g, b)
        } catch (_: Exception) {
            HslColor(0f, 0f, 0f)
        }
    }

    fun rgbToCmyk(r: Int, g: Int, b: Int): List<Int> {
        val rNorm = r.coerceIn(0, 255) / 255f
        val gNorm = g.coerceIn(0, 255) / 255f
        val bNorm = b.coerceIn(0, 255) / 255f
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
        val s = current.saturation.coerceIn(0.45f, 1.0f)
        val l = current.lightness.coerceIn(0.32f, 0.68f)

        val g1 = listOf(
            hslToHex((h - 30f + 360f) % 360f, (s * 1.05f).coerceIn(0.4f, 1f), (l * 0.9f).coerceIn(0.25f, 0.65f)),
            hslToHex(h, s, l),
            hslToHex((h + 30f) % 360f, (s * 0.95f).coerceIn(0.4f, 1f), (l * 1.05f).coerceIn(0.35f, 0.72f)),
            hslToHex((h + 60f) % 360f, s, (l * 1.15f).coerceIn(0.4f, 0.78f))
        )

        val g2 = listOf(
            hslToHex((h + 150f) % 360f, 0.85f, 0.45f),
            hslToHex((h + 180f) % 360f, 0.92f, 0.50f),
            hslToHex((h + 210f) % 360f, 0.85f, 0.58f),
            hslToHex(h, (s * 0.8f).coerceIn(0.3f, 1f), 0.30f)
        )

        val g3 = listOf(
            hslToHex(h, s, l),
            hslToHex((h + 120f) % 360f, 0.88f, 0.48f),
            hslToHex((h + 240f) % 360f, 0.90f, 0.48f),
            hslToHex((h + 180f) % 360f, 0.80f, 0.56f)
        )

        val g4 = listOf(
            "#000000",
            "#1E293B",
            "#475569",
            "#94A3B8"
        )

        val g5 = listOf(
            "#1565C0",
            "#C2185B",
            "#2E7D32",
            "#EA580C"
        )

        return listOf(g1, g2, g3, g4, g5)
    }
}

/**
 * Interactive 2D Spectrum Wheel Canvas Component.
 * Uses rememberUpdatedState to avoid stale gesture closures when dragging or tapping.
 */
@Composable
fun GradientSphereCanvas(
    currentHsl: NatureColorEngine.HslColor,
    modeIndex: Int, // 0=RGB, 1=HSL, 2=HEX, 3=CMYK
    onColorChanged: (hue: Float, saturation: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val latestOnColorChanged by rememberUpdatedState(onColorChanged)
    val borderColor = GeoBorder
    val primaryColor = GeoPrimary

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    fun updateFromOffset(offset: Offset) {
                        val sizePx = minOf(size.width, size.height).toFloat()
                        if (sizePx <= 0f) return
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val radius = (sizePx / 2f) * 0.86f
                        val dx = offset.x - cx
                        val dy = offset.y - cy
                        val dist = hypot(dx, dy).coerceIn(0f, radius)
                        val angleRad = atan2(dy, dx)
                        val angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat().let {
                            if (it < 0f) it + 360f else it
                        }
                        val sat = (dist / radius).coerceIn(0f, 1f)
                        latestOnColorChanged(angleDeg, sat)
                    }

                    detectTapGestures(
                        onTap = { offset -> updateFromOffset(offset) }
                    )
                }
                .pointerInput(Unit) {
                    fun updateFromOffset(offset: Offset) {
                        val sizePx = minOf(size.width, size.height).toFloat()
                        if (sizePx <= 0f) return
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val radius = (sizePx / 2f) * 0.86f
                        val dx = offset.x - cx
                        val dy = offset.y - cy
                        val dist = hypot(dx, dy).coerceIn(0f, radius)
                        val angleRad = atan2(dy, dx)
                        val angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat().let {
                            if (it < 0f) it + 360f else it
                        }
                        val sat = (dist / radius).coerceIn(0f, 1f)
                        latestOnColorChanged(angleDeg, sat)
                    }

                    detectDragGestures(
                        onDragStart = { offset -> updateFromOffset(offset) },
                        onDrag = { change, _ ->
                            change.consume()
                            updateFromOffset(change.position)
                        }
                    )
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val sphereRadius = (size.minDimension / 2f) * 0.86f
            val orbitalRadius = (size.minDimension / 2f) * 0.95f

            val glowColor = when (modeIndex) {
                0 -> Color(0x28E53935)
                1 -> primaryColor.copy(alpha = 0.18f)
                2 -> Color(0x281E88E5)
                3 -> Color(0x2800ACC1)
                else -> primaryColor.copy(alpha = 0.18f)
            }
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor, Color.Transparent),
                    center = center,
                    radius = orbitalRadius * 1.05f
                ),
                radius = orbitalRadius * 1.05f,
                center = center
            )

            drawCircle(
                color = borderColor,
                radius = orbitalRadius,
                center = center,
                style = Stroke(width = 1.2.dp.toPx())
            )

            when (modeIndex) {
                0 -> {
                    listOf(0.0 to Color(0xFFE53935), 120.0 to Color(0xFF43A047), 240.0 to Color(0xFF1E88E5)).forEach { (deg, col) ->
                        val rad = Math.toRadians(deg)
                        val pos = Offset(
                            center.x + orbitalRadius * cos(rad).toFloat(),
                            center.y + orbitalRadius * sin(rad).toFloat()
                        )
                        drawCircle(color = col, radius = 4.dp.toPx(), center = pos)
                    }
                }
                3 -> {
                    listOf(180.0 to Color(0xFF00ACC1), 300.0 to Color(0xFFD81B60), 60.0 to Color(0xFFFDD835)).forEach { (deg, col) ->
                        val rad = Math.toRadians(deg)
                        val pos = Offset(
                            center.x + orbitalRadius * cos(rad).toFloat(),
                            center.y + orbitalRadius * sin(rad).toFloat()
                        )
                        drawCircle(color = col, radius = 4.dp.toPx(), center = pos)
                    }
                }
                else -> {
                    listOf(0.0, 90.0, 180.0, 270.0).forEach { deg ->
                        val rad = Math.toRadians(deg)
                        val pos = Offset(
                            center.x + orbitalRadius * cos(rad).toFloat(),
                            center.y + orbitalRadius * sin(rad).toFloat()
                        )
                        drawCircle(color = primaryColor.copy(alpha = 0.6f), radius = 3.dp.toPx(), center = pos)
                    }
                }
            }

            val spectrumColors = listOf(
                Color(0xFFFF0000),
                Color(0xFFFF8000),
                Color(0xFFFFFF00),
                Color(0xFF00E676),
                Color(0xFF00E5FF),
                Color(0xFF2979FF),
                Color(0xFF8E24AA),
                Color(0xFFFF0080),
                Color(0xFFFF0000)
            )

            drawCircle(
                brush = Brush.sweepGradient(spectrumColors, center),
                radius = sphereRadius,
                center = center
            )

            // White core fading to pure saturation at circumference
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        Color(0xD9FFFFFF),
                        Color(0x59FFFFFF),
                        Color.Transparent
                    ),
                    center = center,
                    radius = sphereRadius
                ),
                radius = sphereRadius,
                center = center
            )

            // Reflect current lightness dark/light overlay subtly so wheel reflects dark/light selections
            val lightnessOverlayAlpha = ((0.5f - currentHsl.lightness) * 1.1f).coerceIn(-0.55f, 0.65f)
            if (lightnessOverlayAlpha > 0.02f) {
                drawCircle(
                    color = Color.Black.copy(alpha = lightnessOverlayAlpha),
                    radius = sphereRadius,
                    center = center
                )
            } else if (lightnessOverlayAlpha < -0.02f) {
                drawCircle(
                    color = Color.White.copy(alpha = -lightnessOverlayAlpha * 0.7f),
                    radius = sphereRadius,
                    center = center
                )
            }

            drawCircle(
                color = borderColor,
                radius = sphereRadius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Live selection indicator
            val indicatorAngleRad = Math.toRadians(currentHsl.hue.toDouble())
            val indicatorDist = currentHsl.saturation.coerceIn(0f, 1f) * sphereRadius
            val indicatorCenter = Offset(
                center.x + (indicatorDist * cos(indicatorAngleRad)).toFloat(),
                center.y + (indicatorDist * sin(indicatorAngleRad)).toFloat()
            )

            drawCircle(
                color = Color.White,
                radius = 9.5.dp.toPx(),
                center = indicatorCenter,
                style = Stroke(width = 2.5.dp.toPx())
            )
            drawCircle(
                color = Color(0xFF0F172A),
                radius = 11.dp.toPx(),
                center = indicatorCenter,
                style = Stroke(width = 1.2.dp.toPx())
            )
            drawCircle(
                color = currentHsl.composeColor,
                radius = 6.dp.toPx(),
                center = indicatorCenter
            )
        }
    }
}

/**
 * Subtle decorative corner accents adapting to current theme.
 */
@Composable
fun BotanicalAtmosphereBackground(modifier: Modifier = Modifier) {
    val accentTint = GeoPrimary.copy(alpha = 0.05f)
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

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
            drawPath(path, color = accentTint)
        }

        drawLeaf(18f, 18f, 26f, 35f)
        drawLeaf(w - 20f, 18f, 24f, -40f)
        drawLeaf(20f, h - 20f, 24f, -45f)
        drawLeaf(w - 22f, h - 22f, 26f, 40f)
    }
}

/**
 * Unified Color Studio Dialog.
 * Features:
 * - Interactive 2D spectrum wheel with live lightness feedback
 * - Live swatch preview with contrast-aware hex readout, copy button, and RGB/HSL/CMYK metrics
 * - Synchronized HSL, RGB, HEX, and CMYK channel controls using rememberUpdatedState (zero stale closures)
 * - Non-interrupting live hex input (never overwrites 3-char partial input mid-typing)
 * - Quick preset colors row + dynamic harmony palettes + saved custom colors manager
 * - Theme-aware surfaces and standardized outlined buttons
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

    val initialHsl = remember(initialColorHex) {
        if (initialColorHex.isNotBlank() && CustomColorManager.isValidHex(initialColorHex)) {
            NatureColorEngine.hexToHsl(initialColorHex)
        } else {
            NatureColorEngine.HslColor(0f, 0f, 0f)
        }
    }

    var currentHue by remember { mutableFloatStateOf(initialHsl.hue) }
    var currentSaturation by remember { mutableFloatStateOf(initialHsl.saturation.coerceIn(0f, 1f)) }
    var currentLightness by remember { mutableFloatStateOf(initialHsl.lightness.coerceIn(0f, 1f)) }

    val currentHslColor = NatureColorEngine.HslColor(currentHue, currentSaturation, currentLightness)
    val currentHex = currentHslColor.hex
    val currentRgb = currentHslColor.rgb
    val currentCmyk = currentHslColor.cmyk

    var selectedModeIndex by remember { mutableIntStateOf(1) } // 0=RGB, 1=HSL, 2=HEX, 3=CMYK
    var hexInputText by remember { mutableStateOf(currentHex) }
    var hexInputError by remember { mutableStateOf(false) }

    // Helper to update color from non-text controls (wheel, sliders, swatches) and keep hexInputText in sync
    val updateHslFromControl: (Float, Float, Float) -> Unit = { h, s, l ->
        val cleanH = h.coerceIn(0f, 360f)
        val cleanS = s.coerceIn(0f, 1f)
        val cleanL = l.coerceIn(0f, 1f)
        currentHue = cleanH
        currentSaturation = cleanS
        currentLightness = cleanL
        hexInputText = NatureColorEngine.hslToHex(cleanH, cleanS, cleanL)
        hexInputError = false
    }

    // Handler for typing in hex text fields without overwriting partial 3-digit input mid-keystroke
    val handleHexTextEdited: (String) -> Unit = { rawInput ->
        hexInputError = false
        val filtered = rawInput.uppercase(Locale.ROOT)
            .filter { it in '0'..'9' || it in 'A'..'F' }
            .take(6)
        hexInputText = "#$filtered"
        if (filtered.length == 6) {
            val hsl = NatureColorEngine.hexToHsl("#$filtered")
            currentHue = hsl.hue
            currentSaturation = hsl.saturation
            currentLightness = hsl.lightness
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

    val cardBg = GeoSurface
    val containerBg = MaterialTheme.colorScheme.surfaceContainer
    val surfaceVariantBg = GeoSurfaceVariant
    val borderColor = GeoBorder
    val textDark = GeoOnSurface
    val textMuted = GeoOnSurfaceVariant
    val primaryAccent = GeoPrimary

    val copiedToastText = when (appLanguage) {
        "ar" -> "تم نسخ $currentHex"
        "en" -> "$currentHex copied"
        else -> "$currentHex kopiert"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.2.dp, borderColor),
            modifier = modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 520.dp)
                .heightIn(max = maxDialogHeight)
                .shadow(12.dp, RoundedCornerShape(22.dp))
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                BotanicalAtmosphereBackground(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(22.dp))
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ==========================================
                    // 1. HEADER: Icon + Title + Subtitle + Close
                    // ==========================================
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(primaryAccent.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = primaryAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.naturePickerTitle,
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = textDark,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = strings.naturePickerSubtitle,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = textMuted,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = strings.close,
                                tint = textMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // =========================================================================
                    // 2. MAIN STUDIO CONTAINER: Spectrum Wheel + Preview + Mode Controls
                    // =========================================================================
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = containerBg,
                        border = BorderStroke(1.dp, borderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                val isNarrow = maxWidth < 330.dp
                                if (isNarrow) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        GradientSphereCanvas(
                                            currentHsl = currentHslColor,
                                            modeIndex = selectedModeIndex,
                                            onColorChanged = { h, s ->
                                                // If lightness was at 0 (black) or 1 (white), bring to 0.5f so wheel selection is visible
                                                val effectiveL = if (currentLightness <= 0.03f || currentLightness >= 0.97f) 0.5f else currentLightness
                                                updateHslFromControl(h, s, effectiveL)
                                            },
                                            modifier = Modifier
                                                .size(156.dp)
                                                .testTag("gradient_sphere_canvas")
                                        )

                                        ColorPreviewAndModesPanel(
                                            currentHex = currentHex,
                                            currentRgb = currentRgb,
                                            currentHue = currentHue,
                                            currentSaturation = currentSaturation,
                                            currentLightness = currentLightness,
                                            currentHslColor = currentHslColor,
                                            selectedModeIndex = selectedModeIndex,
                                            onModeSelected = { selectedModeIndex = it },
                                            onCopyHex = {
                                                clipboardManager.setText(AnnotatedString(currentHex))
                                                Toast.makeText(context, copiedToastText, Toast.LENGTH_SHORT).show()
                                            },
                                            textDark = textDark,
                                            textMuted = textMuted,
                                            primaryAccent = primaryAccent,
                                            surfaceVariantBg = surfaceVariantBg,
                                            borderColor = borderColor,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        GradientSphereCanvas(
                                            currentHsl = currentHslColor,
                                            modeIndex = selectedModeIndex,
                                            onColorChanged = { h, s ->
                                                val effectiveL = if (currentLightness <= 0.03f || currentLightness >= 0.97f) 0.5f else currentLightness
                                                updateHslFromControl(h, s, effectiveL)
                                            },
                                            modifier = Modifier
                                                .size(156.dp)
                                                .testTag("gradient_sphere_canvas")
                                        )

                                        ColorPreviewAndModesPanel(
                                            currentHex = currentHex,
                                            currentRgb = currentRgb,
                                            currentHue = currentHue,
                                            currentSaturation = currentSaturation,
                                            currentLightness = currentLightness,
                                            currentHslColor = currentHslColor,
                                            selectedModeIndex = selectedModeIndex,
                                            onModeSelected = { selectedModeIndex = it },
                                            onCopyHex = {
                                                clipboardManager.setText(AnnotatedString(currentHex))
                                                Toast.makeText(context, copiedToastText, Toast.LENGTH_SHORT).show()
                                            },
                                            textDark = textDark,
                                            textMuted = textMuted,
                                            primaryAccent = primaryAccent,
                                            surfaceVariantBg = surfaceVariantBg,
                                            borderColor = borderColor,
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
                                    RgbControlPanel(
                                        r = currentRgb.first,
                                        g = currentRgb.second,
                                        b = currentRgb.third,
                                        onRgbChanged = { newR, newG, newB ->
                                            val newHsl = NatureColorEngine.rgbToHsl(newR, newG, newB)
                                            updateHslFromControl(newHsl.hue, newHsl.saturation, newHsl.lightness)
                                        },
                                        textDark = textDark,
                                        appLanguage = appLanguage
                                    )
                                }
                                1 -> {
                                    HslControlPanel(
                                        hue = currentHue,
                                        saturation = currentSaturation,
                                        lightness = currentLightness,
                                        currentHslColor = currentHslColor,
                                        onHueChanged = { updateHslFromControl(it, currentSaturation, currentLightness) },
                                        onSaturationChanged = { updateHslFromControl(currentHue, it, currentLightness) },
                                        onLightnessChanged = { updateHslFromControl(currentHue, currentSaturation, it) },
                                        textDark = textDark,
                                        appLanguage = appLanguage
                                    )
                                }
                                2 -> {
                                    HexControlPanel(
                                        hexText = hexInputText,
                                        onHexChange = handleHexTextEdited,
                                        hasError = hexInputError,
                                        currentHslColor = currentHslColor,
                                        textDark = textDark,
                                        textMuted = textMuted,
                                        primaryAccent = primaryAccent,
                                        surfaceBg = cardBg,
                                        borderColor = borderColor
                                    )
                                }
                                3 -> {
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
                                            updateHslFromControl(newHsl.hue, newHsl.saturation, newHsl.lightness)
                                        },
                                        textDark = textDark
                                    )
                                }
                            }
                        }
                    }

                    // ==========================================
                    // 4. QUICK PRESETS & HARMONY SUGGESTIONS
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
                                    tint = primaryAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = strings.harmonySuggestionsTitle,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textDark
                                )
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = textMuted,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        // Standard document & theme preset swatches row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DeclensionHistoryExportHelper.PREDEFINED_COLORS.forEach { (_, presetHex) ->
                                val swatchColor = NatureColorEngine.hexToHsl(presetHex).composeColor
                                val isSelected = presetHex.equals(currentHex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(swatchColor)
                                        .border(
                                            width = if (isSelected) 2.5.dp else 1.dp,
                                            color = if (isSelected) primaryAccent else borderColor,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            val hsl = NatureColorEngine.hexToHsl(presetHex)
                                            updateHslFromControl(hsl.hue, hsl.saturation, hsl.lightness)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = if (swatchColor.luminance() > 0.55f) Color.Black else Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Harmony Palettes horizontal scroll row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            harmonyPalettes.forEach { paletteGroup ->
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = surfaceVariantBg,
                                    border = BorderStroke(1.dp, borderColor),
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
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
                                                        color = if (isSelected) primaryAccent else borderColor,
                                                        shape = CircleShape
                                                    )
                                                    .clickable {
                                                        val hsl = NatureColorEngine.hexToHsl(hex)
                                                        updateHslFromControl(hsl.hue, hsl.saturation, hsl.lightness)
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
                        shape = RoundedCornerShape(12.dp),
                        color = surfaceVariantBg,
                        border = BorderStroke(1.dp, borderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSavedColorsDialog = true }
                            .testTag("saved_colors_section_button")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 9.dp),
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
                                    tint = primaryAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = strings.savedColorsTitle,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textDark
                                )
                                Text(
                                    text = "(${customColors.size})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textMuted
                                )
                            }

                            val displaySaved = if (customColors.isNotEmpty()) {
                                customColors.take(6)
                            } else {
                                listOf("#E53935", "#4CAF50", "#2196F3", "#FF9800", "#9C27B0")
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
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
                                                color = if (isSelected) primaryAccent else borderColor,
                                                shape = CircleShape
                                            )
                                    )
                                }

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = strings.savedColorsTitle,
                                    tint = textMuted,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }

                    // ==========================================
                    // 6. LIVE HEX INPUT & ADD CUSTOM COLOR
                    // ==========================================
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircleOutline,
                                contentDescription = null,
                                tint = primaryAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = strings.colorPickerAddCustomColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = textDark
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = containerBg,
                                border = BorderStroke(
                                    1.2.dp,
                                    if (hexInputError) MaterialTheme.colorScheme.error else borderColor
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
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
                                        onValueChange = handleHexTextEdited,
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            color = textDark,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        cursorBrush = SolidColor(primaryAccent),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("add_custom_hex_input")
                                    )

                                    if (hexInputText.length > 1) {
                                        IconButton(
                                            onClick = { hexInputText = "#" },
                                            modifier = Modifier.size(22.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = strings.close,
                                                tint = textMuted,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(currentHslColor.composeColor)
                                            .border(1.dp, borderColor, CircleShape)
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    if (CustomColorManager.isValidHex(hexInputText)) {
                                        val formatted = CustomColorManager.normalizeHex(hexInputText)
                                        CustomColorManager.addCustomColor(context, formatted)
                                        customColors = CustomColorManager.getCustomColors(context)
                                        val hsl = NatureColorEngine.hexToHsl(formatted)
                                        updateHslFromControl(hsl.hue, hsl.saturation, hsl.lightness)
                                        Toast.makeText(context, formatted, Toast.LENGTH_SHORT).show()
                                    } else {
                                        hexInputError = true
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, borderColor),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = surfaceVariantBg,
                                    contentColor = textDark
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                modifier = Modifier
                                    .widthIn(min = 96.dp)
                                    .height(42.dp)
                                    .testTag("add_custom_color_button")
                            ) {
                                AutoSizingText(
                                    text = strings.colorPickerAddButton,
                                    maxFontSize = 13.sp,
                                    minFontSize = 9.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textDark
                                )
                            }
                        }
                    }

                    // ==========================================
                    // 7. BOTTOM ACTIONS: Cancel / Confirm
                    // ==========================================
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, borderColor),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = textDark
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("color_picker_cancel_button")
                        ) {
                            AutoSizingText(
                                text = strings.cancel,
                                maxFontSize = 13.sp,
                                minFontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textDark
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val finalHex = if (CustomColorManager.isValidHex(hexInputText)) {
                                    CustomColorManager.normalizeHex(hexInputText)
                                } else {
                                    currentHex
                                }
                                onColorChosen(finalHex)
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, borderColor),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = textDark
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("color_picker_apply_button")
                        ) {
                            AutoSizingText(
                                text = strings.confirm,
                                maxFontSize = 13.sp,
                                minFontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textDark
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSavedColorsDialog) {
        SavedColorsManagementDialog(
            customColors = customColors,
            selectedHex = currentHex,
            onSelectColor = { chosenHex ->
                val hsl = NatureColorEngine.hexToHsl(chosenHex)
                updateHslFromControl(hsl.hue, hsl.saturation, hsl.lightness)
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
            cardBg = cardBg,
            containerBg = containerBg,
            borderColor = borderColor,
            textDark = textDark,
            textMuted = textMuted,
            primaryAccent = primaryAccent
        )
    }

    if (colorToDelete != null) {
        val targetHex = colorToDelete!!
        AlertDialog(
            onDismissRequest = { colorToDelete = null },
            containerColor = cardBg,
            title = {
                Text(
                    text = strings.colorPickerDeleteTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textDark
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
                            .border(1.dp, borderColor, CircleShape)
                    )
                    Text(
                        text = strings.deleteColorPrompt(targetHex),
                        fontSize = 13.sp,
                        color = textMuted
                    )
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { colorToDelete = null },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, borderColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = textDark),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        AutoSizingText(
                            text = strings.cancel,
                            maxFontSize = 13.sp,
                            minFontSize = 9.5.sp,
                            color = textDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            CustomColorManager.removeCustomColor(context, targetHex)
                            customColors = CustomColorManager.getCustomColors(context)
                            colorToDelete = null
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        border = BorderStroke(1.dp, borderColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = textDark),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        AutoSizingText(
                            text = strings.confirm,
                            maxFontSize = 13.sp,
                            minFontSize = 9.5.sp,
                            color = textDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        )
    }
}

/**
 * Reusable Gradient Track Slider with rememberUpdatedState so sequential channel drags never use stale closures.
 */
@Composable
private fun ColorGradientTrackSlider(
    label: String,
    valueText: String,
    fraction: Float,
    gradientColors: List<Color>,
    thumbFillColor: Color,
    textDark: Color,
    onFractionChanged: (Float) -> Unit
) {
    val latestOnFractionChanged by rememberUpdatedState(onFractionChanged)
    val cleanFraction = fraction.coerceIn(0f, 1f)

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
                color = textDark
            )
            Text(
                text = valueText,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = textDark
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { offset ->
                            if (size.width > 0) {
                                latestOnFractionChanged((offset.x / size.width).coerceIn(0f, 1f))
                            }
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            if (size.width > 0) {
                                latestOnFractionChanged((offset.x / size.width).coerceIn(0f, 1f))
                            }
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            if (size.width > 0) {
                                latestOnFractionChanged((change.position.x / size.width).coerceIn(0f, 1f))
                            }
                        }
                    )
                }
        ) {
            val trackHeight = 10.dp.toPx()
            val cy = size.height / 2f
            val trackY = cy - trackHeight / 2f
            val thumbRadius = 8.5.dp.toPx()

            drawRoundRect(
                brush = Brush.horizontalGradient(gradientColors),
                topLeft = Offset(0f, trackY),
                size = Size(size.width, trackHeight),
                cornerRadius = CornerRadius(trackHeight / 2f)
            )
            drawRoundRect(
                color = Color(0x33000000),
                topLeft = Offset(0f, trackY),
                size = Size(size.width, trackHeight),
                cornerRadius = CornerRadius(trackHeight / 2f),
                style = Stroke(width = 1.dp.toPx())
            )

            val usableWidth = (size.width - thumbRadius * 2f).coerceAtLeast(1f)
            val thumbX = thumbRadius + cleanFraction * usableWidth

            drawCircle(
                color = Color.White,
                radius = thumbRadius,
                center = Offset(thumbX, cy)
            )
            drawCircle(
                color = Color(0xFF0F172A),
                radius = thumbRadius,
                center = Offset(thumbX, cy),
                style = Stroke(width = 1.5.dp.toPx())
            )
            drawCircle(
                color = thumbFillColor,
                radius = 4.8.dp.toPx(),
                center = Offset(thumbX, cy)
            )
        }
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
    textDark: Color,
    appLanguage: String
) {
    val latestR by rememberUpdatedState(r)
    val latestG by rememberUpdatedState(g)
    val latestB by rememberUpdatedState(b)
    val latestOnRgbChanged by rememberUpdatedState(onRgbChanged)

    val rLabel = when (appLanguage) { "ar" -> "أحمر (R)"; "en" -> "Red (R)"; else -> "Rot (R)" }
    val gLabel = when (appLanguage) { "ar" -> "أخضر (G)"; "en" -> "Green (G)"; else -> "Grün (G)" }
    val bLabel = when (appLanguage) { "ar" -> "أزرق (B)"; "en" -> "Blue (B)"; else -> "Blau (B)" }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ColorGradientTrackSlider(
            label = rLabel,
            valueText = "$r",
            fraction = r / 255f,
            gradientColors = listOf(Color(0, g, b), Color(255, g, b)),
            thumbFillColor = Color(r, g, b),
            textDark = textDark,
            onFractionChanged = { pct ->
                latestOnRgbChanged((pct * 255f).roundToInt().coerceIn(0, 255), latestG, latestB)
            }
        )
        ColorGradientTrackSlider(
            label = gLabel,
            valueText = "$g",
            fraction = g / 255f,
            gradientColors = listOf(Color(r, 0, b), Color(r, 255, b)),
            thumbFillColor = Color(r, g, b),
            textDark = textDark,
            onFractionChanged = { pct ->
                latestOnRgbChanged(latestR, (pct * 255f).roundToInt().coerceIn(0, 255), latestB)
            }
        )
        ColorGradientTrackSlider(
            label = bLabel,
            valueText = "$b",
            fraction = b / 255f,
            gradientColors = listOf(Color(r, g, 0), Color(r, g, 255)),
            thumbFillColor = Color(r, g, b),
            textDark = textDark,
            onFractionChanged = { pct ->
                latestOnRgbChanged(latestR, latestG, (pct * 255f).roundToInt().coerceIn(0, 255))
            }
        )
    }
}

/**
 * Functional HSL Slider Control Panel (Hue 0..360, Saturation 0..100%, Lightness 0..100%).
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
    textDark: Color,
    appLanguage: String
) {
    val hLabel = when (appLanguage) { "ar" -> "درجة اللون (H)"; "en" -> "Hue (H)"; else -> "Farbton (H)" }
    val sLabel = when (appLanguage) { "ar" -> "التشبع (S)"; "en" -> "Saturation (S)"; else -> "Sättigung (S)" }
    val lLabel = when (appLanguage) { "ar" -> "الإضاءة (L)"; "en" -> "Lightness (L)"; else -> "Helligkeit (L)" }

    val rainbowColors = remember {
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
    }

    val minSatColor = NatureColorEngine.HslColor(hue, 0f, lightness.coerceIn(0.15f, 0.85f)).composeColor
    val maxSatColor = NatureColorEngine.HslColor(hue, 1f, lightness.coerceIn(0.15f, 0.85f)).composeColor
    val midLightColor = NatureColorEngine.HslColor(hue, saturation.coerceAtLeast(0.5f), 0.5f).composeColor

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ColorGradientTrackSlider(
            label = hLabel,
            valueText = "${hue.roundToInt()}°",
            fraction = hue / 360f,
            gradientColors = rainbowColors,
            thumbFillColor = currentHslColor.composeColor,
            textDark = textDark,
            onFractionChanged = { pct -> onHueChanged(pct * 360f) }
        )

        ColorGradientTrackSlider(
            label = sLabel,
            valueText = "${(saturation * 100).roundToInt()}%",
            fraction = saturation,
            gradientColors = listOf(minSatColor, maxSatColor),
            thumbFillColor = currentHslColor.composeColor,
            textDark = textDark,
            onFractionChanged = { pct -> onSaturationChanged(pct.coerceIn(0f, 1f)) }
        )

        ColorGradientTrackSlider(
            label = lLabel,
            valueText = "${(lightness * 100).roundToInt()}%",
            fraction = lightness,
            gradientColors = listOf(Color.Black, midLightColor, Color.White),
            thumbFillColor = currentHslColor.composeColor,
            textDark = textDark,
            onFractionChanged = { pct -> onLightnessChanged(pct.coerceIn(0f, 1f)) }
        )
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
    textDark: Color,
    textMuted: Color,
    primaryAccent: Color,
    surfaceBg: Color,
    borderColor: Color
) {
    val appLanguage = LocalAppLanguage.current
    val strings = remember(appLanguage) { AppStrings.get(appLanguage) }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = strings.enterHexCodeLabel,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = textDark
        )

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = surfaceBg,
            border = BorderStroke(
                1.2.dp,
                if (hasError) MaterialTheme.colorScheme.error else borderColor
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
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
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryAccent
                )

                BasicTextField(
                    value = hexText.removePrefix("#"),
                    onValueChange = { onHexChange(it) },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = textDark,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    cursorBrush = SolidColor(primaryAccent),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("hex_mode_input")
                )

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(currentHslColor.composeColor)
                        .border(1.dp, borderColor, CircleShape)
                )
            }
        }

        Text(
            text = strings.hexInputHelperText,
            fontSize = 11.sp,
            color = textMuted
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
    textDark: Color
) {
    val latestC by rememberUpdatedState(c)
    val latestM by rememberUpdatedState(m)
    val latestY by rememberUpdatedState(y)
    val latestK by rememberUpdatedState(k)
    val latestOnCmykChanged by rememberUpdatedState(onCmykChanged)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ColorGradientTrackSlider(
            label = "Cyan (C)",
            valueText = "$c%",
            fraction = c / 100f,
            gradientColors = listOf(Color.White, Color(0xFF00ACC1)),
            thumbFillColor = Color(0xFF00ACC1),
            textDark = textDark,
            onFractionChanged = { pct ->
                latestOnCmykChanged((pct * 100f).roundToInt().coerceIn(0, 100), latestM, latestY, latestK)
            }
        )
        ColorGradientTrackSlider(
            label = "Magenta (M)",
            valueText = "$m%",
            fraction = m / 100f,
            gradientColors = listOf(Color.White, Color(0xFFD81B60)),
            thumbFillColor = Color(0xFFD81B60),
            textDark = textDark,
            onFractionChanged = { pct ->
                latestOnCmykChanged(latestC, (pct * 100f).roundToInt().coerceIn(0, 100), latestY, latestK)
            }
        )
        ColorGradientTrackSlider(
            label = "Yellow (Y)",
            valueText = "$y%",
            fraction = y / 100f,
            gradientColors = listOf(Color.White, Color(0xFFFDD835)),
            thumbFillColor = Color(0xFFFDD835),
            textDark = textDark,
            onFractionChanged = { pct ->
                latestOnCmykChanged(latestC, latestM, (pct * 100f).roundToInt().coerceIn(0, 100), latestK)
            }
        )
        ColorGradientTrackSlider(
            label = "Key / Black (K)",
            valueText = "$k%",
            fraction = k / 100f,
            gradientColors = listOf(Color.White, Color(0xFF212121)),
            thumbFillColor = Color(0xFF212121),
            textDark = textDark,
            onFractionChanged = { pct ->
                latestOnCmykChanged(latestC, latestM, latestY, (pct * 100f).roundToInt().coerceIn(0, 100))
            }
        )
    }
}

/**
 * Preview Panel displaying live swatch, values (HEX, RGB, HSL), and mode selector buttons.
 */
@Composable
private fun ColorPreviewAndModesPanel(
    currentHex: String,
    currentRgb: Triple<Int, Int, Int>,
    currentHue: Float,
    currentSaturation: Float,
    currentLightness: Float,
    currentHslColor: NatureColorEngine.HslColor,
    selectedModeIndex: Int,
    onModeSelected: (Int) -> Unit,
    onCopyHex: () -> Unit,
    textDark: Color,
    textMuted: Color,
    primaryAccent: Color,
    surfaceVariantBg: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    val appLanguage = LocalAppLanguage.current
    val strings = remember(appLanguage) { AppStrings.get(appLanguage) }
    val previewColor = currentHslColor.composeColor
    val onPreviewColor = if (previewColor.luminance() > 0.55f) Color(0xFF0F172A) else Color.White

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = surfaceVariantBg,
            border = BorderStroke(1.dp, borderColor),
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
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textDark
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(previewColor)
                            .border(1.2.dp, borderColor, RoundedCornerShape(12.dp))
                            .testTag("color_preview_swatch"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aa",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = onPreviewColor
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = currentHex,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = textDark
                            )
                            IconButton(
                                onClick = onCopyHex,
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy HEX",
                                    tint = textMuted,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }

                        Text(
                            text = "RGB  ${currentRgb.first}, ${currentRgb.second}, ${currentRgb.third}",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = textMuted,
                            maxLines = 1
                        )

                        Text(
                            text = "HSL  ${currentHue.roundToInt()}°, ${(currentSaturation * 100).roundToInt()}%, ${(currentLightness * 100).roundToInt()}%",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = textMuted,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = strings.colorModesLabel,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = textDark
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
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) primaryAccent.copy(alpha = 0.14f) else surfaceVariantBg)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) primaryAccent else borderColor,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onModeSelected(index) }
                            .testTag("color_mode_$modeName"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = modeName,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isSelected) primaryAccent else textDark
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
    cardBg: Color,
    containerBg: Color,
    borderColor: Color,
    textDark: Color,
    textMuted: Color,
    primaryAccent: Color
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
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.2.dp, borderColor),
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
                            tint = primaryAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = strings.savedColorsTitle,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textDark
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = strings.close,
                            tint = textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = strings.savedColorsSubtitle,
                    fontSize = 12.sp,
                    color = textMuted
                )

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
                            color = textMuted
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
                                color = containerBg,
                                border = BorderStroke(
                                    if (isSelected) 1.8.dp else 1.dp,
                                    if (isSelected) primaryAccent else borderColor
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
                                            .border(1.dp, borderColor, CircleShape)
                                    )

                                    Text(
                                        text = hex,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = textDark
                                    )

                                    IconButton(
                                        onClick = { onDeleteColor(hex) },
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = strings.delete,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = borderColor, thickness = 1.dp)

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = strings.addNewColorLabel,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textDark
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = containerBg,
                            border = BorderStroke(
                                1.dp,
                                if (inputError) MaterialTheme.colorScheme.error else borderColor
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            BasicTextField(
                                value = newHexInput,
                                onValueChange = { raw ->
                                    inputError = false
                                    val filtered = raw.uppercase(Locale.ROOT)
                                        .filter { it in '0'..'9' || it in 'A'..'F' }
                                        .take(6)
                                    newHexInput = "#$filtered"
                                },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = textDark,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                cursorBrush = SolidColor(primaryAccent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 10.dp)
                            )
                        }

                        OutlinedButton(
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
                            border = BorderStroke(1.dp, borderColor),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = textDark),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            AutoSizingText(
                                text = strings.colorPickerAddButton,
                                maxFontSize = 13.sp,
                                minFontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textDark
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, borderColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = textDark),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        AutoSizingText(
                            text = strings.cancel,
                            maxFontSize = 13.sp,
                            minFontSize = 9.5.sp,
                            color = textDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, borderColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = textDark),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        AutoSizingText(
                            text = strings.confirm,
                            maxFontSize = 13.sp,
                            minFontSize = 9.5.sp,
                            color = textDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
