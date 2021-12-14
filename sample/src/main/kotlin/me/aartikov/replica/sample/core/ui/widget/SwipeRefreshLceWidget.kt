package me.aartikov.replica.sample.core.ui.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.aartikov.replica.simple.Loadable

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
            onRefresh = onRefresh
        ) { pullToRefreshPulled ->
            content(data, refreshing = refreshing && !pullToRefreshPulled)
        }
    }
}