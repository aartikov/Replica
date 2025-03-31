package me.aartikov.replica.keyed.behaviour.standard

import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.common.ReplicaAction
import me.aartikov.replica.keyed.KeyedPhysicalReplica
import me.aartikov.replica.keyed.behaviour.KeyedReplicaBehaviour
import kotlin.reflect.KClass

/**
 * Creates a [KeyedReplicaBehaviour] that processes [ReplicaAction] actions of a specific type as defined by [actionClass].
 */
fun <K : Any, T : Any> KeyedReplicaBehaviour.Companion.doOnAction(
    actionClass: KClass<ReplicaAction>,
    handler: suspend KeyedPhysicalReplica<K, T>.(action: ReplicaAction) -> Unit
): KeyedReplicaBehaviour<K, T> = DoOnAction(actionClass, handler)

internal class DoOnAction<K : Any, T : Any, A : ReplicaAction>(
    private val actionClass: KClass<A>,
    private val handler: suspend KeyedPhysicalReplica<K, T>.(action: A) -> Unit
) : KeyedReplicaBehaviour<K, T> {

    override fun setup(replicaClient: ReplicaClient, keyedReplica: KeyedPhysicalReplica<K, T>) {
        replicaClient.actions
            .filterIsInstance(actionClass)
            .onEach { action ->
                keyedReplica.handler(action)
            }
            .launchIn(keyedReplica.coroutineScope)
    }
}