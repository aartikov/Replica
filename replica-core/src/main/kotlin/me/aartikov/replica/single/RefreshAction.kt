package me.aartikov.replica.single

enum class RefreshAction {
    Refresh, RefreshIfHasObservers, RefreshIfHasActiveObservers, DontRefresh
}