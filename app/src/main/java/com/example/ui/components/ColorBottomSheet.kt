package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.LocalAppLanguage
import com.example.ui.theme.AppThemePackage
import com.example.ui.theme.ColorGroupCategory

/**
 * Separate Color Scheme Window (Bottom Sheet).
 * Displays:
 * 1. Mode Selection: [ System ] [ Light ] [ Dark ]
 * 2. 10 Categories of the 100 Colors (plus preserved classic themes) with expandable headers
 * 3. Action buttons: Cancel and Apply
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorBottomSheet(
    currentTheme: AppThemePackage,
    currentThemeMode: String,
    onDismiss: () -> Unit,
    onApplyTheme: (AppThemePackage, String) -> Unit,
    modifier: Modifier = Modifier,
    appLanguage: String = LocalAppLanguage.current
) {
    val sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val strings = remember(appLanguage) { AppStrings.get(appLanguage) }

    // Local preview state that changes when user taps before applying
    var previewTheme by remember { mutableStateOf(currentTheme) }
    var previewMode by remember { mutableStateOf(currentThemeMode) }

    // Map of expanded state for categories. Expand the category of the selected theme by default.
    val expandedCategories = remember {
        mutableStateMapOf<String, Boolean>().apply {
            AppThemePackage.ALL_CATEGORIES.forEach { category ->
                val containsCurrent = category.themes.any { it.id == currentTheme.id || it == currentTheme }
                this[category.id] = containsCurrent
            }
            // If none matched, expand the first category
            if (values.none { it }) {
                this["calm_soft"] = true
            }
        }
    }

    val isSystemDark = isSystemInDarkTheme()
    val isPreviewDark = when (previewMode.lowercase()) {
        "light", "hell" -> false
        "dark", "dunkel" -> true
        else -> isSystemDark
    }

    val windowBg = previewTheme.tier4SurfaceLow(isPreviewDark)
    val textPrimary = if (isPreviewDark) Color(0xFFFFFFFF) else Color(0xFF1A1A1A)
    val textSecondary = if (isPreviewDark) Color(0xFFB4B8C0) else Color(0xFF5A5D64)
    val dividerColor = previewTheme.tier4Border(isPreviewDark)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = windowBg,
        tonalElevation = 6.dp,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = null,
        modifier = modifier.testTag("color_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // ==============================================================
            // Top Bar: Title & Close Button
            // ==============================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(previewTheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = previewTheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = strings.colorSheetTitle,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = strings.colorSheetSubtitle,
                            fontSize = 11.sp,
                            color = textSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("close_color_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = strings.close,
                        tint = textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            HorizontalDivider(color = dividerColor, thickness = 1.dp)

            // Scrollable Content: Mode Selection, Categories, Live Preview
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .heightIn(max = 520.dp)
                    .verticalScroll(scrollState)
                    .padding(vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ==============================================================
                // 1. Mode Selection: System / Light / Dark
                // ==============================================================
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = strings.colorModeLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // System
                        val isSystemSelected = previewMode.lowercase() in listOf("auto", "system")
                        FilterChip(
                            selected = isSystemSelected,
                            onClick = { previewMode = "auto" },
                            label = { Text(strings.themeModeAuto, fontSize = 12.sp, fontWeight = if (isSystemSelected) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.SettingsBrightness,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.Transparent,
                                selectedContainerColor = Color.Transparent,
                                labelColor = textPrimary,
                                iconColor = textSecondary,
                                selectedLabelColor = previewTheme.primary,
                                selectedLeadingIconColor = previewTheme.primary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSystemSelected,
                                borderColor = dividerColor,
                                selectedBorderColor = previewTheme.primary,
                                borderWidth = 1.dp,
                                selectedBorderWidth = 1.5.dp
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        // Light
                        val isLightSelected = previewMode.lowercase() in listOf("light", "hell")
                        FilterChip(
                            selected = isLightSelected,
                            onClick = { previewMode = "light" },
                            label = { Text(strings.themeModeLight, fontSize = 12.sp, fontWeight = if (isLightSelected) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.LightMode,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.Transparent,
                                selectedContainerColor = Color.Transparent,
                                labelColor = textPrimary,
                                iconColor = textSecondary,
                                selectedLabelColor = previewTheme.primary,
                                selectedLeadingIconColor = previewTheme.primary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isLightSelected,
                                borderColor = dividerColor,
                                selectedBorderColor = previewTheme.primary,
                                borderWidth = 1.dp,
                                selectedBorderWidth = 1.5.dp
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        // Dark
                        val isDarkSelected = previewMode.lowercase() in listOf("dark", "dunkel")
                        FilterChip(
                            selected = isDarkSelected,
                            onClick = { previewMode = "dark" },
                            label = { Text(strings.themeModeDark, fontSize = 12.sp, fontWeight = if (isDarkSelected) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DarkMode,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.Transparent,
                                selectedContainerColor = Color.Transparent,
                                labelColor = textPrimary,
                                iconColor = textSecondary,
                                selectedLabelColor = previewTheme.primary,
                                selectedLeadingIconColor = previewTheme.primary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isDarkSelected,
                                borderColor = dividerColor,
                                selectedBorderColor = previewTheme.primary,
                                borderWidth = 1.dp,
                                selectedBorderWidth = 1.5.dp
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                HorizontalDivider(color = dividerColor.copy(alpha = 0.6f), thickness = 1.dp)

                // ==============================================================
                // 2. Ten Categories of the 100 Colors (Expandable)
                // ==============================================================
                val allExpanded = AppThemePackage.ALL_CATEGORIES.all { expandedCategories[it.id] == true }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = strings.chooseColorGroupsLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Text(
                        text = if (allExpanded) strings.collapseDesc else strings.expandDesc,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = previewTheme.tier1Primary(isPreviewDark),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                val nextState = !allExpanded
                                AppThemePackage.ALL_CATEGORIES.forEach { cat ->
                                    expandedCategories[cat.id] = nextState
                                }
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .testTag("toggle_all_color_categories")
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppThemePackage.ALL_CATEGORIES.forEach { category ->
                        CategoryExpandableSection(
                            category = category,
                            categoryTitle = strings.colorCategoryName(category.id, category.name),
                            isExpanded = expandedCategories[category.id] == true,
                            onToggle = {
                                expandedCategories[category.id] = !(expandedCategories[category.id] ?: false)
                            },
                            selectedTheme = previewTheme,
                            onSelectTheme = { selected ->
                                previewTheme = selected
                            },
                            isPreviewDark = isPreviewDark,
                            collapseDesc = strings.collapseDesc,
                            expandDesc = strings.expandDesc
                        )
                    }
                }
            }

            HorizontalDivider(color = dividerColor, thickness = 1.dp)

            // ==============================================================
            // 5. Action Buttons: Cancel and Apply
            // ==============================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, dividerColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = textPrimary),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("cancel_color_selection")
                ) {
                    AutoSizingText(
                        text = strings.cancel,
                        maxFontSize = 13.sp,
                        minFontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )
                }

                OutlinedButton(
                    onClick = {
                        onApplyTheme(previewTheme, previewMode)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, dividerColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = textPrimary),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("apply_color_selection")
                ) {
                    AutoSizingText(
                        text = strings.confirm,
                        maxFontSize = 13.sp,
                        minFontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )
                }
            }
        }
    }
}

/**
 * Expandable Category Header and 10-Color Item Grid
 */
