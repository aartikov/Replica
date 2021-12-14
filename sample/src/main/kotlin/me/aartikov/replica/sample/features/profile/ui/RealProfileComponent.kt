package me.aartikov.replica.sample.features.profile.ui

import androidx.compose.runtime.getValue
import com.arkivanov.decompose.ComponentContext
import me.aartikov.replica.sample.core.ui.error_handing.ErrorHandler
import me.aartikov.replica.sample.core.ui.utils.observeAndHandleErrors
import me.aartikov.replica.sample.features.profile.domain.Profile
import me.aartikov.replica.simple.Replica

class RealProfileComponent(
    componentContext: ComponentContext,
    private val profileReplica: Replica<Profile>,
    errorHandler: ErrorHandler
) : ComponentContext by componentContext, ProfileComponent {

    override val profileState by profileReplica.observeAndHandleErrors(lifecycle, errorHandler)

    override fun onPullToRefresh() {
        profileReplica.refresh()
    }

    override fun onRetryClick() {
        profileReplica.refresh()
    }
}