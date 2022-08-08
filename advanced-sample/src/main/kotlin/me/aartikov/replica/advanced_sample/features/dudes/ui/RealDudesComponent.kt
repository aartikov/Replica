package me.aartikov.replica.advanced_sample.features.dudes.ui

import androidx.compose.runtime.getValue
import com.arkivanov.decompose.ComponentContext
import me.aartikov.replica.advanced_sample.core.error_handling.ErrorHandler
import me.aartikov.replica.advanced_sample.core.utils.observe
import me.aartikov.replica.advanced_sample.features.dudes.domain.Dude
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