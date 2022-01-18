package me.aartikov.replica.single

data class ReplicaState<T : Any>(
    val data: ReplicaData<T>?,
    val loading: Boolean,
    val error: LoadingError?,
    val observingState: ObservingState,
    val dataRequested: Boolean,
    val preloading: Boolean,
    val loadingFromStorageRequired: Boolean
) {

    val hasFreshData get() = data?.fresh == true

    companion object {
        fun <T : Any> createEmpty(hasStorage: Boolean): ReplicaState<T> = ReplicaState(
            data = null,
            loading = false,
            error = null,
            observingState = ObservingState(),
            dataRequested = false,
            preloading = false,
            loadingFromStorageRequired = hasStorage
        )
    }
}

internal fun <T : Any> ReplicaState<T>.toLoadable() = Loadable(
    data = data?.valueWithOptimisticUpdates,
    loading = loading,
    error = error
)