package me.aartikov.replica.advanced_sample.core.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.aartikov.replica.advanced_sample.core.error_handling.errorMessage
import me.aartikov.replica.advanced_sample.core.utils.resolve
import me.aartikov.replica.common.CombinedLoadingError
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.Paged
import me.aartikov.replica.paged.PagedData
import me.aartikov.replica.paged.PagedLoadingStatus
import me.aartikov.replica.single.Loadable

/**
 * Displays Replica state ([Loadable]).
 */
@Composable
fun <T : Any> LceWidget(
    state: Loadable<T>,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (data: T, refreshing: Boolean) -> Unit
) {
    LceWidget(
        loading = state.loading,
        data = state.data,
        error = state.error,
        onRetryClick = onRetryClick,
        modifier = modifier,
        content = {
            content(it, state.loading)
        }
    )
}

/**
 * Displays PagedReplica state ([Paged]).
 */
@Composable
fun <T : Any, P : Page<T>> LceWidget(
    state: Paged<T, P>,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (data: PagedData<T, P>, refreshing: Boolean) -> Unit
) {
    LceWidget(
        loading = state.loadingStatus != PagedLoadingStatus.None,
        data = state.data,
        error = state.error,
        onRetryClick = onRetryClick,
        modifier = modifier,
        content = {
            content(it, state.loadingStatus == PagedLoadingStatus.LoadingFirstPage)
        }
    )
}

@Composable
internal fun <T : Any> LceWidget(
    loading: Boolean,
    data: T?,
    error: CombinedLoadingError?,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (data: T) -> Unit
) {
    when {
        data != null -> content(data)

        loading -> FullscreenCircularProgress(modifier)

        error != null -> ErrorPlaceholder(
            errorMessage = error.exception.errorMessage.resolve(),
            onRetryClick = onRetryClick,
            modifier = modifier
        )
    }
}