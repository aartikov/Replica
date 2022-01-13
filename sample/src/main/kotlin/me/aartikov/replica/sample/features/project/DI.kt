package me.aartikov.replica.sample.features.project

import com.arkivanov.decompose.ComponentContext
import me.aartikov.replica.sample.core.data.network.BaseUrl
import me.aartikov.replica.sample.core.data.network.NetworkApiFactory
import me.aartikov.replica.sample.core.ui.ComponentFactory
import me.aartikov.replica.sample.features.project.data.ProjectApi
import me.aartikov.replica.sample.features.project.data.ProjectRepository
import me.aartikov.replica.sample.features.project.data.ProjectRepositoryImpl
import me.aartikov.replica.sample.features.project.data.ProjectStorage
import me.aartikov.replica.sample.features.project.ui.ProjectComponent
import me.aartikov.replica.sample.features.project.ui.RealProjectComponent
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.koin.dsl.module

val projectModule = module {
    single<ProjectApi> {
        get<NetworkApiFactory>(named(BaseUrl.Github)).createApi()
    }
    single<ProjectRepository> {
        ProjectRepositoryImpl(
            get(),
            ProjectStorage(androidContext()),
            get()
        )
    }
}

fun ComponentFactory.createProjectComponent(
    componentContext: ComponentContext
): ProjectComponent {
    val projectReplica = get<ProjectRepository>().projectReplica
    return RealProjectComponent(componentContext, projectReplica, get(), get())
}