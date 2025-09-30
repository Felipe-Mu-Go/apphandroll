package com.example.apphandroll

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Cream = Color(0xFFFDF2E2)
private val WarmSand = Color(0xFFF5D6A3)
private val SoftBrown = Color(0xFF8B5E3C)
private val DeepBrown = Color(0xFF4F2A1A)
private val ToastedAlmond = Color(0xFFD99E6A)
private val CoffeeBean = Color(0xFF3C1F12)

private val LightColors = lightColorScheme(
    primary = SoftBrown,
    onPrimary = Color.White,
    primaryContainer = WarmSand,
    onPrimaryContainer = DeepBrown,
    secondary = ToastedAlmond,
    onSecondary = Color.White,
    secondaryContainer = WarmSand,
    onSecondaryContainer = DeepBrown,
    surface = Color(0xFFFFF7ED),
    onSurface = DeepBrown,
    background = Cream,
    onBackground = DeepBrown,
    outline = SoftBrown.copy(alpha = 0.4f)
)

private val DarkColors = darkColorScheme(
    primary = ToastedAlmond,
    onPrimary = CoffeeBean,
    primaryContainer = CoffeeBean,
    onPrimaryContainer = ToastedAlmond,
    secondary = SoftBrown,
    onSecondary = CoffeeBean,
    secondaryContainer = DeepBrown,
    onSecondaryContainer = WarmSand,
    surface = Color(0xFF2A140C),
    onSurface = WarmSand,
    background = Color(0xFF1C0D07),
    onBackground = WarmSand,
    outline = ToastedAlmond.copy(alpha = 0.5f)
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
