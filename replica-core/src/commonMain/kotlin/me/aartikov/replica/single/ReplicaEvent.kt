package me.aartikov.replica.single

/**
 * Notifies that something happened in [PhysicalReplica].
 */
sealed interface ReplicaEvent<out T : Any> {

    sealed interface LoadingEvent<out T : Any> : ReplicaEvent<T> {
        data object LoadingStarted : LoadingEvent<Nothing>

        data class DataFromStorageLoaded<out T : Any>(val data: T) : LoadingEvent<T>

        sealed interface LoadingFinished<out T : Any> : LoadingEvent<T> {
            data class Success<out T : Any>(val data: T) : LoadingFinished<T>
            data object Canceled : LoadingFinished<Nothing>
            data class Error(val exception: Exception) : LoadingFinished<Nothing>
        }
    }

    sealed interface FreshnessEvent : ReplicaEvent<Nothing> {
        data object Freshened : FreshnessEvent
        data object BecameStale : FreshnessEvent
    }

    data object ClearedEvent : ReplicaEvent<Nothing>

    data class ObserverCountChangedEvent(
        val count: Int,
        val activeCount: Int,
        val previousCount: Int,
        val previousActiveCount: Int
    ) : ReplicaEvent<Nothing>
}