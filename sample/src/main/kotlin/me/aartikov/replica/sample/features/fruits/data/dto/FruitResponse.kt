package me.aartikov.replica.sample.features.fruits.data.dto

import me.aartikov.replica.sample.features.fruits.domain.Fruit
import me.aartikov.replica.sample.features.fruits.domain.FruitId

data class FruitResponse(
    val id: String,
    val name: String,
    val imageUrl: String,
    val liked: Boolean
)

fun FruitResponse.toDomain(): Fruit {
    return Fruit(
        id = FruitId(id),
        name = name,
        imageUrl = imageUrl,
        liked = liked
    )
}