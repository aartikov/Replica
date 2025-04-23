package me.aartikov.replica.advanced_sample.features.fruits.domain

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class FruitId(val value: String)

data class Fruit(
    val id: FruitId,
    val name: String,
    val imageUrl: String,
    val isFavourite: Boolean
)

fun List<Fruit>.withUpdatedIsFavourite(id: FruitId, isFavourite: Boolean) = map {
    if (it.id == id) it.copy(isFavourite = isFavourite) else it
}