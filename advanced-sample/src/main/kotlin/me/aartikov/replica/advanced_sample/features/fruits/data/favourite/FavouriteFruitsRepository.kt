package me.aartikov.replica.advanced_sample.features.fruits.data.favourite

import me.aartikov.replica.advanced_sample.features.fruits.domain.Fruit
import me.aartikov.replica.advanced_sample.features.fruits.domain.FruitId
import me.aartikov.replica.single.Replica

interface FavouriteFruitsRepository {
    val favouriteFruitsReplica: Replica<List<Fruit>>

    suspend fun removeFromFavourite(fruitId: FruitId)
}
