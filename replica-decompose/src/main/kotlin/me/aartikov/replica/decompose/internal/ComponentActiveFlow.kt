package me.aartikov.replica.decompose.internal

import com.arkivanov.essenty.lifecycle.Lifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal fun Lifecycle.activeFlow(): StateFlow<Boolean> {
    val flow = MutableStateFlow(
        this.state == Lifecycle.State.STARTED || this.state == Lifecycle.State.RESUMED
    )

    this.subscribe(object : Lifecycle.Callbacks {

        override fun onCreate() {
            flow.value = false
        }

        override fun onStart() {
            flow.value = true
        }

        override fun onResume() {
            flow.value = true
        }

        override fun onPause() {
            flow.value = false
        }

        override fun onStop() {
            flow.value = false
        }

        override fun onDestroy() {
            flow.value = false
        }
    })

    return flow
}