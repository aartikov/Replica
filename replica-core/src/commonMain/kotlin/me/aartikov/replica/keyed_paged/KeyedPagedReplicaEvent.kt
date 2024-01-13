package me.aartikov.replica.keyed_paged

import me.aartikov.replica.common.ReplicaId
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedPhysicalReplica

/**
 * Notifies that something happened in [KeyedPagedPhysicalReplica].
 */
sealed interface KeyedPagedReplicaEvent<K : Any, T : Any, P : Page<T>> {

    class ReplicaCreated<K : Any, T : Any, P : Page<T>>(
        val key: K,
        val replica: PagedPhysicalReplica<T, P>
    ) : KeyedPagedReplicaEvent<K, T, P>

    class ReplicaRemoved<K : Any, T : Any, P : Page<T>>(
        val key: K,
        val replicaId: ReplicaId
    ) : KeyedPagedReplicaEvent<K, T, P>
}