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
    primary = StarBlueLight,
    onPrimary = Color.White,
    primaryContainer = StarBluePrimary,
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = StarGoldAccent,
    onSecondary = Color.Black,
    tertiary = CalcGreen,
    background = DarkBg,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = StarBluePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEFF6FF),
    onPrimaryContainer = StarBlueDark,
    secondary = StarGoldAccent,
    onSecondary = Color.Black,
    tertiary = CalcGreen,
    background = LightBg,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = LightOnSurface,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline
)

@Composable
fun StarOfficeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent branded StarOffice colors
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
