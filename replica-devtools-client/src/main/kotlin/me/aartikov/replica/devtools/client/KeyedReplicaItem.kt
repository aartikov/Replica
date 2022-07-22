package me.aartikov.replica.devtools.client

import androidx.compose.runtime.*
import me.aartikov.replica.devtools.client.view_data.KeyedReplicaViewData
import org.jetbrains.compose.web.ExperimentalComposeWebApi
import org.jetbrains.compose.web.css.*

@Composable
fun KeyedReplicaItem(item: KeyedReplicaViewData) {
    var isExpanded by remember { mutableStateOf(false) }

    Container(
        attrs = {
            style {
                display(DisplayStyle.Flex)
                flexFlow(FlexDirection.Row, FlexWrap.Wrap)
                alignItems(AlignItems.Center)
                padding(2.px, 16.px)
            }
            onClick { isExpanded = !isExpanded }
        }
    ) {
        ExpandableImg(isExpanded)
        RText(
            attrs = {
                style {
                    height(48.px)
                    flexGrow(1)
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    marginLeft(16.px)
                }
            },
            value = item.name
        )
        if (!isExpanded) {
            ObserverIcon(item.observerType)
        }
    }
    Divider(attrs = {
        style {
            marginLeft(8.px)
        }
    })
    if (isExpanded) {
        if (item.childReplicas.isEmpty()) {
            ChildReplicaPlaceholder()
        } else {
            item.childReplicas.forEach {
                Container(attrs = { style { paddingLeft(32.px) } }) {
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
                padding(16.px, 16.px, 16.px, 48.px)
            }
        }
    )
    Divider(attrs = {
        style {
            marginLeft(40.px)
        }
    })
}

@OptIn(ExperimentalComposeWebApi::class)
@Composable
fun ExpandableImg(isExpanded: Boolean) {
    ThemedImg(
        src = "arrow_forward_24_black.png",
        attrs = {
            style {
                width(14.px)
                height(14.px)
                padding(1.px)
                if (isExpanded) {
                    transform { rotate(90.deg) }
                }
            }
        }
    )
}