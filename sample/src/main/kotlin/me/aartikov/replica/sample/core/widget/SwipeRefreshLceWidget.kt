package me.aartikov.replica.sample.core.widget

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.SwipeRefreshState
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import me.aartikov.replica.single.Loadable

/**
 * Displays Replica state ([Loadable]) with swipe to refresh functionality.
 *
 * Note: a value of refreshing in [content] is true only when data is refreshing and swipe gesture didn't occur.
 */
@Composable
fun <T : Any> SwipeRefreshLceWidget(
    state: Loadable<T>,
    onRefresh: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (data: T, refreshing: Boolean) -> Unit
) {
    LceWidget(
        state = state,
        onRetryClick = onRetryClick,
        modifier = modifier,
    ) { data, refreshing ->
        StatefulSwipeRefresh(
            refreshing = refreshing,
            onRefresh = onRefresh,
            indicator = { s, trigger ->
                SwipeRefreshIndicator(
                    s, trigger,
                    contentColor = MaterialTheme.colors.primaryVariant
                )
            }
        ) { swipeGestureOccurred ->
            content(data, refreshing = refreshing && !swipeGestureOccurred)
        }
    }
}

/**
 * SwipeRefresh that remembers that swipe gesture occurred.
 *
 * Note: it shows a loader only when [refreshing] is true and swipe gesture occurs.
 */
@Composable
fun StatefulSwipeRefresh(
    refreshing: Boolean,
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit,
    indicator: @Composable (state: SwipeRefreshState, refreshTrigger: Dp) -> Unit = { s, trigger ->
        SwipeRefreshIndicator(s, trigger)
    },
    content: @Composable (swipeToRefreshPulled: Boolean) -> Unit
) {
    var swipeGestureOccurred by remember { mutableStateOf(false) }

    LaunchedEffect(refreshing) {
        if (!refreshing) swipeGestureOccurred = false
    }

    val state = rememberSwipeRefreshState(swipeGestureOccurred && refreshing)

    SwipeRefresh(
        state = state,
        onRefresh = {
            swipeGestureOccurred = true
            onRefresh()
        },
        indicator = indicator,
        modifier = modifier
    ) {
        content(swipeGestureOccurred)
    }
}