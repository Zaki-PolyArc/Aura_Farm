package com.example.aurafarm2.core.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.material3.MaterialTheme

// ── Raw Static Values for Dark Theme ────────────────────────────
private val RawBackground              = Color(0xFF13131A)
private val RawSurfaceDim              = Color(0xFF13131A)
private val RawSurfaceContainerLowest  = Color(0xFF0D0E14)
private val RawSurfaceContainerLow     = Color(0xFF1B1B22)
private val RawSurfaceContainer        = Color(0xFF1F1F26)
private val RawSurfaceContainerHigh    = Color(0xFF2A2931)
private val RawSurfaceContainerHighest = Color(0xFF34343C)
private val RawSurfaceBright           = Color(0xFF393840)
private val RawSurfaceVariant          = Color(0xFF34343C)

private val RawOnSurface               = Color(0xFFE4E1EB)
private val RawOnSurfaceVariant        = Color(0xFFCEC5BA)
private val RawInverseSurface          = Color(0xFFE4E1EB)
private val RawInverseOnSurface        = Color(0xFF303037)

private val RawOutline                 = Color(0xFF979085)
private val RawOutlineVariant          = Color(0xFF4B463D)

private val RawPrimary                 = Color(0xFFF6E7CE)
private val RawOnPrimary               = Color(0xFF382F1F)
private val RawPrimaryContainer        = Color(0xFFD9CBB3)
private val RawOnPrimaryContainer      = Color(0xFF5F5542)
private val RawInversePrimary          = Color(0xFF675D4A)
private val RawSurfaceTint             = Color(0xFFD3C5AD)

private val RawPrimaryFixed            = Color(0xFFEFE1C8)
private val RawPrimaryFixedDim         = Color(0xFFD3C5AD)
private val RawOnPrimaryFixed          = Color(0xFF221B0B)
private val RawOnPrimaryFixedVariant   = Color(0xFF4F4634)

private val RawSecondary               = Color(0xFFD4C4B3)
private val RawOnSecondary             = Color(0xFF382F23)
private val RawSecondaryContainer      = Color(0xFF504538)
private val RawOnSecondaryContainer    = Color(0xFFC2B3A3)

private val RawSecondaryFixed          = Color(0xFFF1E0CF)
private val RawSecondaryFixedDim       = Color(0xFFD4C4B3)
private val RawOnSecondaryFixed        = Color(0xFF221A10)
private val RawOnSecondaryFixedVariant = Color(0xFF504538)

private val RawTertiary                = Color(0xFFEBE7F1)
private val RawOnTertiary              = Color(0xFF302F37)
private val RawTertiaryContainer       = Color(0xFFCECBD5)
private val RawOnTertiaryContainer     = Color(0xFF57555E)

private val RawTertiaryFixed           = Color(0xFFE4E1EB)
private val RawTertiaryFixedDim        = Color(0xFFC8C5CF)
private val RawOnTertiaryFixed         = Color(0xFF1B1B22)
private val RawOnTertiaryFixedVariant  = Color(0xFF47464E)

private val RawError                   = Color(0xFFFFB4AB)
private val RawOnError                 = Color(0xFF690005)
private val RawErrorContainer          = Color(0xFF93000A)
private val RawOnErrorContainer        = Color(0xFFFFDAD6)

// ── Composable properties mapping to active theme ───────────────
val Background: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.background

val SurfaceDim: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceDim

val SurfaceContainerLowest: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainerLowest

val SurfaceContainerLow: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainerLow

val SurfaceContainer: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainer

val SurfaceContainerHigh: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainerHigh

val SurfaceContainerHighest: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainerHighest

val SurfaceBright: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceBright

val SurfaceVariant: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceVariant

val OnSurface: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurface

val OnSurfaceVariant: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant

val InverseSurface: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.inverseSurface

val InverseOnSurface: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.inverseOnSurface

val Outline: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.outline

val OutlineVariant: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.outlineVariant

val Primary: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary

val OnPrimary: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onPrimary

val PrimaryContainer: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primaryContainer

val OnPrimaryContainer: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onPrimaryContainer

val InversePrimary: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.inversePrimary

val SurfaceTint: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceTint

val PrimaryFixed: Color
    @Composable @ReadOnlyComposable get() = RawPrimaryFixed

val PrimaryFixedDim: Color
    @Composable @ReadOnlyComposable get() = RawPrimaryFixedDim

val OnPrimaryFixed: Color
    @Composable @ReadOnlyComposable get() = RawOnPrimaryFixed

val OnPrimaryFixedVariant: Color
    @Composable @ReadOnlyComposable get() = RawOnPrimaryFixedVariant

val Secondary: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.secondary

val OnSecondary: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSecondary

val SecondaryContainer: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.secondaryContainer

val OnSecondaryContainer: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSecondaryContainer

val SecondaryFixed: Color
    @Composable @ReadOnlyComposable get() = RawSecondaryFixed

val SecondaryFixedDim: Color
    @Composable @ReadOnlyComposable get() = RawSecondaryFixedDim

val OnSecondaryFixed: Color
    @Composable @ReadOnlyComposable get() = RawOnSecondaryFixed

val OnSecondaryFixedVariant: Color
    @Composable @ReadOnlyComposable get() = RawOnSecondaryFixedVariant

val Tertiary: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.tertiary

val OnTertiary: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onTertiary

val TertiaryContainer: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.tertiaryContainer

val OnTertiaryContainer: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onTertiaryContainer

val TertiaryFixed: Color
    @Composable @ReadOnlyComposable get() = RawTertiaryFixed

val TertiaryFixedDim: Color
    @Composable @ReadOnlyComposable get() = RawTertiaryFixedDim

val OnTertiaryFixed: Color
    @Composable @ReadOnlyComposable get() = RawOnTertiaryFixed

val OnTertiaryFixedVariant: Color
    @Composable @ReadOnlyComposable get() = RawOnTertiaryFixedVariant

val Error: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.error

val OnError: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onError

val ErrorContainer: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.errorContainer

val OnErrorContainer: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onErrorContainer

@Composable
@ReadOnlyComposable
private fun isLightTheme(): Boolean {
    val bg = MaterialTheme.colorScheme.background
    return bg.red * 0.2126f + bg.green * 0.7152f + bg.blue * 0.0722f > 0.5f
}

// ── Semantic — allocation dots ─────────────────────────────────
val EssentialDot: Color
    @Composable @ReadOnlyComposable get() = if (isLightTheme()) Color(0xFF8A7961) else Color(0xFFD9CBB3)

val LuxuryDot: Color
    @Composable @ReadOnlyComposable get() = if (isLightTheme()) Color(0xFF5D6E61) else Color(0xFF8A9B8E)

val ExtraDot: Color
    @Composable @ReadOnlyComposable get() = if (isLightTheme()) Color(0xFF8C6969) else Color(0xFFB08D8D)

val SalaryDot: Color
    @Composable @ReadOnlyComposable get() = if (isLightTheme()) Color(0xFF8A7961) else Color(0xFFD9CBB3)

val FreelanceDot: Color
    @Composable @ReadOnlyComposable get() = if (isLightTheme()) Color(0xFF5D6E61) else Color(0xFF8A9B8E)

val InvestmentDot: Color
    @Composable @ReadOnlyComposable get() = if (isLightTheme()) Color(0xFF8C6969) else Color(0xFFB08D8D)

val Divider: Color
    @Composable @ReadOnlyComposable get() = if (isLightTheme()) Color(0x333C352D) else Color(0x80393840)

val FabBackground: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primaryContainer

val FabIcon: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onPrimaryContainer

