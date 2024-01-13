package me.aartikov.replica.keyed_paged

import me.aartikov.replica.common.ObservingState
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedReplicaData
import me.aartikov.replica.paged.PagedReplicaState

/**
 * Configures how [KeyedPagedPhysicalReplica] clears children when child count exceeds [KeyedPagedReplicaSettings.maxCount].
 * @property clearOrder see [PagedClearOrder]
 * @property isPrivilegedReplica allows to set privileged replicas. Privileged replica is cleared only if there is no non-privileged one.
 */
data class PagedClearPolicy<K : Any, T : Any, P : Page<T>>(
    val clearOrder: PagedClearOrder<K, T, P> = PagedClearOrder.ByObservingTime,
    val isPrivilegedReplica: ((Pair<K, PagedReplicaState<T, P>>) -> Boolean)? = null
) {
    internal val comparator: Comparator<Pair<K, PagedReplicaState<T, P>>> = when (clearOrder) {
        PagedClearOrder.ByObservingTime -> PagedClearOrder.ByObservingTime.getComparator()
        PagedClearOrder.ByDataChangingTime -> PagedClearOrder.ByDataChangingTime.getComparator()
        is PagedClearOrder.CustomComparator -> clearOrder.comparator
    }.withPrivileged(isPrivilegedReplica)
}

/**
 * Configures in which order [KeyedPagedPhysicalReplica] clears children when child count exceeds [KeyedPagedReplicaSettings.maxCount].
 */
sealed interface PagedClearOrder<out K : Any, out T : Any, out P : Page<T>> {

    /**
     * Compares replicas by [ObservingState.observingTime].
     */
    data object ByObservingTime : PagedClearOrder<Nothing, Nothing, Nothing> {
        internal fun <K : Any, T : Any, P : Page<T>> getComparator()
                    : Comparator<Pair<K, PagedReplicaState<T, P>>> {
            return Comparator { o1, o2 ->
                compareValues(
                    o1.second.observingState.observingTime,
                    o2.second.observingState.observingTime
                )
            }
        }
    }

    /**
     * Compares replicas by [PagedReplicaData.changingTime].
     */
    data object ByDataChangingTime : PagedClearOrder<Nothing, Nothing, Nothing> {
        internal fun <K : Any, T : Any, P : Page<T>> getComparator()
                    : Comparator<Pair<K, PagedReplicaState<T, P>>> {
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
    data class CustomComparator<K : Any, T : Any, P : Page<T>>(
        val comparator: Comparator<Pair<K, PagedReplicaState<T, P>>>
    ) : PagedClearOrder<K, T, P>
}

private fun <K : Any, T : Any, P : Page<T>> Comparator<Pair<K, PagedReplicaState<T, P>>>.withPrivileged(
    isPrivilegedReplica: ((Pair<K, PagedReplicaState<T, P>>) -> Boolean)?
): Comparator<Pair<K, PagedReplicaState<T, P>>> {
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