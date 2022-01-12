package me.aartikov.replica.sample.features.dudes.ui

import androidx.compose.runtime.getValue
import com.arkivanov.decompose.ComponentContext
import me.aartikov.replica.sample.core.ui.error_handing.ErrorHandler
import me.aartikov.replica.sample.core.ui.utils.observe
import me.aartikov.replica.sample.features.dudes.domain.Dude
import me.aartikov.replica.single.Replica

class RealDudesComponent(
    componentContext: ComponentContext,
    private val dudesReplica: Replica<List<Dude>>,
    errorHandler: ErrorHandler
) : ComponentContext by componentContext, DudesComponent {

    override val dudesState by dudesReplica.observe(lifecycle, errorHandler)

    override fun onRefresh() {
        dudesReplica.refresh()
    }

    override fun onRetryClick() {
        dudesReplica.refresh()
    }
}