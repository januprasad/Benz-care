package com.toyota.demo.ui.theme

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
    primary = SophisticatedTextPrimary,
    secondary = SophisticatedTextMuted,
    tertiary = SophisticatedEmerald,
    background = SophisticatedBackground,
    surface = SophisticatedAIBubble,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = SophisticatedTextPrimary,
    onSurface = SophisticatedTextPrimary,
    outline = SophisticatedBorderLight
)

private val LightColorScheme = lightColorScheme(
    primary = ToyotaClassicBlue,
    secondary = ToyotaMetallicGrey,
    tertiary = SophisticatedEmerald,
    background = ToyotaWhiteSurface,
    surface = ToyotaLightCard,
    onPrimary = Color.White,
    onSecondary = ToyotaCharcoalText,
    onBackground = ToyotaCharcoalText,
    onSurface = ToyotaCharcoalText,
    outline = Color(0xFFD1D1D6)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
