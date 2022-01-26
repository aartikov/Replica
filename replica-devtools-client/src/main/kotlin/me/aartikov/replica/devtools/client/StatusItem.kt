package me.aartikov.replica.devtools.client

import androidx.compose.runtime.Composable
import me.aartikov.replica.devtools.dto.ReplicaStateDto
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

enum class StatusItemType(val backgroundColor: CSSColorValue, val textColor: CSSColorValue) {
    Fresh(Color.green, Color.white),
    Stale(Color.slategray, Color.white),
    Error(Color.red, Color.white),
    Loading(Color.blue, Color.white),
    Refresh(Color.blue, Color.white),
    Empty(Color.white, Color.black)
}

@Composable
fun StatusItem(type: StatusItemType) {
    Div(
        attrs = {
            style {
                classes("card", "center-align")
                backgroundColor(type.backgroundColor)
                minWidth(72.px)
                whiteSpace("nowrap")
                overflow("hidden")
                color(type.textColor)
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