package me.aartikov.replica.common

import kotlinx.datetime.Instant

data class ObservingState(
    val observerUuids: Set<String> = emptySet(),
    val activeObserverUuids: Set<String> = emptySet(),
    val observingTime: ObservingTime = ObservingTime.Never
) {

    val observerCount get() = observerUuids.size

    val activeObserverCount get() = activeObserverUuids.size

    val status: ObservingStatus
        get() = when {
            activeObserverCount > 0 -> ObservingStatus.Active
            observerCount > 0 -> ObservingStatus.Inactive
            else -> ObservingStatus.None
        }
}

enum class ObservingStatus {
    None, Inactive, Active
}

sealed interface ObservingTime : Comparable<ObservingTime> {

    object Never : ObservingTime {
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

    object Now : ObservingTime {
        override fun compareTo(other: ObservingTime): Int = when (other) {
            is Never, is TimeInPast -> 1
            Now -> 0
        }
    }
}