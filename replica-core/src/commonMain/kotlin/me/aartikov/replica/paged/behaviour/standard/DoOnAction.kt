package me.aartikov.replica.paged.behaviour.standard

import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.common.ReplicaAction
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedPhysicalReplica
import me.aartikov.replica.paged.behaviour.PagedReplicaBehaviour
import kotlin.reflect.KClass

/**
 * Creates a [PagedReplicaBehaviour] that processes [ReplicaAction] actions of a specific type as defined by [actionClass].
 */
fun <I : Any, P : Page<I>, A : ReplicaAction> PagedReplicaBehaviour.Companion.doOnAction(
    actionClass: KClass<A>,
    handler: suspend PagedPhysicalReplica<I, P>.(action: A) -> Unit
): PagedReplicaBehaviour<I, P> = DoOnAction(actionClass, handler)

private class DoOnAction<I : Any, P : Page<I>, A : ReplicaAction>(
    private val actionClass: KClass<A>,
    private val handler: suspend PagedPhysicalReplica<I, P>.(action: A) -> Unit
) : PagedReplicaBehaviour<I, P> {

    override fun setup(replicaClient: ReplicaClient, pagedReplica: PagedPhysicalReplica<I, P>) {
        replicaClient.actions
            .filterIsInstance(actionClass)
            .onEach { action ->
                pagedReplica.handler(action)
            }
            .launchIn(pagedReplica.coroutineScope)
    }
}