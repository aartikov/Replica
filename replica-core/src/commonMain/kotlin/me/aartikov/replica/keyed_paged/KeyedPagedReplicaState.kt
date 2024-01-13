package me.aartikov.replica.keyed_paged

/**
 * State of [KeyedPagedPhysicalReplica].
 */
data class KeyedPagedReplicaState(
    val replicaCount: Int,
    val replicaWithObserversCount: Int,
    val replicaWithActiveObserversCount: Int
) {

    companion object {
        val Empty = KeyedPagedReplicaState(
            replicaCount = 0,
            replicaWithObserversCount = 0,
            replicaWithActiveObserversCount = 0
        )
    }
}