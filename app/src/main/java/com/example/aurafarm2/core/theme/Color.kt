package com.example.aurafarm2.core.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.material3.MaterialTheme

// ── Raw Static Values for Dark Theme ────────────────────────────
private val RawBackground              = Color(0xFF0F1115)
private val RawSurfaceDim              = Color(0xFF111317)
private val RawSurfaceContainerLowest  = Color(0xFF0C0E12)
private val RawSurfaceContainerLow     = Color(0xFF1A1C20)
private val RawSurfaceContainer        = Color(0xFF1E2024)
private val RawSurfaceContainerHigh    = Color(0xFF282A2E)
private val RawSurfaceContainerHighest = Color(0xFF333539)
private val RawSurfaceBright           = Color(0xFF37393E)
private val RawSurfaceVariant          = Color(0xFF333539)

private val RawOnSurface               = Color(0xFFE2E2E8)
private val RawOnSurfaceVariant        = Color(0xFFC4C7C8)
private val RawInverseSurface          = Color(0xFFE2E2E8)
private val RawInverseOnSurface        = Color(0xFF2F3035)

private val RawOutline                 = Color(0xFF8E9192)
private val RawOutlineVariant          = Color(0xFF444748)

private val RawPrimary                 = Color(0xFFFFFFFF)
private val RawOnPrimary               = Color(0xFF2F3131)
private val RawPrimaryContainer        = Color(0xFFE2E2E2)
private val RawOnPrimaryContainer      = Color(0xFF636565)
private val RawInversePrimary          = Color(0xFF5D5F5F)
private val RawSurfaceTint             = Color(0xFFC6C6C7)

private val RawPrimaryFixed            = Color(0xFFE2E2E2)
private val RawPrimaryFixedDim         = Color(0xFFC6C6C7)
private val RawOnPrimaryFixed          = Color(0xFF1A1C1C)
private val RawOnPrimaryFixedVariant   = Color(0xFF454747)

private val RawSecondary               = Color(0xFFC5C6D1)
private val RawOnSecondary             = Color(0xFF2E3039)
private val RawSecondaryContainer      = Color(0xFF474952)
private val RawOnSecondaryContainer    = Color(0xFFB7B8C2)

private val RawSecondaryFixed          = Color(0xFFE1E1ED)
private val RawSecondaryFixedDim       = Color(0xFFC5C6D1)
private val RawOnSecondaryFixed        = Color(0xFF191B23)
private val RawOnSecondaryFixedVariant = Color(0xFF45464F)

private val RawTertiary                = Color(0xFFFFFFFF)
private val RawOnTertiary              = Color(0xFF1000A9)
private val RawTertiaryContainer       = Color(0xFFE1E0FF)
private val RawOnTertiaryContainer     = Color(0xFF4F51DD)

private val RawTertiaryFixed           = Color(0xFFE1E0FF)
private val RawTertiaryFixedDim        = Color(0xFFC0C1FF)
private val RawOnTertiaryFixed         = Color(0xFF07006C)
private val RawOnTertiaryFixedVariant  = Color(0xFF2F2EBE)

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
    @Composable @ReadOnlyComposable get() = if (isLightTheme()) Color(0xFF404040) else Color(0xFFFFFFFF)

val LuxuryDot: Color
    @Composable @ReadOnlyComposable get() = if (isLightTheme()) Color(0xFF808080) else Color(0xFFB0B0B0)

val ExtraDot: Color
    @Composable @ReadOnlyComposable get() = if (isLightTheme()) Color(0xFFB0B0B0) else Color(0xFF606060)

val SalaryDot: Color
    @Composable @ReadOnlyComposable get() = if (isLightTheme()) Color(0xFF404040) else Color(0xFFFFFFFF)

val FreelanceDot: Color
    @Composable @ReadOnlyComposable get() = if (isLightTheme()) Color(0xFF808080) else Color(0xFFB0B0B0)

val InvestmentDot: Color
    @Composable @ReadOnlyComposable get() = if (isLightTheme()) Color(0xFFB0B0B0) else Color(0xFF606060)

val Divider: Color
    @Composable @ReadOnlyComposable get() = if (isLightTheme()) Color(0x333C352D) else Color(0x80393840)

val FabBackground: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primaryContainer

val FabIcon: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onPrimaryContainer

