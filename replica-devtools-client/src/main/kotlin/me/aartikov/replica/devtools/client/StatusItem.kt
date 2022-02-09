package me.aartikov.replica.devtools.client

import androidx.compose.runtime.Composable
import me.aartikov.replica.devtools.client.view_data.StatusItemType
import org.jetbrains.compose.web.css.*

@Composable
fun StatusItem(type: StatusItemType) {
    val theme = LocalTheme.current
    val (color, textColor) = type.getColors(theme)

    RText(
        value = type.name,
        attrs = {
            style {
                borderRadius(4.px)
                padding(2.px, 4.px)
                textAlign("center")
                backgroundColor(color)
                minWidth(72.px)
                whiteSpace("nowrap")
                overflow("hidden")
            }
        },
        color = textColor,
        backgroundColor = color
    )
}