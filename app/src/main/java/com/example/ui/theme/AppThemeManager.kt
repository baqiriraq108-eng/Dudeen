package com.example.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Material 3 Earth Tone Themes for Deklination:
 * 40 distinctly selected earth tones across 5 color families:
 * 1. Rotbraun & Terracotta (Warm Earth)
 * 2. Sand & Beige (Neutral Warm)
 * 3. Grau & Schiefer (Cool Earth)
 * 4. Braun & Umbra (Deep Earth)
 * 5. Gedämpfte Töne (Lehm & Stein)
 *
 * Default theme: SCHIEFER (#6A7A7A)
 */
enum class AppThemePackage(
    val id: String,
    val germanName: String,
    val shortName: String,
    val primaryHex: Long,
    val secondaryHex: Long,
    val backgroundHex: Long,
    val surfaceHex: Long,
    val onSurfaceHex: Long,
    val primaryContainerHex: Long,
    val onPrimaryContainerHex: Long,
    val accentHex: Long,
    val borderHex: Long,
    val surfaceVariantHex: Long,
    val onSurfaceVariantHex: Long,
    val outlineHex: Long,
    val swatchHex: Long = primaryHex,
    val arabicName: String = "",
    val hasCustomPalette: Boolean = false,
    val primaryLightHex: Long = primaryHex,
    val onPrimaryLightHex: Long = 0xFFFFFFFF,
    val primaryContainerLightHex: Long = primaryContainerHex,
    val onPrimaryContainerLightHex: Long = onPrimaryContainerHex,
    val secondaryLightHex: Long = secondaryHex,
    val onSecondaryLightHex: Long = 0xFFFFFFFF,
    val secondaryContainerLightHex: Long = primaryContainerHex,
    val onSecondaryContainerLightHex: Long = onPrimaryContainerHex,
    val tertiaryLightHex: Long = accentHex,
    val onTertiaryLightHex: Long = 0xFFFFFFFF,
    val tertiaryContainerLightHex: Long = surfaceVariantHex,
    val onTertiaryContainerLightHex: Long = onSurfaceVariantHex,
    val errorLightHex: Long = 0xFFBA1A1A,
    val onErrorLightHex: Long = 0xFFFFFFFF,
    val errorContainerLightHex: Long = 0xFFFFDAD6,
    val onErrorContainerLightHex: Long = 0xFF410002,
    val backgroundLightHex: Long = backgroundHex,
    val onBackgroundLightHex: Long = onSurfaceHex,
    val surfaceLightHex: Long = surfaceHex,
    val onSurfaceLightHex: Long = onSurfaceHex,
    val surfaceVariantLightHex: Long = surfaceVariantHex,
    val onSurfaceVariantLightHex: Long = onSurfaceVariantHex,
    val outlineLightHex: Long = outlineHex,
    val primaryDarkHex: Long = accentHex,
    val onPrimaryDarkHex: Long = 0xFF1E1A16,
    val primaryContainerDarkHex: Long = primaryHex,
    val onPrimaryContainerDarkHex: Long = 0xFFFFDBCF,
    val secondaryDarkHex: Long = secondaryHex,
    val onSecondaryDarkHex: Long = 0xFFFFFFFF,
    val secondaryContainerDarkHex: Long = 0xFF2C2824,
    val onSecondaryContainerDarkHex: Long = 0xFFE8E2DC,
    val tertiaryDarkHex: Long = swatchHex,
    val onTertiaryDarkHex: Long = 0xFF1E1A16,
    val tertiaryContainerDarkHex: Long = 0xFF364D2F,
    val onTertiaryContainerDarkHex: Long = 0xFFCEEBC2,
    val errorDarkHex: Long = 0xFFFFB4AB,
    val onErrorDarkHex: Long = 0xFF690005,
    val errorContainerDarkHex: Long = 0xFF93000A,
    val onErrorContainerDarkHex: Long = 0xFFFFDAD6,
    val backgroundDarkHex: Long = 0xFF141311,
    val onBackgroundDarkHex: Long = 0xFFE8E2DC,
    val surfaceDarkHex: Long = 0xFF1C1B18,
    val onSurfaceDarkHex: Long = 0xFFE8E2DC,
    val surfaceVariantDarkHex: Long = 0xFF2B2824,
    val onSurfaceVariantDarkHex: Long = 0xFFCFC4BA,
    val outlineDarkHex: Long = 0xFF988E84,
    // Creative Bold Theme Styling properties:
    val isBoldTheme: Boolean = false,
    val headerKasusLightHex: Long = surfaceVariantLightHex,
    val headerKasusDarkHex: Long = surfaceVariantDarkHex,
    val glowColorLightHex: Long = accentHex,
    val glowColorDarkHex: Long = accentHex,
    val bannerGradientStartLightHex: Long = primaryContainerLightHex,
    val bannerGradientEndLightHex: Long = accentHex,
    val bannerGradientStartDarkHex: Long = primaryHex,
    val bannerGradientEndDarkHex: Long = accentHex,
    val cardAccentRibbonLightHex: Long = accentHex,
    val cardAccentRibbonDarkHex: Long = accentHex,
    val categoryName: String = ""
) {
    // =========================================================================
    // 0. Material 3 Haupt-Farben (6 Separate Vollständige Paletten)
    // =========================================================================
    GELB(
        id = "Gelb",
        germanName = "Gelb (Sonnengelb)",
        shortName = "Gelb",
        primaryHex = 0xFF6D5E00,
        secondaryHex = 0xFF6D5E00,
        backgroundHex = 0xFFFFFBF0,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF1F1B10,
        primaryContainerHex = 0xFFFAC636,
        onPrimaryContainerHex = 0xFF221B00,
        accentHex = 0xFFFAC636,
        borderHex = 0xFFEBE4D2,
        surfaceVariantHex = 0xFFF3EDDC,
        onSurfaceVariantHex = 0xFF4A473A,
        outlineHex = 0xFF7C7768,
        swatchHex = 0xFFFAC636,
        hasCustomPalette = true,
        primaryLightHex = 0xFF6D5E00,
        onPrimaryLightHex = 0xFFFFFFFF,
        primaryContainerLightHex = 0xFFFAC636,
        onPrimaryContainerLightHex = 0xFF221B00,
        secondaryLightHex = 0xFF6D5E00,
        onSecondaryLightHex = 0xFFFFFFFF,
        secondaryContainerLightHex = 0xFFFFE08A,
        onSecondaryContainerLightHex = 0xFF221B00,
        tertiaryLightHex = 0xFF4C6544,
        onTertiaryLightHex = 0xFFFFFFFF,
        tertiaryContainerLightHex = 0xFFCEEBC2,
        onTertiaryContainerLightHex = 0xFF0A2007,
        errorLightHex = 0xFFBA1A1A,
        onErrorLightHex = 0xFFFFFFFF,
        errorContainerLightHex = 0xFFFFDAD6,
        onErrorContainerLightHex = 0xFF410002,
        backgroundLightHex = 0xFFFFFBF0,
        onBackgroundLightHex = 0xFF1F1B10,
        surfaceLightHex = 0xFFFFFFFF,
        onSurfaceLightHex = 0xFF1F1B10,
        surfaceVariantLightHex = 0xFFF3EDDC,
        onSurfaceVariantLightHex = 0xFF4A473A,
        outlineLightHex = 0xFF7C7768,
        primaryDarkHex = 0xFFE1C34A,
        onPrimaryDarkHex = 0xFF3A2F00,
        primaryContainerDarkHex = 0xFF574500,
        onPrimaryContainerDarkHex = 0xFFFAC636,
        secondaryDarkHex = 0xFFE1C34A,
        onSecondaryDarkHex = 0xFF3A2F00,
        secondaryContainerDarkHex = 0xFF574500,
        onSecondaryContainerDarkHex = 0xFFFFE08A,
        tertiaryDarkHex = 0xFFB2CFA7,
        onTertiaryDarkHex = 0xFF1F3519,
        tertiaryContainerDarkHex = 0xFF364D2F,
        onTertiaryContainerDarkHex = 0xFFCEEBC2,
        errorDarkHex = 0xFFFFB4AB,
        onErrorDarkHex = 0xFF690005,
        errorContainerDarkHex = 0xFF93000A,
        onErrorContainerDarkHex = 0xFFFFDAD6,
        backgroundDarkHex = 0xFF1F1B10,
        onBackgroundDarkHex = 0xFFEBE2D3,
        surfaceDarkHex = 0xFF1F1B10,
        onSurfaceDarkHex = 0xFFEBE2D3,
        surfaceVariantDarkHex = 0xFF4A473A,
        onSurfaceVariantDarkHex = 0xFFCBC5B8,
        outlineDarkHex = 0xFF959080
    ),
    DUNKELBLAU(
        id = "Dunkelblau",
        germanName = "Dunkelblau (Marineblau)",
        shortName = "Dunkelblau",
        primaryHex = 0xFF154B74,
        secondaryHex = 0xFF526070,
        backgroundHex = 0xFFF8F9FF,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF191C20,
        primaryContainerHex = 0xFFD3E4FF,
        onPrimaryContainerHex = 0xFF001C38,
        accentHex = 0xFF154B74,
        borderHex = 0xFFDFE2EB,
        surfaceVariantHex = 0xFFDFE2EB,
        onSurfaceVariantHex = 0xFF43474E,
        outlineHex = 0xFF73777F,
        swatchHex = 0xFF154B74,
        hasCustomPalette = true,
        primaryLightHex = 0xFF154B74,
        onPrimaryLightHex = 0xFFFFFFFF,
        primaryContainerLightHex = 0xFFD3E4FF,
        onPrimaryContainerLightHex = 0xFF001C38,
        secondaryLightHex = 0xFF526070,
        onSecondaryLightHex = 0xFFFFFFFF,
        secondaryContainerLightHex = 0xFFD6E4F7,
        onSecondaryContainerLightHex = 0xFF0F1D2A,
        tertiaryLightHex = 0xFF6B5778,
        onTertiaryLightHex = 0xFFFFFFFF,
        tertiaryContainerLightHex = 0xFFF2DAFF,
        onTertiaryContainerLightHex = 0xFF261430,
        errorLightHex = 0xFFBA1A1A,
        onErrorLightHex = 0xFFFFFFFF,
        errorContainerLightHex = 0xFFFFDAD6,
        onErrorContainerLightHex = 0xFF410002,
        backgroundLightHex = 0xFFF8F9FF,
        onBackgroundLightHex = 0xFF191C20,
        surfaceLightHex = 0xFFFFFFFF,
        onSurfaceLightHex = 0xFF191C20,
        surfaceVariantLightHex = 0xFFDFE2EB,
        onSurfaceVariantLightHex = 0xFF43474E,
        outlineLightHex = 0xFF73777F,
        primaryDarkHex = 0xFFA1C9FD,
        onPrimaryDarkHex = 0xFF003258,
        primaryContainerDarkHex = 0xFF00497D,
        onPrimaryContainerDarkHex = 0xFFD3E4FF,
        secondaryDarkHex = 0xFFBAC8DB,
        onSecondaryDarkHex = 0xFF243141,
        secondaryContainerDarkHex = 0xFF3A4859,
        onSecondaryContainerDarkHex = 0xFFD6E4F7,
        tertiaryDarkHex = 0xFFD6BEE4,
        onTertiaryDarkHex = 0xFF3B2948,
        tertiaryContainerDarkHex = 0xFF523F60,
        onTertiaryContainerDarkHex = 0xFFF2DAFF,
        errorDarkHex = 0xFFFFB4AB,
        onErrorDarkHex = 0xFF690005,
        errorContainerDarkHex = 0xFF93000A,
        onErrorContainerDarkHex = 0xFFFFDAD6,
        backgroundDarkHex = 0xFF191C20,
        onBackgroundDarkHex = 0xFFE2E2E9,
        surfaceDarkHex = 0xFF191C20,
        onSurfaceDarkHex = 0xFFE2E2E9,
        surfaceVariantDarkHex = 0xFF43474E,
        onSurfaceVariantDarkHex = 0xFFC3C6CF,
        outlineDarkHex = 0xFF8D9199
    ),
    HELLGRAU(
        id = "Hellgrau",
        germanName = "Hellgrau (Schimmerndes Grau)",
        shortName = "Hellgrau",
        primaryHex = 0xFF3C606E,
        secondaryHex = 0xFF4F6168,
        backgroundHex = 0xFFF3F6F8,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF191C1E,
        primaryContainerHex = 0xFFD8E5EC,
        onPrimaryContainerHex = 0xFF051F27,
        accentHex = 0xFF3C606E,
        borderHex = 0xFFD8E0E4,
        surfaceVariantHex = 0xFFDEE4E7,
        onSurfaceVariantHex = 0xFF40484B,
        outlineHex = 0xFF70787C,
        swatchHex = 0xFFF3F6F8,
        hasCustomPalette = true,
        primaryLightHex = 0xFF3C606E,
        onPrimaryLightHex = 0xFFFFFFFF,
        primaryContainerLightHex = 0xFFD8E5EC,
        onPrimaryContainerLightHex = 0xFF051F27,
        secondaryLightHex = 0xFF4F6168,
        onSecondaryLightHex = 0xFFFFFFFF,
        secondaryContainerLightHex = 0xFFD2E5EC,
        onSecondaryContainerLightHex = 0xFF0C1D24,
        tertiaryLightHex = 0xFF5C5B7E,
        onTertiaryLightHex = 0xFFFFFFFF,
        tertiaryContainerLightHex = 0xFFE2DFFF,
        onTertiaryContainerLightHex = 0xFF181837,
        errorLightHex = 0xFFBA1A1A,
        onErrorLightHex = 0xFFFFFFFF,
        errorContainerLightHex = 0xFFFFDAD6,
        onErrorContainerLightHex = 0xFF410002,
        backgroundLightHex = 0xFFF3F6F8,
        onBackgroundLightHex = 0xFF191C1E,
        surfaceLightHex = 0xFFFFFFFF,
        onSurfaceLightHex = 0xFF191C1E,
        surfaceVariantLightHex = 0xFFDEE4E7,
        onSurfaceVariantLightHex = 0xFF40484B,
        outlineLightHex = 0xFF70787C,
        primaryDarkHex = 0xFFA4CDDC,
        onPrimaryDarkHex = 0xFF043542,
        primaryContainerDarkHex = 0xFF234B57,
        onPrimaryContainerDarkHex = 0xFFD8E5EC,
        secondaryDarkHex = 0xFFB6C9D0,
        onSecondaryDarkHex = 0xFF213239,
        secondaryContainerDarkHex = 0xFF374950,
        onSecondaryContainerDarkHex = 0xFFD2E5EC,
        tertiaryDarkHex = 0xFFC5C3EA,
        onTertiaryDarkHex = 0xFF2D2D4D,
        tertiaryContainerDarkHex = 0xFF444365,
        onTertiaryContainerDarkHex = 0xFFE2DFFF,
        errorDarkHex = 0xFFFFB4AB,
        onErrorDarkHex = 0xFF690005,
        errorContainerDarkHex = 0xFF93000A,
        onErrorContainerDarkHex = 0xFFFFDAD6,
        backgroundDarkHex = 0xFF191C1E,
        onBackgroundDarkHex = 0xFFE1E3E4,
        surfaceDarkHex = 0xFF191C1E,
        onSurfaceDarkHex = 0xFFE1E3E4,
        surfaceVariantDarkHex = 0xFF40484B,
        onSurfaceVariantDarkHex = 0xFFBFC8CB,
        outlineDarkHex = 0xFF8A9295
    ),
    WEISS(
        id = "Weiss",
        germanName = "Weiß (Klassisches Weiß)",
        shortName = "Weiß",
        primaryHex = 0xFF202020,
        secondaryHex = 0xFF555555,
        backgroundHex = 0xFFFFFFFF,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF181818,
        primaryContainerHex = 0xFFF0F0F0,
        onPrimaryContainerHex = 0xFF141414,
        accentHex = 0xFF202020,
        borderHex = 0xFFE5E5E5,
        surfaceVariantHex = 0xFFF2F2F2,
        onSurfaceVariantHex = 0xFF424242,
        outlineHex = 0xFF757575,
        swatchHex = 0xFFFFFFFF,
        hasCustomPalette = true,
        primaryLightHex = 0xFF202020,
        onPrimaryLightHex = 0xFFFFFFFF,
        primaryContainerLightHex = 0xFFF0F0F0,
        onPrimaryContainerLightHex = 0xFF141414,
        secondaryLightHex = 0xFF555555,
        onSecondaryLightHex = 0xFFFFFFFF,
        secondaryContainerLightHex = 0xFFEEEEEE,
        onSecondaryContainerLightHex = 0xFF141414,
        tertiaryLightHex = 0xFF3D3D3D,
        onTertiaryLightHex = 0xFFFFFFFF,
        tertiaryContainerLightHex = 0xFFE0E0E0,
        onTertiaryContainerLightHex = 0xFF141414,
        errorLightHex = 0xFFBA1A1A,
        onErrorLightHex = 0xFFFFFFFF,
        errorContainerLightHex = 0xFFFFDAD6,
        onErrorContainerLightHex = 0xFF410002,
        backgroundLightHex = 0xFFFFFFFF,
        onBackgroundLightHex = 0xFF181818,
        surfaceLightHex = 0xFFFFFFFF,
        onSurfaceLightHex = 0xFF181818,
        surfaceVariantLightHex = 0xFFF2F2F2,
        onSurfaceVariantLightHex = 0xFF424242,
        outlineLightHex = 0xFF757575,
        primaryDarkHex = 0xFFE0E0E0,
        onPrimaryDarkHex = 0xFF1A1A1A,
        primaryContainerDarkHex = 0xFF303030,
        onPrimaryContainerDarkHex = 0xFFF5F5F5,
        secondaryDarkHex = 0xFFC8C8C8,
        onSecondaryDarkHex = 0xFF252525,
        secondaryContainerDarkHex = 0xFF3A3A3A,
        onSecondaryContainerDarkHex = 0xFFEEEEEE,
        tertiaryDarkHex = 0xFFD0D0D0,
        onTertiaryDarkHex = 0xFF252525,
        tertiaryContainerDarkHex = 0xFF383838,
        onTertiaryContainerDarkHex = 0xFFEAEAEA,
        errorDarkHex = 0xFFFFB4AB,
        onErrorDarkHex = 0xFF690005,
        errorContainerDarkHex = 0xFF93000A,
        onErrorContainerDarkHex = 0xFFFFDAD6,
        backgroundDarkHex = 0xFF121212,
        onBackgroundDarkHex = 0xFFEAEAEA,
        surfaceDarkHex = 0xFF1A1A1A,
        onSurfaceDarkHex = 0xFFEAEAEA,
        surfaceVariantDarkHex = 0xFF2C2C2C,
        onSurfaceVariantDarkHex = 0xFFC4C4C4,
        outlineDarkHex = 0xFF8E8E8E
    ),
    DUNKELGRAU(
        id = "Dunkelgrau",
        germanName = "Dunkelgrau (Anthrazit)",
        shortName = "Dunkelgrau",
        primaryHex = 0xFF232323,
        secondaryHex = 0xFF525252,
        backgroundHex = 0xFFF8F8FA,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF18181B,
        primaryContainerHex = 0xFFE4E4E7,
        onPrimaryContainerHex = 0xFF18181B,
        accentHex = 0xFF232323,
        borderHex = 0xFFE4E4E7,
        surfaceVariantHex = 0xFFEEEEF1,
        onSurfaceVariantHex = 0xFF3F3F46,
        outlineHex = 0xFF71717A,
        swatchHex = 0xFF232323,
        hasCustomPalette = true,
        primaryLightHex = 0xFF232323,
        onPrimaryLightHex = 0xFFFFFFFF,
        primaryContainerLightHex = 0xFFE4E4E7,
        onPrimaryContainerLightHex = 0xFF18181B,
        secondaryLightHex = 0xFF525252,
        onSecondaryLightHex = 0xFFFFFFFF,
        secondaryContainerLightHex = 0xFFE5E5E5,
        onSecondaryContainerLightHex = 0xFF18181B,
        tertiaryLightHex = 0xFF3F3F46,
        onTertiaryLightHex = 0xFFFFFFFF,
        tertiaryContainerLightHex = 0xFFD4D4D8,
        onTertiaryContainerLightHex = 0xFF18181B,
        errorLightHex = 0xFFBA1A1A,
        onErrorLightHex = 0xFFFFFFFF,
        errorContainerLightHex = 0xFFFFDAD6,
        onErrorContainerLightHex = 0xFF410002,
        backgroundLightHex = 0xFFF8F8FA,
        onBackgroundLightHex = 0xFF18181B,
        surfaceLightHex = 0xFFFFFFFF,
        onSurfaceLightHex = 0xFF18181B,
        surfaceVariantLightHex = 0xFFEEEEF1,
        onSurfaceVariantLightHex = 0xFF3F3F46,
        outlineLightHex = 0xFF71717A,
        primaryDarkHex = 0xFFD4D4D8,
        onPrimaryDarkHex = 0xFF18181B,
        primaryContainerDarkHex = 0xFF27272A,
        onPrimaryContainerDarkHex = 0xFFF4F4F5,
        secondaryDarkHex = 0xFFA1A1AA,
        onSecondaryDarkHex = 0xFF18181B,
        secondaryContainerDarkHex = 0xFF3F3F46,
        onSecondaryContainerDarkHex = 0xFFE4E4E7,
        tertiaryDarkHex = 0xFFCBD5E1,
        onTertiaryDarkHex = 0xFF0F172A,
        tertiaryContainerDarkHex = 0xFF334155,
        onTertiaryContainerDarkHex = 0xFFE2E8F0,
        errorDarkHex = 0xFFFFB4AB,
        onErrorDarkHex = 0xFF690005,
        errorContainerDarkHex = 0xFF93000A,
        onErrorContainerDarkHex = 0xFFFFDAD6,
        backgroundDarkHex = 0xFF0F0F11,
        onBackgroundDarkHex = 0xFFF4F4F5,
        surfaceDarkHex = 0xFF18181B,
        onSurfaceDarkHex = 0xFFF4F4F5,
        surfaceVariantDarkHex = 0xFF27272A,
        onSurfaceVariantDarkHex = 0xFFA1A1AA,
        outlineDarkHex = 0xFF71717A
    ),
    ROT(
        id = "Rot",
        germanName = "Rot (Kräftiges Rubinrot)",
        shortName = "Rot",
        primaryHex = 0xFFBA1A2B,
        secondaryHex = 0xFF775656,
        backgroundHex = 0xFFFFF8F7,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF201A1A,
        primaryContainerHex = 0xFFFFDAD9,
        onPrimaryContainerHex = 0xFF410007,
        accentHex = 0xFFE51D33,
        borderHex = 0xFFF4DDDC,
        surfaceVariantHex = 0xFFF4DDDC,
        onSurfaceVariantHex = 0xFF524343,
        outlineHex = 0xFF857372,
        swatchHex = 0xFFE51D33,
        hasCustomPalette = true,
        primaryLightHex = 0xFFBA1A2B,
        onPrimaryLightHex = 0xFFFFFFFF,
        primaryContainerLightHex = 0xFFFFDAD9,
        onPrimaryContainerLightHex = 0xFF410007,
        secondaryLightHex = 0xFF775656,
        onSecondaryLightHex = 0xFFFFFFFF,
        secondaryContainerLightHex = 0xFFFFDAD9,
        onSecondaryContainerLightHex = 0xFF2C1515,
        tertiaryLightHex = 0xFF6C5D2F,
        onTertiaryLightHex = 0xFFFFFFFF,
        tertiaryContainerLightHex = 0xFFF5E1A7,
        onTertiaryContainerLightHex = 0xFF231B00,
        errorLightHex = 0xFFBA1A1A,
        onErrorLightHex = 0xFFFFFFFF,
        errorContainerLightHex = 0xFFFFDAD6,
        onErrorContainerLightHex = 0xFF410002,
        backgroundLightHex = 0xFFFFF8F7,
        onBackgroundLightHex = 0xFF201A1A,
        surfaceLightHex = 0xFFFFFFFF,
        onSurfaceLightHex = 0xFF201A1A,
        surfaceVariantLightHex = 0xFFF4DDDC,
        onSurfaceVariantLightHex = 0xFF524343,
        outlineLightHex = 0xFF857372,
        primaryDarkHex = 0xFFFFB3B1,
        onPrimaryDarkHex = 0xFF69000D,
        primaryContainerDarkHex = 0xFF930018,
        onPrimaryContainerDarkHex = 0xFFFFDAD9,
        secondaryDarkHex = 0xFFE7BDBD,
        onSecondaryDarkHex = 0xFF442929,
        secondaryContainerDarkHex = 0xFF5D3F3F,
        onSecondaryContainerDarkHex = 0xFFFFDAD9,
        tertiaryDarkHex = 0xFFD8C58D,
        onTertiaryDarkHex = 0xFF3B2F05,
        tertiaryContainerDarkHex = 0xFF534619,
        onTertiaryContainerDarkHex = 0xFFF5E1A7,
        errorDarkHex = 0xFFFFB4AB,
        onErrorDarkHex = 0xFF690005,
        errorContainerDarkHex = 0xFF93000A,
        onErrorContainerDarkHex = 0xFFFFDAD6,
        backgroundDarkHex = 0xFF201A1A,
        onBackgroundDarkHex = 0xFFEBE0DF,
        surfaceDarkHex = 0xFF201A1A,
        onSurfaceDarkHex = 0xFFEBE0DF,
        surfaceVariantDarkHex = 0xFF524343,
        onSurfaceVariantDarkHex = 0xFFD7C1C0,
        outlineDarkHex = 0xFF9F8C8B
    ),
    // =========================================================================
    // 0B. Kräftige & Expressive Farben (10 Neue Themen - Nicht Material 3)
    // =========================================================================
    NEON_GELB(
        id = "NeonGelb",
        germanName = "Neon-Gelb (Leuchtend)",
        shortName = "Neon-Gelb",
        primaryHex = 0xFFB28700,
        secondaryHex = 0xFFFFD600,
        backgroundHex = 0xFFFFFDF2,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF1A1910,
        primaryContainerHex = 0xFFFFF07A,
        onPrimaryContainerHex = 0xFF2A2000,
        accentHex = 0xFFFFE600,
        borderHex = 0xFFF5E6A3,
        surfaceVariantHex = 0xFFFFF9D2,
        onSurfaceVariantHex = 0xFF4D4625,
        outlineHex = 0xFFD4A000,
        swatchHex = 0xFFFFE600,
        hasCustomPalette = true,
        primaryLightHex = 0xFFB28700,
        onPrimaryLightHex = 0xFFFFFFFF,
        primaryContainerLightHex = 0xFFFFF07A,
        onPrimaryContainerLightHex = 0xFF2A2000,
        secondaryLightHex = 0xFFE5B800,
        onSecondaryLightHex = 0xFF1A1910,
        secondaryContainerLightHex = 0xFFFFF59D,
        onSecondaryContainerLightHex = 0xFF2D2400,
        tertiaryLightHex = 0xFFFFD600,
        onTertiaryLightHex = 0xFF1A1910,
        tertiaryContainerLightHex = 0xFFFFF9C4,
        onTertiaryContainerLightHex = 0xFF282000,
        errorLightHex = 0xFFBA1A1A,
        onErrorLightHex = 0xFFFFFFFF,
        errorContainerLightHex = 0xFFFFDAD6,
        onErrorContainerLightHex = 0xFF410002,
        backgroundLightHex = 0xFFFFFDF2,
        onBackgroundLightHex = 0xFF1A1910,
        surfaceLightHex = 0xFFFFFFFF,
        onSurfaceLightHex = 0xFF1A1910,
        surfaceVariantLightHex = 0xFFFFF9D2,
        onSurfaceVariantLightHex = 0xFF4D4625,
        outlineLightHex = 0xFFD4A000,
        primaryDarkHex = 0xFFFFE600,
        onPrimaryDarkHex = 0xFF1A1800,
        primaryContainerDarkHex = 0xFF524300,
        onPrimaryContainerDarkHex = 0xFFFFF275,
        secondaryDarkHex = 0xFFFFEA3B,
        onSecondaryDarkHex = 0xFF241D00,
        secondaryContainerDarkHex = 0xFF423700,
        onSecondaryContainerDarkHex = 0xFFFFEE55,
        tertiaryDarkHex = 0xFFFFD600,
        onTertiaryDarkHex = 0xFF221A00,
        tertiaryContainerDarkHex = 0xFF473A00,
        onTertiaryContainerDarkHex = 0xFFFFEA75,
        errorDarkHex = 0xFFFFB4AB,
        onErrorDarkHex = 0xFF690005,
        errorContainerDarkHex = 0xFF93000A,
        onErrorContainerDarkHex = 0xFFFFDAD6,
        backgroundDarkHex = 0xFF12120A,
        onBackgroundDarkHex = 0xFFFBF8E6,
        surfaceDarkHex = 0xFF1A1A10,
        onSurfaceDarkHex = 0xFFFBF8E6,
        surfaceVariantDarkHex = 0xFF2B2917,
        onSurfaceVariantDarkHex = 0xFFE6DC9E,
        outlineDarkHex = 0xFFFFE600,
        isBoldTheme = true,
        headerKasusLightHex = 0xFFFFF275,
        headerKasusDarkHex = 0xFF3D3505,
        glowColorLightHex = 0xFFFFE600,
        glowColorDarkHex = 0xFFFFE600,
        bannerGradientStartLightHex = 0xFFFFE600,
        bannerGradientEndLightHex = 0xFFFFB300,
        bannerGradientStartDarkHex = 0xFFFFE600,
        bannerGradientEndDarkHex = 0xFFFFB300,
        cardAccentRibbonLightHex = 0xFFFFE600,
        cardAccentRibbonDarkHex = 0xFFFFE600
    ),
    KNALLROT(
        id = "Knallrot",
        germanName = "Knallrot (Signalrot)",
        shortName = "Knallrot",
        primaryHex = 0xFFD50000,
        secondaryHex = 0xFFFF1744,
        backgroundHex = 0xFFFFF8F8,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF220A0D,
        primaryContainerHex = 0xFFFFD6D9,
        onPrimaryContainerHex = 0xFF440007,
        accentHex = 0xFFFF1744,
        borderHex = 0xFFFFCDD2,
        surfaceVariantHex = 0xFFFFEBEE,
        onSurfaceVariantHex = 0xFF5C3337,
        outlineHex = 0xFFFF1744,
        swatchHex = 0xFFFF1744,
        hasCustomPalette = true,
        primaryLightHex = 0xFFD50000,
        onPrimaryLightHex = 0xFFFFFFFF,
        primaryContainerLightHex = 0xFFFFD6D9,
        onPrimaryContainerLightHex = 0xFF440007,
        secondaryLightHex = 0xFFFF1744,
        onSecondaryLightHex = 0xFFFFFFFF,
        secondaryContainerLightHex = 0xFFFFEBEE,
        onSecondaryContainerLightHex = 0xFF400006,
        tertiaryLightHex = 0xFFFF5252,
        onTertiaryLightHex = 0xFFFFFFFF,
        tertiaryContainerLightHex = 0xFFFFCDD2,
        onTertiaryContainerLightHex = 0xFF380005,
        errorLightHex = 0xFFBA1A1A,
        onErrorLightHex = 0xFFFFFFFF,
        errorContainerLightHex = 0xFFFFDAD6,
        onErrorContainerLightHex = 0xFF410002,
        backgroundLightHex = 0xFFFFF8F8,
        onBackgroundLightHex = 0xFF220A0D,
        surfaceLightHex = 0xFFFFFFFF,
        onSurfaceLightHex = 0xFF220A0D,
        surfaceVariantLightHex = 0xFFFFEBEE,
        onSurfaceVariantLightHex = 0xFF5C3337,
        outlineLightHex = 0xFFFF1744,
        primaryDarkHex = 0xFFFF5252,
        onPrimaryDarkHex = 0xFF3B0006,
        primaryContainerDarkHex = 0xFF68000E,
        onPrimaryContainerDarkHex = 0xFFFFCDD2,
        secondaryDarkHex = 0xFFFF1744,
        onSecondaryDarkHex = 0xFFFFFFFF,
        secondaryContainerDarkHex = 0xFF50000A,
        onSecondaryContainerDarkHex = 0xFFFFD6D9,
        tertiaryDarkHex = 0xFFFF8A80,
        onTertiaryDarkHex = 0xFF450007,
        tertiaryContainerDarkHex = 0xFF60000D,
        onTertiaryContainerDarkHex = 0xFFFFCDD2,
        errorDarkHex = 0xFFFFB4AB,
        onErrorDarkHex = 0xFF690005,
        errorContainerDarkHex = 0xFF93000A,
        onErrorContainerDarkHex = 0xFFFFDAD6,
        backgroundDarkHex = 0xFF15080A,
        onBackgroundDarkHex = 0xFFFDE8EA,
        surfaceDarkHex = 0xFF1F0E11,
        onSurfaceDarkHex = 0xFFFDE8EA,
        surfaceVariantDarkHex = 0xFF35151A,
        onSurfaceVariantDarkHex = 0xFFF7BDC3,
        outlineDarkHex = 0xFFFF5252,
        isBoldTheme = true,
        headerKasusLightHex = 0xFFFFCDD2,
        headerKasusDarkHex = 0xFF4D1017,
        glowColorLightHex = 0xFFFF1744,
        glowColorDarkHex = 0xFFFF1744,
        bannerGradientStartLightHex = 0xFFFF1744,
        bannerGradientEndLightHex = 0xFFFF5252,
        bannerGradientStartDarkHex = 0xFFFF1744,
        bannerGradientEndDarkHex = 0xFFFF5252,
        cardAccentRibbonLightHex = 0xFFFF1744,
        cardAccentRibbonDarkHex = 0xFFFF1744
    ),
    NEON_CYAN(
        id = "NeonCyan",
        germanName = "Neon-Cyan (Elektro-Aqua)",
        shortName = "Neon-Cyan",
        primaryHex = 0xFF00838F,
        secondaryHex = 0xFF00E5FF,
        backgroundHex = 0xFFF0FBFC,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF051D21,
        primaryContainerHex = 0xFFB2EBF2,
        onPrimaryContainerHex = 0xFF00272B,
        accentHex = 0xFF00E5FF,
        borderHex = 0xFFB2EBF2,
        surfaceVariantHex = 0xFFE0F7FA,
        onSurfaceVariantHex = 0xFF224E54,
        outlineHex = 0xFF00ACC1,
        swatchHex = 0xFF00E5FF,
        hasCustomPalette = true,
        primaryLightHex = 0xFF00838F,
        onPrimaryLightHex = 0xFFFFFFFF,
        primaryContainerLightHex = 0xFFB2EBF2,
        onPrimaryContainerLightHex = 0xFF00272B,
        secondaryLightHex = 0xFF00ACC1,
        onSecondaryLightHex = 0xFFFFFFFF,
        secondaryContainerLightHex = 0xFFE0F7FA,
        onSecondaryContainerLightHex = 0xFF002528,
        tertiaryLightHex = 0xFF00B0FF,
        onTertiaryLightHex = 0xFFFFFFFF,
        tertiaryContainerLightHex = 0xFFD1F1FF,
        onTertiaryContainerLightHex = 0xFF002236,
        errorLightHex = 0xFFBA1A1A,
        onErrorLightHex = 0xFFFFFFFF,
        errorContainerLightHex = 0xFFFFDAD6,
        onErrorContainerLightHex = 0xFF410002,
        backgroundLightHex = 0xFFF0FBFC,
        onBackgroundLightHex = 0xFF051D21,
        surfaceLightHex = 0xFFFFFFFF,
        onSurfaceLightHex = 0xFF051D21,
        surfaceVariantLightHex = 0xFFE0F7FA,
        onSurfaceVariantLightHex = 0xFF224E54,
        outlineLightHex = 0xFF00ACC1,
        primaryDarkHex = 0xFF00E5FF,
        onPrimaryDarkHex = 0xFF002A30,
        primaryContainerDarkHex = 0xFF004D56,
        onPrimaryContainerDarkHex = 0xFF80F2FF,
        secondaryDarkHex = 0xFF40C4FF,
        onSecondaryDarkHex = 0xFF002738,
        secondaryContainerDarkHex = 0xFF004B6E,
        onSecondaryContainerDarkHex = 0xFF82DCFF,
        tertiaryDarkHex = 0xFF18FFFF,
        onTertiaryDarkHex = 0xFF00282E,
        tertiaryContainerDarkHex = 0xFF004C57,
        onTertiaryContainerDarkHex = 0xFF80F7FF,
        errorDarkHex = 0xFFFFB4AB,
        onErrorDarkHex = 0xFF690005,
        errorContainerDarkHex = 0xFF93000A,
        onErrorContainerDarkHex = 0xFFFFDAD6,
        backgroundDarkHex = 0xFF061316,
        onBackgroundDarkHex = 0xFFDDF7FA,
        surfaceDarkHex = 0xFF0E1D21,
        onSurfaceDarkHex = 0xFFDDF7FA,
        surfaceVariantDarkHex = 0xFF172E34,
        onSurfaceVariantDarkHex = 0xFFA5E6EE,
        outlineDarkHex = 0xFF00E5FF,
        isBoldTheme = true,
        headerKasusLightHex = 0xFFB2EBF2,
        headerKasusDarkHex = 0xFF143942,
        glowColorLightHex = 0xFF00E5FF,
        glowColorDarkHex = 0xFF00E5FF,
        bannerGradientStartLightHex = 0xFF00E5FF,
        bannerGradientEndLightHex = 0xFF0091EA,
        bannerGradientStartDarkHex = 0xFF00E5FF,
        bannerGradientEndDarkHex = 0xFF0091EA,
        cardAccentRibbonLightHex = 0xFF00E5FF,
        cardAccentRibbonDarkHex = 0xFF00E5FF
    ),
    MAGENTA_PINK(
        id = "MagentaPink",
        germanName = "Magenta-Pink (Cyber Pink)",
        shortName = "Magenta-Pink",
        primaryHex = 0xFFC2185B,
        secondaryHex = 0xFFFF007F,
        backgroundHex = 0xFFFFF7FB,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF240A1A,
        primaryContainerHex = 0xFFF8BBD0,
        onPrimaryContainerHex = 0xFF3D0022,
        accentHex = 0xFFFF007F,
        borderHex = 0xFFF8BBD0,
        surfaceVariantHex = 0xFFFCE4EC,
        onSurfaceVariantHex = 0xFF5B3047,
        outlineHex = 0xFFFF007F,
        swatchHex = 0xFFFF007F,
        hasCustomPalette = true,
        primaryLightHex = 0xFFC2185B,
        onPrimaryLightHex = 0xFFFFFFFF,
        primaryContainerLightHex = 0xFFF8BBD0,
        onPrimaryContainerLightHex = 0xFF3D0022,
        secondaryLightHex = 0xFFFF007F,
        onSecondaryLightHex = 0xFFFFFFFF,
        secondaryContainerLightHex = 0xFFFCE4EC,
        onSecondaryContainerLightHex = 0xFF3E0023,
        tertiaryLightHex = 0xFFE040FB,
        onTertiaryLightHex = 0xFFFFFFFF,
        tertiaryContainerLightHex = 0xFFF8D2FF,
        onTertiaryContainerLightHex = 0xFF330040,
        errorLightHex = 0xFFBA1A1A,
        onErrorLightHex = 0xFFFFFFFF,
        errorContainerLightHex = 0xFFFFDAD6,
        onErrorContainerLightHex = 0xFF410002,
        backgroundLightHex = 0xFFFFF7FB,
        onBackgroundLightHex = 0xFF240A1A,
        surfaceLightHex = 0xFFFFFFFF,
        onSurfaceLightHex = 0xFF240A1A,
        surfaceVariantLightHex = 0xFFFCE4EC,
        onSurfaceVariantLightHex = 0xFF5B3047,
        outlineLightHex = 0xFFFF007F,
        primaryDarkHex = 0xFFFF4081,
        onPrimaryDarkHex = 0xFF3C0020,
        primaryContainerDarkHex = 0xFF6A003D,
        onPrimaryContainerDarkHex = 0xFFFFB3D0,
        secondaryDarkHex = 0xFFFF007F,
        onSecondaryDarkHex = 0xFFFFFFFF,
        secondaryContainerDarkHex = 0xFF5C002F,
        onSecondaryContainerDarkHex = 0xFFFFB0D8,
        tertiaryDarkHex = 0xFFEA80FC,
        onTertiaryDarkHex = 0xFF380045,
        tertiaryContainerDarkHex = 0xFF5A006E,
        onTertiaryContainerDarkHex = 0xFFF5B6FF,
        errorDarkHex = 0xFFFFB4AB,
        onErrorDarkHex = 0xFF690005,
        errorContainerDarkHex = 0xFF93000A,
        onErrorContainerDarkHex = 0xFFFFDAD6,
        backgroundDarkHex = 0xFF160812,
        onBackgroundDarkHex = 0xFFFDE8F3,
        surfaceDarkHex = 0xFF200E1C,
        onSurfaceDarkHex = 0xFFFDE8F3,
        surfaceVariantDarkHex = 0xFF36182E,
        onSurfaceVariantDarkHex = 0xFFF7BDDE,
        outlineDarkHex = 0xFFFF4081,
        isBoldTheme = true,
        headerKasusLightHex = 0xFFF8BBD0,
        headerKasusDarkHex = 0xFF4E163B,
        glowColorLightHex = 0xFFFF007F,
        glowColorDarkHex = 0xFFFF007F,
        bannerGradientStartLightHex = 0xFFFF007F,
        bannerGradientEndLightHex = 0xFFD500F9,
        bannerGradientStartDarkHex = 0xFFFF007F,
        bannerGradientEndDarkHex = 0xFFD500F9,
        cardAccentRibbonLightHex = 0xFFFF007F,
        cardAccentRibbonDarkHex = 0xFFFF007F
    ),
    GIFTGRUEN(
        id = "Giftgruen",
        germanName = "Giftgrün (Elektro-Lime)",
        shortName = "Giftgrün",
        primaryHex = 0xFF2E7D32,
        secondaryHex = 0xFF76FF03,
        backgroundHex = 0xFFF5FBF2,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF0A1D08,
        primaryContainerHex = 0xFFDCF8C6,
        onPrimaryContainerHex = 0xFF092A06,
        accentHex = 0xFF76FF03,
        borderHex = 0xFFDCF8C6,
        surfaceVariantHex = 0xFFEBF7E5,
        onSurfaceVariantHex = 0xFF2C4E28,
        outlineHex = 0xFF43A047,
        swatchHex = 0xFF76FF03,
        hasCustomPalette = true,
        primaryLightHex = 0xFF2E7D32,
        onPrimaryLightHex = 0xFFFFFFFF,
        primaryContainerLightHex = 0xFFDCF8C6,
        onPrimaryContainerLightHex = 0xFF092A06,
        secondaryLightHex = 0xFF43A047,
        onSecondaryLightHex = 0xFFFFFFFF,
        secondaryContainerLightHex = 0xFFEBF7E5,
        onSecondaryContainerLightHex = 0xFF092706,
        tertiaryLightHex = 0xFF00E676,
        onTertiaryLightHex = 0xFF00220D,
        tertiaryContainerLightHex = 0xFFB9F6CA,
        onTertiaryContainerLightHex = 0xFF002C12,
        errorLightHex = 0xFFBA1A1A,
        onErrorLightHex = 0xFFFFFFFF,
        errorContainerLightHex = 0xFFFFDAD6,
        onErrorContainerLightHex = 0xFF410002,
        backgroundLightHex = 0xFFF5FBF2,
        onBackgroundLightHex = 0xFF0A1D08,
        surfaceLightHex = 0xFFFFFFFF,
        onSurfaceLightHex = 0xFF0A1D08,
        surfaceVariantLightHex = 0xFFEBF7E5,
        onSurfaceVariantLightHex = 0xFF2C4E28,
        outlineLightHex = 0xFF43A047,
        primaryDarkHex = 0xFF76FF03,
        onPrimaryDarkHex = 0xFF082700,
        primaryContainerDarkHex = 0xFF175103,
        onPrimaryContainerDarkHex = 0xFFB8FF79,
        secondaryDarkHex = 0xFF69F0AE,
        onSecondaryDarkHex = 0xFF002A13,
        secondaryContainerDarkHex = 0xFF005027,
        onSecondaryContainerDarkHex = 0xFFA4FFD1,
        tertiaryDarkHex = 0xFF00E676,
        onTertiaryDarkHex = 0xFF002610,
        tertiaryContainerDarkHex = 0xFF004F24,
        onTertiaryContainerDarkHex = 0xFF96FFC8,
        errorDarkHex = 0xFFFFB4AB,
        onErrorDarkHex = 0xFF690005,
        errorContainerDarkHex = 0xFF93000A,
        onErrorContainerDarkHex = 0xFFFFDAD6,
        backgroundDarkHex = 0xFF091406,
        onBackgroundDarkHex = 0xFFE2F7DE,
        surfaceDarkHex = 0xFF11200E,
        onSurfaceDarkHex = 0xFFE2F7DE,
        surfaceVariantDarkHex = 0xFF1D321A,
        onSurfaceVariantDarkHex = 0xFFB6E6B0,
        outlineDarkHex = 0xFF76FF03,
        isBoldTheme = true,
        headerKasusLightHex = 0xFFDCF8C6,
        headerKasusDarkHex = 0xFF22421A,
        glowColorLightHex = 0xFF76FF03,
        glowColorDarkHex = 0xFF76FF03,
        bannerGradientStartLightHex = 0xFF76FF03,
        bannerGradientEndLightHex = 0xFF00E676,
        bannerGradientStartDarkHex = 0xFF76FF03,
        bannerGradientEndDarkHex = 0xFF00E676,
        cardAccentRibbonLightHex = 0xFF76FF03,
        cardAccentRibbonDarkHex = 0xFF76FF03
    ),
    KOENIGSBLAU(
        id = "Koenigsblau",
        germanName = "Königsblau (Royal Kobalt)",
        shortName = "Königsblau",
        primaryHex = 0xFF0047BA,
        secondaryHex = 0xFF0052FF,
        backgroundHex = 0xFFF5F8FF,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF0A1428,
        primaryContainerHex = 0xFFCCDFFF,
        onPrimaryContainerHex = 0xFF001742,
        accentHex = 0xFF0052FF,
        borderHex = 0xFFCCDFFF,
        surfaceVariantHex = 0xFFE3ECFF,
        onSurfaceVariantHex = 0xFF2B3A5A,
        outlineHex = 0xFF0052FF,
        swatchHex = 0xFF0052FF,
        hasCustomPalette = true,
        primaryLightHex = 0xFF0047BA,
        onPrimaryLightHex = 0xFFFFFFFF,
        primaryContainerLightHex = 0xFFCCDFFF,
        onPrimaryContainerLightHex = 0xFF001742,
        secondaryLightHex = 0xFF0052FF,
        onSecondaryLightHex = 0xFFFFFFFF,
        secondaryContainerLightHex = 0xFFE3ECFF,
        onSecondaryContainerLightHex = 0xFF001742,
        tertiaryLightHex = 0xFF2979FF,
        onTertiaryLightHex = 0xFFFFFFFF,
        tertiaryContainerLightHex = 0xFFD6E4FF,
        onTertiaryContainerLightHex = 0xFF001844,
        errorLightHex = 0xFFBA1A1A,
        onErrorLightHex = 0xFFFFFFFF,
        errorContainerLightHex = 0xFFFFDAD6,
        onErrorContainerLightHex = 0xFF410002,
        backgroundLightHex = 0xFFF5F8FF,
        onBackgroundLightHex = 0xFF0A1428,
        surfaceLightHex = 0xFFFFFFFF,
        onSurfaceLightHex = 0xFF0A1428,
        surfaceVariantLightHex = 0xFFE3ECFF,
        onSurfaceVariantLightHex = 0xFF2B3A5A,
        outlineLightHex = 0xFF0052FF,
        primaryDarkHex = 0xFF5B92FF,
        onPrimaryDarkHex = 0xFF001E54,
        primaryContainerDarkHex = 0xFF003694,
        onPrimaryContainerDarkHex = 0xFFBFD5FF,
        secondaryDarkHex = 0xFF80ABFF,
        onSecondaryDarkHex = 0xFF001E54,
        secondaryContainerDarkHex = 0xFF00328E,
        onSecondaryContainerDarkHex = 0xFFC5D9FF,
        tertiaryDarkHex = 0xFF82B1FF,
        onTertiaryDarkHex = 0xFF00225A,
        tertiaryContainerDarkHex = 0xFF003896,
        onTertiaryContainerDarkHex = 0xFFC8DCFF,
        errorDarkHex = 0xFFFFB4AB,
        onErrorDarkHex = 0xFF690005,
        errorContainerDarkHex = 0xFF93000A,
        onErrorContainerDarkHex = 0xFFFFDAD6,
        backgroundDarkHex = 0xFF080D18,
        onBackgroundDarkHex = 0xFFE4ECFB,
        surfaceDarkHex = 0xFF10192A,
        onSurfaceDarkHex = 0xFFE4ECFB,
        surfaceVariantDarkHex = 0xFF1B2842,
        onSurfaceVariantDarkHex = 0xFFBACEEF,
        outlineDarkHex = 0xFF5B92FF,
        isBoldTheme = true,
        headerKasusLightHex = 0xFFCCDFFF,
        headerKasusDarkHex = 0xFF182E55,
        glowColorLightHex = 0xFF0052FF,
        glowColorDarkHex = 0xFF0052FF,
        bannerGradientStartLightHex = 0xFF0052FF,
        bannerGradientEndLightHex = 0xFF2979FF,
        bannerGradientStartDarkHex = 0xFF0052FF,
        bannerGradientEndDarkHex = 0xFF2979FF,
        cardAccentRibbonLightHex = 0xFF0052FF,
        cardAccentRibbonDarkHex = 0xFF0052FF
    ),
    SONNEN_ORANGE(
        id = "SonnenOrange",
        germanName = "Sonniges Orange (Solar Orange)",
        shortName = "Sonnen-Orange",
        primaryHex = 0xFFE65100,
        secondaryHex = 0xFFFF6D00,
        backgroundHex = 0xFFFFFBF7,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF241004,
        primaryContainerHex = 0xFFFFCCBC,
        onPrimaryContainerHex = 0xFF3D1100,
        accentHex = 0xFFFF6D00,
        borderHex = 0xFFFFCCBC,
        surfaceVariantHex = 0xFFFFE0B2,
        onSurfaceVariantHex = 0xFF5C3923,
        outlineHex = 0xFFFF6D00,
        swatchHex = 0xFFFF6D00,
        hasCustomPalette = true,
        primaryLightHex = 0xFFE65100,
        onPrimaryLightHex = 0xFFFFFFFF,
        primaryContainerLightHex = 0xFFFFCCBC,
        onPrimaryContainerLightHex = 0xFF3D1100,
        secondaryLightHex = 0xFFFF6D00,
        onSecondaryLightHex = 0xFFFFFFFF,
        secondaryContainerLightHex = 0xFFFFE0B2,
        onSecondaryContainerLightHex = 0xFF3B1000,
        tertiaryLightHex = 0xFFFF9100,
        onTertiaryLightHex = 0xFF2E0F00,
        tertiaryContainerLightHex = 0xFFFFE8D1,
        onTertiaryContainerLightHex = 0xFF381000,
        errorLightHex = 0xFFBA1A1A,
        onErrorLightHex = 0xFFFFFFFF,
        errorContainerLightHex = 0xFFFFDAD6,
        onErrorContainerLightHex = 0xFF410002,
        backgroundLightHex = 0xFFFFFBF7,
        onBackgroundLightHex = 0xFF241004,
        surfaceLightHex = 0xFFFFFFFF,
        onSurfaceLightHex = 0xFF241004,
        surfaceVariantLightHex = 0xFFFFE0B2,
        onSurfaceVariantLightHex = 0xFF5C3923,
        outlineLightHex = 0xFFFF6D00,
        primaryDarkHex = 0xFFFF9E40,
        onPrimaryDarkHex = 0xFF3E1200,
        primaryContainerDarkHex = 0xFF6A2400,
        onPrimaryContainerDarkHex = 0xFFFFD3B0,
        secondaryDarkHex = 0xFFFF8F00,
        onSecondaryDarkHex = 0xFF381200,
        secondaryContainerDarkHex = 0xFF622200,
        onSecondaryContainerDarkHex = 0xFFFFCC99,
        tertiaryDarkHex = 0xFFFFB74D,
        onTertiaryDarkHex = 0xFF3A1300,
        tertiaryContainerDarkHex = 0xFF5E2200,
        onTertiaryContainerDarkHex = 0xFFFFDCAD,
        errorDarkHex = 0xFFFFB4AB,
        onErrorDarkHex = 0xFF690005,
        errorContainerDarkHex = 0xFF93000A,
        onErrorContainerDarkHex = 0xFFFFDAD6,
        backgroundDarkHex = 0xFF160B04,
        onBackgroundDarkHex = 0xFFFEECE0,
        surfaceDarkHex = 0xFF211309,
        onSurfaceDarkHex = 0xFFFEECE0,
        surfaceVariantDarkHex = 0xFF362013,
        onSurfaceVariantDarkHex = 0xFFF5C5A7,
        outlineDarkHex = 0xFFFF9E40,
        isBoldTheme = true,
        headerKasusLightHex = 0xFFFFCCBC,
        headerKasusDarkHex = 0xFF4E1D06,
        glowColorLightHex = 0xFFFF6D00,
        glowColorDarkHex = 0xFFFF6D00,
        bannerGradientStartLightHex = 0xFFFF6D00,
        bannerGradientEndLightHex = 0xFFFF9100,
        bannerGradientStartDarkHex = 0xFFFF6D00,
        bannerGradientEndDarkHex = 0xFFFF9100,
        cardAccentRibbonLightHex = 0xFFFF6D00,
        cardAccentRibbonDarkHex = 0xFFFF6D00
    ),
    ELEKTRO_VIOLETT(
        id = "ElektroViolett",
        germanName = "Elektro-Violett (Cyber Violet)",
        shortName = "Elektro-Violett",
        primaryHex = 0xFF6D28D9,
        secondaryHex = 0xFF7C3AED,
        backgroundHex = 0xFFFAF8FF,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF170929,
        primaryContainerHex = 0xFFDDD6FE,
        onPrimaryContainerHex = 0xFF260555,
        accentHex = 0xFF7C3AED,
        borderHex = 0xFFDDD6FE,
        surfaceVariantHex = 0xFFEDE9FE,
        onSurfaceVariantHex = 0xFF473366,
        outlineHex = 0xFF7C3AED,
        swatchHex = 0xFF7C3AED,
        hasCustomPalette = true,
        primaryLightHex = 0xFF6D28D9,
        onPrimaryLightHex = 0xFFFFFFFF,
        primaryContainerLightHex = 0xFFDDD6FE,
        onPrimaryContainerLightHex = 0xFF260555,
        secondaryLightHex = 0xFF7C3AED,
        onSecondaryLightHex = 0xFFFFFFFF,
        secondaryContainerLightHex = 0xFFEDE9FE,
        onSecondaryContainerLightHex = 0xFF260555,
        tertiaryLightHex = 0xFFC026D3,
        onTertiaryLightHex = 0xFFFFFFFF,
        tertiaryContainerLightHex = 0xFFF5D0FE,
        onTertiaryContainerLightHex = 0xFF380045,
        errorLightHex = 0xFFBA1A1A,
        onErrorLightHex = 0xFFFFFFFF,
        errorContainerLightHex = 0xFFFFDAD6,
        onErrorContainerLightHex = 0xFF410002,
        backgroundLightHex = 0xFFFAF8FF,
        onBackgroundLightHex = 0xFF170929,
        surfaceLightHex = 0xFFFFFFFF,
        onSurfaceLightHex = 0xFF170929,
        surfaceVariantLightHex = 0xFFEDE9FE,
        onSurfaceVariantLightHex = 0xFF473366,
        outlineLightHex = 0xFF7C3AED,
        primaryDarkHex = 0xFFA78BFA,
        onPrimaryDarkHex = 0xFF240058,
        primaryContainerDarkHex = 0xFF4C1D95,
        onPrimaryContainerDarkHex = 0xFFDDD6FE,
        secondaryDarkHex = 0xFFC4B5FD,
        onSecondaryDarkHex = 0xFF200050,
        secondaryContainerDarkHex = 0xFF47178F,
        onSecondaryContainerDarkHex = 0xFFDDD6FE,
        tertiaryDarkHex = 0xFFF0ABFC,
        onTertiaryDarkHex = 0xFF330040,
        tertiaryContainerDarkHex = 0xFF5A006E,
        onTertiaryContainerDarkHex = 0xFFF5D0FE,
        errorDarkHex = 0xFFFFB4AB,
        onErrorDarkHex = 0xFF690005,
        errorContainerDarkHex = 0xFF93000A,
        onErrorContainerDarkHex = 0xFFFFDAD6,
        backgroundDarkHex = 0xFF0E071A,
        onBackgroundDarkHex = 0xFFEFE8FC,
        surfaceDarkHex = 0xFF180F2B,
        onSurfaceDarkHex = 0xFFEFE8FC,
        surfaceVariantDarkHex = 0xFF291A44,
        onSurfaceVariantDarkHex = 0xFFD5C7F2,
        outlineDarkHex = 0xFFA78BFA,
        isBoldTheme = true,
        headerKasusLightHex = 0xFFDDD6FE,
        headerKasusDarkHex = 0xFF3A1C68,
        glowColorLightHex = 0xFF7C3AED,
        glowColorDarkHex = 0xFF7C3AED,
        bannerGradientStartLightHex = 0xFF7C3AED,
        bannerGradientEndLightHex = 0xFFC026D3,
        bannerGradientStartDarkHex = 0xFF7C3AED,
        bannerGradientEndDarkHex = 0xFFC026D3,
        cardAccentRibbonLightHex = 0xFF7C3AED,
        cardAccentRibbonDarkHex = 0xFF7C3AED
    ),
    LEUCHT_TUERKIS(
        id = "LeuchtTuerkis",
        germanName = "Leuchtendes Türkis (Mint Neon)",
        shortName = "Leucht-Türkis",
        primaryHex = 0xFF00897B,
        secondaryHex = 0xFF00F5D4,
        backgroundHex = 0xFFF0FDFB,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF041E1A,
        primaryContainerHex = 0xFFB2DFDB,
        onPrimaryContainerHex = 0xFF002922,
        accentHex = 0xFF00F5D4,
        borderHex = 0xFFB2DFDB,
        surfaceVariantHex = 0xFFE0F2F1,
        onSurfaceVariantHex = 0xFF214E47,
        outlineHex = 0xFF00BFA5,
        swatchHex = 0xFF00F5D4,
        hasCustomPalette = true,
        primaryLightHex = 0xFF00897B,
        onPrimaryLightHex = 0xFFFFFFFF,
        primaryContainerLightHex = 0xFFB2DFDB,
        onPrimaryContainerLightHex = 0xFF002922,
        secondaryLightHex = 0xFF00BFA5,
        onSecondaryLightHex = 0xFF041E1A,
        secondaryContainerLightHex = 0xFFE0F2F1,
        onSecondaryContainerLightHex = 0xFF002620,
        tertiaryLightHex = 0xFF00BBF9,
        onTertiaryLightHex = 0xFFFFFFFF,
        tertiaryContainerLightHex = 0xFFD0F4FF,
        onTertiaryContainerLightHex = 0xFF002334,
        errorLightHex = 0xFFBA1A1A,
        onErrorLightHex = 0xFFFFFFFF,
        errorContainerLightHex = 0xFFFFDAD6,
        onErrorContainerLightHex = 0xFF410002,
        backgroundLightHex = 0xFFF0FDFB,
        onBackgroundLightHex = 0xFF041E1A,
        surfaceLightHex = 0xFFFFFFFF,
        onSurfaceLightHex = 0xFF041E1A,
        surfaceVariantLightHex = 0xFFE0F2F1,
        onSurfaceVariantLightHex = 0xFF214E47,
        outlineLightHex = 0xFF00BFA5,
        primaryDarkHex = 0xFF00F5D4,
        onPrimaryDarkHex = 0xFF002A24,
        primaryContainerDarkHex = 0xFF004D41,
        onPrimaryContainerDarkHex = 0xFF7DFFE9,
        secondaryDarkHex = 0xFF64FFDA,
        onSecondaryDarkHex = 0xFF002A22,
        secondaryContainerDarkHex = 0xFF004E3F,
        onSecondaryContainerDarkHex = 0xFFA3FFEC,
        tertiaryDarkHex = 0xFF00BBF9,
        onTertiaryDarkHex = 0xFF002235,
        tertiaryContainerDarkHex = 0xFF004464,
        onTertiaryContainerDarkHex = 0xFF8FE2FF,
        errorDarkHex = 0xFFFFB4AB,
        onErrorDarkHex = 0xFF690005,
        errorContainerDarkHex = 0xFF93000A,
        onErrorContainerDarkHex = 0xFFFFDAD6,
        backgroundDarkHex = 0xFF051412,
        onBackgroundDarkHex = 0xFFDEF8F4,
        surfaceDarkHex = 0xFF0C201C,
        onSurfaceDarkHex = 0xFFDEF8F4,
        surfaceVariantDarkHex = 0xFF15332D,
        onSurfaceVariantDarkHex = 0xFFA3EBE0,
        outlineDarkHex = 0xFF00F5D4,
        isBoldTheme = true,
        headerKasusLightHex = 0xFFB2DFDB,
        headerKasusDarkHex = 0xFF133B34,
        glowColorLightHex = 0xFF00F5D4,
        glowColorDarkHex = 0xFF00F5D4,
        bannerGradientStartLightHex = 0xFF00F5D4,
        bannerGradientEndLightHex = 0xFF00BBF9,
        bannerGradientStartDarkHex = 0xFF00F5D4,
        bannerGradientEndDarkHex = 0xFF00BBF9,
        cardAccentRibbonLightHex = 0xFF00F5D4,
        cardAccentRibbonDarkHex = 0xFF00F5D4
    ),
    SATTES_BERNSTEIN(
        id = "SattesBernstein",
        germanName = "Sattes Bernstein (Goldener Bernstein)",
        shortName = "Bernstein",
        primaryHex = 0xFFB26A00,
        secondaryHex = 0xFFFF9100,
        backgroundHex = 0xFFFFFDF8,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF241804,
        primaryContainerHex = 0xFFFFE082,
        onPrimaryContainerHex = 0xFF382000,
        accentHex = 0xFFFF9100,
        borderHex = 0xFFFFE082,
        surfaceVariantHex = 0xFFFFF3E0,
        onSurfaceVariantHex = 0xFF5C4325,
        outlineHex = 0xFFFF9100,
        swatchHex = 0xFFFF9100,
        hasCustomPalette = true,
        primaryLightHex = 0xFFB26A00,
        onPrimaryLightHex = 0xFFFFFFFF,
        primaryContainerLightHex = 0xFFFFE082,
        onPrimaryContainerLightHex = 0xFF382000,
        secondaryLightHex = 0xFFFF9100,
        onSecondaryLightHex = 0xFF241804,
        secondaryContainerLightHex = 0xFFFFF3E0,
        onSecondaryContainerLightHex = 0xFF3A2200,
        tertiaryLightHex = 0xFFFFAB00,
        onTertiaryLightHex = 0xFF261600,
        tertiaryContainerLightHex = 0xFFFFECB3,
        onTertiaryContainerLightHex = 0xFF301C00,
        errorLightHex = 0xFFBA1A1A,
        onErrorLightHex = 0xFFFFFFFF,
        errorContainerLightHex = 0xFFFFDAD6,
        onErrorContainerLightHex = 0xFF410002,
        backgroundLightHex = 0xFFFFFDF8,
        onBackgroundLightHex = 0xFF241804,
        surfaceLightHex = 0xFFFFFFFF,
        onSurfaceLightHex = 0xFF241804,
        surfaceVariantLightHex = 0xFFFFF3E0,
        onSurfaceVariantLightHex = 0xFF5C4325,
        outlineLightHex = 0xFFFF9100,
        primaryDarkHex = 0xFFFFB74D,
        onPrimaryDarkHex = 0xFF3A1F00,
        primaryContainerDarkHex = 0xFF653B00,
        onPrimaryContainerDarkHex = 0xFFFFDE9E,
        secondaryDarkHex = 0xFFFF9100,
        onSecondaryDarkHex = 0xFF331B00,
        secondaryContainerDarkHex = 0xFF5D3300,
        onSecondaryContainerDarkHex = 0xFFFFD394,
        tertiaryDarkHex = 0xFFFFD54F,
        onTertiaryDarkHex = 0xFF352000,
        tertiaryContainerDarkHex = 0xFF593B00,
        onTertiaryContainerDarkHex = 0xFFFFE79E,
        errorDarkHex = 0xFFFFB4AB,
        onErrorDarkHex = 0xFF690005,
        errorContainerDarkHex = 0xFF93000A,
        onErrorContainerDarkHex = 0xFFFFDAD6,
        backgroundDarkHex = 0xFF151006,
        onBackgroundDarkHex = 0xFFFDF0DC,
        surfaceDarkHex = 0xFF20180B,
        onSurfaceDarkHex = 0xFFFDF0DC,
        surfaceVariantDarkHex = 0xFF342614,
        onSurfaceVariantDarkHex = 0xFFF4D4AA,
        outlineDarkHex = 0xFFFFB74D,
        isBoldTheme = true,
        headerKasusLightHex = 0xFFFFE082,
        headerKasusDarkHex = 0xFF462A08,
        glowColorLightHex = 0xFFFF9100,
        glowColorDarkHex = 0xFFFF9100,
        bannerGradientStartLightHex = 0xFFFF9100,
        bannerGradientEndLightHex = 0xFFFFD600,
        bannerGradientStartDarkHex = 0xFFFF9100,
        bannerGradientEndDarkHex = 0xFFFFD600,
        cardAccentRibbonLightHex = 0xFFFF9100,
        cardAccentRibbonDarkHex = 0xFFFF9100
    ),
    // =========================================================================
    // 1. Rotbraun & Terracotta (Warm Earth)
    // =========================================================================
    TERRACOTTA(
        id = "Terracotta",
        germanName = "Terracotta (Warmer Ton)",
        shortName = "Terracotta",
        primaryHex = 0xFFB85D38,
        secondaryHex = 0xFF9E5234,
        backgroundHex = 0xFFFFF8F5,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF2D1B15,
        primaryContainerHex = 0xFFFFDBCF,
        onPrimaryContainerHex = 0xFF3B1205,
        accentHex = 0xFFD27D56,
        borderHex = 0xFFF5D4C7,
        surfaceVariantHex = 0xFFFDF0E9,
        onSurfaceVariantHex = 0xFF5B433B,
        outlineHex = 0xFFB87A64,
        swatchHex = 0xFFD27D56 // #D27D56
    ),
    ZIEGEL(
        id = "Ziegel",
        germanName = "Ziegel (Backsteinrot)",
        shortName = "Ziegel",
        primaryHex = 0xFFA0482B,
        secondaryHex = 0xFF8A3E24,
        backgroundHex = 0xFFFFF6F3,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF2A1610,
        primaryContainerHex = 0xFFFFDACF,
        onPrimaryContainerHex = 0xFF3B0F04,
        accentHex = 0xFFB85A3A,
        borderHex = 0xFFF3C7BC,
        surfaceVariantHex = 0xFFFCECE7,
        onSurfaceVariantHex = 0xFF553D37,
        outlineHex = 0xFFA56654,
        swatchHex = 0xFFB85A3A // #B85A3A
    ),
    ROST(
        id = "Rost",
        germanName = "Rost (Echtes Rostrot)",
        shortName = "Rost",
        primaryHex = 0xFF933B1E,
        secondaryHex = 0xFF7D3219,
        backgroundHex = 0xFFFFF5F1,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF28130B,
        primaryContainerHex = 0xFFFFD7C8,
        onPrimaryContainerHex = 0xFF380A00,
        accentHex = 0xFFA84A2A,
        borderHex = 0xFFEEC0AF,
        surfaceVariantHex = 0xFFF9E8E2,
        onSurfaceVariantHex = 0xFF543830,
        outlineHex = 0xFF9C5A48,
        swatchHex = 0xFFA84A2A // #A84A2A
    ),
    KUPFER(
        id = "Kupfer",
        germanName = "Kupfer (Warmes Metallbraun)",
        shortName = "Kupfer",
        primaryHex = 0xFFA16639,
        secondaryHex = 0xFF89552E,
        backgroundHex = 0xFFFFF8F3,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF291B11,
        primaryContainerHex = 0xFFFFDCC4,
        onPrimaryContainerHex = 0xFF341703,
        accentHex = 0xFFB87A4A,
        borderHex = 0xFFF3CFB6,
        surfaceVariantHex = 0xFFFDF0E5,
        onSurfaceVariantHex = 0xFF564336,
        outlineHex = 0xFFA3785A,
        swatchHex = 0xFFB87A4A // #B87A4A
    ),
    MAHAGONI(
        id = "Mahagoni",
        germanName = "Mahagoni (Edles Dunkelrotbraun)",
        shortName = "Mahagoni",
        primaryHex = 0xFF5E2F20,
        secondaryHex = 0xFF4F271A,
        backgroundHex = 0xFFFDF5F3,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF22110C,
        primaryContainerHex = 0xFFE2BDB2,
        onPrimaryContainerHex = 0xFF2E1007,
        accentHex = 0xFF6A3A2A,
        borderHex = 0xFFD2AAA0,
        surfaceVariantHex = 0xFFF2E6E2,
        onSurfaceVariantHex = 0xFF4B3934,
        outlineHex = 0xFF7F554B,
        swatchHex = 0xFF6A3A2A // #6A3A2A
    ),
    ZIMT(
        id = "Zimt",
        germanName = "Zimt (Würziges Braun)",
        shortName = "Zimt",
        primaryHex = 0xFF8A4E30,
        secondaryHex = 0xFFA36545,
        backgroundHex = 0xFFFAF6F4,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF261914,
        primaryContainerHex = 0xFFF7D9CC,
        onPrimaryContainerHex = 0xFF35170B,
        accentHex = 0xFFA86A4A,
        borderHex = 0xFFEACAB9,
        surfaceVariantHex = 0xFFF6ECE6,
        onSurfaceVariantHex = 0xFF53433C,
        outlineHex = 0xFF8D7166,
        swatchHex = 0xFFA86A4A // #A86A4A
    ),
    SIENA(
        id = "Siena",
        germanName = "Siena (Gebrannte Erde)",
        shortName = "Siena",
        primaryHex = 0xFF9E5224,
        secondaryHex = 0xFFB86A3A,
        backgroundHex = 0xFFFFF7F2,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF2A160A,
        primaryContainerHex = 0xFFFFD7BF,
        onPrimaryContainerHex = 0xFF351200,
        accentHex = 0xFFB86A3A,
        borderHex = 0xFFF2C8B0,
        surfaceVariantHex = 0xFFFCECE2,
        onSurfaceVariantHex = 0xFF563D30,
        outlineHex = 0xFFA46E53,
        swatchHex = 0xFFB86A3A // #B86A3A
    ),
    TON(
        id = "Ton",
        germanName = "Ton (Gebrannter Lehmton)",
        shortName = "Ton",
        primaryHex = 0xFFA86244,
        secondaryHex = 0xFF905238,
        backgroundHex = 0xFFFFF7F4,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF2B1913,
        primaryContainerHex = 0xFFFFDCCF,
        onPrimaryContainerHex = 0xFF36150B,
        accentHex = 0xFFC47A5A,
        borderHex = 0xFFF4CEBF,
        surfaceVariantHex = 0xFFFDEEE7,
        onSurfaceVariantHex = 0xFF574139,
        outlineHex = 0xFFA87766,
        swatchHex = 0xFFC47A5A // #C47A5A
    ),

    // =========================================================================
    // 2. Sand & Beige (Neutral Warm)
    // =========================================================================
    SAND(
        id = "Sand",
        germanName = "Sand (Warmer Wüstensand)",
        shortName = "Sand",
        primaryHex = 0xFF8C714C,
        secondaryHex = 0xFFA58A65,
        backgroundHex = 0xFFFAF8F5,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF251F17,
        primaryContainerHex = 0xFFF3E7D7,
        onPrimaryContainerHex = 0xFF2A2012,
        accentHex = 0xFFC4A882,
        borderHex = 0xFFE5D8C7,
        surfaceVariantHex = 0xFFF5F0E8,
        onSurfaceVariantHex = 0xFF50463A,
        outlineHex = 0xFF867868,
        swatchHex = 0xFFC4A882 // #C4A882
    ),
    DUENE(
        id = "Düne",
        germanName = "Düne (Sanftes Dünenbeige)",
        shortName = "Düne",
        primaryHex = 0xFF877663,
        secondaryHex = 0xFF9E8D79,
        backgroundHex = 0xFFFBF9F7,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF24201B,
        primaryContainerHex = 0xFFEFE9E1,
        onPrimaryContainerHex = 0xFF251E17,
        accentHex = 0xFFC4B09A,
        borderHex = 0xFFE2DAD1,
        surfaceVariantHex = 0xFFF5F1EC,
        onSurfaceVariantHex = 0xFF4C4640,
        outlineHex = 0xFF7F766E,
        swatchHex = 0xFFC4B09A // #C4B09A
    ),
    CREME(
        id = "Creme",
        germanName = "Creme (Helles Warmbeige)",
        shortName = "Creme",
        primaryHex = 0xFF948567,
        secondaryHex = 0xFF7D7054,
        backgroundHex = 0xFFFCFAF5,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF24211A,
        primaryContainerHex = 0xFFF5EEDD,
        onPrimaryContainerHex = 0xFF2B2516,
        accentHex = 0xFFD4C4A4,
        borderHex = 0xFFE8E0CE,
        surfaceVariantHex = 0xFFF8F4EB,
        onSurfaceVariantHex = 0xFF4E4739,
        outlineHex = 0xFF867D6C,
        swatchHex = 0xFFD4C4A4 // #D4C4A4
    ),
    NATUR(
        id = "Natur",
        germanName = "Natur (Leinen & Rohfaser)",
        shortName = "Natur",
        primaryHex = 0xFF807156,
        secondaryHex = 0xFF6D6047,
        backgroundHex = 0xFFFAF8F5,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF221E17,
        primaryContainerHex = 0xFFEAE2D2,
        onPrimaryContainerHex = 0xFF262013,
        accentHex = 0xFFB8A88A,
        borderHex = 0xFFDDD4C2,
        surfaceVariantHex = 0xFFF4F0E8,
        onSurfaceVariantHex = 0xFF4A4437,
        outlineHex = 0xFF7B7262,
        swatchHex = 0xFFB8A88A // #B8A88A
    ),
    KIES(
        id = "Kies",
        germanName = "Kies (Warmgrauer Kieselton)",
        shortName = "Kies",
        primaryHex = 0xFF73654B,
        secondaryHex = 0xFF60543C,
        backgroundHex = 0xFFF9F7F4,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF211D15,
        primaryContainerHex = 0xFFE2D9C7,
        onPrimaryContainerHex = 0xFF221A0C,
        accentHex = 0xFFA8987A,
        borderHex = 0xFFD4CABA,
        surfaceVariantHex = 0xFFF2ECE0,
        onSurfaceVariantHex = 0xFF484234,
        outlineHex = 0xFF766E5E,
        swatchHex = 0xFFA8987A // #A8987A
    ),
    LEHM(
        id = "Lehm",
        germanName = "Lehm (Neutraler Ton)",
        shortName = "Lehm",
        primaryHex = 0xFF965C3E,
        secondaryHex = 0xFF7D4E35,
        backgroundHex = 0xFFFAF6F3,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF261912,
        primaryContainerHex = 0xFFF7DECE,
        onPrimaryContainerHex = 0xFF32190B,
        accentHex = 0xFFB87A5A,
        borderHex = 0xFFEAD2C3,
        surfaceVariantHex = 0xFFF5EDE7,
        onSurfaceVariantHex = 0xFF52443C,
        outlineHex = 0xFF8E7364,
        swatchHex = 0xFFB87A5A // #B87A5A
    ),
    OCKER(
        id = "Ocker",
        germanName = "Ocker (Warme Goldenerde)",
        shortName = "Ocker",
        primaryHex = 0xFFA37B3E,
        secondaryHex = 0xFF896732,
        backgroundHex = 0xFFFCF9F3,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF282012,
        primaryContainerHex = 0xFFF9E8CE,
        onPrimaryContainerHex = 0xFF322307,
        accentHex = 0xFFC49A5A,
        borderHex = 0xFFEEDBBF,
        surfaceVariantHex = 0xFFF9F2E7,
        onSurfaceVariantHex = 0xFF544733,
        outlineHex = 0xFF9B825B,
        swatchHex = 0xFFC49A5A // #C49A5A
    ),
    GELBBRAUN(
        id = "Gelbbraun",
        germanName = "Gelbbraun (Erdenbernstein)",
        shortName = "Gelbbraun",
        primaryHex = 0xFF966C33,
        secondaryHex = 0xFF7F5B29,
        backgroundHex = 0xFFFAF7F2,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF261D10,
        primaryContainerHex = 0xFFF3DFBE,
        onPrimaryContainerHex = 0xFF2F1F05,
        accentHex = 0xFFB88A4A,
        borderHex = 0xFFE6D1AF,
        surfaceVariantHex = 0xFFF6EFE4,
        onSurfaceVariantHex = 0xFF504432,
        outlineHex = 0xFF907751,
        swatchHex = 0xFFB88A4A // #B88A4A
    ),

    // =========================================================================
    // 3. Grau & Schiefer (Cool Earth)
    // =========================================================================
    SCHIEFER(
        id = "Schiefer",
        germanName = "Schiefer (Naturgrau)",
        shortName = "Schiefer",
        primaryHex = 0xFF4B5B5B,
        secondaryHex = 0xFF607272,
        backgroundHex = 0xFFF5F7F7,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF151D1D,
        primaryContainerHex = 0xFFD2E0E0,
        onPrimaryContainerHex = 0xFF101C1C,
        accentHex = 0xFF6A7A7A,
        borderHex = 0xFFC2D4D4,
        surfaceVariantHex = 0xFFEAF0F0,
        onSurfaceVariantHex = 0xFF3D4848,
        outlineHex = 0xFF667575,
        swatchHex = 0xFF6A7A7A // #6A7A7A (New Default)
    ),
    KIESEL(
        id = "Kiesel",
        germanName = "Kiesel (Flussstein)",
        shortName = "Kiesel",
        primaryHex = 0xFF716355,
        secondaryHex = 0xFF87796B,
        backgroundHex = 0xFFF8F7F6,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF221E19,
        primaryContainerHex = 0xFFE6DDD4,
        onPrimaryContainerHex = 0xFF231D16,
        accentHex = 0xFF9A8A7A,
        borderHex = 0xFFD8CCC1,
        surfaceVariantHex = 0xFFF1EDE9,
        onSurfaceVariantHex = 0xFF4A433D,
        outlineHex = 0xFF7C7268,
        swatchHex = 0xFF9A8A7A // #9A8A7A
    ),
    GRANIT(
        id = "Granit",
        germanName = "Granit (Neutrales Steingrau)",
        shortName = "Granit",
        primaryHex = 0xFF585858,
        secondaryHex = 0xFF6D6D6D,
        backgroundHex = 0xFFF6F6F6,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF1A1A1A,
        primaryContainerHex = 0xFFD7D7D7,
        onPrimaryContainerHex = 0xFF181818,
        accentHex = 0xFF7A7A7A,
        borderHex = 0xFFCCCCCC,
        surfaceVariantHex = 0xFFEDEDED,
        onSurfaceVariantHex = 0xFF424242,
        outlineHex = 0xFF707070,
        swatchHex = 0xFF7A7A7A // #7A7A7A
    ),
    STEIN(
        id = "Stein",
        germanName = "Stein (Kühler Naturstein)",
        shortName = "Stein",
        primaryHex = 0xFF656558,
        secondaryHex = 0xFF78786B,
        backgroundHex = 0xFFF7F7F5,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF1E1E19,
        primaryContainerHex = 0xFFDFDFD4,
        onPrimaryContainerHex = 0xFF1C1C15,
        accentHex = 0xFF8A8A7A,
        borderHex = 0xFFD2D2C4,
        surfaceVariantHex = 0xFFEFEFEA,
        onSurfaceVariantHex = 0xFF45453C,
        outlineHex = 0xFF757568,
        swatchHex = 0xFF8A8A7A // #8A8A7A
    ),
    ASCHE(
        id = "Asche",
        germanName = "Asche (Sanftes Aschgrau)",
        shortName = "Asche",
        primaryHex = 0xFF737365,
        secondaryHex = 0xFF878778,
        backgroundHex = 0xFFF8F8F6,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF21211C,
        primaryContainerHex = 0xFFE7E7DE,
        onPrimaryContainerHex = 0xFF21211A,
        accentHex = 0xFF9A9A8A,
        borderHex = 0xFFD8D8CC,
        surfaceVariantHex = 0xFFF2F2EC,
        onSurfaceVariantHex = 0xFF48483F,
        outlineHex = 0xFF7B7B6E,
        swatchHex = 0xFF9A9A8A // #9A9A8A
    ),
    MAUVE(
        id = "Mauve",
        germanName = "Mauve (Grauviolette Erde)",
        shortName = "Mauve",
        primaryHex = 0xFF675858,
        secondaryHex = 0xFF7B6B6B,
        backgroundHex = 0xFFF7F5F5,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF1F1A1A,
        primaryContainerHex = 0xFFDFD5D5,
        onPrimaryContainerHex = 0xFF201818,
        accentHex = 0xFF8A7A7A,
        borderHex = 0xFFD4C7C7,
        surfaceVariantHex = 0xFFEFEAEA,
        onSurfaceVariantHex = 0xFF473F3F,
        outlineHex = 0xFF786A6A,
        swatchHex = 0xFF8A7A7A // #8A7A7A
    ),
    TAUPE(
        id = "Taupe",
        germanName = "Taupe (Warmes Graubraun)",
        shortName = "Taupe",
        primaryHex = 0xFF5D4E41,
        secondaryHex = 0xFF726153,
        backgroundHex = 0xFFF7F5F3,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF1E1A16,
        primaryContainerHex = 0xFFD9CCC0,
        onPrimaryContainerHex = 0xFF20170F,
        accentHex = 0xFF7A6A5A,
        borderHex = 0xFFC9BBAE,
        surfaceVariantHex = 0xFFEFEBE7,
        onSurfaceVariantHex = 0xFF453D36,
        outlineHex = 0xFF706154,
        swatchHex = 0xFF7A6A5A // #7A6A5A
    ),
    GRAPHIT(
        id = "Graphit",
        germanName = "Graphit (Tiefes Schiefergrau)",
        shortName = "Graphit",
        primaryHex = 0xFF424242,
        secondaryHex = 0xFF545454,
        backgroundHex = 0xFFF5F5F5,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF141414,
        primaryContainerHex = 0xFFC7C7C7,
        onPrimaryContainerHex = 0xFF121212,
        accentHex = 0xFF5A5A5A,
        borderHex = 0xFFB8B8B8,
        surfaceVariantHex = 0xFFEBEBEB,
        onSurfaceVariantHex = 0xFF3D3D3D,
        outlineHex = 0xFF636363,
        swatchHex = 0xFF5A5A5A // #5A5A5A
    ),

    // =========================================================================
    // 4. Braun & Umbra (Deep Earth)
    // =========================================================================
    UMBRA(
        id = "Umbra",
        germanName = "Umbra (Erdiges Umbrabraun)",
        shortName = "Umbra",
        primaryHex = 0xFF524429,
        secondaryHex = 0xFF675638,
        backgroundHex = 0xFFF7F6F3,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF1D180F,
        primaryContainerHex = 0xFFD8CCB5,
        onPrimaryContainerHex = 0xFF1E1608,
        accentHex = 0xFF6A5A3A,
        borderHex = 0xFFC7B99E,
        surfaceVariantHex = 0xFFEEEAE2,
        onSurfaceVariantHex = 0xFF443D30,
        outlineHex = 0xFF6D6049,
        swatchHex = 0xFF6A5A3A // #6A5A3A
    ),
    TERRA(
        id = "Terra",
        germanName = "Terra (Warmer Erdboden)",
        shortName = "Terra",
        primaryHex = 0xFF705235,
        secondaryHex = 0xFF896746,
        backgroundHex = 0xFFF9F6F4,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF231A12,
        primaryContainerHex = 0xFFE7D4C2,
        onPrimaryContainerHex = 0xFF28190B,
        accentHex = 0xFF8A6A4A,
        borderHex = 0xFFD6C0AB,
        surfaceVariantHex = 0xFFF2ECE6,
        onSurfaceVariantHex = 0xFF4C4137,
        outlineHex = 0xFF7F6A54,
        swatchHex = 0xFF8A6A4A // #8A6A4A
    ),
    ZEDER(
        id = "Zeder",
        germanName = "Zeder (Dunkles Zedernholz)",
        shortName = "Zeder",
        primaryHex = 0xFF463728,
        secondaryHex = 0xFF5A4938,
        backgroundHex = 0xFFF7F5F3,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF1D1712,
        primaryContainerHex = 0xFFD9CCBF,
        onPrimaryContainerHex = 0xFF1D130B,
        accentHex = 0xFF5A4A3A,
        borderHex = 0xFFC9B9A9,
        surfaceVariantHex = 0xFFEFECE8,
        onSurfaceVariantHex = 0xFF443D36,
        outlineHex = 0xFF6C6156,
        swatchHex = 0xFF5A4A3A // #5A4A3A
    ),
    KAKAO(
        id = "Kakao",
        germanName = "Kakao (Sattes Kakaobraun)",
        shortName = "Kakao",
        primaryHex = 0xFF634629,
        secondaryHex = 0xFF7A5937,
        backgroundHex = 0xFFF8F5F2,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF21170F,
        primaryContainerHex = 0xFFDFCDBB,
        onPrimaryContainerHex = 0xFF241407,
        accentHex = 0xFF7A5A3A,
        borderHex = 0xFFD1BBA5,
        surfaceVariantHex = 0xFFF1EBE5,
        onSurfaceVariantHex = 0xFF483D33,
        outlineHex = 0xFF755E49,
        swatchHex = 0xFF7A5A3A // #7A5A3A
    ),
    MOKKA(
        id = "Mokka",
        germanName = "Mokka (Dunkle Kaffeebohne)",
        shortName = "Mokka",
        primaryHex = 0xFF54371C,
        secondaryHex = 0xFF694829,
        backgroundHex = 0xFFF7F4F1,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF1E140B,
        primaryContainerHex = 0xFFD7C2B0,
        onPrimaryContainerHex = 0xFF231002,
        accentHex = 0xFF6A4A2A,
        borderHex = 0xFFC7AE99,
        surfaceVariantHex = 0xFFEEE7E2,
        onSurfaceVariantHex = 0xFF46392F,
        outlineHex = 0xFF72553B,
        swatchHex = 0xFF6A4A2A // #6A4A2A
    ),
    EICHE(
        id = "Eiche",
        germanName = "Eiche (Echtes Eichenholz)",
        shortName = "Eiche",
        primaryHex = 0xFF6E522B,
        secondaryHex = 0xFF856539,
        backgroundHex = 0xFFF8F5F2,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF221A10,
        primaryContainerHex = 0xFFE5D3BD,
        onPrimaryContainerHex = 0xFF271705,
        accentHex = 0xFF8A6A3A,
        borderHex = 0xFFD4C0A4,
        surfaceVariantHex = 0xFFF3ECE4,
        onSurfaceVariantHex = 0xFF4B4033,
        outlineHex = 0xFF7B6649,
        swatchHex = 0xFF8A6A3A // #8A6A3A
    ),
    WALNUSS(
        id = "Walnuss",
        germanName = "Walnuss (Dunkle Walnussschale)",
        shortName = "Walnuss",
        primaryHex = 0xFF46291B,
        secondaryHex = 0xFF5A3828,
        backgroundHex = 0xFFF7F4F2,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF1D100A,
        primaryContainerHex = 0xFFD2B9AC,
        onPrimaryContainerHex = 0xFF210902,
        accentHex = 0xFF5A3A2A,
        borderHex = 0xFFC0A294,
        surfaceVariantHex = 0xFFEEE6E3,
        onSurfaceVariantHex = 0xFF453630,
        outlineHex = 0xFF6C4C3E,
        swatchHex = 0xFF5A3A2A // #5A3A2A
    ),
    KASTANIE(
        id = "Kastanie",
        germanName = "Kastanie (Glänzendes Kastanienbraun)",
        shortName = "Kastanie",
        primaryHex = 0xFF572A11,
        secondaryHex = 0xFF6D381B,
        backgroundHex = 0xFFF8F3F1,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF200F06,
        primaryContainerHex = 0xFFDEBDB0,
        onPrimaryContainerHex = 0xFF270800,
        accentHex = 0xFF6A3A1A,
        borderHex = 0xFFCCA898,
        surfaceVariantHex = 0xFFEFE5E0,
        onSurfaceVariantHex = 0xFF47342C,
        outlineHex = 0xFF764A35,
        swatchHex = 0xFF6A3A1A // #6A3A1A
    ),

    // =========================================================================
    // 5. Gedämpfte Töne (Lehm & Stein)
    // =========================================================================
    FARN(
        id = "Farn",
        germanName = "Farn (Waldiger Farnton)",
        shortName = "Farn",
        primaryHex = 0xFF445E36,
        secondaryHex = 0xFF567247,
        backgroundHex = 0xFFF6F8F4,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF182014,
        primaryContainerHex = 0xFFCEE0C2,
        onPrimaryContainerHex = 0xFF101E0A,
        accentHex = 0xFF5A7A4A,
        borderHex = 0xFFC0D4B2,
        surfaceVariantHex = 0xFFEEF3E9,
        onSurfaceVariantHex = 0xFF404A3B,
        outlineHex = 0xFF637458,
        swatchHex = 0xFF5A7A4A // #5A7A4A
    ),
    BASILIKUM(
        id = "Basilikum",
        germanName = "Basilikum (Dunkles Kraut)",
        shortName = "Basilikum",
        primaryHex = 0xFF365129,
        secondaryHex = 0xFF476437,
        backgroundHex = 0xFFF5F7F3,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF141D10,
        primaryContainerHex = 0xFFC3DAB3,
        onPrimaryContainerHex = 0xFF0D1C06,
        accentHex = 0xFF4A6A3A,
        borderHex = 0xFFB3CFA1,
        surfaceVariantHex = 0xFFECF2E7,
        onSurfaceVariantHex = 0xFF3B4634,
        outlineHex = 0xFF586E4B,
        swatchHex = 0xFF4A6A3A // #4A6A3A
    ),
    ROSMARIN(
        id = "Rosmarin",
        germanName = "Rosmarin (Blaugrünes Nadelkraut)",
        shortName = "Rosmarin",
        primaryHex = 0xFF355244,
        secondaryHex = 0xFF456455,
        backgroundHex = 0xFFF4F8F6,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF121E19,
        primaryContainerHex = 0xFFC3DDD1,
        onPrimaryContainerHex = 0xFF0A1B14,
        accentHex = 0xFF4A6A5A,
        borderHex = 0xFFB3D1C3,
        surfaceVariantHex = 0xFFEBF3EF,
        onSurfaceVariantHex = 0xFF3B4741,
        outlineHex = 0xFF587265,
        swatchHex = 0xFF4A6A5A // #4A6A5A
    ),
    THYMIAN(
        id = "Thymian",
        germanName = "Thymian (Graugrünes Kraut)",
        shortName = "Thymian",
        primaryHex = 0xFF435C43,
        secondaryHex = 0xFF556F55,
        backgroundHex = 0xFFF5F8F5,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF162016,
        primaryContainerHex = 0xFFD0E3D0,
        onPrimaryContainerHex = 0xFF0E1E0E,
        accentHex = 0xFF5A7A5A,
        borderHex = 0xFFBFD6BF,
        surfaceVariantHex = 0xFFECF3EC,
        onSurfaceVariantHex = 0xFF3F4B3F,
        outlineHex = 0xFF657865,
        swatchHex = 0xFF5A7A5A // #5A7A5A
    ),
    MINZE(
        id = "Minze",
        germanName = "Minze (Frisches Wiesenhellgrün)",
        shortName = "Minze",
        primaryHex = 0xFF547C54,
        secondaryHex = 0xFF689468,
        backgroundHex = 0xFFF6FAF6,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF162216,
        primaryContainerHex = 0xFFD2ECD2,
        onPrimaryContainerHex = 0xFF0F260F,
        accentHex = 0xFF7AAA7A,
        borderHex = 0xFFC2E4C2,
        surfaceVariantHex = 0xFFEAF5EA,
        onSurfaceVariantHex = 0xFF3D4F3D,
        outlineHex = 0xFF6B8C6B,
        swatchHex = 0xFF7AAA7A // #7AAA7A
    ),
    SALBEI(
        id = "Salbei",
        germanName = "Salbei (Sanftes Salbeigrün)",
        shortName = "Salbei",
        primaryHex = 0xFF596B4B,
        secondaryHex = 0xFF6F7E62,
        backgroundHex = 0xFFF7F9F5,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF1D2319,
        primaryContainerHex = 0xFFDDE7D3,
        onPrimaryContainerHex = 0xFF182211,
        accentHex = 0xFF8A9A7A,
        borderHex = 0xFFD2DCB9,
        surfaceVariantHex = 0xFFF1F5ED,
        onSurfaceVariantHex = 0xFF464E41,
        outlineHex = 0xFF748169,
        swatchHex = 0xFF8A9A7A // #8A9A7A
    ),
    MOOS(
        id = "Moos",
        germanName = "Moos (Tiefes Moosgrün)",
        shortName = "Moos",
        primaryHex = 0xFF4E5E2F,
        secondaryHex = 0xFF627342,
        backgroundHex = 0xFFF7F8F4,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF1B2111,
        primaryContainerHex = 0xFFD6E3BE,
        onPrimaryContainerHex = 0xFF131D08,
        accentHex = 0xFF6A7A4A,
        borderHex = 0xFFCCD9B2,
        surfaceVariantHex = 0xFFF0F4E8,
        onSurfaceVariantHex = 0xFF434C37,
        outlineHex = 0xFF6C775D,
        swatchHex = 0xFF6A7A4A // #6A7A4A
    ),
    GRUENSPAN(
        id = "Grünspan",
        germanName = "Grünspan (Kräftiges Waldkraut)",
        shortName = "Grünspan",
        primaryHex = 0xFF2A5E2A,
        secondaryHex = 0xFF3C723C,
        backgroundHex = 0xFFF4F9F4,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF102110,
        primaryContainerHex = 0xFFBDDEBD,
        onPrimaryContainerHex = 0xFF081F08,
        accentHex = 0xFF3A7A3A,
        borderHex = 0xFFAED3AE,
        surfaceVariantHex = 0xFFE9F3E9,
        onSurfaceVariantHex = 0xFF384938,
        outlineHex = 0xFF517E51,
        swatchHex = 0xFF3A7A3A // #3A7A3A
    ),

    // =========================================================================
    // Category 1: Calm & Soft (10 Colors)
    // =========================================================================
    CALM_SAGE("calm_sage", "Sage", 0xFF7C9082, 0xFF5A6B5F, 0xFFA3B5A8, "Calm & Soft"),
    CALM_DUSTY_ROSE("calm_dusty_rose", "Dusty Rose", 0xFFB97A8B, 0xFF8A5A68, 0xFFD4A0AE, "Calm & Soft"),
    CALM_SLATE_BLUE("calm_slate_blue", "Slate Blue", 0xFF5C7A99, 0xFF3D5A78, 0xFF8AA6BF, "Calm & Soft"),
    CALM_MAUVE("calm_mauve", "Mauve", 0xFF8E6A9C, 0xFF6A4A78, 0xFFB595C2, "Calm & Soft"),
    CALM_FOG_GRAY("calm_fog_gray", "Fog Gray", 0xFF6B6E73, 0xFF4A4D52, 0xFF9A9DA3, "Calm & Soft"),
    CALM_MUTED_TEAL("calm_muted_teal", "Muted Teal", 0xFF5A8A85, 0xFF3A6A65, 0xFF88B5AE, "Calm & Soft"),
    CALM_SOFT_INDIGO("calm_soft_indigo", "Soft Indigo", 0xFF5B6B9A, 0xFF3B4B7A, 0xFF8794BD, "Calm & Soft"),
    CALM_PALE_LAVENDER("calm_pale_lavender", "Pale Lavender", 0xFF9A8FB5, 0xFF6E6394, 0xFFC0B8D4, "Calm & Soft"),
    CALM_SOFT_CORAL("calm_soft_coral", "Soft Coral", 0xFFC4837A, 0xFF94635A, 0xFFD9A8A0, "Calm & Soft"),
    CALM_COOL_GRAY("calm_cool_gray", "Cool Gray", 0xFF7A7D82, 0xFF5A5D62, 0xFFA5A8AD, "Calm & Soft"),

    // =========================================================================
    // Category 2: Nature (10 Colors)
    // =========================================================================
    NAT_OLIVE("nat_olive", "Olive", 0xFF7A8A4A, 0xFF5A6A2A, 0xFFA3B37A, "Nature"),
    NAT_DEEP_FOREST("nat_deep_forest", "Deep Forest", 0xFF4A6B52, 0xFF2A4B32, 0xFF7A9A83, "Nature"),
    NAT_MOSS("nat_moss", "Moss", 0xFF6A7A4A, 0xFF4A5A2A, 0xFF93A37A, "Nature"),
    NAT_BARK("nat_bark", "Bark", 0xFF7A5A3A, 0xFF5A3A1A, 0xFFA08A6A, "Nature"),
    NAT_FERN("nat_fern", "Fern", 0xFF5A7A5A, 0xFF3A5A3A, 0xFF8AA38A, "Nature"),
    NAT_PINE("nat_pine", "Pine", 0xFF3A5A4A, 0xFF1A3A2A, 0xFF6A8A7A, "Nature"),
    NAT_SAND("nat_sand", "Sand", 0xFFC4A57B, 0xFF94755B, 0xFFDCC7A6, "Nature"),
    NAT_CLAY("nat_clay", "Clay", 0xFFB87A5A, 0xFF885A3A, 0xFFD4A088, "Nature"),
    NAT_STONE("nat_stone", "Stone", 0xFF8A8A7A, 0xFF5A5A4A, 0xFFB5B5A5, "Nature"),
    NAT_LICHEN("nat_lichen", "Lichen", 0xFF8A9A7A, 0xFF5A6A4A, 0xFFB5C0A5, "Nature"),

    // =========================================================================
    // Category 3: Professional (10 Colors)
    // =========================================================================
    PRO_NAVY("pro_navy", "Navy", 0xFF2C3E50, 0xFF1A2838, 0xFF5A6B7F, "Professional"),
    PRO_CHARCOAL("pro_charcoal", "Charcoal", 0xFF3A3A3A, 0xFF1A1A1A, 0xFF6A6A6A, "Professional"),
    PRO_STEEL("pro_steel", "Steel", 0xFF5A6B7A, 0xFF3A4B5A, 0xFF8A9BA8, "Professional"),
    PRO_GRAPHITE("pro_graphite", "Graphite", 0xFF4A4A4A, 0xFF2A2A2A, 0xFF7A7A7A, "Professional"),
    PRO_MIDNIGHT("pro_midnight", "Midnight", 0xFF1A2A3A, 0xFF0A1520, 0xFF4A5A6A, "Professional"),
    PRO_ASH("pro_ash", "Ash", 0xFF7A7A7A, 0xFF5A5A5A, 0xFFA5A5A5, "Professional"),
    PRO_COBALT("pro_cobalt", "Cobalt", 0xFF2C5282, 0xFF1A3A5A, 0xFF5A7AA8, "Professional"),
    PRO_ESPRESSO("pro_espresso", "Espresso", 0xFF4A3A2A, 0xFF2A1A0A, 0xFF7A6A5A, "Professional"),
    PRO_STEEL_BLUE("pro_steel_blue", "Steel Blue", 0xFF4A6A8A, 0xFF2A4A6A, 0xFF7A9AB5, "Professional"),
    PRO_GUNMETAL("pro_gunmetal", "Gunmetal", 0xFF3A3F4A, 0xFF1A1F2A, 0xFF6A6F7A, "Professional"),

    // =========================================================================
    // Category 4: Warm (10 Colors)
    // =========================================================================
    WARM_TERRACOTTA("warm_terracotta", "Terracotta", 0xFFC1785A, 0xFF915838, 0xFFD9A088, "Warm"),
    WARM_BURNT_ORANGE("warm_burnt_orange", "Burnt Orange", 0xFFC25A3A, 0xFF923A1A, 0xFFD98868, "Warm"),
    WARM_AMBER("warm_amber", "Amber", 0xFFD4A03A, 0xFFA4801A, 0xFFE8C06A, "Warm"),
    WARM_RUST("warm_rust", "Rust", 0xFFA85A3A, 0xFF783A1A, 0xFFC88A68, "Warm"),
    WARM_COPPER("warm_copper", "Copper", 0xFFB87333, 0xFF885318, 0xFFD4A06A, "Warm"),
    WARM_CINNAMON("warm_cinnamon", "Cinnamon", 0xFFA86A4A, 0xFF784A2A, 0xFFC89A7A, "Warm"),
    WARM_CARAMEL("warm_caramel", "Caramel", 0xFFB88A5A, 0xFF886A3A, 0xFFD4B08A, "Warm"),
    WARM_HONEY("warm_honey", "Honey", 0xFFD4B05A, 0xFFA4803A, 0xFFE8C88A, "Warm"),
    WARM_BRICK("warm_brick", "Brick", 0xFFA0503A, 0xFF703018, 0xFFC88068, "Warm"),
    WARM_PEACH("warm_peach", "Peach", 0xFFD49A7A, 0xFFA47A5A, 0xFFE8BCA0, "Warm"),

    // =========================================================================
    // Category 5: Cool (10 Colors)
    // =========================================================================
    COOL_ICE_BLUE("cool_ice_blue", "Ice Blue", 0xFF7AA5C4, 0xFF5A85A4, 0xFFA8C8DE, "Cool"),
    COOL_SKY("cool_sky", "Sky", 0xFF6AA8D4, 0xFF4A88B4, 0xFF98C8E8, "Cool"),
    COOL_OCEAN("cool_ocean", "Ocean", 0xFF3A7A9A, 0xFF1A5A7A, 0xFF6A9AB5, "Cool"),
    COOL_AQUA("cool_aqua", "Aqua", 0xFF5AA8A5, 0xFF3A8885, 0xFF88C8C5, "Cool"),
    COOL_MINT("cool_mint", "Mint", 0xFF7ABFA5, 0xFF5A9F85, 0xFFA5D8C5, "Cool"),
    COOL_TURQUOISE("cool_turquoise", "Turquoise", 0xFF3AA8A0, 0xFF1A8880, 0xFF6AC8C0, "Cool"),
    COOL_DENIM("cool_denim", "Denim", 0xFF4A6A9A, 0xFF2A4A7A, 0xFF7A9AC0, "Cool"),
    COOL_PERIWINKLE("cool_periwinkle", "Periwinkle", 0xFF8A9AD4, 0xFF6A7AB4, 0xFFB5C0E8, "Cool"),
    COOL_AZURE("cool_azure", "Azure", 0xFF4A9AD4, 0xFF2A7AB4, 0xFF7ABAE8, "Cool"),
    COOL_ARCTIC("cool_arctic", "Arctic", 0xFFA8C8D4, 0xFF88A8B4, 0xFFC8E0E8, "Cool"),

    // =========================================================================
    // Category 6: Pastel (10 Colors)
    // =========================================================================
    PASTEL_BABY_PINK("pastel_baby_pink", "Baby Pink", 0xFFE8A8B8, 0xFFC88898, 0xFFF5C8D4, "Pastel"),
    PASTEL_BABY_BLUE("pastel_baby_blue", "Baby Blue", 0xFFA8C8E8, 0xFF88A8C8, 0xFFC8E0F5, "Pastel"),
    PASTEL_BABY_GREEN("pastel_baby_green", "Baby Green", 0xFFA8D4A8, 0xFF88B488, 0xFFC8E8C8, "Pastel"),
    PASTEL_BABY_YELLOW("pastel_baby_yellow", "Baby Yellow", 0xFFE8D4A8, 0xFFC8B488, 0xFFF5E8C8, "Pastel"),
    PASTEL_BABY_PURPLE("pastel_baby_purple", "Baby Purple", 0xFFC8A8E8, 0xFFA888C8, 0xFFE0C8F5, "Pastel"),
    PASTEL_BABY_PEACH("pastel_baby_peach", "Baby Peach", 0xFFE8C8A8, 0xFFC8A888, 0xFFF5E0C8, "Pastel"),
    PASTEL_BABY_MINT("pastel_baby_mint", "Baby Mint", 0xFFA8E8D4, 0xFF88C8B4, 0xFFC8F5E8, "Pastel"),
    PASTEL_BABY_LAVENDER("pastel_baby_lavender", "Baby Lavender", 0xFFD4C8E8, 0xFFB4A8C8, 0xFFE8E0F5, "Pastel"),
    PASTEL_BABY_CORAL("pastel_baby_coral", "Baby Coral", 0xFFE8B8A8, 0xFFC89888, 0xFFF5D4C8, "Pastel"),
    PASTEL_BABY_SKY("pastel_baby_sky", "Baby Sky", 0xFFB8D4E8, 0xFF98B4C8, 0xFFD4E8F5, "Pastel"),

    // =========================================================================
    // Category 7: Dark & Moody (10 Colors)
    // =========================================================================
    DARK_DEEP_PURPLE("dark_deep_purple", "Deep Purple", 0xFF4A2A6A, 0xFF2A0A4A, 0xFF7A5A9A, "Dark & Moody"),
    DARK_DEEP_RED("dark_deep_red", "Deep Red", 0xFF6A2A2A, 0xFF4A0A0A, 0xFF9A5A5A, "Dark & Moody"),
    DARK_DEEP_BLUE("dark_deep_blue", "Deep Blue", 0xFF2A3A6A, 0xFF0A1A4A, 0xFF5A6A9A, "Dark & Moody"),
    DARK_DEEP_GREEN("dark_deep_green", "Deep Green", 0xFF2A4A3A, 0xFF0A2A1A, 0xFF5A7A6A, "Dark & Moody"),
    DARK_DEEP_BROWN("dark_deep_brown", "Deep Brown", 0xFF4A3A2A, 0xFF2A1A0A, 0xFF7A6A5A, "Dark & Moody"),
    DARK_DEEP_TEAL("dark_deep_teal", "Deep Teal", 0xFF2A4A4A, 0xFF0A2A2A, 0xFF5A7A7A, "Dark & Moody"),
    DARK_DEEP_WINE("dark_deep_wine", "Deep Wine", 0xFF5A2A3A, 0xFF3A0A1A, 0xFF8A5A6A, "Dark & Moody"),
    DARK_DEEP_OLIVE("dark_deep_olive", "Deep Olive", 0xFF4A4A2A, 0xFF2A2A0A, 0xFF7A7A5A, "Dark & Moody"),
    DARK_DEEP_SLATE("dark_deep_slate", "Deep Slate", 0xFF3A3A4A, 0xFF1A1A2A, 0xFF6A6A7A, "Dark & Moody"),
    DARK_DEEP_PLUM("dark_deep_plum", "Deep Plum", 0xFF4A2A4A, 0xFF2A0A2A, 0xFF7A5A7A, "Dark & Moody"),

    // =========================================================================
    // Category 8: Vibrant (10 Colors)
    // =========================================================================
    VIB_RUBY("vib_ruby", "Ruby", 0xFFC42A4A, 0xFF941A3A, 0xFFD95A7A, "Vibrant"),
    VIB_EMERALD("vib_emerald", "Emerald", 0xFF2AA85A, 0xFF1A883A, 0xFF5AC88A, "Vibrant"),
    VIB_SAPPHIRE("vib_sapphire", "Sapphire", 0xFF2A5AC4, 0xFF1A3A94, 0xFF5A8AD9, "Vibrant"),
    VIB_TOPAZ("vib_topaz", "Topaz", 0xFFC4902A, 0xFF94701A, 0xFFD9B05A, "Vibrant"),
    VIB_AMETHYST("vib_amethyst", "Amethyst", 0xFF8A2AC4, 0xFF6A1A94, 0xFFB55AD9, "Vibrant"),
    VIB_CORAL("vib_coral", "Coral", 0xFFC45A4A, 0xFF943A2A, 0xFFD98A7A, "Vibrant"),
    VIB_LIME("vib_lime", "Lime", 0xFF8AC42A, 0xFF6A941A, 0xFFB5D95A, "Vibrant"),
    VIB_CYAN("vib_cyan", "Cyan", 0xFF2AC4C4, 0xFF1A9494, 0xFF5AD9D9, "Vibrant"),
    VIB_MAGENTA("vib_magenta", "Magenta", 0xFFC42A8A, 0xFF941A6A, 0xFFD95AB5, "Vibrant"),
    VIB_GOLD("vib_gold", "Gold", 0xFFC4A02A, 0xFF94801A, 0xFFD9C05A, "Vibrant"),

    // =========================================================================
    // Category 9: Earth (10 Colors)
    // =========================================================================
    EARTH_UMBER("earth_umber", "Umber", 0xFF6A5A3A, 0xFF4A3A1A, 0xFF9A8A6A, "Earth"),
    EARTH_SIENNA("earth_sienna", "Sienna", 0xFFA85A2A, 0xFF783A0A, 0xFFC88A5A, "Earth"),
    EARTH_OCHRE("earth_ochre", "Ochre", 0xFFC4A04A, 0xFF94702A, 0xFFD9C07A, "Earth"),
    EARTH_MAHOGANY("earth_mahogany", "Mahogany", 0xFF6A3A2A, 0xFF4A1A0A, 0xFF9A6A5A, "Earth"),
    EARTH_TAUPE("earth_taupe", "Taupe", 0xFF8A7A6A, 0xFF6A5A4A, 0xFFB5A595, "Earth"),
    EARTH_CAMEL("earth_camel", "Camel", 0xFFB89A6A, 0xFF887A4A, 0xFFD4B58A, "Earth"),
    EARTH_KHAKI("earth_khaki", "Khaki", 0xFF9A8A5A, 0xFF7A6A3A, 0xFFC0B08A, "Earth"),
    EARTH_CHESTNUT("earth_chestnut", "Chestnut", 0xFF7A4A2A, 0xFF5A2A0A, 0xFFA87A5A, "Earth"),
    EARTH_WALNUT("earth_walnut", "Walnut", 0xFF5A3A2A, 0xFF3A1A0A, 0xFF8A6A5A, "Earth"),
    EARTH_DUNE("earth_dune", "Dune", 0xFFC4B09A, 0xFF94806A, 0xFFD9C8B5, "Earth"),

    // =========================================================================
    // Category 10: Signature (10 Colors)
    // =========================================================================
    SIG_GOOGLE_BLUE("sig_google_blue", "Google Blue", 0xFF4285F4, 0xFF2A5FC4, 0xFF6AA5F5, "Signature"),
    SIG_GOOGLE_RED("sig_google_red", "Google Red", 0xFFEA4335, 0xFFBA2315, 0xFFF57368, "Signature"),
    SIG_GOOGLE_YELLOW("sig_google_yellow", "Google Yellow", 0xFFFBBC05, 0xFFCB8C00, 0xFFFCD45A, "Signature"),
    SIG_GOOGLE_GREEN("sig_google_green", "Google Green", 0xFF34A853, 0xFF147833, 0xFF5AC87A, "Signature"),
    SIG_DEEP_PURPLE("sig_deep_purple", "Deep Purple (Signature)", 0xFF6750A4, 0xFF47307A, 0xFF9A8AC4, "Signature"),
    SIG_SUNSET_ORANGE("sig_sunset_orange", "Sunset Orange", 0xFFFF6B35, 0xFFCC4B15, 0xFFFF9A6B, "Signature"),
    SIG_ROSE_GOLD("sig_rose_gold", "Rose Gold", 0xFFB76E79, 0xFF874E59, 0xFFD09AA5, "Signature"),
    SIG_EMERALD_CITY("sig_emerald_city", "Emerald City", 0xFF046A38, 0xFF024A18, 0xFF2A9A68, "Signature"),
    SIG_MIDNIGHT_BLUE("sig_midnight_blue", "Midnight Blue", 0xFF191970, 0xFF090940, 0xFF4A4A9A, "Signature"),
    SIG_CRIMSON("sig_crimson", "Crimson", 0xFFDC143C, 0xFFAC001C, 0xFFF54A6C, "Signature");

    constructor(
        id: String,
        name: String,
        primaryHex: Long,
        darkHex: Long,
        lightHex: Long,
        categoryName: String = ""
    ) : this(
        id = id,
        germanName = name,
        shortName = name,
        primaryHex = primaryHex,
        secondaryHex = darkHex,
        backgroundHex = ThemeLayerMath.blendHex(0xFFFAFBFD, primaryHex, 0.045f),
        surfaceHex = ThemeLayerMath.blendHex(0xFFFFFFFF, primaryHex, 0.02f),
        onSurfaceHex = 0xFF1C1D21,
        primaryContainerHex = ThemeLayerMath.blendHex(0xFFFFFFFF, lightHex, 0.28f),
        onPrimaryContainerHex = ThemeLayerMath.darkenHex(darkHex, 0.35f),
        accentHex = lightHex,
        borderHex = ThemeLayerMath.blendHex(0xFFE0E2E7, primaryHex, 0.24f),
        surfaceVariantHex = ThemeLayerMath.blendHex(0xFFF2F4F7, primaryHex, 0.14f),
        onSurfaceVariantHex = ThemeLayerMath.blendHex(0xFF4A4D54, darkHex, 0.25f),
        outlineHex = ThemeLayerMath.blendHex(0xFF72767E, primaryHex, 0.35f),
        swatchHex = primaryHex,
        hasCustomPalette = true,
        primaryLightHex = primaryHex,
        onPrimaryLightHex = 0xFFFFFFFF,
        primaryContainerLightHex = ThemeLayerMath.blendHex(0xFFFFFFFF, lightHex, 0.28f),
        onPrimaryContainerLightHex = ThemeLayerMath.darkenHex(darkHex, 0.35f),
        secondaryLightHex = darkHex,
        onSecondaryLightHex = 0xFFFFFFFF,
        secondaryContainerLightHex = ThemeLayerMath.blendHex(0xFFFFFFFF, darkHex, 0.18f),
        onSecondaryContainerLightHex = ThemeLayerMath.darkenHex(darkHex, 0.42f),
        tertiaryLightHex = lightHex,
        onTertiaryLightHex = 0xFFFFFFFF,
        tertiaryContainerLightHex = ThemeLayerMath.blendHex(0xFFFFFFFF, lightHex, 0.22f),
        onTertiaryContainerLightHex = ThemeLayerMath.darkenHex(primaryHex, 0.38f),
        errorLightHex = 0xFFC0392B,
        onErrorLightHex = 0xFFFFFFFF,
        errorContainerLightHex = 0xFFFFDAD6,
        onErrorContainerLightHex = 0xFF410002,
        backgroundLightHex = ThemeLayerMath.blendHex(0xFFFAFBFD, primaryHex, 0.045f),
        onBackgroundLightHex = 0xFF1A1B1E,
        surfaceLightHex = ThemeLayerMath.blendHex(0xFFFFFFFF, primaryHex, 0.02f),
        onSurfaceLightHex = 0xFF1C1D21,
        surfaceVariantLightHex = ThemeLayerMath.blendHex(0xFFF2F4F7, primaryHex, 0.14f),
        onSurfaceVariantLightHex = ThemeLayerMath.blendHex(0xFF4A4D54, darkHex, 0.25f),
        outlineLightHex = ThemeLayerMath.blendHex(0xFF72767E, primaryHex, 0.35f),
        primaryDarkHex = lightHex,
        onPrimaryDarkHex = ThemeLayerMath.darkenHex(darkHex, 0.55f),
        primaryContainerDarkHex = ThemeLayerMath.blendHex(0xFF18191C, darkHex, 0.58f),
        onPrimaryContainerDarkHex = ThemeLayerMath.lightenHex(lightHex, 0.35f),
        secondaryDarkHex = ThemeLayerMath.blendHex(darkHex, lightHex, 0.40f),
        onSecondaryDarkHex = 0xFFFFFFFF,
        secondaryContainerDarkHex = ThemeLayerMath.blendHex(0xFF1E2024, darkHex, 0.38f),
        onSecondaryContainerDarkHex = ThemeLayerMath.lightenHex(lightHex, 0.42f),
        tertiaryDarkHex = ThemeLayerMath.lightenHex(lightHex, 0.22f),
        onTertiaryDarkHex = ThemeLayerMath.darkenHex(darkHex, 0.55f),
        tertiaryContainerDarkHex = ThemeLayerMath.blendHex(0xFF1E2024, lightHex, 0.28f),
        onTertiaryContainerDarkHex = ThemeLayerMath.lightenHex(lightHex, 0.48f),
        errorDarkHex = 0xFFE74C3C,
        onErrorDarkHex = 0xFFFFFFFF,
        errorContainerDarkHex = 0xFF93000A,
        onErrorContainerDarkHex = 0xFFFFDAD6,
        backgroundDarkHex = ThemeLayerMath.blendHex(0xFF111215, darkHex, 0.12f),
        onBackgroundDarkHex = 0xFFECEEF2,
        surfaceDarkHex = ThemeLayerMath.blendHex(0xFF17181C, darkHex, 0.14f),
        onSurfaceDarkHex = 0xFFE6E8EC,
        surfaceVariantDarkHex = ThemeLayerMath.blendHex(0xFF23252A, primaryHex, 0.18f),
        onSurfaceVariantDarkHex = ThemeLayerMath.blendHex(0xFFB4B8C0, lightHex, 0.22f),
        outlineDarkHex = ThemeLayerMath.blendHex(0xFF8A8E96, lightHex, 0.38f),
        isBoldTheme = false,
        headerKasusLightHex = ThemeLayerMath.blendHex(0xFFF2F4F7, primaryHex, 0.16f),
        headerKasusDarkHex = ThemeLayerMath.blendHex(0xFF23252A, primaryHex, 0.22f),
        glowColorLightHex = primaryHex,
        glowColorDarkHex = lightHex,
        bannerGradientStartLightHex = primaryHex,
        bannerGradientEndLightHex = lightHex,
        bannerGradientStartDarkHex = darkHex,
        bannerGradientEndDarkHex = lightHex,
        cardAccentRibbonLightHex = primaryHex,
        cardAccentRibbonDarkHex = lightHex,
        categoryName = categoryName
    )


    val primary: Color get() = Color(primaryHex)
    val secondary: Color get() = Color(secondaryHex)
    val background: Color get() = Color(backgroundHex)
    val surface: Color get() = Color(surfaceHex)
    val onSurface: Color get() = Color(onSurfaceHex)
    val primaryContainer: Color get() = Color(primaryContainerHex)
    val onPrimaryContainer: Color get() = Color(onPrimaryContainerHex)
    val accent: Color get() = Color(accentHex)
    val border: Color get() = Color(borderHex)
    val surfaceVariant: Color get() = Color(surfaceVariantHex)
    val onSurfaceVariant: Color get() = Color(onSurfaceVariantHex)
    val outline: Color get() = Color(outlineHex)
    val swatch: Color get() = Color(swatchHex)

    val darkVariantHex: Long
        get() = when {
            secondaryHex != 0L && secondaryHex != primaryHex -> secondaryHex
            else -> ThemeLayerMath.darkenHex(primaryHex, 0.24f)
        }

    val lightVariantHex: Long
        get() = when {
            accentHex != 0L && accentHex != primaryHex && accentHex != secondaryHex -> accentHex
            primaryContainerHex != 0L && primaryContainerHex != primaryHex -> ThemeLayerMath.blendHex(primaryHex, primaryContainerHex, 0.45f)
            else -> ThemeLayerMath.lightenHex(primaryHex, 0.26f)
        }

    val darkVariant: Color get() = Color(darkVariantHex)
    val lightVariant: Color get() = Color(lightVariantHex)

    // =========================================================================
    // 4-Tier Multi-Layered Color System Accessors
    // =========================================================================

    fun tier1Primary(isDark: Boolean): Color = Color(
        if (isDark) {
            if (hasCustomPalette) primaryDarkHex else ThemeLayerMath.lightenHex(lightVariantHex, 0.14f)
        } else {
            primaryLightHex
        }
    )

    fun tier2Secondary(isDark: Boolean): Color = Color(
        if (isDark) {
            if (hasCustomPalette && secondaryDarkHex != primaryDarkHex) {
                secondaryDarkHex
            } else {
                ThemeLayerMath.blendHex(darkVariantHex, lightVariantHex, 0.35f)
            }
        } else {
            if (secondaryLightHex != primaryLightHex) {
                secondaryLightHex
            } else {
                darkVariantHex
            }
        }
    )

    fun tier3Tertiary(isDark: Boolean): Color = Color(
        if (isDark) {
            if (hasCustomPalette && tertiaryDarkHex != primaryDarkHex && tertiaryDarkHex != secondaryDarkHex) {
                tertiaryDarkHex
            } else {
                ThemeLayerMath.lightenHex(lightVariantHex, 0.28f)
            }
        } else {
            if (tertiaryLightHex != primaryLightHex && tertiaryLightHex != secondaryLightHex) {
                tertiaryLightHex
            } else {
                lightVariantHex
            }
        }
    )

    fun tier4Quaternary(isDark: Boolean): Color = Color(
        if (isDark) {
            ThemeLayerMath.blendHex(
                if (hasCustomPalette) surfaceVariantDarkHex else 0xFF23252A,
                lightVariantHex,
                0.15f
            )
        } else {
            ThemeLayerMath.blendHex(
                if (hasCustomPalette) surfaceVariantLightHex else surfaceVariantHex,
                primaryLightHex,
                0.10f
            )
        }
    )

    fun tier4Swatch(isDark: Boolean): Color = Color(
        if (isDark) {
            ThemeLayerMath.blendHex(0xFF2A2C32, lightVariantHex, 0.34f)
        } else {
            ThemeLayerMath.blendHex(0xFFF3F5F8, primaryHex, 0.30f)
        }
    )

    fun tier1Container(isDark: Boolean): Color = Color(
        if (isDark) {
            ThemeLayerMath.blendHex(0xFF18191D, darkVariantHex, 0.52f)
        } else {
            if (primaryContainerLightHex != primaryLightHex) primaryContainerLightHex
            else ThemeLayerMath.blendHex(0xFFFFFFFF, lightVariantHex, 0.26f)
        }
    )

    fun onTier1Container(isDark: Boolean): Color = Color(
        if (isDark) {
            ThemeLayerMath.lightenHex(lightVariantHex, 0.42f)
        } else {
            ThemeLayerMath.darkenHex(darkVariantHex, 0.38f)
        }
    )

    fun tier2Container(isDark: Boolean): Color = Color(
        if (isDark) {
            ThemeLayerMath.blendHex(0xFF1D1F24, darkVariantHex, 0.36f)
        } else {
            ThemeLayerMath.blendHex(0xFFFFFFFF, darkVariantHex, 0.16f)
        }
    )

    fun onTier2Container(isDark: Boolean): Color = Color(
        if (isDark) {
            ThemeLayerMath.lightenHex(lightVariantHex, 0.45f)
        } else {
            ThemeLayerMath.darkenHex(darkVariantHex, 0.42f)
        }
    )

    fun tier3Container(isDark: Boolean): Color = Color(
        if (isDark) {
            ThemeLayerMath.blendHex(0xFF1D1F24, lightVariantHex, 0.26f)
        } else {
            ThemeLayerMath.blendHex(0xFFFFFFFF, lightVariantHex, 0.20f)
        }
    )

    fun onTier3Container(isDark: Boolean): Color = Color(
        if (isDark) {
            ThemeLayerMath.lightenHex(lightVariantHex, 0.50f)
        } else {
            ThemeLayerMath.darkenHex(primaryHex, 0.38f)
        }
    )

    fun tier4SurfaceLowest(isDark: Boolean): Color = Color(
        if (isDark) ThemeLayerMath.blendHex(0xFF0E0F12, darkVariantHex, 0.10f)
        else 0xFFFFFFFF
    )

    fun tier4SurfaceLow(isDark: Boolean): Color = Color(
        if (isDark) ThemeLayerMath.blendHex(0xFF16171B, darkVariantHex, 0.14f)
        else ThemeLayerMath.blendHex(0xFFF8F9FB, primaryHex, 0.06f)
    )

    fun tier4SurfaceContainer(isDark: Boolean): Color = Color(
        if (isDark) ThemeLayerMath.blendHex(0xFF1C1E23, darkVariantHex, 0.18f)
        else ThemeLayerMath.blendHex(0xFFF3F5F8, primaryHex, 0.10f)
    )

    fun tier4SurfaceHigh(isDark: Boolean): Color = Color(
        if (isDark) ThemeLayerMath.blendHex(0xFF23262C, primaryHex, 0.20f)
        else ThemeLayerMath.blendHex(0xFFEEF1F5, primaryHex, 0.14f)
    )

    fun tier4SurfaceHighest(isDark: Boolean): Color = Color(
        if (isDark) ThemeLayerMath.blendHex(0xFF2C2F36, lightVariantHex, 0.22f)
        else ThemeLayerMath.blendHex(0xFFE6EAF0, primaryHex, 0.18f)
    )

    fun tier4Border(isDark: Boolean): Color = Color(
        if (isDark) ThemeLayerMath.blendHex(0xFF383B42, lightVariantHex, 0.25f)
        else ThemeLayerMath.blendHex(borderHex, primaryHex, 0.18f)
    )

    companion object {
        // Category 1: Calm & Soft (10 Colors)
        val CALM_AND_SOFT_THEMES = listOf(
            CALM_SAGE, CALM_DUSTY_ROSE, CALM_SLATE_BLUE, CALM_MAUVE, CALM_FOG_GRAY,
            CALM_MUTED_TEAL, CALM_SOFT_INDIGO, CALM_PALE_LAVENDER, CALM_SOFT_CORAL, CALM_COOL_GRAY
        )

        // Category 2: Nature (10 Colors)
        val NATURE_THEMES = listOf(
            NAT_OLIVE, NAT_DEEP_FOREST, NAT_MOSS, NAT_BARK, NAT_FERN,
            NAT_PINE, NAT_SAND, NAT_CLAY, NAT_STONE, NAT_LICHEN
        )

        // Category 3: Professional (10 Colors)
        val PROFESSIONAL_THEMES = listOf(
            PRO_NAVY, PRO_CHARCOAL, PRO_STEEL, PRO_GRAPHITE, PRO_MIDNIGHT,
            PRO_ASH, PRO_COBALT, PRO_ESPRESSO, PRO_STEEL_BLUE, PRO_GUNMETAL
        )

        // Category 4: Warm (10 Colors)
        val WARM_THEMES = listOf(
            WARM_TERRACOTTA, WARM_BURNT_ORANGE, WARM_AMBER, WARM_RUST, WARM_COPPER,
            WARM_CINNAMON, WARM_CARAMEL, WARM_HONEY, WARM_BRICK, WARM_PEACH
        )

        // Category 5: Cool (10 Colors)
        val COOL_THEMES = listOf(
            COOL_ICE_BLUE, COOL_SKY, COOL_OCEAN, COOL_AQUA, COOL_MINT,
            COOL_TURQUOISE, COOL_DENIM, COOL_PERIWINKLE, COOL_AZURE, COOL_ARCTIC
        )

        // Category 6: Pastel (10 Colors)
        val PASTEL_THEMES = listOf(
            PASTEL_BABY_PINK, PASTEL_BABY_BLUE, PASTEL_BABY_GREEN, PASTEL_BABY_YELLOW, PASTEL_BABY_PURPLE,
            PASTEL_BABY_PEACH, PASTEL_BABY_MINT, PASTEL_BABY_LAVENDER, PASTEL_BABY_CORAL, PASTEL_BABY_SKY
        )

        // Category 7: Dark & Moody (10 Colors)
        val DARK_AND_MOODY_THEMES = listOf(
            DARK_DEEP_PURPLE, DARK_DEEP_RED, DARK_DEEP_BLUE, DARK_DEEP_GREEN, DARK_DEEP_BROWN,
            DARK_DEEP_TEAL, DARK_DEEP_WINE, DARK_DEEP_OLIVE, DARK_DEEP_SLATE, DARK_DEEP_PLUM
        )

        // Category 8: Vibrant (10 Colors)
        val VIBRANT_THEMES = listOf(
            VIB_RUBY, VIB_EMERALD, VIB_SAPPHIRE, VIB_TOPAZ, VIB_AMETHYST,
            VIB_CORAL, VIB_LIME, VIB_CYAN, VIB_MAGENTA, VIB_GOLD
        )

        // Category 9: Earth (10 Colors)
        val EARTH_THEMES = listOf(
            EARTH_UMBER, EARTH_SIENNA, EARTH_OCHRE, EARTH_MAHOGANY, EARTH_TAUPE,
            EARTH_CAMEL, EARTH_KHAKI, EARTH_CHESTNUT, EARTH_WALNUT, EARTH_DUNE
        )

        // Category 10: Signature (10 Colors)
        val SIGNATURE_THEMES = listOf(
            SIG_GOOGLE_BLUE, SIG_GOOGLE_RED, SIG_GOOGLE_YELLOW, SIG_GOOGLE_GREEN, SIG_DEEP_PURPLE,
            SIG_SUNSET_ORANGE, SIG_ROSE_GOLD, SIG_EMERALD_CITY, SIG_MIDNIGHT_BLUE, SIG_CRIMSON
        )

        // 100 New Color Groups Combined
        val NEW_100_THEMES = CALM_AND_SOFT_THEMES + NATURE_THEMES + PROFESSIONAL_THEMES +
            WARM_THEMES + COOL_THEMES + PASTEL_THEMES + DARK_AND_MOODY_THEMES +
            VIBRANT_THEMES + EARTH_THEMES + SIGNATURE_THEMES

        // 0. Material 3 Haupt-Farben (6 Separate Themes)
        val M3_THEMES = listOf(
            GELB, DUNKELBLAU, HELLGRAU,
            WEISS, DUNKELGRAU, ROT
        )

        // 0B. 10 Neue Kräftige & Expressive Farben (Nicht M3)
        val BOLD_EXPRESSIVE_THEMES = listOf(
            NEON_GELB, KNALLROT, NEON_CYAN, MAGENTA_PINK, GIFTGRUEN,
            KOENIGSBLAU, SONNEN_ORANGE, ELEKTRO_VIOLETT, LEUCHT_TUERKIS, SATTES_BERNSTEIN
        )

        // 1. Rotbraun & Terracotta (Warm Earth)
        val WARM_EARTH_THEMES = listOf(
            TERRACOTTA, ZIEGEL, ROST, KUPFER,
            MAHAGONI, ZIMT, SIENA, TON
        )

        // 2. Sand & Beige (Neutral Warm)
        val NEUTRAL_WARM_THEMES = listOf(
            SAND, DUENE, CREME, NATUR,
            KIES, LEHM, OCKER, GELBBRAUN
        )

        // 3. Grau & Schiefer (Cool Earth)
        val COOL_EARTH_THEMES = listOf(
            SCHIEFER, KIESEL, GRANIT, STEIN,
            ASCHE, MAUVE, TAUPE, GRAPHIT
        )

        // 4. Braun & Umbra (Deep Earth)
        val DEEP_EARTH_THEMES = listOf(
            UMBRA, TERRA, ZEDER, KAKAO,
            MOKKA, EICHE, WALNUSS, KASTANIE
        )

        // 5. Gedämpfte Töne (Lehm & Stein)
        val MUTED_HERBAL_THEMES = listOf(
            FARN, BASILIKUM, ROSMARIN, THYMIAN,
            MINZE, SALBEI, MOOS, GRUENSPAN
        )

        // Preserved classic earth/herbal groups
        val EXISTING_CLASSIC_THEMES = M3_THEMES + BOLD_EXPRESSIVE_THEMES + WARM_EARTH_THEMES +
            NEUTRAL_WARM_THEMES + COOL_EARTH_THEMES + DEEP_EARTH_THEMES + MUTED_HERBAL_THEMES

        // Category Groups for Color Window
        val TEN_CATEGORIES = listOf(
            ColorGroupCategory("calm_soft", "Calm & Soft", "🌸", CALM_AND_SOFT_THEMES),
            ColorGroupCategory("nature", "Nature", "🌿", NATURE_THEMES),
            ColorGroupCategory("professional", "Professional", "💼", PROFESSIONAL_THEMES),
            ColorGroupCategory("warm", "Warm", "🔥", WARM_THEMES),
            ColorGroupCategory("cool", "Cool", "❄️", COOL_THEMES),
            ColorGroupCategory("pastel", "Pastel", "🌸", PASTEL_THEMES),
            ColorGroupCategory("dark_moody", "Dark & Moody", "🌑", DARK_AND_MOODY_THEMES),
            ColorGroupCategory("vibrant", "Vibrant", "🌈", VIBRANT_THEMES),
            ColorGroupCategory("earth", "Earth", "🏜️", EARTH_THEMES),
            ColorGroupCategory("signature", "Signature", "⭐", SIGNATURE_THEMES)
        )

        val EXISTING_CATEGORIES = listOf(
            ColorGroupCategory("m3_themes", "Material 3", "🎨", M3_THEMES),
            ColorGroupCategory("bold_expressive", "Kräftig & Expressiv", "✨", BOLD_EXPRESSIVE_THEMES),
            ColorGroupCategory("warm_earth", "Warm Earth", "🧱", WARM_EARTH_THEMES),
            ColorGroupCategory("neutral_warm", "Neutral Warm", "🏖️", NEUTRAL_WARM_THEMES),
            ColorGroupCategory("cool_earth", "Cool Earth", "🪨", COOL_EARTH_THEMES),
            ColorGroupCategory("deep_earth", "Deep Earth", "🪵", DEEP_EARTH_THEMES),
            ColorGroupCategory("muted_herbal", "Gedämpfte Töne", "🌱", MUTED_HERBAL_THEMES)
        )

        val ALL_CATEGORIES = TEN_CATEGORIES + EXISTING_CATEGORIES

        // All themes combined (100 new + all existing preserved)
        val ALL_THEMES = NEW_100_THEMES + EXISTING_CLASSIC_THEMES
        val ORDERED_THEMES = ALL_THEMES

        fun fromId(id: String?): AppThemePackage {
            val clean = id?.trim() ?: return DUNKELBLAU
            if (clean.equals("weiss", ignoreCase = true) || clean.equals("weiß", ignoreCase = true)) {
                return WEISS
            }
            val normalized = clean.lowercase()
                .replace("-", "")
                .replace("_", "")
                .replace(" ", "")
                .replace("ä", "ae")
                .replace("ö", "oe")
                .replace("ü", "ue")
                .replace("ß", "ss")
            return entries.firstOrNull {
                it.id.equals(clean, ignoreCase = true) ||
                it.name.equals(clean, ignoreCase = true) ||
                it.shortName.equals(clean, ignoreCase = true) ||
                it.germanName.equals(clean, ignoreCase = true) ||
                it.id.lowercase().replace("-", "").replace("_", "").replace(" ", "") == normalized ||
                it.name.lowercase().replace("-", "").replace("_", "").replace(" ", "") == normalized ||
                it.shortName.lowercase().replace("-", "").replace("_", "").replace(" ", "")
                    .replace("ä", "ae").replace("ö", "oe").replace("ü", "ue").replace("ß", "ss") == normalized
            } ?: DUNKELBLAU
        }
    }
}

data class ColorGroupCategory(
    val id: String,
    val name: String,
    val icon: String,
    val themes: List<AppThemePackage>
)

/**
 * CompositionLocal providing the current active theme package so any composable
 * can access theme colors dynamically without requiring activity restart.
 * Default theme: SCHIEFER
 */
val LocalAppThemePackage = staticCompositionLocalOf { AppThemePackage.SCHIEFER }

/**
 * Direct accessor for current active theme package colors.
 */
object AppThemeColors {
    val current: AppThemePackage
        @Composable
        @ReadOnlyComposable
        get() = LocalAppThemePackage.current

    val primary: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppThemePackage.current.primary

    val primaryContainer: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppThemePackage.current.primaryContainer

    val background: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppThemePackage.current.background

    val surface: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppThemePackage.current.surface

    val onSurface: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppThemePackage.current.onSurface

    val onSurfaceVariant: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppThemePackage.current.onSurfaceVariant

    val border: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppThemePackage.current.border

    val outline: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppThemePackage.current.outline

    val accent: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppThemePackage.current.accent

    val isBoldTheme: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalAppThemePackage.current.isBoldTheme
}

/**
 * Deterministic ARGB hex math for multi-layered tonal palette generation.
 */
object ThemeLayerMath {
    fun blendHex(baseHex: Long, tintHex: Long, ratio: Float): Long {
        val t = ratio.coerceIn(0f, 1f)
        val inv = 1f - t
        val r1 = ((baseHex shr 16) and 0xFF).toFloat()
        val g1 = ((baseHex shr 8) and 0xFF).toFloat()
        val b1 = (baseHex and 0xFF).toFloat()

        val r2 = ((tintHex shr 16) and 0xFF).toFloat()
        val g2 = ((tintHex shr 8) and 0xFF).toFloat()
        val b2 = (tintHex and 0xFF).toFloat()

        val r = (r1 * inv + r2 * t).toInt().coerceIn(0, 255).toLong()
        val g = (g1 * inv + g2 * t).toInt().coerceIn(0, 255).toLong()
        val b = (b1 * inv + b2 * t).toInt().coerceIn(0, 255).toLong()

        return (0xFFL shl 24) or (r shl 16) or (g shl 8) or b
    }

    fun darkenHex(hex: Long, amount: Float): Long = blendHex(hex, 0xFF0A0B0E, amount)

    fun lightenHex(hex: Long, amount: Float): Long = blendHex(hex, 0xFFFFFFFF, amount)
}

val LocalTranslationLanguage = staticCompositionLocalOf { "en" }

/**
 * SharedPreferences persistence manager for selected theme package.
 */
class ThemePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SELECTED_THEME = "selected_theme"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_APP_LANGUAGE = "app_language"
        private const val KEY_TRANSLATION_LANGUAGE = "translation_language"
    }

    fun getSelectedTheme(): AppThemePackage {
        val themeName = prefs.getString(KEY_SELECTED_THEME, AppThemePackage.SCHIEFER.id)
        return AppThemePackage.fromId(themeName)
    }

    fun saveSelectedTheme(theme: AppThemePackage) {
        prefs.edit().putString(KEY_SELECTED_THEME, theme.id).apply()
    }

    fun getThemeMode(): String {
        return prefs.getString(KEY_THEME_MODE, "auto") ?: "auto"
    }

    fun saveThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
    }

    fun getAppLanguage(): String {
        return prefs.getString(KEY_APP_LANGUAGE, "de") ?: "de"
    }

    fun saveAppLanguage(languageCode: String) {
        prefs.edit().putString(KEY_APP_LANGUAGE, languageCode).apply()
    }

    fun getTranslationLanguage(): String {
        return prefs.getString(KEY_TRANSLATION_LANGUAGE, "en") ?: "en"
    }

    fun saveTranslationLanguage(languageCode: String) {
        prefs.edit().putString(KEY_TRANSLATION_LANGUAGE, languageCode).apply()
    }
}
