package me.aartikov.replica.advanced_sample.core.widget

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
    loadingContent: @Composable BoxScope.() -> Unit = { FullscreenCircularProgress() },
    errorContent: @Composable BoxScope.(CombinedLoadingError) -> Unit = { error ->
        ErrorPlaceholder(
            errorMessage = error.exception.errorMessage.resolve(),
            onRetryClick = onRetryClick
        )
    },
    content: @Composable BoxScope.(data: T, refreshing: Boolean) -> Unit,
) {
    val loading = state.loading
    val data = state.data
    val error = state.error

    Box(modifier) {
        when {
            data != null -> content(data, loading)

            loading -> loadingContent()

            error != null -> errorContent(error)
        }
    }
}
