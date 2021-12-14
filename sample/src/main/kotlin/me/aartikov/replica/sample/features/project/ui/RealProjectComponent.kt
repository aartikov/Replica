package me.aartikov.replica.sample.features.project.ui

import androidx.compose.runtime.getValue
import com.arkivanov.decompose.ComponentContext
import me.aartikov.replica.sample.core.ui.error_handing.ErrorHandler
import me.aartikov.replica.sample.core.ui.external_app_service.ExternalAppService
import me.aartikov.replica.sample.core.ui.utils.componentCoroutineScope
import me.aartikov.replica.sample.core.ui.utils.observeAndHandleErrors
import me.aartikov.replica.sample.core.ui.utils.safeLaunch
import me.aartikov.replica.sample.features.project.domain.Project
import me.aartikov.replica.simple.Replica

class RealProjectComponent(
    componentContext: ComponentContext,
    private val projectReplica: Replica<Project>,
    private val externalAppService: ExternalAppService,
    private val errorHandler: ErrorHandler
) : ComponentContext by componentContext, ProjectComponent {

    private val coroutineScope = componentCoroutineScope()

    override val projectState by projectReplica.observeAndHandleErrors(lifecycle, errorHandler)

    override fun onRefresh() {
        projectReplica.refresh()
    }

    override fun onRetryClick() {
        projectReplica.refresh()
    }

    override fun onUrlClick(url: String) {
        coroutineScope.safeLaunch(errorHandler) {
            externalAppService.openBrowser(url)
        }
    }
}