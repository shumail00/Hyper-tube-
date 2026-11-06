package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class HyperThemeMode {
    AUTO,
    LIGHT,
    DARK
}

@Immutable
data class HyperOSGlassColors(
    val isDark: Boolean,
    val glassSurface: Color,
    val glassElevated: Color,
    val glassBorder: Color,
    val subtleBorder: Color,
    val accentBlue: Color,
    val accentCyan: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val canvasBackground: Color
)

val LocalHyperOSGlass = staticCompositionLocalOf {
    HyperOSGlassColors(
        isDark = true,
        glassSurface = HyperDarkGlassSurface,
        glassElevated = HyperDarkGlassElevated,
        glassBorder = HyperDarkGlassBorder,
        subtleBorder = HyperDarkSubtleBorder,
        accentBlue = HyperBlueDark,
        accentCyan = HyperCyanDark,
        textPrimary = HyperDarkOnBackground,
        textSecondary = HyperDarkOnSurfaceVariant,
        canvasBackground = HyperDarkBackground
    )
}

val HyperOSShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(34.dp)
)

private val HyperDarkColorScheme = darkColorScheme(
    primary = HyperBlueDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF193366),
    onPrimaryContainer = Color(0xFFD8E6FF),
    secondary = HyperCyanDark,
    onSecondary = Color(0xFF002924),
    secondaryContainer = Color(0xFF0F3D39),
    onSecondaryContainer = Color(0xFFB5FFF6),
    tertiary = HyperAmber,
    background = HyperDarkBackground,
    onBackground = HyperDarkOnBackground,
    surface = HyperDarkSurface,
    onSurface = HyperDarkOnBackground,
    surfaceVariant = Color(0xFF1F2533),
    onSurfaceVariant = HyperDarkOnSurfaceVariant,
    outline = HyperDarkGlassBorder
)

private val HyperLightColorScheme = lightColorScheme(
    primary = HyperBlueLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE1EBFF),
    onPrimaryContainer = Color(0xFF002878),
    secondary = HyperCyanLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD5FAF6),
    onSecondaryContainer = Color(0xFF003732),
    tertiary = HyperAmber,
    background = HyperLightBackground,
    onBackground = HyperLightOnBackground,
    surface = HyperLightSurface,
    onSurface = HyperLightOnBackground,
    surfaceVariant = Color(0xFFE5E9F2),
    onSurfaceVariant = HyperLightOnSurfaceVariant,
    outline = HyperLightSubtleBorder
)

@Composable
fun MyApplicationTheme(
    themeMode: HyperThemeMode = HyperThemeMode.AUTO,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val useDark = when (themeMode) {
        HyperThemeMode.AUTO -> systemDark
        HyperThemeMode.DARK -> true
        HyperThemeMode.LIGHT -> false
    }

    val colorScheme = if (useDark) HyperDarkColorScheme else HyperLightColorScheme
    val glassColors = if (useDark) {
        HyperOSGlassColors(
            isDark = true,
            glassSurface = HyperDarkGlassSurface,
            glassElevated = HyperDarkGlassElevated,
            glassBorder = HyperDarkGlassBorder,
            subtleBorder = HyperDarkSubtleBorder,
            accentBlue = HyperBlueDark,
            accentCyan = HyperCyanDark,
            textPrimary = HyperDarkOnBackground,
            textSecondary = HyperDarkOnSurfaceVariant,
            canvasBackground = HyperDarkBackground
        )
    } else {
        HyperOSGlassColors(
            isDark = false,
            glassSurface = HyperLightGlassSurface,
            glassElevated = HyperLightGlassElevated,
            glassBorder = HyperLightGlassBorder,
            subtleBorder = HyperLightSubtleBorder,
            accentBlue = HyperBlueLight,
            accentCyan = HyperCyanLight,
            textPrimary = HyperLightOnBackground,
            textSecondary = HyperLightOnSurfaceVariant,
            canvasBackground = HyperLightBackground
        )
    }

    CompositionLocalProvider(LocalHyperOSGlass provides glassColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = HyperOSShapes,
            content = content
        )
    }
}
