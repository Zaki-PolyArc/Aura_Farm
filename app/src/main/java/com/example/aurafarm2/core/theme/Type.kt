package com.example.aurafarm2.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── NOTE ───────────────────────────────────────────────────────
// This design system uses Manrope exclusively.
// Download Manrope from fonts.google.com, place all weight TTFs
// in app/src/main/res/font/, then replace FontFamily.Default.
//
// Weights needed: ExtraLight(200), Light(300), Regular(400),
//                 Medium(500), SemiBold(600)
// ──────────────────────────────────────────────────────────────

val ManropeFamily = FontFamily.Default   // → replace with Manrope

val AppTypography = Typography(

    // display-stat: 48sp W200 — hero numbers ($12,450.00)
    displayLarge = TextStyle(
        fontFamily    = ManropeFamily,
        fontWeight    = FontWeight.ExtraLight,
        fontSize      = 48.sp,
        lineHeight    = 56.sp,
        letterSpacing = (-0.96).sp            // -0.02em
    ),

    // display-stat-mobile: 36sp W200
    displayMedium = TextStyle(
        fontFamily    = ManropeFamily,
        fontWeight    = FontWeight.ExtraLight,
        fontSize      = 36.sp,
        lineHeight    = 44.sp,
        letterSpacing = (-0.72).sp
    ),

    displaySmall = TextStyle(
        fontFamily    = ManropeFamily,
        fontWeight    = FontWeight.Light,
        fontSize      = 28.sp,
        lineHeight    = 36.sp,
        letterSpacing = (-0.56).sp
    ),

    // headline-lg: 32sp W300 — "Income Streams", "Breakdown"
    headlineLarge = TextStyle(
        fontFamily    = ManropeFamily,
        fontWeight    = FontWeight.Light,
        fontSize      = 32.sp,
        lineHeight    = 40.sp,
        letterSpacing = 0.sp
    ),

    // headline-md: 24sp W400 — "Allocation", "Recent Activity"
    headlineMedium = TextStyle(
        fontFamily    = ManropeFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 24.sp,
        lineHeight    = 32.sp,
        letterSpacing = 0.sp
    ),

    // top bar title "Focus"
    headlineSmall = TextStyle(
        fontFamily    = ManropeFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 20.sp,
        lineHeight    = 28.sp,
        letterSpacing = 0.sp
    ),

    // body-lg: 18sp W400 — income stream names "Salary"
    titleLarge = TextStyle(
        fontFamily    = ManropeFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 18.sp,
        lineHeight    = 28.sp,
        letterSpacing = 0.sp
    ),

    // body-md: 16sp W400 — transaction names, subtitles
    titleMedium = TextStyle(
        fontFamily    = ManropeFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 16.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.sp
    ),

    titleSmall = TextStyle(
        fontFamily    = ManropeFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.sp
    ),

    bodyLarge = TextStyle(
        fontFamily    = ManropeFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 16.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.sp
    ),

    bodyMedium = TextStyle(
        fontFamily    = ManropeFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.sp
    ),

    bodySmall = TextStyle(
        fontFamily    = ManropeFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.sp
    ),

    // label-bold: 14sp W600 tracking +0.05em — "MONTHLY NET BALANCE", "FILTER"
    labelLarge = TextStyle(
        fontFamily    = ManropeFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.7.sp               // +0.05em at 14sp
    ),

    // label-sm: 12sp W500
    labelMedium = TextStyle(
        fontFamily    = ManropeFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.sp
    ),

    labelSmall = TextStyle(
        fontFamily    = ManropeFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 11.sp,
        lineHeight    = 14.sp,
        letterSpacing = 0.sp
    )
)
