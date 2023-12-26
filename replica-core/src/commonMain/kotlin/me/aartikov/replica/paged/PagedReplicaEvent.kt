package me.aartikov.replica.paged

sealed interface PagedReplicaEvent<out T : Any, out P : Page<T>> {

    sealed interface LoadingEvent<out T : Any, out P : Page<T>> : PagedReplicaEvent<T, P> {
        data object LoadingStarted : LoadingEvent<Nothing, Nothing>

        sealed interface LoadingFinished<out T : Any, out P : Page<T>> : LoadingEvent<T, P> {
            data class Success<out T : Any, out P : Page<T>>(val data: T) : LoadingFinished<T, P>
            data object Canceled : LoadingFinished<Nothing, Nothing>
            data class Error(val exception: Exception) : LoadingFinished<Nothing, Nothing>
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