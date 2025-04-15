package me.aartikov.replica.advanced_sample.features.fruits.domain

import kotlinx.coroutines.CoroutineScope
import me.aartikov.replica.advanced_sample.core.error_handling.ErrorHandler
import me.aartikov.replica.advanced_sample.core.utils.CancellingJobLauncher
import me.aartikov.replica.advanced_sample.features.fruits.data.favourite.FavouriteFruitsRepository
import kotlin.time.Duration.Companion.milliseconds

class FruitFavouriteUpdater(
    applicationCoroutineScope: CoroutineScope,
    private val favouriteRepository: FavouriteFruitsRepository
) {
    private val jobLauncher = CancellingJobLauncher<FruitId, Boolean>(applicationCoroutineScope)

    val inProgressRequestIds get() = jobLauncher.inProgressRequestIds

    fun setFruitFavourite(fruitId: FruitId, isFavourite: Boolean, errorHandler: ErrorHandler) {
        jobLauncher.launchJob(fruitId, isFavourite, errorHandler) {
            favouriteRepository.setFruitFavourite(
                fruitId = fruitId,
                isFavourite = isFavourite,
                debounceDelay = 500.milliseconds
            )
        }
    }
}