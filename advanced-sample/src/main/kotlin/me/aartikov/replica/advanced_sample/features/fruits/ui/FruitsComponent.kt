package me.aartikov.replica.advanced_sample.features.fruits.ui

import me.aartikov.replica.advanced_sample.features.fruits.domain.Fruit
import me.aartikov.replica.advanced_sample.features.fruits.domain.FruitId
import me.aartikov.replica.single.Loadable

interface FruitsComponent {

    val fruitsState: Loadable<List<Fruit>>

    fun onFruitClick(fruitId: FruitId)

    fun onRefresh()

    fun onRetryClick()
}