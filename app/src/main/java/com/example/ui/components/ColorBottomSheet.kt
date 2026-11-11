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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.ui.theme.GeoFemininBg
import com.example.ui.theme.GeoFemininText
import com.example.ui.theme.GeoMaskulinBg
import com.example.ui.theme.GeoMaskulinText
import com.example.ui.theme.GeoNeutrumBg
import com.example.ui.theme.GeoNeutrumText

/**
 * Separate Color Scheme Window (Bottom Sheet).
 * Displays:
 * 1. Mode Selection: [ System ] [ Light ] [ Dark ]
 * 2. 10 Categories of the 100 Colors (plus preserved classic themes) with expandable headers
 * 3. Live Preview matching the active selected theme and mode
 * 4. Action buttons: Cancel and Apply
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

    val windowBg = if (isPreviewDark) Color(0xFF1F1F1F) else Color(0xFFFFFFFF)
    val textPrimary = if (isPreviewDark) Color(0xFFFFFFFF) else Color(0xFF1A1A1A)
    val textSecondary = if (isPreviewDark) Color(0xFFAAAAAA) else Color(0xFF6B6B6B)
    val dividerColor = if (isPreviewDark) Color(0xFF3F3F3F) else Color(0xFFE5E5E5)

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
                Text(
                    text = strings.chooseColorGroupsLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )

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

                HorizontalDivider(color = dividerColor.copy(alpha = 0.6f), thickness = 1.dp)

                // ==============================================================
                // 3. Live Preview
                // ==============================================================
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = strings.livePreviewLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )

                        Text(
                            text = "${previewTheme.shortName} (${if (isPreviewDark) strings.themeModeDark else strings.themeModeLight})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = previewTheme.primary
                        )
                    }

                    // Live preview mock component
                    LivePreviewMock(
                        theme = previewTheme,
                        isDark = isPreviewDark
                    )
                }

                // ==============================================================
                // 4. Recommendations Note (Card)
                // ==============================================================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPreviewDark) Color(0xFF2A2A2A) else Color(0xFFF7F7F8)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, dividerColor)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = previewTheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                text = strings.designRecommendationsTitle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Text(
                                text = strings.designRecommendationsBody,
                                fontSize = 10.sp,
                                color = textSecondary,
                                lineHeight = 14.sp
                            )
                        }
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
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("cancel_color_selection")
                ) {
                    Text(
                        text = strings.cancel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Button(
                    onClick = {
                        onApplyTheme(previewTheme, previewMode)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = previewTheme.primary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("apply_color_selection")
                ) {
                    Text(
                        text = strings.confirm,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
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
    val cardBg = if (isPreviewDark) Color(0xFF262626) else Color(0xFFF9F9F9)
    val borderColor = if (isPreviewDark) Color(0xFF383838) else Color(0xFFE5E5E5)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(cardBg)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
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
                    color = textPrimary
                )
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
 * Individual Color Card Item displaying Name and Primary, Dark, Light Swatches
 */
@Composable
private fun ColorCardItem(
    theme: AppThemePackage,
    isSelected: Boolean,
    onSelect: () -> Unit,
    isPreviewDark: Boolean,
    modifier: Modifier = Modifier
) {
    val textPrimary = if (isPreviewDark) Color(0xFFFFFFFF) else Color(0xFF1A1A1A)
    val itemBg = if (isPreviewDark) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
    val itemBorder = if (isSelected) theme.primary else (if (isPreviewDark) Color(0xFF383838) else Color(0xFFE2E2E2))

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(itemBg)
            .border(if (isSelected) 2.dp else 1.dp, itemBorder, RoundedCornerShape(8.dp))
            .clickable { onSelect() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Ausgewählt",
                        tint = theme.primary,
                        modifier = Modifier.size(13.dp)
                    )
                }
                Text(
                    text = theme.shortName,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Swatches: Primary, Dark, Light
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Primary
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(theme.primary)
                        .border(0.5.dp, Color.Black.copy(alpha = 0.2f), CircleShape)
                )
                // Dark
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(theme.darkVariant)
                        .border(0.5.dp, Color.Black.copy(alpha = 0.2f), CircleShape)
                )
                // Light
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(theme.lightVariant)
                        .border(0.5.dp, Color.Black.copy(alpha = 0.2f), CircleShape)
                )
            }
        }
    }
}

