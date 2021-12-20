package me.aartikov.replica.simple

sealed interface ReplicaEvent<out T : Any> {

    interface ErrorEvent : ReplicaEvent<Nothing> {
        val error: Exception
    }

    sealed interface LoadingEvent<out T : Any> : ReplicaEvent<T> {
        object LoadingStarted : LoadingEvent<Nothing>

        sealed interface LoadingFinished<out T : Any> : LoadingEvent<T> {
            data class Success<out T : Any>(val data: T) : LoadingFinished<T>
            object Canceled : LoadingFinished<Nothing>
            data class Error(override val error: Exception) : LoadingFinished<Nothing>, ErrorEvent
        }
    }

    sealed interface DataChangingEvent<out T : Any> : ReplicaEvent<T> {
        data class DataSet<out T : Any>(val data: T) : DataChangingEvent<T>
        data class DataMutated<out T : Any>(val data: T) : DataChangingEvent<T>
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