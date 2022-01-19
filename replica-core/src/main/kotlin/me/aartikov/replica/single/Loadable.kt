package me.aartikov.replica.single

import me.aartikov.replica.common.CombinedLoadingError

data class Loadable<out T : Any>(
    val loading: Boolean = false,
    val data: T? = null,
    val error: CombinedLoadingError? = null
)

fun <T : Any, R : Any> Loadable<T>.mapData(transform: (T) -> R): Loadable<R> {
    return Loadable(
        loading = loading,
        data = data?.let { transform(it) },
        error = error
    )
}