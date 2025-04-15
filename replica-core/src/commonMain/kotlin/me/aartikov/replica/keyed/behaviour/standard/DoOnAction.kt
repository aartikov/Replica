package me.aartikov.replica.keyed.behaviour.standard

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.client.sendOptimisticAction
import me.aartikov.replica.common.ReplicaAction
import me.aartikov.replica.common.internal.OptimisticAction
import me.aartikov.replica.keyed.KeyedPhysicalReplica
import me.aartikov.replica.keyed.behaviour.KeyedReplicaBehaviour
import kotlin.reflect.KClass

/**
 * Creates a [KeyedReplicaBehaviour] that processes [ReplicaAction] actions of a specific type [A].
 *
 * There are two types of actions:
 * - Normal actions are sent with [ReplicaClient.sendAction] and are processed immediately.
 * - Optimistic actions are sent with [ReplicaClient.sendOptimisticAction] and are processed only when
 *   the corresponding optimistic update is committed.
 *
 * [handleNormalActions] specifies whether to process normal actions.
 * [handleOptimisticActions] specifies whether to process optimistic actions.
 */
inline fun <K : Any, T : Any, reified A : ReplicaAction> KeyedReplicaBehaviour.Companion.doOnAction(
    handleNormalActions: Boolean = true,
    handleOptimisticActions: Boolean = true,
    noinline handler: suspend KeyedPhysicalReplica<K, T>.(action: A) -> Unit
): KeyedReplicaBehaviour<K, T> =
    doOnAction(A::class, handleNormalActions, handleOptimisticActions, handler)

/**
 * Creates a [KeyedReplicaBehaviour] that processes [ReplicaAction] actions of a specific type as defined by [actionClass].
 *
 * There are two types of actions:
 * - Normal actions are sent with [ReplicaClient.sendAction] and are processed immediately.
 * - Optimistic actions are sent with [ReplicaClient.sendOptimisticAction] and are processed only when
 *   the corresponding optimistic update is committed.
 *
 * [handleNormalActions] specifies whether to process normal actions.
 * [handleOptimisticActions] specifies whether to process optimistic actions.
 */
fun <K : Any, T : Any, A : ReplicaAction> KeyedReplicaBehaviour.Companion.doOnAction(
    actionClass: KClass<A>,
    handleNormalActions: Boolean = true,
    handleOptimisticActions: Boolean = true,
    handler: suspend KeyedPhysicalReplica<K, T>.(action: A) -> Unit
): KeyedReplicaBehaviour<K, T> =
    DoOnAction(actionClass, handleNormalActions, handleOptimisticActions, handler)

private class DoOnAction<K : Any, T : Any, A : ReplicaAction>(
    private val actionClass: KClass<A>,
    private val handleNormalActions: Boolean,
    private val handleOptimisticActions: Boolean,
    private val handler: suspend KeyedPhysicalReplica<K, T>.(action: A) -> Unit
) : KeyedReplicaBehaviour<K, T> {

    @Suppress("UNCHECKED_CAST")
    override fun setup(replicaClient: ReplicaClient, keyedReplica: KeyedPhysicalReplica<K, T>) {
        replicaClient.actions
            .onEach { action ->
                when {
                    handleNormalActions && actionClass.isInstance(action) -> {
                        keyedReplica.handler(action as A)
                    }

                    handleOptimisticActions && action is OptimisticAction &&
                            action.command == OptimisticAction.Command.Commit &&
                            actionClass.isInstance(action.sourceAction) -> {
                        keyedReplica.handler(action.sourceAction as A)
                    }
                }
            }
            .launchIn(keyedReplica.coroutineScope)
    }
}