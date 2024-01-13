package me.aartikov.replica.keyed

import me.aartikov.replica.common.ObservingState
import me.aartikov.replica.single.ReplicaData
import me.aartikov.replica.single.ReplicaState

/**
 * Configures how [KeyedPhysicalReplica] clears children when child count exceeds [KeyedReplicaSettings.maxCount].
 * @property clearOrder see [ClearOrder]
 * @property isPrivilegedReplica allows to set privileged replicas. Privileged replica is cleared only if there is no non-privileged one.
 */
data class ClearPolicy<K : Any, T : Any>(
    val clearOrder: ClearOrder<K, T> = ClearOrder.ByObservingTime,
    val isPrivilegedReplica: ((Pair<K, ReplicaState<T>>) -> Boolean)? = null
) {
    internal val comparator: Comparator<Pair<K, ReplicaState<T>>> = when (clearOrder) {
        ClearOrder.ByObservingTime -> ClearOrder.ByObservingTime.getComparator()
        ClearOrder.ByDataChangingTime -> ClearOrder.ByDataChangingTime.getComparator()
        is ClearOrder.CustomComparator -> clearOrder.comparator
    }.withPrivileged(isPrivilegedReplica)
}

/**
 * Configures in which order [KeyedPhysicalReplica] clears children when child count exceeds [KeyedReplicaSettings.maxCount].
 */
sealed interface ClearOrder<out K : Any, out T : Any> {

    /**
     * Compares replicas by [ObservingState.observingTime].
     */
    data object ByObservingTime : ClearOrder<Nothing, Nothing> {
        internal fun <K : Any, T : Any> getComparator(): Comparator<Pair<K, ReplicaState<T>>> {
            return Comparator { o1, o2 ->
                compareValues(
                    o1.second.observingState.observingTime,
                    o2.second.observingState.observingTime
                )
            }
        }
    }

    /**
     * Compares replicas by [ReplicaData.changingTime].
     */
    data object ByDataChangingTime : ClearOrder<Nothing, Nothing> {
        internal fun <K : Any, T : Any> getComparator(): Comparator<Pair<K, ReplicaState<T>>> {
            return Comparator { o1, o2 ->
                compareValues(
                    o1.second.data?.changingTime,
                    o2.second.data?.changingTime
                )
            }
        }
    }

    /**
     * Allows to specify custom comparator.
     */
    data class CustomComparator<K : Any, T : Any>(
        val comparator: Comparator<Pair<K, ReplicaState<T>>>
    ) : ClearOrder<K, T>
}

private fun <K : Any, T : Any> Comparator<Pair<K, ReplicaState<T>>>.withPrivileged(
    isPrivilegedReplica: ((Pair<K, ReplicaState<T>>) -> Boolean)?
): Comparator<Pair<K, ReplicaState<T>>> {
    return if (isPrivilegedReplica == null) {
        this
    } else {
        Comparator { o1, o2 ->
            val privileged1 = isPrivilegedReplica(o1)
            val privileged2 = isPrivilegedReplica(o2)
            when {
                privileged1 && !privileged2 -> 1
                !privileged1 && privileged2 -> -1
                else -> this.compare(o1, o2)
            }
        }
    }
}