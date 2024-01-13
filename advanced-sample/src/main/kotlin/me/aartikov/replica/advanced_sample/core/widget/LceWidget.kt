package me.aartikov.replica.advanced_sample.core.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.aartikov.replica.advanced_sample.core.error_handling.errorMessage
import me.aartikov.replica.advanced_sample.core.utils.resolve
import me.aartikov.replica.common.AbstractLoadable

/**
 * Displays Replica state ([AbstractLoadable]).
 */
@Composable
fun <T : Any> LceWidget(
    state: AbstractLoadable<T>,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (data: T, refreshing: Boolean) -> Unit
) {
    val loading = state.loading
    val data = state.data
    val error = state.error

    when {
        data != null -> content(data, loading)

        loading -> FullscreenCircularProgress(modifier)

        error != null -> ErrorPlaceholder(
            errorMessage = error.exception.errorMessage.resolve(),
            onRetryClick = onRetryClick,
            modifier = modifier
        )
    }
}