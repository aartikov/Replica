package me.aartikov.replica.common

enum class InvalidationMode {
    DontRefresh, RefreshIfHasObservers, RefreshIfHasActiveObservers, RefreshAlways
}