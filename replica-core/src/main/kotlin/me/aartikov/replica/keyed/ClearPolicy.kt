package me.aartikov.replica.keyed

import me.aartikov.replica.single.ReplicaState

data class ClearPolicy<K : Any, T : Any>(
    val clearOrder: ClearOrder<K, T> = ClearOrder.ByLastUsage,
    val privilegedKeys: Set<K> = emptySet()
) {
    internal val comparator: Comparator<Pair<K, ReplicaState<T>>> = when (clearOrder) {
        ClearOrder.ByLastUsage -> ClearOrder.ByLastUsage.getComparator()
        ClearOrder.ByLastModification -> ClearOrder.ByLastModification.getComparator()
        is ClearOrder.CustomComparator -> clearOrder.comparator
    }.withPrivilegedKeys(privilegedKeys)
}

sealed interface ClearOrder<out K : Any, out T : Any> {

    object ByLastUsage : ClearOrder<Nothing, Nothing> {
        internal fun <K : Any, T : Any> getComparator(): Comparator<Pair<K, ReplicaState<T>>> {
            return Comparator { o1, o2 ->
                val s1 = o1.second
                val s2 = o2.second
                -1 // TODO
            }
        }
    }

    object ByLastModification : ClearOrder<Nothing, Nothing> {
        internal fun <K : Any, T : Any> getComparator(): Comparator<Pair<K, ReplicaState<T>>> {
            return Comparator { o1, o2 ->
                val s1 = o1.second
                val s2 = o2.second
                -1 // TODO
            }
        }
    }

    data class CustomComparator<K : Any, T : Any>(
        val comparator: Comparator<Pair<K, ReplicaState<T>>>
    ) : ClearOrder<K, T>
}

private fun <K : Any, T : Any> Comparator<Pair<K, ReplicaState<T>>>.withPrivilegedKeys(
    privilegedKeys: Set<K>
): Comparator<Pair<K, ReplicaState<T>>> {
    if (privilegedKeys.isEmpty()) {
        return this
    } else {
        return Comparator { o1, o2 ->
            val k1 = o1.first
            val k2 = o2.first
            val privileged1 = privilegedKeys.contains(k1)
            val privileged2 = privilegedKeys.contains(k2)
            when {
                privileged1 && !privileged2 -> 1
                !privileged1 && privileged2 -> -1
                else -> this.compare(o1, o2)
            }
        }
    }
}