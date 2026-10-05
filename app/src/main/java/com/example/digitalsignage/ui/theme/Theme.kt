package com.example.digitalsignage.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Forest,
    onPrimary = Cream,
    secondary = Leaf,
    tertiary = Coral,
    background = Cream,
    onBackground = Ink,
    surface = Cream,
    onSurface = Ink,
    surfaceVariant = Sky.copy(alpha = 0.24f),
    onSurfaceVariant = Ink,
)

@Composable
fun DigitalSignageTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
