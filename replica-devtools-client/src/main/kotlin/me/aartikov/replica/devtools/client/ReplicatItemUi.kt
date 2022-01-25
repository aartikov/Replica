package me.aartikov.replica.devtools.client

import androidx.compose.runtime.Composable
import me.aartikov.replica.devtools.client.components.ImageButton
import me.aartikov.replica.devtools.dto.ReplicaDto
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.Text

@Composable
fun ReplicaItemUi(item: ReplicaDto) {
    Li(
        attrs = {
            style {
                width(100.percent)
                display(DisplayStyle.Flex)
                flexFlow(FlexDirection.Row, FlexWrap.Nowrap)
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.Center)
                padding(2.px, 16.px)
            }
        }
    ) {
        StatusItem(item.state.toStatusItemType())
        Div(
            attrs = {
                style {
                    height(48.px)
                    overflow("hidden")
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
            iconName = if (item.state.activeObserverCount > 0) "visibility" else "visibility_off"
        )
    }
    Li(
        attrs = {
            classes("divider")
            style {
                height(1.px)
                width(100.percent)
            }
        }
    )
}