@Composable
private fun CategoryExpandableSection(
    category: ColorGroupCategory,
    categoryTitle: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    selectedTheme: AppThemePackage,
    onSelectTheme: (AppThemePackage) -> Unit,
    isPreviewDark: Boolean,
    collapseDesc: String,
    expandDesc: String
) {
    val rotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "arrow_rot")
    val textPrimary = if (isPreviewDark) Color(0xFFFFFFFF) else Color(0xFF1A1A1A)
    val cardBg = selectedTheme.tier4SurfaceContainer(isPreviewDark)
    val containsSelected = category.themes.any { it.id == selectedTheme.id || it == selectedTheme }
    val activeAccent = selectedTheme.tier1Primary(isPreviewDark)
    val borderColor = if (containsSelected) activeAccent.copy(alpha = 0.55f) else selectedTheme.tier4Border(isPreviewDark)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(cardBg)
            .border(if (containsSelected) 1.2.dp else 1.dp, borderColor, RoundedCornerShape(10.dp))
    ) {
        // Expandable Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = category.icon,
                    fontSize = 14.sp
                )
                Text(
                    text = "$categoryTitle (${category.themes.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (containsSelected) activeAccent else textPrimary
                )
                if (containsSelected) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(activeAccent)
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) collapseDesc else expandDesc,
                tint = textPrimary,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(rotation)
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Split 10 colors into rows of 2 for comfortable touch targets and previewing Primary, Dark, Light
                category.themes.chunked(2).forEach { rowThemes ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowThemes.forEach { theme ->
                            val isSelected = theme.id == selectedTheme.id || theme == selectedTheme
                            ColorCardItem(
                                theme = theme,
                                isSelected = isSelected,
                                onSelect = { onSelectTheme(theme) },
                                isPreviewDark = isPreviewDark,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowThemes.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual Color Card Item displaying Name and Four Tonal Layer Swatches:
 * Tier 1 (Primary), Tier 2 (Secondary), Tier 3 (Tertiary), Tier 4 (Quaternary)
 */
@Composable
private fun ColorCardItem(
    theme: AppThemePackage,
    isSelected: Boolean,
    onSelect: () -> Unit,
    isPreviewDark: Boolean,
    modifier: Modifier = Modifier
) {
    val tier1 = theme.tier1Primary(isPreviewDark)
    val tier2 = theme.tier2Secondary(isPreviewDark)
    val tier3 = theme.tier3Tertiary(isPreviewDark)
    val tier4 = theme.tier4Swatch(isPreviewDark)

    val textPrimary = if (isPreviewDark) Color(0xFFFFFFFF) else Color(0xFF1A1A1A)
    val textSecondary = if (isPreviewDark) Color(0xFFB4B8C0) else Color(0xFF5A5D64)
    val itemBg = theme.tier4SurfaceLowest(isPreviewDark)
    val itemBorder = if (isSelected) tier1 else theme.tier4Border(isPreviewDark)
    val primaryHex = remember(theme.primaryHex) {
        String.format("#%06X", (0xFFFFFFL and theme.primaryHex))
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(itemBg)
            .border(if (isSelected) 2.dp else 1.dp, itemBorder, RoundedCornerShape(10.dp))
            .clickable { onSelect() }
            .padding(horizontal = 9.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = tier1,
                        modifier = Modifier.size(13.dp)
                    )
                }
                Text(
                    text = theme.shortName,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) tier1 else textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Four Tonal Layer Swatches + Primary Hex label
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(tier1, tier2, tier3, tier4).forEach { swatchColor ->
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(swatchColor)
                                .border(
                                    0.6.dp,
                                    if (isPreviewDark) Color.White.copy(alpha = 0.22f) else Color.Black.copy(alpha = 0.18f),
                                    CircleShape
                                )
                        )
                    }
                }
                Text(
                    text = primaryHex,
                    fontSize = 9.sp,
                    color = textSecondary,
                    maxLines = 1
                )
            }
        }
    }
}
