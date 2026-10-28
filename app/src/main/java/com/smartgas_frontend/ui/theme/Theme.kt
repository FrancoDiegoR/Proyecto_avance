package com.smartgas_frontend.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Blue,
    onPrimary = Color.White,
    primaryContainer = BlueSoft,
    onPrimaryContainer = BlueDark,
    secondary = BlueDark,
    onSecondary = Color.White,
    secondaryContainer = BlueSoft,
    onSecondaryContainer = BlueDark,
    tertiary = Orange,
    onTertiary = Color.White,
    tertiaryContainer = OrangeSoft,
    onTertiaryContainer = Orange,
    error = Red,
    onError = Color.White,
    errorContainer = RedSoft,
    onErrorContainer = Red,
    background = PageBackground,
    onBackground = Ink,
    surface = Surface,
    onSurface = Ink,
    surfaceVariant = Soft,
    onSurfaceVariant = Muted,
    surfaceContainerLowest = Surface,
    surfaceContainerLow = Surface,
    surfaceContainer = Surface,
    surfaceContainerHigh = Soft,
    surfaceContainerHighest = Soft,
    outline = Muted,
    outlineVariant = Line
)

private val DarkColorScheme = darkColorScheme(
    primary = Cyan,
    onPrimary = Color(0xFF04223B),
    primaryContainer = DarkAccentContainer,
    onPrimaryContainer = DarkAccent,
    secondary = DarkAccent,
    onSecondary = Color(0xFF04223B),
    secondaryContainer = DarkAccentContainer,
    onSecondaryContainer = DarkAccent,
    tertiary = Orange,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF3A1D12),
    onTertiaryContainer = Color(0xFFFFB59A),
    error = Color(0xFFFF8A8A),
    onError = Color(0xFF3B0A0A),
    errorContainer = Color(0xFF3A1717),
    onErrorContainer = Color(0xFFFFB4B4),
    background = DarkBackground,
    onBackground = DarkInk,
    surface = DarkSurface,
    onSurface = DarkInk,
    surfaceVariant = DarkSurfaceHeader,
    onSurfaceVariant = DarkMuted,
    surfaceContainerLowest = DarkInput,
    surfaceContainerLow = DarkSurface,
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkSurfaceHeader,
    surfaceContainerHighest = DarkSurfaceHeader,
    outline = DarkMuted,
    outlineVariant = DarkLine
)

@Composable
fun SmartGasTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
