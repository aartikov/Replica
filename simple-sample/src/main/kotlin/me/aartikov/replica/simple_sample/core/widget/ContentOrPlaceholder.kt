package me.aartikov.replica.simple_sample.core.widget

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable

@Composable
fun <T> BoxScope.ContentOrPlaceholder(
    items: List<T>,
    placeholder: @Composable () -> Unit,
    content: @Composable (List<T>) -> Unit,
) {
    content(items)

    if (items.isEmpty()) placeholder()
}
