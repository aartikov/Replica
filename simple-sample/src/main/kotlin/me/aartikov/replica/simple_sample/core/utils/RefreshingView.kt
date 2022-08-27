package me.aartikov.replica.simple_sample.core.utils

import androidx.core.view.isVisible
import me.aartikov.replica.simple_sample.databinding.LayoutRefreshingViewBinding

var LayoutRefreshingViewBinding.refreshing: Boolean
    get() = progressBar.isVisible
    set(value) {
        progressBar.isVisible = value
    }