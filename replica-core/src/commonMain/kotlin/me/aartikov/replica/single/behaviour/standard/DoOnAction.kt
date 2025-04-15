package me.aartikov.replica.single.behaviour.standard

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.client.sendOptimisticAction
import me.aartikov.replica.common.ReplicaAction
import me.aartikov.replica.common.internal.OptimisticAction
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.behaviour.ReplicaBehaviour
import kotlin.reflect.KClass

/**
 * Creates a [ReplicaBehaviour] that processes [ReplicaAction] actions of a specific type [A].
 *
 * There are two types of actions:
 * - Normal actions are sent with [ReplicaClient.sendAction] and are processed immediately.
 * - Optimistic actions are sent with [ReplicaClient.sendOptimisticAction] and are processed only when
 *   the corresponding optimistic update is committed.
 *
 * [handleNormalActions] specifies whether to process normal actions.
 * [handleOptimisticActions] specifies whether to process optimistic actions.
 */
inline fun <T : Any, reified A : ReplicaAction> ReplicaBehaviour.Companion.doOnAction(
    handleNormalActions: Boolean = true,
    handleOptimisticActions: Boolean = true,
    noinline handler: suspend PhysicalReplica<T>.(action: A) -> Unit
): ReplicaBehaviour<T> = doOnAction(A::class, handleNormalActions, handleOptimisticActions, handler)

/**
 * Creates a [ReplicaBehaviour] that processes [ReplicaAction] actions of a specific type as defined by [actionClass].
 *
 * There are two types of actions:
 * - Normal actions are sent with [ReplicaClient.sendAction] and are processed immediately.
 * - Optimistic actions are sent with [ReplicaClient.sendOptimisticAction] and are processed only when
 *   the corresponding optimistic update is committed.
 *
 * [handleNormalActions] specifies whether to process normal actions.
 * [handleOptimisticActions] specifies whether to process optimistic actions.
 */
fun <T : Any, A : ReplicaAction> ReplicaBehaviour.Companion.doOnAction(
    actionClass: KClass<A>,
    handleNormalActions: Boolean = true,
    handleOptimisticActions: Boolean = true,
    handler: suspend PhysicalReplica<T>.(action: A) -> Unit
): ReplicaBehaviour<T> =
    DoOnAction(actionClass, handleNormalActions, handleOptimisticActions, handler)

private class DoOnAction<T : Any, A : ReplicaAction>(
    private val actionClass: KClass<A>,
    private val handleNormalActions: Boolean,
    private val handleOptimisticActions: Boolean,
    private val handler: suspend PhysicalReplica<T>.(action: A) -> Unit
) : ReplicaBehaviour<T> {

    @Suppress("UNCHECKED_CAST")
    override fun setup(replicaClient: ReplicaClient, replica: PhysicalReplica<T>) {
        replicaClient.actions
            .onEach { action ->
                when {
                    handleNormalActions && actionClass.isInstance(action) -> {
                        replica.handler(action as A)
                    }

                    handleOptimisticActions && action is OptimisticAction &&
                            action.command == OptimisticAction.Command.Commit &&
                            actionClass.isInstance(action.sourceAction) -> {
                        replica.handler(action.sourceAction as A)
                    }
                }
            }
            .launchIn(replica.coroutineScope)
    }
}