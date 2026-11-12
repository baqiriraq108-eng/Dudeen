package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalIsDarkTheme = staticCompositionLocalOf { false }

private fun buildLightColorScheme(theme: AppThemePackage) = lightColorScheme(
    primary = theme.tier1Primary(isDark = false),
    onPrimary = Color(theme.onPrimaryLightHex),
    primaryContainer = theme.tier1Container(isDark = false),
    onPrimaryContainer = theme.onTier1Container(isDark = false),
    secondary = theme.tier2Secondary(isDark = false),
    onSecondary = Color.White,
    secondaryContainer = theme.tier2Container(isDark = false),
    onSecondaryContainer = theme.onTier2Container(isDark = false),
    tertiary = theme.tier3Tertiary(isDark = false),
    onTertiary = Color.White,
    tertiaryContainer = theme.tier3Container(isDark = false),
    onTertiaryContainer = theme.onTier3Container(isDark = false),
    error = Color(0xFFC0392B),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(theme.backgroundLightHex),
    onBackground = Color(theme.onSurfaceLightHex),
    surface = Color(theme.surfaceLightHex),
    onSurface = Color(theme.onSurfaceLightHex),
    surfaceVariant = theme.tier4Quaternary(isDark = false),
    onSurfaceVariant = Color(theme.onSurfaceVariantLightHex),
    surfaceContainerLowest = theme.tier4SurfaceLowest(isDark = false),
    surfaceContainerLow = theme.tier4SurfaceLow(isDark = false),
    surfaceContainer = theme.tier4SurfaceContainer(isDark = false),
    surfaceContainerHigh = theme.tier4SurfaceHigh(isDark = false),
    surfaceContainerHighest = theme.tier4SurfaceHighest(isDark = false),
    outline = Color(theme.outlineLightHex),
    outlineVariant = theme.tier4Border(isDark = false)
)

private fun buildDarkColorScheme(theme: AppThemePackage) = darkColorScheme(
    primary = theme.tier1Primary(isDark = true),
    onPrimary = Color(theme.onPrimaryDarkHex),
    primaryContainer = theme.tier1Container(isDark = true),
    onPrimaryContainer = theme.onTier1Container(isDark = true),
    secondary = theme.tier2Secondary(isDark = true),
    onSecondary = Color.White,
    secondaryContainer = theme.tier2Container(isDark = true),
    onSecondaryContainer = theme.onTier2Container(isDark = true),
    tertiary = theme.tier3Tertiary(isDark = true),
    onTertiary = Color.White,
    tertiaryContainer = theme.tier3Container(isDark = true),
    onTertiaryContainer = theme.onTier3Container(isDark = true),
    error = Color(0xFFE74C3C),
    onError = Color.White,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(theme.backgroundDarkHex),
    onBackground = Color(theme.onSurfaceDarkHex),
    surface = Color(theme.surfaceDarkHex),
    onSurface = Color(theme.onSurfaceDarkHex),
    surfaceVariant = theme.tier4Quaternary(isDark = true),
    onSurfaceVariant = Color(theme.onSurfaceVariantDarkHex),
    surfaceContainerLowest = theme.tier4SurfaceLowest(isDark = true),
    surfaceContainerLow = theme.tier4SurfaceLow(isDark = true),
    surfaceContainer = theme.tier4SurfaceContainer(isDark = true),
    surfaceContainerHigh = theme.tier4SurfaceHigh(isDark = true),
    surfaceContainerHighest = theme.tier4SurfaceHighest(isDark = true),
    outline = Color(theme.outlineDarkHex),
    outlineVariant = theme.tier4Border(isDark = true)
)

@Composable
fun MyApplicationTheme(
    selectedTheme: AppThemePackage = AppThemePackage.SCHIEFER,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> buildDarkColorScheme(selectedTheme)
        else -> buildLightColorScheme(selectedTheme)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            try {
                var ctx = view.context
                while (ctx is android.content.ContextWrapper && ctx !is Activity) {
                    ctx = ctx.baseContext
                }
                val window = (ctx as? Activity)?.window
                if (window != null) {
                    val insetsController = WindowCompat.getInsetsController(window, view)
                    insetsController.isAppearanceLightStatusBars = !darkTheme
                    insetsController.isAppearanceLightNavigationBars = !darkTheme
                }
            } catch (_: Throwable) {
                // Safe ignore for tests and custom context wrappers
            }
        }
    }

    CompositionLocalProvider(
        LocalAppThemePackage provides selectedTheme,
        LocalIsDarkTheme provides darkTheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