/**
 * Live Preview Mock Component simulating the app interface using Section 3 rules:
 * - Header: Primary background + White text/icons (Light) / Dark variant + White text/icons (Dark)
 * - Main background: 5% Primary (Light) / 5% Dark variant (Dark)
 * - Card background: #FFFFFF (Light) / #1F1F1F (Dark)
 * - Main titles: #1A1A1A (Light) / #FFFFFF (Dark)
 * - Card titles: Primary (Light) / Light variant (Dark)
 * - Body text: #2A2A2A (Light) / #E0E0E0 (Dark)
 * - Secondary text: #6B6B6B (Light) / #AAAAAA (Dark)
 * - Primary buttons: Primary + White
 * - Result card top accent bar (2dp): Primary (Light) / Light variant (Dark)
 * - Search field: #F5F5F5 (Light) / #2A2A2A (Dark)
 * - Audio icon: Primary (Light) / Light variant (Dark)
 * - Semantic labels (Maskulin, Feminin, Neutrum): remain fixed
 */
@Composable
private fun LivePreviewMock(
    theme: AppThemePackage,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val headerBg = if (isDark) theme.darkVariant else theme.primary
    val mainBg = if (isDark) Color(0xFF141311) else Color(0xFFFAFAFA)
    val cardBg = if (isDark) Color(0xFF1F1F1F) else Color(0xFFFFFFFF)
    val mainTitle = if (isDark) Color(0xFFFFFFFF) else Color(0xFF1A1A1A)
    val cardTitle = if (isDark) theme.lightVariant else theme.primary
    val bodyText = if (isDark) Color(0xFFE0E0E0) else Color(0xFF2A2A2A)
    val secondaryText = if (isDark) Color(0xFFAAAAAA) else Color(0xFF6B6B6B)
    val searchIdleBg = if (isDark) Color(0xFF2A2A2A) else Color(0xFFF5F5F5)
    val accentBarColor = if (isDark) theme.lightVariant else theme.primary
    val audioIconColor = if (isDark) theme.lightVariant else theme.primary

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = mainBg),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF3F3F3F) else Color(0xFFE5E5E5))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. Header (Primary / Dark variant + White text/icons)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerBg)
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🇩🇪", fontSize = 10.sp)
                    }
                    Text(
                        text = "Deklination & Konjugation",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✨", fontSize = 9.sp)
                    }
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎨", fontSize = 9.sp)
                    }
                }
            }

            // Preview Body with Search & Result Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 2. Search Field + Primary Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(searchIdleBg)
                            .border(1.dp, if (isDark) Color(0xFF3F3F3F) else Color(0xFFE5E5E5), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = cardTitle,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Haus",
                            fontSize = 10.sp,
                            color = bodyText
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(theme.primary)
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Suchen",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // 3. Result Card with 2dp Top Accent Bar
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(0.5.dp, if (isDark) Color(0xFF3F3F3F) else Color(0xFFE5E5E5))
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // 2dp accent bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(accentBarColor)
                        )

                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Title & Audio icon
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "das Haus",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = cardTitle
                                    )
                                    // Fixed Semantic Badge: Neutrum (remains fixed)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(GeoNeutrumBg)
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "Neutrum",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GeoNeutrumText
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = null,
                                    tint = audioIconColor,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            // Secondary translation & body text
                            Text(
                                text = "Übersetzung: house, building • Plural: die Häuser",
                                fontSize = 10.sp,
                                color = bodyText
                            )

                            // Fixed Semantic Badges: Maskulin & Feminin to demonstrate fixed colors
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(GeoMaskulinBg)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("der (M)", fontSize = 8.sp, color = GeoMaskulinText, fontWeight = FontWeight.Bold)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(GeoFemininBg)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("die (F)", fontSize = 8.sp, color = GeoFemininText, fontWeight = FontWeight.Bold)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(GeoNeutrumBg)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("das (N)", fontSize = 8.sp, color = GeoNeutrumText, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
