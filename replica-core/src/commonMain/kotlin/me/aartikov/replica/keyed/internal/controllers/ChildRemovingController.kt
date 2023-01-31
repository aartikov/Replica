package me.aartikov.replica.keyed.internal.controllers

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.aartikov.replica.common.ObservingStatus
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaState
import me.aartikov.replica.single.currentState
import kotlin.time.Duration.Companion.seconds

internal class ChildRemovingController<K : Any, T : Any>(
    private val removeReplica: (K) -> Unit
) {

    companion object {
        private val AdditionalCheckDelay = 0.5.seconds
    }

    fun setupAutoRemoving(key: K, replica: PhysicalReplica<T>) {

        // Additional check required to remove "abandoned" child replica. For example:
        //   someKeyedReplica.onReplica(someKey) {
        //      // do nothing that will change replica state
        //   }
        val additionalCheckJob = replica.coroutineScope.launch {
            delay(AdditionalCheckDelay.inWholeMilliseconds)
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

    private val <T : Any> ReplicaState<T>.canBeRemoved: Boolean
        get() = data == null && error == null && !loading && !dataRequested && observingState.status == ObservingStatus.None

}