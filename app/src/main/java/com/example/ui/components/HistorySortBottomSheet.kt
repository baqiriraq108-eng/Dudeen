package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.LocalAppLanguage
import com.example.ui.theme.GeoBackground
import com.example.ui.theme.GeoBorder
import com.example.ui.theme.GeoGlowColor
import com.example.ui.theme.GeoIsBoldTheme
import com.example.ui.theme.GeoOnPrimaryContainer
import com.example.ui.theme.GeoOnSurface
import com.example.ui.theme.GeoOnSurfaceVariant
import com.example.ui.theme.GeoPrimary
import com.example.ui.theme.GeoPrimaryContainer
import com.example.ui.theme.GeoSurface
import com.example.ui.theme.GeoSurfaceVariant
import com.example.util.DateSortMode
import com.example.util.GenderSortTarget
import com.example.util.HistorySortField
import com.example.util.HistorySortOption
import com.example.util.SortDirection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HistorySortBottomSheet(
    currentSortOption: HistorySortOption,
    selectedCategoryIndex: Int,
    onSortOptionChanged: (HistorySortOption) -> Unit,
    onDismissRequest: () -> Unit,
    appLanguage: String = LocalAppLanguage.current
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val strings = remember(appLanguage) { AppStrings.get(appLanguage) }
    val categoryNames = remember(strings) { strings.historyCategories() }
    val currentCategoryName = categoryNames.getOrElse(selectedCategoryIndex) { strings.historyTitle }

    // DatePicker dialog state
    var showDatePickerDialog by remember { mutableStateOf(false) }

    // Genus dropdown state
    var isGenusDropdownExpanded by remember { mutableStateOf(false) }

    // Date formatting helper
    val dateFormatter = remember { SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN) }

    // Tailored criteria per entry type
    // Nomen (1) and Alle (0): Datum, Name, Genus, Typ
    // Verben (2), Sätze (3), Wörter (4), Adjektive (5): Datum, Name, Typ (NO Genus!)
    val availableCriteria: List<HistorySortField> = when (selectedCategoryIndex) {
        2, 3, 4, 5 -> listOf(HistorySortField.DATE, HistorySortField.NAME, HistorySortField.TYPE)
        else -> listOf(HistorySortField.DATE, HistorySortField.NAME, HistorySortField.GENDER, HistorySortField.TYPE)
    }

    // If current field is not available for this category (e.g. Genus in Verben), fallback to DATE
    val effectiveField = if (currentSortOption.field in availableCriteria) {
        currentSortOption.field
    } else {
        HistorySortField.DATE
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = GeoSurface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = null,
        modifier = Modifier.testTag("history_sort_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header Row: Compact Title + Current Category Tag + Close
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(GeoPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = null,
                            tint = GeoPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = strings.sortSheetTitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = GeoOnSurface,
                        maxLines = 1,
                        softWrap = false
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GeoSurfaceVariant)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = currentCategoryName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GeoPrimary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("sort_sheet_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = strings.close,
                        tint = GeoOnSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Direction Section (Richtung)
            Text(
                text = strings.sortDirectionLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = GeoOnSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp),
                maxLines = 1,
                softWrap = false
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SortDirectionChip(
                    label = SortDirection.DESCENDING.label(appLanguage),
                    icon = Icons.Default.ArrowDownward,
                    isSelected = currentSortOption.direction == SortDirection.DESCENDING,
                    onClick = {
                        onSortOptionChanged(currentSortOption.copy(direction = SortDirection.DESCENDING))
                    },
                    modifier = Modifier.weight(1f),
                    testTag = "sort_direction_descending"
                )

                SortDirectionChip(
                    label = SortDirection.ASCENDING.label(appLanguage),
                    icon = Icons.Default.ArrowUpward,
                    isSelected = currentSortOption.direction == SortDirection.ASCENDING,
                    onClick = {
                        onSortOptionChanged(currentSortOption.copy(direction = SortDirection.ASCENDING))
                    },
                    modifier = Modifier.weight(1f),
                    testTag = "sort_direction_ascending"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Criteria Section (Kriterium)
            Text(
                text = strings.sortCriterionLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = GeoOnSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp),
                maxLines = 1,
                softWrap = false
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                availableCriteria.forEach { field ->
                    val isSelected = effectiveField == field
                    val icon = when (field) {
                        HistorySortField.DATE -> Icons.Default.CalendarToday
                        HistorySortField.NAME -> Icons.Default.SortByAlpha
                        HistorySortField.GENDER -> Icons.Default.Wc
                        HistorySortField.TYPE -> Icons.Default.Category
                    }

                    SortCriterionChip(
                        label = field.label(appLanguage),
                        icon = icon,
                        isSelected = isSelected,
                        onClick = {
                            if (isSelected) {
                                // Tapping the active criterion toggles between ascending & descending
                                val newDirection = if (currentSortOption.direction == SortDirection.DESCENDING) {
                                    SortDirection.ASCENDING
                                } else {
                                    SortDirection.DESCENDING
                                }
                                onSortOptionChanged(currentSortOption.copy(direction = newDirection))
                            } else {
                                onSortOptionChanged(currentSortOption.copy(field = field))
                            }
                        },
                        testTag = "sort_field_${field.name.lowercase(Locale.ROOT)}"
                    )
                }
            }

            // Sub-Options for Datum (Automatic vs Manual Date)
            AnimatedVisibility(
                visible = effectiveField == HistorySortField.DATE,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Text(
                        text = strings.sortDateModeLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GeoOnSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp),
                        maxLines = 1,
                        softWrap = false
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Automatic Mode Chip
                        SubOptionChip(
                            label = strings.sortDateAutomatic,
                            isSelected = currentSortOption.dateSortMode == DateSortMode.AUTOMATIC,
                            onClick = {
                                onSortOptionChanged(
                                    currentSortOption.copy(
                                        field = HistorySortField.DATE,
                                        dateSortMode = DateSortMode.AUTOMATIC
                                    )
                                )
                            },
                            testTag = "date_mode_automatic"
                        )

                        // Manual Date Mode Chip
                        SubOptionChip(
                            label = strings.sortDateManual,
                            isSelected = currentSortOption.dateSortMode == DateSortMode.MANUAL,
                            onClick = {
                                val targetTime = currentSortOption.manualDateMillis ?: System.currentTimeMillis()
                                onSortOptionChanged(
                                    currentSortOption.copy(
                                        field = HistorySortField.DATE,
                                        dateSortMode = DateSortMode.MANUAL,
                                        manualDateMillis = targetTime
                                    )
                                )
                            },
                            testTag = "date_mode_manual"
                        )

                        // If Manual Date Mode is active, show the picked date button
                        if (currentSortOption.dateSortMode == DateSortMode.MANUAL) {
                            val pickedMillis = currentSortOption.manualDateMillis ?: System.currentTimeMillis()
                            val formattedDate = dateFormatter.format(Date(pickedMillis))

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GeoPrimaryContainer)
                                    .border(1.dp, GeoPrimary, RoundedCornerShape(8.dp))
                                    .clickable { showDatePickerDialog = true }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                    .testTag("date_picker_trigger"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EditCalendar,
                                    contentDescription = null,
                                    tint = GeoPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = formattedDate,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GeoPrimary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }

            // Sub-Options / Dropdown for Genus (when field is GENDER and category has genus)
            AnimatedVisibility(
                visible = effectiveField == HistorySortField.GENDER && (selectedCategoryIndex == 0 || selectedCategoryIndex == 1),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Text(
                        text = strings.sortGenderSelectionLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GeoOnSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp),
                        maxLines = 1,
                        softWrap = false
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Dropdown Selector Trigger Button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GeoSurfaceVariant)
                                .border(1.dp, GeoBorder, RoundedCornerShape(8.dp))
                                .clickable { isGenusDropdownExpanded = true }
                                .padding(horizontal = 10.dp)
                                .testTag("genus_dropdown_button"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Wc,
                                    contentDescription = null,
                                    tint = GeoPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = currentSortOption.genderTarget.label(appLanguage),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GeoOnSurface,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        // Dropdown Menu for Genus options
                        DropdownMenu(
                            expanded = isGenusDropdownExpanded,
                            onDismissRequest = { isGenusDropdownExpanded = false },
                            shape = RoundedCornerShape(14.dp),
                            containerColor = GeoSurface,
                            border = BorderStroke(1.dp, GeoBorder)
                        ) {
                            GenderSortTarget.entries.forEach { target ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = target.label(appLanguage),
                                                fontSize = 13.sp,
                                                fontWeight = if (currentSortOption.genderTarget == target) FontWeight.Bold else FontWeight.Normal,
                                                color = if (currentSortOption.genderTarget == target) GeoPrimary else GeoOnSurface
                                            )
                                            if (currentSortOption.genderTarget == target) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = GeoPrimary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        onSortOptionChanged(
                                            currentSortOption.copy(
                                                field = HistorySortField.GENDER,
                                                genderTarget = target
                                            )
                                        )
                                        isGenusDropdownExpanded = false
                                    },
                                    modifier = Modifier.testTag("genus_option_${target.name.lowercase(Locale.ROOT)}")
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Cancel & Confirm
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        onSortOptionChanged(HistorySortOption())
                        onDismissRequest()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("sort_reset_button"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, GeoBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GeoOnSurface),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    AutoSizingText(
                        text = strings.cancel,
                        maxFontSize = 13.sp,
                        minFontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GeoOnSurface
                    )
                }

                OutlinedButton(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("sort_apply_button"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, GeoBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GeoOnSurface),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    AutoSizingText(
                        text = strings.confirm,
                        maxFontSize = 13.sp,
                        minFontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GeoOnSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
        }
    }

    // Material 3 DatePicker Dialog for Manual Date selection
    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = currentSortOption.manualDateMillis ?: System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showDatePickerDialog = false },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, GeoBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GeoOnSurface),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        AutoSizingText(
                            text = strings.cancel,
                            maxFontSize = 13.sp,
                            minFontSize = 9.5.sp,
                            color = GeoOnSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            val selectedMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                            onSortOptionChanged(
                                currentSortOption.copy(
                                    field = HistorySortField.DATE,
                                    dateSortMode = DateSortMode.MANUAL,
                                    manualDateMillis = selectedMillis
                                )
                            )
                            showDatePickerDialog = false
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("date_picker_confirm"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, GeoBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GeoOnSurface),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        AutoSizingText(
                            text = strings.confirm,
                            maxFontSize = 13.sp,
                            minFontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GeoOnSurface
                        )
                    }
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun SortDirectionChip(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val textColor = if (isSelected) GeoPrimary else GeoOnSurfaceVariant
    val iconColor = if (isSelected) GeoPrimary else GeoOnSurfaceVariant

    Row(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(GeoSurfaceVariant)
            .border(
                if (isSelected) 1.5.dp else 1.dp,
                if (isSelected) GeoPrimary else if (GeoIsBoldTheme) GeoGlowColor.copy(alpha = 0.5f) else GeoBorder,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun SortCriterionChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    testTag: String = ""
) {
    val textColor = if (isSelected) GeoPrimary else GeoOnSurfaceVariant
    val iconColor = if (isSelected) GeoPrimary else GeoOnSurfaceVariant

    Row(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(GeoSurfaceVariant)
            .border(
                if (isSelected) 1.5.dp else 1.dp,
                if (isSelected) GeoPrimary else if (GeoIsBoldTheme) GeoGlowColor.copy(alpha = 0.5f) else GeoBorder,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun SubOptionChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String = ""
) {
    val textColor = if (isSelected) GeoPrimary else GeoOnSurfaceVariant

    Box(
        modifier = Modifier
            .height(28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(GeoSurfaceVariant)
            .border(
                if (isSelected) 1.5.dp else 1.dp,
                if (isSelected) GeoPrimary else GeoBorder,
                RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor,
            maxLines = 1,
            softWrap = false
        )
    }
}
