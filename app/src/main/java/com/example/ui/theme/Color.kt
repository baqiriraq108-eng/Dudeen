package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// Dynamic Theme Accessors matching the current active theme package
// Automatically adjusts for Light and Dark modes via MaterialTheme.colorScheme.
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
            MaterialTheme.colorScheme.surfaceVariant
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
    get() = LocalAppThemePackage.current.accent

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

// Section 3: Element Application Specific Accessors
val GeoHeaderBackground: Color
    @Composable
    @ReadOnlyComposable
    get() {
        val theme = LocalAppThemePackage.current
        val isDark = LocalIsDarkTheme.current
        return if (isDark) theme.darkVariant else theme.primary
    }

val GeoHeaderContent: Color = Color.White

val GeoMainBackground: Color
    @Composable
    @ReadOnlyComposable
    get() {
        val theme = LocalAppThemePackage.current
        val isDark = LocalIsDarkTheme.current
        return if (isDark) {
            Color(theme.darkVariantHex).copy(alpha = 0.05f)
        } else {
            Color(theme.primaryLightHex).copy(alpha = 0.05f)
        }
    }

val GeoCardBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) Color(0xFF1F1F1F) else Color(0xFFFFFFFF)

val GeoMainTitle: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) Color(0xFFFFFFFF) else Color(0xFF1A1A1A)

val GeoCardTitle: Color
    @Composable
    @ReadOnlyComposable
    get() {
        val theme = LocalAppThemePackage.current
        val isDark = LocalIsDarkTheme.current
        return if (isDark) theme.lightVariant else theme.primary
    }

val GeoBodyText: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) Color(0xFFE0E0E0) else Color(0xFF2A2A2A)

val GeoSecondaryText: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) Color(0xFFAAAAAA) else Color(0xFF6B6B6B)

val GeoAudioIconColor: Color
    @Composable
    @ReadOnlyComposable
    get() {
        val theme = LocalAppThemePackage.current
        val isDark = LocalIsDarkTheme.current
        return if (isDark) theme.lightVariant else theme.primary
    }

val GeoDeleteIconColor: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) Color(0xFFE74C3C) else Color(0xFFC0392B)

val GeoDividerColor: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) Color(0xFF3F3F3F) else Color(0xFFE5E5E5)

val GeoSearchFieldIdleBg: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) Color(0xFF2A2A2A) else Color(0xFFF5F5F5)

val GeoSearchFieldFocusBorder: Color
    @Composable
    @ReadOnlyComposable
    get() {
        val theme = LocalAppThemePackage.current
        val isDark = LocalIsDarkTheme.current
        return if (isDark) theme.lightVariant else theme.primary
    }

val GeoAccentBar: Color
    @Composable
    @ReadOnlyComposable
    get() {
        val theme = LocalAppThemePackage.current
        val isDark = LocalIsDarkTheme.current
        return if (isDark) theme.lightVariant else theme.primary
    }

val GeoGrammarTableHeaderBg: Color
    @Composable
    @ReadOnlyComposable
    get() {
        val theme = LocalAppThemePackage.current
        val isDark = LocalIsDarkTheme.current
        return if (isDark) theme.primary.copy(alpha = 0.15f) else theme.primary.copy(alpha = 0.10f)
    }

fun getGrammarTableRowBg(rowIndex: Int, isDark: Boolean): Color {
    return if (isDark) {
        if (rowIndex % 2 == 0) Color(0xFF1F1F1F) else Color(0xFF252525)
    } else {
        if (rowIndex % 2 == 0) Color(0xFFFFFFFF) else Color(0xFFF9F9F9)
    }
}

val GeoModalWindowBg: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) Color(0xFF1F1F1F) else Color(0xFFFFFFFF)

