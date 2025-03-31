package me.aartikov.replica.paged.behaviour

import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedPhysicalReplica
import me.aartikov.replica.paged.PagedReplicaSettings
import me.aartikov.replica.paged.behaviour.standard.doOnCreated
import me.aartikov.replica.paged.behaviour.standard.doOnEvent
import me.aartikov.replica.paged.behaviour.standard.doOnNetworkConnectivityChanged
import me.aartikov.replica.paged.behaviour.standard.doOnStateCondition

/**
 * Allows to add a custom behavior to a replica.
 * All features represented in [PagedReplicaSettings] a implemented as [PagedReplicaBehaviour].
 * See also: [doOnCreated], [doOnEvent], [doOnStateCondition], [doOnNetworkConnectivityChanged].
 */
interface PagedReplicaBehaviour<I : Any, P : Page<I>> {

    companion object

    fun setup(replica: PagedPhysicalReplica<I, P>)
}