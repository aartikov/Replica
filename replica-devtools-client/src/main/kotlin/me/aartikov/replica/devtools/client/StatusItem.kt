package me.aartikov.replica.devtools.client

import androidx.compose.runtime.Composable
import me.aartikov.replica.devtools.client.view_data.StatusItemType
import org.jetbrains.compose.web.css.*

@Composable
fun StatusItem(type: StatusItemType) {
    val theme = LocalTheme.current
    val statusColor = type.getColor(theme)

    RText(
        value = type.value,
        attrs = {
            style {
                borderRadius(4.px)
                padding(4.px, 4.px)
                textAlign("center")
                backgroundColor(statusColor.background)
                minWidth(72.px)
                whiteSpace("nowrap")
                overflow("hidden")
            }
        },
        color = statusColor.text,
        backgroundColor = statusColor.background
    )
}