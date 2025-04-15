package me.aartikov.replica.advanced_sample.features.fruits

import com.arkivanov.decompose.ComponentContext
import me.aartikov.replica.advanced_sample.core.ComponentFactory
import me.aartikov.replica.advanced_sample.features.fruits.data.all_fruits.AllFruitsRepository
import me.aartikov.replica.advanced_sample.features.fruits.data.all_fruits.AllFruitsRepositoryImpl
import me.aartikov.replica.advanced_sample.features.fruits.data.api.FakeFruitApi
import me.aartikov.replica.advanced_sample.features.fruits.data.api.FruitApi
import me.aartikov.replica.advanced_sample.features.fruits.data.favourite.FavouriteFruitsRepository
import me.aartikov.replica.advanced_sample.features.fruits.data.favourite.FavouriteFruitsRepositoryImpl
import me.aartikov.replica.advanced_sample.features.fruits.domain.FruitFavouriteUpdater
import me.aartikov.replica.advanced_sample.features.fruits.ui.FruitsComponent
import me.aartikov.replica.advanced_sample.features.fruits.ui.RealFruitsComponent
import me.aartikov.replica.advanced_sample.features.fruits.ui.all.FruitsAllComponent
import me.aartikov.replica.advanced_sample.features.fruits.ui.all.RealFruitsAllComponent
import me.aartikov.replica.advanced_sample.features.fruits.ui.favourites.FruitsFavouritesComponent
import me.aartikov.replica.advanced_sample.features.fruits.ui.favourites.RealFruitsFavouritesComponent
import org.koin.core.component.get
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val fruitsModule = module {
    single<FruitApi> { FakeFruitApi(get()) }
    singleOf(::AllFruitsRepositoryImpl) bind AllFruitsRepository::class
    singleOf(::FavouriteFruitsRepositoryImpl) bind FavouriteFruitsRepository::class
    singleOf(::FruitFavouriteUpdater)
}

fun ComponentFactory.createFruitsComponent(
    componentContext: ComponentContext
): FruitsComponent {
    return RealFruitsComponent(componentContext, get())
}

fun ComponentFactory.createFruitsAllComponent(
    componentContext: ComponentContext
): FruitsAllComponent {
    val fruitsReplica = get<AllFruitsRepository>().fruitsReplica
    return RealFruitsAllComponent(componentContext, fruitsReplica, get(), get())
}

fun ComponentFactory.createFruitsFavouritesComponent(
    componentContext: ComponentContext
): FruitsFavouritesComponent {
    val fruitsReplica = get<FavouriteFruitsRepository>().favouriteFruitsReplica
    return RealFruitsFavouritesComponent(componentContext, fruitsReplica, get(), get())
}
