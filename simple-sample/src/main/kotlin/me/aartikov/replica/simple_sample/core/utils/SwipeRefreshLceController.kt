package me.aartikov.replica.simple_sample.core.utils

import androidx.core.view.isVisible
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import me.aartikov.replica.simple_sample.core.error_handling.errorMessage
import me.aartikov.replica.simple_sample.databinding.LayoutErrorViewBinding
import me.aartikov.replica.simple_sample.databinding.LayoutLoadingViewBinding
import me.aartikov.replica.single.Loadable

/**
 * Simplifies binding of Replica [Loadable] state to UI with swipe-to-refresh functionality.
 */
class SwipeRefreshLceController<T : Any>(
    private val contentView: SwipeRefreshLayout,
    private val loadingView: LayoutLoadingViewBinding,
    private val errorView: LayoutErrorViewBinding,
    private val setContent: (T, refreshing: Boolean) -> Unit,
    private val resetContent: () -> Unit = {},
    private val onRefresh: () -> Unit,
    private val onRetryClick: () -> Unit
) {

    var swipeGestureOccurred = false

    init {
        contentView.setOnRefreshListener {
            swipeGestureOccurred = true
            onRefresh()
        }
        errorView.retryButton.setOnClickListener {
            onRetryClick()
        }
    }

    fun setState(state: Loadable<T>) {
        val (loading, data, error) = state

        if (!loading) {
            swipeGestureOccurred = false
        }

        when {
            data != null -> {
                contentView.isVisible = true
                contentView.isRefreshing = loading && swipeGestureOccurred
                loadingView.root.isVisible = false
                errorView.root.isVisible = false
                setContent(data, loading && !swipeGestureOccurred)
            }

            loading -> {
                contentView.isVisible = false
                contentView.isRefreshing = false
                loadingView.root.isVisible = true
                errorView.root.isVisible = false
                resetContent()
            }

            error != null -> {
                contentView.isVisible = false
                contentView.isRefreshing = false
                loadingView.root.isVisible = false
                errorView.root.isVisible = true
                errorView.message.text =
                    error.exception.errorMessage.resolve(errorView.root.context)
                resetContent()
            }

            else -> {
                contentView.isVisible = false
                contentView.isRefreshing = false
                loadingView.root.isVisible = false
                errorView.root.isVisible = false
                resetContent()
            }
        }
    }
}