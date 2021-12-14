package me.aartikov.replica.sample.core.ui.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.aartikov.replica.sample.core.ui.error_handing.errorMessage
import me.aartikov.replica.sample.core.ui.utils.resolve
import me.aartikov.replica.simple.Loadable

@Composable
fun <T : Any> LceWidget(
    state: Loadable<T>,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
    emptyContent: @Composable (() -> Unit)? = null,
    content: @Composable (data: T, refreshing: Boolean) -> Unit
) {
    val (data, loading, error) = state
    when {
        data != null -> content(data, loading)

        loading -> FullscreenCircularProgress(modifier)

        error != null -> ErrorPlaceholder(
            errorMessage = error.errorMessage.resolve(),
            onRetryClick = onRetryClick,
            modifier = modifier
        )

        else -> if (emptyContent != null) {
            emptyContent()
        }
    }
}