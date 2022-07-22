package me.aartikov.replica.sample.features.fruits

import com.arkivanov.decompose.ComponentContext
import me.aartikov.replica.sample.core.ComponentFactory
import me.aartikov.replica.sample.features.fruits.data.FakeFruitApi
import me.aartikov.replica.sample.features.fruits.data.FruitApi
import me.aartikov.replica.sample.features.fruits.data.FruitRepository
import me.aartikov.replica.sample.features.fruits.data.FruitRepositoryImpl
import me.aartikov.replica.sample.features.fruits.domain.ToggleFruitLikeInteractor
import me.aartikov.replica.sample.features.fruits.ui.FruitsComponent
import me.aartikov.replica.sample.features.fruits.ui.RealFruitsComponent
import org.koin.core.component.get
import org.koin.dsl.module

val fruitsModule = module {
    single<FruitApi> { FakeFruitApi(get()) }
    single<FruitRepository> { FruitRepositoryImpl(get(), get()) }
    factory { ToggleFruitLikeInteractor(get()) }
}

fun ComponentFactory.createFruitsComponent(
    componentContext: ComponentContext
): FruitsComponent {
    val fruitsReplica = get<FruitRepository>().fruitsReplica
    return RealFruitsComponent(componentContext, fruitsReplica, get(), get(), get())
}