package me.aartikov.replica.sample.core.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import me.aartikov.sesame.dialog.DialogControl

@Composable
fun <T : Any, R : Any> ShowDialog(
    dialogControl: DialogControl<T, R>,
    dialog: @Composable (data: T) -> Unit
) {
    val state by dialogControl.stateFlow.collectAsState()
    (state as? DialogControl.State.Shown)?.data?.let { data ->
        dialog(data)
    }
}