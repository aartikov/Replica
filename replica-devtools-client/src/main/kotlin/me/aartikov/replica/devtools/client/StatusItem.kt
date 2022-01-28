package me.aartikov.replica.devtools.client

import androidx.compose.runtime.Composable
import me.aartikov.replica.devtools.client.view_data.StatusItemType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun StatusItem(type: StatusItemType) {
    val theme = LocalTheme.current
    val (color, textColor) = type.getColors(theme)

    Div(
        attrs = {
            style {
                classes("card", "center-align")
                backgroundColor(color)
                minWidth(72.px)
                whiteSpace("nowrap")
                overflow("hidden")
                color(textColor)
            }
        }
    ) { Text(type.name) }
}