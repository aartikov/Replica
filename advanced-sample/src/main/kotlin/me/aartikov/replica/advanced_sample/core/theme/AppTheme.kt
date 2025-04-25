package me.aartikov.replica.advanced_sample.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val LightColors = lightColorScheme(
    primary = RawColors.mint,
    onPrimary = RawColors.white,
    primaryContainer = RawColors.darkMint,
    onPrimaryContainer = RawColors.white,
    secondary = RawColors.mint,
    onSecondary = RawColors.white,
    secondaryContainer = RawColors.mint,
    onSecondaryContainer = RawColors.white,
    error = RawColors.red,
    onError = RawColors.white,
    background = RawColors.white,
    onBackground = RawColors.black,
    surface = RawColors.lightGray,
    onSurface = RawColors.black
)

private val DarkColors = darkColorScheme(
    primary = RawColors.desaturatedMint,
    onPrimary = RawColors.black,
    primaryContainer = RawColors.desaturatedMint,
    onPrimaryContainer = RawColors.black,
    secondary = RawColors.desaturatedMint,
    onSecondary = RawColors.black,
    secondaryContainer = RawColors.desaturatedMint,
    onSecondaryContainer = RawColors.black,
    error = RawColors.desaturatedRed,
    onError = RawColors.black,
    background = RawColors.darkGray,
    onBackground = RawColors.white,
    surface = RawColors.darkGray,
    onSurface = RawColors.white
)

@Composable
fun AppTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (isDarkTheme) DarkColors else LightColors

    CompositionLocalProvider(
        LocalContentColor provides colorScheme.onBackground
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
