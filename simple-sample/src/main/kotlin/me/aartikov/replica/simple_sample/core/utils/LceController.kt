package me.aartikov.replica.simple_sample.core.utils

import android.view.View
import androidx.core.view.isVisible
import me.aartikov.replica.simple_sample.core.error_handling.errorMessage
import me.aartikov.replica.simple_sample.databinding.LayoutErrorViewBinding
import me.aartikov.replica.simple_sample.databinding.LayoutLoadingViewBinding
import me.aartikov.replica.single.Loadable

/**
 * Simplifies binding of Replica [Loadable] state to UI.
 */
class LceController<T : Any>(
    private val contentView: View,
    private val loadingView: LayoutLoadingViewBinding,
    private val errorView: LayoutErrorViewBinding,
    private val setContent: (T, refreshing: Boolean) -> Unit,
    private val resetContent: () -> Unit = {},
    private val onRetryClick: () -> Unit
) {

    init {
        errorView.retryButton.setOnClickListener {
            onRetryClick()
        }
    }

    fun setState(state: Loadable<T>) {
        val (loading, data, error) = state

        when {
            data != null -> {
                contentView.isVisible = true
                loadingView.root.isVisible = false
                errorView.root.isVisible = false
                setContent(data, loading)
            }

            loading -> {
                contentView.isVisible = false
                loadingView.root.isVisible = true
                errorView.root.isVisible = false
                resetContent()
            }

            error != null -> {
                contentView.isVisible = false
                loadingView.root.isVisible = false
                errorView.root.isVisible = true
                errorView.message.text =
                    error.exception.errorMessage.resolve(errorView.root.context)
                resetContent()
            }

            else -> {
                contentView.isVisible = false
                loadingView.root.isVisible = false
                errorView.root.isVisible = false
                resetContent()
            }
        }
    }
}