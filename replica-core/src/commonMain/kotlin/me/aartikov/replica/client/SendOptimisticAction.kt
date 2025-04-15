package me.aartikov.replica.client

import me.aartikov.replica.common.ReplicaAction
import me.aartikov.replica.common.internal.OptimisticAction
import me.aartikov.replica.common.internal.OptimisticAction.Command
import me.aartikov.replica.common.performOptimisticUpdate
import me.aartikov.replica.paged.behaviour.standard.doOnAction
import me.aartikov.replica.paged.behaviour.standard.mutateOnAction

/**
 * Sends an [action] to perform optimistic updates across multiple replicas while executing a [block] of code.
 *
 * To handle the [action], use any of the following behaviours:
 * - [mutateOnAction] - to modify replica data optimistically.
 * - [doOnAction] - to perform operations when the optimistic update is committed.
 *
 * [onSuccess], [onError], [onCanceled] are optional callbacks for additional operations.
 *
 * See also: [performOptimisticUpdate].
 */
suspend fun <R> ReplicaClient.sendOptimisticAction(
    action: ReplicaAction,
    onSuccess: (suspend () -> Unit)? = null,
    onError: (suspend (Exception) -> Unit)? = null,
    onCanceled: (suspend () -> Unit)? = null,
    block: suspend () -> R
): R {

    // TODO: required cross-platform random UUID
    val operationId = "optimistic-action-${Any().hashCode()}"

    val sendActionForCommand = { command: Command ->
        sendAction(OptimisticAction(action, command, operationId))
    }

    return performOptimisticUpdate(
        begin = { sendActionForCommand(Command.Begin) },
        rollback = { sendActionForCommand(Command.Rollback) },
        commit = { sendActionForCommand(Command.Commit) },
        block = block,
        onSuccess = onSuccess,
        onError = onError,
        onCanceled = onCanceled
    )
}