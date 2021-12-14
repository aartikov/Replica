package me.aartikov.replica.simple

sealed interface ReplicaEvent<out T : Any> {

    interface ErrorEvent : ReplicaEvent<Nothing> {
        val error: Exception
    }

    sealed interface LoadingEvent<out T : Any> : ReplicaEvent<T> {
        object LoadingStarted : LoadingEvent<Nothing>
        data class DataLoaded<T : Any>(val data: T) : LoadingEvent<T>
        object LoadingCanceled : LoadingEvent<Nothing>
        data class LoadingError(override val error: Exception) : LoadingEvent<Nothing>, ErrorEvent
    }

    sealed interface FreshnessEvent : ReplicaEvent<Nothing> {
        object Freshened : FreshnessEvent
        object BecameStale : FreshnessEvent
    }

    data class ObserverCountChanged(
        val count: Int,
        val activeCount: Int,
        val previousCount: Int,
        val previousActiveCount: Int
    ) : ReplicaEvent<Nothing>
}