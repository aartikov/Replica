package me.aartikov.replica.single

data class Loadable<out T : Any>(
    val data: T? = null,
    val loading: Boolean = false,
    val error: Exception? = null
)

fun <T : Any, R : Any> Loadable<T>.mapData(transform: (T) -> R): Loadable<R> {
    return Loadable(
        data = data?.let { transform(it) },
        loading = loading,
        error = error
    )
}