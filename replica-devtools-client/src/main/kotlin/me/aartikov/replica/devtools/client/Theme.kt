package me.aartikov.replica.devtools.client

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.browser.window
import org.jetbrains.compose.web.css.CSSColorValue
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.rgb

class Theme(
    val isDark: Boolean,
    val primary: CSSColorValue,
    val onPrimary: CSSColorValue,
    val secondaryColor: CSSColorValue,
    val onSecondaryColor: CSSColorValue,
    val background: CSSColorValue,
    val onBackground: CSSColorValue,
    val error: CSSColorValue,
    val onError: CSSColorValue,
    val name: String
) {

    companion object {
        val lightTheme = Theme(
            isDark = false,
            primary = rgb(98, 0, 238),
            onPrimary = Color.white,
            secondaryColor = rgb(3, 218, 198),
            onSecondaryColor = Color.black,
            background = Color.white,
            onBackground = Color.black,
            error = rgb(176, 0, 32),
            onError = Color.white,
            name = "light"
        )
        val darkTheme = Theme(
            isDark = true,
            primary = rgb(187, 134, 252),
            onPrimary = rgb(36, 36, 36),
            secondaryColor = rgb(3, 218, 198),
            onSecondaryColor = rgb(36, 36, 36),
            background = rgb(36, 36, 36),
            onBackground = rgb(248, 248, 242),
            error = rgb(207, 102, 121),
            onError = rgb(36, 36, 36),
            name = "dark"
        )
    }
}

val LocalTheme = staticCompositionLocalOf { Theme.lightTheme }
const val LocalStorageThemeKey = "theme"

@Composable
fun Theme(
    isDarkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val theme = if (isDarkTheme) {
        Theme.darkTheme
    } else {
        Theme.lightTheme
    }
    window.localStorage.setItem(LocalStorageThemeKey, theme.name)

    CompositionLocalProvider(LocalTheme provides theme) {
        content()
    }
}