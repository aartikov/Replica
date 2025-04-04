package me.aartikov.replica.common.internal

import me.aartikov.replica.common.ReplicaAction

internal data class OptimisticUpdateAction(
    val sourceAction: ReplicaAction,
    val command: Command,
    val operationId: String
) : ReplicaAction {

    enum class Command {
        Begin, Commit, Rollback
    }
}
