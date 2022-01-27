package me.aartikov.replica.devtools.client

import androidx.compose.runtime.*
import me.aartikov.replica.devtools.dto.KeyedReplicaDto
import org.jetbrains.compose.web.css.*

@Composable
fun KeyedReplicaItem(item: KeyedReplicaDto) {
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
        MaterialIcon(
            name = if (item.state.replicaWithActiveObserversCount > 0) "visibility" else "visibility_off",
            attrs = { style { marginLeft(16.px) } }
        )
    }
    Divider(attrs = { style { marginLeft(16.px) } })
    if (isExpanded) {
        item.childReplicas.values.forEach {
            Container(
                attrs = { style { paddingLeft(48.px) } }
            ) {
                ReplicaItem(it)
            }
        }
    }
}