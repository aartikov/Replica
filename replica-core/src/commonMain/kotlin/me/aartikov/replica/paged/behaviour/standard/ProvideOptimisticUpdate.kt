package me.aartikov.replica.paged.behaviour.standard

import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.client.sendActionWithOptimisticUpdate
import me.aartikov.replica.common.OptimisticUpdate
import me.aartikov.replica.common.ReplicaAction
import me.aartikov.replica.common.internal.OptimisticUpdateAction
import me.aartikov.replica.common.internal.OptimisticUpdateAction.Command
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedPhysicalReplica
import me.aartikov.replica.paged.behaviour.PagedReplicaBehaviour
import kotlin.reflect.KClass

/**
 * Provides [OptimisticUpdate] for a [ReplicaAction] of type [A] that was send with [ReplicaClient.sendActionWithOptimisticUpdate].
 */
inline fun <I : Any, P : Page<I>, reified A : ReplicaAction> PagedReplicaBehaviour.Companion.provideOptimisticUpdate(
    noinline updateProvider: (action: A) -> OptimisticUpdate<List<P>>
): PagedReplicaBehaviour<I, P> = provideOptimisticUpdate(A::class, updateProvider)

/**
 * Provides [OptimisticUpdate] for a [ReplicaAction] of type [A] that was send with [ReplicaClient.sendActionWithOptimisticUpdate].
 */
fun <I : Any, P : Page<I>, A : ReplicaAction> PagedReplicaBehaviour.Companion.provideOptimisticUpdate(
    sourceActionClass: KClass<A>,
    updateProvider: suspend (action: A) -> OptimisticUpdate<List<P>>
): PagedReplicaBehaviour<I, P> {
    return doOnAction { optimisticAction: OptimisticUpdateAction ->
        val sourceAction = optimisticAction.sourceAction
        if (sourceActionClass.isInstance(sourceAction)) {
            @Suppress("UNCHECKED_CAST")
            val update = updateProvider(sourceAction as A)
            performOptimisticUpdate(update, optimisticAction.command, optimisticAction.operationId)
        }
    }
}

private suspend fun <I : Any, P : Page<I>> PagedPhysicalReplica<I, P>.performOptimisticUpdate(
    update: OptimisticUpdate<List<P>>,
    command: Command,
    operationId: String
) = when (command) {
    Command.Begin -> beginOptimisticUpdate(update, operationId)
    Command.Commit -> commitOptimisticUpdate(operationId)
    Command.Rollback -> rollbackOptimisticUpdate(operationId)
}