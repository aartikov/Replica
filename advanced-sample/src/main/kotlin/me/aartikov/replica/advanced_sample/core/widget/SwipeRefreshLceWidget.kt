package me.aartikov.replica.advanced_sample.core.widget

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.SwipeRefreshState
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import me.aartikov.replica.common.CombinedLoadingError
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.Paged
import me.aartikov.replica.paged.PagedData
import me.aartikov.replica.paged.PagedLoadingStatus
import me.aartikov.replica.single.Loadable

/**
 * Displays Replica state ([Loadable]) with swipe-to-refresh functionality.
 *
 * Note: a value of refreshing in [content] is true only when data is refreshing and swipe gesture didn't occur.
 */
@Composable
fun <T : Any> SwipeRefreshLceWidget(
    state: Loadable<T>,
    onRefresh: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
    swipeRefreshIndicator: @Composable (state: SwipeRefreshState, refreshTrigger: Dp) -> Unit = { s, trigger ->
        SwipeRefreshIndicator(s, trigger, contentColor = MaterialTheme.colors.primaryVariant)
    },
    content: @Composable (data: T, refreshing: Boolean) -> Unit
) {
    SwipeRefreshLceWidget(
        loading = state.loading,
        data = state.data,
        error = state.error,
        onRefresh = onRefresh,
        onRetryClick = onRetryClick,
        modifier = modifier,
        swipeRefreshIndicator = swipeRefreshIndicator,
        content = { data, swipeGestureOccurred ->
            content(data, state.loading && !swipeGestureOccurred)
        }
    )
}

/**
 * Displays Replica state ([Loadable]) with swipe-to-refresh functionality.
 *
 * Note: a value of refreshing in [content] is true only when data is refreshing and swipe gesture didn't occur.
 */
@Composable
fun <T : Any, P : Page<T>> SwipeRefreshLceWidget(
    state: Paged<T, P>,
    onRefresh: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
    swipeRefreshIndicator: @Composable (state: SwipeRefreshState, refreshTrigger: Dp) -> Unit = { s, trigger ->
        SwipeRefreshIndicator(s, trigger, contentColor = MaterialTheme.colors.primaryVariant)
    },
    content: @Composable (data: PagedData<T, P>, refreshing: Boolean) -> Unit
) {
    SwipeRefreshLceWidget(
        loading = state.loadingStatus != PagedLoadingStatus.None,
        data = state.data,
        error = state.error,
        onRefresh = onRefresh,
        onRetryClick = onRetryClick,
        modifier = modifier,
        swipeRefreshIndicator = swipeRefreshIndicator,
        content = { data, swipeGestureOccurred ->
            content(data, state.loadingStatus == PagedLoadingStatus.LoadingFirstPage && !swipeGestureOccurred)
        }
    )
}

@Composable
private fun <T : Any> SwipeRefreshLceWidget(
    loading: Boolean,
    data: T?,
    error: CombinedLoadingError?,
    onRefresh: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
    swipeRefreshIndicator: @Composable (state: SwipeRefreshState, refreshTrigger: Dp) -> Unit,
    content: @Composable (data: T, swipeGestureOccurred: Boolean) -> Unit
) {
    LceWidget(
        loading = loading,
        data = data,
        error = error,
        onRetryClick = onRetryClick,
        modifier = modifier
    ) {
        var swipeGestureOccurred by remember { mutableStateOf(false) }

        LaunchedEffect(loading) {
            if (!loading) swipeGestureOccurred = false
        }

        val swipeRefreshState = rememberSwipeRefreshState(swipeGestureOccurred && loading)

        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = {
                swipeGestureOccurred = true
                onRefresh()
            },
            indicator = swipeRefreshIndicator
        ) {
            content(it, swipeGestureOccurred)
        }
    }
}