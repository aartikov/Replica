package me.aartikov.replica.devtools.client.view_data

import me.aartikov.replica.devtools.client.StatusItemType
import me.aartikov.replica.devtools.dto.KeyedReplicaDto
import me.aartikov.replica.devtools.dto.ReplicaDto
import me.aartikov.replica.devtools.dto.ReplicaStateDto

sealed interface ItemViewData

data class SimpleReplicaViewData(
    val id: String,
    val name: String,
    val status: StatusItemType,
    val observerType: ObserverType
) : ItemViewData

data class KeyedReplicaViewData(
    val id: String,
    val name: String,
    val childReplicas: List<SimpleReplicaViewData>
) : ItemViewData

fun ReplicaDto.toViewData(): SimpleReplicaViewData {
    return SimpleReplicaViewData(
        id = id,
        name = name,
        status = state.toStatusItemType(),
        observerType = state.toObserverType()
    )
}

fun KeyedReplicaDto.toViewData(): KeyedReplicaViewData {
    return KeyedReplicaViewData(
        id = id,
        name = name,
        childReplicas = childReplicas.values.map { it.toViewData() }
    )
}

fun ReplicaStateDto.toStatusItemType(): StatusItemType {
    return when {
        hasData && !loading && dataIsFresh -> StatusItemType.Fresh
        hasData && !loading && !dataIsFresh -> StatusItemType.Stale
        !hasData && hasError && !loading -> StatusItemType.Error
        !hasData && loading -> StatusItemType.Loading
        hasData && loading -> StatusItemType.Refresh
        !hasData && !hasError && !loading -> StatusItemType.Empty
        else -> throw IllegalArgumentException()
    }
}