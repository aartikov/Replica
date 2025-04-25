package me.aartikov.replica.advanced_sample.features.dudes.ui

import com.arkivanov.decompose.ComponentContext
import me.aartikov.replica.advanced_sample.core.error_handling.ErrorHandler
import me.aartikov.replica.advanced_sample.core.utils.observe
import me.aartikov.replica.advanced_sample.features.dudes.data.DudeRepository

class RealDudesComponent(
    componentContext: ComponentContext,
    dudeRepository: DudeRepository,
    errorHandler: ErrorHandler
) : ComponentContext by componentContext, DudesComponent {

    private val dudesReplica = dudeRepository.dudesReplica

    override val dudesState = dudesReplica.observe(lifecycle, errorHandler)

    override fun onRefresh() {
        dudesReplica.refresh()
    }

    override fun onRetryClick() {
        dudesReplica.refresh()
    }

    override fun onLoadNext() {
        dudesReplica.loadNext()
    }
}