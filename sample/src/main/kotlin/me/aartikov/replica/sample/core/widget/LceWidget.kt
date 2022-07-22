package me.aartikov.replica.sample.core.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.aartikov.replica.sample.core.error_handling.errorMessage
import me.aartikov.replica.sample.core.utils.resolve
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
    val (loading, data, error) = state
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