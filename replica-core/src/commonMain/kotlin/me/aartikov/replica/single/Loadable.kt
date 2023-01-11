package me.aartikov.replica.single

import me.aartikov.replica.common.CombinedLoadingError

/**
 * Replica state that can be observed on a UI.
 * In opposite to [ReplicaState] this class contains very limited set of fields.
 */
data class Loadable<out T : Any>(
    val loading: Boolean = false,
    val data: T? = null,
    val error: CombinedLoadingError? = null
)

/**
 * Transforms data with a [transform] functions.
 */
fun <T : Any, R : Any> Loadable<T>.mapData(transform: (T) -> R): Loadable<R> {
    return Loadable(
        loading = loading,
        data = data?.let { transform(it) },
        error = error
    )
}