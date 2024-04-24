package me.aartikov.replica.devtools.internal

import me.aartikov.replica.common.ObservingTime
import me.aartikov.replica.devtools.dto.*
import me.aartikov.replica.keyed.KeyedPhysicalReplica
import me.aartikov.replica.keyed.KeyedReplicaState
import me.aartikov.replica.keyed.currentState
import me.aartikov.replica.keyed_paged.KeyedPagedPhysicalReplica
import me.aartikov.replica.keyed_paged.KeyedPagedReplicaState
import me.aartikov.replica.paged.PagedLoadingStatus
import me.aartikov.replica.paged.PagedPhysicalReplica
import me.aartikov.replica.paged.PagedReplicaState
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaState
import me.aartikov.replica.single.currentState

internal fun PhysicalReplica<*>.toDto(): ReplicaDto {
    return ReplicaDto(
        id = id.value,
        name = name,
        state = currentState.toDto()
    )
}

internal fun ReplicaState<*>.toDto(): ReplicaStateDto {
    return ReplicaStateDto(
        loading = loading,
        hasData = data != null,
        hasError = error != null,
        dataIsFresh = data?.fresh == true,
        observerCount = observingState.observerCount,
        activeObserverCount = observingState.activeObserverCount,
        observingTime = observingState.observingTime.toDto()
    )
}

internal fun KeyedPhysicalReplica<*, *>.toDto(): KeyedReplicaDto {
    return KeyedReplicaDto(
        id = id.value,
        name = name,
        state = currentState.toDto()
    )
}

internal fun KeyedReplicaState.toDto(): KeyedReplicaStateDto {
    return KeyedReplicaStateDto(
        replicaCount = replicaCount,
        replicaWithObserversCount = replicaWithObserversCount,
        replicaWithActiveObserversCount = replicaWithActiveObserversCount
    )
}

internal fun PagedPhysicalReplica<*, *>.toDto(): ReplicaDto {
    return ReplicaDto(
        id = id.value,
        name = name,
        state = stateFlow.value.toDto()
    )
}

internal fun PagedReplicaState<*, *>.toDto(): ReplicaStateDto {
    return ReplicaStateDto(
        loading = this.loadingStatus == PagedLoadingStatus.LoadingFirstPage,
        hasData = data != null,
        hasError = error != null,
        dataIsFresh = data?.fresh == true,
        observerCount = observingState.observerCount,
        activeObserverCount = observingState.activeObserverCount,
        observingTime = observingState.observingTime.toDto()
    )
}

internal fun KeyedPagedPhysicalReplica<*, *, *>.toDto(): KeyedReplicaDto {
    return KeyedReplicaDto(
        id = id.value,
        name = name,
        state = stateFlow.value.toDto()
    )
}

internal fun KeyedPagedReplicaState.toDto(): KeyedReplicaStateDto {
    return KeyedReplicaStateDto(
        replicaCount = replicaCount,
        replicaWithObserversCount = replicaWithObserversCount,
        replicaWithActiveObserversCount = replicaWithActiveObserversCount
    )
}

internal fun ObservingTime.toDto(): ObservingTimeDto {
    return when (this) {
        is ObservingTime.Now -> ObservingTimeDto.Now
        is ObservingTime.Never -> ObservingTimeDto.Never
        is ObservingTime.TimeInPast -> ObservingTimeDto.TimeInPast(time)
    }
}