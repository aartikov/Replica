package me.aartikov.replica.client

import me.aartikov.replica.common.ReplicaAction
import me.aartikov.replica.common.internal.OptimisticUpdateAction
import me.aartikov.replica.common.internal.OptimisticUpdateAction.Command
import me.aartikov.replica.common.performOptimisticUpdate
import me.aartikov.replica.single.behaviour.standard.doOnAction
import me.aartikov.replica.single.behaviour.standard.provideOptimisticUpdate

/**
 * Utility method to perform optimistic updates across multiple replicas.
 *
 * Takes an [action] that defines the operation to be performed on the replicas.
 *
 * To support optimistic updates, the corresponding replicas must be configured using the [provideOptimisticUpdate] behaviour,
 * which specifies how to apply optimistic updates for the given [action].
 *
 * Under the hood, the [action] is transformed into three internal actions for the 'begin', 'commit', and 'rollback' commands.
 * After the [block] completes successfully, the [action] is sent normally.
 * This allows additional handling via the [doOnAction] behaviour.
 *
 * [onSuccess], [onError], [onCanceled], [onFinished] are optional callbacks for additional operations.
 */
suspend fun <R> ReplicaClient.sendActionWithOptimisticUpdate(
    action: ReplicaAction,
    onSuccess: (suspend () -> Unit)? = null,
    onError: (suspend (Exception) -> Unit)? = null,
    onCanceled: (suspend () -> Unit)? = null,
    onFinished: (suspend () -> Unit)? = null,
    block: suspend () -> R
): R {

    // TODO: required cross-platform random UUID
    val operationId = "optimistic-update-action-${Any().hashCode()}"

    val sendActionForCommand = { command: Command ->
        sendAction(OptimisticUpdateAction(action, command, operationId))
    }

    return performOptimisticUpdate(
        begin = { sendActionForCommand(Command.Begin) },
        rollback = { sendActionForCommand(Command.Rollback) },
        commit = { sendActionForCommand(Command.Commit) },
        block = block,
        onSuccess = {
            sendAction(action)
            onSuccess?.invoke()
        },
        onError = onError,
        onCanceled = onCanceled,
        onFinished = onFinished
    )
}