package me.aartikov.replica.sample.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val LightColors = lightColors(
    primary = RawColors.mint,
    primaryVariant = RawColors.darkMint,
    secondary = RawColors.mint,
    background = RawColors.white,
    surface = RawColors.lightGray,
    error = RawColors.red,
    onPrimary = RawColors.white,
    onSecondary = RawColors.white,
    onBackground = RawColors.black,
    onSurface = RawColors.black
)

private val DarkColors = darkColors(
    primary = RawColors.desaturatedMint,
    primaryVariant = RawColors.desaturatedMint,
    secondary = RawColors.desaturatedMint,
    background = RawColors.darkGray,
    surface = RawColors.darkGray,
    error = RawColors.desaturatedRed,
    onPrimary = RawColors.black,
    onSecondary = RawColors.black,
    onBackground = RawColors.white,
    onSurface = RawColors.white
)

@Composable
fun AppTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = if (isDarkTheme) DarkColors else LightColors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}