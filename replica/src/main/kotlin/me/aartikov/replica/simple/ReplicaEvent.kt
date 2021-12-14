package me.aartikov.replica.simple

sealed interface ReplicaEvent<out T : Any> {

    sealed interface Error : ReplicaEvent<Nothing> {
        val error: Exception

        data class LoadingError(override val error: Exception) : Error
    }

    sealed interface Freshness : ReplicaEvent<Nothing> {
        object BecameFresh : Freshness
        data class BecameStale(val reason: StaleReason) : Freshness
    }

    data class ObserverCountChanged(
        val count: Int,
        val activeCount: Int,
        val previousCount: Int,
        val previousActiveCount: Int
    ) : ReplicaEvent<Nothing>
}