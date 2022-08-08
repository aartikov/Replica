package me.aartikov.replica.advanced_sample.features.fruits.domain

import me.aartikov.replica.advanced_sample.features.fruits.data.FruitRepository

class ToggleFruitLikeInteractor(
    private val fruitRepository: FruitRepository
) {

    suspend fun execute(fruit: Fruit) {
        fruitRepository.setFruitLiked(fruitId = fruit.id, liked = !fruit.liked)
    }
}