package me.aartikov.replica.devtools.client

import androidx.compose.runtime.Composable
import me.aartikov.replica.devtools.client.view_data.ObserverType
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.marginLeft
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Img

@Composable
fun ObserverIcon(type: ObserverType) {
    Container(
        attrs = {
            style {
                marginLeft(16.px)
                height(24.px)
                width(24.px)
            }
        }
    ) {
        val isDarkTheme = LocalTheme.current.isDark
        when (type) {
            ObserverType.Active -> Img(
                src = if (isDarkTheme) "ic_eye_24_white.png" else "ic_eye_24_black.png"
            )
            ObserverType.Inactive -> Img(
                src = if (isDarkTheme) "ic_closed_eye_24_white.png" else "ic_closed_eye_24_black.png"
            )
            ObserverType.None -> {
                // Nothing
            }
        }
    }
}