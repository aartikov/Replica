package me.aartikov.replica.devtools.client

import androidx.compose.runtime.*
import me.aartikov.replica.devtools.client.view_data.KeyedReplicaViewData
import org.jetbrains.compose.web.css.*

@Composable
fun KeyedReplicaItem(item: KeyedReplicaViewData) {
    var isExpanded by remember { mutableStateOf(false) }

    Container(
        attrs = {
            style {
                width(100.percent)
                display(DisplayStyle.Flex)
                flexFlow(FlexDirection.Row, FlexWrap.Nowrap)
                alignItems(AlignItems.Center)
                padding(2.px, 16.px)
                onClick { isExpanded = !isExpanded }
            }
        }
    ) {
        MaterialIcon(
            name = if (isExpanded) "keyboard_arrow_down" else "keyboard_arrow_right"
        )
        RText(
            attrs = {
                style {
                    height(48.px)
                    flexGrow(1)
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                }
            },
            value = item.name
        )
    }
    Divider(attrs = { style { marginLeft(16.px) } })
    if (isExpanded) {
        if (item.childReplicas.isEmpty()) {
            ChildReplicaPlaceholder()
        } else {
            item.childReplicas.forEach {
                Container(attrs = { style { paddingLeft(48.px) } }) {
                    ReplicaItem(it)
                }
            }
        }
    }
}

@Composable
fun ChildReplicaPlaceholder() {
    RText(
        "No child replicas",
        attrs = {
            style {
                width(100.percent)
                padding(16.px, 16.px, 16.px, 64.px)
            }
        }
    )
    Divider(attrs = { style { marginLeft(64.px) } })
}