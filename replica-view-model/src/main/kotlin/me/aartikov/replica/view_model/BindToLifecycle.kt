package me.aartikov.replica.view_model

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

/**
 * Binds [Activable] to onStart and onStop events of Android [Lifecycle].
 */
fun Activable.bindToLifecycle(lifecycle: Lifecycle) {
    lifecycle.addObserver(ActivableLifecycleObserver(this))
}

private class ActivableLifecycleObserver(
    private val activable: Activable
) : DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        activable.activeFlow.value = true
    }

    override fun onStop(owner: LifecycleOwner) {
        activable.activeFlow.value = false
    }
}