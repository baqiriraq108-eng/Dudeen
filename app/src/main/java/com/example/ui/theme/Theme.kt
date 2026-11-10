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
    primary = Color(theme.primaryHex),
    onPrimary = Color(theme.onPrimaryLightHex),
    primaryContainer = Color(theme.primaryHex),
    onPrimaryContainer = Color(theme.onPrimaryLightHex),
    secondary = Color(theme.secondaryHex),
    onSecondary = Color.White,
    secondaryContainer = Color(theme.surfaceVariantLightHex),
    onSecondaryContainer = Color(theme.onSurfaceLightHex),
    tertiary = Color(theme.accentHex),
    onTertiary = Color.White,
    tertiaryContainer = Color(theme.surfaceVariantLightHex),
    onTertiaryContainer = Color(theme.onSurfaceVariantLightHex),
    error = Color(0xFFFF0000), // True Red
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(theme.backgroundLightHex),
    onBackground = Color(theme.onSurfaceLightHex),
    surface = Color(theme.surfaceLightHex),
    onSurface = Color(theme.onSurfaceLightHex),
    surfaceVariant = Color(theme.surfaceVariantLightHex),
    onSurfaceVariant = Color(theme.onSurfaceVariantLightHex),
    outline = Color(theme.outlineLightHex),
    outlineVariant = Color(theme.borderHex)
)

private fun buildDarkColorScheme(theme: AppThemePackage) = darkColorScheme(
    primary = Color(theme.primaryDarkHex),
    onPrimary = Color(theme.onPrimaryDarkHex),
    primaryContainer = Color(theme.primaryDarkHex),
    onPrimaryContainer = Color(theme.onPrimaryDarkHex),
    secondary = Color(theme.secondaryHex),
    onSecondary = Color.White,
    secondaryContainer = Color(theme.surfaceVariantDarkHex),
    onSecondaryContainer = Color(theme.onSurfaceDarkHex),
    tertiary = Color(theme.accentHex),
    onTertiary = Color.White,
    tertiaryContainer = Color(theme.surfaceVariantDarkHex),
    onTertiaryContainer = Color(theme.onSurfaceVariantDarkHex),
    error = Color(0xFFFF4444), // Error Dark
    onError = Color.White,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(theme.backgroundDarkHex),
    onBackground = Color(theme.onSurfaceDarkHex),
    surface = Color(theme.surfaceDarkHex),
    onSurface = Color(theme.onSurfaceDarkHex),
    surfaceVariant = Color(theme.surfaceVariantDarkHex),
    onSurfaceVariant = Color(theme.onSurfaceVariantDarkHex),
    outline = Color(theme.outlineDarkHex),
    outlineVariant = Color(theme.borderHex)
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
