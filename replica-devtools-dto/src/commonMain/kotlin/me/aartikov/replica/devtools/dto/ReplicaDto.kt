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
    val loading: Boolean,
    val hasData: Boolean,
    val hasError: Boolean,
    val dataIsFresh: Boolean,
    val observerCount: Int,
    val activeObserverCount: Int,
    val observingTime: ObservingTimeDto
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