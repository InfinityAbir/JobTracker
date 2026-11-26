package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

/*
 * Retro type pairing:
 * - Serif display faces for headlines and record titles (vintage print).
 * - Monospace for labels, data and metadata (letterpress / terminal).
 * - Sans for running text and form input.
 *
 * All three families are bundled (res/font, SIL OFL) instead of using FontFamily.Serif /
 * Monospace / SansSerif. Those generic families resolve to whatever the OEM or the user's
 * system-font setting provides, so the same screen used to look different on every phone.
 * Bengali glyphs are not in these fonts and still fall back to the system Bengali font.
 */
private val WEIGHTS = listOf(
    FontWeight.Normal,
    FontWeight.Medium,
    FontWeight.SemiBold,
    FontWeight.Bold,
    FontWeight.Black
)

private fun variableFamily(resId: Int): FontFamily = FontFamily(
    WEIGHTS.map { weight ->
        Font(
            resId = resId,
            weight = weight,
            variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight))
        )
    }
)

val RetroDisplay: FontFamily = variableFamily(R.font.playfair_display_variable)
val RetroMono: FontFamily = variableFamily(R.font.jetbrains_mono_variable)
val RetroBody: FontFamily = variableFamily(R.font.inter_variable)

val Typography = Typography(
    displaySmall = TextStyle(
        fontFamily = RetroDisplay,
        fontWeight = FontWeight.Black,
        fontSize = 34.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.6).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = RetroDisplay,
        fontWeight = FontWeight.Black,
        fontSize = 27.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.3).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = RetroDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.1).sp
    ),
    titleLarge = TextStyle(
        fontFamily = RetroDisplay,
        fontWeight = FontWeight.Black,
        fontSize = 22.sp,
        lineHeight = 27.sp,
        letterSpacing = (-0.2).sp
    ),
    titleMedium = TextStyle(
        fontFamily = RetroDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 23.sp,
        letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontFamily = RetroMono,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.7.sp
    ),
    // Running text (descriptions, notes, form input) uses the readable system sans;
    // mono stays reserved for labels, dates, counters and other metadata.
    bodyLarge = TextStyle(
        fontFamily = RetroBody,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = RetroBody,
        fontWeight = FontWeight.Normal,
        fontSize = 13.5.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodySmall = TextStyle(
        fontFamily = RetroBody,
        fontWeight = FontWeight.Normal,
        fontSize = 12.5.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    ),
    labelLarge = TextStyle(
        fontFamily = RetroMono,
        fontWeight = FontWeight.Bold,
        fontSize = 12.5.sp,
        lineHeight = 17.sp,
        letterSpacing = 1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = RetroMono,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.8.sp
    ),
    labelSmall = TextStyle(
        fontFamily = RetroMono,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.7.sp
    )
)

/** Optional italic caption style for editorial flourishes. */
val RetroCaption = TextStyle(
    fontFamily = RetroDisplay,
    fontWeight = FontWeight.Medium,
    fontStyle = FontStyle.Italic,
    fontSize = 13.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.sp
)
