package me.aartikov.replica.keyed.behaviour.standard

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.aartikov.replica.common.ObservingStatus
import me.aartikov.replica.keyed.ClearPolicy
import me.aartikov.replica.keyed.KeyedPhysicalReplica
import me.aartikov.replica.keyed.behaviour.KeyedReplicaBehaviour
import me.aartikov.replica.keyed.currentState
import me.aartikov.replica.single.ReplicaState
import me.aartikov.replica.single.currentState
import kotlin.math.max

class LimitChildCount<K : Any, T : Any>(
    private val maxCount: Int,
    private val clearPolicy: ClearPolicy<K, T>
) : KeyedReplicaBehaviour<K, T> {

    override fun setup(keyedReplica: KeyedPhysicalReplica<K, T>) {
        val mutex = Mutex()
        keyedReplica.stateFlow
            .onEach { state ->
                mutex.withLock {
                    if (state.replicaCount > maxCount) {
                        clearReplicas(keyedReplica)
                    }
                }
            }
            .launchIn(keyedReplica.coroutineScope)
    }

    private suspend fun clearReplicas(keyedReplica: KeyedPhysicalReplica<K, T>) {
        val totalCount = keyedReplica.currentState.replicaCount
        val countForRemoving = max(0, totalCount - maxCount)
        if (countForRemoving == 0) return

        val keysWithStateForRemoving = mutableListOf<Pair<K, ReplicaState<T>>>()
        keyedReplica.onEachReplica { key ->
            val state = currentState
            val justCreatedReplica = state.data == null && state.error == null && !state.loading
            val removableReplica =
                !state.loading && state.observingState.status == ObservingStatus.None
            if (!justCreatedReplica && removableReplica) {
                keysWithStateForRemoving.add(key to state)
            }
        }

        if (keysWithStateForRemoving.size <= countForRemoving) {
            keysWithStateForRemoving.forEach { (key, _) ->
                keyedReplica.clear(key)
            }
            return
        }

        val sortedKeysWithStateForRemoving =
            keysWithStateForRemoving.sortedWith(clearPolicy.comparator)

        sortedKeysWithStateForRemoving.take(countForRemoving).forEach { (key, _) ->
            keyedReplica.clear(key)
        }
    }
}