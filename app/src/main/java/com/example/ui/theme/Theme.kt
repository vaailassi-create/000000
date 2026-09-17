package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = CyberBlack,
    primaryContainer = CyberDarkCharcoal,
    onPrimaryContainer = ElectricCyan,
    secondary = NeonViolet,
    onSecondary = Color.White,
    secondaryContainer = CyberSurface,
    onSecondaryContainer = NeonViolet,
    tertiary = SynthMagenta,
    background = CyberBlack,
    onBackground = TextWhite,
    surface = CyberNearBlack,
    onSurface = TextWhite,
    surfaceVariant = CyberDarkCharcoal,
    onSurfaceVariant = TextMuted,
    outline = CyberSurfaceBorder
)

@Composable
fun AssistantXTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CyberColorScheme,
        typography = Typography,
        content = content
    )
}
