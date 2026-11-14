package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.LocalAppLanguage
import com.example.ui.theme.GeoBorder
import com.example.ui.theme.GeoOnSurface
import com.example.ui.theme.GeoOnSurfaceVariant
import com.example.ui.theme.GeoPrimary
import com.example.ui.theme.GeoSecondary
import com.example.ui.theme.GeoSurface
import com.example.ui.theme.GeoSurfaceVariant
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs
import kotlin.math.round

const val MIN_SPEECH_SPEED = 0.25f
const val MAX_SPEECH_SPEED = 2.0f
const val SPEECH_SPEED_STEP = 0.25f

val PRESET_SPEECH_RATES = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)

/**
 * Steps the speech rate by [delta] (default ±0.25x) and snaps to the nearest 0.05x increment
 * within [MIN_SPEECH_SPEED]..[MAX_SPEECH_SPEED].
 */
fun stepSpeechSpeed(current: Float, delta: Float = SPEECH_SPEED_STEP): Float {
    val next = current + delta
    val nearestQuarter = round(current * 4f) / 4f
    val snapped = if (abs(current - nearestQuarter) < 0.03f && abs(abs(delta) - 0.25f) < 0.01f) {
        round((nearestQuarter + delta) * 4f) / 4f
    } else {
        round(next * 20f) / 20f
    }
    return snapped.coerceIn(MIN_SPEECH_SPEED, MAX_SPEECH_SPEED)
}

/**
 * Formats a speech speed value cleanly with fixed two-decimal tabular layout
 * (e.g. "0.25x", "0.50x", "1.00x", "1.25x", "2.00x") so text length stays constant.
 */
fun formatSpeechSpeed(speed: Float): String {
    val rounded20 = round(speed.coerceIn(MIN_SPEECH_SPEED, MAX_SPEECH_SPEED) * 20f).toInt()
    val clean = rounded20 / 20f
    return String.format(Locale.US, "%.2fx", clean)
}

/**
 * Fixed-width tabular speed text that automatically scales its font size if needed
 * (e.g. under large accessibility font scales) so the entire speed string is always
 * rendered inside a completely constant-width container.
 */
@Composable
fun FixedTabularSpeedText(
    speedText: String,
    color: Color,
    modifier: Modifier = Modifier,
    maxFontSize: TextUnit = 11.5.sp,
    minFontSize: TextUnit = 8.sp,
    fontWeight: FontWeight = FontWeight.Bold
) {
    var scaledFontSize by remember(speedText, maxFontSize) { mutableStateOf(maxFontSize) }
    var readyToDraw by remember(speedText, maxFontSize) { mutableStateOf(false) }

    Text(
        text = speedText,
        style = LocalTextStyle.current.copy(
            fontFeatureSettings = "tnum",
            textAlign = TextAlign.Center
        ),
        fontSize = scaledFontSize,
        fontWeight = fontWeight,
        color = color,
        maxLines = 1,
        softWrap = false,
        modifier = modifier.drawWithContent {
            if (readyToDraw) drawContent()
        },
        onTextLayout = { result ->
            if (result.didOverflowWidth && scaledFontSize.value > minFontSize.value) {
                val next = (scaledFontSize.value - 0.5f).coerceAtLeast(minFontSize.value)
                if (next != scaledFontSize.value) {
                    scaledFontSize = next.sp
                } else {
                    readyToDraw = true
                }
            } else {
                readyToDraw = true
            }
        }
    )
}

/**
 * Reusable interactive button that fires [onStep] on single tap AND auto-repeats rapidly
 * when held down (after [initialDelayMillis], every [repeatIntervalMillis]).
 */
