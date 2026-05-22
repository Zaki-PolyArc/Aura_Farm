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
    primary              = Color(0xFFF6E7CE),       // warm sand
    onPrimary            = Color(0xFF382F1F),
    primaryContainer     = Color(0xFFD9CBB3),
    onPrimaryContainer   = Color(0xFF5F5542),
    inversePrimary       = Color(0xFF675D4A),

    secondary            = Color(0xFFD4C4B3),       // taupe
    onSecondary          = Color(0xFF382F23),
    secondaryContainer   = Color(0xFF504538),
    onSecondaryContainer = Color(0xFFC2B3A3),

    tertiary             = Color(0xFFEBE7F1),       // soft lavender-grey
    onTertiary           = Color(0xFF302F37),
    tertiaryContainer    = Color(0xFFCECBD5),
    onTertiaryContainer  = Color(0xFF57555E),

    error                = Color(0xFFFFB4AB),
    onError              = Color(0xFF690005),
    errorContainer       = Color(0xFF93000A),
    onErrorContainer     = Color(0xFFFFDAD6),

    background           = Color(0xFF13131A),
    onBackground         = Color(0xFFE4E1EB),

    surface              = Color(0xFF1F1F26),
    onSurface            = Color(0xFFE4E1EB),
    onSurfaceVariant     = Color(0xFFCEC5BA),
    surfaceVariant       = Color(0xFF34343C),

    inverseSurface       = Color(0xFFE4E1EB),
    inverseOnSurface     = Color(0xFF303037),

    outline              = Color(0xFF979085),
    outlineVariant       = Color(0xFF4B463D),

    surfaceTint          = Color(0xFFD3C5AD),
    scrim                = Color(0xFF13131A),
)

private val LightColorScheme = lightColorScheme(
    primary              = Color(0xFF7A6A53),       // luxurious deep warm sand
    onPrimary            = Color(0xFFFFFDF9),       // warm cream
    primaryContainer     = Color(0xFFF4EAD4),       // sand primary container
    onPrimaryContainer   = Color(0xFF5F513A),       // dark warm brown
    inversePrimary       = Color(0xFFF6E7CE),

    secondary            = Color(0xFF6F624E),       // muted warm brown
    onSecondary          = Color(0xFFFFFFFF),
    secondaryContainer   = Color(0xFFF5EFEB),
    onSecondaryContainer = Color(0xFF382F23),

    tertiary             = Color(0xFF6A607A),       // muted greyish lavender
    onTertiary           = Color(0xFFFFFFFF),
    tertiaryContainer    = Color(0xFFECE7F2),
    onTertiaryContainer  = Color(0xFF241B32),

    error                = Color(0xFFBA1A1A),
    onError              = Color(0xFFFFFFFF),
    errorContainer       = Color(0xFFFFDAD6),
    onErrorContainer     = Color(0xFF410002),

    background           = Color(0xFFFFFDF9),       // very soft off-white/warm cream
    onBackground         = Color(0xFF3C352D),       // rich brown text

    surface              = Color(0xFFF7F2E9),       // warm sand container
    onSurface            = Color(0xFF3C352D),
    onSurfaceVariant     = Color(0xFF7E7262),
    surfaceVariant       = Color(0xFFECE1D3),

    inverseSurface       = Color(0xFF303037),
    inverseOnSurface     = Color(0xFFFAF7F2),

    outline              = Color(0xFF8F806E),
    outlineVariant       = Color(0xFFD4C7B5),

    surfaceTint          = Color(0xFF7A6A53),
    scrim                = Color(0xFF3C352D),
)

@Composable
fun AppTheme(
    appearance: String = "Dark",
    content: @Composable () -> Unit
) {
    val darkTheme = when (appearance) {
        "Light" -> false
        "Dark" -> true
        else -> isSystemInDarkTheme()
    }

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

