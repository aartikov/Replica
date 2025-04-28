package me.aartikov.replica.advanced_sample.core.widget

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
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
    content: @Composable (data: T, refreshing: Boolean) -> Unit,
) {
    val loading = state.loading
    val data = state.data
    val error = state.error

    Box(modifier) {
        when {
            data != null -> content(data, loading)

            loading -> FullscreenCircularProgress(Modifier.navigationBarsPadding())

            error != null -> ErrorPlaceholder(
                modifier = Modifier.navigationBarsPadding(),
                errorMessage = error.exception.errorMessage.resolve(),
                onRetryClick = onRetryClick,
            )
        }
    }
}