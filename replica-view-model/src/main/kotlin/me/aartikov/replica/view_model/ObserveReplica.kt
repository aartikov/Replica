package me.aartikov.replica.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import me.aartikov.replica.single.Replica
import me.aartikov.replica.single.ReplicaObserver

/**
 * Observes [Replica] in a scope of [ViewModel]. ViewModel has to be [Activable].
 */
fun <T : Any, VM> Replica<T>.observe(viewModel: VM): ReplicaObserver<T>
    where VM : ViewModel, VM : Activable {
    return this.observe(
        viewModel.viewModelScope,
        viewModel.activeFlow
    )
}