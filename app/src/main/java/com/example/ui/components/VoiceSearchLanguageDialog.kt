package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.LocalAppLanguage
import com.example.ui.theme.GeoBorder
import com.example.ui.theme.GeoOnSurface
import com.example.ui.theme.GeoOnSurfaceVariant
import com.example.ui.theme.GeoPrimary
import com.example.ui.theme.GeoPrimaryContainer
import com.example.ui.theme.GeoSurface

/**
 * Quick Language Selector Dialog shown when the user taps the microphone icon in either search bar.
 * Allows choosing between German (de-DE) and English (en-US) before speech recognition starts.
 */
@Composable
fun VoiceSearchLanguageDialog(
    onDismissRequest: () -> Unit,
    onSelectLanguage: (languageCode: String, prompt: String) -> Unit,
    modifier: Modifier = Modifier,
    appLanguage: String = LocalAppLanguage.current
) {
    val strings = remember(appLanguage) { AppStrings.get(appLanguage) }
    val defaultIsEnglish = appLanguage.equals("en", ignoreCase = true)

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = GeoSurface),
            border = BorderStroke(1.dp, GeoBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = modifier
                .fillMaxWidth(0.92f)
                .testTag("voice_search_language_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with mic icon and title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = GeoPrimaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = GeoPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.voiceLanguageDialogTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GeoOnSurface
                        )
                        Text(
                            text = strings.voiceLanguageDialogSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = GeoOnSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }

                // Option 1: Deutsch (German)
                Surface(
                    onClick = {
                        onSelectLanguage("de-DE", strings.voiceSearchPromptGerman)
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = if (!defaultIsEnglish) GeoPrimaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(
                        width = if (!defaultIsEnglish) 1.5.dp else 1.dp,
                        color = if (!defaultIsEnglish) GeoPrimary else GeoBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("voice_lang_de_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = "🇩🇪", fontSize = 20.sp)
                            Column {
                                Text(
                                    text = "Deutsch",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = GeoOnSurface
                                )
                                Text(
                                    text = "de-DE",
                                    fontSize = 11.sp,
                                    color = GeoOnSurfaceVariant
                                )
                            }
                        }

                        if (!defaultIsEnglish) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = GeoPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Option 2: English (Englisch)
                Surface(
                    onClick = {
                        onSelectLanguage("en-US", strings.voiceSearchPromptEnglish)
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = if (defaultIsEnglish) GeoPrimaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(
                        width = if (defaultIsEnglish) 1.5.dp else 1.dp,
                        color = if (defaultIsEnglish) GeoPrimary else GeoBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("voice_lang_en_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = "🇬🇧", fontSize = 20.sp)
                            Column {
                                Text(
                                    text = "English",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = GeoOnSurface
                                )
                                Text(
                                    text = "en-US",
                                    fontSize = 11.sp,
                                    color = GeoOnSurfaceVariant
                                )
                            }
                        }

                        if (defaultIsEnglish) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = GeoPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Cancel button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(
                            text = strings.cancel,
                            fontWeight = FontWeight.SemiBold,
                            color = GeoPrimary
                        )
                    }
                }
            }
        }
    }
}
