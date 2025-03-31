package me.aartikov.replica.single

import me.aartikov.replica.common.AbstractLoadable
import me.aartikov.replica.common.CombinedLoadingError

/**
 * Represents a replica state that can be observed in the UI.
 * Unlike [ReplicaState], this class contains a limited set of fields.
 */
data class Loadable<out T : Any>(
    override val loading: Boolean = false,
    override val data: T? = null,
    override val error: CombinedLoadingError? = null
) : AbstractLoadable<T>

/**
 * Transforms the data using the given [transform] function.
 *
 * @param transform A function that converts data of type T to type R.
 * @return A new [Loadable] instance containing the transformed data.
 */
fun <T : Any, R : Any> Loadable<T>.mapData(transform: (T) -> R): Loadable<R> {
    return Loadable(
        loading = loading,
        data = data?.let { transform(it) },
        error = error
    )
}