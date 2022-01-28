package me.aartikov.replica.devtools.client

import androidx.compose.runtime.Composable
import me.aartikov.replica.devtools.client.view_data.ObserverType
import org.jetbrains.compose.web.css.marginLeft
import org.jetbrains.compose.web.css.px

@Composable
fun ObserverIcon(type: ObserverType) {
    MaterialIcon(
        name = when (type) {
            ObserverType.Active -> "visibility"
            ObserverType.Inactive -> "visibility_off"
            ObserverType.None -> ""
        },
        attrs = {
            style {
                marginLeft(16.px)
            }
        }
    )
}