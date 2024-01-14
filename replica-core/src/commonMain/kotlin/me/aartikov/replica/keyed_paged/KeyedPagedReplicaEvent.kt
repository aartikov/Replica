package me.aartikov.replica.keyed_paged

import me.aartikov.replica.common.ReplicaId
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedPhysicalReplica

/**
 * Notifies that something happened in [KeyedPagedPhysicalReplica].
 */
sealed interface KeyedPagedReplicaEvent<K : Any, I : Any, P : Page<I>> {

    class ReplicaCreated<K : Any, I : Any, P : Page<I>>(
        val key: K,
        val replica: PagedPhysicalReplica<I, P>
    ) : KeyedPagedReplicaEvent<K, I, P>

    class ReplicaRemoved<K : Any, I : Any, P : Page<I>>(
        val key: K,
        val replicaId: ReplicaId
    ) : KeyedPagedReplicaEvent<K, I, P>
}