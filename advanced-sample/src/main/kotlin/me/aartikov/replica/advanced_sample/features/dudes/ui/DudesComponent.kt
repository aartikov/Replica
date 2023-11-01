package me.aartikov.replica.advanced_sample.features.dudes.ui

import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.advanced_sample.features.dudes.domain.Dude
import me.aartikov.replica.single.Loadable

interface DudesComponent {

    val dudesState: StateFlow<Loadable<List<Dude>>>

    fun onRefresh()

    fun onRetryClick()
}