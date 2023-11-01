package me.aartikov.replica.advanced_sample.features.fruits.ui

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import me.aartikov.replica.advanced_sample.core.error_handling.ErrorHandler
import me.aartikov.replica.advanced_sample.core.error_handling.safeLaunch
import me.aartikov.replica.advanced_sample.core.utils.observe
import me.aartikov.replica.advanced_sample.features.fruits.domain.Fruit
import me.aartikov.replica.advanced_sample.features.fruits.domain.FruitId
import me.aartikov.replica.advanced_sample.features.fruits.domain.ToggleFruitLikeInteractor
import me.aartikov.replica.single.Replica

class RealFruitsComponent(
    componentContext: ComponentContext,
    private val fruitsReplica: Replica<List<Fruit>>,
    private val toggleFruitLikeInteractor: ToggleFruitLikeInteractor,
    private val applicationCoroutineScope: CoroutineScope,
    private val errorHandler: ErrorHandler
) : ComponentContext by componentContext, FruitsComponent {

    override val fruitsState = fruitsReplica.observe(lifecycle, errorHandler)

    private val toggleLikeJobs = mutableMapOf<FruitId, Job>()

    override fun onFruitClick(fruitId: FruitId) {
        val fruit = fruitsState.value.data?.find { it.id == fruitId } ?: return
        toggleLikeJobs[fruitId]?.cancel()
        toggleLikeJobs[fruitId] = applicationCoroutineScope.safeLaunch(errorHandler) {
            toggleFruitLikeInteractor.execute(fruit)
        }
    }

    override fun onRefresh() {
        fruitsReplica.refresh()
    }

    override fun onRetryClick() {
        fruitsReplica.refresh()
    }
}