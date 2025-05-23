package me.aartikov.replica.single.behaviour

import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.single.behaviour.standard.doOnAction
import me.aartikov.replica.single.behaviour.standard.doOnCreated
import me.aartikov.replica.single.behaviour.standard.doOnEvent
import me.aartikov.replica.single.behaviour.standard.doOnNetworkConnectivityChanged
import me.aartikov.replica.single.behaviour.standard.doOnStateCondition

/**
 * Allows to add a custom behavior to a replica.
 * All features represented in [ReplicaSettings] a implemented as [ReplicaBehaviour].
 * See also: [doOnCreated], [doOnEvent], [doOnAction], [doOnStateCondition], [doOnNetworkConnectivityChanged].
 */
interface ReplicaBehaviour<T : Any> {

    companion object

    fun setup(replicaClient: ReplicaClient, replica: PhysicalReplica<T>)
}