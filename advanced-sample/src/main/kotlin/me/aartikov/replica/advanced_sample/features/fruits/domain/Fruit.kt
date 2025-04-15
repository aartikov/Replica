package me.aartikov.replica.advanced_sample.features.fruits.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@JvmInline
value class FruitId(val value: String) : Parcelable

data class Fruit(
    val id: FruitId,
    val name: String,
    val imageUrl: String,
    val isFavourite: Boolean
)

fun List<Fruit>.withUpdatedIsFavourite(id: FruitId, isFavourite: Boolean) = map {
    if (it.id == id) it.copy(isFavourite = isFavourite) else it
}