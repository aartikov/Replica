package me.aartikov.replica.advanced_sample.features.dudes.ui

import com.arkivanov.decompose.ComponentContext
import me.aartikov.replica.advanced_sample.core.error_handling.ErrorHandler
import me.aartikov.replica.advanced_sample.core.utils.observe
import me.aartikov.replica.advanced_sample.features.dudes.domain.Dude
import me.aartikov.replica.advanced_sample.features.dudes.domain.DudesPage
import me.aartikov.replica.paged.PagedReplica

class RealDudesComponent(
    componentContext: ComponentContext,
    private val dudesReplica: PagedReplica<Dude, DudesPage>,
    errorHandler: ErrorHandler
) : ComponentContext by componentContext, DudesComponent {

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