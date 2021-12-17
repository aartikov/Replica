package me.aartikov.replica.sample.core.ui.widget

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun StatefulSwipeRefresh(
    refreshing: Boolean,
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit,
    content: @Composable (pullToRefreshPulled: Boolean) -> Unit
) {
    var pullToRefreshPulled by rememberSaveable { mutableStateOf(false) }
    val state = rememberSwipeRefreshState(pullToRefreshPulled && refreshing)

    LaunchedEffect(refreshing) {
        if (!refreshing) pullToRefreshPulled = false
    }

    SwipeRefresh(
        state = state,
        onRefresh = {
            pullToRefreshPulled = true
            onRefresh()
        },
        indicator = { s, trigger ->
            SwipeRefreshIndicator(
                s, trigger,
                contentColor = MaterialTheme.colors.primaryVariant
            )
        },
        modifier = modifier
    ) {
        content(pullToRefreshPulled)
    }
}