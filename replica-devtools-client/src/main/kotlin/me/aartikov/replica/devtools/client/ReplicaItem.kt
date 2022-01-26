package me.aartikov.replica.devtools.client

import androidx.compose.runtime.Composable
import me.aartikov.replica.devtools.dto.ReplicaDto
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.Text

@Composable
fun ReplicaItem(item: ReplicaDto) {
    Li(
        attrs = {
            style {
                width(100.percent)
                display(DisplayStyle.Flex)
                flexFlow(FlexDirection.Row, FlexWrap.Nowrap)
                alignItems(AlignItems.Center)
                padding(2.px, 16.px)
            }
        }
    ) {
        Div(
            attrs = {
                style {
                    flexGrow(1)
                    overflow("hidden")
                    whiteSpace("nowrap")
                    property("text-overflow", "ellipsis")
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                }
            }
        ) { Text(value = item.name) }
        StatusItem(item.state.toStatusItemType())
        MaterialIcon(
            name = if (item.state.activeObserverCount > 0) "visibility" else "visibility_off",
            attrs = {
                style {
                    marginLeft(16.px)
                }
            }
        )
    }
    Divider(attrs = { style { marginLeft(16.px) } })
}