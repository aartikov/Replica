package me.aartikov.replica.common

import kotlinx.datetime.Instant

/**
 * Has information about observers of a replica.
 */
data class ObservingState(
    val observerIds: Set<Long> = emptySet(),
    val activeObserverIds: Set<Long> = emptySet(),
    val observingTime: ObservingTime = ObservingTime.Never
) {

    val observerCount get() = observerIds.size

    val activeObserverCount get() = activeObserverIds.size

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

/**
 * Represent information when a replica was observed in last time.
 */
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