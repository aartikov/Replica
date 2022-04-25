package me.aartikov.replica.common

/**
 * Specifies how a replica behaves after invalidation.
 */
enum class InvalidationMode {
    DontRefresh, RefreshIfHasObservers, RefreshIfHasActiveObservers, RefreshAlways
}