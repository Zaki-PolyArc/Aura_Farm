package com.example.aurafarm2.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── NOTE ───────────────────────────────────────────────────────
// The design calls for Geist, Manrope, and Hanken Grotesk.
// Since these are not bundled in Android by default, we use
// downloadable fonts via Google Fonts in the manifest, OR
// we fall back to system sans-serif here until the font files
// are placed in res/font/. Replace FontFamily.Default below
// with your actual font families once the TTF files are added.
//
// To add fonts:
// 1. Download from fonts.google.com:
//    - Manrope (all weights)
//    - Hanken Grotesk (all weights)
//    - Geist (light, thin) — available at vercel.com/font
// 2. Place .ttf files in app/src/main/res/font/
// 3. Replace FontFamily.Default below with FontFamily(Font(...))
// ──────────────────────────────────────────────────────────────

// Placeholder families — swap once TTFs are in res/font/
val GeistFamily = FontFamily.Default   // → replace with Geist
val ManropeFamily = FontFamily.Default   // → replace with Manrope
val HankenFamily = FontFamily.Default   // → replace with Hanken Grotesk

val AppTypography = Typography(

    // Display — used for the big $3,420.50 stat number
    // Geist ExtraLight, 48sp, tracking -0.02em
    displayLarge = TextStyle(
        fontFamily = GeistFamily,
        fontWeight = FontWeight.ExtraLight,   // W200
        fontSize = 48.sp,
        lineHeight = 56.sp,
        letterSpacing = (-0.96).sp            // -0.02em at 48sp
    ),

    // displayMedium — secondary stat numbers
    displayMedium = TextStyle(
        fontFamily = GeistFamily,
        fontWeight = FontWeight.Light,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.72).sp
    ),

    displaySmall = TextStyle(
        fontFamily = GeistFamily,
        fontWeight = FontWeight.Light,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.56).sp
    ),

    // Headline — section titles like "Allocation", "Recent Activity"
    // Manrope SemiBold, 24sp
    headlineLarge = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.24).sp            // -0.01em at 24sp
    ),

    // "Focus" top bar title
    headlineMedium = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),

    headlineSmall = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),

    // Title — transaction names like "Whole Market"
    titleLarge = TextStyle(
        fontFamily = HankenFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),

    titleMedium = TextStyle(
        fontFamily = HankenFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),

    titleSmall = TextStyle(
        fontFamily = HankenFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    ),

    // Body — subtitles like "Groceries", "Travel"
    bodyLarge = TextStyle(
        fontFamily = HankenFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),

    bodyMedium = TextStyle(
        fontFamily = HankenFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),

    bodySmall = TextStyle(
        fontFamily = HankenFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),

    // Label — "MONTHLY NET", category labels in small caps style
    // Hanken Grotesk SemiBold 12sp, tracking +0.05em
    labelLarge = TextStyle(
        fontFamily = HankenFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.6.sp               // +0.05em at 12sp
    ),

    labelMedium = TextStyle(
        fontFamily = HankenFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.55.sp
    ),

    labelSmall = TextStyle(
        fontFamily = HankenFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 13.sp,
        letterSpacing = 0.5.sp
    )
)
