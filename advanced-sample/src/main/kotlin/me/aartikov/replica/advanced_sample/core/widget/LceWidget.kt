package me.aartikov.replica.advanced_sample.core.widget

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.aartikov.replica.advanced_sample.core.error_handling.errorMessage
import me.aartikov.replica.advanced_sample.core.utils.resolve
import me.aartikov.replica.common.AbstractLoadable
import me.aartikov.replica.common.CombinedLoadingError

/**
 * Displays Replica state ([AbstractLoadable]).
 */
@Composable
fun <T : Any> LceWidget(
    state: AbstractLoadable<T>,
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
    val loading = state.loading
    val data = state.data
    val error = state.error

    Box(modifier) {
        when {
            data != null -> content(data, loading, contentWindowInsets.asPaddingValues())

            loading -> loadingContent(contentWindowInsets.asPaddingValues())

            error != null -> errorContent(error, contentWindowInsets.asPaddingValues())
        }
    }
}
