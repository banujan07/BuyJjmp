package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BuyJumpBlueLight,
    onPrimary = Color.White,
    primaryContainer = BuyJumpBlue,
    onPrimaryContainer = Color.White,
    secondary = BuyJumpOrangeLight,
    onSecondary = Color.White,
    tertiary = BuyJumpEmerald,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = Color(0xFF1E293B),
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = BuyJumpBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = BuyJumpBlue,
    secondary = BuyJumpOrange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFEDD5),
    onSecondaryContainer = Color(0xFF9A3412),
    tertiary = BuyJumpEmerald,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = BorderLight
)

@Composable
fun BuyJumpTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent BuyJump brand identity
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
