package me.aartikov.replica.advanced_sample.features.fruits.ui.favourites

import com.arkivanov.decompose.ComponentContext
import me.aartikov.replica.advanced_sample.core.error_handling.ErrorHandler
import me.aartikov.replica.advanced_sample.core.utils.observe
import me.aartikov.replica.advanced_sample.features.fruits.domain.Fruit
import me.aartikov.replica.advanced_sample.features.fruits.domain.FruitFavouriteUpdater
import me.aartikov.replica.advanced_sample.features.fruits.domain.FruitId
import me.aartikov.replica.single.Replica

class RealFruitsFavouritesComponent(
    componentContext: ComponentContext,
    private val fruitsReplica: Replica<List<Fruit>>,
    private val fruitFavouriteUpdater: FruitFavouriteUpdater,
    private val errorHandler: ErrorHandler
) : ComponentContext by componentContext, FruitsFavouritesComponent {

    override val fruitsState = fruitsReplica.observe(lifecycle, errorHandler)

    override val removingInProgress = fruitFavouriteUpdater.inProgressRequestIds

    override fun onRemoveFruitClick(fruitId: FruitId) {
        val fruit = fruitsState.value.data?.find { it.id == fruitId } ?: return

        if (!fruit.isFavourite || fruitId in removingInProgress.value) return

        fruitFavouriteUpdater.removeFromFavourite(fruitId, errorHandler)
    }

    override fun onRefresh() = fruitsReplica.refresh()
    override fun onRetryClick() = fruitsReplica.revalidate()
}