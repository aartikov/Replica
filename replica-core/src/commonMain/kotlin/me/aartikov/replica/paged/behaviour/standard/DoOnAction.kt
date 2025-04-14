package me.aartikov.replica.paged.behaviour.standard

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.client.sendOptimisticAction
import me.aartikov.replica.common.ReplicaAction
import me.aartikov.replica.common.internal.OptimisticAction
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedPhysicalReplica
import me.aartikov.replica.paged.behaviour.PagedReplicaBehaviour
import kotlin.reflect.KClass

/**
 * Creates a [PagedReplicaBehaviour] that processes [ReplicaAction] actions of a specific type [A].
 *
 * There are two types of actions:
 * - Normal actions are sent with [ReplicaClient.sendAction] and are processed immediately.
 * - Optimistic actions are sent with [ReplicaClient.sendOptimisticAction] and are processed only when
 *   the corresponding optimistic update is committed.
 *
 * [handleNormalActions] specifies whether to process normal actions.
 * [handleOptimisticActions] specifies whether to process optimistic actions.
 */
inline fun <I : Any, P : Page<I>, reified A : ReplicaAction> PagedReplicaBehaviour.Companion.doOnAction(
    handleNormalActions: Boolean = true,
    handleOptimisticActions: Boolean = true,
    noinline handler: suspend PagedPhysicalReplica<I, P>.(action: A) -> Unit
): PagedReplicaBehaviour<I, P> =
    doOnAction(A::class, handleNormalActions, handleOptimisticActions, handler)

/**
 * Creates a [PagedReplicaBehaviour] that processes [ReplicaAction] actions of a specific type as defined by [actionClass].
 *
 * There are two types of actions:
 * - Normal actions are sent with [ReplicaClient.sendAction] and are processed immediately.
 * - Optimistic actions are sent with [ReplicaClient.sendOptimisticAction] and are processed only when
 *   the corresponding optimistic update is committed.
 *
 * [handleNormalActions] specifies whether to process normal actions.
 * [handleOptimisticActions] specifies whether to process optimistic actions.
 */
fun <I : Any, P : Page<I>, A : ReplicaAction> PagedReplicaBehaviour.Companion.doOnAction(
    actionClass: KClass<A>,
    handleNormalActions: Boolean = true,
    handleOptimisticActions: Boolean = true,
    handler: suspend PagedPhysicalReplica<I, P>.(action: A) -> Unit
): PagedReplicaBehaviour<I, P> =
    DoOnAction(actionClass, handleNormalActions, handleOptimisticActions, handler)

private class DoOnAction<I : Any, P : Page<I>, A : ReplicaAction>(
    private val actionClass: KClass<A>,
    private val handleNormalActions: Boolean,
    private val handleOptimisticActions: Boolean,
    private val handler: suspend PagedPhysicalReplica<I, P>.(action: A) -> Unit
) : PagedReplicaBehaviour<I, P> {

    @Suppress("UNCHECKED_CAST")
    override fun setup(replicaClient: ReplicaClient, pagedReplica: PagedPhysicalReplica<I, P>) {
        replicaClient.actions
            .onEach { action ->
                when {
                    handleNormalActions && actionClass.isInstance(action) -> {
                        pagedReplica.handler(action as A)
                    }

                    handleOptimisticActions && action is OptimisticAction &&
                            action.command == OptimisticAction.Command.Commit &&
                            actionClass.isInstance(action.sourceAction) -> {
                        pagedReplica.handler(action.sourceAction as A)
                    }
                }
            }
            .launchIn(pagedReplica.coroutineScope)
    }
}