package me.aartikov.replica.devtools.client.view_data

import kotlinx.datetime.Instant
import me.aartikov.replica.devtools.dto.ObservingTimeDto

sealed interface ObservingTime : Comparable<ObservingTime> {

    data object Never : ObservingTime {
        override fun compareTo(other: ObservingTime): Int = when (other) {
            is Never -> 0
            is TimeInPast, is Now -> -1
        }
    }

    data class TimeInPast(val time: Instant) : ObservingTime {
        override fun compareTo(other: ObservingTime): Int = when (other) {
            is Never -> 1
            is TimeInPast -> time.compareTo(other.time)
            Now -> -1
        }
    }

    data object Now : ObservingTime {
        override fun compareTo(other: ObservingTime): Int = when (other) {
            is Never, is TimeInPast -> 1
            Now -> 0
        }
    }
}

fun ObservingTimeDto.toViewData(): ObservingTime {
    return when (this) {
        is ObservingTimeDto.Never -> ObservingTime.Never
        is ObservingTimeDto.Now -> ObservingTime.Now
        is ObservingTimeDto.TimeInPast -> ObservingTime.TimeInPast(time)
    }
}