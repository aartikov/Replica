package me.aartikov.replica.paged

enum class PagedLoadingStatus {
    None,
    LoadingFirstPage,
    LoadingNextPage,
    LoadingPreviousPage
}