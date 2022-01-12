package me.aartikov.replica.sample.features.dudes.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@JvmInline
value class DudeId(val value: String) : Parcelable

data class Dude(
    val id: DudeId,
    val name: String,
    val photoUrl: String
)