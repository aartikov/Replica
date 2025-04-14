package me.aartikov.replica.advanced_sample.features.fruits.data.favourite

import me.aartikov.replica.advanced_sample.features.fruits.domain.Fruit
import me.aartikov.replica.advanced_sample.features.fruits.domain.FruitId
import me.aartikov.replica.single.Replica
import kotlin.time.Duration

interface FavouriteFruitsRepository {

    val favouriteFruitsReplica: Replica<List<Fruit>>

    suspend fun setFruitFavourite(fruitId: FruitId, isFavourite: Boolean, debounceDelay: Duration)
}
