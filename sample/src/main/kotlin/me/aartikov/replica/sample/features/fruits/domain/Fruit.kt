package me.aartikov.replica.sample.features.fruits.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@JvmInline
value class FruitId(val value: String) : Parcelable

data class Fruit(
    val id: FruitId,
    val name: String,
    val imageUrl: String,
    val liked: Boolean
)