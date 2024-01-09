package me.aartikov.replica.paged

import me.aartikov.replica.common.CombinedLoadingError
import me.aartikov.replica.common.LoadingError
import me.aartikov.replica.common.ObservingState

data class PagedReplicaState<T : Any, P : Page<T>>(
    val loadingStatus: PagedLoadingStatus,
    val data: PagedReplicaData<T, P>?,
    val error: LoadingError?,
    val observingState: ObservingState,
    val preloading: Boolean
) {

    val hasFreshData get() = data?.fresh == true

    companion object {
        fun <T : Any, P : Page<T>> createEmpty() = PagedReplicaState<T, P>(
            loadingStatus = PagedLoadingStatus.None,
            data = null,
            error = null,
            observingState = ObservingState(),
            preloading = false
        )
    }
}

internal fun <T : Any, P : Page<T>> PagedReplicaState<T, P>.toPaged() = Paged(
    loadingStatus = loadingStatus,
    data = data?.valueWithOptimisticUpdates,
    error = error?.let { CombinedLoadingError(listOf(it)) }
)