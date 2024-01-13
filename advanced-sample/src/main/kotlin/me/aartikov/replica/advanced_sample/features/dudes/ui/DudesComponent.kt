package me.aartikov.replica.advanced_sample.features.dudes.ui

import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.advanced_sample.features.dudes.domain.Dude
import me.aartikov.replica.advanced_sample.features.dudes.domain.DudesPage
import me.aartikov.replica.paged.Paged

interface DudesComponent {

    val dudesState: StateFlow<Paged<Dude, DudesPage>>

    fun onRefresh()

    fun onRetryClick()

    fun onLoadNext()
}