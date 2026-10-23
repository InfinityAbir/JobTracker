package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val RetroLightColorScheme = lightColorScheme(
    primary = RetroBurntOrange,
    onPrimary = RetroPaper,
    primaryContainer = RetroApricot,
    onPrimaryContainer = RetroDeepRust,
    secondary = RetroTeal,
    onSecondary = RetroPaper,
    secondaryContainer = RetroSage,
    onSecondaryContainer = RetroDeepTeal,
    tertiary = RetroMustard,
    onTertiary = RetroOliveBrown,
    tertiaryContainer = RetroButter,
    onTertiaryContainer = RetroOliveBrown,
    background = RetroCanvas,
    onBackground = RetroInk,
    surface = RetroPaper,
    onSurface = RetroInk,
    surfaceVariant = RetroSand,
    onSurfaceVariant = RetroUmber,
    surfaceTint = RetroBurntOrange,
    outline = RetroInkSoft,
    outlineVariant = RetroLine,
    error = RetroRust,
    onError = RetroPaper,
    errorContainer = RetroBlush,
    onErrorContainer = RetroDeepRust,
    inverseSurface = RetroInk,
    inverseOnSurface = RetroPaper,
    inversePrimary = RetroAmber,
    scrim = RetroInk
)

private val RetroDarkColorScheme = darkColorScheme(
    primary = RetroAmber,
    onPrimary = Color(0xFF2B1606),
    primaryContainer = RetroAmberDeep,
    onPrimaryContainer = RetroAmberSoft,
    secondary = RetroPhosphorTeal,
    onSecondary = Color(0xFF062A24),
    secondaryContainer = RetroPhosphorDeep,
    onSecondaryContainer = RetroPhosphorSoft,
    tertiary = RetroSunYellow,
    onTertiary = Color(0xFF2E2408),
    tertiaryContainer = RetroSunDeep,
    onTertiaryContainer = RetroSunSoft,
    background = RetroNightCanvas,
    onBackground = RetroNightInk,
    surface = RetroNightSurface,
    onSurface = RetroNightInk,
    surfaceVariant = RetroNightSurfaceVariant,
    onSurfaceVariant = RetroNightMuted,
    surfaceTint = RetroAmber,
    outline = RetroNightOutline,
    outlineVariant = RetroNightLine,
    error = RetroNightError,
    onError = Color(0xFF2B0B05),
    errorContainer = RetroNightErrorDeep,
    onErrorContainer = RetroNightErrorSoft,
    inverseSurface = RetroNightInk,
    inverseOnSurface = RetroNightCanvas,
    inversePrimary = RetroBurntOrange,
    scrim = RetroNightCanvas
)

// Small, squarish corners keep the print-shop feel without looking like generic Material.
private val RetroShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(3.dp),
    medium = RoundedCornerShape(5.dp),
    large = RoundedCornerShape(7.dp),
    extraLarge = RoundedCornerShape(10.dp)
)

@Composable
fun JobTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Dynamic colour is deliberately ignored: the retro palette is the identity.
    val colorScheme = if (darkTheme) RetroDarkColorScheme else RetroLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = RetroShapes,
        content = content
    )
}
