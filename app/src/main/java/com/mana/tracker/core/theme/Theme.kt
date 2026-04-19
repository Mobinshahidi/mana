package com.mana.tracker.core.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = ManaWarmAccent,
    onPrimary = ManaDarkNeutral,
    secondary = ManaDarkNeutral,
    onSecondary = ManaLightBeige,
    tertiary = ManaLightBeige,
    background = ColorTokens.LightBackground,
    surface = ColorTokens.LightSurface,
    onSurface = ManaDarkNeutral
)

private val DarkColors = darkColorScheme(
    primary = ManaWarmAccent,
    onPrimary = ManaDarkNeutral,
    secondary = ManaLightBeige,
    onSecondary = ManaDarkNeutral,
    tertiary = ManaWarmAccent,
    background = ManaDarkNeutral,
    surface = ColorTokens.DarkSurface,
    onSurface = ManaLightBeige
)

object ColorTokens {
    val LightBackground = androidx.compose.ui.graphics.Color(0xFFF3F1EC)
    val LightSurface = androidx.compose.ui.graphics.Color(0xFFE4E2D7)
    val DarkSurface = androidx.compose.ui.graphics.Color(0xFF2A2A28)
}

@Composable
fun ManaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
