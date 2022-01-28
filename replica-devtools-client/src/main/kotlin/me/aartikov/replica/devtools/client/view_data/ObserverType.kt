package me.aartikov.replica.devtools.client.view_data

import me.aartikov.replica.devtools.dto.ReplicaStateDto

enum class ObserverType {
    None, Inactive, Active
}

fun ReplicaStateDto.toObserverType(): ObserverType {
    return when {
        activeObserverCount > 0 -> ObserverType.Active
        activeObserverCount == 0 && observerCount > 0 -> ObserverType.Inactive
        activeObserverCount == 0 && observerCount == 0 -> ObserverType.None
        else -> throw IllegalStateException("Not supported observer type for state $this")
    }
}