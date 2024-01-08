package me.aartikov.replica.paged

import me.aartikov.replica.common.LoadingReason

sealed interface PagedReplicaEvent<out T : Any, out P : Page<T>> {

    sealed interface LoadingEvent<out T : Any, out P : Page<T>> : PagedReplicaEvent<T, P> {

        val reason: LoadingReason

        data class LoadingStarted(
            override val reason: LoadingReason
        ) : LoadingEvent<Nothing, Nothing>

        sealed interface LoadingFinished<out T : Any, out P : Page<T>> : LoadingEvent<T, P> {
            data class Success<out T : Any, out P : Page<T>>(
                override val reason: LoadingReason,
                val page: P
            ) : LoadingFinished<T, P>

            data class Canceled(
                override val reason: LoadingReason,
            ) : LoadingFinished<Nothing, Nothing>

            data class Error(
                override val reason: LoadingReason,
                val exception: Exception
            ) : LoadingFinished<Nothing, Nothing>
        }
    }

    sealed interface FreshnessEvent : PagedReplicaEvent<Nothing, Nothing> {
        data object Freshened : FreshnessEvent
        data object BecameStale : FreshnessEvent
    }

    data object ClearedEvent : PagedReplicaEvent<Nothing, Nothing>

    data class ObserverCountChangedEvent(
        val count: Int,
        val activeCount: Int,
        val previousCount: Int,
        val previousActiveCount: Int
    ) : PagedReplicaEvent<Nothing, Nothing>
}