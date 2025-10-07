package com.example.apphandroll

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val AppFonts = FontFamily.Serif

private val DefaultTypography = Typography()

private val AppTypography = Typography(
    displayLarge = DefaultTypography.displayLarge.copy(fontFamily = AppFonts),
    displayMedium = DefaultTypography.displayMedium.copy(fontFamily = AppFonts),
    displaySmall = DefaultTypography.displaySmall.copy(fontFamily = AppFonts),
    headlineLarge = DefaultTypography.headlineLarge.copy(
        fontFamily = AppFonts,
        fontWeight = FontWeight.SemiBold
    ),
    headlineMedium = DefaultTypography.headlineMedium.copy(
        fontFamily = AppFonts,
        fontWeight = FontWeight.SemiBold
    ),
    headlineSmall = DefaultTypography.headlineSmall.copy(
        fontFamily = AppFonts,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp
    ),
    titleLarge = DefaultTypography.titleLarge.copy(
        fontFamily = AppFonts,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    titleMedium = DefaultTypography.titleMedium.copy(
        fontFamily = AppFonts,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp
    ),
    titleSmall = DefaultTypography.titleSmall.copy(
        fontFamily = AppFonts,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp
    ),
    bodyLarge = DefaultTypography.bodyLarge.copy(
        fontFamily = AppFonts,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    ),
    bodyMedium = DefaultTypography.bodyMedium.copy(
        fontFamily = AppFonts,
        fontSize = 15.sp
    ),
    bodySmall = DefaultTypography.bodySmall.copy(
        fontFamily = AppFonts,
        fontSize = 13.sp
    ),
    labelLarge = DefaultTypography.labelLarge.copy(
        fontFamily = AppFonts,
        fontWeight = FontWeight.SemiBold
    ),
    labelMedium = DefaultTypography.labelMedium.copy(
        fontFamily = AppFonts,
        fontWeight = FontWeight.Medium
    ),
    labelSmall = DefaultTypography.labelSmall.copy(fontFamily = AppFonts)
)

private val ElectricBlue = Color(0xFF1E88E5)
private val DeepBlue = Color(0xFF0B3C8A)
private val SkyGlimmer = Color(0xFFAED2FF)
private val SunsetCoral = Color(0xFFFF6F61)
private val CoralGlow = Color(0xFFFFB4A3)
private val SoftSky = Color(0xFFF5F8FF)
private val OceanShadow = Color(0xFF0F1A33)
private val MidnightBlue = Color(0xFF10213F)
private val TwilightBlue = Color(0xFF1C2E55)

private val LightColors = lightColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = SkyGlimmer,
    onPrimaryContainer = DeepBlue,
    secondary = SunsetCoral,
    onSecondary = Color.White,
    secondaryContainer = CoralGlow,
    onSecondaryContainer = Color(0xFF561313),
    surface = Color.White,
    onSurface = MidnightBlue,
    background = SoftSky,
    onBackground = OceanShadow,
    outline = ElectricBlue.copy(alpha = 0.35f)
)

private val DarkColors = darkColorScheme(
    primary = SkyGlimmer,
    onPrimary = DeepBlue,
    primaryContainer = DeepBlue,
    onPrimaryContainer = SkyGlimmer,
    secondary = CoralGlow,
    onSecondary = Color(0xFF551B0F),
    secondaryContainer = Color(0xFF7C2F23),
    onSecondaryContainer = CoralGlow,
    surface = MidnightBlue,
    onSurface = Color(0xFFE0ECFF),
    background = TwilightBlue,
    onBackground = Color(0xFFE0ECFF),
    outline = SkyGlimmer.copy(alpha = 0.5f)
)

@Composable
fun AppHandrollTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}
