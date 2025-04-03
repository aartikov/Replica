package me.aartikov.replica.single.behaviour.standard

import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.common.ReplicaAction
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.behaviour.ReplicaBehaviour
import kotlin.reflect.KClass

/**
 * Creates a [ReplicaBehaviour] that processes [ReplicaAction] actions of a specific type [A].
 */
inline fun <T : Any, reified A : ReplicaAction> ReplicaBehaviour.Companion.doOnAction(
    noinline handler: suspend PhysicalReplica<T>.(action: A) -> Unit
): ReplicaBehaviour<T> = doOnAction(A::class, handler)

/**
 * Creates a [ReplicaBehaviour] that processes [ReplicaAction] actions of a specific type as defined by [actionClass].
 */
fun <T : Any, A : ReplicaAction> ReplicaBehaviour.Companion.doOnAction(
    actionClass: KClass<A>,
    handler: suspend PhysicalReplica<T>.(action: A) -> Unit
): ReplicaBehaviour<T> = DoOnAction(actionClass, handler)

private class DoOnAction<T : Any, A : ReplicaAction>(
    private val actionClass: KClass<A>,
    private val handler: suspend PhysicalReplica<T>.(action: A) -> Unit
) : ReplicaBehaviour<T> {

    override fun setup(replicaClient: ReplicaClient, replica: PhysicalReplica<T>) {
        replicaClient.actions
            .filterIsInstance(actionClass)
            .onEach { action ->
                replica.handler(action)
            }
            .launchIn(replica.coroutineScope)
    }
}