package me.aartikov.replica.devtools.client

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.browser.window
import org.jetbrains.compose.web.css.CSSColorValue
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.rgb

data class StatusColor(
    val background: CSSColorValue,
    val text: CSSColorValue
)

class Theme(
    val isDark: Boolean,
    val name: String,

    val backgroundColor: CSSColorValue,
    val textColor: CSSColorValue,
    val dividerColor: CSSColorValue,
    val bottomBarColor: CSSColorValue,

    val loadingStatusColor: StatusColor,
    val freshStatusColor: StatusColor,
    val errorStatusColor: StatusColor,
    val staleStatusColor: StatusColor,
    val emptyStatusColor: StatusColor
) {

    companion object {
        val lightTheme = Theme(
            isDark = false,
            name = "light",

            backgroundColor = Color.white,
            textColor = Color.black,
            dividerColor = Color.lightgray,
            bottomBarColor = Color.whitesmoke,

            loadingStatusColor = StatusColor(
                background = rgb(60, 170, 221),
                text = Color.white
            ),
            freshStatusColor = StatusColor(
                background = rgb(6, 206, 149),
                text = Color.white
            ),
            errorStatusColor = StatusColor(
                background = rgb(224, 96, 104),
                text = Color.white
            ),
            staleStatusColor = StatusColor(
                background = rgb(165, 172, 191),
                text = Color.white
            ),
            emptyStatusColor = StatusColor(
                background = rgb(223, 223, 223),
                text = rgb(90, 90, 90),
            )
        )
        val darkTheme = Theme(
            isDark = true,
            name = "dark",

            backgroundColor = rgb(68, 72, 74),
            textColor = rgb(195, 195, 195),
            bottomBarColor = rgb(60, 60, 60),
            dividerColor = rgb(105, 105, 105),

            loadingStatusColor = StatusColor(
                background = rgb(57, 158, 205),
                text = Color.white
            ),
            freshStatusColor = StatusColor(
                background = rgb(82, 167, 97),
                text = Color.white
            ),
            errorStatusColor = StatusColor(
                background = rgb(176, 107, 105),
                text = Color.white
            ),
            staleStatusColor = StatusColor(
                background = rgb(128, 131, 138),
                text = rgb(230, 230, 230)
            ),
            emptyStatusColor = StatusColor(
                background = rgb(195, 195, 195),
                text = rgb(70, 70, 70)
            )
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