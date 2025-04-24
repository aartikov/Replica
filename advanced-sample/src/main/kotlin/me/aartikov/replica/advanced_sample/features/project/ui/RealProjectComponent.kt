package me.aartikov.replica.advanced_sample.features.project.ui

import com.arkivanov.decompose.ComponentContext
import me.aartikov.replica.advanced_sample.core.error_handling.ErrorHandler
import me.aartikov.replica.advanced_sample.core.error_handling.safeRun
import me.aartikov.replica.advanced_sample.core.external_app_service.ExternalAppService
import me.aartikov.replica.advanced_sample.core.utils.observe
import me.aartikov.replica.advanced_sample.features.project.data.ProjectRepository

class RealProjectComponent(
    componentContext: ComponentContext,
    projectRepository: ProjectRepository,
    private val externalAppService: ExternalAppService,
    private val errorHandler: ErrorHandler
) : ComponentContext by componentContext, ProjectComponent {

    private val projectReplica = projectRepository.projectReplica

    override val projectState = projectReplica.observe(lifecycle, errorHandler)

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