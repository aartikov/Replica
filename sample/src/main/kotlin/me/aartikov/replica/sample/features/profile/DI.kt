package me.aartikov.replica.sample.features.profile

import com.arkivanov.decompose.ComponentContext
import me.aartikov.replica.sample.core.ui.ComponentFactory
import me.aartikov.replica.sample.features.profile.data.ProfileRepository
import me.aartikov.replica.sample.features.profile.data.ProfileRepositoryImpl
import me.aartikov.replica.sample.features.profile.ui.ProfileComponent
import me.aartikov.replica.sample.features.profile.ui.RealProfileComponent
import org.koin.core.component.get
import org.koin.dsl.module

val profileModule = module {
    single<ProfileRepository> { ProfileRepositoryImpl(get()) }
}

fun ComponentFactory.createProfileComponent(
    componentContext: ComponentContext
): ProfileComponent {
    val profileReplica = get<ProfileRepository>().profileReplica
    return RealProfileComponent(componentContext, profileReplica, get())
}