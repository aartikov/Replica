package me.aartikov.replica.paged

import me.aartikov.replica.common.LoadingReason

sealed interface PagedReplicaEvent<out I : Any, out P : Page<I>> {

    sealed interface LoadingEvent<out I : Any, out P : Page<I>> : PagedReplicaEvent<I, P> {

        val reason: LoadingReason

        data class LoadingStarted(
            override val reason: LoadingReason
        ) : LoadingEvent<Nothing, Nothing>

        sealed interface LoadingFinished<out I : Any, out P : Page<I>> : LoadingEvent<I, P> {
            data class Success<out I : Any, out P : Page<I>>(
                override val reason: LoadingReason,
                val page: P
            ) : LoadingFinished<I, P>

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