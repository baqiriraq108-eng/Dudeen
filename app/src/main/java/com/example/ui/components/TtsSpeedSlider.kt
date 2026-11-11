package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.LocalAppLanguage
import com.example.ui.theme.GeoBorder
import com.example.ui.theme.GeoOnSurface
import com.example.ui.theme.GeoOnSurfaceVariant
import com.example.ui.theme.GeoPrimary
import com.example.ui.theme.GeoSurfaceVariant
import java.util.Locale
import kotlin.math.max
import kotlin.math.round

val PRESET_SPEECH_RATES = listOf(0.3f, 0.5f, 0.7f, 1.0f, 1.3f, 1.5f, 1.7f, 2.0f)

/**
 * Custom dotted track for Speech Tempo Slider:
 * - Renders evenly spaced circular dots across the interior of the track.
 * - Enforces a generous horizontal edge inset so NO dots appear at the far left or far right terminals.
 * - Dots on the active side (left of thumb) use the primary accent color; dots on the inactive side use a muted tone.
 */
@Composable
private fun DottedSliderTrack(
    fraction: Float,
    activeColor: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp)
    ) {
        val trackWidth = size.width
        val centerY = size.height / 2f
        // Edge inset ensures no dots are drawn at the far left or far right terminals of the track
        val edgeInsetPx = 12.dp.toPx()
        val dotRadiusPx = 2.1.dp.toPx()
        val targetSpacingPx = 8.dp.toPx()

        val usableWidth = (trackWidth - edgeInsetPx * 2f).coerceAtLeast(0f)
        if (usableWidth > 0f) {
            val intervals = max(4, (usableWidth / targetSpacingPx).toInt())
            val actualStep = usableWidth / intervals.toFloat()
            val thumbX = edgeInsetPx + usableWidth * fraction.coerceIn(0f, 1f)

            for (i in 0..intervals) {
                val dotX = edgeInsetPx + i * actualStep
                val isDotActive = dotX <= thumbX + 0.5f
                drawCircle(
                    color = if (isDotActive) activeColor else inactiveColor,
                    radius = dotRadiusPx,
                    center = Offset(dotX, centerY)
                )
            }
        }
    }
}

