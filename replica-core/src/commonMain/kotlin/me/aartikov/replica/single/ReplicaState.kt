package me.aartikov.replica.single

import me.aartikov.replica.common.CombinedLoadingError
import me.aartikov.replica.common.LoadingError
import me.aartikov.replica.common.ObservingState

/**
 * State of [PhysicalReplica].
 */
data class ReplicaState<T : Any>(
    val loading: Boolean,
    val data: ReplicaData<T>?,
    val error: LoadingError?,
    val observingState: ObservingState,
    val dataRequested: Boolean,
    val preloading: Boolean,
    val loadingFromStorageRequired: Boolean
) {

    val hasFreshData get() = data?.fresh == true

    companion object {
        fun <T : Any> createEmpty(hasStorage: Boolean): ReplicaState<T> = ReplicaState(
            loading = false,
            data = null,
            error = null,
            observingState = ObservingState(),
            dataRequested = false,
            preloading = false,
            loadingFromStorageRequired = hasStorage
        )
    }
}

internal fun <T : Any> ReplicaState<T>.toLoadable() = Loadable(
    loading = loading,
    data = data?.valueWithOptimisticUpdates,
    error = error?.let { CombinedLoadingError(it) }
)