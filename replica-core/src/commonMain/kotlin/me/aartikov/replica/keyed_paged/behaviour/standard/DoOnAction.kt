package me.aartikov.replica.keyed_paged.behaviour.standard

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.client.sendOptimisticAction
import me.aartikov.replica.common.ReplicaAction
import me.aartikov.replica.common.internal.OptimisticAction
import me.aartikov.replica.keyed_paged.KeyedPagedPhysicalReplica
import me.aartikov.replica.keyed_paged.behaviour.KeyedPagedReplicaBehaviour
import me.aartikov.replica.paged.Page
import kotlin.reflect.KClass

/**
 * Creates a [KeyedPagedReplicaBehaviour] that processes [ReplicaAction] actions of a specific type [A].
 *
 * There are two types of actions:
 * - Normal actions are sent with [ReplicaClient.sendAction] and are processed immediately.
 * - Optimistic actions are sent with [ReplicaClient.sendOptimisticAction] and are processed only when
 *   the corresponding optimistic update is committed.
 *
 * [handleNormalActions] specifies whether to process normal actions.
 * [handleOptimisticActions] specifies whether to process optimistic actions.
 */
inline fun <K : Any, I : Any, P : Page<I>, reified A : ReplicaAction> KeyedPagedReplicaBehaviour.Companion.doOnAction(
    handleNormalActions: Boolean = true,
    handleOptimisticActions: Boolean = true,
    noinline handler: suspend KeyedPagedPhysicalReplica<K, I, P>.(action: A) -> Unit
): KeyedPagedReplicaBehaviour<K, I, P> =
    doOnAction(A::class, handleNormalActions, handleOptimisticActions, handler)

/**
 * Creates a [KeyedPagedReplicaBehaviour] that processes [ReplicaAction] actions of a specific type as defined by [actionClass].
 *
 * There are two types of actions:
 * - Normal actions are sent with [ReplicaClient.sendAction] and are processed immediately.
 * - Optimistic actions are sent with [ReplicaClient.sendOptimisticAction] and are processed only when
 *   the corresponding optimistic update is committed.
 *
 * [handleNormalActions] specifies whether to process normal actions.
 * [handleOptimisticActions] specifies whether to process optimistic actions.
 */
fun <K : Any, I : Any, P : Page<I>, A : ReplicaAction> KeyedPagedReplicaBehaviour.Companion.doOnAction(
    actionClass: KClass<A>,
    handleNormalActions: Boolean = true,
    handleOptimisticActions: Boolean = true,
    handler: suspend KeyedPagedPhysicalReplica<K, I, P>.(action: A) -> Unit
): KeyedPagedReplicaBehaviour<K, I, P> =
    DoOnAction(actionClass, handleNormalActions, handleOptimisticActions, handler)

private class DoOnAction<K : Any, I : Any, P : Page<I>, A : ReplicaAction>(
    private val actionClass: KClass<A>,
    private val handleNormalActions: Boolean,
    private val handleOptimisticActions: Boolean,
    private val handler: suspend KeyedPagedPhysicalReplica<K, I, P>.(action: A) -> Unit
) : KeyedPagedReplicaBehaviour<K, I, P> {

    @Suppress("UNCHECKED_CAST")
    override fun setup(
        replicaClient: ReplicaClient,
        keyedPagedReplica: KeyedPagedPhysicalReplica<K, I, P>
    ) {
        replicaClient.actions
            .onEach { action ->
                when {
                    handleNormalActions && actionClass.isInstance(action) -> {
                        keyedPagedReplica.handler(action as A)
                    }

                    handleOptimisticActions && action is OptimisticAction &&
                            action.command == OptimisticAction.Command.Commit &&
                            actionClass.isInstance(action.sourceAction) -> {
                        keyedPagedReplica.handler(action.sourceAction as A)
                    }
                }
            }
            .launchIn(keyedPagedReplica.coroutineScope)
    }
}