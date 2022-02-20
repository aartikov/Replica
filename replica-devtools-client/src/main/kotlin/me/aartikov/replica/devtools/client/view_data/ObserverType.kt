package me.aartikov.replica.devtools.client.view_data

import me.aartikov.replica.devtools.dto.KeyedReplicaStateDto
import me.aartikov.replica.devtools.dto.ReplicaStateDto

enum class ObserverType {
    None, Inactive, Active
}

fun ReplicaStateDto.toObserverType(): ObserverType {
    return when {
        activeObserverCount > 0 -> ObserverType.Active
        activeObserverCount == 0 && observerCount > 0 -> ObserverType.Inactive
        else -> ObserverType.None
    }
}

fun KeyedReplicaStateDto.toObserverType(): ObserverType {
    return when {
        replicaWithActiveObserversCount > 0 -> ObserverType.Active
        replicaWithActiveObserversCount == 0 && replicaWithObserversCount > 0 -> ObserverType.Inactive
        else -> ObserverType.None
    }
}