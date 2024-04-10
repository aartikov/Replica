package me.aartikov.replica.advanced_sample.features.project

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.Dispatchers
import me.aartikov.replica.advanced_sample.core.ComponentFactory
import me.aartikov.replica.advanced_sample.core.network.BaseUrl
import me.aartikov.replica.advanced_sample.core.network.NetworkApiFactory
import me.aartikov.replica.advanced_sample.features.project.data.ProjectApi
import me.aartikov.replica.advanced_sample.features.project.data.ProjectRepository
import me.aartikov.replica.advanced_sample.features.project.data.ProjectRepositoryImpl
import me.aartikov.replica.advanced_sample.features.project.data.settings.AndroidSettingsFactory
import me.aartikov.replica.advanced_sample.features.project.data.settings.SettingsFactory
import me.aartikov.replica.advanced_sample.features.project.ui.ProjectComponent
import me.aartikov.replica.advanced_sample.features.project.ui.RealProjectComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.koin.dsl.module

val projectModule = module {
    single<ProjectApi> {
        get<NetworkApiFactory>(named(BaseUrl.Github)).createApi()
    }
    single<ProjectRepository> {
        ProjectRepositoryImpl(get(), get(), get())
    }
    single<SettingsFactory> {
        AndroidSettingsFactory(get(), Dispatchers.IO)
    }
}

fun ComponentFactory.createProjectComponent(
    componentContext: ComponentContext
): ProjectComponent {
    val projectReplica = get<ProjectRepository>().projectReplica
    return RealProjectComponent(componentContext, projectReplica, get(), get())
}