package me.aartikov.replica.keyed_paged.internal.controllers

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.aartikov.replica.common.ObservingStatus
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedLoadingStatus
import me.aartikov.replica.paged.PagedPhysicalReplica
import me.aartikov.replica.paged.PagedReplicaState
import me.aartikov.replica.paged.currentState
import kotlin.time.Duration.Companion.seconds

internal class ChildRemovingController<K : Any, T : Any, P : Page<T>>(
    private val removeReplica: (K) -> Unit
) {

    companion object {
        private val AdditionalCheckDelay = 0.5.seconds
    }

    fun setupAutoRemoving(key: K, replica: PagedPhysicalReplica<T, P>) {

        // Additional check required to remove "abandoned" child replica. For example:
        //   someKeyedPagedReplica.onPagedReplica(someKey) {
        //      // do nothing that will change replica state
        //   }
        val additionalCheckJob = replica.coroutineScope.launch {
            delay(AdditionalCheckDelay)
            if (replica.currentState.canBeRemoved) {
                removeReplica(key)
            }
        }

        replica.stateFlow
            .drop(1)
            .onEach { state ->
                if (state.canBeRemoved) {
                    removeReplica(key)
                } else {
                    additionalCheckJob.cancel()
                }
            }
            .launchIn(replica.coroutineScope)
    }

    private val <T : Any, P : Page<T>> PagedReplicaState<T, P>.canBeRemoved: Boolean
        get() = data == null && error == null && loadingStatus == PagedLoadingStatus.None &&
                    observingState.status == ObservingStatus.None

}