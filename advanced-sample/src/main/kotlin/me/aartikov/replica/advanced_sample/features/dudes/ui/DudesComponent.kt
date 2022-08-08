package me.aartikov.replica.advanced_sample.features.dudes.ui

import me.aartikov.replica.advanced_sample.features.dudes.domain.Dude
import me.aartikov.replica.single.Loadable

interface DudesComponent {

    val dudesState: Loadable<List<Dude>>

    fun onRefresh()

    fun onRetryClick()
}