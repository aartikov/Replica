package me.aartikov.replica.devtools.client.view_data

import me.aartikov.replica.devtools.dto.KeyedReplicaDto
import me.aartikov.replica.devtools.dto.ReplicaDto

sealed class ItemViewData(
    open val id: Long,
    open val name: String,
    open val observingTime: ObservingTime
)

data class SimpleReplicaViewData(
    override val id: Long,
    override val name: String,
    val status: StatusItemType,
    val observerType: ObserverType,
    override val observingTime: ObservingTime,
    val pagesAmount: Int?
) : ItemViewData(id, name, observingTime)

data class KeyedReplicaViewData(
    override val id: Long,
    override val name: String,
    val childReplicas: List<SimpleReplicaViewData>,
    override val observingTime: ObservingTime,
    val observerType: ObserverType,
) : ItemViewData(id, name, observingTime)

fun ReplicaDto.toViewData(): SimpleReplicaViewData {
    return SimpleReplicaViewData(
        id = id,
        name = name,
        status = state.toStatusItemType(),
        observerType = state.toObserverType(),
        observingTime = state.observingTime.toViewData(),
        pagesAmount = state.pagesAmount
    )
}

fun KeyedReplicaDto.toViewData(type: SortType): KeyedReplicaViewData {
    val childReplicasViewData = childReplicas.values.map { it.toViewData() }

    return KeyedReplicaViewData(
        id = id,
        name = name,
        childReplicas = childReplicasViewData
            .sortedByDescending {
                when (type) {
                    SortType.ByObservingTime -> it.observingTime
                }
            },
        observingTime = childReplicasViewData
            .maxOfOrNull { it.observingTime }
            ?: ObservingTime.Never,
        observerType = state.toObserverType()
    )
}
