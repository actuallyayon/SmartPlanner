package com.smartplanner.view.theme

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

val PrimaryBlue = Color(0xFF2F6BFF)
val PrimaryBlueVariant = Color(0xFF1E52D5)
val SuccessGreen = Color(0xFF22C55E)
val DestructiveRed = Color(0xFFEF4444)

// Dark Theme Colors
val DarkBg = Color(0xFF0B0E14)
val DarkSurface = Color(0xFF171C26)
val DarkOnBg = Color(0xFFF8FAFC)
val DarkOnSurface = Color(0xFFE2E8F0)
val DarkOutline = Color(0xFF334155)

// Light Theme Colors
val LightBg = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightOnBg = Color(0xFF0F172A)
val LightOnSurface = Color(0xFF1E293B)
val LightOutline = Color(0xFFCBD5E1)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    background = DarkBg,
    onBackground = DarkOnBg,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    outline = DarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    background = LightBg,
    onBackground = LightOnBg,
    surface = LightSurface,
    onSurface = LightOnSurface,
    outline = LightOutline
)

@Composable
fun SmartPlannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Keep it false to enforce our branded design guidelines
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
        content = content
    )
}
