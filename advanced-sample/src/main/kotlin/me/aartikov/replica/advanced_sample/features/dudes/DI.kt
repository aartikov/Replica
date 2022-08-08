package me.aartikov.replica.advanced_sample.features.dudes

import com.arkivanov.decompose.ComponentContext
import me.aartikov.replica.advanced_sample.core.ComponentFactory
import me.aartikov.replica.advanced_sample.core.network.BaseUrl
import me.aartikov.replica.advanced_sample.core.network.NetworkApiFactory
import me.aartikov.replica.advanced_sample.features.dudes.data.DudeApi
import me.aartikov.replica.advanced_sample.features.dudes.data.DudeRepository
import me.aartikov.replica.advanced_sample.features.dudes.data.DudeRepositoryImpl
import me.aartikov.replica.advanced_sample.features.dudes.ui.DudesComponent
import me.aartikov.replica.advanced_sample.features.dudes.ui.RealDudesComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.koin.dsl.module

val dudesModule = module {
    single<DudeApi> {
        get<NetworkApiFactory>(named(BaseUrl.RandomData)).createApi()
    }
    single<DudeRepository> {
        DudeRepositoryImpl(get(), get())
    }
}

fun ComponentFactory.createDudesComponent(
    componentContext: ComponentContext
): DudesComponent {
    val dudesReplica = get<DudeRepository>().dudesReplica
    return RealDudesComponent(componentContext, dudesReplica, get())
}