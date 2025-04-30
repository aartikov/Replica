package me.aartikov.replica.simple_sample.core.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Displays a horizontal progress bar on top of screen when data is refreshing.
 */
@Composable
fun RefreshingProgress(active: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = active,
        modifier = modifier,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth()
        )
    }
}