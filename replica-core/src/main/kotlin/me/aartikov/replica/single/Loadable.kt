package me.aartikov.replica.single

import me.aartikov.replica.common.LoadingError

data class Loadable<out T : Any>(
    val data: T? = null,
    val loading: Boolean = false,
    val error: LoadingError? = null
)

fun <T : Any, R : Any> Loadable<T>.mapData(transform: (T) -> R): Loadable<R> {
    return Loadable(
        data = data?.let { transform(it) },
        loading = loading,
        error = error
    )
}