package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = ForestGreenPrimary,
    onPrimary = TextOnDark,
    primaryContainer = ForestGreenContainer,
    onPrimaryContainer = OnForestGreenContainer,
    secondary = ForestGreenLight,
    onSecondary = TextOnDark,
    background = WarmBackground,
    onBackground = TextPrimary,
    surface = WarmSurface,
    onSurface = TextPrimary,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = OutlineMuted
)

private val DarkColorScheme = darkColorScheme(
    primary = ForestGreenLight,
    onPrimary = TextOnDark,
    primaryContainer = ForestGreenDark,
    onPrimaryContainer = ForestGreenContainer,
    secondary = ForestGreenContainer,
    onSecondary = OnForestGreenContainer,
    background = Color(0xFF111713),
    onBackground = Color(0xFFE8ECE8),
    surface = Color(0xFF19221C),
    onSurface = Color(0xFFE8ECE8),
    surfaceVariant = Color(0xFF233027),
    onSurfaceVariant = Color(0xFFB0BEB4),
    outline = Color(0xFF3B4D40)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Keep warm light theme default to match MiniLedger design
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            // Each screen owns icon contrast: photo headers use white icons,
            // while Profile and its detail pages use dark icons on white.
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
