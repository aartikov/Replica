package me.aartikov.replica.common

import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.paged.behaviour.PagedReplicaBehaviour
import me.aartikov.replica.paged.behaviour.standard.doOnAction
import me.aartikov.replica.single.behaviour.ReplicaBehaviour
import me.aartikov.replica.single.behaviour.standard.doOnAction

/**
 * Enables broadcasting actions through the [ReplicaClient].
 *
 *  Use [ReplicaBehaviour.Companion.doOnAction], [PagedReplicaBehaviour.Companion.doOnAction] to process these actions.
 */
interface ReplicaAction