package me.aartikov.replica.paged

import me.aartikov.replica.common.CombinedLoadingError
import me.aartikov.replica.common.LoadingError
import me.aartikov.replica.common.ObservingState

data class PagedReplicaState<I : Any, P : Page<I>>(
    val loadingStatus: PagedLoadingStatus,
    val data: PagedReplicaData<I, P>?,
    val error: LoadingError?,
    val observingState: ObservingState,
    val preloading: Boolean
) {

    val hasFreshData get() = data?.fresh == true

    companion object {
        fun <I : Any, P : Page<I>> createEmpty() = PagedReplicaState<I, P>(
            loadingStatus = PagedLoadingStatus.None,
            data = null,
            error = null,
            observingState = ObservingState(),
            preloading = false
        )
    }
}

internal fun <I : Any, P : Page<I>> PagedReplicaState<I, P>.toPaged() = Paged(
    loadingStatus = loadingStatus,
    data = data?.valueWithOptimisticUpdates,
    error = error?.let { CombinedLoadingError(listOf(it)) }
)