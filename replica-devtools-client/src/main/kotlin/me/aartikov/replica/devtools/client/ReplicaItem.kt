package me.aartikov.replica.devtools.client

import androidx.compose.runtime.Composable
import me.aartikov.replica.devtools.dto.ReplicaDto
import org.jetbrains.compose.web.css.*

@Composable
fun ReplicaItem(item: ReplicaDto) {
    Container(
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
        RText(
            attrs = {
                style {
                    flexGrow(1)
                    overflow("hidden")
                    whiteSpace("nowrap")
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                }
            },
            value = item.name
        )
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