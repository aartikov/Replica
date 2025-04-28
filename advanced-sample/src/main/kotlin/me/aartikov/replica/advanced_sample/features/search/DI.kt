package me.aartikov.replica.advanced_sample.features.search

import com.arkivanov.decompose.ComponentContext
import me.aartikov.replica.advanced_sample.core.ComponentFactory
import me.aartikov.replica.advanced_sample.core.network.BaseUrl
import me.aartikov.replica.advanced_sample.core.network.NetworkApiFactory
import me.aartikov.replica.advanced_sample.features.search.data.WikiApi
import me.aartikov.replica.advanced_sample.features.search.data.WikiRepository
import me.aartikov.replica.advanced_sample.features.search.data.WikiRepositoryImpl
import me.aartikov.replica.advanced_sample.features.search.ui.RealSearchComponent
import me.aartikov.replica.advanced_sample.features.search.ui.SearchComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.koin.dsl.module

val searchModule = module {
    single<WikiApi> { get<NetworkApiFactory>(named(BaseUrl.Wiki)).createApi() }
    single<WikiRepository> { WikiRepositoryImpl(get(), get()) }
}

fun ComponentFactory.createSearchComponent(
    componentContext: ComponentContext,
): SearchComponent {
    return RealSearchComponent(componentContext, get(), get(), get())
}
