package me.aartikov.replica.single

enum class RefreshCondition {
    Never, IfHasObservers, IfHasActiveObservers, Always
}