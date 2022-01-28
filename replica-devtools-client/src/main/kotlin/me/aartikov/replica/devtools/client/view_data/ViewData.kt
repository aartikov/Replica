package me.aartikov.replica.devtools.client.view_data

import me.aartikov.replica.devtools.dto.ReplicaClientDto

data class ViewData(
    val items: List<ItemViewData>
)

fun ReplicaClientDto.toViewData(type: SortType): ViewData {
    val replicas = replicas.values.map { it.toViewData() }
    val keyedReplicas = keyedReplicas.values.map { it.toViewData(type) }
    val allReplicas = replicas
        .plus(keyedReplicas)
        .sortedByDescending { viewData ->
            when (type) {
                SortType.ByObservingTime -> viewData.observingTime
            }
        }
    return ViewData(allReplicas)
}