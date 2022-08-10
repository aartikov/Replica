package me.aartikov.replica.simple_sample.core.vm

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

/**
 * Binds [Activable] to Android [Lifecycle].
 * An activable become active in onStart and inactive in onStop.
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