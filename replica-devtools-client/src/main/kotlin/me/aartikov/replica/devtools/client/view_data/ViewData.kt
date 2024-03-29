package me.aartikov.replica.devtools.client.view_data

import me.aartikov.replica.devtools.client.ConnectionStatus
import me.aartikov.replica.devtools.dto.ReplicaClientDto

data class ViewData(
    val items: List<ItemViewData>,
    val connectionStatusType: ConnectionStatusType
) {
    companion object {
        val Empty = ViewData(
            items = emptyList(),
            connectionStatusType = ConnectionStatusType.Connecting
        )
    }
}

fun ReplicaClientDto.toViewData(sortType: SortType, connectionStatus: ConnectionStatus): ViewData {
    val replicas = replicas.values.map { it.toViewData() }
    val keyedReplicas = keyedReplicas.values.map { it.toViewData(sortType) }
    val allReplicas = replicas
        .plus(keyedReplicas)
        .sortedByDescending { viewData ->
            when (sortType) {
                SortType.ByObservingTime -> viewData.observingTime
            }
        }
    return ViewData(allReplicas, connectionStatus.toViewData())
}