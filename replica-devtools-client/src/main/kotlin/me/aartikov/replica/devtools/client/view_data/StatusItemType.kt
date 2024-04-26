package me.aartikov.replica.devtools.client.view_data

import me.aartikov.replica.devtools.client.StatusColor
import me.aartikov.replica.devtools.client.Theme
import me.aartikov.replica.devtools.dto.ReplicaStateDto

enum class StatusItemType(val value: String) {
    Fresh("Fresh"),
    Stale("Stale"),
    Error("Error"),
    Loading("Loading"),
    Refresh("Refresh"),
    LoadingNextPage("Next"),
    LoadingPreviousPage("Previous"),
    Empty("Empty");

    fun getColor(theme: Theme): StatusColor {
        return when (this) {
            Loading, LoadingNextPage, LoadingPreviousPage -> theme.loadingStatusColor
            Fresh -> theme.freshStatusColor
            Error -> theme.errorStatusColor
            Refresh -> theme.loadingStatusColor
            Empty -> theme.emptyStatusColor
            Stale -> theme.staleStatusColor
        }
    }
}

fun ReplicaStateDto.toStatusItemType(): StatusItemType {
    val loading = loadingFirstPage || loadingNextPage || loadingPreviousPage

    return when {
        loadingNextPage -> StatusItemType.LoadingNextPage
        loadingPreviousPage -> StatusItemType.LoadingPreviousPage
        hasData && !loading -> StatusItemType.Fresh
        hasData && !loading && !dataIsFresh -> StatusItemType.Stale
        !hasData && hasError &&  !loading-> StatusItemType.Error
        !hasData && loadingFirstPage -> StatusItemType.Loading
        hasData && loadingFirstPage -> StatusItemType.Refresh
        else -> StatusItemType.Empty
    }
}
