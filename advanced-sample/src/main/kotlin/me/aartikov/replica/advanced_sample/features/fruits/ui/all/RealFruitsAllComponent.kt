package me.aartikov.replica.advanced_sample.features.fruits.ui.all

import com.arkivanov.decompose.ComponentContext
import me.aartikov.replica.advanced_sample.core.error_handling.ErrorHandler
import me.aartikov.replica.advanced_sample.core.utils.observe
import me.aartikov.replica.advanced_sample.features.fruits.data.all_fruits.AllFruitsRepository
import me.aartikov.replica.advanced_sample.features.fruits.domain.FruitFavouriteUpdater
import me.aartikov.replica.advanced_sample.features.fruits.domain.FruitId

class RealFruitsAllComponent(
    componentContext: ComponentContext,
    allFruitsRepository: AllFruitsRepository,
    private val fruitFavouriteUpdater: FruitFavouriteUpdater,
    private val errorHandler: ErrorHandler
) : ComponentContext by componentContext, FruitsAllComponent {

    private val fruitsReplica = allFruitsRepository.fruitsReplica

    override val fruitsState = fruitsReplica.observe(lifecycle, errorHandler)

    override fun onFruitClick(fruitId: FruitId) {
        val fruit = fruitsState.value.data?.find { it.id == fruitId } ?: return
        fruitFavouriteUpdater.setFruitFavourite(fruit.id, !fruit.isFavourite, errorHandler)
    }

    override fun onRefresh() = fruitsReplica.refresh()

    override fun onRetryClick() = fruitsReplica.revalidate()
}