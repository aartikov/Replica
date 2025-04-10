package me.aartikov.replica.advanced_sample.features.fruits.ui.favourites

import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.advanced_sample.features.fruits.domain.Fruit
import me.aartikov.replica.advanced_sample.features.fruits.domain.FruitId
import me.aartikov.replica.single.Loadable

interface FruitsFavouritesComponent {
    val fruitsState: StateFlow<Loadable<List<Fruit>>>
    val removingInProgress: StateFlow<Set<FruitId>>

    fun onRemoveFruitClick(fruitId: FruitId)

    fun onRefresh()
    fun onRetryClick()
}