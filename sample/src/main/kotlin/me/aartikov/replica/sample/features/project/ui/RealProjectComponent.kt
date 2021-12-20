package me.aartikov.replica.sample.features.project.ui

import androidx.compose.runtime.getValue
import com.arkivanov.decompose.ComponentContext
import me.aartikov.replica.sample.core.ui.error_handing.ErrorHandler
import me.aartikov.replica.sample.core.ui.error_handing.safeRun
import me.aartikov.replica.sample.core.ui.external_app_service.ExternalAppService
import me.aartikov.replica.sample.core.ui.utils.observe
import me.aartikov.replica.sample.features.project.domain.Project
import me.aartikov.replica.single.Replica

class RealProjectComponent(
    componentContext: ComponentContext,
    private val projectReplica: Replica<Project>,
    private val externalAppService: ExternalAppService,
    private val errorHandler: ErrorHandler
) : ComponentContext by componentContext, ProjectComponent {

    override val projectState by projectReplica.observe(lifecycle, errorHandler)

    override fun onRefresh() {
        projectReplica.refresh()
    }

    override fun onRetryClick() {
        projectReplica.refresh()
    }

    override fun onUrlClick(url: String) {
        safeRun(errorHandler) {
            externalAppService.openBrowser(url)
        }
    }
}