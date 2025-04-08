package me.aartikov.replica.advanced_sample.features.fruits.domain

import kotlinx.coroutines.CoroutineScope
import me.aartikov.replica.advanced_sample.core.error_handling.ErrorHandler
import me.aartikov.replica.advanced_sample.core.utils.CancellingJobLauncher
import me.aartikov.replica.advanced_sample.features.fruits.data.FruitRepository
import kotlin.time.Duration.Companion.milliseconds

class FruitLikeUpdater(
    applicationCoroutineScope: CoroutineScope,
    private val fruitRepository: FruitRepository
) {

    private val jobLauncher = CancellingJobLauncher<FruitId, Boolean>(applicationCoroutineScope)

    fun setFruitLiked(fruitId: FruitId, liked: Boolean, errorHandler: ErrorHandler) {
        jobLauncher.launchJob(fruitId, targetState = liked, errorHandler) {
            fruitRepository.setFruitLiked(
                fruitId = fruitId,
                liked = liked,
                debounceDelay = 500.milliseconds
            )
        }
    }
}