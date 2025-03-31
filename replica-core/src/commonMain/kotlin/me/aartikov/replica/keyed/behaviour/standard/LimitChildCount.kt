package me.aartikov.replica.keyed.behaviour.standard

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.common.ObservingStatus
import me.aartikov.replica.keyed.ClearPolicy
import me.aartikov.replica.keyed.KeyedPhysicalReplica
import me.aartikov.replica.keyed.behaviour.KeyedReplicaBehaviour
import me.aartikov.replica.keyed.currentState
import me.aartikov.replica.single.ReplicaState
import me.aartikov.replica.single.currentState
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds

internal fun <K : Any, T : Any> KeyedReplicaBehaviour.Companion.limitChildCount(
    maxCount: Int,
    clearPolicy: ClearPolicy<K, T>
): KeyedReplicaBehaviour<K, T> = LimitChildCount(maxCount, clearPolicy)

private class LimitChildCount<K : Any, T : Any>(
    private val maxCount: Int,
    private val clearPolicy: ClearPolicy<K, T>
) : KeyedReplicaBehaviour<K, T> {

    companion object {
        private val ClearingDebounceTime = 100.milliseconds
    }

    @OptIn(FlowPreview::class)
    override fun setup(replicaClient: ReplicaClient, keyedReplica: KeyedPhysicalReplica<K, T>) {
        keyedReplica.stateFlow
            // Debounce is used to wait until a just created replica will change state
            .debounce(ClearingDebounceTime.inWholeMilliseconds)
            .onEach { state ->
                if (state.replicaCount > maxCount) {
                    clearReplicas(keyedReplica)
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
            val removable = !state.loading && state.observingState.status == ObservingStatus.None
            if (removable) {
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