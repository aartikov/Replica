package me.aartikov.replica.advanced_sample.features.fruits.data.all_fruits

import me.aartikov.replica.advanced_sample.features.fruits.domain.Fruit
import me.aartikov.replica.single.Replica

interface AllFruitsRepository {
    val fruitsReplica: Replica<List<Fruit>>
}