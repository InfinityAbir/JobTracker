package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = VioletPrimaryDark,
    onPrimary = VioletOnPrimaryDark,
    primaryContainer = VioletContainerDark,
    onPrimaryContainer = VioletOnContainerDark,
    secondary = CyanSecondaryDark,
    onSecondary = CyanOnSecondaryDark,
    secondaryContainer = NavyContainerDark,
    onSecondaryContainer = CyanOnSecondaryContainerDark,
    tertiary = RoseTertiaryDark,
    onTertiary = RoseOnTertiaryDark,
    tertiaryContainer = RoseContainerDark,
    onTertiaryContainer = RoseOnTertiaryContainerDark,
    background = NavyBackgroundDark,
    onBackground = OnNavyBackgroundDark,
    surface = NavySurfaceDark,
    onSurface = OnNavySurfaceDark,
    surfaceVariant = NavyContainerDark,
    onSurfaceVariant = OnNavyVariantDark,
    outline = NavyOutlineDark,
    outlineVariant = NavyOutlineVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = VioletPrimaryLight,
    onPrimary = VioletOnPrimaryLight,
    primaryContainer = VioletContainerLight,
    onPrimaryContainer = VioletOnContainerLight,
    secondary = CyanSecondaryLight,
    onSecondary = CyanOnSecondaryLight,
    secondaryContainer = CyanContainerLight,
    onSecondaryContainer = CyanOnSecondaryContainerLight,
    tertiary = RoseTertiaryLight,
    onTertiary = RoseOnTertiaryLight,
    tertiaryContainer = RoseContainerLight,
    onTertiaryContainer = RoseOnTertiaryContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight
)

@Composable
fun JobTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}


