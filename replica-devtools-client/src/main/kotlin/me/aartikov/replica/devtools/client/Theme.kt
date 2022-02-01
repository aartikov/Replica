package me.aartikov.replica.devtools.client

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.browser.window
import org.jetbrains.compose.web.css.CSSColorValue
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.rgb

class Theme(
    val isDark: Boolean,
    val backgroundColor: CSSColorValue,
    val textColor: CSSColorValue,
    val defaultStatusColor: CSSColorValue,
    val statusTextColor: CSSColorValue,
    val errorStatusColor: CSSColorValue,
    val freshStatusColor: CSSColorValue,
    val bottomBarColor: CSSColorValue,
    val dividerColor: CSSColorValue,
    val name: String
) {

    companion object {
        val lightTheme = Theme(
            isDark = false,
            defaultStatusColor = rgb(98, 0, 238),
            statusTextColor = Color.white,
            freshStatusColor = rgb(3, 218, 198),
            backgroundColor = Color.white,
            textColor = Color.black,
            errorStatusColor = rgb(176, 0, 32),
            name = "light",
            bottomBarColor = Color.whitesmoke,
            dividerColor = Color.lightgray
        )
        val darkTheme = Theme(
            isDark = true,
            defaultStatusColor = rgb(187, 134, 252),
            statusTextColor = rgb(36, 36, 36),
            freshStatusColor = rgb(3, 218, 198),
            backgroundColor = rgb(36, 36, 36),
            textColor = rgb(248, 248, 242),
            errorStatusColor = rgb(207, 102, 121),
            bottomBarColor = Color.gray,
            dividerColor = rgb(248, 248, 242),
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
    SideEffect {
        window.localStorage.setItem(LocalStorageThemeKey, theme.name)
    }

    CompositionLocalProvider(LocalTheme provides theme) {
        content()
    }
}