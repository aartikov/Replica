package me.aartikov.replica.advanced_sample.features.fruits.ui.all

import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.advanced_sample.features.fruits.domain.Fruit
import me.aartikov.replica.advanced_sample.features.fruits.domain.FruitId
import me.aartikov.replica.single.Loadable

interface FruitsAllComponent {
    val fruitsState: StateFlow<Loadable<List<Fruit>>>

    fun onFruitClick(fruitId: FruitId)

    fun onRefresh()
    fun onRetryClick()
}