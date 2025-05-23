package me.aartikov.replica.advanced_sample.features.fruits.ui.favourites

import com.arkivanov.decompose.ComponentContext
import me.aartikov.replica.advanced_sample.core.error_handling.ErrorHandler
import me.aartikov.replica.advanced_sample.core.utils.observe
import me.aartikov.replica.advanced_sample.features.fruits.data.favourite.FavouriteFruitsRepository
import me.aartikov.replica.advanced_sample.features.fruits.domain.FruitFavouriteUpdater
import me.aartikov.replica.advanced_sample.features.fruits.domain.FruitId

class RealFruitsFavouritesComponent(
    componentContext: ComponentContext,
    favouriteFruitsRepository: FavouriteFruitsRepository,
    private val fruitFavouriteUpdater: FruitFavouriteUpdater,
    private val errorHandler: ErrorHandler
) : ComponentContext by componentContext, FruitsFavouritesComponent {

    private val fruitsReplica = favouriteFruitsRepository.favouriteFruitsReplica

    override val fruitsState = fruitsReplica.observe(lifecycle, errorHandler)

    override val removingInProgress = fruitFavouriteUpdater.inProgressRequestIds

    override fun onRemoveFruitClick(fruitId: FruitId) {
        val fruit = fruitsState.value.data?.find { it.id == fruitId } ?: return

        if (!fruit.isFavourite || fruitId in removingInProgress.value) return

        fruitFavouriteUpdater.setFruitFavourite(fruitId, false, errorHandler)
    }

    override fun onRefresh() = fruitsReplica.refresh()

    override fun onRetryClick() = fruitsReplica.revalidate()
}