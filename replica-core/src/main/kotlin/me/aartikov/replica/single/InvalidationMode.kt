package me.aartikov.replica.single

enum class InvalidationMode {
    DontRefresh, RefreshIfHasObservers, RefreshIfHasActiveObservers, RefreshAlways
}