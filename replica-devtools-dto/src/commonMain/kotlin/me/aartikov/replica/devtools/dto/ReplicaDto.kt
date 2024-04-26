package me.aartikov.replica.devtools.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ReplicaDto(
    val id: Long,
    val name: String,
    val state: ReplicaStateDto
)

@Serializable
data class ReplicaStateDto(
    val loadingFirstPage: Boolean,
    val loadingNextPage: Boolean = false,
    val loadingPreviousPage: Boolean = false,
    val hasData: Boolean,
    val hasError: Boolean,
    val dataIsFresh: Boolean,
    val observerCount: Int,
    val activeObserverCount: Int,
    val observingTime: ObservingTimeDto,
    val pagesAmount: Int? = null
)

@Serializable
sealed class ObservingTimeDto {

    @Serializable
    object Never : ObservingTimeDto()

    @Serializable
    data class TimeInPast(val time: Instant) : ObservingTimeDto()

    @Serializable
    object Now : ObservingTimeDto()
}
