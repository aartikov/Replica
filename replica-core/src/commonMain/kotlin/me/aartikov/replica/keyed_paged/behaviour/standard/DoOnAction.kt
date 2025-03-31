package me.aartikov.replica.keyed_paged.behaviour.standard

import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.common.ReplicaAction
import me.aartikov.replica.keyed_paged.KeyedPagedPhysicalReplica
import me.aartikov.replica.keyed_paged.behaviour.KeyedPagedReplicaBehaviour
import me.aartikov.replica.paged.Page
import kotlin.reflect.KClass

/**
 * Creates a [KeyedPagedReplicaBehaviour] that processes [ReplicaAction] actions of a specific type as defined by [actionClass].
 */
fun <K : Any, I : Any, P : Page<I>, A : ReplicaAction> KeyedPagedReplicaBehaviour.Companion.doOnAction(
    actionClass: KClass<ReplicaAction>,
    handler: suspend KeyedPagedPhysicalReplica<K, I, P>.(action: ReplicaAction) -> Unit
): KeyedPagedReplicaBehaviour<K, I, P> = DoOnAction(actionClass, handler)

private class DoOnAction<K : Any, I : Any, P : Page<I>>(
    private val actionClass: KClass<ReplicaAction>,
    private val handler: suspend KeyedPagedPhysicalReplica<K, I, P>.(action: ReplicaAction) -> Unit
) : KeyedPagedReplicaBehaviour<K, I, P> {

    override fun setup(
        replicaClient: ReplicaClient,
        keyedPagedReplica: KeyedPagedPhysicalReplica<K, I, P>
    ) {
        replicaClient.actions
            .filterIsInstance(actionClass)
            .onEach { action ->
                keyedPagedReplica.handler(action)
            }
            .launchIn(keyedPagedReplica.coroutineScope)
    }
}