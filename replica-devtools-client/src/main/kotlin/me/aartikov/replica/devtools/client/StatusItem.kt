package me.aartikov.replica.devtools.client

import androidx.compose.runtime.Composable
import me.aartikov.replica.devtools.dto.ReplicaStateDto
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

enum class StatusItemType(val color: CSSColorValue) {
    Fresh(Color.green),
    Stale(Color.gray),
    Error(Color.red),
    Loading(Color.blue),
    Refresh(Color.blue),
    Empty(Color.white)
}

@Composable
fun StatusItem(type: StatusItemType) {
    Div(
        attrs = {
            style {
                classes("card")
                backgroundColor(type.color)
                height(48.px)
                whiteSpace("nowrap")
                overflow("hidden")
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                padding(4.px, 12.px)
            }
        }
    ) { Text(type.name) }
}

fun ReplicaStateDto.toStatusItemType(): StatusItemType {
    return when {
        hasData && !loading && dataIsFresh -> StatusItemType.Fresh
        hasData && !loading && !dataIsFresh -> StatusItemType.Stale
        !hasData && hasError && !loading -> StatusItemType.Error
        !hasData && loading -> StatusItemType.Loading
        hasData && loading -> StatusItemType.Refresh
        !hasData && !hasError && !loading -> StatusItemType.Empty
        else -> throw IllegalArgumentException()
    }
}