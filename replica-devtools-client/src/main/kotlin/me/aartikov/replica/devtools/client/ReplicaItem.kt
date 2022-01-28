package me.aartikov.replica.devtools.client

import androidx.compose.runtime.Composable
import me.aartikov.replica.devtools.client.view_data.SimpleReplicaViewData
import org.jetbrains.compose.web.css.*

@Composable
fun ReplicaItem(item: SimpleReplicaViewData) {
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
        StatusItem(item.status)
        ObserverIcon(item.observerType)
    }
    Divider(attrs = { style { marginLeft(16.px) } })
}