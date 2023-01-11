package me.aartikov.replica.single.behaviour

import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.single.behaviour.standard.DoOnCreated
import me.aartikov.replica.single.behaviour.standard.DoOnEvent
import me.aartikov.replica.single.behaviour.standard.DoOnNetworkConnectivityChanged
import me.aartikov.replica.single.behaviour.standard.DoOnStateCondition

/**
 * Allows to add a custom behavior to a replica.
 * All features represented in [ReplicaSettings] a implemented as [ReplicaBehaviour].
 * See also: [DoOnCreated], [DoOnEvent], [DoOnStateCondition], [DoOnNetworkConnectivityChanged].
 */
interface ReplicaBehaviour<T : Any> {

    fun setup(replica: PhysicalReplica<T>)
}