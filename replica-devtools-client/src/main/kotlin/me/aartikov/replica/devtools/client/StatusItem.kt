package me.aartikov.replica.devtools.client

import androidx.compose.runtime.Composable
import me.aartikov.replica.devtools.dto.ReplicaStateDto
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

enum class StatusItemType {
    Fresh,
    Stale,
    Error,
    Loading,
    Refresh,
    Empty;

    fun getColors(theme: Theme): Pair<CSSColorValue, CSSColorValue> {
        return when (this) {
            Loading -> theme.primary to theme.onPrimary
            Fresh -> theme.secondaryColor to Color.white
            Error -> theme.error to theme.onError
            Refresh -> theme.primary to theme.onPrimary
            Empty -> theme.onBackground to theme.background
            Stale -> Color.slategray to Color.white
        }
    }
}

@Composable
fun StatusItem(type: StatusItemType) {
    val theme = LocalTheme.current
    val (color, textColor) = type.getColors(theme)

    Div(
        attrs = {
            style {
                classes("card", "center-align")
                backgroundColor(color)
                minWidth(72.px)
                whiteSpace("nowrap")
                overflow("hidden")
                color(textColor)
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