/**
 * Speech speed control container:
 * High-performance continuous scrubbing (0.3x - 2.0x) with custom inset dotted track,
 * preset quick picker, and +/- buttons.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SprechTempoControl(
    speed: Float,
    onSpeedChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    appLanguage: String = LocalAppLanguage.current
) {
    val strings = remember(appLanguage) { AppStrings.get(appLanguage) }
    var rawSliderValue by remember { mutableFloatStateOf(speed.coerceIn(0.3f, 2.0f)) }
    var isDragging by remember { mutableStateOf(false) }
    var showPresetMenu by remember { mutableStateOf(false) }

    LaunchedEffect(speed) {
        if (!isDragging) {
            rawSliderValue = speed.coerceIn(0.3f, 2.0f)
        }
    }

    val displaySpeed by remember {
        derivedStateOf {
            (round(rawSliderValue * 10f) / 10f).coerceIn(0.3f, 2.0f)
        }
    }

    val activeColor = GeoPrimary
    val inactiveDotColor = GeoOnSurfaceVariant.copy(alpha = 0.35f)

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
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Speedometer Icon
                Icon(
                    painter = painterResource(id = R.drawable.ic_playback_speed),
                    contentDescription = null,
                    tint = GeoPrimary,
                    modifier = Modifier
                        .size(17.dp)
                        .testTag("sprech_tempo_icon")
                )

                // Localized label: Sprech-Tempo / Speech Speed
                Text(
                    text = strings.speechTempoTitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GeoOnSurface,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.testTag("sprech_tempo_title")
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Quick Minus Button (0.1 step decrement down to 0.3)
            IconButton(
                onClick = {
                    val prevSpeed = (round((displaySpeed - 0.1f) * 10f) / 10f).coerceIn(0.3f, 2.0f)
                    rawSliderValue = prevSpeed
                    onSpeedChange(prevSpeed)
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = strings.slowerDesc,
                    tint = GeoPrimary,
                    modifier = Modifier.size(13.dp)
                )
            }

            // Smooth Continuous Slider with Custom Inset Dotted Track
            val sliderFraction = ((rawSliderValue - 0.3f) / (2.0f - 0.3f)).coerceIn(0f, 1f)
            Slider(
                value = rawSliderValue,
                onValueChange = { raw ->
                    isDragging = true
                    rawSliderValue = raw.coerceIn(0.3f, 2.0f)
                },
                onValueChangeFinished = {
                    isDragging = false
                    val snapped = (round(rawSliderValue * 10f) / 10f).coerceIn(0.3f, 2.0f)
                    rawSliderValue = snapped
                    onSpeedChange(snapped)
                },
                valueRange = 0.3f..2.0f,
                colors = SliderDefaults.colors(
                    thumbColor = GeoPrimary,
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent
                ),
                thumb = {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .shadow(2.dp, CircleShape)
                            .border(2.dp, Color.White.copy(alpha = 0.85f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(16.dp)) {
                            drawCircle(color = activeColor)
                        }
                    }
                },
                track = {
                    DottedSliderTrack(
                        fraction = sliderFraction,
                        activeColor = activeColor,
                        inactiveColor = inactiveDotColor
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .testTag("sprech_tempo_slider")
            )

            // Quick Plus Button (0.1 step increment up to 2.0)
            IconButton(
                onClick = {
                    val nextSpeed = (round((displaySpeed + 0.1f) * 10f) / 10f).coerceIn(0.3f, 2.0f)
                    rawSliderValue = nextSpeed
                    onSpeedChange(nextSpeed)
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = strings.fasterDesc,
                    tint = GeoPrimary,
                    modifier = Modifier.size(13.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Speech Speed Value Text - displayed directly beside slider without any container, always visible
            Box {
                Text(
                    text = String.format(Locale.US, "%.1fx", displaySpeed),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = GeoPrimary,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier
                        .testTag("sprech_tempo_value")
                        .clickable { showPresetMenu = true }
                )

                DropdownMenu(
                    expanded = showPresetMenu,
                    onDismissRequest = { showPresetMenu = false }
                ) {
                    PRESET_SPEECH_RATES.forEach { preset ->
                        val isSelected = (round(preset * 10f) == round(displaySpeed * 10f))
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .then(
                                            if (isSelected) {
                                                Modifier
                                                    .border(1.5.dp, GeoPrimary, RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            } else {
                                                Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            }
                                        )
                                ) {
                                    Text(
                                        text = String.format(Locale.US, "%.1fx", preset),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) GeoPrimary else GeoOnSurface
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = GeoPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            onClick = {
                                rawSliderValue = preset
                                onSpeedChange(preset)
                                showPresetMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Material 3 Playback Speed Slider for controlling German TTS speech rate with inset dotted track.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TtsSpeedSlider(
    speed: Float,
    onSpeedChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    sliderWidth: androidx.compose.ui.unit.Dp = 180.dp
) {
    var rawSliderValue by remember { mutableFloatStateOf(speed.coerceIn(0.3f, 2.0f)) }
    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(speed) {
        if (!isDragging) {
            rawSliderValue = speed.coerceIn(0.3f, 2.0f)
        }
    }

    val displaySpeed by remember {
        derivedStateOf {
            (round(rawSliderValue * 10f) / 10f).coerceIn(0.3f, 2.0f)
        }
    }

    val activeColor = GeoPrimary
    val inactiveDotColor = GeoOnSurfaceVariant.copy(alpha = 0.35f)
    val sliderFraction = ((rawSliderValue - 0.3f) / (2.0f - 0.3f)).coerceIn(0f, 1f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .wrapContentHeight()
            .testTag("tts_speed_control_container")
    ) {
        Slider(
            value = rawSliderValue,
            onValueChange = { raw ->
                isDragging = true
                rawSliderValue = raw.coerceIn(0.3f, 2.0f)
            },
            onValueChangeFinished = {
                isDragging = false
                val snapped = (round(rawSliderValue * 10f) / 10f).coerceIn(0.3f, 2.0f)
                rawSliderValue = snapped
                onSpeedChange(snapped)
            },
            valueRange = 0.3f..2.0f,
            colors = SliderDefaults.colors(
                thumbColor = GeoPrimary,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            ),
            thumb = {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .shadow(2.dp, CircleShape)
                        .border(2.dp, Color.White.copy(alpha = 0.85f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(16.dp)) {
                        drawCircle(color = activeColor)
                    }
                }
            },
            track = {
                DottedSliderTrack(
                    fraction = sliderFraction,
                    activeColor = activeColor,
                    inactiveColor = inactiveDotColor
                )
            },
            modifier = Modifier
                .width(sliderWidth)
                .wrapContentHeight()
                .testTag("tts_speed_slider")
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = String.format(Locale.US, "%.1fx", displaySpeed),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = GeoOnSurface,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.testTag("tts_speed_value_label")
        )
    }
}
