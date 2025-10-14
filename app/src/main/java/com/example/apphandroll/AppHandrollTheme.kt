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

private val RiceWhite = Color(0xFFFFF8F1)
private val Porcelain = Color(0xFFFFFFFF)
private val CocoaInk = Color(0xFF2B2118)
private val AlmondOutline = Color(0xFFD8C6B6)
private val SurfaceVariantLight = Color(0xFFF4E7DA)
private val OnSurfaceVariantLight = Color(0xFF4B3E33)
private val PrimaryCafeNori = Color(0xFF9C6A3B)
private val OnPrimaryCafeNori = Color(0xFFFFFFFF)
private val PrimaryContainerLight = Color(0xFFEBD3BE)
private val OnPrimaryContainerLight = Color(0xFF3B2A1D)
private val SecondaryLatte = Color(0xFFC99770)
private val OnSecondaryLatte = Color(0xFF1E120B)
private val SecondaryContainerLight = Color(0xFFF1D9C6)
private val OnSecondaryContainerLight = Color(0xFF3B2517)
private val TertiaryCobre = Color(0xFFC47A4A)
private val OnTertiaryCobre = Color(0xFFFFFFFF)
private val TertiaryContainerLight = Color(0xFFF0CFBD)
private val OnTertiaryContainerLight = Color(0xFF3B1F12)
private val OutlineVariantLight = Color(0xFFE8DACE)

private val BackgroundDark = Color(0xFF1A1511)
private val SurfaceDark = Color(0xFF221C17)
private val OnSurfaceDark = Color(0xFFEFE7E0)
private val SurfaceVariantDark = Color(0xFF3A2F26)
private val OnSurfaceVariantDark = Color(0xFFDCCDBE)
private val PrimaryDark = Color(0xFFE0B48C)
private val OnPrimaryDark = Color(0xFF3B2A1D)
private val PrimaryContainerDark = Color(0xFF5B3D25)
private val OnPrimaryContainerDark = Color(0xFFF6E6D8)
private val SecondaryDark = Color(0xFFE7C09F)
private val OnSecondaryDark = Color(0xFF2C1C12)
private val SecondaryContainerDark = Color(0xFF4E3626)
private val OnSecondaryContainerDark = Color(0xFFF8E8DC)
private val TertiaryDark = Color(0xFFE4A883)
private val OnTertiaryDark = Color(0xFF2E160C)
private val TertiaryContainerDark = Color(0xFF5A3726)
private val OnTertiaryContainerDark = Color(0xFFFBE7DB)
private val OutlineDark = Color(0xFF4D4036)
private val OutlineVariantDark = Color(0xFF695B50)

private val Error = Color(0xFFB3261E)
private val OnError = Color(0xFFFFFFFF)

private val LightColors = lightColorScheme(
    primary = PrimaryCafeNori,
    onPrimary = OnPrimaryCafeNori,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLatte,
    onSecondary = OnSecondaryLatte,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryCobre,
    onTertiary = OnTertiaryCobre,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    background = RiceWhite,
    onBackground = CocoaInk,
    surface = Porcelain,
    onSurface = CocoaInk,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = AlmondOutline,
    outlineVariant = OutlineVariantLight,
    error = Error,
    onError = OnError
)

private val DarkColors = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    error = Error,
    onError = OnError
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
