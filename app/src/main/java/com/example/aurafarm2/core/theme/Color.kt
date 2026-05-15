package com.example.aurafarm2.core.theme

import androidx.compose.ui.graphics.Color

// ── Base surfaces ──────────────────────────────────────────────
val Background = Color(0xFF13131A)
val SurfaceDim = Color(0xFF13131A)
val SurfaceContainerLowest = Color(0xFF0E0E14)
val SurfaceContainerLow = Color(0xFF1B1B22)
val SurfaceContainer = Color(0xFF1F1F26)
val SurfaceContainerHigh = Color(0xFF2A2931)
val SurfaceContainerHighest = Color(0xFF34343C)
val SurfaceBright = Color(0xFF393840)
val SurfaceVariant = Color(0xFF34343C)

// ── On-surface text ────────────────────────────────────────────
val OnSurface = Color(0xFFE4E1EB)
val OnSurfaceVariant = Color(0xFFC6C5D5)
val InverseSurface = Color(0xFFE4E1EB)
val InverseOnSurface = Color(0xFF303037)

// ── Outline ────────────────────────────────────────────────────
val Outline = Color(0xFF908F9E)
val OutlineVariant = Color(0xFF454653)

// ── Primary — Soft Indigo ──────────────────────────────────────
val Primary = Color(0xFFBDC2FF)   // light indigo (text on dark)
val OnPrimary = Color(0xFF131E8C)
val PrimaryContainer = Color(0xFF818CF8)   // mid indigo (buttons, glows)
val OnPrimaryContainer = Color(0xFF101B8A)
val InversePrimary = Color(0xFF4953BC)
val SurfaceTint = Color(0xFFBDC2FF)

// Primary fixed
val PrimaryFixed = Color(0xFFE0E0FF)
val PrimaryFixedDim = Color(0xFFBDC2FF)
val OnPrimaryFixed = Color(0xFF000767)
val OnPrimaryFixedVariant = Color(0xFF2F3AA3)

// ── Secondary — Muted Cyan ─────────────────────────────────────
val Secondary = Color(0xFF5DE6FF)
val OnSecondary = Color(0xFF00363E)
val SecondaryContainer = Color(0xFF00CBE6)
val OnSecondaryContainer = Color(0xFF00515D)

// Secondary fixed
val SecondaryFixed = Color(0xFFA2EEFF)
val SecondaryFixedDim = Color(0xFF2FD9F4)
val OnSecondaryFixed = Color(0xFF001F25)
val OnSecondaryFixedVariant = Color(0xFF004E5A)

// ── Tertiary — Soft Purple ─────────────────────────────────────
val Tertiary = Color(0xFFDDB8FF)
val OnTertiary = Color(0xFF490081)
val TertiaryContainer = Color(0xFFB67AF1)
val OnTertiaryContainer = Color(0xFF46007B)

// Tertiary fixed
val TertiaryFixed = Color(0xFFF0DBFF)
val TertiaryFixedDim = Color(0xFFDDB8FF)
val OnTertiaryFixed = Color(0xFF2C0051)
val OnTertiaryFixedVariant = Color(0xFF62259B)

// ── Error ──────────────────────────────────────────────────────
val Error = Color(0xFFFFB4AB)
val OnError = Color(0xFF690005)
val ErrorContainer = Color(0xFF93000A)
val OnErrorContainer = Color(0xFFFFDAD6)

// ── Semantic shortcuts used throughout the UI ──────────────────
val IncomeGreen = Color(0xFF5DE6FF)   // cyan dot for income
val ExpenseRed = Color(0xFFFFB4AB)   // soft red dot for expense
val EssentialDot = Color(0xFF818CF8)   // indigo
val LuxuryDot = Color(0xFF5DE6FF)   // cyan
val ExtraDot = Color(0xFFDDB8FF)   // purple

// ── Glow helpers (used with alpha in code) ─────────────────────
val GlowIndigo = Color(0xFF818CF8)   // use at 0.15–0.20 alpha
val GlowCyan = Color(0xFF5DE6FF)   // use at 0.15 alpha

// ── Divider ────────────────────────────────────────────────────
val Divider = Color(0x1AFFFFFF)   // 10% white
