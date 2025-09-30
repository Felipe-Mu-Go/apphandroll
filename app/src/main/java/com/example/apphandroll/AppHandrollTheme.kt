package com.example.apphandroll

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF9C6B45),
    onPrimary = Color.White,
    background = Color(0xFFF5E6CC),
    surface = Color(0xFFE9D4B8),
    onBackground = Color(0xFF5C3A21),
    onSurface = Color(0xFF5C3A21),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF9C6B45),
    onPrimary = Color.White,
    background = Color(0xFF2B1A10),
    surface = Color(0xFF3A2416),
    onBackground = Color(0xFFF5E6CC),
    onSurface = Color(0xFFF5E6CC),
)

@Composable
fun AppHandrollTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        shapes = Shapes(
            extraSmall = RoundedCornerShape(8.dp),
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(16.dp),
            large = RoundedCornerShape(24.dp)
        ),
        content = content
    )
}
