package me.aartikov.replica.single

sealed interface ReplicaEvent<out T : Any> {

    sealed interface LoadingEvent<out T : Any> : ReplicaEvent<T> {
        object LoadingStarted : LoadingEvent<Nothing>

        sealed interface LoadingFinished<out T : Any> : LoadingEvent<T> {
            data class Success<out T : Any>(val data: T) : LoadingFinished<T>
            object Canceled : LoadingFinished<Nothing>
            data class Error(val exception: Exception) : LoadingFinished<Nothing>
        }
    }

    sealed interface FreshnessEvent : ReplicaEvent<Nothing> {
        object Freshened : FreshnessEvent
        object BecameStale : FreshnessEvent
    }

    object ClearedEvent : ReplicaEvent<Nothing>

    data class ObserverCountChanged(
        val count: Int,
        val activeCount: Int,
        val previousCount: Int,
        val previousActiveCount: Int
    ) : ReplicaEvent<Nothing>
}