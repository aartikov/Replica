package me.aartikov.replica.sample.features.fruits.data

import me.aartikov.replica.sample.features.fruits.domain.Fruit
import me.aartikov.replica.sample.features.fruits.domain.FruitId
import me.aartikov.replica.single.Replica

interface FruitRepository {

    val fruitsReplica: Replica<List<Fruit>>

    suspend fun setFruitLiked(fruitId: FruitId, liked: Boolean)
}