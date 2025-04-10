package me.aartikov.replica.advanced_sample.features.fruits.ui.all

import com.arkivanov.decompose.ComponentContext
import me.aartikov.replica.advanced_sample.core.error_handling.ErrorHandler
import me.aartikov.replica.advanced_sample.core.utils.observe
import me.aartikov.replica.advanced_sample.features.fruits.domain.Fruit
import me.aartikov.replica.advanced_sample.features.fruits.domain.FruitFavouriteUpdater
import me.aartikov.replica.advanced_sample.features.fruits.domain.FruitId
import me.aartikov.replica.single.Replica

class RealFruitsAllComponent(
    componentContext: ComponentContext,
    private val fruitsReplica: Replica<List<Fruit>>,
    private val fruitFavouriteUpdater: FruitFavouriteUpdater,
    private val errorHandler: ErrorHandler
) : ComponentContext by componentContext, FruitsAllComponent {
    override val fruitsState = fruitsReplica.observe(lifecycle, errorHandler)

    override fun onFruitClick(fruitId: FruitId) {
        val fruit = fruitsState.value.data?.find { it.id == fruitId } ?: return
        fruitFavouriteUpdater.toggleFruitFavourite(fruit, errorHandler)
    }

    override fun onRefresh() = fruitsReplica.refresh()
    override fun onRetryClick() = fruitsReplica.revalidate()
}