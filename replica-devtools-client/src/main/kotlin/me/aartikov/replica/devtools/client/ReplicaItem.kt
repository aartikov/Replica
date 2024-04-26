package me.aartikov.replica.devtools.client

import androidx.compose.runtime.Composable
import me.aartikov.replica.devtools.client.view_data.SimpleReplicaViewData
import org.jetbrains.compose.web.css.*

@Composable
fun ReplicaItem(item: SimpleReplicaViewData) {
    Container(
        attrs = {
            style {
                display(DisplayStyle.Flex)
                flexFlow(FlexDirection.Row, FlexWrap.Wrap)
                alignItems(AlignItems.Center)
                padding(6.px, 16.px)
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

        if (item.pagesAmount != null) {
            RText(
                attrs = {
                    style {
                        paddingRight(16.px)
                        overflow("hidden")
                        whiteSpace("nowrap")
                        display(DisplayStyle.Flex)
                    }
                },
                value = "Pages: ${item.pagesAmount}",
            )
        }

        StatusItem(item.status)
        ObserverIcon(item.observerType)
    }
    Divider(attrs = {
        style {
            marginLeft(8.px)
        }
    })
}
