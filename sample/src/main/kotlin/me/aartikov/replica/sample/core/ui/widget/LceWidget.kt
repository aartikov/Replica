package me.aartikov.replica.sample.core.ui.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.aartikov.replica.sample.core.ui.error_handing.errorMessage
import me.aartikov.replica.sample.core.ui.utils.resolve
import me.aartikov.replica.single.Loadable

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
            errorMessage = error.firstException.errorMessage.resolve(),
            onRetryClick = onRetryClick,
            modifier = modifier
        )
    }
}