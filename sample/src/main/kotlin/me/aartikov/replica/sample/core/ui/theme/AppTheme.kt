package me.aartikov.replica.sample.core.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val LightColorPalette = lightColors(
    primary = Colors.Red,
    primaryVariant = Colors.DarkRed,
    secondary = Colors.Green,
    surface = Colors.LightGray
)

@Composable
fun AppTheme(content: @Composable() () -> Unit) {
    MaterialTheme(
        colors = LightColorPalette,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}