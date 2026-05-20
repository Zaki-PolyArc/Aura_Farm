package com.example.aurafarm2.core.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary              = Primary,
    onPrimary            = OnPrimary,
    primaryContainer     = PrimaryContainer,
    onPrimaryContainer   = OnPrimaryContainer,
    inversePrimary       = InversePrimary,

    secondary            = Secondary,
    onSecondary          = OnSecondary,
    secondaryContainer   = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,

    tertiary             = Tertiary,
    onTertiary           = OnTertiary,
    tertiaryContainer    = TertiaryContainer,
    onTertiaryContainer  = OnTertiaryContainer,

    error                = Error,
    onError              = OnError,
    errorContainer       = ErrorContainer,
    onErrorContainer     = OnErrorContainer,

    background           = Background,
    onBackground         = OnSurface,

    surface              = SurfaceContainer,
    onSurface            = OnSurface,
    onSurfaceVariant     = OnSurfaceVariant,
    surfaceVariant       = SurfaceVariant,

    inverseSurface       = InverseSurface,
    inverseOnSurface     = InverseOnSurface,

    outline              = Outline,
    outlineVariant       = OutlineVariant,

    surfaceTint          = SurfaceTint,
    scrim                = Background,
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars     = false
                isAppearanceLightNavigationBars = false
            }
        }
    }
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = AppTypography,
        content     = content
    )
}
