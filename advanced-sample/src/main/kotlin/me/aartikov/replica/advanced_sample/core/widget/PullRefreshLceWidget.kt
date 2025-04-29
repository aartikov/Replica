package me.aartikov.replica.advanced_sample.core.widget

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import me.aartikov.replica.advanced_sample.core.error_handling.errorMessage
import me.aartikov.replica.advanced_sample.core.utils.resolve
import me.aartikov.replica.common.AbstractLoadable
import me.aartikov.replica.common.CombinedLoadingError

/**
 * Displays Replica state ([AbstractLoadable]) with pull-to-refresh functionality.
 *
 * Note: a value of refreshing in [content] is true only when data is refreshing and pull gesture didn't occur.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Any> PullRefreshLceWidget(
    state: AbstractLoadable<T>,
    onRefresh: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentWindowInsets: WindowInsets = WindowInsets.navigationBars,
    loadingContent: @Composable BoxScope.(PaddingValues) -> Unit = {
        FullscreenCircularProgress(Modifier.windowInsetsPadding(contentWindowInsets))
    },
    errorContent: @Composable BoxScope.(CombinedLoadingError, PaddingValues) -> Unit = { error, _ ->
        ErrorPlaceholder(
            modifier = Modifier.windowInsetsPadding(contentWindowInsets),
            errorMessage = error.exception.errorMessage.resolve(),
            onRetryClick = onRetryClick
        )
    },
    content: @Composable BoxScope.(data: T, refreshing: Boolean, paddingValues: PaddingValues) -> Unit,
) {
    LceWidget(
        state = state,
        onRetryClick = onRetryClick,
        modifier = modifier,
        loadingContent = loadingContent,
        errorContent = errorContent,
    ) { data, refreshing, paddingValues ->
        var pullGestureOccurred by remember { mutableStateOf(false) }

        val pullRefreshState = rememberPullToRefreshState()

        val isRefreshing by remember(pullGestureOccurred, refreshing) {
            derivedStateOf {
                pullGestureOccurred && refreshing
            }
        }

        LaunchedEffect(refreshing) {
            if (!refreshing) pullGestureOccurred = false
        }

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                pullGestureOccurred = true
                onRefresh()
            },
            state = pullRefreshState,
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullRefreshState,
                    isRefreshing = isRefreshing,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            },
        ) {
            content(data, refreshing && !pullGestureOccurred, paddingValues)
        }
    }
}
