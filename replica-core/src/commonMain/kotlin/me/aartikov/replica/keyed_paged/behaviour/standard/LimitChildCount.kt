package me.aartikov.replica.keyed_paged.behaviour.standard

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.common.ObservingStatus
import me.aartikov.replica.keyed_paged.KeyedPagedPhysicalReplica
import me.aartikov.replica.keyed_paged.PagedClearPolicy
import me.aartikov.replica.keyed_paged.behaviour.KeyedPagedReplicaBehaviour
import me.aartikov.replica.keyed_paged.currentState
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedLoadingStatus
import me.aartikov.replica.paged.PagedReplicaState
import me.aartikov.replica.paged.currentState
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds

internal fun <K : Any, I : Any, P : Page<I>> KeyedPagedReplicaBehaviour.Companion.limitChildCount(
    maxCount: Int,
    clearPolicy: PagedClearPolicy<K, I, P>
): KeyedPagedReplicaBehaviour<K, I, P> = LimitChildCount(maxCount, clearPolicy)

private class LimitChildCount<K : Any, I : Any, P : Page<I>>(
    private val maxCount: Int,
    private val clearPolicy: PagedClearPolicy<K, I, P>
) : KeyedPagedReplicaBehaviour<K, I, P> {

    companion object {
        private val ClearingDebounceTime = 100.milliseconds
    }

    @OptIn(FlowPreview::class)
    override fun setup(keyedPagedReplica: KeyedPagedPhysicalReplica<K, I, P>) {
        keyedPagedReplica.stateFlow
            // Debounce is used to wait until a just created replica will change state
            .debounce(ClearingDebounceTime.inWholeMilliseconds)
            .onEach { state ->
                if (state.replicaCount > maxCount) {
                    clearReplicas(keyedPagedReplica)
                }
            }
            .launchIn(keyedPagedReplica.coroutineScope)
    }

    private suspend fun clearReplicas(keyedPagedReplica: KeyedPagedPhysicalReplica<K, I, P>) {
        val totalCount = keyedPagedReplica.currentState.replicaCount
        val countForRemoving = max(0, totalCount - maxCount)
        if (countForRemoving == 0) return

        val keysWithStateForRemoving = mutableListOf<Pair<K, PagedReplicaState<I, P>>>()
        keyedPagedReplica.onEachPagedReplica { key ->
            val state = currentState
            val removable = state.loadingStatus == PagedLoadingStatus.None
                    && state.observingState.status == ObservingStatus.None
            if (removable) {
                keysWithStateForRemoving.add(key to state)
            }
        }

        if (keysWithStateForRemoving.size <= countForRemoving) {
            keysWithStateForRemoving.forEach { (key, _) ->
                keyedPagedReplica.clear(key)
            }
            return
        }

        val sortedKeysWithStateForRemoving =
            keysWithStateForRemoving.sortedWith(clearPolicy.comparator)

        sortedKeysWithStateForRemoving.take(countForRemoving).forEach { (key, _) ->
            keyedPagedReplica.clear(key)
        }
    }
}