package me.aartikov.replica.devtools.client

import androidx.compose.runtime.Composable
import me.aartikov.replica.devtools.client.view_data.ObserverType
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.marginLeft
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width

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
        when (type) {
            ObserverType.Active -> ThemedImg("ic_eye_24_black.png")
            ObserverType.Inactive -> ThemedImg("ic_closed_eye_24_black.png")
            ObserverType.None -> {
                // Nothing
            }
        }
    }
}