package me.aartikov.replica.single.behaviour.standard

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.client.sendOptimisticAction
import me.aartikov.replica.common.OptimisticUpdate
import me.aartikov.replica.common.ReplicaAction
import me.aartikov.replica.common.internal.OptimisticAction
import me.aartikov.replica.common.internal.OptimisticAction.Command
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.behaviour.ReplicaBehaviour
import me.aartikov.replica.single.withOptimisticUpdate
import kotlin.reflect.KClass

/**
 * [ReplicaBehaviour] that mutates replica data when receiving [ReplicaAction] of a specific type [A].
 *
 * There are two types of actions:
 * - Normal actions are sent with [ReplicaClient.sendAction] and are processed immediately with [PhysicalReplica.mutateData].
 * - Optimistic actions are sent with [ReplicaClient.sendOptimisticAction] and are processed as an optimistic update (see: [withOptimisticUpdate]).
 *
 * [handleNormalActions] specifies whether to process normal actions.
 * [handleOptimisticActions] specifies whether to process optimistic actions.
 */
inline fun <T : Any, reified A : ReplicaAction> ReplicaBehaviour.Companion.mutateOnAction(
    handleNormalActions: Boolean = true,
    handleOptimisticActions: Boolean = true,
    noinline transform: (action: A, data: T) -> T
): ReplicaBehaviour<T> =
    mutateOnAction(A::class, handleNormalActions, handleOptimisticActions, transform)

/**
 * [ReplicaBehaviour] that mutates replica data when receiving [ReplicaAction] of a specific type as defined by [actionClass].
 *
 * There are two types of actions:
 * - Normal actions are sent with [ReplicaClient.sendAction] and are processed immediately with [PhysicalReplica.mutateData].
 * - Optimistic actions are sent with [ReplicaClient.sendOptimisticAction] and are processed as an optimistic update (see: [withOptimisticUpdate]).
 *
 * [handleNormalActions] specifies whether to process normal actions.
 * [handleOptimisticActions] specifies whether to process optimistic actions.
 */
fun <T : Any, A : ReplicaAction> ReplicaBehaviour.Companion.mutateOnAction(
    actionClass: KClass<A>,
    handleNormalActions: Boolean = true,
    handleOptimisticActions: Boolean = true,
    transform: (action: A, data: T) -> T
): ReplicaBehaviour<T> =
    MutateOnAction(actionClass, handleNormalActions, handleOptimisticActions, transform)

private class MutateOnAction<T : Any, A : ReplicaAction>(
    private val actionClass: KClass<A>,
    private val handleNormalActions: Boolean,
    private val handleOptimisticActions: Boolean,
    private val transform: (action: A, data: T) -> T
) : ReplicaBehaviour<T> {

    @Suppress("UNCHECKED_CAST")
    override fun setup(replicaClient: ReplicaClient, replica: PhysicalReplica<T>) {
        replicaClient.actions
            .onEach { action ->
                when {
                    handleNormalActions && actionClass.isInstance(action) -> {
                        replica.mutateData { transform(action as A, it) }
                    }

                    handleOptimisticActions && action is OptimisticAction
                            && actionClass.isInstance(action.sourceAction) -> {
                        val update = OptimisticUpdate<T> { transform(action.sourceAction as A, it) }
                        replica.performOptimisticUpdate(update, action.command, action.operationId)
                    }
                }
            }
            .launchIn(replica.coroutineScope)
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