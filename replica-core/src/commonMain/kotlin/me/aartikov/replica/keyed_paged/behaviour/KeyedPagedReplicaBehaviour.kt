package me.aartikov.replica.keyed_paged.behaviour

import me.aartikov.replica.keyed_paged.KeyedPagedPhysicalReplica
import me.aartikov.replica.paged.Page

/**
 * Allows to add a custom behavior to a keyed paged replica.
 */
interface KeyedPagedReplicaBehaviour<K : Any, T : Any, P : Page<T>> {

    fun setup(keyedPagedReplica: KeyedPagedPhysicalReplica<K, T, P>)
}