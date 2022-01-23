package me.aartikov.replica.devtools.internal

import me.aartikov.replica.devtools.dto.KeyedReplicaDto
import me.aartikov.replica.devtools.dto.KeyedReplicaStateDto
import me.aartikov.replica.devtools.dto.ReplicaDto
import me.aartikov.replica.devtools.dto.ReplicaStateDto
import me.aartikov.replica.keyed.KeyedPhysicalReplica
import me.aartikov.replica.keyed.KeyedReplicaState
import me.aartikov.replica.keyed.currentState
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaState
import me.aartikov.replica.single.currentState

fun PhysicalReplica<*>.toDto(): ReplicaDto {
    return ReplicaDto(
        id = id.value,
        name = name,
        state = currentState.toDto()
    )
}

fun ReplicaState<*>.toDto(): ReplicaStateDto {
    return ReplicaStateDto(
        loading = loading,
        hasData = data != null,
        hasError = error != null,
        dataIsFresh = data?.fresh == true,
        observerCount = observingState.observerCount,
        activeObserverCount = observingState.activeObserverCount
    )
}

fun KeyedPhysicalReplica<*, *>.toDto(): KeyedReplicaDto {
    return KeyedReplicaDto(
        id = id.value,
        name = name,
        state = currentState.toDto()
    )
}

fun KeyedReplicaState.toDto(): KeyedReplicaStateDto {
    return KeyedReplicaStateDto(
        replicaCount = replicaCount,
        replicaWithObserversCount = replicaWithObserversCount,
        replicaWithActiveObserversCount = replicaWithActiveObserversCount
    )
}
