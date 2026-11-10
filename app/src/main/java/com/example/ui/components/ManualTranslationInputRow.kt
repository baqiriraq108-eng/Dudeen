package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GeoBorder
import com.example.ui.theme.GeoOnSurface
import com.example.ui.theme.GeoOnSurfaceVariant
import com.example.ui.theme.GeoPrimary
import com.example.util.formatManualTranslations
import com.example.util.isArabicText
import com.example.util.parseManualTranslations

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ManualTranslationInputRow(
    currentManualTranslation: String,
    onSave: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val translations = remember(currentManualTranslation) {
        parseManualTranslations(currentManualTranslation)
    }
    var textValue by remember { mutableStateOf("") }
    var editingIndex by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // List of existing translations as chips with delete (x) buttons
        if (translations.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                translations.forEachIndexed { index, item ->
                    val isBeingEdited = editingIndex == index
                    val isAr = isArabicText(item)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isBeingEdited) GeoPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(
                            1.dp,
                            if (isBeingEdited) GeoPrimary else GeoBorder.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.clip(RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable {
                                    textValue = item
                                    editingIndex = index
                                }
                                .padding(start = 10.dp, end = 4.dp, top = 6.dp, bottom = 6.dp)
                        ) {
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.5.sp,
                                    fontWeight = if (isBeingEdited) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isBeingEdited) GeoPrimary else GeoOnSurface,
                                    lineHeight = 18.sp,
                                    textDirection = TextDirection.Content
                                )
                            )
                            IconButton(
                                onClick = {
                                    if (editingIndex == index) {
                                        editingIndex = null
                                        textValue = ""
                                    }
                                    val updated = translations.filterIndexed { i, _ -> i != index }
                                    onSave(formatManualTranslations(updated))
                                },
                                modifier = Modifier
                                    .size(24.dp)
                                    .testTag("delete_manual_translation_$index")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Übersetzung entfernen",
                                    tint = GeoOnSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Input row to add new translations or edit selected one
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = textValue,
                onValueChange = { textValue = it },
                placeholder = {
                    Text(
                        text = if (editingIndex != null) {
                            "Übersetzung bearbeiten..."
                        } else if (translations.isEmpty()) {
                            "Eigene Übersetzung..."
                        } else {
                            "Weitere Übersetzung hinzufügen..."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = GeoOnSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                singleLine = false,
                minLines = 1,
                maxLines = 4,
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("manual_translation_input"),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.5.sp,
                    color = GeoOnSurface,
                    lineHeight = 22.sp,
                    textDirection = TextDirection.Content
                ),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GeoPrimary,
                    unfocusedBorderColor = GeoBorder,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Button(
                onClick = {
                    val trimmed = textValue.trim()
                    if (trimmed.isNotBlank()) {
                        val newItems = trimmed.split("\n", "\r\n")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }

                        val currentIdx = editingIndex
                        val updated = if (currentIdx != null && currentIdx in translations.indices) {
                            translations.toMutableList().apply {
                                removeAt(currentIdx)
                                add(currentIdx, newItems.first())
                                if (newItems.size > 1) {
                                    addAll(currentIdx + 1, newItems.drop(1))
                                }
                            }
                        } else {
                            (translations + newItems).distinct()
                        }
                        onSave(formatManualTranslations(updated))
                        textValue = ""
                        editingIndex = null
                    } else if (translations.isEmpty()) {
                        onSave("")
                    }
                },
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("save_manual_translation_button"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GeoPrimary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 14.dp)
            ) {
                if (editingIndex != null) {
                    Text(
                        text = "OK",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                } else if (translations.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Hinzufügen",
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "OK",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    Text(
                        text = "OK",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
