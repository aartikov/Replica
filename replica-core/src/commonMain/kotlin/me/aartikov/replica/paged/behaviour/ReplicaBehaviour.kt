package me.aartikov.replica.paged.behaviour

import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedPhysicalReplica
import me.aartikov.replica.paged.PagedReplicaSettings
import me.aartikov.replica.paged.behaviour.standard.PagedDoOnCreated
import me.aartikov.replica.paged.behaviour.standard.PagedDoOnEvent
import me.aartikov.replica.paged.behaviour.standard.PagedDoOnNetworkConnectivityChanged
import me.aartikov.replica.paged.behaviour.standard.PagedDoOnStateCondition

/**
 * Allows to add a custom behavior to a replica.
 * All features represented in [PagedReplicaSettings] a implemented as [PagedReplicaBehaviour].
 * See also: [PagedDoOnCreated], [PagedDoOnEvent], [PagedDoOnStateCondition], [PagedDoOnNetworkConnectivityChanged].
 */
interface PagedReplicaBehaviour<I : Any, P : Page<I>> {

    fun setup(replica: PagedPhysicalReplica<I, P>)
}