@Composable
fun HoldRepeatStepButton(
    onStep: () -> Unit,
    enabled: Boolean = true,
    initialDelayMillis: Long = 220L,
    repeatIntervalMillis: Long = 100L,
    shape: Shape = RoundedCornerShape(10.dp),
    containerColor: Color = GeoSurface,
    activeContainerColor: Color = GeoPrimary.copy(alpha = 0.16f),
    borderColor: Color = GeoPrimary.copy(alpha = 0.35f),
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val currentOnStep by rememberUpdatedState(onStep)
    val currentEnabled by rememberUpdatedState(enabled)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var didHoldRepeat by remember { mutableStateOf(false) }

    LaunchedEffect(interactionSource) {
        var repeatJob: Job? = null
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    repeatJob?.cancel()
                    didHoldRepeat = false
                    if (currentEnabled) {
                        repeatJob = launch {
                            delay(initialDelayMillis)
                            while (currentEnabled) {
                                didHoldRepeat = true
                                currentOnStep()
                                delay(repeatIntervalMillis)
                            }
                        }
                    }
                }
                is PressInteraction.Release, is PressInteraction.Cancel -> {
                    repeatJob?.cancel()
                    repeatJob = null
                }
            }
        }
    }

    val effectiveBg = when {
        !enabled -> containerColor.copy(alpha = 0.45f)
        isPressed -> activeContainerColor
        else -> containerColor
    }
    val effectiveBorder = when {
        !enabled -> borderColor.copy(alpha = 0.18f)
        isPressed -> GeoPrimary
        else -> borderColor
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(shape)
            .background(effectiveBg)
            .border(1.dp, effectiveBorder, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                enabled = enabled,
                onClick = {
                    if (didHoldRepeat) {
                        didHoldRepeat = false
                    } else {
                        currentOnStep()
                    }
                }
            )
    ) {
        content()
    }
}

/**
 * Unified Speech Speed Selection Popup Dialog (used in both the main screen Sprech-Tempo bar
 * and the Grammar Tables header). Uses the standardized slightly-rounded 14.dp popup border.
 */
