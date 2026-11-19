package com.example.warbage.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldNeon,
    onPrimary = Color(0xFF032213),
    primaryContainer = Color(0xFF064E3B),
    onPrimaryContainer = EmeraldLight,
    
    secondary = CyanNeon,
    onSecondary = Color(0xFF03222B),
    secondaryContainer = Color(0xFF0E4A5C),
    onSecondaryContainer = CyanLight,
    
    tertiary = VioletNeon,
    onTertiary = Color(0xFF261047),
    tertiaryContainer = Color(0xFF4C1D95),
    onTertiaryContainer = VioletLight,
    
    background = DarkBackground,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    
    error = ErrorNeon,
    onError = Color.White,
    errorContainer = ErrorContainerDark,
    onErrorContainer = Color(0xFFFFCCD5),
    
    outline = DarkSurfaceBorder,
    outlineVariant = Color(0xFF1E293B)
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldDeep,
    onPrimary = Color.White,
    primaryContainer = EmeraldLight,
    onPrimaryContainer = Color(0xFF022C22),
    
    secondary = CyanDeep,
    onSecondary = Color.White,
    secondaryContainer = CyanLight,
    onSecondaryContainer = Color(0xFF083344),
    
    tertiary = VioletDeep,
    onTertiary = Color.White,
    tertiaryContainer = VioletLight,
    onTertiaryContainer = Color(0xFF2E1065),
    
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    
    error = ErrorNeon,
    onError = Color.White,
    errorContainer = ErrorContainerLight,
    onErrorContainer = Color(0xFF881337),
    
    outline = LightSurfaceBorder,
    outlineVariant = Color(0xFFCBD5E1)
)

@Composable
fun WarbageTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
