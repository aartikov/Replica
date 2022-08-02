package me.aartikov.replica.keyed

/**
 * State of [KeyedPhysicalReplica].
 */
data class KeyedReplicaState(
    val replicaCount: Int,
    val replicaWithObserversCount: Int,
    val replicaWithActiveObserversCount: Int
) {

    companion object {
        val Empty = KeyedReplicaState(
            replicaCount = 0,
            replicaWithObserversCount = 0,
            replicaWithActiveObserversCount = 0
        )
    }
}