package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// =============================================================================
// CENTRALIZED 4-TIER COLOR HIERARCHY & OBJECT MAPPING SYSTEM
// =============================================================================
// Tier 1 (Primary): Dominant brand & high-emphasis interactive accent
//   -> Confirm buttons, active selection outlines (1.5-2dp), top header banner,
//      active slider track/thumb, result card top accent bar (2dp), primary audio icons.
// Tier 2 (Secondary): Supporting structural tone & deep/tonal interactive accent
//   -> Secondary action buttons, leading icon badges, grammar category/table headers,
//      dark-mode header surface, secondary interactive pills.
// Tier 3 (Tertiary): Soft harmonic highlight, badge & subtle interactive accent
//   -> Metadata pills, secondary utility/copy/share icons, inactive slider track dots,
//      focused input labels/accents.
// Tier 4 (Quaternary): Ambient surface container, card elevation & structural border layer
//   -> Elevated card/container backgrounds, search field idle background,
//      unselected card/button borders (1dp), section dividers, table row alternation.
// =============================================================================

val GeoTierPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppThemePackage.current.tier1Primary(LocalIsDarkTheme.current)

val GeoTierSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppThemePackage.current.tier2Secondary(LocalIsDarkTheme.current)

val GeoTierTertiary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppThemePackage.current.tier3Tertiary(LocalIsDarkTheme.current)

val GeoTierQuaternary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppThemePackage.current.tier4Quaternary(LocalIsDarkTheme.current)

val GeoTier1Container: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppThemePackage.current.tier1Container(LocalIsDarkTheme.current)

val GeoOnTier1Container: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppThemePackage.current.onTier1Container(LocalIsDarkTheme.current)

val GeoTier2Container: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppThemePackage.current.tier2Container(LocalIsDarkTheme.current)

val GeoOnTier2Container: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppThemePackage.current.onTier2Container(LocalIsDarkTheme.current)

val GeoTier3Container: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppThemePackage.current.tier3Container(LocalIsDarkTheme.current)

val GeoOnTier3Container: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppThemePackage.current.onTier3Container(LocalIsDarkTheme.current)

val GeoTier4SurfaceLow: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppThemePackage.current.tier4SurfaceLow(LocalIsDarkTheme.current)

val GeoTier4SurfaceContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppThemePackage.current.tier4SurfaceContainer(LocalIsDarkTheme.current)

val GeoTier4SurfaceHigh: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppThemePackage.current.tier4SurfaceHigh(LocalIsDarkTheme.current)

val GeoTier4Border: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppThemePackage.current.tier4Border(LocalIsDarkTheme.current)

// =============================================================================
// Dynamic Material 3 & Geo Theme Accessors (Mapped to the 4-Tier System)
// =============================================================================
val GeoPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.primary

val GeoOnPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onPrimary

val GeoPrimaryContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.primaryContainer

val GeoOnPrimaryContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onPrimaryContainer

val GeoSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.secondary

val GeoOnSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSecondary

val GeoSecondaryContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.secondaryContainer

val GeoOnSecondaryContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSecondaryContainer

val GeoTertiary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.tertiary

val GeoOnTertiary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onTertiary

val GeoBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.background

val GeoSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surface

val GeoSurfaceVariant: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surfaceVariant

val GeoOnSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurface

val GeoOnSurfaceVariant: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurfaceVariant

val GeoBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.outlineVariant

val GeoOutline: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.outline

val GeoHeaderKasus: Color
    @Composable
    @ReadOnlyComposable
    get() {
        val theme = LocalAppThemePackage.current
        val isDark = LocalIsDarkTheme.current
        return if (theme.isBoldTheme) {
            Color(if (isDark) theme.headerKasusDarkHex else theme.headerKasusLightHex)
        } else {
            theme.tier2Container(isDark)
        }
    }

val GeoIsBoldTheme: Boolean
    @Composable
    @ReadOnlyComposable
    get() = LocalAppThemePackage.current.isBoldTheme

val GeoGlowColor: Color
    @Composable
    @ReadOnlyComposable
    get() {
        val theme = LocalAppThemePackage.current
        val isDark = LocalIsDarkTheme.current
        return Color(if (isDark) theme.glowColorDarkHex else theme.glowColorLightHex)
    }

