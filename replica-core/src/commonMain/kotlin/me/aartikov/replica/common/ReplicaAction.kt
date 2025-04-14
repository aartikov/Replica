package me.aartikov.replica.common

import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.paged.behaviour.standard.doOnAction
import me.aartikov.replica.single.behaviour.standard.doOnAction
import me.aartikov.replica.single.behaviour.standard.mutateOnAction

/**
 * Enables broadcasting actions through the [ReplicaClient].
 *
 *  Use [doOnAction] or [mutateOnAction] to process these actions.
 */
interface ReplicaAction