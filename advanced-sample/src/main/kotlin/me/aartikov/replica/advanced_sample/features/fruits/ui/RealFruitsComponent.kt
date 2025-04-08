package me.aartikov.replica.advanced_sample.features.fruits.ui

import com.arkivanov.decompose.ComponentContext
import me.aartikov.replica.advanced_sample.core.error_handling.ErrorHandler
import me.aartikov.replica.advanced_sample.core.utils.observe
import me.aartikov.replica.advanced_sample.features.fruits.domain.Fruit
import me.aartikov.replica.advanced_sample.features.fruits.domain.FruitId
import me.aartikov.replica.advanced_sample.features.fruits.domain.FruitLikeUpdater
import me.aartikov.replica.single.Replica

class RealFruitsComponent(
    componentContext: ComponentContext,
    private val fruitsReplica: Replica<List<Fruit>>,
    private val fruitLikeUpdater: FruitLikeUpdater,
    private val errorHandler: ErrorHandler
) : ComponentContext by componentContext, FruitsComponent {

    override val fruitsState = fruitsReplica.observe(lifecycle, errorHandler)

    override fun onFruitClick(fruitId: FruitId) {
        val fruit = fruitsState.value.data?.find { it.id == fruitId } ?: return
        fruitLikeUpdater.setFruitLiked(fruit.id, liked = !fruit.liked, errorHandler)
    }

    override fun onRefresh() {
        fruitsReplica.refresh()
    }

    override fun onRetryClick() {
        fruitsReplica.refresh()
    }
}