val GeoCardRibbonColor: Color
    @Composable
    @ReadOnlyComposable
    get() {
        val theme = LocalAppThemePackage.current
        val isDark = LocalIsDarkTheme.current
        return Color(if (isDark) theme.cardAccentRibbonDarkHex else theme.cardAccentRibbonLightHex)
    }

val GeoBannerGradient: List<Color>
    @Composable
    @ReadOnlyComposable
    get() {
        val theme = LocalAppThemePackage.current
        val isDark = LocalIsDarkTheme.current
        return if (isDark) {
            listOf(Color(theme.bannerGradientStartDarkHex), Color(theme.bannerGradientEndDarkHex))
        } else {
            listOf(Color(theme.bannerGradientStartLightHex), Color(theme.bannerGradientEndLightHex))
        }
    }

val GeoAccentColor: Color
    @Composable
    @ReadOnlyComposable
    get() = GeoTierTertiary

val GeoError: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.error

val GeoOnError: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onError

// Static fallback values for non-composable usages if any
val DefaultGeoPrimary = Color(0xFF6A7A7A)
val DefaultGeoBackground = Color(0xFFF5F7F7)
val DefaultGeoSurface = Color(0xFFFFFFFF)

// Gender Accents (consistent grammatical color convention - fixed semantic labels)
val GeoMaskulinText = Color(0xFF1565C0)
val GeoMaskulinBg = Color(0xFFE0F2FE)
val GeoFemininText = Color(0xFFC2185B)
val GeoFemininBg = Color(0xFFFCE7F3)
val GeoNeutrumText = Color(0xFF8C6239)
val GeoNeutrumBg = Color(0xFFFAF2E6)
val GeoPluralText = Color(0xFF6B21A8)
val GeoPluralBg = Color(0xFFF3E8FF)
val GeoVerbText = Color(0xFF424242)
val GeoVerbBg = Color(0xFFEEEEEE)
val GeoAdjektivText = Color(0xFF00796B)
val GeoAdjektivBg = Color(0xFFE0F2F1)
val GeoSatzText = Color(0xFF2E7D32)
val GeoSatzBg = Color(0xFFE8F5E9)
val GeoWortText = Color(0xFFF57F17)
val GeoWortBg = Color(0xFFFFF9C4)

// =============================================================================
// Element-Specific Accessors Mapped Strictly to the 4-Tier Color System
// =============================================================================
val GeoHeaderBackground: Color
    @Composable
    @ReadOnlyComposable
    get() {
        val theme = LocalAppThemePackage.current
        val isDark = LocalIsDarkTheme.current
        return if (isDark) theme.tier2Secondary(isDark = false) else theme.tier1Primary(isDark = false)
    }

val GeoHeaderContent: Color = Color.White

val GeoMainBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.background

val GeoCardBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppThemePackage.current.tier4SurfaceContainer(LocalIsDarkTheme.current)

val GeoMainTitle: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurface

val GeoCardTitle: Color
    @Composable
    @ReadOnlyComposable
    get() = GeoTierPrimary

val GeoBodyText: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurface

val GeoSecondaryText: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurfaceVariant

val GeoAudioIconColor: Color
    @Composable
    @ReadOnlyComposable
    get() = GeoTierPrimary

val GeoDeleteIconColor: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.error

val GeoDividerColor: Color
    @Composable
    @ReadOnlyComposable
    get() = GeoTier4Border

val GeoSearchFieldIdleBg: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppThemePackage.current.tier4SurfaceHigh(LocalIsDarkTheme.current)

val GeoSearchFieldFocusBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = GeoTierPrimary

val GeoAccentBar: Color
    @Composable
    @ReadOnlyComposable
    get() = GeoTierPrimary

val GeoGrammarTableHeaderBg: Color
    @Composable
    @ReadOnlyComposable
    get() = GeoTier2Container

fun getGrammarTableRowBg(rowIndex: Int, isDark: Boolean): Color {
    return if (isDark) {
        if (rowIndex % 2 == 0) Color(0xFF1A1C20) else Color(0xFF22252B)
    } else {
        if (rowIndex % 2 == 0) Color(0xFFFFFFFF) else Color(0xFFF5F7FA)
    }
}

val GeoModalWindowBg: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppThemePackage.current.tier4SurfaceLow(LocalIsDarkTheme.current)
