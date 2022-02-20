package me.aartikov.replica.devtools.client.view_data

import me.aartikov.replica.devtools.client.StatusColor
import me.aartikov.replica.devtools.client.Theme
import me.aartikov.replica.devtools.dto.ReplicaStateDto

enum class StatusItemType {
    Fresh,
    Stale,
    Error,
    Loading,
    Refresh,
    Empty;

    fun getColor(theme: Theme): StatusColor {
        return when (this) {
            Loading -> theme.loadingStatusColor
            Fresh -> theme.freshStatusColor
            Error -> theme.errorStatusColor
            Refresh -> theme.loadingStatusColor
            Empty -> theme.emptyStatusColor
            Stale -> theme.staleStatusColor
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
        else -> StatusItemType.Empty
    }
}