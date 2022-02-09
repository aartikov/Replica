package me.aartikov.replica.devtools.client.view_data

import me.aartikov.replica.devtools.client.Theme
import me.aartikov.replica.devtools.dto.ReplicaStateDto
import org.jetbrains.compose.web.css.CSSColorValue
import org.jetbrains.compose.web.css.Color

enum class StatusItemType {
    Fresh,
    Stale,
    Error,
    Loading,
    Refresh,
    Empty;

    fun getColors(theme: Theme): Pair<CSSColorValue, CSSColorValue> {
        return when (this) {
            Loading -> theme.defaultStatusColor to theme.statusTextColor
            Fresh -> theme.freshStatusColor to Color.white
            Error -> theme.errorStatusColor to theme.statusTextColor
            Refresh -> theme.defaultStatusColor to theme.statusTextColor
            Empty -> theme.textColor to theme.backgroundColor
            Stale -> Color.slategray to Color.white
        }
    }
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