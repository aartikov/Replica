package me.aartikov.replica.advanced_sample.features.fruits.data

import me.aartikov.replica.advanced_sample.features.fruits.domain.FruitId
import me.aartikov.replica.common.ReplicaAction

data class SetFruitFavouriteAction(
    val fruitId: FruitId,
    val isFavourite: Boolean
) : ReplicaAction
