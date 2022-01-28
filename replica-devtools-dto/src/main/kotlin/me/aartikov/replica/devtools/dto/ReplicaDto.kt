package me.aartikov.replica.devtools.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ReplicaDto(
    val id: String,
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
sealed class ObservingTimeDto : Comparable<ObservingTimeDto> {

    @Serializable
    object Never : ObservingTimeDto() {
        override fun compareTo(other: ObservingTimeDto): Int = when (other) {
            is Never -> 0
            is TimeInPast, is Now -> -1
        }
    }

    @Serializable
    data class TimeInPast(val time: Instant) : ObservingTimeDto() {
        override fun compareTo(other: ObservingTimeDto): Int = when (other) {
            is Never -> 1
            is TimeInPast -> time.compareTo(other.time)
            Now -> -1
        }
    }

    @Serializable
    object Now : ObservingTimeDto() {
        override fun compareTo(other: ObservingTimeDto): Int = when (other) {
            is Never, is TimeInPast -> 1
            Now -> 0
        }
    }
}