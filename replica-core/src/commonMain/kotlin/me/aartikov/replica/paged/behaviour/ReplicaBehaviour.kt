package me.aartikov.replica.paged.behaviour

import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedPhysicalReplica
import me.aartikov.replica.paged.PagedReplicaSettings
import me.aartikov.replica.paged.behaviour.standard.DoOnCreated
import me.aartikov.replica.paged.behaviour.standard.DoOnEvent
import me.aartikov.replica.paged.behaviour.standard.DoOnNetworkConnectivityChanged
import me.aartikov.replica.paged.behaviour.standard.DoOnStateCondition

/**
 * Allows to add a custom behavior to a replica.
 * All features represented in [PagedReplicaSettings] a implemented as [PagedReplicaBehaviour].
 * See also: [DoOnCreated], [DoOnEvent], [DoOnStateCondition], [DoOnNetworkConnectivityChanged].
 */
interface PagedReplicaBehaviour<T : Any, P : Page<T>> {

    fun setup(replica: PagedPhysicalReplica<T, P>)
}