package me.aartikov.replica.single.behaviour.standard

import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.client.sendActionWithOptimisticUpdate
import me.aartikov.replica.common.OptimisticUpdate
import me.aartikov.replica.common.ReplicaAction
import me.aartikov.replica.common.internal.OptimisticUpdateAction
import me.aartikov.replica.common.internal.OptimisticUpdateAction.Command
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.behaviour.ReplicaBehaviour
import kotlin.reflect.KClass

/**
 * Provides [OptimisticUpdate] for a [ReplicaAction] of type [A] that was send with [ReplicaClient.sendActionWithOptimisticUpdate].
 */
inline fun <T : Any, reified A : ReplicaAction> ReplicaBehaviour.Companion.provideOptimisticUpdate(
    noinline updateProvider: (action: A) -> OptimisticUpdate<T>
): ReplicaBehaviour<T> = provideOptimisticUpdate(A::class, updateProvider)

/**
 * Provides [OptimisticUpdate] for a [ReplicaAction] of type [A] that was send with [ReplicaClient.sendActionWithOptimisticUpdate].
 */
fun <T : Any, A : ReplicaAction> ReplicaBehaviour.Companion.provideOptimisticUpdate(
    sourceActionClass: KClass<A>,
    updateProvider: (action: A) -> OptimisticUpdate<T>
): ReplicaBehaviour<T> {
    return doOnAction { optimisticAction: OptimisticUpdateAction ->
        val sourceAction = optimisticAction.sourceAction
        if (sourceActionClass.isInstance(sourceAction)) {
            @Suppress("UNCHECKED_CAST")
            val update = updateProvider(sourceAction as A)
            performOptimisticUpdate(update, optimisticAction.command, optimisticAction.operationId)
        }
    }
}

private suspend fun <T : Any> PhysicalReplica<T>.performOptimisticUpdate(
    update: OptimisticUpdate<T>,
    command: Command,
    operationId: String
) = when (command) {
    Command.Begin -> beginOptimisticUpdate(update, operationId)
    Command.Commit -> commitOptimisticUpdate(operationId)
    Command.Rollback -> rollbackOptimisticUpdate(operationId)
}