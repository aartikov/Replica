package me.aartikov.replica.devtools.client

import androidx.compose.runtime.*
import me.aartikov.replica.devtools.client.components.Divider
import me.aartikov.replica.devtools.client.components.ImageButton
import me.aartikov.replica.devtools.dto.KeyedReplicaDto
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.Text

@Composable
fun KeyedReplicaItem(item: KeyedReplicaDto) {
    var isExpanded by remember { mutableStateOf(false) }

    Li(
        attrs = {
            style {
                classes("waves-effect", "waves-teal", "btn-flat")
                width(100.percent)
                display(DisplayStyle.Flex)
                flexFlow(FlexDirection.Row, FlexWrap.Nowrap)
                alignItems(AlignItems.Center)
                padding(2.px, 16.px)
                onClick { isExpanded = !isExpanded }
            }
        }
    ) {
        ImageButton(
            onClick = null,
            iconName = if (isExpanded) "keyboard_arrow_down" else "keyboard_arrow_right"
        )
        Div(
            attrs = {
                style {
                    height(48.px)
                    overflow("hidden")
                    flexGrow(1)
                    whiteSpace("nowrap")
                    property("text-overflow", "ellipsis")
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                }
            }
        ) {
            Text(value = item.name)
        }
        ImageButton(
            onClick = null,
            iconName = if (item.state.replicaWithActiveObserversCount > 0) "visibility" else "visibility_off"
        )
    }
    Divider()
    if (isExpanded) {
        item.childReplicas.values.forEach {
            Div(
                attrs = { style { paddingLeft(48.px) } }
            ) {
                ReplicaItemUi(it)
            }
        }
    }
}