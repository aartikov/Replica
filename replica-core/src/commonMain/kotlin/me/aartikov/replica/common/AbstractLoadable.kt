package me.aartikov.replica.common

interface AbstractLoadable<out T : Any> {

    val loading: Boolean

    val data: T?

    val error: CombinedLoadingError?
}