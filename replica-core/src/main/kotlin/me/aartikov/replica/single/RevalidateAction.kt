package me.aartikov.replica.single

enum class RevalidateAction {
    Revalidate, RevalidateIfHasObservers, RevalidateIfHasActiveObservers, DontRevalidate
}