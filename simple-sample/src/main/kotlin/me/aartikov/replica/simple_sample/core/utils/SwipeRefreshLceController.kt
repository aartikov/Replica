package me.aartikov.replica.simple_sample.core.utils

import androidx.core.view.isVisible
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import me.aartikov.replica.simple_sample.core.error_handling.errorMessage
import me.aartikov.replica.simple_sample.databinding.LayoutErrorViewBinding
import me.aartikov.replica.simple_sample.databinding.LayoutLoadingViewBinding
import me.aartikov.replica.single.Loadable

class SwipeRefreshLceController<T : Any>(
    private val swipeRefeshView: SwipeRefreshLayout,
    private val loadingView: LayoutLoadingViewBinding,
    private val errorView: LayoutErrorViewBinding,
    private val setContent: (T, refreshing: Boolean) -> Unit,
    private val resetContent: () -> Unit = {},
    private val onRefresh: () -> Unit,
    private val onRetryClick: () -> Unit
) {

    var swipeGestureOccurred = false

    init {
        swipeRefeshView.setOnRefreshListener {
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
                swipeRefeshView.isVisible = true
                swipeRefeshView.isRefreshing = loading && swipeGestureOccurred
                loadingView.root.isVisible = false
                errorView.root.isVisible = false
                setContent(data, loading && !swipeGestureOccurred)
            }

            loading -> {
                swipeRefeshView.isVisible = false
                swipeRefeshView.isRefreshing = false
                loadingView.root.isVisible = true
                errorView.root.isVisible = false
                resetContent()
            }

            error != null -> {
                swipeRefeshView.isVisible = false
                swipeRefeshView.isRefreshing = false
                loadingView.root.isVisible = false
                errorView.root.isVisible = true
                errorView.message.text =
                    error.exception.errorMessage.resolve(errorView.root.context)
                resetContent()
            }

            else -> {
                swipeRefeshView.isVisible = false
                swipeRefeshView.isRefreshing = false
                loadingView.root.isVisible = false
                errorView.root.isVisible = false
                resetContent()
            }
        }
    }
}