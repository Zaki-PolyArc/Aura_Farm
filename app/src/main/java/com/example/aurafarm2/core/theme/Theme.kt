package com.example.aurafarm2.core.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary              = Color(0xFFFFFFFF),       
    onPrimary            = Color(0xFF2F3131),
    primaryContainer     = Color(0xFFE2E2E2),
    onPrimaryContainer   = Color(0xFF636565),
    inversePrimary       = Color(0xFF5D5F5F),

    secondary            = Color(0xFFC5C6D1),       
    onSecondary          = Color(0xFF2E3039),
    secondaryContainer   = Color(0xFF474952),
    onSecondaryContainer = Color(0xFFB7B8C2),

    tertiary             = Color(0xFFFFFFFF),       
    onTertiary           = Color(0xFF1000A9),
    tertiaryContainer    = Color(0xFFE1E0FF),
    onTertiaryContainer  = Color(0xFF4F51DD),

    error                = Color(0xFFFFB4AB),
    onError              = Color(0xFF690005),
    errorContainer       = Color(0xFF93000A),
    onErrorContainer     = Color(0xFFFFDAD6),

    background           = Color(0xFF0F1115),
    onBackground         = Color(0xFFE2E2E8),

    surface              = Color(0xFF111317),
    onSurface            = Color(0xFFE2E2E8),
    onSurfaceVariant     = Color(0xFFC4C7C8),
    surfaceVariant       = Color(0xFF333539),

    inverseSurface       = Color(0xFFE2E2E8),
    inverseOnSurface     = Color(0xFF2F3035),

    outline              = Color(0xFF8E9192),
    outlineVariant       = Color(0xFF444748),

    surfaceTint          = Color(0xFFC6C6C7),
    scrim                = Color(0xFF0F1115),
)

// We'll use the same color scheme for light mode to enforce the "True Black" UI
private val LightColorScheme = DarkColorScheme

@Composable
fun AppTheme(
    appearance: String = "Dark",
    content: @Composable () -> Unit
) {
    val darkTheme = true // Enforce dark mode for Kinetic Obsidian
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars     = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        content     = content
    )
}

