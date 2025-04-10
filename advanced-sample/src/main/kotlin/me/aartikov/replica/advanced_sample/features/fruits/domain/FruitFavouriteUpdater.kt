package me.aartikov.replica.advanced_sample.features.fruits.domain

import kotlinx.coroutines.CoroutineScope
import me.aartikov.replica.advanced_sample.core.error_handling.ErrorHandler
import me.aartikov.replica.advanced_sample.core.utils.CancellingJobLauncher
import me.aartikov.replica.advanced_sample.features.fruits.data.all_fruits.AllFruitsRepository
import me.aartikov.replica.advanced_sample.features.fruits.data.favourite.FavouriteFruitsRepository
import kotlin.time.Duration.Companion.milliseconds

class FruitFavouriteUpdater(
    applicationCoroutineScope: CoroutineScope,
    private val fruitRepository: AllFruitsRepository,
    private val favouriteRepository: FavouriteFruitsRepository
) {
    private val jobLauncher = CancellingJobLauncher<FruitId, Boolean>(applicationCoroutineScope)

    val inProgressRequestIds get() = jobLauncher.inProgressRequestIds

    fun toggleFruitFavourite(fruit: Fruit, errorHandler: ErrorHandler) {
        val targetIsFavourite = !fruit.isFavourite

        jobLauncher.launchJob(fruit.id, targetIsFavourite, errorHandler) {
            fruitRepository.setFruitFavourite(
                fruitId = fruit.id,
                isFavourite = targetIsFavourite,
                debounceDelay = 500.milliseconds
            )
        }
    }

    fun removeFromFavourite(fruitId: FruitId, errorHandler: ErrorHandler) {
        jobLauncher.launchJob(fruitId, false, errorHandler) {
            favouriteRepository.removeFromFavourite(fruitId)
        }
    }
}