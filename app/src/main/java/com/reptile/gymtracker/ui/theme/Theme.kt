package com.reptile.gymtracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PurplePrimary,
    onPrimary = White,
    primaryContainer = PurpleContainer,
    onPrimaryContainer = OnSurfaceDark,
    secondary = GreenAccent,
    onSecondary = BackgroundDark,
    secondaryContainer = GreenAccentDark,
    onSecondaryContainer = BackgroundDark,
    tertiary = PurpleLight,
    onTertiary = White,
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    error = ErrorColor,
    onError = White,
    outline = PurpleDim,
    scrim = BackgroundDark,
)

@Composable
fun RepTileTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = RepTileTypography,
        content = content
    )
}
