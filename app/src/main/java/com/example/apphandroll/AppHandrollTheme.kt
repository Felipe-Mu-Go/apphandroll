package com.example.apphandroll

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primaryContainer = Color(0xFFFFE0B2),
    onPrimaryContainer = Color(0xFF3A2500),
)

private val DarkColors = darkColorScheme(
    primaryContainer = Color(0xFF5A3A00),
    onPrimaryContainer = Color(0xFFFFDDB3),
)

@Composable
fun AppHandrollTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        content = content
    )
}
