package me.aartikov.replica.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import me.aartikov.replica.paged.PagedReplica
import me.aartikov.replica.paged.PagedReplicaObserver

/**
 * Observes [PagedReplica] in a scope of [ViewModel]. ViewModel has to be [Activable].
 */
fun <T : Any, VM> PagedReplica<T>.observe(viewModel: VM): PagedReplicaObserver<T>
            where VM : ViewModel, VM : Activable {
    return this.observe(
        viewModel.viewModelScope,
        viewModel.activeFlow
    )
}