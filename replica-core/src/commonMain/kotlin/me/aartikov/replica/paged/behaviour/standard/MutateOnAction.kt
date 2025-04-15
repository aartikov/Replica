package me.aartikov.replica.paged.behaviour.standard

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.client.sendOptimisticAction
import me.aartikov.replica.common.OptimisticUpdate
import me.aartikov.replica.common.ReplicaAction
import me.aartikov.replica.common.internal.OptimisticAction
import me.aartikov.replica.common.internal.OptimisticAction.Command
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedPhysicalReplica
import me.aartikov.replica.paged.behaviour.PagedReplicaBehaviour
import me.aartikov.replica.paged.withOptimisticUpdate
import kotlin.reflect.KClass

/**
 * [PagedReplicaBehaviour] that mutates replica data when receiving [ReplicaAction] of a specific type [A].
 *
 * There are two types of actions:
 * - Normal actions are sent with [ReplicaClient.sendAction] and are processed immediately with [PagedPhysicalReplica.mutateData].
 * - Optimistic actions are sent with [ReplicaClient.sendOptimisticAction] and are processed as an optimistic update (see: [withOptimisticUpdate]).
 *
 * [handleNormalActions] specifies whether to process normal actions.
 * [handleOptimisticActions] specifies whether to process optimistic actions.
 */
inline fun <I : Any, P : Page<I>, reified A : ReplicaAction> PagedReplicaBehaviour.Companion.mutateOnAction(
    handleNormalActions: Boolean = true,
    handleOptimisticActions: Boolean = true,
    noinline transform: (action: A, data: List<P>) -> List<P>
): PagedReplicaBehaviour<I, P> =
    mutateOnAction(A::class, handleNormalActions, handleOptimisticActions, transform)

/**
 * [PagedReplicaBehaviour] that mutates replica data when receiving [ReplicaAction] of a specific type as defined by [actionClass].
 *
 * There are two types of actions:
 * - Normal actions are sent with [ReplicaClient.sendAction] and are processed immediately with [PagedPhysicalReplica.mutateData].
 * - Optimistic actions are sent with [ReplicaClient.sendOptimisticAction] and are processed as an optimistic update (see: [withOptimisticUpdate]).
 *
 * [handleNormalActions] specifies whether to process normal actions.
 * [handleOptimisticActions] specifies whether to process optimistic actions.
 */
fun <I : Any, P : Page<I>, A : ReplicaAction> PagedReplicaBehaviour.Companion.mutateOnAction(
    actionClass: KClass<A>,
    handleNormalActions: Boolean = true,
    handleOptimisticActions: Boolean = true,
    transform: (action: A, data: List<P>) -> List<P>
): PagedReplicaBehaviour<I, P> =
    MutateOnAction(actionClass, handleNormalActions, handleOptimisticActions, transform)

private class MutateOnAction<I : Any, P : Page<I>, A : ReplicaAction>(
    private val actionClass: KClass<A>,
    private val handleNormalActions: Boolean,
    private val handleOptimisticActions: Boolean,
    private val transform: (action: A, data: List<P>) -> List<P>
) : PagedReplicaBehaviour<I, P> {

    @Suppress("UNCHECKED_CAST")
    override fun setup(replicaClient: ReplicaClient, pagedReplica: PagedPhysicalReplica<I, P>) {
        replicaClient.actions
            .onEach { action ->
                when {
                    handleNormalActions && actionClass.isInstance(action) -> {
                        pagedReplica.mutateData { transform(action as A, it) }
                    }

                    handleOptimisticActions && action is OptimisticAction
                            && actionClass.isInstance(action.sourceAction) -> {
                        val update = OptimisticUpdate<List<P>> {
                            transform(action.sourceAction as A, it)
                        }
                        pagedReplica.performOptimisticUpdate(
                            update, action.command, action.operationId
                        )
                    }
                }
            }
            .launchIn(pagedReplica.coroutineScope)
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