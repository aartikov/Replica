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

    fun toggleFruitLiked(fruit: Fruit, errorHandler: ErrorHandler) {
        val targetLiked = !fruit.liked
        jobLauncher.launchJob(fruit.id, targetLiked, errorHandler) {
            fruitRepository.setFruitLiked(
                fruitId = fruit.id,
                liked = targetLiked,
                debounceDelay = 500.milliseconds
            )
        }
    }
}