@Composable
fun SpeechSpeedSelectionDialog(
    initialSpeed: Float,
    onConfirmSpeed: (Float) -> Unit,
    onDismiss: () -> Unit,
    appLanguage: String = LocalAppLanguage.current
) {
    var tempSpeed by remember(initialSpeed) {
        mutableFloatStateOf(initialSpeed.coerceIn(MIN_SPEECH_SPEED, MAX_SPEECH_SPEED))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            border = BorderStroke(1.dp, GeoBorder),
            modifier = Modifier
                .widthIn(max = 340.dp)
                .fillMaxWidth(0.9f)
                .padding(16.dp)
                .testTag("speech_speed_selection_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = when (appLanguage.lowercase()) {
                        "en" -> "Select speech rate"
                        "ar" -> "اختيار سرعة النطق"
                        else -> "Sprechtempo wählen"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Preset buttons grid: 0.25x..2.00x in rows of 4
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presetRows = PRESET_SPEECH_RATES.chunked(4)

                    presetRows.forEach { rowPresets ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowPresets.forEach { preset ->
                                val isSelected = (round(tempSpeed * 20f) == round(preset * 20f))
                                Surface(
                                    onClick = { tempSpeed = preset },
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    border = BorderStroke(
                                        if (isSelected) 1.5.dp else 1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else GeoBorder
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 4.dp)
                                    ) {
                                        FixedTabularSpeedText(
                                            speedText = formatSpeechSpeed(preset),
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                            maxFontSize = 12.sp,
                                            minFontSize = 9.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                            val remaining = 4 - rowPresets.size
                            if (remaining > 0) {
                                repeat(remaining) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(
                    thickness = 0.8.dp,
                    color = GeoBorder.copy(alpha = 0.6f)
                )

                // Fine-tuning section: Eigene Geschwindigkeit (±0.25x steps + hold-to-repeat)
                val canDecreaseCustom = tempSpeed > MIN_SPEECH_SPEED + 0.01f
                val canIncreaseCustom = tempSpeed < MAX_SPEECH_SPEED - 0.01f
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = when (appLanguage.lowercase()) {
                            "en" -> "Custom speed (±0.25x):"
                            "ar" -> "سرعة مخصصة (±0.25x):"
                            else -> "Eigene Geschwindigkeit (±0,25x):"
                        },
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HoldRepeatStepButton(
                            onStep = {
                                tempSpeed = stepSpeechSpeed(tempSpeed, -SPEECH_SPEED_STEP)
                            },
                            enabled = canDecreaseCustom,
                            shape = RoundedCornerShape(10.dp),
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            activeContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                            borderColor = GeoBorder,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("grammar_speed_minus")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Langsamer",
                                tint = if (canDecreaseCustom) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)),
                            modifier = Modifier
                                .width(96.dp)
                                .height(40.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 6.dp)
                            ) {
                                FixedTabularSpeedText(
                                    speedText = formatSpeechSpeed(tempSpeed),
                                    color = MaterialTheme.colorScheme.primary,
                                    maxFontSize = 14.5.sp,
                                    minFontSize = 10.sp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        HoldRepeatStepButton(
                            onStep = {
                                tempSpeed = stepSpeechSpeed(tempSpeed, +SPEECH_SPEED_STEP)
                            },
                            enabled = canIncreaseCustom,
                            shape = RoundedCornerShape(10.dp),
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            activeContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                            borderColor = GeoBorder,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("grammar_speed_plus")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Schneller",
                                tint = if (canIncreaseCustom) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Action buttons: [ Abbrechen / Cancel ] [ Bestätigen / Confirm ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, GeoBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        AutoSizingText(
                            text = when (appLanguage.lowercase()) {
                                "en" -> "Cancel"
                                "ar" -> "إلغاء"
                                else -> "Abbrechen"
                            },
                            maxFontSize = 13.sp,
                            minFontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            onConfirmSpeed(tempSpeed)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, GeoBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        AutoSizingText(
                            text = when (appLanguage.lowercase()) {
                                "en" -> "Confirm"
                                "ar" -> "تأكيد"
                                else -> "Bestätigen"
                            },
                            maxFontSize = 13.sp,
                            minFontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

/**
 * Smooth, jitter-free linear slider built directly on BoxWithConstraints + absolute pointer X mapping.
 * Avoids Material 3 Slider's track-to-thumb coordinate drift and ensures zero oscillation during drag.
 */
@Composable
fun SmoothLinearSpeedSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val latestOnValueChange by rememberUpdatedState(onValueChange)
    val latestOnValueChangeFinished by rememberUpdatedState(onValueChangeFinished)
    var isDragging by remember { mutableStateOf(false) }

    val targetFraction = ((value - MIN_SPEECH_SPEED) / (MAX_SPEECH_SPEED - MIN_SPEECH_SPEED)).coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(durationMillis = if (isDragging) 0 else 95),
        label = "smooth_speed_fraction"
    )
    val visualFraction = if (isDragging) targetFraction else animatedFraction

    val thumbRadiusDp by animateDpAsState(
        targetValue = if (isDragging) 9.dp else 7.5.dp,
        animationSpec = tween(durationMillis = 95),
        label = "smooth_speed_thumb_radius"
    )

    val activeStartColor = GeoPrimary
    val activeEndColor = GeoSecondary
    val inactiveTrackColor = GeoOnSurfaceVariant.copy(alpha = 0.22f)
    val haloColor = GeoPrimary.copy(alpha = if (isDragging) 0.24f else 0.14f)

    BoxWithConstraints(
        modifier = modifier.height(34.dp),
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val thumbPadPx = with(density) { 10.dp.toPx() }
        val usableWidth = (widthPx - thumbPadPx * 2f).coerceAtLeast(1f)

        fun mapPointerXToSpeed(pointerX: Float): Float {
            val f = ((pointerX - thumbPadPx) / usableWidth).coerceIn(0f, 1f)
            return MIN_SPEECH_SPEED + f * (MAX_SPEECH_SPEED - MIN_SPEECH_SPEED)
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(widthPx) {
                    detectHorizontalDragGestures(
                        onDragStart = { startOffset ->
                            isDragging = true
                            latestOnValueChange(mapPointerXToSpeed(startOffset.x))
                        },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            latestOnValueChange(mapPointerXToSpeed(change.position.x))
                        },
                        onDragEnd = {
                            isDragging = false
                            latestOnValueChangeFinished()
                        },
                        onDragCancel = {
                            isDragging = false
                            latestOnValueChangeFinished()
                        }
                    )
                }
                .pointerInput(widthPx) {
                    detectTapGestures(
                        onTap = { tapOffset ->
                            latestOnValueChange(mapPointerXToSpeed(tapOffset.x))
                            latestOnValueChangeFinished()
                        }
                    )
                }
        ) {
            val padPx = 10.dp.toPx()
            val trackWidth = (size.width - padPx * 2f).coerceAtLeast(1f)
            val centerY = size.height / 2f
            val trackHeight = 6.dp.toPx()
            val trackRadius = trackHeight / 2f
            val thumbCenterX = padPx + trackWidth * visualFraction.coerceIn(0f, 1f)
            val thumbRadiusPx = thumbRadiusDp.toPx()

            // Inactive track capsule
            drawRoundRect(
                color = inactiveTrackColor,
                topLeft = Offset(padPx, centerY - trackRadius),
                size = Size(trackWidth, trackHeight),
                cornerRadius = CornerRadius(trackRadius, trackRadius)
            )

            // Active smooth gradient capsule
            val activeWidth = (thumbCenterX - padPx).coerceAtLeast(0f)
            if (activeWidth > 0.5f) {
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(activeStartColor, activeEndColor),
                        startX = padPx,
                        endX = padPx + trackWidth
                    ),
                    topLeft = Offset(padPx, centerY - trackRadius),
                    size = Size(activeWidth, trackHeight),
                    cornerRadius = CornerRadius(trackRadius, trackRadius)
                )
            }

            // Soft thumb halo
            drawCircle(
                color = haloColor,
                radius = thumbRadiusPx + 4.dp.toPx(),
                center = Offset(thumbCenterX, centerY)
            )

            // Crisp white outer ring
            drawCircle(
                color = Color.White.copy(alpha = 0.94f),
                radius = thumbRadiusPx + 1.6.dp.toPx(),
                center = Offset(thumbCenterX, centerY)
            )

            // Solid accent thumb core
            drawCircle(
                color = activeStartColor,
                radius = thumbRadiusPx,
                center = Offset(thumbCenterX, centerY)
            )
        }
    }
}

/**
 * Speech speed control container:
 * - No +/- buttons on the bar so the smooth slider stretches across the full middle width.
 * - Clean vertical divider line separating "Sprech-Tempo" from the slider track.
 * - Fixed-width, slightly rounded (10.dp), arrow-free speed value box ("1.00x") that opens
 *   the unified SpeechSpeedSelectionDialog on tap.
 */
@Composable
fun SprechTempoControl(
    speed: Float,
    onSpeedChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    appLanguage: String = LocalAppLanguage.current
) {
    val strings = remember(appLanguage) { AppStrings.get(appLanguage) }
    var rawSliderValue by remember { mutableFloatStateOf(speed.coerceIn(MIN_SPEECH_SPEED, MAX_SPEECH_SPEED)) }
    var isDragging by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }

    LaunchedEffect(speed) {
        if (!isDragging) {
            rawSliderValue = speed.coerceIn(MIN_SPEECH_SPEED, MAX_SPEECH_SPEED)
        }
    }

    val displaySpeed by remember {
        derivedStateOf {
            (round(rawSliderValue * 20f) / 20f).coerceIn(MIN_SPEECH_SPEED, MAX_SPEECH_SPEED)
        }
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, GeoBorder),
        colors = CardDefaults.cardColors(
            containerColor = GeoSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("sprech_tempo_container")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_playback_speed),
                    contentDescription = null,
                    tint = GeoPrimary,
                    modifier = Modifier
                        .size(16.dp)
                        .testTag("sprech_tempo_icon")
                )

                Text(
                    text = strings.speechTempoTitle,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GeoOnSurface,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.testTag("sprech_tempo_title")
                )
            }

            // Styled Vertical Divider separating "Sprech-Tempo" from the slider track
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                GeoBorder.copy(alpha = 0.2f),
                                GeoPrimary.copy(alpha = 0.45f),
                                GeoBorder.copy(alpha = 0.2f)
                            )
                        )
                    )
                    .testTag("sprech_tempo_divider")
            )

            // Full-Length Smooth, Jitter-Free Linear Slider
            SmoothLinearSpeedSlider(
                value = rawSliderValue,
                onValueChange = { raw ->
                    isDragging = true
                    rawSliderValue = raw.coerceIn(MIN_SPEECH_SPEED, MAX_SPEECH_SPEED)
                },
                onValueChangeFinished = {
                    isDragging = false
                    val snapped = (round(rawSliderValue * 20f) / 20f).coerceIn(MIN_SPEECH_SPEED, MAX_SPEECH_SPEED)
                    rawSliderValue = snapped
                    onSpeedChange(snapped)
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("sprech_tempo_slider")
            )

            // Fixed-Width Speech Speed Value Box (slightly rounded 10.dp, zero arrows, opens unified dialog)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(56.dp)
                    .height(28.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GeoPrimary.copy(alpha = 0.12f))
                    .border(1.dp, GeoPrimary.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                    .clickable { showSpeedDialog = true }
                    .padding(horizontal = 4.dp)
                    .testTag("sprech_tempo_value")
            ) {
                FixedTabularSpeedText(
                    speedText = formatSpeechSpeed(displaySpeed),
                    color = GeoPrimary,
                    maxFontSize = 11.5.sp,
                    minFontSize = 8.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showSpeedDialog) {
        SpeechSpeedSelectionDialog(
            initialSpeed = displaySpeed,
            onConfirmSpeed = { newSpeed ->
                rawSliderValue = newSpeed
                onSpeedChange(newSpeed)
            },
            onDismiss = { showSpeedDialog = false },
            appLanguage = appLanguage
        )
    }
}

/**
 * Compact smooth Playback Speed Slider with full-length track and a fixed-width
 * slightly rounded (10.dp) tabular speed label (without +/- buttons or arrows).
 */
@Composable
fun TtsSpeedSlider(
    speed: Float,
    onSpeedChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    sliderWidth: Dp = 180.dp
) {
    val appLanguage = LocalAppLanguage.current
    var rawSliderValue by remember { mutableFloatStateOf(speed.coerceIn(MIN_SPEECH_SPEED, MAX_SPEECH_SPEED)) }
    var isDragging by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }

    LaunchedEffect(speed) {
        if (!isDragging) {
            rawSliderValue = speed.coerceIn(MIN_SPEECH_SPEED, MAX_SPEECH_SPEED)
        }
    }

    val displaySpeed by remember {
        derivedStateOf {
            (round(rawSliderValue * 20f) / 20f).coerceIn(MIN_SPEECH_SPEED, MAX_SPEECH_SPEED)
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .wrapContentHeight()
            .testTag("tts_speed_control_container")
    ) {
        SmoothLinearSpeedSlider(
            value = rawSliderValue,
            onValueChange = { raw ->
                isDragging = true
                rawSliderValue = raw.coerceIn(MIN_SPEECH_SPEED, MAX_SPEECH_SPEED)
            },
            onValueChangeFinished = {
                isDragging = false
                val snapped = (round(rawSliderValue * 20f) / 20f).coerceIn(MIN_SPEECH_SPEED, MAX_SPEECH_SPEED)
                rawSliderValue = snapped
                onSpeedChange(snapped)
            },
            modifier = Modifier
                .width(sliderWidth)
                .testTag("tts_speed_slider")
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(54.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(GeoPrimary.copy(alpha = 0.12f))
                .border(1.dp, GeoPrimary.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                .clickable { showSpeedDialog = true }
                .padding(horizontal = 4.dp)
                .testTag("tts_speed_value_label")
        ) {
            FixedTabularSpeedText(
                speedText = formatSpeechSpeed(displaySpeed),
                color = GeoPrimary,
                maxFontSize = 11.sp,
                minFontSize = 8.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showSpeedDialog) {
        SpeechSpeedSelectionDialog(
            initialSpeed = displaySpeed,
            onConfirmSpeed = { newSpeed ->
                rawSliderValue = newSpeed
                onSpeedChange(newSpeed)
            },
            onDismiss = { showSpeedDialog = false },
            appLanguage = appLanguage
        )
    }
}
