package me.aartikov.replica.advanced_sample.features.fruits.data.dto

import me.aartikov.replica.advanced_sample.features.fruits.domain.Fruit
import me.aartikov.replica.advanced_sample.features.fruits.domain.FruitId

data class FruitResponse(
    val id: String,
    val name: String,
    val imageUrl: String,
    val isFavourite: Boolean
)

fun FruitResponse.toDomain(): Fruit {
    return Fruit(
        id = FruitId(id),
        name = name,
        imageUrl = imageUrl,
        isFavourite = isFavourite
    )
}